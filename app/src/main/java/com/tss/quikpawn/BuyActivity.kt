package com.tss.quikpawn

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.centerm.centermposoversealib.thailand.ThiaIdInfoBeen
import com.centerm.centermposoversealib.util.Utility
import com.centerm.smartpos.aidl.printer.PrinterParams
import com.google.gson.Gson
import com.tss.quikpawn.adapter.BuyListAdapter
import com.tss.quikpawn.models.*
import com.tss.quikpawn.networks.Network
import com.tss.quikpawn.util.DialogUtil
import com.tss.quikpawn.util.FileUtil
import com.tss.quikpawn.util.NumberTextWatcherForThousand
import com.tss.quikpawn.util.Util
import com.tss.quikpawn.util.Util.Companion.addRectangle
import com.tss.quikpawn.util.Util.Companion.dashLine
import com.tss.quikpawn.util.Util.Companion.rotageBitmap
import kotlinx.android.synthetic.main.activity_buy.*
import kotlinx.android.synthetic.main.item_sign_view.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream


class BuyActivity : BaseK9Activity(), BuyListAdapter.OnItemClickListener {
    lateinit var buyProductListAdapter: BuyListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy)
        title = getString(R.string.buy_item)
        buyProductListAdapter = BuyListAdapter(this, this)
        recycler_view.setLayoutManager(LinearLayoutManager(this))
        recycler_view.setHasFixedSize(true)
        recycler_view.setAdapter(buyProductListAdapter)
