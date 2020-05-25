package com.tss.quikpawn

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.get
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.centerm.centermposoversealib.thailand.ThiaIdInfoBeen
import com.centerm.centermposoversealib.util.Utility
import com.centerm.smartpos.aidl.printer.PrinterParams
import com.google.gson.Gson
import com.tss.quikpawn.models.ConsignmentParamModel
import com.tss.quikpawn.models.ConsignmentProductModel
import com.tss.quikpawn.models.DialogParamModel
import com.tss.quikpawn.models.OrderModel
import com.tss.quikpawn.networks.Network
import com.tss.quikpawn.util.DialogUtil
import com.tss.quikpawn.util.NumberTextWatcherForThousand
import com.tss.quikpawn.util.Util
import kotlinx.android.synthetic.main.activity_consignment.*
import kotlinx.android.synthetic.main.item_sign_view.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class ConsignmentActivity : BaseK9Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consignment)
        title = getString(R.string.consignment_item)
        new_item.setOnClickListener{
            val inflater = LayoutInflater.from(baseContext)
            val contentView: View = inflater.inflate(R.layout.item_detail_consignment_view, null, false)
            val delete = contentView.findViewById<ImageView>(R.id.delete_detail_btn)
            val camera = contentView.findViewById<ImageView>(R.id.img_view1)
            val loadingProgressBarConsignment = contentView.findViewById<ProgressBar>(R.id.loading_photo_consignment)
            val edtCost = contentView.findViewById<EditText>(R.id.edt_cost)

            edtCost.addTextChangedListener(NumberTextWatcherForThousand(edtCost))
            delete.visibility = View.VISIBLE
            contentView.tag = layout_detail.childCount
            delete.tag = contentView.tag
            delete.setOnClickListener {
                (layout_detail.findViewWithTag<View>(it.tag).parent as ViewManager).removeView(layout_detail.findViewWithTag<View>(it.tag))
            }
            layout_detail.addView(contentView)
            camera.setOnClickListener {
                cameraOpen(it as ImageView, loadingProgressBarConsignment, layout_detail.childCount)
            }
        }

        clearsign_btn.setOnClickListener{
            sliptest()
//            signature_pad.clear()
        }

        btn_ok.setOnClickListener {
            var signatureBitmap = signature_pad.getSignatureBitmap()
            signatureBitmap = Bitmap.createScaledBitmap(signatureBitmap, 130, 130, false)
            val customerName = edt_name.text.toString()
            var customerId = citizenId
            val interest = edt_interest_rate.text.toString()
            val phoneNumber = edt_phonenumber.text.toString()
            val expire = edt_time.text.toString()
            val signature = Util.bitmapToBase64(signatureBitmap)

            if (customerId.isEmpty()) {
                customerId = edt_idcard.text.toString()
            }
            if (!customerName.isEmpty() &&
                !customerId.isEmpty() &&
                !signature.isEmpty() &&
                !interest.isEmpty() && !interest.equals("0") &&
                !phoneNumber.isEmpty() &&
                !expire.isEmpty() &&
                (loadingProgressBar != null && !loadingProgressBar!!.isShown)   ) {
                val productList = ArrayList<ConsignmentProductModel>()
                for (i in 0..layout_detail.childCount - 1) {
                    val contentView = layout_detail.get(i)
                    val camera = contentView.findViewById<ImageView>(R.id.img_view1)
                    val detail = contentView.findViewById<EditText>(R.id.edt_detail)
                    val cost = contentView.findViewById<EditText>(R.id.edt_cost)
                    val productName = contentView.findViewById<EditText>(R.id.edt_product_name)

                    val refImg = camera.tag as String
                    var costA = cost.text.toString()
                    if (costA.isEmpty()) {
                        costA = "0"
                    }
                        val product = ConsignmentProductModel(
                            productName.text.toString(),
                            "5",
                            detail.text.toString(),
                            "0",
                            NumberTextWatcherForThousand.trimCommaOfString(costA),
                            refImg
                        )
                        productList.add(product)

                }


                var sum = 0
                val list = mutableListOf("รหัสลูกค้า : " + customerId + "\nรายการ\n")
                for (product in productList) {
                    list.add(product.name + " : " + NumberTextWatcherForThousand.getDecimalFormattedString(product.cost))
                    sum += Integer.parseInt(product.cost)
                }
                list.add("รวม " + NumberTextWatcherForThousand.getDecimalFormattedString(sum.toString()) + " บาท")

                val param = DialogParamModel(getString(R.string.msg_confirm_title_order), list, "ยืนยัน")
                DialogUtil.showConfirmDialog(param, this, DialogUtil.InputTextBackListerner {
                    val dialog = createProgressDialog(this, "Loading...")
                    dialog.show()
                    val model = ConsignmentParamModel(
                        customerId,
                        customerName,
                        address,
                        customerPhoto,
                        phoneNumber,
                        productList,
                        PreferencesManager.getInstance().companyId,
                        PreferencesManager.getInstance().companyBranchId,
                        interest,
                        expire,
                        signature,
                        PreferencesManager.getInstance().userId
                    )
                    Network.orderConsignment(model, object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject) {
                            dialog.dismiss()
                            Log.e("panya", "onResponse : $response")
                            val status = response.getString("status_code")
                            if (status == "200") {
                                val data = response.getJSONObject("data")
                                printSlip(Gson().fromJson(data.toString(), OrderModel::class.java))
//                                printSlip(Gson().fromJson(data.toString(), OrderModel::class.java))
                                finish()
                            }
                        }

                        override fun onError(error: ANError) {
                            dialog.dismiss()
                            error.printStackTrace()
                            Log.e(
                                "panya",
                                "onError : " + error.errorCode + ", detail " + error.errorDetail + ", errorBody" + error.errorBody
                            )
                        }
                    })
                })

            } else {

                Toast.makeText(this@ConsignmentActivity, "ข้อมูลไม่ครบถ้วน", Toast.LENGTH_LONG).show()
            }
        }
        new_item.callOnClick()
        initialK9()
    }

    override fun setupView(info: ThiaIdInfoBeen) {
        super.setupView(info)
        edt_name.setText(info.thaiFirstName + " " + info.thaiLastName)
        edt_idcard.setText(info.citizenId?.substring(0, info.citizenId.length-3) + "XXX")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        if (IMAGE_CAPTURE_CODE == requestCode) {
            //show loading
            loadingProgressBar?.visibility = View.VISIBLE
            Network.uploadBase64(getFileToByte(imageFilePath), object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e("panya", "onResponse : $response")
                    loadingProgressBar?.visibility = View.GONE
                    val status = response.getString("status_code")
                    if (status == "200") {
                        val data = response.getJSONObject("data")
                        val refCode = data.getString("ref_code")
                        setTagToImageView(refCode)
                    }
                }

                override fun onError(error: ANError) {
                    loadingProgressBar?.visibility = View.GONE
                    error.printStackTrace()
                    Log.e("panya", "onError : " + error.errorCode +", detail "+error.errorDetail+", errorBody"+ error.errorBody)
                }
            })
        }
    }

    fun getFileToByte(filePath: String?): String {
        var bmp: Bitmap? = null
        var bos: ByteArrayOutputStream? = null
        var bt: ByteArray? = null
        var encodeString: String = ""
        try {
            bmp = BitmapFactory.decodeFile(filePath)
            bos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 30, bos)
            bt = bos.toByteArray()
            encodeString = Base64.encodeToString(bt, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return encodeString
    }

    fun sliptest() {

        var text = " "
        val bounds = Rect()
        val textPaint = Paint()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        val height: Int = bounds.height()
        val width: Int = bounds.width()

        val textList = java.util.ArrayList<PrinterParams>()
        var printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        text = getTextProductList("test", "1000000")
        printerParams1.setText(text)
        textList.add(printerParams1)

         printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        text = getTextProductList("aaaaaaaaaaaaaaaaaaaaa", "1000000")
        printerParams1.setText(text)
        textList.add(printerParams1)
         printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        text = getTextProductList("bbbbbbbbbbbbbbb", "1000000")
        printerParams1.setText(text)
        textList.add(printerParams1)
         printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        text = getTextProductList("ฟฟฟฟฟฟฟฟฟฟฟฟฟฟฟฟฟฟฟฟ", "1000000")
        printerParams1.setText(text)
        textList.add(printerParams1)
         printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        text = getTextProductList("ถั่ว", "1000000")
        printerParams1.setText(text)
        textList.add(printerParams1)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        text = getTextProductList("ที่", "1000000")
        printerParams1.setText(text)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        text = getTextProductList("ท", "1000000")
        printerParams1.setText(text)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        text = getTextProductList("a", "1000000")
        printerParams1.setText(text)
        textList.add(printerParams1)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        text = getTextProductList("                 ", "1000000")
        printerParams1.setText(text)
        textList.add(printerParams1)
        printdata(textList)
    }

    fun printSlip1(data: OrderModel) {
        var imageicon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.gold_b)
        imageicon = Bitmap.createScaledBitmap(imageicon, 130, 130, false)
        imageicon = Utility.toGrayscale(imageicon)

        var qr: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.gold_a)
        qr = Bitmap.createScaledBitmap(qr, 200, 250, false)
        qr = Utility.toGrayscale(qr)

        val textList = java.util.ArrayList<PrinterParams>()
        var printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("ใบฝากขาย")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("เลขที่ "+data.order_code)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
        printerParams1.setTextSize(22)
        printerParams1.setText("วันที่ " + Util.toDateFormat(data.date_create))
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("ข้าพเจ้า"+data.customer_name+" \nบัตรประชาชน "+data.idcard)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("ได้ทำหนังสือขายฝากนี้ให้แก่ นายสุรศักดิ์ ขจิตธรรมกุล ดังมีข้อความดังต่อไปนี้\n" + "   ข้อ 1. ผู้ขายฝากได้นำทรัพย์สินปรากฎตามรายการดังนี้\n\n")
        textList.add(printerParams1)

        var sum = 0
        for (productModel in data.products) {
            sum += Integer.parseInt(productModel.cost)
            printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(22)
            printerParams1.setText(getTextProductList(productModel.product_name, productModel.cost))
            textList.add(printerParams1)
        }

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\nมาขายฝากให้เป็นจำนวนเงิน "+ sum +" บาท\nและได้รับเงินไปเสร็จเรียบร้อยแล้ว จึงลงลายมือชื่อไว้เป็นหลักฐาน")
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

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n\n\n___________________")
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

    fun getTextProductList(name: String, price: String): String {
        var realName = StringBuilder()
        var realPrice = StringBuilder()

        for (i in 0..10) {
            if (i < name.length) {
                realName.append(name[i])
            }
        }
//        var y = true
//
//        while (y) {
//            val boundsName = Rect()
//            val textPaintName = Paint()
//            val textName = "1." + realName.toString()
//            textPaintName.getTextBounds(textName, 0, textName.length, boundsName)
//            var widthName = boundsName.width()
//            if (widthName < 100) {
//                realName.insert(0, ' ')
//            } else {
//                y = false
//            }
//        }
        val size = 25 - realName.length
        for (i in 0..size) {
            if (i < price.length) {
                realPrice.append(price[i])
            } else {
                realPrice.insert(0, ' ')
            }
        }
        var x = true
        while (x) {
            val bounds = Rect()
            val textPaint = Paint()
            val text = "1." + realName.toString() + ' ' + realPrice.toString()
            textPaint.getTextBounds(text, 0, text.length, bounds)
            val width: Int = bounds.width()
            if (width < 180) {
                realPrice.insert(0, ' ')
            } else {
                x = false
            }
        }


        val bounds = Rect()
        val textPaint = Paint()
        val text = "1." + realName.toString() + realPrice.toString()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        val width: Int = bounds.width()
        Log.e("panya", "text : "+ text + ", width : "+width)
        return text
    }

    fun printSlip(data: OrderModel) {
        val textList = ArrayList<PrinterParams>()

        var bitmap = createImageBarcode(data.order_code, "Barcode")!!
        bitmap = Utility.toGrayscale(bitmap)

        var bitmap2 = createImageBarcode(data.order_code, "QR Code")!!
        bitmap2 = Utility.toGrayscale(bitmap2)

        var printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(30)
        printerParams1.setText("\n"+data.type_name)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(24)
        printerParams1.setText("\n")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(bitmap)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(bitmap2)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(24)
        printerParams1.setText(data.order_code)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("ร้าน "+PreferencesManager.getInstance().companyName)
        textList.add(printerParams1)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("สาขา "+PreferencesManager.getInstance().companyBranchName)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("วันที่ " + Util.toDateFormat(data.date_create))
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("ลูกค้า "+data.customer_name)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("รหัสปชช. "+data.idcard)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("ครบกำหนด "+Util.toDateFormat(data.date_expire))
        textList.add(printerParams1)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("ราคา "+data.price)
        textList.add(printerParams1)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("ดอกเบี้ย "+data.interest_price)
        textList.add(printerParams1)


        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("รายการสินค้า\n")
        textList.add(printerParams1)

        for (product in data.products) {
            printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
            printerParams1.setTextSize(24)
            printerParams1.setText("*"+product.product_code + " "+product.cost+" บาท")
            textList.add(printerParams1)
        }
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
        printerParams1.setTextSize(24)
        printerParams1.setText("ชำระเงิน "+data.price+" บาท")
        textList.add(printerParams1)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        printdata(textList)

        textList.clear()
        for (product in data.products) {

            printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(24)
            printerParams1.setText("สินค้า : " + product.product_name + "\n")
            textList.add(printerParams1)

            bitmap = Utility.toGrayscale(createImageBarcode(product.product_code, "Barcode")!!)
            printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
            printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
            printerParams1.setBitmap(bitmap)
            textList.add(printerParams1)

            printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
            printerParams1.setTextSize(24)
            printerParams1.setText(product.product_code + "\n\n")
            textList.add(printerParams1)

        }
        printdata(textList)
    }
}
