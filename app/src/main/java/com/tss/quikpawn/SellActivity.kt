package com.tss.quikpawn

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.centerm.centermposoversealib.thailand.ThiaIdInfoBeen
import com.centerm.centermposoversealib.util.Utility
import com.centerm.smartpos.aidl.printer.PrinterParams
import kotlinx.android.synthetic.main.activity_sell.*
import kotlinx.android.synthetic.main.view_image.*
import java.util.jar.Manifest
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_buy.*
import kotlinx.android.synthetic.main.activity_sell.signature_pad


class SellActivity : BaseK9Activity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell)

        capture_btn .setOnClickListener {
            cameraOpen(it as ImageView)
        }

        btn_clearsign.setOnClickListener{
            signature_pad.clear()
        }

        btn_ok.setOnClickListener {
            print()
        }
        initialK9()
    }


    override fun setupView(info: ThiaIdInfoBeen) {
        edt_name.setText(info.thaiFirstName + " " + info.thaiLastName)
        edt_idcard.setText(info.citizenId?.substring(0, info.citizenId.length-3) + "XXX")
    }


    fun print() {
        val textList = ArrayList<PrinterParams>()
        val bitmap = (img_view.getDrawable() as BitmapDrawable).bitmap


//        var imageicon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.gold_b)
        var imageicon = Bitmap.createScaledBitmap(bitmap, 130, 130, false)
        imageicon = Utility.toGrayscale(imageicon)

        var imagelogo: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_quikpawn)
        imagelogo = Bitmap.createScaledBitmap(imagelogo, 130, 130, false)
        imagelogo = Utility.toGrayscale(imagelogo)

        var signatureBitmap = signature_pad.getSignatureBitmap()
        signatureBitmap = Bitmap.createScaledBitmap(signatureBitmap, 130, 130, false)
        signatureBitmap = Utility.toGrayscale(signatureBitmap)


        var printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(imagelogo)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\nใบขาย")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("เลขที่ 3982")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
        printerParams1.setTextSize(22)
        printerParams1.setText("วันที่ 1 มกราคม พ.ศ. 2563")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("ข้าพเจ้า "+edt_name.text.toString()+" \nบัตรประชาชน "+ edt_idcard.text.toString()+"\n\n")
        textList.add(printerParams1)

//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
//        printerParams1.setTextSize(22)
//        printerParams1.setText("ได้ทำหนังสือขายฝากนี้ให้แก่ นายสุรศักดิ์ ขจิตธรรมกุล ดังมีข้อความดังต่อไปนี้\n" + "   ข้อ 1. ผู้ขายฝากได้นำทรัพย์สินปรากฎตามรายการดังนี้\n\n")
//        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(imageicon)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\nรายละเอียด \n "+edt_sell_detail.text.toString())
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\nมาขายฝากให้เป็นจำนวนเงิน "+edt_sell_price.text.toString()+" บาท\nและได้รับเงินไปเสร็จเรียบร้อยแล้ว จึงลงลายมือชื่อไว้เป็นหลักฐาน")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n\n\n___________________")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("ผู้ขายฝาก")
        textList.add(printerParams1)

//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
//        printerParams1.setTextSize(22)
//        printerParams1.setText("\n\n\n\n___________________")
//        textList.add(printerParams1)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(signatureBitmap)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("ผู้รับซื้อฝาก")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n\n\n___________________")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("พยาน")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n\n\n___________________")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("พยาน/ผู้พิมพ์")
        textList.add(printerParams1)


        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n\n\n\n")
        textList.add(printerParams1)
        printdata(textList)
    }
}
