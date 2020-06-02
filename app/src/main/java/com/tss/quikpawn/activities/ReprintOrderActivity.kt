package com.tss.quikpawn.activities

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.centerm.centermposoversealib.util.Utility
import com.centerm.smartpos.aidl.printer.PrinterParams
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tss.quikpawn.BaseK9Activity
import com.tss.quikpawn.PreferencesManager
import com.tss.quikpawn.R
import com.tss.quikpawn.adapter.OrderListAdapter
import com.tss.quikpawn.models.DialogParamModel
import com.tss.quikpawn.models.OrderModel
import com.tss.quikpawn.models.OrderType
import com.tss.quikpawn.models.OrderType.getOrderType
import com.tss.quikpawn.models.ProductModel2
import com.tss.quikpawn.networks.Network
import com.tss.quikpawn.util.DialogUtil
import com.tss.quikpawn.util.Util
import kotlinx.android.synthetic.main.activity_reprint_order.*
import org.json.JSONObject
import java.lang.reflect.Type

class ReprintOrderActivity : BaseK9Activity(), OrderListAdapter.OnItemClickListener {
    lateinit var orderList: OrderListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reprint_order)
        title = getString(R.string.title_reprint)
        orderList = OrderListAdapter(this, this)
        recycler_view.setLayoutManager(LinearLayoutManager(this))
        recycler_view.setHasFixedSize(true)
        recycler_view.setAdapter(orderList)
        Network.getLoadOrder(object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                Log.e("panya", response.toString())
                val data = response.getJSONArray("data")
                val productListType: Type = object : TypeToken<ArrayList<OrderModel>?>() {}.type
                val orderArray: ArrayList<OrderModel> =
                    Gson().fromJson(data.toString(), productListType)
                orderList.updateData(orderArray)
                progress.visibility = View.GONE
                if (orderArray.isEmpty()) {
                    no_data.visibility = View.VISIBLE
                }
            }

            override fun onError(anError: ANError?) {
                progress.visibility = View.GONE
                no_data.visibility = View.VISIBLE
                Log.e("panya", "error code :" + anError?.errorCode + " body :" + anError?.errorBody)
            }
        })
    }


    override fun onItemClick(orderModel: OrderModel) {
        Log.e("panya", "orderModel :" + orderModel.order_code)
        val orderType = getOrderType(orderModel.type_id)

        when (orderType) {
            OrderType.SELL -> {
                manageSellOrder(orderModel)
            }
            OrderType.BUY -> {
                manageBuyOrder(orderModel)
            }
            OrderType.CONSIGNMENT -> {
                manageConsignOrder(orderModel)
            }
            OrderType.INTEREST -> {
                manageInterestOrder(orderModel)
            }
            OrderType.REDEEM ->  {
                manageRedeemOrder(orderModel)
            }
            OrderType.BORRROWED -> {
                manageBorrowOrder(orderModel)
            }
            OrderType.RETURN -> {
                manageReturnOrder(orderModel)
            }
            else -> {
                Toast.makeText(this@ReprintOrderActivity, "ไม่สามารถปริ้นได้", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    fun manageBuyOrder(orderModel: OrderModel) {
        printBuySlip(orderModel, false)
        val list = listOf(getString(R.string.dialog_msg_for_shop))
        val dialogParamModel = DialogParamModel("ปริ้น", list, getString(R.string.text_ok), getString(R.string.text_skip))
        DialogUtil.showConfirmDialog(dialogParamModel, this, DialogUtil.InputTextBackListerner {
            if (it.equals(DialogUtil.CONFIRM)) {
                printBuySlip(orderModel, true)
            }
        })
    }

    fun printBuySlip(data: OrderModel, printList: Boolean) {
        val textList = ArrayList<PrinterParams>()

        var bitmap = createImageBarcode(data.order_code, "QR Code")!!
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
        printerParams1.setText("ร้าน " + PreferencesManager.getInstance().companyName)
        textList.add(printerParams1)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("สาขา " + PreferencesManager.getInstance().companyBranchName)
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
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("รายการสินค้า\n")
        textList.add(printerParams1)

        val list = Util.productListToProductList2Cost(data.products)
        val listBitmap = Util.productListToBitmap(list)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(listBitmap)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
        printerParams1.setTextSize(24)
        printerParams1.setText("ชำระเงิน " + data.total + " บาท")
        textList.add(printerParams1)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        if (printList) {
            var i = 0
            for (product in data.products) {
                i++
                printerParams1 = PrinterParams()
                printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
                printerParams1.setTextSize(24)
                printerParams1.setText("" + i + ". " + product.product_name + "\n")
                textList.add(printerParams1)

                bitmap = Utility.toGrayscale(createImageBarcode(product.product_code, "QR Code")!!)
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
        }

        printdata(textList)
    }

    fun manageSellOrder(orderModel: OrderModel) {
        printSellSlip(orderModel)
        Handler().postDelayed({
            printSellCertificate(orderModel)
            val list = listOf(getString(R.string.dialog_msg_for_shop))
            val dialogParamModel = DialogParamModel("ปริ้น", list, getString(R.string.text_ok), getString(R.string.text_skip))
            DialogUtil.showConfirmDialog(dialogParamModel, this, DialogUtil.InputTextBackListerner {
                if (it.equals(DialogUtil.CONFIRM)) {
                    printSellSlip(orderModel)
                }
            })}, 2000)
    }

    fun printSellSlip(data: OrderModel) {
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
        printerParams1.setTextSize(25)
        printerParams1.isBold = true
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
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("รายการสินค้า\n")
        textList.add(printerParams1)

        val list = Util.productListToProductList2Sell(data.products)
        val listBitmap = Util.productListToBitmap(list)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(listBitmap)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
        printerParams1.setTextSize(24)
        printerParams1.setText("ชำระเงิน " + data.total + " บาท")
        textList.add(printerParams1)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        printdata(textList)
    }

    fun printSellCertificate(data: OrderModel) {
        val textList = ArrayList<PrinterParams>()
        for (product in data.products) {
            var bitmap = createImageBarcode(data.order_code, "Barcode")!!
            bitmap = Utility.toGrayscale(bitmap)

            var printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
            printerParams1.setTextSize(30)
            printerParams1.setText("ใบรับประกัน")
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
            printerParams1.setText("ร้าน " + PreferencesManager.getInstance().companyName)
            textList.add(printerParams1)
            printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(20)
            printerParams1.setText("สาขา " + PreferencesManager.getInstance().companyBranchName)
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

            val list = Util.productListToProductList2Certificate(product)
            val listBitmap = Util.productListToBitmap(list)
            printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
            printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
            printerParams1.setBitmap(listBitmap)
            textList.add(printerParams1)

            printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
            printerParams1.setTextSize(22)
            printerParams1.setText("\n\n\n\n___________________\n")
            textList.add(printerParams1)

            printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
            printerParams1.setTextSize(22)
            printerParams1.setText("ผู้ขาย")
            textList.add(printerParams1)

            printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(22)
            printerParams1.setText("\n\n\n\n")
            textList.add(printerParams1)

        }
        printdata(textList)
    }

    fun manageConsignOrder(orderModel: OrderModel) {
        printConsignSlip1(orderModel)
        Handler().postDelayed({
            printConsignSlip1(orderModel)
            printConsignSlip(orderModel, false)
        }, 3000)
        val list = listOf(getString(R.string.dialog_msg_for_shop))
        val dialogParamModel = DialogParamModel("ปริ้น", list, getString(R.string.text_ok), getString(R.string.text_skip))
        DialogUtil.showConfirmDialog(dialogParamModel, this, DialogUtil.InputTextBackListerner {
            if (it.equals(DialogUtil.CONFIRM)) {
                printConsignSlip(orderModel, true)
            }
        })
    }

    fun printConsignSlip1(data: OrderModel) {

        val textList = java.util.ArrayList<PrinterParams>()
        var printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("หนังสือขายฝาก\n")
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

        var sum = 0.00
        for (productModel in data.products) {
            sum += productModel.cost.toDouble()
        }
        val list = Util.productListToProductList2Cost(data.products)
        val listBitmap = Util.productListToBitmap(list)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(listBitmap)
        textList.add(printerParams1)

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

    fun printConsignSlip(data: OrderModel, printList: Boolean) {
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

        val list = Util.productListToProductList2Cost(data.products)
        val listBitmap = Util.productListToBitmap(list)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(listBitmap)
        textList.add(printerParams1)

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
        if (printList) {
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
        }
        printdata(textList)
    }

    fun manageInterestOrder(orderModel: OrderModel) {
        printInterestSlip(orderModel)
        val list = listOf(getString(R.string.dialog_msg_for_shop))
        val dialogParamModel = DialogParamModel("ปริ้น", list, getString(R.string.text_ok), getString(R.string.text_skip))
        DialogUtil.showConfirmDialog(dialogParamModel, this, DialogUtil.InputTextBackListerner {
            if (it.equals(DialogUtil.CONFIRM)) {
                printInterestSlip(orderModel)
            }
        })
    }

    fun printInterestSlip(data: OrderModel) {
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
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(18)
        printerParams1.setText("รายการ")
        textList.add(printerParams1)

        val list = arrayListOf<ProductModel2>()
        for (interest in data.interest) {
            list.add(ProductModel2("เดือนที่ : "+interest.month, interest.price+ " บาท"))
        }

        val listBitmap = Util.productListToBitmap(list)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(listBitmap)
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

    fun manageRedeemOrder(orderModel: OrderModel) {
        printRedeemSlip(orderModel)
        val list = listOf(getString(R.string.dialog_msg_for_shop))
        val dialogParamModel = DialogParamModel("ปริ้น", list, getString(R.string.text_ok), getString(R.string.text_skip))
        DialogUtil.showConfirmDialog(dialogParamModel, this, DialogUtil.InputTextBackListerner {
            if (it.equals(DialogUtil.CONFIRM)) {
                printRedeemSlip(orderModel)
            }
        })
    }

    fun printRedeemSlip(data: OrderModel) {
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
        printerParams1.setText("ร้าน " + PreferencesManager.getInstance().companyName)
        textList.add(printerParams1)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("สาขา " + PreferencesManager.getInstance().companyBranchName)
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
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(18)
        printerParams1.setText("รายการสินค้า")
        textList.add(printerParams1)

        val list = Util.productListToProductList2Sell(data.products)
        val listBitmap = Util.productListToBitmap(list)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(listBitmap)
        textList.add(printerParams1)

        val list2 = arrayListOf<ProductModel2>()
        if (data.interest.isNotEmpty()) {
            printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(18)
            printerParams1.setText("รายการดอกเบี้ย")
            textList.add(printerParams1)

            for (interest in data.interest) {
                list2.add(ProductModel2("เดือนที่ : " + interest.month, interest.price+" บาท"))
            }
            list2.add(ProductModel2("ค่าปรับ", data.mulct_price+" บาท"))
            val listBitmap = Util.productListToBitmap(list2)
            printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
            printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
            printerParams1.setBitmap(listBitmap)
            textList.add(printerParams1)
        } else {
            list2.add(ProductModel2("ค่าปรับ", data.mulct_price+" บาท"))
            val listBitmap = Util.productListToBitmap(list2)
            printerParams1 = PrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
            printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
            printerParams1.setBitmap(listBitmap)
            textList.add(printerParams1)
        }

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

    fun manageBorrowOrder(orderModel: OrderModel) {
        printBorrowSlip(orderModel)
        val list = listOf(getString(R.string.dialog_msg_for_shop))
        val dialogParamModel = DialogParamModel("ปริ้น", list, getString(R.string.text_ok), getString(R.string.text_skip))
        DialogUtil.showConfirmDialog(dialogParamModel, this, DialogUtil.InputTextBackListerner {
            if (it.equals(DialogUtil.CONFIRM)) {
                printBorrowSlip(orderModel)
            }
        })
    }

    fun printBorrowSlip(data: OrderModel) {
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

        val list = Util.productListToProductList2Sell(data.products)
        val listBitmap = Util.productListToBitmap(list)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(listBitmap)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(24)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        printdata(textList)
    }

    fun manageReturnOrder(orderModel: OrderModel) {
        printReturnSlip(orderModel)
        val list = listOf(getString(R.string.dialog_msg_for_shop))
        val dialogParamModel = DialogParamModel("ปริ้น", list, getString(R.string.text_ok), getString(R.string.text_skip))
        DialogUtil.showConfirmDialog(dialogParamModel, this, DialogUtil.InputTextBackListerner {
            if (it.equals(DialogUtil.CONFIRM)) {
                printReturnSlip(orderModel)
            }
        })
    }

    fun printReturnSlip(data: OrderModel) {
        val textList = ArrayList<PrinterParams>()

        var printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(30)
        printerParams1.setText("คืนสินค้า\n\n")
        textList.add(printerParams1)

        var bitmap = createImageBarcode(data.order_code, "Barcode")!!
        bitmap = Utility.toGrayscale(bitmap)

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
        printerParams1.setText("ร้าน " + PreferencesManager.getInstance().companyName)
        textList.add(printerParams1)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("สาขา " + PreferencesManager.getInstance().companyBranchName)
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
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("รายการสินค้า\n")
        textList.add(printerParams1)

        val list = Util.productListToProductList2Sell(data.products)
        val listBitmap = Util.productListToBitmap(list)
        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(listBitmap)
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        printdata(textList)
    }

}