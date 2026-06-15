# Movie App Project

Dự án ứng dụng xem phim với Backend viết bằng Spring Boot và Frontend là ứng dụng di động Android (Java).

## Cấu trúc dự án
- `backend/movie-app-server/`: Chứa mã nguồn máy chủ (Spring Boot).
- `frontend/app/`: Chứa mã nguồn ứng dụng di động Android.

## Yêu cầu môi trường
1. **Java JDK 17+**
2. **Android Studio** (Phiên bản mới nhất)
3. **MySQL Server** (Đang chạy ở cổng 3306)

---

## Hướng dẫn cài đặt Backend

### 1. Chuẩn bị Cơ sở dữ liệu (Database)
- Đảm bảo bạn đã cài đặt và khởi động MySQL.
- Bạn cần tạo trước một Schema tên là `movieapp` hoặc cấu hình `createDatabaseIfNotExist=true` sẽ tự động tạo.
- Cấu hình tài khoản đăng nhập MySQL trong file `backend/movie-app-server/movie-app-server/src/main/resources/application.properties`:
  ```properties
  spring.datasource.username=root
  spring.datasource.password=123456
  ```

### 2. Dữ liệu phim (Data Seeder)
- Dự án sử dụng API của **TMDB** để tự động kéo dữ liệu về DB khi khởi động.
- Nếu không có mạng hoặc không kết nối được TMDB, dự án có sẵn cơ chế **MockDataSeeder** để tạo các bộ phim mẫu (Your Name, Interstellar, Deadpool & Wolverine...) để test.
- Bạn có thể cấu hình Token TMDB, Cloudinary, VNPay, Momo tại `application.properties`.

### 3. Khởi động Backend
- Mở thư mục `backend/movie-app-server/` bằng IntelliJ IDEA hoặc Eclipse.
- Chạy file `MovieAppServerApplication.java`.
- Server sẽ khởi động mặc định ở cổng `8080`. Chú ý: trong lần chạy đầu tiên, hệ thống sẽ mất chút thời gian để tự động tải/tạo dữ liệu phim vào Database (do `spring.jpa.hibernate.ddl-auto=create`).

---

## Hướng dẫn cài đặt Frontend (Android)

1. Mở thư mục `frontend/` bằng **Android Studio**.
2. Đợi Gradle đồng bộ (Sync) xong các thư viện.
3. Đảm bảo cấu hình IP kết nối tới Backend chính xác trong file cấu hình mạng của app (Ví dụ: `ApiClient` hoặc file XML Network Security) nếu bạn định chạy trên điện thoại thật. Nếu chạy máy ảo, có thể dùng `10.0.2.2`.
4. Chọn máy ảo (Emulator) hoặc cắm thiết bị thật vào và bấm **Run (Shift + F10)**.

## Lưu ý chung
- Không đưa các file `build/`, file cấu hình cục bộ của IDE (`.idea/`, `.gradle/`) lên Git. Đã có `.gitignore` xử lý việc này.
- Khi làm việc với thanh điều hướng hay giao diện tràn viền (Edge-to-Edge), chú ý các thiết lập `WindowInsets` để tránh bị cắt xén UI.