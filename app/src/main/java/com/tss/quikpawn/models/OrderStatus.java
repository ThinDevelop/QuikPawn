package com.tss.quikpawn.models;

import androidx.annotation.ColorRes;

import com.tss.quikpawn.R;

import java.util.ArrayList;
import java.util.List;

public enum OrderStatus {
    UNKNOW("0", "ไม่ระบุ", R.color.colorWhite),
    BUY("1", "ซื้อ", R.color.Gold),
    SELL("2", "ขาย", R.color.Green),
    PAWN("3", "จำนำ", R.color.Blue),
    CONSIGNMENT("4", "ขายฝาก", R.color.OrangeRed),
    REDEEM("5", "ไถ่ถอน", R.color.Silver),
    PAWN_OUT("6", "หลุดจำนำ", R.color.Blue),
    BORRROWED("7", "ยืม", R.color.DarkOrange),
    RETURN("8", "คืนแล้ว", R.color.PaleVioletRed);

    private final String id;
    private final String name;
    private final int color;

    OrderStatus(String id, String name, @ColorRes int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public String getStatusId() {
        return this.id;
    }
    public String getStatusName() {
        return this.name;
    }

    public static OrderStatus getOrderStatus(String id) {
        for (OrderStatus order : list) {
            if (order.id.equals(id)) {
                return order;
            }
        }
       return OrderStatus.UNKNOW;
    }


    @ColorRes
    public int getColor() {
        return this.color;
    }

    private static List<OrderStatus> list = new ArrayList<OrderStatus>() {
        {
            add(UNKNOW);
            add(BUY);
            add(SELL);
            add(PAWN);
            add(CONSIGNMENT);
            add(REDEEM);
            add(PAWN_OUT);
            add(BORRROWED);
            add(RETURN);
        }
    };
}
