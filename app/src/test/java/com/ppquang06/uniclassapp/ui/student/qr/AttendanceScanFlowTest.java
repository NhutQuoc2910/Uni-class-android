package com.ppquang06.uniclassapp.ui.student.qr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.os.Bundle;

import com.google.gson.Gson;
import com.ppquang06.uniclassapp.R;
import com.ppquang06.uniclassapp.data.model.request.AttendanceCheckInRequest;
import com.ppquang06.uniclassapp.data.model.response.AttendanceCheckInResponse;

import org.junit.Test;

public class AttendanceScanFlowTest {

    private final Gson gson = new Gson();

    @Test
    public void classifyError_recognizesAllHttpCodesAndErrorNames() {
        assertEquals(StudentScanVerifyingFragment.ERROR_QR_EXPIRED,
                StudentScanVerifyingFragment.classifyError(40010, "", null));
        assertEquals(StudentScanVerifyingFragment.ERROR_QR_EXPIRED,
                StudentScanVerifyingFragment.classifyError(400, "QR_EXPIRED", null));

        assertEquals(StudentScanVerifyingFragment.ERROR_WIFI_MISMATCH,
                StudentScanVerifyingFragment.classifyError(40011, "", null));
        assertEquals(StudentScanVerifyingFragment.ERROR_WIFI_MISMATCH,
                StudentScanVerifyingFragment.classifyError(400, "WIFI_MISMATCH", null));

        assertEquals(StudentScanVerifyingFragment.ERROR_GPS_OUT_OF_RANGE,
                StudentScanVerifyingFragment.classifyError(40012, "", null));
        assertEquals(StudentScanVerifyingFragment.ERROR_GPS_OUT_OF_RANGE,
                StudentScanVerifyingFragment.classifyError(400, "GPS_OUT_OF_RANGE", null));

        assertEquals(StudentScanVerifyingFragment.ERROR_DEVICE_ALREADY_USED,
                StudentScanVerifyingFragment.classifyError(40013, "", null));
        assertEquals(StudentScanVerifyingFragment.ERROR_DEVICE_ALREADY_USED,
                StudentScanVerifyingFragment.classifyError(400, "DEVICE_ALREADY_USED", null));

        assertEquals(StudentScanVerifyingFragment.ERROR_NETWORK,
                StudentScanVerifyingFragment.classifyError(500, "NETWORK_ERROR", null));

        // Simulation mode takes precedence
        assertEquals(StudentScanVerifyingFragment.ERROR_WIFI_MISMATCH,
                StudentScanVerifyingFragment.classifyError(40010, "QR_EXPIRED", "WIFI_MISMATCH"));

        // Fallback
        assertEquals(StudentScanVerifyingFragment.ERROR_QR_EXPIRED,
                StudentScanVerifyingFragment.classifyError(500, "UNKNOWN_ERROR", null));
    }

    @Test
    public void resolveErrorConfig_qrExpiredShowsFailedOnStep1() {
        StudentScanResultFragment.ErrorConfig config =
                StudentScanResultFragment.resolveErrorConfig(StudentScanVerifyingFragment.ERROR_QR_EXPIRED);

        assertEquals(R.string.error_qr_expired_title, config.titleRes);
        assertEquals(R.string.error_qr_expired_desc, config.subtitleRes);
        assertEquals(R.string.error_qr_expired_advice, config.adviceRes);
        assertEquals(R.drawable.ic_clock, config.iconRes);
        assertEquals(R.color.color_error, config.iconTintRes);
        assertEquals(R.drawable.bg_circle_red_light, config.heroBgRes);

        assertEquals(StudentScanVerifyingFragment.STEP_STATE_FAILED, config.step1State);
        assertEquals(StudentScanVerifyingFragment.STEP_STATE_WAITING, config.step2State);
        assertEquals(StudentScanVerifyingFragment.STEP_STATE_WAITING, config.step3State);
    }

    @Test
    public void resolveErrorConfig_wifiMismatchShowsPass1AndFail2() {
        StudentScanResultFragment.ErrorConfig config =
                StudentScanResultFragment.resolveErrorConfig(StudentScanVerifyingFragment.ERROR_WIFI_MISMATCH);

        assertEquals(R.string.error_wifi_mismatch_title, config.titleRes);
        assertEquals(R.string.error_wifi_mismatch_desc, config.subtitleRes);
        assertEquals(R.string.error_wifi_mismatch_advice, config.adviceRes);
        assertEquals(R.drawable.ic_wifi, config.iconRes);
        assertEquals(R.color.color_warning, config.iconTintRes);
        assertEquals(R.drawable.bg_circle_amber_light, config.heroBgRes);

        assertEquals(StudentScanVerifyingFragment.STEP_STATE_PASSED, config.step1State);
        assertEquals(StudentScanVerifyingFragment.STEP_STATE_FAILED, config.step2State);
        assertEquals(StudentScanVerifyingFragment.STEP_STATE_WAITING, config.step3State);
    }

