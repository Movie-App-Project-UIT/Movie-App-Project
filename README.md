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
  spring.datasource.password=123456 hoặc tùy chỉnh mật khẩu MySQL của bạn
  ```

### 2. Dữ liệu phim (Data Seeder)
- Dự án sử dụng API của **TMDB** để tự động kéo dữ liệu về DB khi khởi động.
- Nếu không có mạng hoặc không kết nối được TMDB, dự án có sẵn cơ chế **MockDataSeeder** để tạo các bộ phim mẫu (Your Name, Interstellar, Deadpool & Wolverine...) để test.
- Bạn có thể cấu hình Token TMDB, Cloudinary, VNPay, Momo tại `application.properties`.

### 3. Khởi động Backend
- Mở thư mục `backend/movie-app-server/` bằng IntelliJ IDEA hoặc Eclipse.
- Chạy file `MovieAppServerApplication.java`.
- Server sẽ khởi động mặc định ở cổng `8080`. Chú ý:
  + Hiện tại `spring.jpa.hibernate.ddl-auto=update` nên dữ liệu sẽ KHÔNG bị mất khi khởi động lại Backend.
  + Hệ thống đã được cấu hình tự động lấy phim từ TMDB (`TmdbDataSeeder` có quyền ưu tiên cao nhất `@Order(1)`). Nếu DB đã có phim, hệ thống sẽ bỏ qua việc tải thêm.
  + Để lấy lại dữ liệu mới nhất (bao gồm cả ngôn ngữ, thể loại chuẩn), hãy xóa (Drop) Database `movieapp` trong MySQL rồi chạy lại Backend.
  + Nếu kết nối mạng / VPN bình thường, phim thật từ TMDB sẽ được nạp. Nếu lỗi, `MockDataSeeder` sẽ nạp 4 phim giả để dự phòng.

---

## Hướng dẫn cài đặt Frontend (Android)

1. Mở thư mục `frontend/` bằng **Android Studio**.
2. Đợi Gradle đồng bộ (Sync) xong các thư viện.
3. Đảm bảo cấu hình IP kết nối tới Backend chính xác trong file cấu hình mạng của app (Ví dụ: `ApiClient` hoặc file XML Network Security) nếu bạn định chạy trên điện thoại thật. Nếu chạy máy ảo, có thể dùng `10.0.2.2`.
4. Chọn máy ảo (Emulator) hoặc cắm thiết bị thật vào và bấm **Run (Shift + F10)**.

## Lưu ý chung
- Không đưa các file `build/`, file cấu hình cục bộ của IDE (`.idea/`, `.gradle/`) lên Git. Đã có `.gitignore` xử lý việc này.
- Khi làm việc với thanh điều hướng hay giao diện tràn viền (Edge-to-Edge), chú ý các thiết lập `WindowInsets` để tránh bị cắt xén UI.
