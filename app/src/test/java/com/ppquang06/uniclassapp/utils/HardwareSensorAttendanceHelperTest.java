package com.ppquang06.uniclassapp.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class HardwareSensorAttendanceHelperTest {

    @Test
    public void testFallbackConstants() {
        assertEquals("00:11:22:33:44:55", HardwareSensorAttendanceHelper.DEFAULT_FALLBACK_BSSID);
        assertEquals(10.8700, HardwareSensorAttendanceHelper.DEFAULT_FALLBACK_LAT, 0.0001);
        assertEquals(106.8031, HardwareSensorAttendanceHelper.DEFAULT_FALLBACK_LNG, 0.0001);
    }

    @Test
    public void testNullContextBssidFallback() {
        String bssid = HardwareSensorAttendanceHelper.getBssid(null);
        assertEquals(HardwareSensorAttendanceHelper.DEFAULT_FALLBACK_BSSID, bssid);
    }

    @Test
    public void testNullContextDeviceIdFallback() {
        String deviceId = HardwareSensorAttendanceHelper.getDeviceId(null);
        assertNotNull(deviceId);
        assertEquals("android-student-device-01", deviceId);
    }

    @Test
    public void testNullContextLocationFallback() {
        AtomicBoolean called = new AtomicBoolean(false);
        HardwareSensorAttendanceHelper.getCurrentLocation(null, (lat, lng) -> {
            called.set(true);
            assertEquals(HardwareSensorAttendanceHelper.DEFAULT_FALLBACK_LAT, lat, 0.0001);
            assertEquals(HardwareSensorAttendanceHelper.DEFAULT_FALLBACK_LNG, lng, 0.0001);
        });
        assertEquals(true, called.get());
    }
}
