package com.ppquang06.uniclassapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;
import androidx.lifecycle.MutableLiveData;

import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.ui.common.BaseViewModel;
import com.ppquang06.uniclassapp.ui.common.UiState;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.Application;

public class BaseViewModelTest {

    private static class TestViewModel extends BaseViewModel {
        public TestViewModel(@NonNull Application application) {
            super(application);
        }

        public void invokeOnCleared() {
            onCleared();
        }
    }

    private static class FakeCall<T> implements Call<ApiResponse<T>> {
        private boolean canceled = false;
        private boolean executed = false;
        private Callback<ApiResponse<T>> callback;

        @Override
        public Response<ApiResponse<T>> execute() throws IOException {
            executed = true;
            return null;
        }

        @Override
        public void enqueue(Callback<ApiResponse<T>> callback) {
            this.callback = callback;
        }

        @Override
        public boolean isExecuted() {
            return executed;
        }

        @Override
        public void cancel() {
            canceled = true;
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public Call<ApiResponse<T>> clone() {
            return this;
        }

        @Override
        public Request request() {
            return new Request.Builder().url("http://localhost/").build();
        }

        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }

        public Callback<ApiResponse<T>> getCallback() {
            return callback;
        }
    }

    private TestViewModel viewModel;

    @Before
    public void setUp() {
        ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
            @Override
            public void executeOnDiskIO(@NonNull Runnable runnable) {
                runnable.run();
            }

            @Override
            public void postToMainThread(@NonNull Runnable runnable) {
                runnable.run();
            }

            @Override
            public boolean isMainThread() {
                return true;
            }
        });
        viewModel = new TestViewModel(new Application());
    }

    @After
    public void tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null);
    }

    @Test
    public void executeCall_addsCallToActiveCalls_andEmitsLoadingState() {
        FakeCall<String> fakeCall = new FakeCall<>();
        MutableLiveData<UiState<String>> liveData = new MutableLiveData<>();

        viewModel.executeCall(fakeCall, liveData);

        assertEquals(1, viewModel.getActiveCalls().size());
        assertTrue(viewModel.getActiveCalls().contains(fakeCall));
        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isLoading());
    }

    @Test
    public void executeCall_onSuccessResponse_emitsSuccessState_andRemovesFromActiveCalls() {
        FakeCall<String> fakeCall = new FakeCall<>();
        MutableLiveData<UiState<String>> liveData = new MutableLiveData<>();

        viewModel.executeCall(fakeCall, liveData);

        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(true);
        apiResponse.setData("Server Data Loaded");
        Response<ApiResponse<String>> retrofitResponse = Response.success(apiResponse);

        fakeCall.getCallback().onResponse(fakeCall, retrofitResponse);

        assertEquals(0, viewModel.getActiveCalls().size());
        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isSuccess());
        assertEquals("Server Data Loaded", liveData.getValue().getData());
    }

    @Test
    public void executeCall_onErrorResponseBody_parsesMessageAndEmitsErrorState() {
        FakeCall<String> fakeCall = new FakeCall<>();
        MutableLiveData<UiState<String>> liveData = new MutableLiveData<>();

        viewModel.executeCall(fakeCall, liveData, "Fallback message");

        String errorJson = "{\"success\":false,\"code\":400,\"message\":\"Lớp học không tồn tại\"}";
        ResponseBody errorBody = ResponseBody.create(MediaType.parse("application/json"), errorJson);
        Response<ApiResponse<String>> retrofitResponse = Response.error(400, errorBody);

        fakeCall.getCallback().onResponse(fakeCall, retrofitResponse);

        assertEquals(0, viewModel.getActiveCalls().size());
        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isError());
        assertEquals("Lớp học không tồn tại", liveData.getValue().getMessage());
    }

    @Test
    public void executeCall_onUnparseableError_usesDefaultErrorMessage() {
        FakeCall<String> fakeCall = new FakeCall<>();
        MutableLiveData<UiState<String>> liveData = new MutableLiveData<>();

        String customDefault = "Không thể tải dữ liệu lớp học";
        viewModel.executeCall(fakeCall, liveData, customDefault);

        ResponseBody errorBody = ResponseBody.create(MediaType.parse("text/html"), "<html>502 Bad Gateway</html>");
        Response<ApiResponse<String>> retrofitResponse = Response.error(502, errorBody);

        fakeCall.getCallback().onResponse(fakeCall, retrofitResponse);

        assertEquals(0, viewModel.getActiveCalls().size());
        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isError());
        assertEquals(customDefault, liveData.getValue().getMessage());
    }

    @Test
    public void executeCall_onFailure_emitsErrorWithThrowable() {
        FakeCall<String> fakeCall = new FakeCall<>();
        MutableLiveData<UiState<String>> liveData = new MutableLiveData<>();

        viewModel.executeCall(fakeCall, liveData, "Default Error");

        IOException networkError = new IOException("Connection refused");
        fakeCall.getCallback().onFailure(fakeCall, networkError);

        assertEquals(0, viewModel.getActiveCalls().size());
        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isError());
        assertEquals("Connection refused", liveData.getValue().getMessage());
        assertEquals(networkError, liveData.getValue().getError());
    }

    @Test
    public void executeCall_whenCallIsCanceled_ignoresResponse() {
        FakeCall<String> fakeCall = new FakeCall<>();
        MutableLiveData<UiState<String>> liveData = new MutableLiveData<>();

        viewModel.executeCall(fakeCall, liveData);
        assertEquals(1, viewModel.getActiveCalls().size());

        // Cancel the call
        fakeCall.cancel();

        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(true);
        apiResponse.setData("Late Data");
        Response<ApiResponse<String>> retrofitResponse = Response.success(apiResponse);

        fakeCall.getCallback().onResponse(fakeCall, retrofitResponse);

        // Call was canceled, state remains LOADING (or initial before response), activeCalls cleared
        assertEquals(0, viewModel.getActiveCalls().size());
        assertNotNull(liveData.getValue());
        assertTrue("Canceled call response must not overwrite liveData to SUCCESS", liveData.getValue().isLoading());
    }

    @Test
    public void onCleared_cancelsAllActiveCalls() {
        FakeCall<String> call1 = new FakeCall<>();
        FakeCall<String> call2 = new FakeCall<>();
        MutableLiveData<UiState<String>> liveData1 = new MutableLiveData<>();
        MutableLiveData<UiState<String>> liveData2 = new MutableLiveData<>();

        viewModel.executeCall(call1, liveData1);
        viewModel.executeCall(call2, liveData2);

        assertEquals(2, viewModel.getActiveCalls().size());
        assertFalse(call1.isCanceled());
        assertFalse(call2.isCanceled());

        viewModel.invokeOnCleared();

        assertTrue(call1.isCanceled());
        assertTrue(call2.isCanceled());
        assertEquals(0, viewModel.getActiveCalls().size());
    }

    @Test
    public void executeCall_preservesCachedData_onLoadingAndError() {
        FakeCall<String> fakeCall = new FakeCall<>();
        MutableLiveData<UiState<String>> liveData = new MutableLiveData<>(UiState.success("Initial Cached Data"));

        viewModel.executeCall(fakeCall, liveData, "Fallback Error");

        // While loading, previous data should be preserved
        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isLoading());
        assertEquals("Initial Cached Data", liveData.getValue().getData());

        // On failure, previous data should still be retained
        fakeCall.getCallback().onFailure(fakeCall, new IOException("Network Down"));
        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isError());
        assertEquals("Network Down", liveData.getValue().getMessage());
        assertEquals("Initial Cached Data", liveData.getValue().getData());
    }

    @Test
    public void executeCall_onErrorResponseBody_parsesStandardSpringBootErrorField() {
        FakeCall<String> fakeCall = new FakeCall<>();
        MutableLiveData<UiState<String>> liveData = new MutableLiveData<>();

        viewModel.executeCall(fakeCall, liveData);

        String errorJson = "{\"status\":401,\"error\":\"Unauthorized\"}";
        ResponseBody errorBody = ResponseBody.create(MediaType.parse("application/json"), errorJson);
        Response<ApiResponse<String>> retrofitResponse = Response.error(401, errorBody);

        fakeCall.getCallback().onResponse(fakeCall, retrofitResponse);

        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isError());
        assertEquals("Unauthorized", liveData.getValue().getMessage());
    }

    @Test
    public void cancelAllCalls_handlesExceptionGracefully() {
        Call<ApiResponse<String>> throwingCall = new FakeCall<String>() {
            @Override
            public void cancel() {
                throw new RuntimeException("Simulated cancellation failure");
            }
        };
        FakeCall<String> normalCall = new FakeCall<>();
        MutableLiveData<UiState<String>> liveData1 = new MutableLiveData<>();
        MutableLiveData<UiState<String>> liveData2 = new MutableLiveData<>();

        viewModel.executeCall(throwingCall, liveData1);
        viewModel.executeCall(normalCall, liveData2);

        assertEquals(2, viewModel.getActiveCalls().size());

        // cancelAllCalls should not crash and should cancel remaining calls
        viewModel.cancelAllCalls();

        assertTrue(normalCall.isCanceled());
        assertEquals(0, viewModel.getActiveCalls().size());
    }
}
