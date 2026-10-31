package com.ppquang06.uniclassapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.ppquang06.uniclassapp.ui.common.UiState;

import org.junit.Test;

public class UiStateTest {

    @Test
    public void idleFactory_createsIdleState() {
        UiState<String> state = UiState.idle();

        assertEquals(UiState.Status.IDLE, state.getStatus());
        assertTrue(state.isIdle());
        assertFalse(state.isLoading());
        assertFalse(state.isSuccess());
        assertFalse(state.isError());

        assertNull(state.getData());
        assertNull(state.getMessage());
        assertNull(state.getError());
    }

    @Test
    public void loadingFactory_withoutData_createsLoadingState() {
        UiState<String> state = UiState.loading();

        assertEquals(UiState.Status.LOADING, state.getStatus());
        assertTrue(state.isLoading());
        assertFalse(state.isIdle());
        assertFalse(state.isSuccess());
        assertFalse(state.isError());

        assertNull(state.getData());
        assertNull(state.getMessage());
        assertNull(state.getError());
    }

    @Test
    public void loadingFactory_withCachedData_preservesData() {
        String cached = "cached payload";
        UiState<String> state = UiState.loading(cached);

        assertEquals(UiState.Status.LOADING, state.getStatus());
        assertTrue(state.isLoading());
        assertEquals(cached, state.getData());
        assertNull(state.getMessage());
        assertNull(state.getError());
    }

    @Test
    public void successFactory_createsSuccessStateWithData() {
        String data = "success data";
        UiState<String> state = UiState.success(data);

        assertEquals(UiState.Status.SUCCESS, state.getStatus());
        assertTrue(state.isSuccess());
        assertFalse(state.isIdle());
        assertFalse(state.isLoading());
        assertFalse(state.isError());

        assertEquals(data, state.getData());
        assertNull(state.getMessage());
        assertNull(state.getError());
    }

    @Test
    public void errorFactory_withMessageOnly_createsErrorState() {
        String msg = "Connection timed out";
        UiState<String> state = UiState.error(msg);

        assertEquals(UiState.Status.ERROR, state.getStatus());
        assertTrue(state.isError());
        assertFalse(state.isIdle());
        assertFalse(state.isLoading());
        assertFalse(state.isSuccess());

        assertEquals(msg, state.getMessage());
        assertNull(state.getData());
        assertNull(state.getError());
    }

    @Test
    public void errorFactory_withMessageAndThrowable_preservesBoth() {
        String msg = "Failed to load classes";
        Throwable error = new RuntimeException("500 Internal Server Error");
        UiState<String> state = UiState.error(msg, error);

        assertEquals(UiState.Status.ERROR, state.getStatus());
        assertTrue(state.isError());
        assertEquals(msg, state.getMessage());
        assertEquals(error, state.getError());
        assertNull(state.getData());
    }

    @Test
    public void errorFactory_withMessageAndFallbackData_preservesBoth() {
        String msg = "Sync failed, showing offline data";
        String fallback = "offline data";
        UiState<String> state = UiState.error(msg, fallback);

        assertEquals(UiState.Status.ERROR, state.getStatus());
        assertTrue(state.isError());
        assertEquals(msg, state.getMessage());
        assertEquals(fallback, state.getData());
        assertNull(state.getError());
    }

    @Test
    public void errorFactory_withMessageThrowableAndFallbackData_preservesAll() {
        String msg = "Sync failed completely";
        Throwable error = new RuntimeException("IO Exception");
        String fallback = "cached fallback";
        UiState<String> state = UiState.error(msg, error, fallback);

        assertEquals(UiState.Status.ERROR, state.getStatus());
        assertTrue(state.isError());
        assertEquals(msg, state.getMessage());
        assertEquals(error, state.getError());
        assertEquals(fallback, state.getData());
    }

    @Test
    public void successFactory_withNullData_createsSuccessStateWithNullData() {
        UiState<Void> state = UiState.success(null);

        assertEquals(UiState.Status.SUCCESS, state.getStatus());
        assertTrue(state.isSuccess());
        assertNull(state.getData());
        assertNull(state.getMessage());
        assertNull(state.getError());
    }

    @Test
    public void equalsAndHashCode_behaveConsistently() {
        UiState<String> state1 = UiState.success("test");
        UiState<String> state2 = UiState.success("test");
        UiState<String> state3 = UiState.error("test");

        assertEquals(state1, state2);
        assertEquals(state1.hashCode(), state2.hashCode());
        assertNotEquals(state1, state3);
        assertNotEquals(null, state1);
        assertEquals(state1, state1);
    }

    @Test
    public void toString_containsKeyFields() {
        UiState<String> state = UiState.success("payload-123");
        String str = state.toString();

        assertNotNull(str);
        assertTrue(str.contains("SUCCESS"));
        assertTrue(str.contains("payload-123"));
    }
}
