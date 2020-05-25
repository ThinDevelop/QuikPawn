package com.tss.quikpawn.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewManager
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.centerm.centermposoversealib.thailand.ThaiIDSecurityBeen
import com.centerm.centermposoversealib.thailand.ThiaIdInfoBeen
import com.centerm.centermposoversealib.util.Utility
import com.centerm.smartpos.aidl.printer.PrinterParams
import com.google.gson.Gson
import com.tss.quikpawn.BaseK9Activity
import com.tss.quikpawn.PreferencesManager
import com.tss.quikpawn.R
import com.tss.quikpawn.ScanActivity
import com.tss.quikpawn.models.*
import com.tss.quikpawn.networks.Network
import com.tss.quikpawn.util.DialogUtil
import com.tss.quikpawn.util.NumberTextWatcherForThousand
import com.tss.quikpawn.util.Util
import kotlinx.android.synthetic.main.activity_borrow.*
import kotlinx.android.synthetic.main.item_customer_info.*
import kotlinx.android.synthetic.main.item_search.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BorrowActivity: BaseK9Activity() {
    var productList = mutableListOf<SellProductModel>()
    var picker: DatePickerDialog? = null
    var resultDeadline: String = ""
    val resultCldr: Calendar = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_borrow)

        title = getString(R.string.borrow_item)
        scan.setOnClickListener {
            val intent = Intent(this@BorrowActivity, ScanActivity::class.java)
            startActivityForResult(intent, SCAN_REQUEST_CODE)
        }
        btn_clearsign.setOnClickListener{
            signature_pad.clear()
        }

        deadline.setOnClickListener{
            val cldr: Calendar = Calendar.getInstance()
            val day: Int = cldr.get(Calendar.DAY_OF_MONTH)
            val month: Int = cldr.get(Calendar.MONTH)
            val year: Int = cldr.get(Calendar.YEAR)
            // date picker dialog
            // date picker dialog
            picker = DatePickerDialog(
                this@BorrowActivity,
                object : DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(
                        view: DatePicker?,
                        year: Int,
                        monthOfYear: Int,
                        dayOfMonth: Int
                    ) {
                        (it as TextView).setText(dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year)
                        resultDeadline = ""+year+"-"+(monthOfYear + 1)+"-"+dayOfMonth.toString()
                        resultCldr.set(year, monthOfYear, dayOfMonth)
                    }
                }, year, month, day
            )
            picker!!.getDatePicker().minDate = cldr.timeInMillis
            picker!!.show()
        }

        btn_ok.setOnClickListener {
            var signatureBitmap = signature_pad.getSignatureBitmap()
            signatureBitmap = Bitmap.createScaledBitmap(signatureBitmap, 130, 130, false)
            val customerName = edt_name.text.toString()
            var customerId = citizenId
            var customerAddress = address
            var customerPhoto = customerPhoto
            var customerPhone = edt_phonenumber.text.toString()
            val txtDeadline = deadline.text.toString()
            val signature = Util.bitmapToBase64(signatureBitmap)
            if (customerId.isEmpty()) {
                customerId = edt_idcard.text.toString()
            }
            val format = SimpleDateFormat("YYYY-MM-dd")
            val deadline = format.format(resultCldr.time)
            Log.e("panya", "deadline : $deadline")

            if (!customerName.isEmpty() &&
                !customerId.isEmpty() &&
                !signature.isEmpty() &&
                !customerPhone.isEmpty()){

                var sum = 0
                val list = mutableListOf("รหัสลูกค้า : " + customerId + "\nรายการ\n")
                for (product in productList) {
                    list.add(product.product_code + " : " + NumberTextWatcherForThousand.getDecimalFormattedString(product.sale_price.toString()))
                    sum ++
                }
                list.add("รวม " + NumberTextWatcherForThousand.getDecimalFormattedString(sum.toString()) + " ชิ้น")

                val param = DialogParamModel(getString(R.string.msg_confirm_title_order), list, "ยืนยัน")
                DialogUtil.showConfirmDialog(param, this, DialogUtil.InputTextBackListerner {
                    val dialog = createProgressDialog(this, "Loading...")
                    dialog.show()
                val model = LendParamModel(customerId, customerName, customerAddress, customerPhoto, customerPhone, productList, deadline, signature)
                Network.lend(model, object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        dialog.dismiss()
                        Log.e("panya", "onResponse : $response")
                        val status = response.getString("status_code")
                        if (status == "200") {
                            val data = response.getJSONObject("data")
                            printSlip(Gson().fromJson(data.toString(), OrderModel::class.java))
                            showConfirmDialog(data)
                        }
                    }

                    override fun onError(error: ANError) {
                        dialog.dismiss()
                        error.printStackTrace()
                        Log.e("panya", "onError : " + error.errorCode +", detail "+error.errorDetail+", errorBody"+ error.errorBody)
                    }
                } )
                })
            } else {
                Toast.makeText(this@BorrowActivity, "ข้อมูลไม่ครบถ้วน", Toast.LENGTH_LONG).show()
            }
        }
        edt_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (!query.equals("")) {
                        loadItem(query)
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        initialK9()
    }

    fun showConfirmDialog(data: JSONObject) {
        val list = listOf("สำหรับร้านค้า")
        val dialogParamModel = DialogParamModel("ปริ้น", list, "ตกลง")
        DialogUtil.showConfirmDialog(dialogParamModel, this, DialogUtil.InputTextBackListerner {
            printSlip(Gson().fromJson(data.toString(), OrderModel::class.java))
            finish()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SCAN_REQUEST_CODE) {
                val barcode = data?.getStringExtra("barcode")
                Log.e("panya", "barcode : "+barcode)
                if (checkContains(barcode!!)) {
                    Toast.makeText(this@BorrowActivity, "สินค้านี้ถูกเพิ่มในรายการแล้ว", Toast.LENGTH_LONG).show()
                } else {
                    loadItem(barcode!!)
                }
            }
        }
    }

    fun checkContains(barcode: String): Boolean {
        for (product in productList) {
            if (product.product_code.equals(barcode)) {
                return true
            }
        }
        return false
    }

    fun loadItem(key: String) {
        Network.searchProductByCode(key, object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                Log.e("panya", "onResponse : $response")
                val status = response.getString("status_code")
                if (status == "200") {
                    val product = SellProductModel(key, 0)
                    productList.add(product)
                    val dataJsonObj = response.getJSONObject("data")
                    val data = Gson().fromJson(dataJsonObj.toString(), ProductModel::class.java)
                    addItemView(data)
                }
            }

            override fun onError(error: ANError) {
                error.printStackTrace()
                Log.e("panya", "onError : " + error.errorCode +", detail "+error.errorDetail+", errorBody"+ error.errorBody)
            }
        } )
    }

    fun addItemView(productModel: ProductModel) {
        val inflater = LayoutInflater.from(baseContext)
        val contentView: View = inflater.inflate(R.layout.item_cardview_sell, null, false)
        val delete = contentView.findViewById<ImageView>(R.id.btn_delete)
        val image = contentView.findViewById<ImageView>(R.id.img_item)
        val txtId = contentView.findViewById<TextView>(R.id.txt_item_id)
        val txtDetail = contentView.findViewById<TextView>(R.id.txt_detail)
        val txtCost = contentView.findViewById<TextView>(R.id.txt_cost)
        val txtSell = contentView.findViewById<TextView>(R.id.txt_sell)

        txtSell.visibility = View.VISIBLE
        txtId.text = productModel.product_id
        txtDetail.text = productModel.detail
        txtCost.text = productModel.cost

        delete.visibility = View.VISIBLE
        contentView.tag = productModel.product_code
        delete.tag = contentView.tag
        delete.setOnClickListener {
            val list = mutableListOf<SellProductModel>()
            for (product in productList) {
                if (product.product_code != it.tag) {
                    list.add(product)
                }
            }
            productList = list
            (item_container.findViewWithTag<View>(it.tag).parent as ViewManager).removeView(item_container.findViewWithTag<View>(it.tag))
        }
        Glide.with(this) //1
            .load(productModel.image_small)
            .placeholder(R.drawable.ic_image_black_24dp)
            .error(R.drawable.ic_broken_image_black_24dp)
            .skipMemoryCache(true) //2
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE) //3
            .into(image)

        contentView.setOnClickListener {
            DialogUtil.showInputDialog(this@BorrowActivity,  object : DialogUtil.InputTextBackListerner {
                override fun onClickConfirm(result: String?) {
                    result?.let{
                        if (it.isEmpty()) return
                        val price = Integer.parseInt(result)
                        val productCode = contentView.tag as String
                        val sellProductModel = SellProductModel(productCode, price)
                        productList.add(sellProductModel)
                        txtSell.text = NumberTextWatcherForThousand.getDecimalFormattedString(result)
                    }

                }
            })
        }
        item_container.addView(contentView)
    }

    override fun setupView(info: ThiaIdInfoBeen) {
        super.setupView(info)
        edt_name.setText(info.thaiFirstName +" "+info.thaiLastName)
        edt_idcard.setText(info.citizenId?.substring(0, info.citizenId.length-3) + "XXX")
    }

    fun printSlip(data: OrderModel) {
        val textList = ArrayList<PrinterParams>()

        var bitmap = createImageBarcode(data.order_code, "Barcode")!!
        bitmap = Utility.toGrayscale(bitmap)

        var printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(30)
        printerParams1.setText(data.type_name)
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
        printerParams1.setText("ร้าน "+ PreferencesManager.getInstance().companyName)
        textList.add(printerParams1)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("สาขา "+ PreferencesManager.getInstance().companyBranchName)
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
        printerParams1.setText("รายการสินค้า\n")
        textList.add(printerParams1)

        for (product in data.products) {
            printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
            printerParams1.setTextSize(24)
            printerParams1.setText("*"+product.product_code + " "+product.sale+" บาท")
            textList.add(printerParams1)
        }
//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
//        printerParams1.setTextSize(22)
//        printerParams1.setText("มูลค่า "+data.price+" บาท")
//        textList.add(printerParams1)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(24)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        printdata(textList)
    }
}