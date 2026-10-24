package com.ppquang06.uniclassapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.ppquang06.uniclassapp.data.model.response.UserResponse;

public class SessionManager {
    private static volatile SessionManager instance;
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    private SessionManager(Context context) {
        pref = context.getApplicationContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void saveAuth(String token, UserResponse user) {
        editor.putString(Constants.KEY_TOKEN, token);
        if (user != null) {
            if (user.getId() != null) editor.putLong(Constants.KEY_USER_ID, user.getId());
            editor.putString(Constants.KEY_USER_NAME, user.getFullName());
            editor.putString(Constants.KEY_USER_EMAIL, user.getEmail());
            editor.putString(Constants.KEY_USER_ROLE, user.getRole());
            editor.putString(Constants.KEY_USER_AVATAR, user.getAvatarUrl());
            editor.putString(Constants.KEY_USER_PHONE, user.getPhone());
        }
        editor.apply();
    }

    public String getToken() {
        return pref.getString(Constants.KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public UserResponse getUser() {
        if (!isLoggedIn()) {
            return null;
        }
        UserResponse user = new UserResponse();
        long id = pref.getLong(Constants.KEY_USER_ID, -1L);
        if (id != -1L) {
            user.setId(id);
        }
        user.setFullName(pref.getString(Constants.KEY_USER_NAME, ""));
        user.setEmail(pref.getString(Constants.KEY_USER_EMAIL, ""));
        user.setRole(pref.getString(Constants.KEY_USER_ROLE, "STUDENT"));
        user.setAvatarUrl(pref.getString(Constants.KEY_USER_AVATAR, null));
        user.setPhone(pref.getString(Constants.KEY_USER_PHONE, ""));
        return user;
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
