package com.tss.quikpawn.models;

import com.centerm.smartpos.aidl.printer.PrinterParams;

public class TssPrinterParams extends PrinterParams {

    public TssPrinterParams() {
        setBold(true);
    }
    @Override
    public boolean isBold() {
        return true;
    }

    @Override
    public void setTextSize(int textSize) {
        super.setTextSize(25);
    }
}
