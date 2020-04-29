package com.tss.quikpawn.models;

public enum OrderType {
    UNKNOWN("0", "ไม่ระบุ"),
    BUY("1", "ซื้อ"),
    SELL("2", "ขาย"),
    PAWN("3", "จำนำ"),
    CONSIGNMENT("4", "ฝากขาย"),
    INTEREST("5", "ต่อดอก"),
    REDEEM("6", "ไถ่ถอน"),
    BORRROWED("7", "ยืม"),
    RETURN("8", "คืน");

    private final String id;
    private final String name;

    OrderType(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getTypeId() {
        return this.id;
    }

}
