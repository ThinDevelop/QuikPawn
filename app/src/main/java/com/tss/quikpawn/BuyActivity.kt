package com.tss.quikpawn

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.centerm.centermposoversealib.util.Utility
import com.centerm.smartpos.aidl.printer.PrinterParams
import kotlinx.android.synthetic.main.activity_buy.*
import kotlinx.android.synthetic.main.activity_buy.signature_pad
import kotlinx.android.synthetic.main.activity_sell.*


class BuyActivity : BaseK9Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy)
        val code = intent.getStringExtra("barcode")
        //clear sign button
        clearsign_btn.setOnClickListener{
            signature_pad.clear()
        }
        //get bitmap signature
        ok_btn.setOnClickListener {
            print()
        }
        barcode_btn!!.setOnClickListener {
            val intent = Intent(this@BuyActivity, ScanActivity::class.java)
            startActivity(intent)
            this@BuyActivity.finish()
        }

        if(code != null){
            order_number!!.setText(code)
            layout_detail.visibility = View.VISIBLE
            edt_cost.setText("1,300,000")
            edt_detail.setText("ทองคำแท่ง น้ำหนัก 1 กิโลกรัม")
            img_view1.setImageResource(R.drawable.gold_a)

        }
    }

    fun print() {
        val textList = ArrayList<PrinterParams>()
        val bitmap = (img_view1.getDrawable() as BitmapDrawable).bitmap

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
        printerParams1.setText("\n\nใบซื้อ")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("เลขที่ 3983")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
        printerParams1.setTextSize(22)
        printerParams1.setText("วันที่ 1 มกราคม พ.ศ. 2563")
        textList.add(printerParams1)

//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
//        printerParams1.setTextSize(22)
//        printerParams1.setText("ข้าพเจ้า "+edt_name.text.toString()+" \nบัตรประชาชน "+ edt_idcard.text.toString()+"\n\n")
//        textList.add(printerParams1)

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
        printerParams1.setText("\n\nรายละเอียด \n "+edt_detail.text.toString())
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\nซื้อด้วยราคา \n "+edt_price.text.toString())
        textList.add(printerParams1)

//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
//        printerParams1.setTextSize(22)
//        printerParams1.setText("\n\nมาขายฝากให้เป็นจำนวนเงิน "+edt_sell_price.text.toString()+" บาท\nและได้รับเงินไปเสร็จเรียบร้อยแล้ว จึงลงลายมือชื่อไว้เป็นหลักฐาน")
//        textList.add(printerParams1)

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
        printerParams1.setText("ผู้ขาย")
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
