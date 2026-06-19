package com.example.movie_app_server.core.firebase;

import com.example.movie_app_server.user.entity.User;
import com.example.movie_app_server.user.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class FirebaseTokenFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Lấy chuỗi Token từ Header "Authorization" do App Android gửi lên
        String authHeader = request.getHeader("Authorization");

        // Kiểm tra xem Header có chứa Bearer Token không
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Cắt bỏ chữ "Bearer " để lấy Token gốc

            try {
                // Xác thực Token với Firebase
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                String uid = decodedToken.getUid(); // Rút trích ID người dùng (UID)

                // Kiểm tra xem user có bị khóa trong database không
                Optional<User> userOpt = userRepository.findByFirebaseUid(uid);
                if (userOpt.isPresent() && !userOpt.get().isActive()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Tai khoan cua ban da bi khoa");
                    return;
                }

                // Cấp quyền truy cập hệ thống cho UID này
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(uid, null, Collections.emptyList());

                // Lưu vào Context để các Controller (ví dụ UserController) có thể gọi hàm getPrincipal() để lấy UID ra dùng
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (FirebaseAuthException e) {
                // Token hết hạn hoặc không hợp lệ -> Báo lỗi 401 Unauthorized
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Xac thuc that bai: Token khong hop le hoac da het han");
                return;
            }
        }

        // Cho phép Request đi tiếp
        filterChain.doFilter(request, response);
    }
}