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
import com.bumptech.glide.request.transition.Transition
import com.centerm.centermposoversealib.thailand.ThaiIDSecurityBeen
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
import kotlinx.android.synthetic.main.activity_interest.*
import kotlinx.android.synthetic.main.item_customer_info.*
import kotlinx.android.synthetic.main.item_interest_detail.*
import kotlinx.android.synthetic.main.item_search.*
import org.json.JSONObject


class InterestActivity: BaseK9Activity() {
    var summary = 0.00f
    var mulctPrice = 0
    var interestOrderModel: OrderModel? = null
    var listInterestMonthModel = mutableListOf<InterestMonthModel>()
    val SELECT_ORDER_REQUEST_CODE = 2015
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interest)

        title = getString(R.string.interest_item)
        scan.setOnClickListener {
            val intent = Intent(this@InterestActivity, ScanActivity::class.java)
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
            var customerAddress = address
            var customerPhoto = customerPhoto
            var phoneNumber = edt_phonenumber.text.toString()
            val signature = Util.bitmapToBase64(signatureBitmap)
            if (customerId.isEmpty()) {
                customerId = edt_idcard.text.toString()
            }
            if (!customerName.isEmpty() &&
                !customerId.isEmpty() &&
                !signature.isEmpty() &&
                interestOrderModel != null) {

                val list = mutableListOf("รหัสลูกค้า : " + customerId)
                list.add(interestOrderModel!!.order_code)
                list.add("ดอกเบี้ยรวม " + NumberTextWatcherForThousand.getDecimalFormattedString(summary.toString()) + " บาท")

                val param = DialogParamModel(getString(R.string.msg_confirm_title_order), list, "ยืนยัน")
                DialogUtil.showConfirmDialog(param, this, DialogUtil.InputTextBackListerner {
                val x = InterestParamModel(
                    interestOrderModel!!.order_code,
                    customerId,
                    customerName,
                    customerAddress,
                    customerPhoto,
                    phoneNumber,
                    listInterestMonthModel,
                    "0",
                    signature,
                    PreferencesManager.getInstance().userId
                )
                Network.interest(x, object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        Log.e("panya", "onResponse : $response")
                        val status = response.getString("status_code")
                        if (status == "200") {
                            val dataJsonObj = response.getJSONObject("data")
                            Log.e("panya", dataJsonObj.toString())

                            printSlip(Gson().fromJson(dataJsonObj.toString(), OrderModel::class.java))
                            showConfirmDialog(dataJsonObj)
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

    fun showConfirmDialog(data: JSONObject) {
        val list = listOf("สำหรับร้านค้า")
        val dialogParamModel = DialogParamModel("ปริ้น", list, "ตกลง")
        DialogUtil.showConfirmDialog(dialogParamModel, this, DialogUtil.InputTextBackListerner {
            printSlip(Gson().fromJson(data.toString(), OrderModel::class.java))
            finish()
        })
    }

    override fun setupView(info: ThiaIdInfoBeen) {
        super.setupView(info)
        edt_name.setText(info.thaiFirstName + " " + info.thaiLastName)
        edt_idcard.setText(info.citizenId?.substring(0, info.citizenId.length-3) + "XXX")
        if (interestOrderModel == null) {
            loadOrder(info.citizenId)
        }
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
                        val intent = Intent(this@InterestActivity, OrderListActivity::class.java)
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
            Network.searchOrder(key, OrderType.CONSIGNMENT.typeId, object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e("panya", "onResponse : $response")
                    val status = response.getString("status_code")
                    if (status == "200") {
                        val dataJsonObj = response.getJSONObject("data")
                        interestOrderModel = Gson().fromJson(dataJsonObj.toString(), OrderModel::class.java)
                        addItemView(interestOrderModel!!)
                    }
                }

                override fun onError(error: ANError) {
                    error.printStackTrace()
                    Log.e("panya", "onError : " + error.errorCode +", detail "+error.errorDetail+", errorBody"+ error.errorBody)
                }
            } )
        }
    }

    fun addItemView(interestOrderModel: OrderModel) {
        val inflater = LayoutInflater.from(baseContext)
        val contentView: View = inflater.inflate(R.layout.item_interest_detail, null, false)
        val delete = contentView.findViewById<ImageView>(R.id.btn_delete)
        val layout = contentView.findViewById<LinearLayout>(R.id.interest_container)
        val mulct = contentView.findViewById<EditText>(R.id.edt_mulct)
        val summaryInterest = contentView.findViewById<TextView>(R.id.summary_interest)
        val imgProduct = contentView.findViewById<MultiImageView>(R.id.img_product)
        val orderId = contentView.findViewById<TextView>(R.id.order_id)

        imgProduct.shape = MultiImageView.Shape.RECTANGLE
        imgProduct.rectCorners = 10

        orderId.text = interestOrderModel.order_code
        for (product in interestOrderModel.products) {
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
        summaryInterest.text = getString(R.string.pay_interest, NumberTextWatcherForThousand.getDecimalFormattedString((summary + mulctPrice).toString()))
        mulct.visibility = View.GONE
        delete.visibility = View.VISIBLE
        contentView.tag = item_container.childCount
        delete.tag = contentView.tag
        delete.setOnClickListener {
            this.interestOrderModel = null
            (item_container.findViewWithTag<View>(it.tag).parent as ViewManager).removeView(item_container.findViewWithTag<View>(it.tag))
        }

        for (interest in interestOrderModel.interest) {
            val checkBox = CheckBox(this)
            checkBox.text = "เดือนที่ : "+ interest.month
            checkBox.isEnabled = !interest.status
            checkBox.isChecked = interest.status
                checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                Toast.makeText(this,isChecked.toString(),Toast.LENGTH_SHORT).show()
                if (isChecked) {
                    summary += interest.price.toFloat()
                    val interestMonth = InterestMonthModel(interest.month, interest.price)
                    listInterestMonthModel.add(interestMonth)
                } else {
                    summary -= interest.price.toFloat()
                    for (monthModel in listInterestMonthModel) {
                        if (monthModel.month.equals(interest.month)) {
                            listInterestMonthModel.remove(monthModel)
                            break
                        }
                    }
                }
                summaryInterest.text = getString(R.string.pay_interest, (summary + mulctPrice).toString())
            }
            layout.addView(checkBox)
        }
        mulct.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0?.let {
                    if (p0.isEmpty()) {
                        mulctPrice = 0
                    }else {
                        mulctPrice = Integer.parseInt(p0.toString())
                    }
                }
                summaryInterest.text = getString(R.string.pay_interest, (summary + mulctPrice).toString())
            }
        })

        item_container.addView(contentView)
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
        printerParams1.setText("ลูกค้า " + data.customer_name)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("รหัสปชช. " + data.idcard)
        textList.add(printerParams1)

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