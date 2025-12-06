# StuShare - Cộng đồng chia sẻ tài liệu học tập 📚

<p align="center">
  <img src="app/src/main/res/drawable/logo.png" alt="StuShare Logo" width="150"/>
</p>

---

## 1. Lý do ra đời 💡
Trong môi trường đại học, việc tìm kiếm tài liệu ôn thi, bài giảng hay các giáo trình tham khảo chất lượng thường gặp nhiều khó khăn do thông tin nằm rải rác và thiếu tính xác thực.

**StuShare** ra đời với sứ mệnh kết nối cộng đồng sinh viên, tạo ra một nền tảng tập trung, tin cậy để mọi người có thể:
* Chia sẻ tài liệu cá nhân.
* Tìm kiếm nhanh chóng tài liệu mình cần.
* Hỗ trợ nhau giải đáp thắc mắc và cùng nhau tiến bộ.

## 2. Đối tượng người dùng 🎯
* **Sinh viên:** Đặc biệt là sinh viên các trường Cao đẳng, Đại học muốn tìm kiếm tài liệu ôn thi, giáo trình (CNTT, Kinh tế, Cơ khí...).
* **Giảng viên/Trợ giảng:** Muốn chia sẻ tài liệu tham khảo chính thống cho sinh viên.
* **Quản trị viên (Admin):** Người chịu trách nhiệm duy trì chất lượng nội dung và sự lành mạnh của cộng đồng.

## 3. Các tính năng chính ✨

### 👤 Người dùng (Sinh viên)
* **Đăng nhập đa nền tảng:** Hỗ trợ Email/Mật khẩu, Google Sign-In, và Xác thực số điện thoại (OTP).
* **Kho tài liệu phong phú:** Truy cập tài liệu mới nhất, tài liệu ôn thi, sách giáo trình, bài giảng.
* **Tìm kiếm & Lọc:** Tìm tài liệu theo từ khóa, môn học hoặc loại file.
* **Tương tác tài liệu:** Xem trước (PDF Viewer tích hợp), Tải xuống, và Đánh giá chất lượng.
* **Đăng tải tài liệu:** Upload file PDF trực tiếp từ thiết bị với tiêu đề và mô tả chi tiết.
* **Góc yêu cầu (Community Request):** Đăng bài nhờ cộng đồng tìm giúp tài liệu và bình luận trao đổi.
* **Bảng xếp hạng (Leaderboard):** Vinh danh top thành viên có đóng góp tích cực nhất.
* **Hồ sơ cá nhân:** Quản lý tài liệu đã đăng, tài liệu đã lưu và chỉnh sửa thông tin cá nhân.

### 🛠️ Quản trị viên (Admin Dashboard)
* **Thống kê tổng quan:** Xem số lượng người dùng, tài liệu và yêu cầu trong hệ thống.
* **Quản lý báo cáo:** Xem xét và xử lý các báo cáo vi phạm nội dung từ người dùng.
* **Quản lý người dùng:** Danh sách người dùng, thực hiện Khóa (Ban) hoặc Mở khóa tài khoản vi phạm.
* **Thông báo hệ thống:** Gửi thông báo đến toàn bộ người dùng hoặc một người dùng cụ thể qua Email.

## 4. Công nghệ sử dụng 💻

Dự án được phát triển Native Android với kiến trúc hiện đại, đảm bảo hiệu năng và khả năng mở rộng:

* **Ngôn ngữ:** [Kotlin](https://kotlinlang.org/) (100%)
* **Kiến trúc:** MVVM (Model-View-ViewModel) + Clean Architecture
* **Giao diện (UI):** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material Design 3)
* **Dependency Injection:** [Hilt](https://dagger.dev/hilt/)
* **Xử lý bất đồng bộ:** Coroutines & Flow
* **Network:** Retrofit + Moshi
* **Database Local:** Room Database
* **Backend & Cloud (Firebase):**
    * **Authentication:** Quản lý đăng nhập/đăng ký.
    * **Cloud Firestore:** Cơ sở dữ liệu NoSQL thời gian thực.
    * **Cloud Storage:** Lưu trữ file tài liệu và hình ảnh.
* **Điều hướng:** Navigation Compose (Type-Safe)
* **Thư viện khác:** Coil (Load ảnh), WorkManager (Tác vụ nền).

## 5. Hướng phát triển 🚀
* [ ] **Tích hợp AI:** Tự động tóm tắt nội dung tài liệu PDF khi upload.
* [ ] **Chế độ Offline:** Cho phép xem tài liệu đã lưu mà không cần mạng.
* [ ] **Chat Realtime:** Nhắn tin cá nhân giữa các sinh viên.
* [ ] **Hỗ trợ đa định dạng:** Mở rộng hỗ trợ file Word (.docx), Powerpoint (.pptx).
* [ ] **Phiên bản Web:** Đồng bộ dữ liệu đa nền tảng.

## 6. Cách cài đặt và chạy dự án ⚙️

### Yêu cầu hệ thống
* Android Studio (Phiên bản Ladybug hoặc mới hơn).
* JDK 11 hoặc 17.
* Thiết bị Android hoặc Emulator chạy Android 7.0 (API 24) trở lên.

### Các bước thực hiện

1.  **Clone dự án:**
    ```bash
    git clone [https://github.com/username/StuShare.git](https://github.com/username/StuShare.git)
    cd StuShare
    ```

2.  **Cấu hình Firebase (BẮT BUỘC):**
    * Tạo project mới trên [Firebase Console](https://console.firebase.google.com/).
    * Thêm ứng dụng Android với package name: `com.example.stushare`.
    * Tải file `google-services.json` về và đặt vào thư mục `app/`.
    * **Quan trọng:** Để dùng Google Sign-In, bạn cần thêm **SHA-1 Fingerprint** của máy bạn vào phần *Project Settings* trên Firebase.

3.  **Cấu hình biến môi trường (Nếu có):**
    * Kiểm tra file `libs.versions.toml` và `build.gradle.kts` để đảm bảo các version thư viện tương thích.

4.  **Chạy dự án:**
    * Mở dự án bằng Android Studio.
    * Đợi Gradle sync hoàn tất.
    * Nhấn nút **Run** (▶️) hoặc chạy lệnh terminal:
    ```bash
    ./gradlew installDebug
    ```

---
**Dự án học tập phát triển bởi:** [Tên Của Bạn]
**Liên hệ:** dungdao108@gmail.com