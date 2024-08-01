package com.example;

public class CodeRangeInfo {
    private String itemValue;
    private int rangeStart;
    private int rangeEnd;

    public CodeRangeInfo(String itemValue, int rangeStart, int rangeEnd) {
        this.itemValue = itemValue;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
    }

    public String getItemValue() {
        return itemValue;
    }

    public int getRangeStart() {
        return rangeStart;
    }

    public int getRangeEnd() {
        return rangeEnd;
    }

    public boolean isInRange(int palletNumber) {
        return palletNumber >= rangeStart && palletNumber <= rangeEnd;
    }
}
