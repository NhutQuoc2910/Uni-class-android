package com.ppquang06.uniclassapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;
import androidx.lifecycle.MutableLiveData;

import com.ppquang06.uniclassapp.data.api.UserClassApiService;
import com.ppquang06.uniclassapp.data.local.dao.AssignmentDao;
import com.ppquang06.uniclassapp.data.local.dao.ClassDao;
import com.ppquang06.uniclassapp.data.local.entity.AssignmentEntity;
import com.ppquang06.uniclassapp.data.local.entity.ClassEntity;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.AttendanceCheckInRequest;
import com.ppquang06.uniclassapp.data.model.request.ChangePasswordRequest;
import com.ppquang06.uniclassapp.data.model.request.CreateAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.request.CreateClassRequest;
import com.ppquang06.uniclassapp.data.model.request.CreateMaterialRequest;
import com.ppquang06.uniclassapp.data.model.request.GradeSubmissionRequest;
import com.ppquang06.uniclassapp.data.model.request.JoinClassRequest;
import com.ppquang06.uniclassapp.data.model.request.OpenQrSessionRequest;
import com.ppquang06.uniclassapp.data.model.request.SubmitAssignmentRequest;
import com.ppquang06.uniclassapp.data.model.request.UpdateProfileRequest;
import com.ppquang06.uniclassapp.data.model.response.AssignmentDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.AssignmentItem;
import com.ppquang06.uniclassapp.data.model.response.AssignmentSubmissionListResponse;
import com.ppquang06.uniclassapp.data.model.response.AttendanceCheckInResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassDetailResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassGradesResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassMemberItemResponse;
import com.ppquang06.uniclassapp.data.model.response.ClassStatisticsResponse;
import com.ppquang06.uniclassapp.data.model.response.CourseClassItem;
import com.ppquang06.uniclassapp.data.model.response.FileUploadResponse;
import com.ppquang06.uniclassapp.data.model.response.HomeSummaryResponse;
import com.ppquang06.uniclassapp.data.model.response.LiveAttendanceSessionResponse;
import com.ppquang06.uniclassapp.data.model.response.NotificationItemResponse;
import com.ppquang06.uniclassapp.data.model.response.UserResponse;
import com.ppquang06.uniclassapp.data.repository.AssignmentRepository;
import com.ppquang06.uniclassapp.data.repository.ClassRepository;
import com.ppquang06.uniclassapp.data.repository.UserRepository;
import com.ppquang06.uniclassapp.ui.common.UiState;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RepositoryOfflineCachingTest {

    private static class FakeCall<T> implements Call<ApiResponse<T>> {
        private boolean canceled = false;
        private Callback<ApiResponse<T>> callback;

        @Override
        public Response<ApiResponse<T>> execute() {
            return null;
        }

        @Override
        public void enqueue(Callback<ApiResponse<T>> callback) {
            this.callback = callback;
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
            return new Request.Builder().url("http://localhost/").build();
        }

        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }

        public void succeed(T payload) {
            ApiResponse<T> apiResponse = new ApiResponse<>();
            apiResponse.setSuccess(true);
            apiResponse.setCode(200);
            apiResponse.setData(payload);
            callback.onResponse(this, Response.success(apiResponse));
        }

        public void fail(Throwable t) {
            callback.onFailure(this, t);
        }

        public void respondHttpError(int statusCode, String jsonBody) {
            ResponseBody body = ResponseBody.create(jsonBody, MediaType.parse("application/json"));
            callback.onResponse(this, Response.error(statusCode, body));
        }
    }

    private static class InMemoryClassDao implements ClassDao {
        private final Map<String, ClassEntity> store = new LinkedHashMap<>();

        @Override
        public List<ClassEntity> getAllClasses() {
            return new ArrayList<>(store.values());
        }

        @Override
        public ClassEntity getClassById(String classId) {
            return store.get(classId);
        }

        @Override
        public void insertClasses(List<ClassEntity> classes) {
            if (classes == null) return;
            for (ClassEntity entity : classes) {
                if (entity != null) {
                    store.put(entity.getId(), entity);
                }
            }
        }

        @Override
        public void insertClass(ClassEntity classEntity) {
            if (classEntity != null) {
                store.put(classEntity.getId(), classEntity);
            }
        }

        @Override
        public void clearClasses() {
            store.clear();
        }
    }

    private static class InMemoryAssignmentDao implements AssignmentDao {
        private final Map<String, AssignmentEntity> store = new LinkedHashMap<>();

        @Override
        public List<AssignmentEntity> getAllAssignments() {
            return new ArrayList<>(store.values());
        }

        @Override
        public List<AssignmentEntity> getAssignmentsByClass(String classId) {
            List<AssignmentEntity> list = new ArrayList<>();
            for (AssignmentEntity entity : store.values()) {
                if (classId != null && classId.equals(entity.getClassId())) {
                    list.add(entity);
                }
            }
            return list;
        }

        @Override
        public void insertAssignments(List<AssignmentEntity> assignments) {
            if (assignments == null) return;
            for (AssignmentEntity entity : assignments) {
                if (entity != null) {
                    store.put(entity.getId(), entity);
                }
            }
        }

        @Override
        public void clearByClass(String classId) {
            store.entrySet().removeIf(entry ->
                    classId != null && classId.equals(entry.getValue().getClassId()));
        }

        @Override
        public void clearAll() {
            store.clear();
        }
    }

    private static class StubUserClassApiService implements UserClassApiService {
        FakeCall<List<CourseClassItem>> classesCall = new FakeCall<>();
        FakeCall<ClassDetailResponse> classDetailCall = new FakeCall<>();
        FakeCall<HomeSummaryResponse> homeSummaryCall = new FakeCall<>();

        @Override
        public Call<ApiResponse<UserResponse>> getCurrentUser() { return null; }

        @Override
        public Call<ApiResponse<UserResponse>> updateCurrentUser(UpdateProfileRequest request) { return null; }

        @Override
        public Call<ApiResponse<UserResponse>> updateCurrentUser(UserResponse request) { return null; }

        @Override
        public Call<ApiResponse<Void>> changePassword(ChangePasswordRequest request) { return null; }

        @Override
        public Call<ApiResponse<HomeSummaryResponse>> getHomeSummary() { return homeSummaryCall; }

        @Override
        public Call<ApiResponse<List<CourseClassItem>>> getClasses() { return classesCall; }

        @Override
        public Call<ApiResponse<CourseClassItem>> joinClass(JoinClassRequest request) { return null; }

        @Override
        public Call<ApiResponse<ClassDetailResponse>> getClassDetail(String classId) { return classDetailCall; }

        @Override
        public Call<ApiResponse<List<NotificationItemResponse>>> getNotifications() { return null; }

        @Override
        public Call<ApiResponse<AssignmentDetailResponse>> getAssignmentDetail(String id) { return null; }

        @Override
        public Call<ApiResponse<AssignmentDetailResponse>> submitAssignment(String id, SubmitAssignmentRequest request) { return null; }

        @Override
        public Call<ApiResponse<FileUploadResponse>> uploadFile(MultipartBody.Part file, RequestBody folder) { return null; }

        @Override
        public Call<ApiResponse<AttendanceCheckInResponse>> checkInAttendance(AttendanceCheckInRequest request) { return null; }

        @Override
        public Call<ApiResponse<ClassGradesResponse>> getClassGrades(String classId) { return null; }

        @Override
        public Call<ApiResponse<CourseClassItem>> createClass(CreateClassRequest request) { return null; }

        @Override
        public Call<ApiResponse<CourseClassItem>> archiveClass(String classId) { return null; }

        @Override
        public Call<ApiResponse<List<ClassMemberItemResponse>>> getClassMembers(String classId) { return null; }

        @Override
        public Call<ApiResponse<Void>> removeClassMember(String classId, String studentId) { return null; }

        @Override
        public Call<ApiResponse<ClassDetailResponse.MaterialItem>> createMaterial(String classId, CreateMaterialRequest request) { return null; }

        @Override
        public Call<ApiResponse<ClassStatisticsResponse>> getClassStatistics(String classId) { return null; }

        @Override
        public Call<ApiResponse<ClassDetailResponse.ClassAssignmentItem>> createAssignment(String classId, CreateAssignmentRequest request) { return null; }

        @Override
        public Call<ApiResponse<AssignmentSubmissionListResponse>> getAssignmentSubmissions(String assignmentId) { return null; }

        @Override
        public Call<ApiResponse<AssignmentSubmissionListResponse>> gradeSubmission(String assignmentId, String studentId, GradeSubmissionRequest request) { return null; }

        @Override
        public Call<ApiResponse<LiveAttendanceSessionResponse>> openQrSession(String classId, OpenQrSessionRequest request) { return null; }

        @Override
        public Call<ApiResponse<LiveAttendanceSessionResponse>> getLiveAttendanceSession(String sessionId) { return null; }

        @Override
        public Call<ApiResponse<LiveAttendanceSessionResponse>> closeAttendanceSession(String sessionId) { return null; }
    }

    private InMemoryClassDao classDao;
    private InMemoryAssignmentDao assignmentDao;
    private StubUserClassApiService apiService;
    private final Executor directExecutor = Runnable::run;

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
        classDao = new InMemoryClassDao();
        assignmentDao = new InMemoryAssignmentDao();
        apiService = new StubUserClassApiService();
    }

    @After
    public void tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null);
    }

    @Test
    public void fetchClassesOfflineFirst_networkSuccess_persistsToClassDaoAndEmitsSuccess() {
        ClassRepository repository = new ClassRepository(apiService, classDao, assignmentDao, directExecutor);
        MutableLiveData<UiState<List<CourseClassItem>>> liveData = new MutableLiveData<>();
        Set<Call<?>> activeCalls = Collections.newSetFromMap(new ConcurrentHashMap<>());

        // Pre-populate stale class to verify clearClasses() replaces old cache
        classDao.insertClass(ClassEntity.fromCourseClassItem(
                new CourseClassItem("stale-id", "OLD101", "Old Class", "HK1", 10, 5)
        ));

        repository.fetchClassesOfflineFirst(liveData, activeCalls);
        assertEquals(1, activeCalls.size());
        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isLoading());

        List<CourseClassItem> freshList = new ArrayList<>();
        freshList.add(new CourseClassItem("cls-1", "NT118", "Mobile Dev", "HK1 2025-2026", "C302", "Dr. Quang", "INV1", "ACTIVE", 80, 45));
        freshList.add(new CourseClassItem("cls-2", "SE104", "Software Eng", "HK1 2025-2026", "B104", "Dr. Minh", "INV2", "ACTIVE", 60, 50));

        apiService.classesCall.succeed(freshList);

        assertEquals(0, activeCalls.size());
        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isSuccess());
        assertEquals(2, liveData.getValue().getData().size());

        List<ClassEntity> cachedEntities = classDao.getAllClasses();
        assertEquals(2, cachedEntities.size());
        assertEquals("cls-1", cachedEntities.get(0).getId());
        assertEquals("NT118", cachedEntities.get(0).getCode());
        assertEquals("cls-2", cachedEntities.get(1).getId());
    }

    @Test
    public void fetchClassesOfflineFirst_networkFailure_emitsCachedClassesFromClassDao() {
        ClassRepository repository = new ClassRepository(apiService, classDao, assignmentDao, directExecutor);
        MutableLiveData<UiState<List<CourseClassItem>>> liveData = new MutableLiveData<>();
        Set<Call<?>> activeCalls = Collections.newSetFromMap(new ConcurrentHashMap<>());

        classDao.insertClass(ClassEntity.fromCourseClassItem(
                new CourseClassItem("cls-cached", "NT118", "Cached Mobile Dev", "HK1", "C302", "Dr. Quang", "INV99", "ACTIVE", 75, 40)
        ));

        repository.fetchClassesOfflineFirst(liveData, activeCalls);

        // Should emit LOADING with cached list while waiting for network
        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isLoading());
        assertNotNull(liveData.getValue().getData());
        assertEquals(1, liveData.getValue().getData().size());
        assertEquals("cls-cached", liveData.getValue().getData().get(0).getId());

        IOException networkError = new IOException("No internet connection");
        apiService.classesCall.fail(networkError);

        assertEquals(0, activeCalls.size());
        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isError());
        assertEquals("No internet connection", liveData.getValue().getMessage());
        assertEquals(networkError, liveData.getValue().getError());
        assertNotNull(liveData.getValue().getData());
        assertEquals(1, liveData.getValue().getData().size());
        assertEquals("Cached Mobile Dev", liveData.getValue().getData().get(0).getName());
    }

    @Test
    public void fetchClassDetailOfflineFirst_networkSuccess_persistsClassAndAssignments() {
        ClassRepository repository = new ClassRepository(apiService, classDao, assignmentDao, directExecutor);
        MutableLiveData<UiState<ClassDetailResponse>> liveData = new MutableLiveData<>();
        Set<Call<?>> activeCalls = Collections.newSetFromMap(new ConcurrentHashMap<>());

        repository.fetchClassDetailOfflineFirst("cls-101", liveData, activeCalls);
        assertEquals(1, activeCalls.size());

        ClassDetailResponse detail = new ClassDetailResponse();
        detail.setId("cls-101");
        detail.setCode("NT118");
        detail.setName("Mobile Application Development");
        detail.setSemester("HK1 2025-2026");
        detail.setRoom("C302");
        detail.setLecturerName("Dr. Quang");
        detail.setInviteCode("JOIN101");
        detail.setEnrolledCount(42);
        detail.setAttendanceRate(90);

        List<ClassDetailResponse.ClassAssignmentItem> assignments = new ArrayList<>();
        assignments.add(new ClassDetailResponse.ClassAssignmentItem(
                "asg-1", "Lab 1", "Setup Android Studio", "2026-11-01T23:59:00Z", null, true, 9.0, "Good"
        ));
        assignments.add(new ClassDetailResponse.ClassAssignmentItem(
                "asg-2", "Lab 2", "Room Offline Cache", "2026-11-15T23:59:00Z", "https://example.com/lab2.pdf", false, null, null
        ));
        detail.setAssignments(assignments);

        apiService.classDetailCall.succeed(detail);

        assertEquals(0, activeCalls.size());
        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isSuccess());
        assertEquals("Mobile Application Development", liveData.getValue().getData().getName());

        ClassEntity cachedClass = classDao.getClassById("cls-101");
        assertNotNull(cachedClass);
        assertEquals("NT118", cachedClass.getCode());
        assertEquals("Mobile Application Development", cachedClass.getName());
        assertEquals("C302", cachedClass.getRoom());

        List<AssignmentEntity> cachedAssignments = assignmentDao.getAssignmentsByClass("cls-101");
        assertEquals(2, cachedAssignments.size());
        assertEquals("asg-1", cachedAssignments.get(0).getId());
        assertEquals("Lab 1", cachedAssignments.get(0).getTitle());
        assertEquals(Double.valueOf(9.0), cachedAssignments.get(0).getScore());
        assertEquals("asg-2", cachedAssignments.get(1).getId());
    }

    @Test
    public void fetchClassDetailOfflineFirst_networkFailure_reconstructsClassDetailFromCache() {
        ClassRepository repository = new ClassRepository(apiService, classDao, assignmentDao, directExecutor);
        MutableLiveData<UiState<ClassDetailResponse>> liveData = new MutableLiveData<>();

        classDao.insertClass(new ClassEntity(
                "cls-101", "NT118", "Mobile App Dev", "HK1 2025-2026", "C302",
                "Dr. Quang", "INV101", "ACTIVE", 85, 42, System.currentTimeMillis()
        ));

        List<AssignmentEntity> cachedAsgs = new ArrayList<>();
        cachedAsgs.add(AssignmentEntity.fromClassAssignmentItem(
                "cls-101",
                "Mobile App Dev",
                new ClassDetailResponse.ClassAssignmentItem(
                        "asg-99", "Offline Cached Lab", "Complete offline mode", "2026-12-01T00:00:00Z",
                        null, true, 10.0, "Excellent"
                )
        ));
        assignmentDao.insertAssignments(cachedAsgs);

        repository.fetchClassDetailOfflineFirst("cls-101", liveData, null);
        apiService.classDetailCall.fail(new IOException("Server unreachable"));

        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isError());
        assertEquals("Server unreachable", liveData.getValue().getMessage());

        ClassDetailResponse fallback = liveData.getValue().getData();
        assertNotNull(fallback);
        assertEquals("cls-101", fallback.getId());
        assertEquals("NT118", fallback.getCode());
        assertEquals("Mobile App Dev", fallback.getName());
        assertEquals("HK1 2025-2026", fallback.getSemester());
        assertEquals("C302", fallback.getRoom());
        assertEquals("Dr. Quang", fallback.getLecturerName());
        assertEquals(42, fallback.getEnrolledCount());
        assertEquals(85, fallback.getAttendanceRate());
        assertEquals(1, fallback.getAssignments().size());
        assertEquals("asg-99", fallback.getAssignments().get(0).getId());
        assertEquals("Offline Cached Lab", fallback.getAssignments().get(0).getTitle());
        assertEquals(Double.valueOf(10.0), fallback.getAssignments().get(0).getScore());
    }

    @Test
    public void fetchHomeSummaryOfflineFirst_networkSuccess_persistsClassesAndAssignments() {
        UserRepository userRepository = new UserRepository(apiService, classDao, assignmentDao, directExecutor);
        MutableLiveData<UiState<HomeSummaryResponse>> liveData = new MutableLiveData<>();
        Set<Call<?>> activeCalls = Collections.newSetFromMap(new ConcurrentHashMap<>());

        userRepository.fetchHomeSummaryOfflineFirst(liveData, activeCalls);
        assertEquals(1, activeCalls.size());
        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isLoading());

        HomeSummaryResponse summary = new HomeSummaryResponse();
        summary.setActiveCoursesCount(1);
        summary.setPendingAssignmentsCount(1);
        summary.setAttendanceRate(95);

        List<CourseClassItem> courses = new ArrayList<>();
        courses.add(new CourseClassItem("cls-home-1", "NT118", "Mobile Dev", "HK1", "C302", "Dr. Quang", 95, 40));
        summary.setCourses(courses);

        List<AssignmentItem> dueSoon = new ArrayList<>();
        AssignmentItem asg = new AssignmentItem();
        asg.setId("asg-home-1");
        asg.setTitle("Homework 3");
        asg.setClassName("NT118");
        asg.setDueDate("2026-11-20T23:59:00Z");
        asg.setSubmitted(false);
        dueSoon.add(asg);
        summary.setDueSoonAssignments(dueSoon);

        apiService.homeSummaryCall.succeed(summary);

        assertEquals(0, activeCalls.size());
        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isSuccess());
        assertEquals(95, liveData.getValue().getData().getAttendanceRate());

        assertEquals(1, classDao.getAllClasses().size());
        assertEquals("cls-home-1", classDao.getAllClasses().get(0).getId());

        assertEquals(1, assignmentDao.getAllAssignments().size());
        assertEquals("asg-home-1", assignmentDao.getAllAssignments().get(0).getId());
        assertEquals("Homework 3", assignmentDao.getAllAssignments().get(0).getTitle());
    }

    @Test
    public void fetchHomeSummaryOfflineFirst_networkFailure_emitsFallbackSummaryFromCache() {
        UserRepository userRepository = new UserRepository(apiService, classDao, assignmentDao, directExecutor);
        MutableLiveData<UiState<HomeSummaryResponse>> liveData = new MutableLiveData<>();

        classDao.insertClass(ClassEntity.fromCourseClassItem(
                new CourseClassItem("cls-home-cached", "NT118", "Cached Course", "HK1", 80, 35)
        ));

        AssignmentItem cachedItem = new AssignmentItem();
        cachedItem.setId("asg-home-cached");
        cachedItem.setTitle("Cached Assignment");
        cachedItem.setClassName("NT118");
        cachedItem.setDueDate("2026-11-25T23:59:00Z");
        cachedItem.setSubmitted(false);
        assignmentDao.insertAssignments(Collections.singletonList(AssignmentEntity.fromAssignmentItem(cachedItem)));

        userRepository.fetchHomeSummaryOfflineFirst(liveData, null);
        IOException ioException = new IOException("Timeout");
        apiService.homeSummaryCall.fail(ioException);

        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isError());
        assertEquals("Timeout", liveData.getValue().getMessage());
        assertEquals(ioException, liveData.getValue().getError());

        HomeSummaryResponse fallback = liveData.getValue().getData();
        assertNotNull(fallback);
        assertEquals(1, fallback.getActiveCoursesCount());
        assertEquals(1, fallback.getPendingAssignmentsCount());
        assertEquals(1, fallback.getCourses().size());
        assertEquals("cls-home-cached", fallback.getCourses().get(0).getId());
        assertEquals(1, fallback.getDueSoonAssignments().size());
        assertEquals("asg-home-cached", fallback.getDueSoonAssignments().get(0).getId());
    }

    @Test
    public void assignmentRepository_cacheAndGetCachedClassAssignments_roundTrip() {
        AssignmentRepository assignmentRepository = new AssignmentRepository(apiService, assignmentDao, directExecutor);

        List<ClassDetailResponse.ClassAssignmentItem> items = new ArrayList<>();
        items.add(new ClassDetailResponse.ClassAssignmentItem(
                "asg-r1", "Quiz 1", "Chapter 1-3", "2026-10-10T10:00:00Z", null, true, 8.5, "Well done"
        ));

        assignmentRepository.cacheClassAssignments("cls-77", "NT118", items);

        List<ClassDetailResponse.ClassAssignmentItem> cached =
                assignmentRepository.getCachedClassAssignments("cls-77");
        assertEquals(1, cached.size());
        assertEquals("asg-r1", cached.get(0).getId());
        assertEquals("Quiz 1", cached.get(0).getTitle());
        assertEquals(Double.valueOf(8.5), cached.get(0).getScore());
    }

    @Test
    public void fetchClassesOfflineFirst_fastNetworkResponseBeforeInitialCacheRead_doesNotOverwriteSuccess() {
        List<Runnable> deferredTasks = new ArrayList<>();
        Executor deferredInitialReadExecutor = new Executor() {
            private boolean firstTaskDeferred = false;

            @Override
            public void execute(Runnable command) {
                if (!firstTaskDeferred) {
                    firstTaskDeferred = true;
                    deferredTasks.add(command);
                } else {
                    command.run();
                }
            }
        };

        classDao.insertClass(ClassEntity.fromCourseClassItem(
                new CourseClassItem("cls-stale", "OLD101", "Stale Cached Class", "HK1", 10, 5)
        ));

        ClassRepository repository = new ClassRepository(apiService, classDao, assignmentDao, deferredInitialReadExecutor);
        MutableLiveData<UiState<List<CourseClassItem>>> liveData = new MutableLiveData<>();

        repository.fetchClassesOfflineFirst(liveData, null);
        assertEquals(1, deferredTasks.size());

        // Network succeeds BEFORE the initial cache read runnable executes
        List<CourseClassItem> freshList = Collections.singletonList(
                new CourseClassItem("cls-fresh", "NT118", "Fresh Class", "HK1", 90, 45)
        );
        apiService.classesCall.succeed(freshList);

        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isSuccess());
        assertEquals("cls-fresh", liveData.getValue().getData().get(0).getId());

        // Now the delayed initial cache read finally runs on the worker thread
        deferredTasks.get(0).run();

        // Must remain SUCCESS with fresh data, never overwritten by LOADING(cached)
        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isSuccess());
        assertEquals("cls-fresh", liveData.getValue().getData().get(0).getId());
    }

    @Test
    public void fetchClassesOfflineFirst_httpErrorBodyJson_extractsErrorMessageAndEmitsCachedData() {
        ClassRepository repository = new ClassRepository(apiService, classDao, assignmentDao, directExecutor);
        MutableLiveData<UiState<List<CourseClassItem>>> liveData = new MutableLiveData<>();

        classDao.insertClass(ClassEntity.fromCourseClassItem(
                new CourseClassItem("cls-cached", "NT118", "Cached Class", "HK1", 80, 40)
        ));

        repository.fetchClassesOfflineFirst(liveData, null);
        apiService.classesCall.respondHttpError(403, "{\"success\":false,\"code\":403,\"message\":\"Phiên đăng nhập đã hết hạn\"}");

        assertNotNull(liveData.getValue());
        assertTrue(liveData.getValue().isError());
        assertEquals("Phiên đăng nhập đã hết hạn", liveData.getValue().getMessage());
        assertNotNull(liveData.getValue().getData());
        assertEquals(1, liveData.getValue().getData().size());
        assertEquals("cls-cached", liveData.getValue().getData().get(0).getId());
    }

    @Test
    public void fetchHomeSummaryOfflineFirst_canceledCall_ignoresResponseAndRemovesFromActiveCalls() {
        UserRepository userRepository = new UserRepository(apiService, classDao, assignmentDao, directExecutor);
        MutableLiveData<UiState<HomeSummaryResponse>> liveData = new MutableLiveData<>();
        Set<Call<?>> activeCalls = Collections.newSetFromMap(new ConcurrentHashMap<>());

        userRepository.fetchHomeSummaryOfflineFirst(liveData, activeCalls);
        assertEquals(1, activeCalls.size());
        assertTrue(liveData.getValue().isLoading());

        apiService.homeSummaryCall.cancel();
        apiService.homeSummaryCall.fail(new IOException("Canceled"));

        assertEquals(0, activeCalls.size());
        assertTrue(liveData.getValue().isLoading());
    }
}
