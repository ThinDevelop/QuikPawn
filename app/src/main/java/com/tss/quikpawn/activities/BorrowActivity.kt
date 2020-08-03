package com.tss.quikpawn.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
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
import com.tss.quikpawn.util.Util.Companion.dashSignature
import kotlinx.android.synthetic.main.activity_borrow.*
import kotlinx.android.synthetic.main.item_customer_info.*
import kotlinx.android.synthetic.main.item_search.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BorrowActivity: BaseK9Activity() {
    var productName = HashMap<String, String>()
    var productList = mutableListOf<SellProductModel>()
    var productModelList = mutableListOf<ProductModel>()
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
            val customerName = edt_name.text.toString()
            var customerId = citizenId
            var customerAddress = address
            var customerPhoto = customerPhoto
            var customerPhone = edt_phonenumber.text.toString()
            val txtDeadline = deadline.text.toString()
            if (customerId.isEmpty()) {
                customerId = edt_idcard.text.toString()
            }
            val format = SimpleDateFormat("yyyy-MM-dd")
            val deadline = format.format(resultCldr.time)
            Log.e("panya", "deadline : $deadline")

            if (!customerName.isEmpty() &&
//                !customerId.isEmpty() &&
                !customerPhone.isEmpty()){

                if (productList.isEmpty()) {
                    DialogUtil.showNotiDialog(
                        this@BorrowActivity,
                        getString(R.string.title_error),
                        getString(R.string.data_missing)
                    )
                }
                if (customerPhone.length != 10) {
                    DialogUtil.showNotiDialog(this, getString(R.string.data_is_wrong), getString(R.string.wrong_phone_number))
                    return@setOnClickListener
                }

                var sum = 0
                val list = mutableListOf("รหัสลูกค้า : " + customerId + "\nรายการ")
                for (product in productList) {
                    if (product.sale_price == 0) {
                        DialogUtil.showNotiDialog(this@BorrowActivity, getString(R.string.data_missing), getString(R.string.please_add_sale_price))
                        return@setOnClickListener
                    }
                    sum ++
                    list.add(sum.toString() +". "+getProductNameByCode(product.product_code) + " : " + Util.addComma(product.sale_price.toString())+ " บาท")
                }
                list.add("รวม " + sum.toString() + " ชิ้น")

                val param = DialogParamModel(getString(R.string.msg_confirm_title_order), list, getString(R.string.text_confirm), getString(R.string.text_cancel))
                DialogUtil.showConfirmDialog(param, this, DialogUtil.InputTextBackListerner {
                    if (it.equals(DialogUtil.CONFIRM)) {
                        val dialog = createProgressDialog(this, "Loading...")
                        dialog.show()
                        val model = LendParamModel(customerId, customerName, customerAddress, customerPhoto, customerPhone, productList, deadline)
                        Network.lend(model, object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                dialog.dismiss()
                                Log.e("panya", "onResponse : $response")
                                val status = response.getString("status_code")
                                if (status == "200") {
                                    val data = response.getJSONObject("data")
                                    printSlip(Gson().fromJson(data.toString(), OrderModel::class.java))
                                    showConfirmDialog(data)
                                } else {
                                    showResponse(status, this@BorrowActivity)
                                }
                            }

                            override fun onError(error: ANError) {
                                dialog.dismiss()
                                error?.errorBody?.let {
                                    val jObj = JSONObject(it)
                                    if (jObj.has("status_code")) {
                                        val status = jObj.getString("status_code")
                                        showResponse(status, this@BorrowActivity)
                                    }
                                }
                                error?.let {
                                    showResponse(error.errorCode.toString(), this@BorrowActivity)
                                }
                                error.printStackTrace()
                                Log.e("panya", "onError : " + error.errorCode +", detail "+error.errorDetail+", errorBody"+ error.errorBody)
                            }
                        } )
                    }
                })
            } else {
                Toast.makeText(this@BorrowActivity, "ข้อมูลไม่ครบถ้วน", Toast.LENGTH_LONG).show()
            }
        }
        edt_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (!query.equals("")) {
                        if (checkContains(query)) {
                            Toast.makeText(this@BorrowActivity, "สินค้านี้ถูกเพิ่มในรายการแล้ว", Toast.LENGTH_LONG).show()
                        } else {
                            loadItem(query)
                        }
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
        val list = listOf(getString(R.string.dialog_msg_for_shop))
        val dialogParamModel = DialogParamModel("ปริ้น", list, getString(R.string.text_ok), getString(R.string.text_skip))
        DialogUtil.showConfirmDialog(dialogParamModel, this, DialogUtil.InputTextBackListerner {
            if (it.equals(DialogUtil.CONFIRM)) {
                printSlip(Gson().fromJson(data.toString(), OrderModel::class.java))
            }
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

    fun getProductNameByCode(code: String): String {
        for (product in productModelList) {
            if (product.product_code.equals(code)) {
                return product.product_name
            }
        }
        return code
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
                    val dataJsonObj = response.getJSONObject("data")
                    val data = Gson().fromJson(dataJsonObj.toString(), ProductModel::class.java)
                    if (data.status_id.equals(ProductStatus.READY_FOR_SALE.statusId)) {
                        val product = SellProductModel(key, 0)//Integer.parseInt(data.sale.replace(".00", "")))
                        productModelList.add(data)
                        productList.add(product)
                        addItemView(data)
                    } else {
                        DialogUtil.showNotiDialog(this@BorrowActivity, "สินค้าไม่พร้อมให้ยืม", "")
                    }
                } else {
                    showResponse(status, this@BorrowActivity)
                }
            }

            override fun onError(error: ANError) {
                error.printStackTrace()
                var status = error.errorCode.toString()
                error.errorBody?.let {
                    val jObj = JSONObject(it)
                    if (jObj.has("status_code")) {
                        status = jObj.getString("status_code")
                    }
                }
                showResponse(status, this@BorrowActivity)
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
        txtId.text = productModel.product_name
        txtDetail.text = productModel.detail
        txtCost.text = Util.addComma(productModel.cost) + " บาท"
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
            removeProduct(it.tag as String)
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
                        val price = result.replace(",", "").toInt()
                        val productCode = contentView.tag as String
                        val sellProductModel = SellProductModel(productCode, price)
                        updatePrice(sellProductModel)
                        txtSell.text = Util.addComma(price.toString()+".00") + " บาท"

                    }

                }
            })
        }
        item_container.addView(contentView)
    }

    fun removeProduct(product_code: String) {
        for (product in productModelList) {
            if (product.equals(product_code)) {
                productModelList.remove(product)
                break
            }
        }
    }
    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, getString(R.string.please_back_again), Toast.LENGTH_SHORT).show()

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    fun updatePrice(sellPrice: SellProductModel) {
        for (product in productList) {
            if (product.product_code.equals(sellPrice.product_code)) {
                product.sale_price = sellPrice.sale_price
                break
            }
        }
    }

    override fun setupView(info: ThiaIdInfoBeen) {
        super.setupView(info)
        edt_name.setText(info.thaiTitle +" "+info.thaiFirstName + "  " + info.thaiLastName)
        edt_idcard.setText(info.citizenId?.substring(0, info.citizenId.length-3) + "XXX")
    }

    fun printSlip(data: OrderModel) {
        val textList = ArrayList<PrinterParams>()

        var bitmap = createImageBarcode(data.order_code, "Barcode")!!
        bitmap = Utility.toGrayscale(bitmap)

        val title = Util.textToBitmap(data.type_name)
        var printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(title)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(24)
        printerParams1.setText("\n")
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(bitmap)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(24)
        printerParams1.setText(data.order_code)
        textList.add(printerParams1)


        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("ร้าน "+ PreferencesManager.getInstance().companyName)
        textList.add(printerParams1)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("สาขา "+ PreferencesManager.getInstance().companyBranchName)
        textList.add(printerParams1)

        textList.add(getAddress())
        textList.add(getPhoneNumber())
        textList.add(getZipCode())

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("วันที่ " + Util.toDateFormat(data.date_create))
        textList.add(printerParams1)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("วันครบกำหนด "+Util.toDateFormat(data.date_expire))
        textList.add(printerParams1)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("ลูกค้า "+data.customer_name)
        textList.add(printerParams1)


        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("รหัสปชช. "+data.idcard)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("รายการสินค้า\n")
        textList.add(printerParams1)

//        val list = Util.productListToProductList2Sell(data.products)
//        val listBitmap = Util.productListToBitmap(list)
//        printerParams1 = TssPrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
//        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
//        printerParams1.setBitmap(listBitmap)
//        textList.add(printerParams1)

        var i = 0
        var sumPrice = 0.0
        for (product in data.products) {
            i++
            var name = product.product_name
            var detail = product.detail
            sumPrice += product.sale.toDouble()
            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(20)
            printerParams1.setText("\n" + i + ". " + name.replace(" "," ")+"\n"+detail.replace(" "," "))
            textList.add(printerParams1)

            val listProduct = arrayListOf<ProductModel>()
            listProduct.add(product)
            val list = Util.productListToProductList3Sell(listProduct)
            val listBitmap = Util.productListToBitmap2(list)
            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
            printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
            printerParams1.setBitmap(listBitmap)
            textList.add(printerParams1)

        }

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(24)
        printerParams1.setText("\n")
        textList.add(printerParams1)

        val productModel = data.products.get(0)
        productModel.product_name = getString(R.string.summary_price)
        productModel.sale = sumPrice.toString()
        val listProduct = arrayListOf<ProductModel>()
        listProduct.add(productModel)
        val list = Util.productListToProductList3Sell(listProduct)
        val listBitmap = Util.productListToBitmap(list)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(listBitmap)
        textList.add(printerParams1)

        textList.add(dashSignature())
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(24)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        textList.add(Util.dashLine(this@BorrowActivity))
        printdata(textList)
    }
}