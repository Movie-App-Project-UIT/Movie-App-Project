package com.example.pemomovie.dto;

public class FilterOption {
    private Long id;
    private String name;
    // For releaseYear which might be integer instead of long, or we can just use Long.valueOf(year) or id=null for "Tất cả"
    private Integer intValue;

    public FilterOption(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    public FilterOption(Integer intValue, String name) {
        this.intValue = intValue;
        this.name = name;
    }

    public Long getId() { return id; }
    public Integer getIntValue() { return intValue; }
    public String getName() { return name; }
}
