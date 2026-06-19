package com.example.pemomovie.mockdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockDataHelper {

    public static List<CategoryMock> getMockCategories() {
        List<CategoryMock> list = new ArrayList<>();
        list.add(new CategoryMock("1", "Hành Động", "#EF4444", 0)); // Red
        list.add(new CategoryMock("2", "Hài Hước", "#F59E0B", 0)); // Amber
        list.add(new CategoryMock("3", "Tình Cảm", "#EC4899", 0)); // Pink
        list.add(new CategoryMock("4", "Kinh Dị", "#1F2937", 0)); // Dark Gray
        list.add(new CategoryMock("5", "Viễn Tưởng", "#3B82F6", 0)); // Blue
        list.add(new CategoryMock("6", "Hoạt Hình", "#10B981", 0)); // Green
        list.add(new CategoryMock("7", "Tài Liệu", "#8B5CF6", 0)); // Purple
        list.add(new CategoryMock("8", "Phiêu Lưu", "#06B6D4", 0)); // Cyan
        return list;
    }

    public static List<SubscriptionMock> getMockSubscriptions() {
        List<SubscriptionMock> list = new ArrayList<>();
        list.add(new SubscriptionMock("Gói Cơ Bản", "49.000đ/tháng", "#9CA3AF", 
            Arrays.asList("Xem phim chất lượng HD", "Hỗ trợ 1 thiết bị"), false));
        list.add(new SubscriptionMock("Gói Premium", "99.000đ/tháng", "#F59E0B", 
            Arrays.asList("Xem phim 4K HDR", "Không quảng cáo", "Hỗ trợ 4 thiết bị cùng lúc", "Tải phim offline"), true));
        list.add(new SubscriptionMock("Gói VIP", "199.000đ/tháng", "#8B5CF6", 
            Arrays.asList("Mọi đặc quyền Premium", "Hỗ trợ thiết bị không giới hạn", "Truy cập phòng chiếu VIP"), false));
        return list;
    }

    public static List<UserMock> getMockUsers() {
        List<UserMock> list = new ArrayList<>();
        list.add(new UserMock("u1", "Nguyễn Văn A", "nguyenvana@gmail.com", true, ""));
        list.add(new UserMock("u2", "Trần Thị B", "tranthib@gmail.com", false, ""));
        list.add(new UserMock("u3", "Lê Khang", "khang.le@uit.edu.vn", true, ""));
        list.add(new UserMock("u4", "Hoàng C", "hoangc99@yahoo.com", false, ""));
        list.add(new UserMock("u5", "Phạm D", "phamd88@gmail.com", true, ""));
        list.add(new UserMock("u6", "Đặng E", "dang.e@outlook.com", false, ""));
        return list;
    }
}
