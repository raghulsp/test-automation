package com.framework.drivers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DeviceManager {

    private static final ConcurrentLinkedQueue<Device> devicePool = new ConcurrentLinkedQueue<>();
    private static final ThreadLocal<Device> assignedDevice = new ThreadLocal<>();

    static {
        try (InputStream is = DeviceManager.class
                .getClassLoader()
                .getResourceAsStream("devices.json")) {
            if (is == null) throw new RuntimeException("devices.json not found on classpath");
            ObjectMapper mapper = JsonMapper.builder()
                    .enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS)
                    .build();
            List<Device> devices = mapper.readValue(is, new TypeReference<>() {});
            devicePool.addAll(devices);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load devices.json", e);
        }
    }

    public static Device acquireDevice() {
        Device device = devicePool.poll();
        if (device == null) throw new RuntimeException("No available devices in pool — all in use");
        assignedDevice.set(device);
        return device;
    }

    public static void releaseDevice() {
        Device device = assignedDevice.get();
        if (device != null) {
            devicePool.offer(device);
            assignedDevice.remove();
        }
    }

    public static Device currentDevice() {
        return assignedDevice.get();
    }
}
