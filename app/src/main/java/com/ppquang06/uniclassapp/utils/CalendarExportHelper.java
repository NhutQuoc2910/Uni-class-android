package com.ppquang06.uniclassapp.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.util.Log;

import com.ppquang06.uniclassapp.data.model.response.ClassScheduleItem;
import com.ppquang06.uniclassapp.data.model.response.ScheduleExceptionItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Helper for exporting the semester schedule to iCalendar (.ics) format and syncing directly
 * into the device's system Calendar via {@link CalendarContract}.
 */
public final class CalendarExportHelper {

    private static final String TAG = "CalendarExportHelper";
    private static final String SYNC_MARKER = "[UniClass-Sync]";
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter ICS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.US);
    private static final DateTimeFormatter ICS_TIME_FORMAT = DateTimeFormatter.ofPattern("HHmmss", Locale.US);

    private CalendarExportHelper() {
    }

    /**
     * Generates standard RFC 5545 iCalendar (.ics) content for regular schedules and exceptions.
     */
    public static String generateIcsContent(List<ClassScheduleItem> regularSchedules,
                                            List<ScheduleExceptionItem> exceptions) {
        List<ClassScheduleItem> safeRegular = regularSchedules != null ? regularSchedules : Collections.emptyList();
        List<ScheduleExceptionItem> safeExceptions = exceptions != null ? exceptions : Collections.emptyList();

        List<ScheduleExceptionItem> publishedExceptions = new ArrayList<>();
        for (ScheduleExceptionItem ex : safeExceptions) {
            if (ex == null) continue;
            String st = ex.getStatus();
            if (st == null || st.trim().isEmpty() || "PUBLISHED".equalsIgnoreCase(st.trim())) {
                publishedExceptions.add(ex);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//UniClass//UniClass Timetable//VN\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");
        sb.append("X-WR-CALNAME:Thời khóa biểu UniClass\r\n");
        sb.append("X-WR-TIMEZONE:Asia/Ho_Chi_Minh\r\n");

        // 1. Recurring events for regular schedules
        for (ClassScheduleItem reg : safeRegular) {
            if (reg == null || reg.getDayOfWeek() == null) continue;
            int dayOfWeek = reg.getDayOfWeek();
            if (dayOfWeek < 1 || dayOfWeek > 7) continue;

            LocalDate firstDate = resolveFirstOccurrenceDate(reg.getStartDate(), dayOfWeek);
            LocalDate endDate = WeeklyTimetableCalculator.parseLocalDate(reg.getEndDate());
            if (endDate != null && firstDate.isAfter(endDate)) {
                continue;
            }

            LocalTime startTime = WeeklyTimetableCalculator.parseLocalTime(reg.getStartTime());
            LocalTime endTime = WeeklyTimetableCalculator.parseLocalTime(reg.getEndTime());
            if (startTime == null) startTime = LocalTime.of(7, 30);
            if (endTime == null || !endTime.isAfter(startTime)) endTime = startTime.plusMinutes(90);

            String dtStart = firstDate.format(ICS_DATE_FORMAT) + "T" + startTime.format(ICS_TIME_FORMAT);
            String dtEnd = firstDate.format(ICS_DATE_FORMAT) + "T" + endTime.format(ICS_TIME_FORMAT);
            String byDay = toIcsByDay(dayOfWeek);

            StringBuilder rrule = new StringBuilder("FREQ=WEEKLY;BYDAY=").append(byDay);
            if (endDate != null) {
                rrule.append(";UNTIL=").append(endDate.format(ICS_DATE_FORMAT)).append("T235959Z");
            }

            String uid = "uniclass-reg-" + (reg.getId() != null ? reg.getId() : (reg.getClassId() + "-" + dayOfWeek + "-" + dtStart)) + "@uniclass.app";
            String summary = formatEventTitle(reg.getClassCode(), reg.getClassName(), null);
            String location = formatRoom(reg.getRoom());
            String description = "Lịch học định kỳ môn " + safeStr(reg.getClassName()) + " (" + safeStr(reg.getClassCode()) + ") " + SYNC_MARKER;

            sb.append("BEGIN:VEVENT\r\n");
            sb.append("UID:").append(escapeIcsText(uid)).append("\r\n");
            sb.append("DTSTART;TZID=Asia/Ho_Chi_Minh:").append(dtStart).append("\r\n");
            sb.append("DTEND;TZID=Asia/Ho_Chi_Minh:").append(dtEnd).append("\r\n");
            sb.append("RRULE:").append(rrule).append("\r\n");

            // Add EXDATE for CANCELLED or MODIFIED occurrences of this schedule
            for (ScheduleExceptionItem ex : publishedExceptions) {
                String exType = ex.getExceptionType() != null ? ex.getExceptionType().trim().toUpperCase(Locale.US) : "";
                if (!"CANCELLED".equals(exType) && !"MODIFIED".equals(exType)) continue;
                if (!matchesSchedule(reg, ex)) continue;
                LocalDate origDate = WeeklyTimetableCalculator.parseLocalDate(ex.getOriginalDate());
                if (origDate != null) {
                    String exDateStr = origDate.format(ICS_DATE_FORMAT) + "T" + startTime.format(ICS_TIME_FORMAT);
                    sb.append("EXDATE;TZID=Asia/Ho_Chi_Minh:").append(exDateStr).append("\r\n");
                }
            }

            sb.append("SUMMARY:").append(escapeIcsText(summary)).append("\r\n");
            if (!location.isEmpty()) {
                sb.append("LOCATION:").append(escapeIcsText(location)).append("\r\n");
            }
            sb.append("DESCRIPTION:").append(escapeIcsText(description)).append("\r\n");
            sb.append("END:VEVENT\r\n");
        }

        // 2. Single events for MAKEUP and MODIFIED exceptions
        for (ScheduleExceptionItem ex : publishedExceptions) {
            String exType = ex.getExceptionType() != null ? ex.getExceptionType().trim().toUpperCase(Locale.US) : "";
            if (!"MAKEUP".equals(exType) && !"MODIFIED".equals(exType)) {
                continue;
            }
            LocalDate eventDate = "MAKEUP".equals(exType)
                    ? WeeklyTimetableCalculator.parseLocalDate(ex.getOccurrenceDate())
                    : WeeklyTimetableCalculator.parseLocalDate(ex.getOriginalDate());
            if (eventDate == null) {
                eventDate = WeeklyTimetableCalculator.parseLocalDate(ex.getOriginalDate());
            }
            if (eventDate == null) continue;

            LocalTime startTime = WeeklyTimetableCalculator.parseLocalTime(ex.getStartTime());
            LocalTime endTime = WeeklyTimetableCalculator.parseLocalTime(ex.getEndTime());
            if (startTime == null) startTime = LocalTime.of(7, 30);
            if (endTime == null || !endTime.isAfter(startTime)) endTime = startTime.plusMinutes(90);

            String dtStart = eventDate.format(ICS_DATE_FORMAT) + "T" + startTime.format(ICS_TIME_FORMAT);
            String dtEnd = eventDate.format(ICS_DATE_FORMAT) + "T" + endTime.format(ICS_TIME_FORMAT);
            String prefix = "MAKEUP".equals(exType) ? "Học bù" : "Đổi lịch";
            String summary = formatEventTitle(ex.getClassCode(), ex.getClassName(), prefix);
            String location = formatRoom(ex.getRoom());
            String reason = ex.getReason() != null && !ex.getReason().trim().isEmpty()
                    ? "Lý do: " + ex.getReason().trim() + " - "
                    : "";
            String description = reason + SYNC_MARKER;
            String uid = "uniclass-ex-" + (ex.getId() != null ? ex.getId() : (ex.getClassId() + "-" + dtStart)) + "@uniclass.app";

            sb.append("BEGIN:VEVENT\r\n");
            sb.append("UID:").append(escapeIcsText(uid)).append("\r\n");
            sb.append("DTSTART;TZID=Asia/Ho_Chi_Minh:").append(dtStart).append("\r\n");
            sb.append("DTEND;TZID=Asia/Ho_Chi_Minh:").append(dtEnd).append("\r\n");
            sb.append("SUMMARY:").append(escapeIcsText(summary)).append("\r\n");
            if (!location.isEmpty()) {
                sb.append("LOCATION:").append(escapeIcsText(location)).append("\r\n");
            }
            sb.append("DESCRIPTION:").append(escapeIcsText(description)).append("\r\n");
            sb.append("END:VEVENT\r\n");
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    /**
     * Saves the .ics content to Downloads/UniClass and returns its Uri.
     */
    public static Uri saveIcsFileToDownloads(Context context, String icsContent) {
        if (context == null || icsContent == null || icsContent.isEmpty()) {
            return null;
        }
        String fileName = "ThoiKhoaBieu_UniClass_" + System.currentTimeMillis() + ".ics";
        byte[] bytes = icsContent.getBytes(StandardCharsets.UTF_8);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "text/calendar");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/UniClass");
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            ContentResolver resolver = context.getContentResolver();
            Uri uri = null;
            try {
                uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) return null;
                try (OutputStream os = resolver.openOutputStream(uri)) {
                    if (os == null) return null;
                    os.write(bytes);
                    os.flush();
                }
                values.clear();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                resolver.update(uri, values, null, null);
                return uri;
            } catch (Exception e) {
                Log.e(TAG, "Failed to save .ics to MediaStore.Downloads", e);
                if (uri != null) {
                    try {
                        resolver.delete(uri, null, null);
                    } catch (Exception ignored) {
                    }
                }
                return null;
            }
        } else {
            try {
                File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (dir == null) {
                    dir = context.getCacheDir();
                }
                if (!dir.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    dir.mkdirs();
                }
                File file = new File(dir, fileName);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(bytes);
                    fos.flush();
                }
                return Uri.fromFile(file);
            } catch (Exception e) {
                Log.e(TAG, "Failed to save .ics to legacy directory", e);
                return null;
            }
        }
    }

    /**
     * Opens the saved .ics Uri with the system Calendar / Google Calendar app if available.
     */
    public static void openIcsUri(Context context, Uri uri) {
        if (context == null || uri == null) return;
        try {
            Intent viewIntent = new Intent(Intent.ACTION_VIEW);
            viewIntent.setDataAndType(uri, "text/calendar");
            viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(viewIntent, "Mở bằng ứng dụng Lịch"));
        } catch (Exception e) {
            Log.w(TAG, "No direct viewer for text/calendar", e);
        }
    }

    /**
     * Directly inserts recurring regular schedules and makeup/modified sessions into the device's
     * system Calendar via {@link CalendarContract}. Requires READ_CALENDAR and WRITE_CALENDAR permissions.
     *
     * @return Number of calendar events synced, or -1 on error.
     */
    public static int syncDirectlyToDeviceCalendar(Context context,
                                                   List<ClassScheduleItem> regularSchedules,
                                                   List<ScheduleExceptionItem> exceptions) {
        if (context == null) return -1;
        try {
            ContentResolver resolver = context.getContentResolver();
            long calendarId = findOrCreateTargetCalendarId(resolver);
            if (calendarId < 0) {
                return -1;
            }

            // Remove old UniClass synced events to avoid duplication
            try {
                resolver.delete(
                        CalendarContract.Events.CONTENT_URI,
                        CalendarContract.Events.CALENDAR_ID + " = ? AND " + CalendarContract.Events.DESCRIPTION + " LIKE ?",
                        new String[]{String.valueOf(calendarId), "%" + SYNC_MARKER + "%"}
                );
            } catch (Exception e) {
                Log.w(TAG, "Could not clean previous synced events", e);
            }

            int syncedCount = 0;
            List<ClassScheduleItem> safeRegular = regularSchedules != null ? regularSchedules : Collections.emptyList();
            List<ScheduleExceptionItem> safeExceptions = exceptions != null ? exceptions : Collections.emptyList();

            for (ClassScheduleItem reg : safeRegular) {
                if (reg == null || reg.getDayOfWeek() == null) continue;
                int dayOfWeek = reg.getDayOfWeek();
                if (dayOfWeek < 1 || dayOfWeek > 7) continue;

                LocalDate firstDate = resolveFirstOccurrenceDate(reg.getStartDate(), dayOfWeek);
                LocalDate endDate = WeeklyTimetableCalculator.parseLocalDate(reg.getEndDate());
                if (endDate != null && firstDate.isAfter(endDate)) {
                    continue;
                }

                LocalTime startTime = WeeklyTimetableCalculator.parseLocalTime(reg.getStartTime());
                LocalTime endTime = WeeklyTimetableCalculator.parseLocalTime(reg.getEndTime());
                if (startTime == null) startTime = LocalTime.of(7, 30);
                if (endTime == null || !endTime.isAfter(startTime)) endTime = startTime.plusMinutes(90);

                long startMillis = ZonedDateTime.of(firstDate, startTime, VN_ZONE).toInstant().toEpochMilli();
                long durationMinutes = Math.max(15, Duration.between(startTime, endTime).toMinutes());

                String byDay = toIcsByDay(dayOfWeek);
                StringBuilder rrule = new StringBuilder("FREQ=WEEKLY;BYDAY=").append(byDay);
                if (endDate != null) {
                    rrule.append(";UNTIL=").append(endDate.format(ICS_DATE_FORMAT)).append("T235959Z");
                }

                ContentValues values = new ContentValues();
                values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
                values.put(CalendarContract.Events.TITLE, formatEventTitle(reg.getClassCode(), reg.getClassName(), null));
                values.put(CalendarContract.Events.EVENT_LOCATION, formatRoom(reg.getRoom()));
                values.put(CalendarContract.Events.DESCRIPTION,
                        "Lịch học định kỳ môn " + safeStr(reg.getClassName()) + " (" + safeStr(reg.getClassCode()) + ") " + SYNC_MARKER);
                values.put(CalendarContract.Events.DTSTART, startMillis);
                values.put(CalendarContract.Events.DURATION, "PT" + durationMinutes + "M");
                values.put(CalendarContract.Events.RRULE, rrule.toString());
                values.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Ho_Chi_Minh");

                Uri inserted = resolver.insert(CalendarContract.Events.CONTENT_URI, values);
                if (inserted != null) {
                    syncedCount++;
                }
            }

            for (ScheduleExceptionItem ex : safeExceptions) {
                if (ex == null) continue;
                String st = ex.getStatus();
                if (st != null && !st.trim().isEmpty() && !"PUBLISHED".equalsIgnoreCase(st.trim())) {
                    continue;
                }
                String exType = ex.getExceptionType() != null ? ex.getExceptionType().trim().toUpperCase(Locale.US) : "";
                if (!"MAKEUP".equals(exType) && !"MODIFIED".equals(exType)) {
                    continue;
                }

                LocalDate eventDate = "MAKEUP".equals(exType)
                        ? WeeklyTimetableCalculator.parseLocalDate(ex.getOccurrenceDate())
                        : WeeklyTimetableCalculator.parseLocalDate(ex.getOriginalDate());
                if (eventDate == null) {
                    eventDate = WeeklyTimetableCalculator.parseLocalDate(ex.getOriginalDate());
                }
                if (eventDate == null) continue;

                LocalTime startTime = WeeklyTimetableCalculator.parseLocalTime(ex.getStartTime());
                LocalTime endTime = WeeklyTimetableCalculator.parseLocalTime(ex.getEndTime());
                if (startTime == null) startTime = LocalTime.of(7, 30);
                if (endTime == null || !endTime.isAfter(startTime)) endTime = startTime.plusMinutes(90);

                long startMillis = ZonedDateTime.of(eventDate, startTime, VN_ZONE).toInstant().toEpochMilli();
                long endMillis = ZonedDateTime.of(eventDate, endTime, VN_ZONE).toInstant().toEpochMilli();
                String prefix = "MAKEUP".equals(exType) ? "Học bù" : "Đổi lịch";

                ContentValues values = new ContentValues();
                values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
                values.put(CalendarContract.Events.TITLE, formatEventTitle(ex.getClassCode(), ex.getClassName(), prefix));
                values.put(CalendarContract.Events.EVENT_LOCATION, formatRoom(ex.getRoom()));
                values.put(CalendarContract.Events.DESCRIPTION,
                        (ex.getReason() != null && !ex.getReason().trim().isEmpty() ? "Lý do: " + ex.getReason().trim() + "\n" : "") + SYNC_MARKER);
                values.put(CalendarContract.Events.DTSTART, startMillis);
                values.put(CalendarContract.Events.DTEND, endMillis);
                values.put(CalendarContract.Events.EVENT_TIMEZONE, "Asia/Ho_Chi_Minh");

                Uri inserted = resolver.insert(CalendarContract.Events.CONTENT_URI, values);
                if (inserted != null) {
                    syncedCount++;
                }
            }

            return syncedCount;
        } catch (SecurityException se) {
            Log.e(TAG, "Missing calendar permissions", se);
            return -1;
        } catch (Exception e) {
            Log.e(TAG, "Error syncing to device calendar", e);
            return -1;
        }
    }

    private static long findOrCreateTargetCalendarId(ContentResolver resolver) {
        String[] projection = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.IS_PRIMARY,
                CalendarContract.Calendars.VISIBLE,
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
        };
        long fallbackId = -1;
        try (Cursor cursor = resolver.query(CalendarContract.Calendars.CONTENT_URI, projection, null, null, null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(0);
                    int isPrimary = cursor.getInt(1);
                    int visible = cursor.getInt(2);
                    int access = cursor.getInt(3);
                    if (access >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR) {
                        if (isPrimary == 1) {
                            return id;
                        }
                        if (fallbackId < 0 || visible == 1) {
                            fallbackId = id;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Query existing calendars failed", e);
        }

        if (fallbackId >= 0) {
            return fallbackId;
        }

        // Create a local calendar if the device/emulator has no Google Calendar account configured
        try {
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Calendars.ACCOUNT_NAME, "UniClass");
            values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
            values.put(CalendarContract.Calendars.NAME, "Thời khóa biểu UniClass");
            values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "Thời khóa biểu UniClass");
            values.put(CalendarContract.Calendars.CALENDAR_COLOR, 0xFF2563EB);
            values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
            values.put(CalendarContract.Calendars.OWNER_ACCOUNT, "UniClass");
            values.put(CalendarContract.Calendars.VISIBLE, 1);
            values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
            values.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, "Asia/Ho_Chi_Minh");

            Uri creationUri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
                    .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                    .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, "UniClass")
                    .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                    .build();

            Uri created = resolver.insert(creationUri, values);
            if (created != null && created.getLastPathSegment() != null) {
                return Long.parseLong(created.getLastPathSegment());
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create local fallback calendar", e);
        }

        return -1;
    }

    public static LocalDate resolveFirstOccurrenceDate(String rawStartDate, int targetDayOfWeek) {
        LocalDate base = WeeklyTimetableCalculator.parseLocalDate(rawStartDate);
        if (base == null) {
            base = WeeklyTimetableCalculator.getMondayOfWeek(LocalDate.now(VN_ZONE));
        }
        int safeTarget = Math.max(1, Math.min(7, targetDayOfWeek));
        while (base.getDayOfWeek().getValue() != safeTarget) {
            base = base.plusDays(1);
        }
        return base;
    }

    public static String toIcsByDay(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1:
                return "MO";
            case 2:
                return "TU";
            case 3:
                return "WE";
            case 4:
                return "TH";
            case 5:
                return "FR";
            case 6:
                return "SA";
            case 7:
                return "SU";
            default:
                return "MO";
        }
    }

    private static boolean matchesSchedule(ClassScheduleItem reg, ScheduleExceptionItem ex) {
        if (reg.getId() != null && ex.getClassScheduleId() != null) {
            return reg.getId().equals(ex.getClassScheduleId());
        }
        return reg.getClassId() != null && reg.getClassId().equals(ex.getClassId());
    }

    private static String formatEventTitle(String classCode, String className, String prefix) {
        String code = safeStr(classCode);
        String name = safeStr(className);
        String tag;
        if (prefix != null && !prefix.isEmpty()) {
            tag = !code.isEmpty() ? "[" + prefix + " - " + code + "] " : "[" + prefix + "] ";
        } else {
            tag = !code.isEmpty() ? "[" + code + "] " : "";
        }
        return (tag + (!name.isEmpty() ? name : "Lớp học")).trim();
    }

    private static String formatRoom(String rawRoom) {
        if (rawRoom == null || rawRoom.trim().isEmpty()) {
            return "";
        }
        String r = rawRoom.trim();
        if (r.toLowerCase(Locale.ROOT).startsWith("phòng")) {
            return r;
        }
        return "Phòng " + r;
    }

    private static String safeStr(String s) {
        return s != null ? s.trim() : "";
    }

    private static String escapeIcsText(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n");
    }
}
