package com.tss.quikpawn.models;

import com.centerm.smartpos.aidl.printer.PrinterParams;

public class TssPrinterParams extends PrinterParams {

    public TssPrinterParams() {
        setBold(true);
    }

    @Override
    public void setText(String text) throws Exception {
        super.setText(text.replace((char) ' ', (char) 0x7F));//replace(" ", " "));//
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
