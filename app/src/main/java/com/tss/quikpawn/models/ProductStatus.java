package com.tss.quikpawn.models;

import androidx.annotation.ColorRes;

import com.tss.quikpawn.R;

import java.util.ArrayList;
import java.util.List;

public enum ProductStatus {
    UNKNOW("0", "ไม่ระบุ", R.color.colorWhite),
    CONSIGNMENT("1", "ฝากขาย", R.color.Gold),
    REDEEM("2", "ไถ่ถอน", R.color.Gray),
    READY_FOR_SALE("3", "พร้อมขาย", R.color.Green),
    SOLD("4", "ขายแล้ว", R.color.Gray),
    BORRROWED("5", "ถูกยืม", R.color.DarkOrange),
    PAWN("6", "จำนำ", R.color.colorWhite);

    private final String id;
    private final String name;
    private final int color;

    ProductStatus(String id, String name, @ColorRes int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public String getStatusId() {
        return this.id;
    }

    public static ProductStatus getProductStatus(String id) {
        for (ProductStatus order : list) {
            if (order.id.equals(id)) {
                return order;
            }
        }
        return ProductStatus.UNKNOW;
    }

    private static List<ProductStatus> list = new ArrayList<ProductStatus>() {
        {
            add(UNKNOW);
            add(CONSIGNMENT);
            add(SOLD);
            add(PAWN);
            add(REDEEM);
            add(READY_FOR_SALE);
            add(BORRROWED);
        }
    };

    @ColorRes
    public int getColor() {
        return this.color;
    }

}