//        val buyProductModel = BuyProductModel("","","","","", "")
//        buyProductModelList.add(buyProductModel)
//        buyProductListAdapter.updateData(buyProductModelList)
        new_item.setOnClickListener {
            buyProductListAdapter.newItem()

//            val inflater = LayoutInflater.from(baseContext)
//            val contentView: View = inflater.inflate(R.layout.item_detail_view, null, false)
//            val delete = contentView.findViewById<ImageView>(R.id.delete_detail_btn)
//            val camera = contentView.findViewById<ImageView>(R.id.img_view1)
//            val loading = contentView.findViewById<ProgressBar>(R.id.loading)
//            val edtCost = contentView.findViewById<EditText>(R.id.edt_cost)
//
//            edtCost.addTextChangedListener(NumberTextWatcherForThousand(edtCost))
//            delete.visibility = View.VISIBLE
//            contentView.tag = layout_detail.childCount
//            delete.tag = contentView.tag
//            delete.setOnClickListener {
//                (layout_detail.findViewWithTag<View>(it.tag).parent as ViewManager).removeView(
//                    layout_detail.findViewWithTag(it.tag)
//                )
//            }
//            camera.setOnClickListener {
//                cameraOpen(it as ImageView, loading, layout_detail.childCount)
//            }
//
//            layout_detail.addView(contentView)
        }

        //clear sign button
        clearsign_btn.setOnClickListener {
            signature_pad.clear()
        }
        //get bitmap signature
        btn_ok.setOnClickListener {
            val customerName = edt_name.text.toString()
            var customerId = citizenId
            var customerAddress = address
            var customerPhoto = customerPhoto
            var customerPhone = edt_phonenumber.text.toString()
            if (customerId.isEmpty()) {
                customerId = edt_idcard.text.toString()
            }
            if (!customerName.isEmpty()
//                &&
//                !customerId.isEmpty() &&
//                (loadingProgressBar != null && !loadingProgressBar!!.isShown)
            ) {
                val productList = buyProductListAdapter.getBuyProductItems()

                for (buyProduct in productList) {
//                    val contentView = layout_detail.get(i)
//                    val camera = contentView.findViewById<ImageView>(R.id.img_view1)
                    val detail = buyProduct.detail//contentView.findViewById<EditText>(R.id.edt_detail)
                    val cost = buyProduct.cost//contentView.findViewById<EditText>(R.id.edt_cost)
                    val name = buyProduct.name//contentView.findViewById<EditText>(R.id.edt_product_name)
                    val costStr =
                        NumberTextWatcherForThousand.trimCommaOfString(cost)

                    if (buyProduct.ref_image.isEmpty()) {
                        DialogUtil.showNotiDialog(
                            this,
                            getString(R.string.data_missing),
                            getString(R.string.please_add_photo)
                        )
                        return@setOnClickListener
                    } else if (name.isEmpty()) {
                        DialogUtil.showNotiDialog(
                            this,
                            getString(R.string.data_missing),
                            getString(R.string.please_add_name)
                        )
                        return@setOnClickListener
                    } else if (costStr.isEmpty()) {
                        DialogUtil.showNotiDialog(
                            this,
                            getString(R.string.data_missing),
                            getString(R.string.please_add_price)
                        )
                        return@setOnClickListener
                    } else if (customerPhone.length != 10) {
                        DialogUtil.showNotiDialog(this, getString(R.string.data_is_wrong), getString(R.string.wrong_phone_number))
                        return@setOnClickListener
                    }
                }

                if (productList.size == 0) {
                    DialogUtil.showNotiDialog(
                        this,
                        getString(R.string.data_missing),
                        getString(R.string.please_add_item)
                    )
                    return@setOnClickListener
                }

                var sum = 0
                val list = mutableListOf("รหัสลูกค้า : " + customerId + "\nรายการ")
                for (product in productList) {
                    list.add(
                        product.name + " : " + NumberTextWatcherForThousand.getDecimalFormattedString(
                            product.cost
                        ) + " บาท"
                    )
                    sum += Integer.parseInt(product.cost)
                }
                list.add("รวม " + NumberTextWatcherForThousand.getDecimalFormattedString(sum.toString()) + " บาท")

                val param =
                    DialogParamModel(
                        getString(R.string.msg_confirm_title_order),
                        list,
                        getString(R.string.text_confirm),
                        getString(R.string.text_cancel)
                    )
                DialogUtil.showConfirmDialog(param, this, DialogUtil.InputTextBackListerner {
                    if (DialogUtil.CONFIRM.equals(it)) {
                        val dialog = createProgressDialog(this, "Loading...")
                        dialog.show()
                        val model = BuyParamModel(
                            customerId,
                            customerName,
                            customerAddress,
                            customerPhoto,
                            customerPhone,
                            productList,
                            PreferencesManager.getInstance().companyId,
                            PreferencesManager.getInstance().companyBranchId,
                            PreferencesManager.getInstance().userId
                        )
                        Network.buyItem(model, object : JSONObjectRequestListener {
                            override fun onResponse(response: JSONObject) {
                                dialog.dismiss()
                                val status = response.getString("status_code")
                                if (status == "200") {
                                    val data = response.getJSONObject("data")
                                    printSlip(
                                        Gson().fromJson(
                                            data.toString(),
                                            OrderModel::class.java
                                        ), false
                                    )
                                    showConfirmDialog(data)
                                } else {
                                    showResponse(status, this@BuyActivity)
                                }
                            }

                            override fun onError(anError: ANError) {
                                dialog.dismiss()
                                var status = anError.errorCode.toString()
                                anError.errorBody?.let {
                                    val jObj = JSONObject(it)
                                    if (jObj.has("status_code")) {
                                        status = jObj.getString("status_code")
                                    }
                                }
                                showResponse(status, this@BuyActivity)
                                anError.printStackTrace()
                                Log.e(
                                    "panya",
                                    "onError : " + anError.errorCode + ", detail " + anError.errorDetail + ", errorBody" + anError.errorBody
                                )
                            }
                        })
                    }
                })
            } else {
                Toast.makeText(this@BuyActivity, "ข้อมูลไม่ครบถ้วน", Toast.LENGTH_LONG).show()
            }
        }

        new_item.callOnClick()
        initialK9()

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
                printSlip(Gson().fromJson(data.toString(), OrderModel::class.java), true)
            }
            finish()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (IMAGE_CAPTURE_CODE == requestCode) {
            if (resultCode != Activity.RESULT_OK) {
                buyProductListAdapter.notifyDataSetChanged()
                recycler_view.scrollToPosition(index)
                return
            }
            //show loading
//            loadingProgressBar?.visibility = View.VISIBLE
            Network.uploadBase64(getFileToByte(imageFilePath), object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e("panya", "onResponse : $response")
                    loadingProgressBar?.visibility = View.GONE
                    val status = response.getString("status_code")
                    if (status == "200") {
                        val data = response.getJSONObject("data")
                        val refCode = data.getString("ref_code")
                        val url = data.getString("image_small")

//                        val buyProductModel = buyProductModelList.get(index)
//                        buyProductModel.ref_image = refCode
//                        buyProductModel.url = url
                        buyProductListAdapter.updateImage(index, refCode, url)
//                        setTagToImageView(refCode)
                    } else {
                        showResponse(status, this@BuyActivity)
                    }
                }

                override fun onError(anError: ANError) {
                    loadingProgressBar?.visibility = View.GONE
                    var status = anError.errorCode.toString()
                    anError.errorBody?.let {
                        val jObj = JSONObject(it)
                        if (jObj.has("status_code")) {
                            status = jObj.getString("status_code")
                        }
                    }
                    showResponse(status, this@BuyActivity)
                    anError.printStackTrace()
                    Log.e(
                        "panya",
                        "onError : " + anError.errorCode + ", detail " + anError.errorDetail + ", errorBody" + anError.errorBody
                    )
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
            bmp = rotageBitmap(filePath)
            bos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 30, bos)
            bt = bos.toByteArray()
            encodeString = Base64.encodeToString(bt, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bos?.close()
        }
        return encodeString
    }

    override fun setupView(info: ThiaIdInfoBeen) {
        super.setupView(info)
        Log.e("panya", "address : " + info.address)
        edt_name.setText(info.thaiTitle +" "+info.thaiFirstName + "  " + info.thaiLastName)
        edt_idcard.setText(info.citizenId?.substring(0, info.citizenId.length - 3) + "XXX")
    }

    fun printSlip(data: OrderModel, printList: Boolean) {
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
        printerParams1.isBold = true
        printerParams1.setText("ร้าน " + PreferencesManager.getInstance().companyName)
        textList.add(printerParams1)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.isBold = true
        printerParams1.setText("สาขา " + PreferencesManager.getInstance().companyBranchName)
        textList.add(printerParams1)

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
        printerParams1.setTextSize(20)
        printerParams1.setText("รายการสินค้า\n")
        textList.add(printerParams1)

        var i = 0
        for (product in data.products) {
            i++
            var name = product.product_name
            var detail = product.detail
            detail.replace(" "," ")
            name.replace(" "," ")
            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(20)
            printerParams1.setText("\n" + i + ". " + name+"\n"+detail)
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

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
        printerParams1.setTextSize(24)
        val total = data.total.replace(".00", "")
        printerParams1.setText("ชำระเงิน " + Util.addComma(total) + " บาท")
        textList.add(printerParams1)
        textList.add(Util.dashSignature())
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        textList.add(dashLine(this@BuyActivity))
        if (printList) {
            var i = 0
            for (product in data.products) {
                i++
                printerParams1 = TssPrinterParams()
                printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
                printerParams1.setTextSize(24)
                printerParams1.setText("" + i + ". " + product.product_name + "\n")
                textList.add(printerParams1)

                bitmap = Utility.toGrayscale(createImageBarcode(product.product_code, "QR Code")!!)
                bitmap = addRectangle(bitmap)
                printerParams1 = TssPrinterParams()
                printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
                printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
                printerParams1.setBitmap(bitmap)
                textList.add(printerParams1)

                printerParams1 = TssPrinterParams()
                printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
                printerParams1.setTextSize(24)
                printerParams1.setText(product.product_code + "\n\n")
                textList.add(printerParams1)
                textList.add(dashLine(this@BuyActivity))
            }
        }
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(24)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        printdata(textList)
    }

    override fun onDestroy() {
        super.onDestroy()
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES + "/quikpawn")
        FileUtil.deleteRecursive(storageDir)
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

    override fun onTakePhotoClick(index: Int) {
        cameraOpen(index)
    }
}
