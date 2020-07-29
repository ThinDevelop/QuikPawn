package com.tss.quikpawn

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.centerm.centermposoversealib.thailand.ThiaIdInfoBeen
import com.centerm.centermposoversealib.util.Utility
import com.centerm.smartpos.aidl.printer.PrinterParams
import com.google.gson.Gson
import com.tss.quikpawn.adapter.ConsignListAdapter
import com.tss.quikpawn.models.*
import com.tss.quikpawn.networks.Network
import com.tss.quikpawn.util.DialogUtil
import com.tss.quikpawn.util.NumberTextWatcherForThousand
import com.tss.quikpawn.util.Util
import com.tss.quikpawn.util.Util.Companion.addRectangle
import com.tss.quikpawn.util.Util.Companion.getMonth
import com.tss.quikpawn.util.Util.Companion.rotageBitmap
import com.tss.quikpawn.util.Util.Companion.stringToCalendar
import kotlinx.android.synthetic.main.activity_consignment.*
import kotlinx.android.synthetic.main.item_sign_view.*
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class ConsignmentActivity : BaseK9Activity(), ConsignListAdapter.OnItemClickListener{
    var expire = "0"
    var interest = "1"
    private val PERMISSION_CODE = 2000
    var imageIdFilePath = ""
    private var IMAGE_CAPTURE_IDCARD_CODE = 2001
    lateinit var consignListAdapter: ConsignListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consignment)
        title = getString(R.string.consignment_item)
        consignListAdapter = ConsignListAdapter(this, this)
        con_recycler_view.setLayoutManager(LinearLayoutManager(this))
        con_recycler_view.setHasFixedSize(true)
        con_recycler_view.setAdapter(consignListAdapter)
        new_item.setOnClickListener{
            consignListAdapter.newItem()
        }

        clearsign_btn.setOnClickListener{
            signature_pad.clear()
        }

        img_take_card.setOnClickListener {
            openCameraForCard()
        }

        btn_ok.setOnClickListener {
            val customerName = edt_name.text.toString()
            var customerId = citizenId
            interest = edt_interest_rate.text.toString()
            val phoneNumber = edt_phonenumber.text.toString()
            expire = edt_time.text.toString()
            address = edt_address.text.toString()
            if (customerId.isEmpty()) {
                customerId = edt_idcard.text.toString()
            }
            if (!customerName.isEmpty() &&
                !customerId.isEmpty() &&
                !interest.isEmpty() && !interest.equals("0") &&
                !phoneNumber.isEmpty() &&
                !expire.isEmpty() &&
                !address.isEmpty()
//                && (loadingProgressBar != null && !loadingProgressBar!!.isShown)
            ) {

                val productList  = consignListAdapter.getConProductItems()
                for (product in productList) {
                    val camera = product.ref_image
                    val detail = product.detail
                    val cost = product.cost
                    val name = product.name
                    val costStr= NumberTextWatcherForThousand.trimCommaOfString(cost)
                    product.cost = costStr
                    if (camera.isEmpty()) {
                        DialogUtil.showNotiDialog(this, getString(R.string.data_missing), getString(R.string.please_add_photo))
                        return@setOnClickListener
                    } else if (name.isEmpty()) {
                        DialogUtil.showNotiDialog(this, getString(R.string.data_missing), getString(R.string.please_add_name))
                        return@setOnClickListener
                    } else if (costStr.isEmpty()) {
                        DialogUtil.showNotiDialog(this, getString(R.string.data_missing), getString(R.string.please_add_price))
                        return@setOnClickListener
                    } else if (phoneNumber.length != 10) {
                        DialogUtil.showNotiDialog(this, getString(R.string.data_is_wrong), getString(R.string.wrong_phone_number))
                        return@setOnClickListener
                    }
                }

                var sum = 0.0
                val list = mutableListOf("รหัสลูกค้า : " + customerId + "\nรายการ")
                for (product in productList) {
                    list.add(product.name + " : " + Util.addComma(product.cost)+" บาท")
                    sum += NumberTextWatcherForThousand.trimCommaOfString(product.cost).toDouble()
                }
                list.add("รวม " + Util.addComma(sum.toString()) + " บาท")

                val param = DialogParamModel(getString(R.string.msg_confirm_title_order), list, getString(R.string.text_confirm), getString(R.string.text_cancel))
                DialogUtil.showConfirmDialog(param, this, DialogUtil.InputTextBackListerner {
                    if (DialogUtil.CONFIRM.equals(it)) {
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
                        PreferencesManager.getInstance().userId,
                        getFileToByte(imageIdFilePath)
                    )
                    Network.orderConsignment(model, object : JSONObjectRequestListener {
                        override fun onResponse(response: JSONObject) {
                            dialog.dismiss()
                            Log.e("panya", "onResponse : $response")
                            val status = response.getString("status_code")
                            if (status == "200") {
                                val data = response.getJSONObject("data")
                                val orderModel = Gson().fromJson(data.toString(), OrderModel::class.java)
                                printSlip1(orderModel)
                                Handler().postDelayed({
                                    printSlip1(orderModel)
                                }, 3000)
                                showConfirmDialog(data)
                            } else {
                                showResponse(status, this@ConsignmentActivity)
                            }
                        }

                        override fun onError(error: ANError) {
                            dialog.dismiss()
                            var status = error.errorCode.toString()
                            error.errorBody?.let {
                                val jObj = JSONObject(it)
                                if (jObj.has("status_code")) {
                                    status = jObj.getString("status_code")
                                }
                            }
                            showResponse(status, this@ConsignmentActivity)
                            error.printStackTrace()
                            Log.e(
                                "panya",
                                "onError : " + error.errorCode + ", detail " + error.errorDetail + ", errorBody" + error.errorBody
                            )
                        }
                    })
                }
                })

            } else {

                Toast.makeText(this@ConsignmentActivity, "ข้อมูลไม่ครบถ้วน", Toast.LENGTH_LONG).show()
            }
        }
        new_item.callOnClick()
        initialK9()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE -> {
                if(grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    openCameraIdcard()
                }
                else{
                    alreadyOpen = true
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun openCameraForCard() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
                val permission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permission,PERMISSION_CODE)
            }
            else{
                openCameraIdcard()
            }
        } else {
            openCameraIdcard()
        }
    }
    fun openCameraIdcard() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            //Create a file to store the image
            var photoFile: File? = null
            try {

                val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES+ "/quikpawn")
                photoFile = File.createTempFile(""+System.currentTimeMillis() ,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */)
                imageIdFilePath = photoFile.getAbsolutePath()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            photoFile?.let {
                val photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile)
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(pictureIntent, IMAGE_CAPTURE_IDCARD_CODE)
            }
        }
    }

    override fun setupView(info: ThiaIdInfoBeen) {
        super.setupView(info)
        edt_name.setText(info.thaiTitle +" "+info.thaiFirstName + "  " + info.thaiLastName)
        edt_idcard.setText(info.citizenId?.substring(0, info.citizenId.length-3) + "XXX")
        edt_address.setText(address)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (IMAGE_CAPTURE_IDCARD_CODE == requestCode) {

            if (resultCode != Activity.RESULT_OK) return

            //show loading
            idcard_container?.visibility = View.VISIBLE
            img_idcard?.setImageURI(Uri.parse(imageIdFilePath))
            delete_image_idcard.setOnClickListener {
                idcard_container?.visibility = View.GONE
                imageIdFilePath = ""
            }
        } else if (IMAGE_CAPTURE_CODE == requestCode) {

            if (resultCode != Activity.RESULT_OK) {
                consignListAdapter.notifyDataSetChanged()
                con_recycler_view.scrollToPosition(index)
                return
            }
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
                        val url = data.getString("image_small")
                        consignListAdapter.updateImage(index, refCode, url)
                    } else {
                        showResponse(status, this@ConsignmentActivity)
                    }
                }

                override fun onError(error: ANError) {
                    loadingProgressBar?.visibility = View.GONE

                    var status = error.errorCode.toString()
                    error.errorBody?.let {
                        val jObj = JSONObject(it)
                        if (jObj.has("status_code")) {
                            status = jObj.getString("status_code")
                        }
                    }
                    showResponse(status, this@ConsignmentActivity)
                    error.printStackTrace()
                    Log.e("panya", "onError : " + error.errorCode +", detail "+error.errorDetail+", errorBody"+ error.errorBody)
                }
            })
        }
    }

    fun showConfirmDialog(data: JSONObject) {
        val list = listOf("สำหรับร้านค้า")
        val dialogParamModel = DialogParamModel("ปริ้น", list, getString(R.string.text_ok), getString(R.string.text_skip))
        DialogUtil.showConfirmDialog(dialogParamModel, this, DialogUtil.InputTextBackListerner {
            if (it.equals(DialogUtil.CONFIRM)) {
                printSlip(Gson().fromJson(data.toString(), OrderModel::class.java), true)
            }
            finish()

        })
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
        }
        return encodeString
    }

    fun printSlip1(data: OrderModel) {
        val textList = java.util.ArrayList<PrinterParams>()
        var bitmap = createImageBarcode(data.order_code, "Barcode")!!
        bitmap = Utility.toGrayscale(bitmap)

        val title = Util.textToBitmap("ใบขายฝาก")
        var printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(title)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
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
        printerParams1.setTextSize(22)
        printerParams1.setText("เลขที่ "+data.order_code)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("เขียนที่ ร้าน "+PreferencesManager.getInstance().companyName)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("สาขา "+PreferencesManager.getInstance().companyBranchName)
        textList.add(printerParams1)

        textList.add(getAddress())
        textList.add(getPhoneNumber())
        textList.add(getZipCode())

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
        printerParams1.setTextSize(22)
        printerParams1.setText("วันที่ " + Util.toDateFormat(data.date_create))
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("ข้าพเจ้า"+data.customer_name+" \nบัตรประชาชนเลขที่ "+data.idcard+"\n" +
                "ผู้ขายฝากอยู่บ้านเลขที่ "+data.customer_address+"\n")
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("ได้ทำหนังสือขายฝากนี้ให้แก่ \nนาย"+PreferencesManager.getInstance().contact+" ดังมีข้อความดังต่อไปนี้\n" + "ข้อ 1. ผู้ขายฝากได้นำทรัพย์สินปรากฎตามรายการดังนี้\n\n")
        textList.add(printerParams1)

        var sum = 0.00
        for (productModel in data.products) {
            sum += productModel.cost.toDouble()
        }

        var i = 0
        for (product in data.products) {
            i++
            var name = product.product_name
            var detail = product.detail
            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(20)
            printerParams1.setText("\n" + i + ". " + name.replace(" "," ")+"\n"+detail.replace(" "," "))
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

//        for (product in data.products) {
//            val listProduct = arrayListOf<ProductModel>()
//            listProduct.add(product)
//            val list = Util.productListToProductList3Cost(listProduct)
//            val listBitmap = Util.productListToBitmap(list)
//            printerParams1 = TssPrinterParams()
//            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
//            printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
//            printerParams1.setBitmap(listBitmap)
//            textList.add(printerParams1)
//
//        }
        val calendar = stringToCalendar(data.date_expire)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\nมาขายฝากให้เป็นจำนวนเงิน \n"+ Util.addComma(sum.toString()) +" บาท\nและได้รับเงินไปเสร็จแล้วแต่วันทำหนังสือนี้\nข้อ 2. ผู้ขายฝากยอมให้คิดดอกเบี้ย\nตามจำนวนเงินที่ขายฝากไว้\n" +
                " นับตั้งแต่วันทำหนังสือนี้เป็นต้นไป\n จนกว่าจะมาไถ่ถอนคืน\nในวันที่ "+ calendar.get(Calendar.DATE) +" เดือน "+ getMonth(calendar) +" พ.ศ."+ (calendar.get(Calendar.YEAR)+543)+"\n" +
                "ข้อ 3. ผู้ขายฝากยืนยันว่าผู้ขายฝาก\nเป็นผู้มีกรรมสิทธิ์ในทรัพย์สินที่มา\nขายฝากแต่เพียงผู้เดียวและไม่มีคู่สมรสแต่อย่างใด\n" +
                "ข้อ 4. คู่กรณีได้อ่านหนีงสือนี้เข้าใจ\nรับว่าถูกต้องเป็นความจริงแล้วจึง\nลงลายมือชื่อไว้เป็นหลักฐาน\n ")
        textList.add(printerParams1)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("วันครบกำหนด "+Util.toDateFormat(data.date_expire))
        textList.add(printerParams1)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("ราคา " + Util.addComma(data.price) + " บาท")
        textList.add(printerParams1)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("ค่าธรรมเนียม "+ Util.addComma(data.interest_price) +" บาท/เดือน \nระยะเวลา "+ expire+ " เดือน")//data.interest_price
        textList.add(printerParams1)


        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n\n\n___________________")
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("ผู้ขายฝาก")
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n\n\n___________________")
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("ผู้รับซื้อฝาก")
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n\n\n___________________")
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("พยาน")
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n\n\n___________________")
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("พยาน/ผู้พิมพ์")
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\nข้าพเจ้ามอบอำนาจให้\n\n___________________")
        textList.add(printerParams1)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("มาไถ่ถอนทรัพย์แทนข้าพเจ้าลงชื่อ")
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n___________________\nผู้ขายฝาก")
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n\n")
        textList.add(printerParams1)
        textList.add(Util.dashLine(this@ConsignmentActivity))
        printdata(textList)

    }


    fun printSlip(data: OrderModel, printList: Boolean) {
        val textList = ArrayList<PrinterParams>()

        var bitmap = createImageBarcode(data.order_code, "Barcode")!!
        bitmap = Utility.toGrayscale(bitmap)

        var bitmap2 = createImageBarcode(data.order_code, "QR Code")!!
        bitmap2 = Utility.toGrayscale(bitmap2)

        val title = Util.textToBitmap(OrderType.getOrderType(data.type_id).typeName)
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
        printerParams1.setText("ร้าน "+PreferencesManager.getInstance().companyName)
        textList.add(printerParams1)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("สาขา "+PreferencesManager.getInstance().companyBranchName)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("วันที่ " + Util.toDateFormat(data.date_create))
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
        printerParams1.setText("ครบกำหนด "+Util.toDateFormat(data.date_expire))
        textList.add(printerParams1)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("ราคา " + Util.addComma(data.price) + " บาท")
        textList.add(printerParams1)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("ดอกเบี้ย "+data.interest_month + "%")
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("ระยะเวลา "+data.num_expire + "เดือน")
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
            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(20)
            printerParams1.setText("\n" + i + ". " + name.replace(" "," ")+"\n"+detail.replace(" "," "))
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
        printerParams1.setText("ชำระเงิน " + Util.addComma(data.price) + " บาท")
        textList.add(printerParams1)
        textList.add(Util.dashSignature())
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        textList.add(Util.dashLine(this@ConsignmentActivity))
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
                textList.add(Util.dashLine(this@ConsignmentActivity))

            }
        }
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

    override fun onTakePhotoClick(index: Int) {
        cameraOpen(index)
    }
}
