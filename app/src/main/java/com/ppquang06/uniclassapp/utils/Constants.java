package com.ppquang06.uniclassapp.utils;

public final class Constants {
    private Constants() {
    }

    // Android emulator loops back to localhost via 10.0.2.2
    public static final String BASE_URL = "http://70.153.85.133:8080/api/v1/";

    // Shared Preferences Keys
    public static final String PREF_NAME = "uniclass_session";
    public static final String KEY_TOKEN = "jwt_token";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_USER_ROLE = "user_role";
    public static final String KEY_USER_AVATAR = "user_avatar";
    public static final String KEY_USER_PHONE = "user_phone";

    // Intent extras
    public static final String EXTRA_EMAIL = "extra_email";
    public static final String EXTRA_OTP = "extra_otp";
}
