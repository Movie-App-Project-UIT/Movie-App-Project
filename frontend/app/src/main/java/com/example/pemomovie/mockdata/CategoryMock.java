package com.example.pemomovie.mockdata;

public class CategoryMock {
    private String id;
    private String name;
    private String colorHex;
    private int iconResId;

    public CategoryMock(String id, String name, String colorHex, int iconResId) {
        this.id = id;
        this.name = name;
        this.colorHex = colorHex;
        this.iconResId = iconResId;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getColorHex() { return colorHex; }
    public int getIconResId() { return iconResId; }
}
