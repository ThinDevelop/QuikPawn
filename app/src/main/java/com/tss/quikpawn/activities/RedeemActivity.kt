package com.tss.quikpawn.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewManager
import android.widget.*
import androidx.appcompat.widget.SearchView
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.centerm.centermposoversealib.thailand.ThiaIdInfoBeen
import com.centerm.centermposoversealib.util.Utility
import com.centerm.smartpos.aidl.printer.PrinterParams
import com.google.gson.Gson
import com.stfalcon.multiimageview.MultiImageView
import com.tss.quikpawn.BaseK9Activity
import com.tss.quikpawn.PreferencesManager
import com.tss.quikpawn.R
import com.tss.quikpawn.ScanActivity
import com.tss.quikpawn.models.*
import com.tss.quikpawn.networks.Network
import com.tss.quikpawn.util.DialogUtil
import com.tss.quikpawn.util.NumberTextWatcherForThousand
import com.tss.quikpawn.util.Util
import kotlinx.android.synthetic.main.activity_redeem.*
import kotlinx.android.synthetic.main.item_customer_info.*
import kotlinx.android.synthetic.main.item_search.*
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

class RedeemActivity : BaseK9Activity() {
    var summary = 0.00
    var mulctPrice = 0.00
    var cost = 0.00
    val SELECT_ORDER_REQUEST_CODE = 2015
    var interestOrderModel: OrderModel? = null
    var listInterestMonthModel = mutableListOf<InterestMonthModel>()
    var orderList = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_redeem)
        title = getString(R.string.redeem_item)
        scan.setOnClickListener {
            if (orderList.isNotEmpty()) {
                DialogUtil.showNotiDialog(
                    this@RedeemActivity,
                    "ไม่สามารถทำการค้นหาได้",
                    "กรุณาลบรายการก่อนหน้าออกก่อน"
                )
            } else {
                val intent = Intent(this@RedeemActivity, ScanActivity::class.java)
                startActivityForResult(intent, SCAN_REQUEST_CODE)
            }
        }
        btn_clearsign.setOnClickListener {
            signature_pad.clear()
        }

        btn_ok.setOnClickListener {
            val customerName = edt_name.text.toString()
            var customerId = citizenId
            var customerAddress = address
            var customerPhoto = customerPhoto
            var customerPhoneNumber = edt_phonenumber.text.toString()
            if (customerId.isEmpty()) {
                customerId = edt_idcard.text.toString()
            }

            if (customerPhoneNumber.length != 10) {
                DialogUtil.showNotiDialog(this, getString(R.string.data_is_wrong), getString(R.string.wrong_phone_number))
                return@setOnClickListener
            }

            if (!customerName.isEmpty() &&
//                !customerId.isEmpty() &&
                interestOrderModel != null
            ) {

                val list = mutableListOf("รหัสลูกค้า : " + customerId)
                list.add("รายการเลขที่ " + interestOrderModel!!.order_code)
                list.add(
                    getString(
                        R.string.pay_interest, Util.addComma((cost + summary + mulctPrice).toString())
                    )
                )

                if ((cost + summary + mulctPrice) == 0.0) {
                    DialogUtil.showNotiDialog(
                        this@RedeemActivity,
                        getString(R.string.data_missing),
                        getString(R.string.please_add_pay_per_month)
                    )
                    return@setOnClickListener
                }
                val param =
                    DialogParamModel(
                        getString(R.string.msg_confirm_title_order),
                        list,
                        getString(R.string.text_confirm),
                        getString(R.string.text_cancel)
                    )
                DialogUtil.showConfirmDialog(param, this, DialogUtil.InputTextBackListerner {
                    if (DialogUtil.CONFIRM.equals(it)) {
                        val x = RedeemParamModel(
                            interestOrderModel!!.order_code,
                            customerId,
                            customerName,
                            customerAddress,
                            customerPhoto,
                            customerPhoneNumber,
                            listInterestMonthModel,
                            cost.toString(),
                            mulctPrice.toString(),
                            PreferencesManager.getInstance().userId
                        )
                        Network.redeem(x, object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                Log.e("panya", "onResponse : $response")
                                val status = response.getString("status_code")
                                if (status == "200") {
                                    val dataJsonObj = response.getJSONObject("data")
                                    Log.e("panya", dataJsonObj.toString())
                                    printSlip(
                                        Gson().fromJson(
                                            dataJsonObj.toString(),
                                            OrderModel::class.java
                                        )
                                    )
                                    showConfirmDialog(dataJsonObj)
                                } else {
                                    showResponse(status, this@RedeemActivity)
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
                                showResponse(status, this@RedeemActivity)
                                Log.e(
                                    "panya",
                                    "onError : " + error.errorCode + ", detail " + error.errorDetail + ", errorBody" + error.errorBody
                                )
                            }
                        })
                    }
                })
            }
        }
        edt_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (orderList.isNotEmpty()) {
                    DialogUtil.showNotiDialog(
                        this@RedeemActivity,
                        "ไม่สามารถทำการค้นหาได้",
                        "กรุณาลบรายการก่อนหน้าออกก่อน"
                    )
                } else {
                    query?.let {
                        if (!query.equals("")) {
                            if (checkContains(query)) {
                                Toast.makeText(
                                    this@RedeemActivity,
                                    "รายการนี้มีอยู่แล้ว",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                loadOrder(query)
                            }
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

    fun checkContains(id: String): Boolean {
        return orderList.contains(id)
    }

    fun showConfirmDialog(data: JSONObject) {
        val list = listOf(getString(R.string.dialog_msg_for_shop))
        val dialogParamModel = DialogParamModel(
            "ปริ้น",
            list,
            getString(R.string.text_ok),
            getString(R.string.text_skip)
        )
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
                loadOrder(barcode!!)
            } else if (requestCode == SELECT_ORDER_REQUEST_CODE) {
                val barcode = data?.getStringExtra("order_code")
                loadOrder(barcode!!)
            }
        }
    }

    fun loadOrder(key: String) {
        if (key.length == 13) {
            Network.searchOrderByIdCardAndType(
                key,
                OrderType.CONSIGNMENT.typeId,
                object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        Log.e("panya", "onResponse : $response")
                        val status = response.getString("status_code")
                        if (status == "200") {
                            val dataJsonArray = response.getJSONArray("data")
                            if  (dataJsonArray.length() == 0) {
                                DialogUtil.showNotiDialog(
                                    this@RedeemActivity,
                                    getString(R.string.order_not_found),
                                    getString(R.string.order_not_found)
                                )
                            } else {
                                val intent =
                                    Intent(this@RedeemActivity, OrderListActivity::class.java)
                                intent.putExtra("order_list", dataJsonArray.toString())
                                startActivityForResult(intent, SELECT_ORDER_REQUEST_CODE)
                            }
                        } else {
                            showResponse(status, this@RedeemActivity)
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
                        showResponse(status, this@RedeemActivity)
                        Log.e(
                            "panya",
                            "onError : " + error.errorCode + ", detail " + error.errorDetail + ", errorBody" + error.errorBody
                        )
                    }
                })
        } else if (checkContains(key)) {
            Toast.makeText(
                this@RedeemActivity,
                "รายการนี้มีอยู่แล้ว",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Network.searchOrder(
                key,
                OrderType.CONSIGNMENT.typeId,
                object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        Log.e("panya", "onResponse : $response")
                        val status = response.getString("status_code")
                        if (status == "200") {
                            val dataJsonObj = response.getJSONObject("data")
                            interestOrderModel = Gson().fromJson(
                                dataJsonObj.toString(),
                                OrderModel::class.java
                            )
                            addItemView(interestOrderModel!!)
                        } else {
                            showResponse(status, this@RedeemActivity)
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
                        showResponse(status, this@RedeemActivity)
                        Log.e(
                            "panya",
                            "onError : " + error.errorCode + ", detail " + error.errorDetail + ", errorBody" + error.errorBody
                        )
                    }
                })
        }
    }

    override fun setupView(info: ThiaIdInfoBeen) {
        super.setupView(info)
        edt_name.setText(info.thaiTitle +" "+info.thaiFirstName + "  " + info.thaiLastName)
        edt_idcard.setText(info.citizenId?.substring(0, info.citizenId.length - 3) + "XXX")
        if (interestOrderModel == null) {
            loadOrder(citizenId)
        }
    }

    fun addItemView(interestOrder: OrderModel) {
        orderList.add(interestOrder.order_code)
        val inflater = LayoutInflater.from(baseContext)
        val contentView: View = inflater.inflate(R.layout.item_redeem_detail, null, false)
        val delete = contentView.findViewById<ImageView>(R.id.btn_delete)
        val layout = contentView.findViewById<LinearLayout>(R.id.interest_container)
        val mulct = contentView.findViewById<EditText>(R.id.edt_mulct)
        val summaryInterest = contentView.findViewById<TextView>(R.id.summary_pay)
        val imgProduct = contentView.findViewById<MultiImageView>(R.id.img_product)
        val orderId = contentView.findViewById<TextView>(R.id.order_id)

        mulct.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editText: Editable?) {
                try {
                    mulct.removeTextChangedListener(this)
                    val value: String = mulct.getText().toString()
                    if (value != null && value != "") {
                        if (value.startsWith(".")) {
                            mulct.setText("0.")
                        }
                        if (value.startsWith("0") && !value.startsWith("0.")) {
                            mulct.setText("")
                        }
                        val str: String =
                            mulct.getText().toString().replace(",", "")
                        if (value != "") mulct.setText(
                            NumberTextWatcherForThousand.getDecimalFormattedString(
                                str
                            )
                        )
                        mulct.setSelection(mulct.getText().toString().length)
                    }
                    mulct.addTextChangedListener(this)
                    return
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    mulct.addTextChangedListener(this)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0?.let {
                    if (p0.isEmpty()) {
                        mulctPrice = 0.0
                    } else {
                        mulctPrice = NumberTextWatcherForThousand.trimCommaOfString(p0.toString()).toDouble()
                    }
                }
                summaryInterest.text =
                    getString(R.string.pay_interest, Util.addComma((cost + summary + mulctPrice).toString()))
            }
        })
        imgProduct.shape = MultiImageView.Shape.RECTANGLE
        imgProduct.rectCorners = 10
        orderId.text = interestOrder.order_code
        cost = interestOrder.total.toDouble()
        var i = 0
        for (product in interestOrder.products) {
            i++
            if (i>3) continue
            Glide.with(this) //1
                .asBitmap()
                .load(product.image_small)
                .placeholder(R.drawable.ic_image_black_24dp)
                .error(R.drawable.ic_broken_image_black_24dp)
                .skipMemoryCache(true) //2
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE) //3
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {
                        imgProduct.addImage(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {

                    }
                })
        }

        summaryInterest.text =
            getString(R.string.pay_interest, Util.addComma((cost + summary + mulctPrice).toString()))

        delete.visibility = View.VISIBLE
        contentView.tag = item_container.childCount
        delete.tag = contentView.tag
        delete.setOnClickListener {
            (item_container.findViewWithTag<View>(it.tag).parent as ViewManager).removeView(
                item_container.findViewWithTag<View>(it.tag)
            )
            orderList.clear()
            interestOrderModel = null
        }

        for (interest in interestOrder.interests) {
            val checkBox = CheckBox(this)
            checkBox.text = "เดือนที่ : " + interest.month
            checkBox.isEnabled = !interest.status
            checkBox.isChecked = interest.status
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
//                Toast.makeText(this, isChecked.toString(), Toast.LENGTH_SHORT).show()
                if (isChecked) {
                    summary += interest.price.toDouble()
                    val interestMonth = InterestMonthModel(interest.month, interest.price)
                    listInterestMonthModel.add(interestMonth)
                } else {
                    summary -= interest.price.toDouble()
                    for (monthModel in listInterestMonthModel) {
                        if (monthModel.month.equals(interest.month)) {
                            listInterestMonthModel.remove(monthModel)
                            break
                        }
                    }
                }
                summaryInterest.text =
                    getString(R.string.pay_interest, Util.addComma((cost.toDouble() + summary + mulctPrice.toDouble()).toString()))
            }
            layout.addView(checkBox)
        }
        insertCheckbox(
            layout,
            (interestOrder.interests[0].price.toDouble() / 2).toString(),
            "ดอกเบี้ยครึ่งเดือน",
            summaryInterest
        )
        insertCheckbox(
            layout,
            interestOrder.interests[0].price,
            "ดอกเบี้ยเต็มเดือน",
            summaryInterest
        )

        item_container.addView(contentView)
    }

    fun insertCheckbox(linearLayout: LinearLayout, price: String, msg: String, sumText: TextView) {
        val checkBox = CheckBox(this)
        checkBox.text = msg
        checkBox.isEnabled = true
        checkBox.isChecked = false
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
//            Toast.makeText(this, isChecked.toString(), Toast.LENGTH_SHORT).show()
            if (isChecked) {
                mulctPrice += price.toDouble()
            } else {
                mulctPrice -= price.toDouble()
            }
            sumText.text =
                getString(R.string.pay_interest, Util.addComma((cost + summary + mulctPrice).toString()))
        }
        linearLayout.addView(checkBox)
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
        printerParams1.setText("ร้าน " + PreferencesManager.getInstance().companyName)
        textList.add(printerParams1)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("สาขา " + PreferencesManager.getInstance().companyBranchName)
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
        printerParams1.setText("ลูกค้า " + data.customer_name)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("รหัสปชช. " + data.idcard)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(18)
        printerParams1.setText("รายการสินค้า")
        textList.add(printerParams1)

