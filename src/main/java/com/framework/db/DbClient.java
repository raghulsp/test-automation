package com.framework.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Thin JDBC wrapper using PreparedStatement for all parameterized queries.
 * Each public method opens and closes its own connection, making it safe for
 * parallel test execution without external connection management.
 *
 * Column names in returned maps are lowercased so callers can use either case.
 */
public class DbClient {

    private static final Logger log = LoggerFactory.getLogger(DbClient.class);

    private final String url;
    private final String username;
    private final String password;

    /** Reads connection details from db.properties (or db/<env>.properties via -Ddb.env=). */
    public DbClient() {
        this(DbConfig.get("db.url"), DbConfig.get("db.username"), DbConfig.get("db.password"));
        String driver = DbConfig.get("db.driver", "");
        if (!driver.isEmpty()) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("JDBC driver not found: " + driver, e);
            }
        }
    }

    /** Explicit credentials — useful when overriding per-test or injecting from secrets. */
    public DbClient(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    // -------------------------------------------------------------------------
    // Query methods
    // -------------------------------------------------------------------------

    /**
     * Executes a parameterized SELECT and returns every row as a map of
     * column-name (lowercase) → value. Returns an empty list when no rows match.
     */
    public List<Map<String, Object>> queryRows(String sql, Object... params) {
        log.debug("queryRows sql=[{}] params={}", sql, params);
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return extractRows(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("queryRows failed: " + sql, e);
        }
    }

    /**
     * Returns the first row of the result, or {@code null} when the query returns no rows.
     * Use this when exactly one row is expected — combine with {@link DbAssertions#assertRowCount}
     * if you need to assert that there is exactly one match.
     */
    public Map<String, Object> queryRow(String sql, Object... params) {
        List<Map<String, Object>> rows = queryRows(sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * Returns the value of the first column in the first row (e.g. for COUNT/MAX/MIN queries),
     * or {@code null} when the query returns no rows.
     */
    public Object queryScalar(String sql, Object... params) {
        Map<String, Object> row = queryRow(sql, params);
        if (row == null || row.isEmpty()) return null;
        return row.values().iterator().next();
    }

    // -------------------------------------------------------------------------
    // Mutation methods
    // -------------------------------------------------------------------------

    /**
     * Executes a parameterized INSERT / UPDATE / DELETE and returns the affected-row count.
     */
    public int executeUpdate(String sql, Object... params) {
        log.debug("executeUpdate sql=[{}] params={}", sql, params);
        try (Connection conn = openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindParams(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("executeUpdate failed: " + sql, e);
        }
    }

    /**
     * Executes the same DML statement for each element of {@code paramBatches} in a single
     * transaction. Rolls back the entire batch on any failure.
     *
     * @param sql          parameterized DML statement
     * @param paramBatches each {@code Object[]} is one row's worth of bind values
     * @return per-row affected-row counts from {@link PreparedStatement#executeBatch()}
     */
    public int[] executeBatch(String sql, List<Object[]> paramBatches) {
        log.debug("executeBatch sql=[{}] batches={}", sql, paramBatches.size());
        try (Connection conn = openConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Object[] params : paramBatches) {
                    bindParams(ps, params);
                    ps.addBatch();
                }
                int[] counts = ps.executeBatch();
                conn.commit();
                return counts;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("executeBatch failed: " + sql, e);
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Binds positional parameters to a PreparedStatement.
     * Handles: null, String, Integer, Long, Double, Float, Boolean, LocalDate, LocalDateTime.
     * Falls back to {@link PreparedStatement#setObject} for any other type.
     */
    private void bindParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            int pos = i + 1;
            Object p = params[i];
            if (p == null) {
                ps.setNull(pos, Types.NULL);
            } else if (p instanceof String s) {
                ps.setString(pos, s);
            } else if (p instanceof Integer n) {
                ps.setInt(pos, n);
            } else if (p instanceof Long n) {
                ps.setLong(pos, n);
            } else if (p instanceof Double d) {
                ps.setDouble(pos, d);
            } else if (p instanceof Float f) {
                ps.setFloat(pos, f);
            } else if (p instanceof Boolean b) {
                ps.setBoolean(pos, b);
            } else if (p instanceof LocalDate d) {
                ps.setDate(pos, java.sql.Date.valueOf(d));
            } else if (p instanceof LocalDateTime dt) {
                ps.setTimestamp(pos, Timestamp.valueOf(dt));
            } else {
                ps.setObject(pos, p);
            }
        }
    }

    /**
     * Iterates the full ResultSet and converts every row to a LinkedHashMap.
     * Column names are lowercased; column aliases (from AS) are used when present.
     */
    private List<Map<String, Object>> extractRows(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        List<Map<String, Object>> rows = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>(cols);
            for (int c = 1; c <= cols; c++) {
                row.put(meta.getColumnLabel(c).toLowerCase(), rs.getObject(c));
            }
            rows.add(row);
        }
        return rows;
    }
}
