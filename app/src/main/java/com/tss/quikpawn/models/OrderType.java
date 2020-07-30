package com.tss.quikpawn.models;

import java.util.ArrayList;
import java.util.List;

public enum OrderType {
    UNKNOWN("0", "ไม่ระบุ"),
    BUY("1", "ซื้อ"),
    SELL("2", "ขาย"),
    PAWN("3", "จำนำ"),
    CONSIGNMENT("4", "ขายฝาก"),
    INTEREST("5", "ต่อดอก"),
    REDEEM("6", "ไถ่ถอน"),
    BORRROWED("7", "ยืม"),
    RETURN("8", "คืน");

    private final String id;
    private final String typeName;

    OrderType(String id, String name) {
        this.id = id;
        this.typeName = name;
    }

    public String getTypeName() {
        return this.typeName;
    }

    public String getTypeId() {
        return this.id;
    }

    public static OrderType getOrderType(String id) {
        OrderType type = OrderType.UNKNOWN;
        for (OrderType orderType: orderTypes) {
            if (orderType.id.equals(id)) {
                type = orderType;
                break;
            }
        }
        return type;
    }

    private static List<OrderType> orderTypes = new ArrayList<OrderType>(){{
                add(OrderType.BUY);
                add(OrderType.SELL);
                add(OrderType.BORRROWED);
                add(OrderType.CONSIGNMENT);
                add(OrderType.REDEEM);
                add(OrderType.RETURN);
                add(OrderType.INTEREST);
    }};

}
