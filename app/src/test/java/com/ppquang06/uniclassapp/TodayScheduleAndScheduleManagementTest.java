package com.ppquang06.uniclassapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;

import com.ppquang06.uniclassapp.data.api.UserClassApiService;
import com.ppquang06.uniclassapp.data.model.ApiResponse;
import com.ppquang06.uniclassapp.data.model.request.CreateClassScheduleRequest;
import com.ppquang06.uniclassapp.data.model.request.CreateScheduleExceptionRequest;
import com.ppquang06.uniclassapp.data.model.response.ClassScheduleItem;
import com.ppquang06.uniclassapp.data.model.response.ScheduleExceptionItem;
import com.ppquang06.uniclassapp.data.model.response.SemesterScheduleResponse;
import com.ppquang06.uniclassapp.data.model.response.TodayScheduleItem;
import com.ppquang06.uniclassapp.data.repository.ClassRepository;
import com.ppquang06.uniclassapp.data.repository.UserRepository;
import com.ppquang06.uniclassapp.ui.common.HomeViewModel;
import com.ppquang06.uniclassapp.ui.common.UiState;
import com.ppquang06.uniclassapp.ui.lecturer.classes.LecturerManagementViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TodayScheduleAndScheduleManagementTest {

    private static class ImmediateCall<T> implements Call<ApiResponse<T>> {
        private final ApiResponse<T> responsePayload;

        ImmediateCall(ApiResponse<T> responsePayload) {
            this.responsePayload = responsePayload;
        }

        @Override
        public Response<ApiResponse<T>> execute() {
            return Response.success(responsePayload);
        }

        @Override
        public void enqueue(Callback<ApiResponse<T>> callback) {
            callback.onResponse(this, Response.success(responsePayload));
        }

        @Override
        public boolean isExecuted() { return true; }

        @Override
        public void cancel() {}

        @Override
        public boolean isCanceled() { return false; }

        @Override
        public Call<ApiResponse<T>> clone() { return this; }

        @Override
        public Request request() {
            return new Request.Builder().url("http://localhost/").build();
        }

        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }
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

    @Test
    public void testTodayScheduleItem_isCancelledAndTypeLabels() {
        TodayScheduleItem item = new TodayScheduleItem();
        assertFalse(item.isCancelled());
        assertEquals("Lịch định kỳ", item.getScheduleTypeLabel());

        item.setScheduleType("CANCELLED");
        assertTrue(item.isCancelled());
        assertEquals("Báo nghỉ", item.getScheduleTypeLabel());

        item.setScheduleType(" cancelled ");
        assertTrue(item.isCancelled());
        assertEquals("Báo nghỉ", item.getScheduleTypeLabel());

        item.setScheduleType("MAKEUP");
        assertFalse(item.isCancelled());
        assertEquals("Học bù", item.getScheduleTypeLabel());

        item.setScheduleType("MODIFIED");
        assertFalse(item.isCancelled());
        assertEquals("Thay đổi lịch", item.getScheduleTypeLabel());

        item.setScheduleType("RESCHEDULED");
        assertFalse(item.isCancelled());
        assertEquals("Thay đổi lịch", item.getScheduleTypeLabel());

        item.setScheduleType("ROOM_CHANGE");
        assertFalse(item.isCancelled());
        assertEquals("Thay đổi lịch", item.getScheduleTypeLabel());

        item.setScheduleType("REGULAR");
        assertFalse(item.isCancelled());
        assertEquals("Lịch định kỳ", item.getScheduleTypeLabel());

        item.setScheduleType("UNKNOWN");
        assertFalse(item.isCancelled());
        assertEquals("Lịch định kỳ", item.getScheduleTypeLabel());
    }

    @Test
    public void testTodayScheduleItem_timeParsingAndRealTimeStatus() {
        TodayScheduleItem item = new TodayScheduleItem();
        item.setStartTime("07:30");
        item.setEndTime("11:30:00");
        assertEquals(450, item.getStartMinutes());
        assertEquals(690, item.getEndMinutes());
        assertEquals("07:30 - 11:30", item.getFormattedTimeRange());

        // Before QR scan window (start - 15 = 435)
        int beforeWindow = 430;
        assertTrue(item.isUpcoming(beforeWindow));
        assertFalse(item.isOngoing(beforeWindow));
        assertFalse(item.canOpenOrScanQr(beforeWindow));
        assertEquals("Sắp tới", item.getRealTimeStatusLabel(beforeWindow));

        // Inside QR window but before class (435 to 449)
        int inWindow = 440;
        assertTrue(item.isUpcoming(inWindow));
        assertFalse(item.isOngoing(inWindow));
        assertTrue(item.canOpenOrScanQr(inWindow));
        assertEquals("Sắp tới", item.getRealTimeStatusLabel(inWindow));

        // Exactly start time (450)
        assertTrue(item.isOngoing(450));
        assertFalse(item.isUpcoming(450));
        assertTrue(item.canOpenOrScanQr(450));
        assertEquals("Đang diễn ra", item.getRealTimeStatusLabel(450));

        // Mid class (500)
        assertTrue(item.isOngoing(500));
        assertFalse(item.isUpcoming(500));
        assertTrue(item.canOpenOrScanQr(500));
        assertEquals("Đang diễn ra", item.getRealTimeStatusLabel(500));

        // Exactly end time (690)
        assertTrue(item.isOngoing(690));
        assertTrue(item.canOpenOrScanQr(690));
        assertEquals("Đang diễn ra", item.getRealTimeStatusLabel(690));

        // After end time (691)
        int afterClass = 691;
        assertFalse(item.isOngoing(afterClass));
        assertFalse(item.isUpcoming(afterClass));
        assertFalse(item.canOpenOrScanQr(afterClass));
        assertEquals("Đã kết thúc", item.getRealTimeStatusLabel(afterClass));

        // If cancelled, status label should always be "Báo nghỉ" and canOpenOrScanQr is false
        item.setScheduleType("CANCELLED");
        assertTrue(item.isCancelled());
        assertFalse(item.isOngoing(500));
        assertFalse(item.isUpcoming(430));
        assertFalse(item.canOpenOrScanQr(500));
        assertEquals("Báo nghỉ", item.getRealTimeStatusLabel(500));
    }

    @Test
    public void testTodayScheduleItem_invalidTimeHandling() {
        TodayScheduleItem item = new TodayScheduleItem();
        item.setStartTime("invalid");
        item.setEndTime(null);

        assertEquals(-1, item.getStartMinutes());
        assertEquals(-1, item.getEndMinutes());
        assertFalse(item.isOngoing(500));
        assertFalse(item.isUpcoming(500));
        assertFalse(item.canOpenOrScanQr(500));
        assertEquals("", item.getRealTimeStatusLabel(500));
    }

    @Test
    public void testClassScheduleItem_labelsAndTimeRange() {
        ClassScheduleItem schedule = new ClassScheduleItem(
                "s1", "c1", "SE100", "Nhập môn KT Phần mềm",
                2, "07:30:00", "11:30:00", "A101", "2026-09-01", "2026-12-31"
        );
        assertEquals("s1", schedule.getId());
        assertEquals("c1", schedule.getClassId());
        assertEquals("SE100", schedule.getClassCode());
        assertEquals("Nhập môn KT Phần mềm", schedule.getClassName());
        assertEquals(Integer.valueOf(2), schedule.getDayOfWeek());
        assertEquals("Thứ Ba", schedule.getDayOfWeekLabel());
        assertEquals("A101", schedule.getRoom());
        assertEquals("07:30 - 11:30", schedule.getFormattedTimeRange());

        schedule.setDayOfWeek(1);
        assertEquals("Thứ Hai", schedule.getDayOfWeekLabel());
        schedule.setDayOfWeek(7);
        assertEquals("Chủ Nhật", schedule.getDayOfWeekLabel());
        schedule.setDayOfWeek(null);
        assertEquals("", schedule.getDayOfWeekLabel());
    }

    @Test
    public void testScheduleExceptionItem_labelsAndTimeRange() {
        ScheduleExceptionItem exception = new ScheduleExceptionItem(
                "e1", "c1", "SE100", "Nhập môn KT Phần mềm",
                "s1", "2026-10-10", "2026-10-17",
                "MAKEUP", "PUBLISHED", "13:00", "16:30", "B202", "Dạy bù", null
        );

        assertEquals("e1", exception.getId());
        assertEquals("Học bù", exception.getExceptionTypeLabel());
        assertEquals("Đã công bố", exception.getStatusLabel());
        assertEquals("13:00 - 16:30", exception.getFormattedTimeRange());

        exception.setExceptionType("CANCELLED");
        assertEquals("Báo nghỉ", exception.getExceptionTypeLabel());

        exception.setExceptionType("MODIFIED");
        assertEquals("Thay đổi lịch", exception.getExceptionTypeLabel());

        exception.setStatus("DRAFT");
        assertEquals("Bản nháp", exception.getStatusLabel());

        exception.setStatus("REVOKED");
        assertEquals("Đã thu hồi", exception.getStatusLabel());
    }

    @Test
    public void testSemesterScheduleResponse_andRequests() {
        SemesterScheduleResponse response = new SemesterScheduleResponse();
        assertNotNull(response.getRegularSchedules());
        assertNotNull(response.getScheduleExceptions());

        CreateClassScheduleRequest scheduleReq = new CreateClassScheduleRequest(
                3, "08:00", "11:30", "C101", "2026-09-01", "2026-12-30"
        );
        assertEquals(Integer.valueOf(3), scheduleReq.getDayOfWeek());
        assertEquals("08:00", scheduleReq.getStartTime());
        assertEquals("11:30", scheduleReq.getEndTime());
        assertEquals("C101", scheduleReq.getRoom());
        assertEquals("2026-09-01", scheduleReq.getStartDate());
        assertEquals("2026-12-30", scheduleReq.getEndDate());

        CreateScheduleExceptionRequest exReq = new CreateScheduleExceptionRequest(
                "s1", "2026-11-10", "2026-11-10", "CANCELLED",
                null, null, null, "Giảng viên đi công tác", null
        );
        assertEquals("s1", exReq.getClassScheduleId());
        assertEquals("CANCELLED", exReq.getExceptionType());
        assertEquals("Giảng viên đi công tác", exReq.getReason());
    }

    @Test
    public void testHomeViewModel_loadSemesterSchedule() {
        SemesterScheduleResponse mockSemester = new SemesterScheduleResponse(
                Collections.singletonList(new ClassScheduleItem("s1", "c1", "SE100", "CS", 2, "07:30", "11:30", "A101", "2026-09-01", "2026-12-31")),
                new ArrayList<>()
        );

        UserClassApiService mockApi = createMockUserClassApi((proxy, method, args) -> {
            if ("getSemesterSchedule".equals(method.getName())) {
                return new ImmediateCall<>(createSuccess(mockSemester));
            }
            return null;
        });

        UserRepository userRepository = new UserRepository(mockApi);
        HomeViewModel homeViewModel = new HomeViewModel(userRepository);

        homeViewModel.loadSemesterSchedule();

        UiState<SemesterScheduleResponse> state = homeViewModel.getSemesterScheduleState().getValue();
        assertNotNull(state);
        assertTrue(state.isSuccess());
        assertNotNull(state.getData());
        assertEquals(1, state.getData().getRegularSchedules().size());
    }

    @Test
    public void testLecturerManagementViewModel_scheduleAndExceptionActions() {
        ClassScheduleItem scheduleItem = new ClassScheduleItem("s1", "c1", "SE100", "CS", 2, "07:30", "11:30", "A101", "2026-09-01", "2026-12-31");
        ScheduleExceptionItem exceptionItem = new ScheduleExceptionItem("e1", "c1", "SE100", "CS", "s1", "2026-10-10", "2026-10-10", "CANCELLED", "PUBLISHED", null, null, null, "Nghỉ", null);

        UserClassApiService mockApi = createMockUserClassApi((proxy, method, args) -> {
            String name = method.getName();
            if ("getClassSchedules".equals(name)) {
                return new ImmediateCall<>(createSuccess(Collections.singletonList(scheduleItem)));
            }
            if ("createClassSchedule".equals(name) || "updateClassSchedule".equals(name)) {
                return new ImmediateCall<>(createSuccess(scheduleItem));
            }
            if ("getClassScheduleExceptions".equals(name)) {
                return new ImmediateCall<>(createSuccess(Collections.singletonList(exceptionItem)));
            }
            if ("createScheduleException".equals(name) || "publishScheduleException".equals(name) || "revokeScheduleException".equals(name)) {
                return new ImmediateCall<>(createSuccess(exceptionItem));
            }
            return null;
        });

        ClassRepository classRepository = new ClassRepository(mockApi);
        LecturerManagementViewModel viewModel = new LecturerManagementViewModel(classRepository);

        // Load schedules
        viewModel.loadClassSchedules("c1");
        UiState<List<ClassScheduleItem>> schedulesState = viewModel.getClassSchedulesState().getValue();
        assertNotNull(schedulesState);
        assertTrue(schedulesState.isSuccess());
        assertEquals(1, schedulesState.getData().size());

        // Create schedule
        viewModel.createClassSchedule("c1", new CreateClassScheduleRequest(2, "07:30", "11:30", "A101", "2026-09-01", "2026-12-31"));
        UiState<ClassScheduleItem> createScheduleState = viewModel.getCreateScheduleState().getValue();
        assertNotNull(createScheduleState);
        assertTrue(createScheduleState.isSuccess());
        assertEquals("s1", createScheduleState.getData().getId());

        // Update schedule
        viewModel.updateClassSchedule("c1", "s1", new CreateClassScheduleRequest(2, "08:00", "11:30", "A102", "2026-09-01", "2026-12-31"));
        assertTrue(viewModel.getCreateScheduleState().getValue().isSuccess());

        // Reset create schedule state
        viewModel.resetCreateScheduleState();
        assertTrue(viewModel.getCreateScheduleState().getValue().isIdle());

        // Load exceptions
        viewModel.loadClassExceptions("c1");
        UiState<List<ScheduleExceptionItem>> exceptionsState = viewModel.getClassExceptionsState().getValue();
        assertNotNull(exceptionsState);
        assertTrue(exceptionsState.isSuccess());
        assertEquals(1, exceptionsState.getData().size());

        // Create exception
        viewModel.createScheduleException("c1", new CreateScheduleExceptionRequest("s1", "2026-10-10", "2026-10-10", "CANCELLED", null, null, null, "Nghỉ", null));
        assertTrue(viewModel.getCreateExceptionState().getValue().isSuccess());

        // Reset create exception state
        viewModel.resetCreateExceptionState();
        assertTrue(viewModel.getCreateExceptionState().getValue().isIdle());

        // Publish exception
        viewModel.publishScheduleException("c1", "e1");
        assertTrue(viewModel.getExceptionActionState().getValue().isSuccess());

        // Revoke exception
        viewModel.revokeScheduleException("c1", "e1");
        assertTrue(viewModel.getExceptionActionState().getValue().isSuccess());

        // Reset exception action state
        viewModel.resetExceptionActionState();
        assertTrue(viewModel.getExceptionActionState().getValue().isIdle());
    }

    @Test
    public void testHeroScheduleSelectionLogic() {
        int nowMinutes = 8 * 60 + 0; // 08:00 (480 mins)

        TodayScheduleItem finishedItem = new TodayScheduleItem();
        finishedItem.setClassId("c1");
        finishedItem.setStartTime("06:00");
        finishedItem.setEndTime("07:30");
        finishedItem.setScheduleType("REGULAR");

        TodayScheduleItem ongoingItem = new TodayScheduleItem();
        ongoingItem.setClassId("c2");
        ongoingItem.setStartTime("07:30");
        ongoingItem.setEndTime("09:30");
        ongoingItem.setScheduleType("REGULAR");

        TodayScheduleItem upcomingItem = new TodayScheduleItem();
        upcomingItem.setClassId("c3");
        upcomingItem.setStartTime("13:00");
        upcomingItem.setEndTime("17:00");
        upcomingItem.setScheduleType("REGULAR");

        TodayScheduleItem cancelledItem = new TodayScheduleItem();
        cancelledItem.setClassId("c4");
        cancelledItem.setStartTime("07:00");
        cancelledItem.setEndTime("11:00");
        cancelledItem.setScheduleType("CANCELLED");

        List<TodayScheduleItem> list = new ArrayList<>();
        list.add(finishedItem);
        list.add(ongoingItem);
        list.add(upcomingItem);
        list.add(cancelledItem);

        // Filter active
        List<TodayScheduleItem> active = new ArrayList<>();
        for (TodayScheduleItem item : list) {
            if (!item.isCancelled()) active.add(item);
        }
        assertEquals(3, active.size());

        // Hero selection: 1st priority = ongoing
        TodayScheduleItem hero = null;
        for (TodayScheduleItem item : active) {
            if (item.isOngoing(nowMinutes)) {
                hero = item;
                break;
            }
        }
        assertNotNull(hero);
        assertEquals("c2", hero.getClassId());
        assertTrue(hero.canOpenOrScanQr(nowMinutes));

        // When ongoing is finished (e.g. 10:00 -> 600 mins)
        int laterMinutes = 10 * 60;
        hero = null;
        for (TodayScheduleItem item : active) {
            if (item.isOngoing(laterMinutes)) {
                hero = item;
                break;
            }
        }
        if (hero == null) {
            for (TodayScheduleItem item : active) {
                if (item.isUpcoming(laterMinutes)) {
                    hero = item;
                    break;
                }
            }
        }
        assertNotNull(hero);
        assertEquals("c3", hero.getClassId());
        assertFalse(hero.canOpenOrScanQr(laterMinutes)); // 10:00 is not within 15 mins of 13:00

        // At 12:50 (within 15 mins of 13:00)
        int preClassMinutes = 12 * 60 + 50;
        assertTrue(hero.canOpenOrScanQr(preClassMinutes));

        // When all are finished (e.g. 18:00)
        int eveningMinutes = 18 * 60;
        hero = null;
        for (TodayScheduleItem item : active) {
            if (item.isOngoing(eveningMinutes)) {
                hero = item;
                break;
            }
        }
        if (hero == null) {
            for (TodayScheduleItem item : active) {
                if (item.isUpcoming(eveningMinutes)) {
                    hero = item;
                    break;
                }
            }
        }
        assertEquals(null, hero);
    }

    @Test
    public void testSemesterScheduleAdapterDateFormatting() {
        assertEquals("15/09/2026", com.ppquang06.uniclassapp.ui.common.adapter.SemesterScheduleAdapter.formatDate("2026-09-15"));
        assertEquals("15/09/2026", com.ppquang06.uniclassapp.ui.common.adapter.SemesterScheduleAdapter.formatDate("15/09/2026"));
        assertEquals("", com.ppquang06.uniclassapp.ui.common.adapter.SemesterScheduleAdapter.formatDate(null));
        assertEquals("", com.ppquang06.uniclassapp.ui.common.adapter.SemesterScheduleAdapter.formatDate(""));

        assertEquals("Áp dụng: 15/09/2026 - 15/01/2027",
                com.ppquang06.uniclassapp.ui.common.adapter.SemesterScheduleAdapter.formatDateRange("2026-09-15", "2027-01-15"));
        assertEquals("Áp dụng từ: 15/09/2026",
                com.ppquang06.uniclassapp.ui.common.adapter.SemesterScheduleAdapter.formatDateRange("2026-09-15", null));
        assertEquals("Áp dụng đến: 15/01/2027",
                com.ppquang06.uniclassapp.ui.common.adapter.SemesterScheduleAdapter.formatDateRange(null, "2027-01-15"));
        assertEquals("",
                com.ppquang06.uniclassapp.ui.common.adapter.SemesterScheduleAdapter.formatDateRange(null, null));
    }

    @Test
    public void testSemesterScheduleAdapterFilteringByDay() {
        com.ppquang06.uniclassapp.ui.common.adapter.SemesterScheduleAdapter adapter =
                new com.ppquang06.uniclassapp.ui.common.adapter.SemesterScheduleAdapter();

        List<ClassScheduleItem> items = new ArrayList<>();
        items.add(new ClassScheduleItem("s1", "c1", "CS101", "Mon Class", 1, "07:30", "11:30", "A101", "2026-09-15", "2027-01-15"));
        items.add(new ClassScheduleItem("s2", "c2", "CS102", "Wed Class", 3, "13:00", "17:00", "B202", "2026-09-15", "2027-01-15"));
        items.add(new ClassScheduleItem("s3", "c3", "CS103", "Fri Class", 5, "07:30", "11:30", "C303", "2026-09-15", "2027-01-15"));

        adapter.submitList(items);
        assertEquals(3, adapter.getItemCount());
        assertEquals(0, adapter.getCurrentDayFilter());

        // Filter by Monday (1)
        adapter.filterByDayOfWeek(1);
        assertEquals(1, adapter.getItemCount());
        assertEquals("CS101", adapter.getFilteredItems().get(0).getClassCode());

        // Filter by Tuesday (2) - none
        adapter.filterByDayOfWeek(2);
        assertEquals(0, adapter.getItemCount());

        // Filter by Wednesday (3)
        adapter.filterByDayOfWeek(3);
        assertEquals(1, adapter.getItemCount());
        assertEquals("CS102", adapter.getFilteredItems().get(0).getClassCode());

        // Reset to all (0)
        adapter.filterByDayOfWeek(0);
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void testScheduleExceptionAdapterDateFormatting() {
        assertEquals("20/10/2026", com.ppquang06.uniclassapp.ui.common.adapter.ScheduleExceptionAdapter.formatDate("2026-10-20"));
        assertEquals("20/10/2026", com.ppquang06.uniclassapp.ui.common.adapter.ScheduleExceptionAdapter.formatDate("20/10/2026"));
        assertEquals("", com.ppquang06.uniclassapp.ui.common.adapter.ScheduleExceptionAdapter.formatDate(null));
    }

    @Test
    public void testScheduleExceptionAdapterManagementModeAndActionListener() {
        final boolean[] published = {false};
        final boolean[] revoked = {false};

        com.ppquang06.uniclassapp.ui.common.adapter.ScheduleExceptionAdapter.OnExceptionActionListener actionListener =
                new com.ppquang06.uniclassapp.ui.common.adapter.ScheduleExceptionAdapter.OnExceptionActionListener() {
                    @Override
                    public void onPublishClick(ScheduleExceptionItem item) {
                        published[0] = true;
                    }

                    @Override
                    public void onRevokeClick(ScheduleExceptionItem item) {
                        revoked[0] = true;
                    }
                };

        com.ppquang06.uniclassapp.ui.common.adapter.ScheduleExceptionAdapter adapter =
                new com.ppquang06.uniclassapp.ui.common.adapter.ScheduleExceptionAdapter(true, actionListener);

        assertTrue(adapter.isManagementMode());
        assertEquals(actionListener, adapter.getActionListener());

        ScheduleExceptionItem draftItem = new ScheduleExceptionItem();
        draftItem.setId("e1");
        draftItem.setStatus("DRAFT");
        draftItem.setExceptionType("CANCELLED");

        adapter.getActionListener().onPublishClick(draftItem);
        assertTrue(published[0]);

        ScheduleExceptionItem pubItem = new ScheduleExceptionItem();
        pubItem.setId("e2");
        pubItem.setStatus("PUBLISHED");
        adapter.getActionListener().onRevokeClick(pubItem);
        assertTrue(revoked[0]);

        adapter.setManagementMode(false);
        assertFalse(adapter.isManagementMode());
    }

    @Test
    public void testCreateClassScheduleRequest() {
        CreateClassScheduleRequest request = new CreateClassScheduleRequest(
                2, "07:30", "11:30", "B3.10", "2026-09-01", "2027-01-15"
        );
        assertEquals(Integer.valueOf(2), request.getDayOfWeek());
        assertEquals("07:30", request.getStartTime());
        assertEquals("11:30", request.getEndTime());
        assertEquals("B3.10", request.getRoom());
        assertEquals("2026-09-01", request.getStartDate());
        assertEquals("2027-01-15", request.getEndDate());

        request.setDayOfWeek(4);
        request.setRoom("C1.05");
        assertEquals(Integer.valueOf(4), request.getDayOfWeek());
        assertEquals("C1.05", request.getRoom());
    }

    @Test
    public void testCreateScheduleExceptionRequest() {
        CreateScheduleExceptionRequest request = new CreateScheduleExceptionRequest(
                "sched-1", "2026-10-20", null, "CANCELLED", null, null, null, "Công tác", null
        );
        assertEquals("sched-1", request.getClassScheduleId());
        assertEquals("2026-10-20", request.getOriginalDate());
        assertNull(request.getOccurrenceDate());
        assertEquals("CANCELLED", request.getExceptionType());
        assertEquals("Công tác", request.getReason());

        request.setExceptionType("MAKEUP");
        request.setOccurrenceDate("2026-10-25");
        request.setStartTime("13:00");
        request.setEndTime("17:00");
        request.setRoom("A2.01");
        assertEquals("MAKEUP", request.getExceptionType());
        assertEquals("2026-10-25", request.getOccurrenceDate());
        assertEquals("13:00", request.getStartTime());
        assertEquals("17:00", request.getEndTime());
        assertEquals("A2.01", request.getRoom());
    }

    @Test
    public void testSerializationOfScheduleModels() throws Exception {
        ClassScheduleItem originalSched = new ClassScheduleItem("s1", "c1", "CS101", "Name", 1, "07:30", "11:30", "A1", "2026-09-01", "2027-01-01");
        java.io.ByteArrayOutputStream baosSched = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oosSched = new java.io.ObjectOutputStream(baosSched);
        oosSched.writeObject(originalSched);
        oosSched.close();

        java.io.ObjectInputStream oisSched = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(baosSched.toByteArray()));
        ClassScheduleItem desSched = (ClassScheduleItem) oisSched.readObject();
        assertEquals("s1", desSched.getId());
        assertEquals(Integer.valueOf(1), desSched.getDayOfWeek());
        assertEquals("07:30", desSched.getStartTime());

        ScheduleExceptionItem originalEx = new ScheduleExceptionItem("e1", "c1", "CS101", "Name", "s1", "2026-10-20", null, "CANCELLED", "DRAFT", null, null, null, "Bận việc", null);
        java.io.ByteArrayOutputStream baosEx = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oosEx = new java.io.ObjectOutputStream(baosEx);
        oosEx.writeObject(originalEx);
        oosEx.close();

        java.io.ObjectInputStream oisEx = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(baosEx.toByteArray()));
        ScheduleExceptionItem desEx = (ScheduleExceptionItem) oisEx.readObject();
        assertEquals("e1", desEx.getId());
        assertEquals("CANCELLED", desEx.getExceptionType());
        assertEquals("DRAFT", desEx.getStatus());
        assertEquals("Bận việc", desEx.getReason());
    }
}
