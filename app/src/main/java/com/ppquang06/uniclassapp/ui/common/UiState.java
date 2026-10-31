package com.ppquang06.uniclassapp.ui.common;

import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Generic wrapper representing the state of UI operations (e.g. API requests).
 *
 * @param <T> Payload data type.
 */
public class UiState<T> {

    public enum Status {
        IDLE,
        LOADING,
        SUCCESS,
        ERROR
    }

    private final Status status;
    @Nullable
    private final T data;
    @Nullable
    private final String message;
    @Nullable
    private final Throwable error;

    public UiState(Status status, @Nullable T data, @Nullable String message, @Nullable Throwable error) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.error = error;
    }

    // Factory methods
    public static <T> UiState<T> idle() {
        return new UiState<>(Status.IDLE, null, null, null);
    }

    public static <T> UiState<T> loading() {
        return new UiState<>(Status.LOADING, null, null, null);
    }

    public static <T> UiState<T> loading(@Nullable T cachedData) {
        return new UiState<>(Status.LOADING, cachedData, null, null);
    }

    public static <T> UiState<T> success(@Nullable T data) {
        return new UiState<>(Status.SUCCESS, data, null, null);
    }

    public static <T> UiState<T> error(String message) {
        return new UiState<>(Status.ERROR, null, message, null);
    }

    public static <T> UiState<T> error(String message, @Nullable Throwable error) {
        return new UiState<>(Status.ERROR, null, message, error);
    }

    public static <T> UiState<T> error(String message, @Nullable T fallbackData) {
        return new UiState<>(Status.ERROR, fallbackData, message, null);
    }

    public static <T> UiState<T> error(String message, @Nullable Throwable error, @Nullable T fallbackData) {
        return new UiState<>(Status.ERROR, fallbackData, message, error);
    }

    // Helpers
    public boolean isIdle() {
        return status == Status.IDLE;
    }

    public boolean isLoading() {
        return status == Status.LOADING;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }

    // Getters
    public Status getStatus() {
        return status;
    }

    @Nullable
    public T getData() {
        return data;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @Nullable
    public Throwable getError() {
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UiState<?> uiState = (UiState<?>) o;
        return status == uiState.status &&
                Objects.equals(data, uiState.data) &&
                Objects.equals(message, uiState.message) &&
                Objects.equals(error, uiState.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, data, message, error);
    }

    @Override
    public String toString() {
        return "UiState{" +
                "status=" + status +
                ", data=" + data +
                ", message='" + message + '\'' +
                ", error=" + error +
                '}';
    }
}
