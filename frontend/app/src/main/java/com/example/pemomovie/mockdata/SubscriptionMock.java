package com.example.pemomovie.mockdata;

import java.util.List;

public class SubscriptionMock {
    private String name;
    private String price;
    private String colorHex;
    private List<String> benefits;
    private boolean isPopular;

    public SubscriptionMock(String name, String price, String colorHex, List<String> benefits, boolean isPopular) {
        this.name = name;
        this.price = price;
        this.colorHex = colorHex;
        this.benefits = benefits;
        this.isPopular = isPopular;
    }

    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getColorHex() { return colorHex; }
    public List<String> getBenefits() { return benefits; }
    public boolean isPopular() { return isPopular; }
}
