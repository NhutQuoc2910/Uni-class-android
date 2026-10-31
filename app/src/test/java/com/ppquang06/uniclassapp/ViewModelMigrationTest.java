package com.ppquang06.uniclassapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;

import com.ppquang06.uniclassapp.data.api.AuthApiService;
import com.ppquang06.uniclassapp.data.api.UserClassApiService;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.AttendanceCheckInRequest;
import com.ppquang06.uniclassapp.data.model.request.CreateAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.request.CreateClassRequest;
import com.ppquang06.uniclassapp.data.model.request.GradeSubmissionRequest;
import com.ppquang06.uniclassapp.data.model.request.LoginRequest;
import com.ppquang06.uniclassapp.data.model.request.SubmitAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.response.AssignmentDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.AssignmentSubmissionListResponse;
import com.ppquang06.uniclassapp.data.model.response.AttendanceCheckInResponse;
import com.ppquang06.uniclassapp.data.model.response.AuthResponse;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.data.model.response.HomeSummaryResponse;
import com.ppquang06.uniclassapp.data.repository.AssignmentRepository;
import com.ppquang06.uniclassapp.data.repository.AttendanceRepository;
import com.ppquang06.uniclassapp.data.repository.AuthRepository;
import com.ppquang06.uniclassapp.data.repository.ClassRepository;
import com.ppquang06.uniclassapp.data.repository.UserRepository;
import com.ppquang06.uniclassapp.ui.auth.AuthViewModel;
import com.ppquang06.uniclassapp.ui.common.HomeViewModel;
import com.ppquang06.uniclassapp.ui.common.UiState;
import com.ppquang06.uniclassapp.ui.common.profile.ProfileViewModel;
import com.ppquang06.uniclassapp.ui.lecturer.assignments.GradingViewModel;
import com.ppquang06.uniclassapp.ui.lecturer.attendance.AttendanceViewModel;
import com.ppquang06.uniclassapp.ui.lecturer.classes.LecturerManagementViewModel;
import com.ppquang06.uniclassapp.ui.student.classes.ClassDetailViewModel;
import com.ppquang06.uniclassapp.ui.student.classes.StudentAssignmentViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewModelMigrationTest {

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
    }

    @After
    public void tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null);
    }

    private static <T> ApiResponse<T> createSuccess(T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setSuccess(true);
        res.setCode(200);
        res.setData(data);
        return res;
    }

    private static UserClassApiService createMockUserClassApi(InvocationHandler handler) {
        return (UserClassApiService) Proxy.newProxyInstance(
                UserClassApiService.class.getClassLoader(),
                new Class<?>[]{UserClassApiService.class},
                handler
        );
    }

    private static AuthApiService createMockAuthApi(InvocationHandler handler) {
        return (AuthApiService) Proxy.newProxyInstance(
                AuthApiService.class.getClassLoader(),
                new Class<?>[]{AuthApiService.class},
                handler
        );
    }

    private static class FakeCall<T> implements Call<ApiResponse<T>> {
        private boolean canceled = false;
        private Callback<ApiResponse<T>> callback;
        private final ApiResponse<T> successResponse;
        private final int errorCode;
        private final String errorJson;

        public FakeCall(ApiResponse<T> successResponse) {
            this.successResponse = successResponse;
            this.errorCode = 200;
            this.errorJson = null;
        }

        public FakeCall(int errorCode, String errorJson) {
            this.successResponse = null;
            this.errorCode = errorCode;
            this.errorJson = errorJson;
        }

        @Override
        public Response<ApiResponse<T>> execute() throws IOException {
            return null;
        }

        @Override
        public void enqueue(Callback<ApiResponse<T>> callback) {
            this.callback = callback;
            if (successResponse != null) {
                callback.onResponse(this, Response.success(successResponse));
            } else {
                ResponseBody body = ResponseBody.create(
                        MediaType.parse("application/json"),
                        errorJson != null ? errorJson : "{\"message\":\"Error\"}"
                );
                callback.onResponse(this, Response.error(errorCode, body));
            }
        }

        @Override
        public boolean isExecuted() {
            return callback != null;
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
            return new Request.Builder().url("https://example.com").build();
        }

        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }
    }

    @Test
    public void testAuthViewModel_LoginSuccess() {
        AuthApiService mockApi = createMockAuthApi((proxy, method, args) -> {
            if ("login".equals(method.getName())) {
                AuthResponse auth = new AuthResponse();
                auth.setToken("test_token_123");
                return new FakeCall<>(createSuccess(auth));
            }
            return null;
        });

        AuthRepository repo = new AuthRepository(mockApi);
        AuthViewModel viewModel = new AuthViewModel(repo);
        assertEquals(UiState.Status.IDLE, viewModel.getLoginState().getValue().getStatus());

        viewModel.login(new LoginRequest("test@uniclass.edu", "password123"));

        UiState<AuthResponse> state = viewModel.getLoginState().getValue();
        assertNotNull(state);
        assertEquals(UiState.Status.SUCCESS, state.getStatus());
        assertEquals("test_token_123", state.getData().getToken());
    }

    @Test
    public void testHomeViewModel_SummarySuccess() {
        HomeSummaryResponse summary = new HomeSummaryResponse();
        summary.setActiveCoursesCount(5);

        UserClassApiService mockApi = createMockUserClassApi((proxy, method, args) -> {
            if ("getHomeSummary".equals(method.getName())) {
                return new FakeCall<>(createSuccess(summary));
            }
            return null;
        });

        UserRepository repo = new UserRepository(mockApi);
        HomeViewModel viewModel = new HomeViewModel(repo);
        viewModel.loadHomeSummary();

        UiState<HomeSummaryResponse> state = viewModel.getHomeSummaryState().getValue();
        assertNotNull(state);
        assertEquals(UiState.Status.SUCCESS, state.getStatus());
        assertEquals(5, state.getData().getActiveCoursesCount());
    }

    @Test
    public void testProfileViewModel_ChangePasswordError() {
        UserClassApiService mockApi = createMockUserClassApi((proxy, method, args) -> {
            if ("changePassword".equals(method.getName())) {
                return new FakeCall<>(400, "{\"code\":40006,\"message\":\"Mật khẩu cũ không đúng\"}");
            }
            return null;
        });

        UserRepository repo = new UserRepository(mockApi);
        ProfileViewModel viewModel = new ProfileViewModel(repo);
        viewModel.changePassword("oldPass", "newPass");

        UiState<Void> state = viewModel.getChangePasswordState().getValue();
        assertNotNull(state);
        assertEquals(UiState.Status.ERROR, state.getStatus());
        assertEquals("Mật khẩu hiện tại không chính xác", state.getMessage());
    }

    @Test
    public void testClassDetailViewModel_JoinClassAlreadyJoined() {
        UserClassApiService mockApi = createMockUserClassApi((proxy, method, args) -> {
            if ("joinClass".equals(method.getName())) {
                return new FakeCall<>(400, "{\"code\":40008,\"message\":\"Already joined\"}");
            }
            return null;
        });

        ClassRepository repo = new ClassRepository(mockApi);
        ClassDetailViewModel viewModel = new ClassDetailViewModel(repo);
        viewModel.joinClass("CLASS123");

        UiState<CourseClassItem> state = viewModel.getJoinClassState().getValue();
        assertNotNull(state);
        assertEquals(UiState.Status.ERROR, state.getStatus());
        assertEquals("ALREADY_JOINED", state.getMessage());
    }

    @Test
    public void testStudentAssignmentViewModel_SubmitSuccess() {
        AssignmentDetailResponse detail = new AssignmentDetailResponse();
        detail.setId("assign_1");
        detail.setSubmitted(true);

        UserClassApiService mockApi = createMockUserClassApi((proxy, method, args) -> {
            if ("submitAssignment".equals(method.getName())) {
                return new FakeCall<>(createSuccess(detail));
            }
            return null;
        });

        AssignmentRepository repo = new AssignmentRepository(mockApi);
        StudentAssignmentViewModel viewModel = new StudentAssignmentViewModel(repo);
        viewModel.submitAssignment("assign_1", new SubmitAssignmentRequest("https://url.com/file.pdf", "file.pdf"));

        UiState<AssignmentDetailResponse> state = viewModel.getSubmitAssignmentState().getValue();
        assertNotNull(state);
        assertEquals(UiState.Status.SUCCESS, state.getStatus());
        assertTrue(state.getData().isSubmitted());
    }

    @Test
    public void testLecturerManagementViewModel_CreateClassSuccess() {
        CourseClassItem item = new CourseClassItem();
        item.setId("class_new");
        item.setName("Lập trình Java");

        UserClassApiService mockApi = createMockUserClassApi((proxy, method, args) -> {
            if ("createClass".equals(method.getName())) {
                return new FakeCall<>(createSuccess(item));
            }
            return null;
        });

        ClassRepository repo = new ClassRepository(mockApi);
        LecturerManagementViewModel viewModel = new LecturerManagementViewModel(repo);
        viewModel.createClass(new CreateClassRequest("Lập trình Java", "CS101", "HK1-2026", "A101"));

        UiState<CourseClassItem> state = viewModel.getCreateClassState().getValue();
        assertNotNull(state);
        assertEquals(UiState.Status.SUCCESS, state.getStatus());
        assertEquals("Lập trình Java", state.getData().getName());
    }

    @Test
    public void testGradingViewModel_GradeSubmissionSuccess() {
        AssignmentSubmissionListResponse res = new AssignmentSubmissionListResponse();
        res.setAssignmentTitle("Bài tập lớn");

        UserClassApiService mockApi = createMockUserClassApi((proxy, method, args) -> {
            if ("gradeSubmission".equals(method.getName())) {
                return new FakeCall<>(createSuccess(res));
            }
            return null;
        });

        AssignmentRepository repo = new AssignmentRepository(mockApi);
        GradingViewModel viewModel = new GradingViewModel(repo);
        viewModel.gradeSubmission("assign_1", "student_1", new GradeSubmissionRequest(9.5, "Rất tốt", true));

        UiState<AssignmentSubmissionListResponse> state = viewModel.getGradeState().getValue();
        assertNotNull(state);
        assertEquals(UiState.Status.SUCCESS, state.getStatus());
        assertEquals("Bài tập lớn", state.getData().getAssignmentTitle());
    }

    @Test
    public void testAttendanceViewModel_CheckInQrExpired() {
        UserClassApiService mockApi = createMockUserClassApi((proxy, method, args) -> {
            if ("checkInAttendance".equals(method.getName())) {
                return new FakeCall<>(400, "{\"code\":40010,\"errorCode\":\"QR_EXPIRED\",\"message\":\"QR đã hết hạn\"}");
            }
            return null;
        });

        AttendanceRepository repo = new AttendanceRepository(mockApi);
        AttendanceViewModel viewModel = new AttendanceViewModel(repo);
        viewModel.checkIn(new AttendanceCheckInRequest("sess_1", "00:11:22:33:44:55", 10.5, 106.5, "dev_123"));

        UiState<AttendanceCheckInResponse> state = viewModel.getCheckInState().getValue();
        assertNotNull(state);
        assertEquals(UiState.Status.ERROR, state.getStatus());
        assertEquals("QR_EXPIRED", state.getMessage());
    }
}
