package com.tss.quikpawn

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.centerm.centermposoversealib.thailand.ThaiIDSecurityBeen
import com.centerm.centermposoversealib.util.Utility
import com.centerm.smartpos.aidl.printer.PrinterParams
import com.google.gson.Gson
import com.tss.quikpawn.models.ConsignmentParamModel
import com.tss.quikpawn.models.ConsignmentProductModel
import com.tss.quikpawn.models.DialogParamModel
import com.tss.quikpawn.models.OrderModel
import com.tss.quikpawn.networks.Network
import com.tss.quikpawn.util.DialogUtil
import com.tss.quikpawn.util.Util
import kotlinx.android.synthetic.main.activity_consignment.*
import kotlinx.android.synthetic.main.activity_consignment.edt_name
import kotlinx.android.synthetic.main.activity_consignment.layout_detail
import kotlinx.android.synthetic.main.item_detail_consignment_view.*
import kotlinx.android.synthetic.main.item_sign_view.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class ConsignmentActivity : BaseK9Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consignment)
        title = getString(R.string.consignment_item)
        img_view1.setOnClickListener {
            cameraOpen(it as ImageView, loading_photo_consignment, layout_detail.childCount)
        }

        new_item.setOnClickListener{
            val inflater = LayoutInflater.from(baseContext)
            val contentView: View = inflater.inflate(R.layout.item_detail_consignment_view, null, false)
            val delete = contentView.findViewById<ImageView>(R.id.delete_detail_btn)
            val camera = contentView.findViewById<ImageView>(R.id.img_view1)
            val loadingProgressBarConsignment = contentView.findViewById<ProgressBar>(R.id.loading_photo_consignment)

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
            signature_pad.clear()
        }

        btn_ok.setOnClickListener {
            var signatureBitmap = signature_pad.getSignatureBitmap()
            signatureBitmap = Bitmap.createScaledBitmap(signatureBitmap, 130, 130, false)
            val customerName = edt_name.text.toString()
            var customerId = citizenId
            val interest = edt_interest_rate.text.toString()
            val expire = edt_time.text.toString()
            val signature = Util.bitmapToBase64(signatureBitmap)

            if (customerId.isEmpty()) {
                customerId = edt_idcard.text.toString()
            }
            if (!customerName.isEmpty() &&
                !customerId.isEmpty() &&
                !signature.isEmpty() &&
                !interest.isEmpty() && !interest.equals("0") &&
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

                    val product = ConsignmentProductModel(
                        productName.text.toString(),
                        "5",
                        detail.text.toString(),
                        "0",
                        cost.text.toString(),
                        refImg
                    )
                    productList.add(product)
                }


                var sum = 0
                val list = mutableListOf("รหัสลูกค้า : " + customerId + "\nรายการ\n")
                for (product in productList) {
                    list.add(product.name + " : " + product.cost)
                    sum += Integer.parseInt(product.cost)
                }
                list.add("รวม " + sum + " บาท")

                val param = DialogParamModel(getString(R.string.msg_confirm_title_order), list, "ยืนยัน")
                DialogUtil.showConfirmDialog(param, this, DialogUtil.InputTextBackListerner {
                    val dialog = createProgressDialog(this, "Loading...")
                    dialog.show()
                    val model = ConsignmentParamModel(
                        customerId,
                        customerName,
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

        initialK9()
    }

    override fun setupView(info: ThaiIDSecurityBeen) {
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

    fun printSlip(data: OrderModel) {
        val textList = ArrayList<PrinterParams>()

        var bitmap = createImageBarcode(data.order_code, "Barcode")!!
        bitmap = Utility.toGrayscale(bitmap)

        var printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(24)
        printerParams1.setText("รายการ "+data.type_name)
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
