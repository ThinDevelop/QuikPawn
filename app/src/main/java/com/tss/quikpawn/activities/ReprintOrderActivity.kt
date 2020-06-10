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
import com.tss.quikpawn.models.*
import com.tss.quikpawn.models.OrderType.getOrderType
import com.tss.quikpawn.networks.Network
import com.tss.quikpawn.util.DialogUtil
import com.tss.quikpawn.util.Util
import kotlinx.android.synthetic.main.activity_reprint_order.*
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

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
                DialogUtil.showNotiDialog(
                    this@ReprintOrderActivity,
                    getString(R.string.title_error),
                    getString(R.string.connect_error)
                )
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
        printerParams1.setDataType(PrinterParams.DATATYPE.TEXT)
        printerParams1.setTextSize(20)
        printerParams1.setText("ร้าน " + PreferencesManager.getInstance().companyName)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
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

        val list = Util.productListToProductList2Cost(data.products)
        val listBitmap = Util.productListToBitmap(list)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(listBitmap)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
        printerParams1.setTextSize(24)
        printerParams1.setText("\nชำระเงิน " + data.total + " บาท")
        textList.add(printerParams1)
        textList.add(Util.dashSignature())
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        textList.add(Util.dashLine(this@ReprintOrderActivity))
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

                textList.add(Util.dashLine(this@ReprintOrderActivity))
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
        printerParams1.setTextSize(25)
        printerParams1.isBold = true
        printerParams1.setText("ร้าน "+ PreferencesManager.getInstance().companyName)
        textList.add(printerParams1)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(20)
        printerParams1.setText("สาขา "+ PreferencesManager.getInstance().companyBranchName)
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

        val list = Util.productListToProductList2Sell(data.products)
        val listBitmap = Util.productListToBitmap(list)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(listBitmap)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
        printerParams1.setTextSize(24)
        val total = data.total.replace(".00", "")
        printerParams1.setText("\nชำระเงิน " + Util.addComma(total) + " บาท")
        textList.add(printerParams1)
        textList.add(Util.dashSignature())
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        textList.add(Util.dashLine(this@ReprintOrderActivity))
        printdata(textList)
    }

    fun printSellCertificate(data: OrderModel) {
        val textList = ArrayList<PrinterParams>()
        for (product in data.products) {
            var bitmap = createImageBarcode(data.order_code, "Barcode")!!
            bitmap = Utility.toGrayscale(bitmap)

            val title = Util.textToBitmap("ใบรับประกัน")
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

            val list = Util.productListToProductList2Certificate(product)
            val listBitmap = Util.productListToBitmap(list)
            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
            printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
            printerParams1.setBitmap(listBitmap)
            textList.add(printerParams1)
            textList.add(Util.dashSignature())

            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(22)
            printerParams1.setText("\n\n\n")
            textList.add(printerParams1)
            textList.add(Util.dashLine(this@ReprintOrderActivity))
        }
        printdata(textList)
    }

    fun manageConsignOrder(orderModel: OrderModel) {
        printConsignSlip1(orderModel)
        Handler().postDelayed({
            printConsignSlip1(orderModel)
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

        val title = Util.textToBitmap("ใบขายฝาก")
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
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("เลขที่ "+data.order_code)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
        printerParams1.setTextSize(22)
        printerParams1.setText("วันที่ " + Util.toDateFormat(data.date_create))
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("ข้าพเจ้า"+data.customer_name+" \nบัตรประชาชน "+data.idcard)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
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
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(listBitmap)
        textList.add(printerParams1)
        val calendar = Util.stringToCalendar(data.date_expire)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\nมาขายฝากให้เป็นจำนวนเงิน \n"+  Util.addComma(sum.roundToInt()) +".00 บาท\nและได้รับเงินไปเสร็จแล้วแต่วันทำหนังสือนี้\nข้อ 2. ผู้ขายฝากยอมให้คิดดอกเบี้ย\nตามจำนวนเงินที่ขายฝากไว้\n" +
                " นับตั้งแต่วันทำหนังสือนี้เป็นต้นไป\n จนกว่าจะมาไถ่ถอนคืน\nในวันที่ "+ calendar.get(
            Calendar.DATE) +" เดือน "+ Util.getMonth(calendar) +" พ.ศ."+ (calendar.get(Calendar.YEAR)+543)+"\n" +
                "ข้อ 3. ผู้ขายฝากยืนยันว่าผู้ขายฝาก\nเป็นผู้มีกรรมสิทธิ์ในทรัพย์สินที่มา\nขายฝากแต่เพียงผู้เดียวและไม่มีคู่สมรสแต่อย่างใด\n" +
                "ข้อ 4. คู่กรณีได้อ่านหนีงสือนี้เข้าใจ\nรับว่าถูกต้องเป็นความจริงแล้วจึง\nลงลายมือชื่อไว้เป็นหลักฐาน\n ")
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
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n\n\n\n")
        textList.add(printerParams1)
        textList.add(Util.dashLine(this@ReprintOrderActivity))
        printdata(textList)

    }

    fun printConsignSlip(data: OrderModel, printList: Boolean) {
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
        val total = data.price.replace(".00", "")
        printerParams1.setText("ราคา " + Util.addComma(total) + " บาท")
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

        val list = Util.productListToProductList2Cost(data.products)
        val listBitmap = Util.productListToBitmap(list)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(listBitmap)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
        printerParams1.setTextSize(24)

        val sumPrice = data.price.replace(".00", "")
        printerParams1.setText("\nชำระเงิน " + Util.addComma(sumPrice) + " บาท")
        textList.add(printerParams1)
        textList.add(Util.dashSignature())
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        textList.add(Util.dashLine(this@ReprintOrderActivity))
        if (printList) {
            for (product in data.products) {

                printerParams1 = TssPrinterParams()
                printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
                printerParams1.setTextSize(24)
                printerParams1.setText("สินค้า : " + product.product_name + "\n")
                textList.add(printerParams1)

                bitmap = Utility.toGrayscale(createImageBarcode(product.product_code, "QR Code")!!)
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
                textList.add(Util.dashLine(this@ReprintOrderActivity))
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
        printerParams1.setText("รายการ")
        textList.add(printerParams1)

        val list = arrayListOf<ProductModel2>()
        for (interest in data.interest) {
            list.add(ProductModel2("เดือนที่ : "+interest.month, interest.price+ " บาท"))
        }

        val listBitmap = Util.productListToBitmap(list)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(listBitmap)
        textList.add(printerParams1)

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
        printerParams1.setTextSize(24)
        val total = data.total.replace(".00", "")
        printerParams1.setText("ยอดชำระ " + Util.addComma(total) + " บาท")
        textList.add(printerParams1)
        textList.add(Util.dashSignature())
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        textList.add(Util.dashLine(this@ReprintOrderActivity))
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

        val list = Util.productListToProductList2Sell(data.products)
        val listBitmap = Util.productListToBitmap(list)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(listBitmap)
        textList.add(printerParams1)

        val list2 = arrayListOf<ProductModel2>()
        if (data.interest.isNotEmpty()) {
            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
            printerParams1.setTextSize(18)
            printerParams1.setText("รายการดอกเบี้ย")
            textList.add(printerParams1)

            for (interest in data.interest) {
                list2.add(ProductModel2("เดือนที่ : " + interest.month, interest.price+" บาท"))
            }
            list2.add(ProductModel2("ค่าปรับ", data.mulct_price+" บาท"))
            val listBitmap = Util.productListToBitmap(list2)
            printerParams1 = TssPrinterParams()
            printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
            printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
            printerParams1.setBitmap(listBitmap)
            textList.add(printerParams1)
        } else {
            list2.add(ProductModel2("ค่าปรับ", data.mulct_price+" บาท"))
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
        val total = data.total.replace(".00", "")
        printerParams1.setText("ยอดชำระ " + Util.addComma(total) + " บาท")
        textList.add(printerParams1)
        textList.add(Util.dashSignature())
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        textList.add(Util.dashLine(this@ReprintOrderActivity))
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
        printerParams1.setText("รายการสินค้า\n")
        textList.add(printerParams1)

        val list = Util.productListToProductList2Sell(data.products)
        val listBitmap = Util.productListToBitmap(list)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(listBitmap)
        textList.add(printerParams1)

        textList.add(Util.dashSignature())
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(24)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        textList.add(Util.dashLine(this@ReprintOrderActivity))
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

        val title = Util.textToBitmap("คืนสินค้า")
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

        var bitmap = createImageBarcode(data.order_code, "Barcode")!!
        bitmap = Utility.toGrayscale(bitmap)

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

        val list = Util.productListToProductList2Sell(data.products)
        val listBitmap = Util.productListToBitmap(list)
        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setDataType(PrinterParams.DATATYPE.IMAGE)
        printerParams1.setBitmap(listBitmap)
        textList.add(printerParams1)

        textList.add(Util.dashSignature())

        printerParams1 = TssPrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n")
        textList.add(printerParams1)
        textList.add(Util.dashLine(this@ReprintOrderActivity))
        printdata(textList)
    }

}