package com.framework.db;

import org.testng.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Static assertion helpers for row-level validation of {@link DbClient} results.
 * All methods delegate to TestNG {@link Assert} so failures appear as normal test failures.
 */
public final class DbAssertions {

    private DbAssertions() {}

    // -------------------------------------------------------------------------
    // Result-set level
    // -------------------------------------------------------------------------

    /** Asserts the query returned exactly {@code expected} rows. */
    public static void assertRowCount(List<Map<String, Object>> rows, int expected) {
        Assert.assertEquals(rows.size(), expected,
            "Row count: expected " + expected + " but got " + rows.size());
    }

    /** Asserts the query returned at least one row. */
    public static void assertNotEmpty(List<Map<String, Object>> rows) {
        Assert.assertFalse(rows.isEmpty(), "Expected at least one row but result set is empty");
    }

    /** Asserts the query returned no rows. */
    public static void assertEmpty(List<Map<String, Object>> rows) {
        Assert.assertTrue(rows.isEmpty(),
            "Expected no rows but got " + rows.size());
    }

    // -------------------------------------------------------------------------
    // Row level
    // -------------------------------------------------------------------------

    /**
     * Asserts that {@code row[column]} equals {@code expected}.
     * Comparison is done as {@code String.valueOf} to normalise numeric types
     * (e.g. {@code Long} vs {@code Integer}) returned by different JDBC drivers.
     */
    public static void assertColumnValue(Map<String, Object> row, String column, Object expected) {
        Assert.assertNotNull(row, "Row is null — query may have returned no results");
        Object actual = row.get(column.toLowerCase());
        Assert.assertEquals(String.valueOf(actual), String.valueOf(expected),
            "Column '" + column + "': expected [" + expected + "] but was [" + actual + "]");
    }

    /**
     * Asserts that the string representation of {@code row[column]} contains {@code substring}.
     */
    public static void assertColumnContains(Map<String, Object> row, String column, String substring) {
        Assert.assertNotNull(row, "Row is null — query may have returned no results");
        String actual = String.valueOf(row.get(column.toLowerCase()));
        Assert.assertTrue(actual.contains(substring),
            "Column '" + column + "' value [" + actual + "] does not contain [" + substring + "]");
    }

    /**
     * Asserts that the string representation of {@code row[column]} does not contain {@code substring}.
     */
    public static void assertColumnNotContains(Map<String, Object> row, String column, String substring) {
        Assert.assertNotNull(row, "Row is null — query may have returned no results");
        String actual = String.valueOf(row.get(column.toLowerCase()));
        Assert.assertFalse(actual.contains(substring),
            "Column '" + column + "' value [" + actual + "] should not contain [" + substring + "]");
    }

    /**
     * Asserts that {@code row[column]} is {@code null} or the literal string {@code "null"}.
     */
    public static void assertColumnNull(Map<String, Object> row, String column) {
        Assert.assertNotNull(row, "Row is null — query may have returned no results");
        Object actual = row.get(column.toLowerCase());
        Assert.assertNull(actual,
            "Column '" + column + "' expected null but was [" + actual + "]");
    }

    // -------------------------------------------------------------------------
    // Cross-row searches
    // -------------------------------------------------------------------------

    /**
     * Asserts that at least one row in {@code rows} has {@code column} equal to {@code expected}.
     */
    public static void assertRowExists(List<Map<String, Object>> rows, String column, Object expected) {
        boolean found = rows.stream()
            .anyMatch(r -> Objects.equals(
                String.valueOf(r.get(column.toLowerCase())),
                String.valueOf(expected)));
        Assert.assertTrue(found,
            "No row found where column '" + column + "' = [" + expected + "]");
    }

    /**
     * Asserts that no row in {@code rows} has {@code column} equal to {@code expected}.
     */
    public static void assertRowAbsent(List<Map<String, Object>> rows, String column, Object expected) {
        boolean found = rows.stream()
            .anyMatch(r -> Objects.equals(
                String.valueOf(r.get(column.toLowerCase())),
                String.valueOf(expected)));
        Assert.assertFalse(found,
            "Expected no row where column '" + column + "' = [" + expected + "] but one was found");
    }

    /**
     * Asserts that every row in {@code rows} has {@code column} equal to {@code expected}.
     * Also asserts the list is non-empty (a vacuously-true assertion on an empty list would mask bugs).
     */
    public static void assertAllRowsMatch(List<Map<String, Object>> rows, String column, Object expected) {
        assertNotEmpty(rows);
        rows.forEach(row -> assertColumnValue(row, column, expected));
    }
}
