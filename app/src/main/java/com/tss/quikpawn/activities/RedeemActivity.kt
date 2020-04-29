package com.tss.quikpawn.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
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
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.centerm.centermposoversealib.thailand.ThaiIDSecurityBeen
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
import com.tss.quikpawn.util.Util
import kotlinx.android.synthetic.main.activity_redeem.*
import kotlinx.android.synthetic.main.item_customer_info.*
import kotlinx.android.synthetic.main.item_redeem_detail.*
import kotlinx.android.synthetic.main.item_search.*
import org.json.JSONObject

class RedeemActivity: BaseK9Activity() {
    var summary = 0.0f
    var mulctPrice = 0.0f
    var cost = 0
    val SELECT_ORDER_REQUEST_CODE = 2015
    var interestOrderModel: OrderModel? = null
    var listInterestMonthModel = mutableListOf<InterestMonthModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_redeem)
        title = getString(R.string.redeem_item)
        scan.setOnClickListener {
            val intent = Intent(this@RedeemActivity, ScanActivity::class.java)
            startActivityForResult(intent, SCAN_REQUEST_CODE)
        }
        btn_clearsign.setOnClickListener{
            signature_pad.clear()
        }

        btn_ok.setOnClickListener {
            var signatureBitmap = signature_pad.getSignatureBitmap()
            signatureBitmap = Bitmap.createScaledBitmap(signatureBitmap, 130, 130, false)
            val customerName = edt_name.text.toString()
            var customerId = citizenId
            val signature = Util.bitmapToBase64(signatureBitmap)
            if (customerId.isEmpty()) {
                customerId = edt_idcard.text.toString()
            }
            if (!customerName.isEmpty() &&
                !customerId.isEmpty() &&
                !signature.isEmpty() &&
                interestOrderModel != null
            ) {

                val list = mutableListOf("รหัสลูกค้า : " + customerId)
                list.add(interestOrderModel!!.order_code)
                list.add(getString(R.string.pay_interest, (cost + summary + mulctPrice).toString()))

                val param = DialogParamModel(getString(R.string.msg_confirm_title_order), list, "ยืนยัน")
                DialogUtil.showConfirmDialog(param, this, DialogUtil.InputTextBackListerner {

                    val x = RedeemParamModel(
                        interestOrderModel!!.order_code,
                        interestOrderModel!!.idcard,
                        interestOrderModel!!.customer_name,
                        listInterestMonthModel,
                        cost.toString(),
                        mulctPrice.toString(),
                        signature,
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
                                finish()
                            }
                        }

                        override fun onError(error: ANError) {
                            error.printStackTrace()
                            Log.e(
                                "panya",
                                "onError : " + error.errorCode + ", detail " + error.errorDetail + ", errorBody" + error.errorBody
                            )
                        }
                    })
                })
            }
        }
        edt_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (!query.equals("")) {
                        loadOrder(query)
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
            Network.searchOrderByIdCardAndType(key, OrderType.CONSIGNMENT.typeId, object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e("panya", "onResponse : $response")
                    val status = response.getString("status_code")
                    if (status == "200") {
                        val dataJsonArray = response.getJSONArray("data")
                        val intent = Intent(this@RedeemActivity, OrderListActivity::class.java)
                        intent.putExtra("order_list", dataJsonArray.toString())
                        startActivityForResult(intent, SELECT_ORDER_REQUEST_CODE)
                    }
                }

                override fun onError(error: ANError) {
                    error.printStackTrace()
                    Log.e("panya", "onError : " + error.errorCode +", detail "+error.errorDetail+", errorBody"+ error.errorBody)
                }
            } )
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
                        }
                    }

                    override fun onError(error: ANError) {
                        error.printStackTrace()
                        Log.e(
                            "panya",
                            "onError : " + error.errorCode + ", detail " + error.errorDetail + ", errorBody" + error.errorBody
                        )
                    }
                })
        }
    }

    override fun setupView(info: ThaiIDSecurityBeen) {
        super.setupView(info)
        edt_name.setText(info.thaiFirstName + " " + info.thaiLastName)
        edt_idcard.setText(info.citizenId?.substring(0, info.citizenId.length-3) + "XXX")
        if (interestOrderModel == null) {
            loadOrder(citizenId)
        }
    }

    fun addItemView() {
        val inflater = LayoutInflater.from(baseContext)
        val contentView: View = inflater.inflate(R.layout.item_redeem_detail, null, false)
        val delete = contentView.findViewById<ImageView>(R.id.btn_delete)
        delete.visibility = View.VISIBLE
        contentView.tag = item_container.childCount
        delete.tag = contentView.tag
        delete.setOnClickListener {
            (item_container.findViewWithTag<View>(it.tag).parent as ViewManager).removeView(item_container.findViewWithTag<View>(it.tag))
        }

        item_container.addView(contentView)
    }

    fun addItemView(interestOrder: OrderModel) {
        val inflater = LayoutInflater.from(baseContext)
        val contentView: View = inflater.inflate(R.layout.item_redeem_detail, null, false)
        val delete = contentView.findViewById<ImageView>(R.id.btn_delete)
        val layout = contentView.findViewById<LinearLayout>(R.id.interest_container)
        val mulct = contentView.findViewById<EditText>(R.id.edt_mulct)
        val summaryInterest = contentView.findViewById<TextView>(R.id.summary_pay)
        val imgProduct = contentView.findViewById<MultiImageView>(R.id.img_product)
        val orderId = contentView.findViewById<TextView>(R.id.order_id)

        imgProduct.shape = MultiImageView.Shape.RECTANGLE
        imgProduct.rectCorners = 10
        orderId.text = interestOrder.order_code
        cost = Integer.parseInt(interestOrder.total)
        for (product in interestOrder.products) {
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

        summaryInterest.text = getString(R.string.pay_interest, (cost + summary + mulctPrice).toString())

        delete.visibility = View.VISIBLE
        contentView.tag = item_container.childCount
        delete.tag = contentView.tag
        delete.setOnClickListener {
            (item_container.findViewWithTag<View>(it.tag).parent as ViewManager).removeView(item_container.findViewWithTag<View>(it.tag))
            interestOrderModel = null
        }

        for (interest in interestOrder.interest) {
            val checkBox = CheckBox(this)
            checkBox.text = "เดือนที่ : "+ interest.month
            checkBox.isEnabled = !interest.status
            checkBox.isChecked = interest.status
            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                Toast.makeText(this,isChecked.toString(), Toast.LENGTH_SHORT).show()
                if (isChecked) {
                    summary += interest.price.toLong()
                    val interestMonth = InterestMonthModel(interest.month, interest.price)
                    listInterestMonthModel.add(interestMonth)
                } else {
                    summary -= interest.price.toLong()
                    for (monthModel in listInterestMonthModel) {
                        if (monthModel.month.equals(interest.month)) {
                            listInterestMonthModel.remove(monthModel)
                            break
                        }
                    }
                }
                summaryInterest.text = getString(R.string.pay_interest, (cost + summary + mulctPrice).toString())
            }
            layout.addView(checkBox)
        }
        insertCheckbox(layout, (interestOrder.interest[0].price.toLong()/2).toString(), "ดอกเบี้ยครึ่งเดือน", summaryInterest)
        insertCheckbox(layout, interestOrder.interest[0].price, "ดอกเบี้ยเต็มเดือน", summaryInterest)

        mulct.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0?.let {
                    if (p0.isEmpty()) {
                        mulctPrice = 0.0f
                    }else {
                        mulctPrice = p0.toString().toFloat()
                    }
                }
                summaryInterest.text = getString(R.string.pay_interest, (cost + summary + mulctPrice).toString())
            }
        })

        item_container.addView(contentView)
    }

    fun insertCheckbox(linearLayout: LinearLayout, price: String, msg: String, sumText: TextView) {
        val checkBox = CheckBox(this)
        checkBox.text = msg
        checkBox.isEnabled = true
        checkBox.isChecked = false
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            Toast.makeText(this,isChecked.toString(), Toast.LENGTH_SHORT).show()
            if (isChecked) {
                mulctPrice += price.toLong()
            } else {
                mulctPrice -= price.toLong()
            }
            sumText.text = getString(R.string.pay_interest, (cost + summary + mulctPrice).toString())
        }
        linearLayout.addView(checkBox)
    }

    fun printSlip(data: OrderModel) {
        val textList = ArrayList<PrinterParams>()

        var bitmap = createImageBarcode(data.order_code, "Barcode")!!
        bitmap = Utility.toGrayscale(bitmap)

        var printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(24)
        printerParams1.setText("รายการ " + data.type_name)
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
        printerParams1.setText("ลูกค้า " + data.customer_name)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("รหัสปชช. " + data.idcard)
        textList.add(printerParams1)

//        printerParams1 = PrinterParams()
//        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
//        printerParams1.setTextSize(18)
//        printerParams1.setText("รายการสินค้า")
//        textList.add(printerParams1)
//
//        for (product in data.products) {
//            printerParams1 = PrinterParams()
//            printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
//            printerParams1.setTextSize(22)
//            printerParams1.setText("*" + product.product_code + " " + product.cost + " บาท")
//            textList.add(printerParams1)
//        }
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
        printerParams1.setTextSize(24)
        printerParams1.setText("ยอดชำระ " + data.total + " บาท")
        textList.add(printerParams1)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        printdata(textList)
    }
}