package com.ppquang06.uniclassapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;
import androidx.lifecycle.MutableLiveData;

import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.repository.BaseRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class BaseRepositoryTest {

    private static class ConcreteRepository extends BaseRepository {
        public String callExtractErrorMessage(Response<?> response, String fallback) {
            return extractErrorMessage(response, fallback);
        }

        public String callExtractErrorMessage(Response<?> response) {
            return extractErrorMessage(response);
        }

        public <V> void callDispatchState(MutableLiveData<V> liveData, V value) {
            dispatchState(liveData, value);
        }
    }

    private ConcreteRepository repository;

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
        repository = new ConcreteRepository();
    }

    @After
    public void tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null);
    }

    @Test
    public void extractErrorMessage_whenApiResponseHasMessage_returnsMessage() {
        ApiResponse<String> body = new ApiResponse<>();
        body.setMessage("Tài khoản không tồn tại");
        Response<ApiResponse<String>> response = Response.success(body);

        String result = repository.callExtractErrorMessage(response, "Lỗi mặc định");
        assertEquals("Tài khoản không tồn tại", result);
    }

    @Test
    public void extractErrorMessage_whenErrorBodyHasSpecificErrorCode_mapsAppropriately() {
        String errorJson = "{\"code\":40006,\"message\":\"Sai pass\"}";
        ResponseBody errorBody = ResponseBody.create(MediaType.parse("application/json"), errorJson);
        Response<ApiResponse<String>> response = Response.error(400, errorBody);

        String result = repository.callExtractErrorMessage(response, "Lỗi mặc định");
        assertEquals("Mật khẩu hiện tại không chính xác", result);
    }

    @Test
    public void extractErrorMessage_whenErrorBodyHasStandardSpringBootFields_extractsErrorOrDetail() {
        String errorJson = "{\"status\":403,\"error\":\"Forbidden access\"}";
        ResponseBody errorBody = ResponseBody.create(MediaType.parse("application/json"), errorJson);
        Response<ApiResponse<String>> response = Response.error(403, errorBody);

        String result = repository.callExtractErrorMessage(response, "Lỗi mặc định");
        assertEquals("Forbidden access", result);
    }

    @Test
    public void extractErrorMessage_whenErrorBodyUnparseable_returnsFallbackMessage() {
        ResponseBody errorBody = ResponseBody.create(MediaType.parse("text/html"), "<html>Bad Gateway</html>");
        Response<ApiResponse<String>> response = Response.error(502, errorBody);

        String result = repository.callExtractErrorMessage(response, "Lỗi kết nối máy chủ");
        assertEquals("Lỗi kết nối máy chủ", result);
    }

    @Test
    public void dispatchState_updatesLiveData() {
        MutableLiveData<String> liveData = new MutableLiveData<>();
        repository.callDispatchState(liveData, "Hello World");

        assertNotNull(liveData.getValue());
        assertEquals("Hello World", liveData.getValue());
    }
}