//        val list = Util.productListToProductList2Cost(data.products)
//        val listBitmap = Util.productListToBitmap(list)
//        printerParams1 = TssPrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
//        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
//        printerParams1.setBitmap(listBitmap)
//        textList.add(printerParams1)
        var i = 0
        for (product in data.products) {
            i++
            var name = product.product_name
            var detail = product.detail
            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(20)
            printerParams1.setText("\n" + i + ". " + name.replace(" "," "))
            textList.add(printerParams1)

            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(20)
            printerParams1.setText(detail)
            textList.add(printerParams1)

            val listProduct = arrayListOf<ProductModel>()
            listProduct.add(product)
            val list = Util.productListToProductList3Cost(listProduct)
            val listBitmap = Util.productListToBitmap2(list)
            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
            printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
            printerParams1.setBitmap(listBitmap)
            textList.add(printerParams1)

        }

        val list2 = arrayListOf<ProductModel2>()
        if (data.interests.isNotEmpty()) {
            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(18)
            printerParams1.setText("รายการดอกเบี้ย")
            textList.add(printerParams1)

            for (interest in data.interests) {
                list2.add(ProductModel2("เดือนที่ : " + interest.month, Util.addComma(interest.price) + " บาท"))
            }
            list2.add(ProductModel2("ค่าปรับ", Util.addComma(data.mulct_price) + " บาท"))
            val listBitmap = Util.productListToBitmap(list2)
            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
            printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
            printerParams1.setBitmap(listBitmap)
            textList.add(printerParams1)
        } else {
            list2.add(ProductModel2("ค่าปรับ", Util.addComma(data.mulct_price) + " บาท"))
            val listBitmap = Util.productListToBitmap(list2)
            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
            printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
            printerParams1.setBitmap(listBitmap)
            textList.add(printerParams1)
        }

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
        printerParams1.setTextSize(24)
        printerParams1.setText("ยอดชำระ " + Util.addComma(data.total) + " บาท")
        textList.add(printerParams1)
        textList.add(Util.dashSignature())
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        textList.add(Util.dashLine(this@RedeemActivity))
        printdata(textList)
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
}