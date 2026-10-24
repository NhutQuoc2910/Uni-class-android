package com.ppquang06.uniclassapp.data.model.response;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName(value = "accessToken", alternate = {"token"})
    private String token;

    @SerializedName(value = "tokenType", alternate = {"type"})
    private String type;

    @SerializedName("user")
    private UserResponse user;

    public AuthResponse() {}

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }
}
