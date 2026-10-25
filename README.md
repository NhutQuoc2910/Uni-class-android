# Uni-Class - Android Mobile Application 🎓📱

> Ứng dụng di động quản lý lớp học đại học & điểm danh chống gian lận đa tầng dành cho **Sinh viên** và **Giảng viên**.

---

## 1. Giới thiệu tổng quan

**Uni-Class Android** là phân hệ mobile client thuộc hệ sinh thái Uni-Class, phục vụ hai đối tượng người dùng chính: **Giảng viên** và **Sinh viên**. Ứng dụng cung cấp các công cụ quản lý lớp học thực tế như upload/download tài liệu, giao và nộp bài tập file, xem điểm số, và đặc biệt là hệ thống **Điểm danh QR động chống gian lận 3 lớp** kết hợp định vị WiFi BSSID, toạ độ GPS và ràng buộc thiết bị phần cứng.

---

## 2. Tính năng nổi bật

### Dành cho Sinh viên

- **Ghi danh lớp học:** Tham gia lớp nhanh chóng qua mã mời (invite code) do giảng viên cung cấp.
- **Điểm danh QR 3 lớp bảo mật:**
  - Quét mã QR xoay vòng trực tiếp từ màn hình giảng viên.
  - Tự động thu thập WiFi BSSID (đang kết nối) và tọa độ vị trí GPS hiện tại gửi lên server đối soát.
  - Xác thực khóa định danh thiết bị (`Android ID`) đảm bảo **1 thiết bị chỉ điểm danh cho 1 tài khoản**.
- **Học tập & Tài liệu:** Xem danh sách môn học, đọc thông báo lớp, tải slide/tài liệu bài giảng (PDF, Word, PPT).
- **Nộp bài tập:** Nộp bài làm dạng file (PDF, Word, ZIP, Ảnh) trực tiếp lên Supabase Storage trước deadline.
- **Theo dõi học tập:** Tra cứu điểm thi, xem nhận xét từ giảng viên, xem lịch sử điểm danh với thanh cảnh báo đỏ khi tỷ lệ vắng vượt ngưỡng $20\%$.
- **Thông báo tức thời:** Nhận Push Notification qua Firebase Cloud Messaging (FCM) khi có bài tập mới, thông báo dời lịch, nhắc deadline.

### Dành cho Giảng viên

- **Tạo & Cấu hình lớp học:** Thiết lập thông tin môn học, mã lớp, học kỳ, cấp mã mời cho sinh viên.
- **Phiên điểm danh chống gian lận Live:**
  - Khởi tạo phiên điểm danh, nhận payload JWT qua WebSocket.
  - Trình chiếu mã QR động tự làm mới mỗi 30 giây (xoay vòng token, vô hiệu hóa mã cũ chụp ảnh gửi ra ngoài).
  - **Live Dashboard:** Giám sát danh sách sinh viên quét mã thành công nhảy số theo thời gian thực (Supabase Realtime).
  - Chốt phiên và tự động tính toán tỷ lệ chuyên cần lưu trữ vào PostgreSQL.
- **Quản trị học tập:**
  - Upload bài giảng, slide lên Supabase Storage.
  - Đăng thông báo khẩn (đổi phòng, dời lịch) tự động kích hoạt FCM Push đến toàn bộ sinh viên.
  - Tạo đề bài tập kèm hạn chót (deadline).
  - Tải hàng loạt file nộp bài của sinh viên về máy để chấm.
  - Nhập điểm thủ công và phản hồi trực tiếp cho từng bài nộp.
  - Thống kê phổ điểm và danh sách sinh viên có nguy cơ cấm thi (vắng $> 20\%$).

---

## 🛠 3. Kiến trúc & Công nghệ sử dụng

- **Ngôn ngữ:** Java (tương thích JDK 17).
- **Kiến trúc phần mềm:** Clean Architecture kết hợp **MVVM (Model - View - ViewModel)**.
- **Giao diện (UI):**
  - XML Layout chuẩn Material Design 3 (Material Components).
  - Single Activity Architecture phối hợp **Jetpack Navigation Component**.
  - **ViewBinding** loại bỏ `findViewById`.
- **Phần cứng & Camera:** CameraX kết hợp ZXing Barcode Scanner để nhận diện QR Code tốc độ cao.
- **Xử lý nền & Dữ liệu cục bộ:**
  - Room DB (lưu trữ cache ngoại tuyến lớp học và lịch sử).
  - WorkManager (đồng bộ ngầm các tác vụ tải tệp, retry nộp bài).
  - LiveData & ViewModel (quản lý trạng thái và vòng đời Fragment).
- **Giao tiếp mạng:**
  - Retrofit 2 + OkHttp 3 (REST API Client).
  - OkHttp WebSocket Client (nhận token QR động và trạng thái realtime).
- **Đám mây & Thông báo:**
  - Supabase Client (Auth Session & Realtime).
  - Firebase Cloud Messaging (FCM SDK).

---

## 📂 4. Quy ước đặt tên & Cấu trúc thư mục

Giao diện áp dụng kiến trúc mỗi màn hình là 1 Fragment, tiền tố layout và fragment tuân thủ nghiêm ngặt theo vai trò:

```
app/src/main/
├── java/vn/edu/uit/Uni-Class/
│   ├── data/                 # Repositories, Local DB (Room), Remote API
│   │   ├── local/
│   │   ├── remote/           # Retrofit Services, Supabase API
│   │   └── repository/
│   ├── model/                # Data Classes, DTOs, Enums
│   ├── security/             # Device Binding (Android ID), Crypto helper
│   ├── ui/                   # Giao diện người dùng
│   │   ├── auth/             # Login, Register, ForgotPassword Fragment
│   │   ├── student/          # Màn hình sinh viên
│   │   │   ├── classlist/    # student/class_list
│   │   │   ├── classdetail/  # student/class_detail
│   │   │   ├── qrscan/       # student/qr_scan (CameraX + GPS + WiFi)
│   │   │   ├── assignment/   # student/assignment_submit
│   │   │   └── profile/      # student/profile
│   │   ├── lecturer/         # Màn hình giảng viên
│   │   │   ├── classlist/    # lecturer/class_list
│   │   │   ├── qrsession/    # lecturer/qr_session (Live 30s Dynamic QR)
│   │   │   ├── grading/      # lecturer/assignment_grading
│   │   │   └── statistics/   # lecturer/class_statistics
│   │   └── common/           # Custom Views, Adapters, Dialogs
│   └── util/                 # WifiManager, LocationHelper, Constants
└── res/
    ├── layout/
    │   ├── fragment_student_class_list.xml
    │   ├── fragment_student_qr_scan.xml
    │   ├── fragment_student_assignment_submit.xml
    │   ├── fragment_lecturer_qr_session.xml
    │   ├── fragment_lecturer_assignment_grading.xml
    │   └── ...
    └── navigation/
        └── nav_graph.xml     # Navigation Component Graph
```