    @Test
    public void resolveErrorConfig_gpsOutOfRangeShowsPass1AndPass2AndFail3() {
        StudentScanResultFragment.ErrorConfig config =
                StudentScanResultFragment.resolveErrorConfig(StudentScanVerifyingFragment.ERROR_GPS_OUT_OF_RANGE);

        assertEquals(R.string.error_gps_out_title, config.titleRes);
        assertEquals(R.string.error_gps_out_desc, config.subtitleRes);
        assertEquals(R.string.error_gps_out_advice, config.adviceRes);
        assertEquals(R.drawable.ic_gps, config.iconRes);
        assertEquals(R.color.color_error, config.iconTintRes);
        assertEquals(R.drawable.bg_circle_red_light, config.heroBgRes);

        assertEquals(StudentScanVerifyingFragment.STEP_STATE_PASSED, config.step1State);
        assertEquals(StudentScanVerifyingFragment.STEP_STATE_PASSED, config.step2State);
        assertEquals(StudentScanVerifyingFragment.STEP_STATE_FAILED, config.step3State);
    }

    @Test
    public void resolveErrorConfig_deviceAlreadyUsedShowsPass1AndPass2AndFail3() {
        StudentScanResultFragment.ErrorConfig config =
                StudentScanResultFragment.resolveErrorConfig(StudentScanVerifyingFragment.ERROR_DEVICE_ALREADY_USED);

        assertEquals(R.string.error_device_used_title, config.titleRes);
        assertEquals(R.string.error_device_used_desc, config.subtitleRes);
        assertEquals(R.string.error_device_used_advice, config.adviceRes);
        assertEquals(R.drawable.ic_smartphone, config.iconRes);
        assertEquals(R.color.color_error, config.iconTintRes);
        assertEquals(R.drawable.bg_circle_red_light, config.heroBgRes);

        assertEquals(StudentScanVerifyingFragment.STEP_STATE_PASSED, config.step1State);
        assertEquals(StudentScanVerifyingFragment.STEP_STATE_PASSED, config.step2State);
        assertEquals(StudentScanVerifyingFragment.STEP_STATE_FAILED, config.step3State);
    }

    @Test
    public void resolveErrorConfig_networkErrorShowsFailedStep1() {
        StudentScanResultFragment.ErrorConfig config =
                StudentScanResultFragment.resolveErrorConfig(StudentScanVerifyingFragment.ERROR_NETWORK);

        assertEquals(R.string.error_network_title, config.titleRes);
        assertEquals(R.string.error_network_desc, config.subtitleRes);
        assertEquals(R.string.error_network_advice, config.adviceRes);
        assertEquals(R.drawable.ic_error_circle, config.iconRes);
        assertEquals(R.color.color_error, config.iconTintRes);
        assertEquals(R.drawable.bg_circle_red_light, config.heroBgRes);

        assertEquals(StudentScanVerifyingFragment.STEP_STATE_FAILED, config.step1State);
        assertEquals(StudentScanVerifyingFragment.STEP_STATE_WAITING, config.step2State);
        assertEquals(StudentScanVerifyingFragment.STEP_STATE_WAITING, config.step3State);
    }

    @Test
    public void requestAndResponseDtoSerialization_roundtripsCorrectly() {
        AttendanceCheckInRequest request = new AttendanceCheckInRequest(
                "DEMO_OPEN",
                "00:11:22:33:44:55",
                10.8700,
                106.8031,
                "dev-12345"
        );
        String json = gson.toJson(request);
        AttendanceCheckInRequest parsedReq = gson.fromJson(json, AttendanceCheckInRequest.class);
        assertEquals("DEMO_OPEN", parsedReq.getSessionId());
        assertEquals("00:11:22:33:44:55", parsedReq.getWifiBssid());
        assertEquals(Double.valueOf(10.8700), parsedReq.getLatitude());
        assertEquals(Double.valueOf(106.8031), parsedReq.getLongitude());
        assertEquals("dev-12345", parsedReq.getDeviceId());

        AttendanceCheckInResponse response = new AttendanceCheckInResponse(
                "rec-01",
                "DEMO_OPEN",
                "NT118.Q11",
                "Phát triển ứng dụng di động",
                "B1.12",
                "ThS. Trần Minh Khoa",
                "21:30 · 28/10/2026",
                "dev-12345",
                "PRESENT",
                true,
                true,
                true,
                12.5
        );
        String resJson = gson.toJson(response);
        AttendanceCheckInResponse parsedRes = gson.fromJson(resJson, AttendanceCheckInResponse.class);
        assertEquals("rec-01", parsedRes.getRecordId());
        assertEquals("NT118.Q11", parsedRes.getClassCode());
        assertEquals("B1.12", parsedRes.getRoom());
        assertEquals("PRESENT", parsedRes.getStatus());
        assertTrue(parsedRes.isQrVerified());
        assertTrue(parsedRes.isWifiVerified());
        assertTrue(parsedRes.isGpsVerified());
        assertEquals(12.5, parsedRes.getDistanceMeters(), 0.001);
    }
}
