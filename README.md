# PeMo - Ứng dụng xem phim trực tuyến (Movie App)

Đây là dự án ứng dụng xem phim trực tuyến trên nền tảng Android, kết nối với Backend được xây dựng bằng Spring Boot. 

Dự án bao gồm 2 phần chính:
- **Frontend**: Ứng dụng Android (Ngôn ngữ Java).
- **Backend**: Server API Spring Boot (Ngôn ngữ Java), sử dụng cơ sở dữ liệu TiDB Cloud (MySQL).

---

## 🛠 Yêu cầu hệ thống (Prerequisites)

Để chạy được dự án này, máy tính cần cài đặt sẵn:
- **Android Studio** (Phiên bản mới nhất, khuyên dùng Koala/Ladybug trở lên).
- **IntelliJ IDEA** (Hoặc Eclipse) để chạy Backend.
- **Java Development Kit (JDK)** bản 11 hoặc 17.
- **Máy ảo Android (Emulator)** hoặc thiết bị thật có hệ điều hành Android 7.0 (API 24) trở lên.
- Có kết nối Internet ổn định (vì Backend sử dụng Database Cloud và các dịch vụ lưu trữ bên ngoài).

---

## 🚀 Hướng dẫn khởi chạy Backend (Spring Boot)

1. Mở phần mềm **IntelliJ IDEA**.
2. Chọn **Open** và trỏ tới đường dẫn thư mục: `backend/movie-app-server/movie-app-server`.
3. Chờ một lúc để **Maven** tự động tải xuống các thư viện cần thiết.
4. **Cấu hình (Không bắt buộc thay đổi):**
   - Project đang sử dụng **TiDB Cloud** (Cloud Database), vì vậy **KHÔNG CẦN** phải cài đặt hay chạy MySQL cục bộ trên máy. Cấu hình kết nối đã có sẵn trong file `src/main/resources/application.properties`.
   - Các API Key của TMDB (lấy dữ liệu phim), Cloudinary & ImageKit (lưu trữ ảnh/video), và VNPay Sandbox (thanh toán) cũng đã được nhúng sẵn vào file cấu hình.
5. Tìm đến file `MovieAppServerApplication.java` (trong package `com.example.movie_app_server`).
6. Nhấn chuột phải chọn **Run 'MovieAppServerApplication'** (hoặc biểu tượng Tam giác xanh).
7. Đợi log console báo `Started MovieAppServerApplication...`. Server mặc định sẽ chạy ở địa chỉ `http://localhost:8080`.

---

## 📱 Hướng dẫn khởi chạy Frontend (Android App)

1. Mở phần mềm **Android Studio**.
2. Chọn **Open** và trỏ tới thư mục gốc dự án, Android Studio sẽ tự động nhận diện module `frontend/app`.
3. Chờ Android Studio tiến hành **Gradle Sync** (tải thư viện) cho đến khi hoàn tất và không có báo lỗi (thường mất 2-5 phút).
4. **Kiểm tra file cấu hình môi trường:**
   - Mở file `frontend/app/build.gradle.kts` (Module :app).
   - Tìm đến thẻ `buildTypes -> debug` và chắc chắn dòng `BASE_URL` đang được trỏ tới máy ảo localhost:
     ```kotlin
     buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080\"")
     ```
     *(Lưu ý: Địa chỉ `10.0.2.2` là IP chuẩn để máy ảo Android kết nối ngược về `localhost:8080` của máy tính đang chạy Backend).*
5. Trên thanh công cụ trên cùng, chọn một **máy ảo (Emulator)**.
6. Nhấn nút **Run 'app'** (Biểu tượng tam giác xanh).
7. App sẽ được build và tự động khởi chạy trên máy ảo.

### ⚠️ Lưu ý về tính năng Đăng nhập (Authentication)
Ứng dụng sử dụng Firebase Authentication.
- **Đăng nhập bằng Email/Mật khẩu:** Hoạt động bình thường ở mọi máy. (Giảng viên có thể sử dụng chức năng Đăng ký tài khoản mới bằng Email để test hệ thống ngay lập tức).
- **Đăng nhập bằng Google:** Do chính sách bảo mật của Google, tính năng này bắt buộc máy tính compile code (máy tính của bạn/giảng viên) phải khai báo mã `SHA-1` trên Firebase Console của dự án. Do đó, nếu tải code về một máy tính lạ, nút "Đăng nhập Google" có thể báo Lỗi 10 (Developer Error). Đây là tính năng bảo mật hệ thống chứ không phải lỗi code, vui lòng sử dụng tính năng **Đăng ký / Đăng nhập bằng Email** thay thế để test tính năng ứng dụng.

---

## 📂 Cấu trúc dự án

- **`backend/`**: Chứa mã nguồn Server Spring Boot, các Model, Repository, Service, và Controller xử lý logic nghiệp vụ và bảo mật (JWT + Firebase Token).
- **`frontend/`**: Chứa mã nguồn Android Client. Bao gồm các Activity, Fragment, Adapter, và Retrofit interfaces (nằm trong package `com.example.pemomovie`).

Chúc bạn có trải nghiệm tốt với dự án này!
