package com.tss.quikpawn.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import com.google.zxing.Result
import com.tss.quikpawn.R
import com.tss.quikpawn.models.DialogParamModel
import com.tss.quikpawn.models.ProductModel
import com.tss.quikpawn.models.ProductStatus
import com.tss.quikpawn.models.SellProductModel
import com.tss.quikpawn.networks.Network
import com.tss.quikpawn.util.DialogUtil
import com.tss.quikpawn.util.Util
import kotlinx.android.synthetic.main.activity_select_item.*
import kotlinx.android.synthetic.main.activity_sell.*
import kotlinx.android.synthetic.main.activity_sell.item_container
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.json.JSONObject
import java.util.*

class SelectItemActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {


    var productList = mutableListOf<SellProductModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_item)
    }

    public override fun onResume() {
        super.onResume()
        scanner!!.setResultHandler(this) // Register ourselves as a handler for scan results.
        scanner!!.startCamera()          // Start camera on resume
    }

    public override fun onPause() {
        super.onPause()
        scanner!!.stopCamera()           // Stop camera on pause
    }

    override fun handleResult(rawResult: Result) {
        val code = rawResult.text
        loadItem(code)
    }

    fun createProgressDialog(context: Context, title: String): AlertDialog {
        val view = layoutInflater.inflate(R.layout.layout_loading_dialog, null)
        val text = view.findViewById<TextView>(R.id.txt_load)
        text.text = title
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false) // if you want user to wait for some process to finish,
        builder.setView(view)
        return builder.create()
    }

    fun loadItem(key: String) {
        val dialog = createProgressDialog(this, "Loading...")
        dialog.show()
        Network.searchProductByCode(key, object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                dialog.dismiss()
                Log.e("panya", "onResponse : $response")
                val status = response.getString("status_code")
                if (status == "200") {
                    val dataJsonObj = response.getJSONObject("data")
                    val data = Gson().fromJson(dataJsonObj.toString(), ProductModel::class.java)
                    val product = SellProductModel(key, 0)//Integer.parseInt(data.sale.replace(".00", "")))
                    productList.add(product)
                    if (data.status_id.equals(ProductStatus.READY_FOR_SALE.statusId)) {
                        addItemView(data)
                    } else {
                        DialogUtil.showNotiDialog(this@SelectItemActivity, "สินค้าไม่พร้อมขาย", "")
                    }
                } else {
                    showResponse(status, this@SelectItemActivity)
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
                showResponse(status, this@SelectItemActivity)
                error.printStackTrace()
                Log.e(
                    "panya",
                    "onError : " + error.errorCode + ", detail " + error.errorDetail + ", errorBody" + error.errorBody
                )
            }
        })
    }

    fun addItemView(productModel: ProductModel) {
        val inflater = LayoutInflater.from(baseContext)
        val contentView: View = inflater.inflate(R.layout.item_cardview_sell, null, false)
        val delete = contentView.findViewById<ImageView>(R.id.btn_delete)
        val image = contentView.findViewById<ImageView>(R.id.img_item)
        val txtId = contentView.findViewById<TextView>(R.id.txt_item_id)
        val txtDetail = contentView.findViewById<TextView>(R.id.txt_detail)
        val txtCost = contentView.findViewById<TextView>(R.id.txt_cost)
        val txtsell = contentView.findViewById<TextView>(R.id.txt_sell)
        txtsell.visibility = View.VISIBLE
        txtId.text = productModel.product_name
        txtDetail.text = productModel.detail
        txtCost.text = Util.addComma(productModel.cost) + "บาท"
        txtsell.text = "0 บาท"//Util.addComma(productModel.sale) + "0 บาท"
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
            (item_container.findViewWithTag<View>(it.tag).parent as ViewManager).removeView(
                item_container.findViewWithTag<View>(it.tag)
            )
        }
        Glide.with(this) //1
            .load(productModel.image_small)
            .placeholder(R.drawable.ic_image_black_24dp)
            .error(R.drawable.ic_broken_image_black_24dp)
            .skipMemoryCache(true) //2
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.NONE) //3
            .into(image)

        item_container.addView(contentView)
    }


    fun showResponse(status: String, context: Context?) {
        if (status == "401") {
            val msg = ArrayList<String>()
            msg.add("การยืนยันตัวตนล้มเหลว")
            val param = DialogParamModel(
                "ปัญหายืนยันตัวตน", msg,
                getString(R.string.text_ok), ""
            )
            DialogUtil.showConfirmDialog(
                param, context
            ) { result ->
                if (DialogUtil.CONFIRM == result) {
                    finish()
                    val intent = Intent(context, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
        } else if (status == "201") {
            val msg = ArrayList<String>()
            msg.add("กรุณาเพิ่มอุปกรณ์ในระบบ")
            val param = DialogParamModel(
                "ไม่พบอุปกรณ์ในระบบ", msg,
                getString(R.string.text_ok), ""
            )
            DialogUtil.showConfirmDialog(
                param, context
            ) { result ->
                if (DialogUtil.CONFIRM == result) {
                    finish()
                    val intent = Intent(context, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
        } else if (status == "202") {
            val msg = ArrayList<String>()
            msg.add("แพ็คเกจหมดอายุ")
            val param = DialogParamModel(
                "กรุณาติดต่อผู้ดูแลระบบ", msg,
                getString(R.string.text_ok), ""
            )
            DialogUtil.showConfirmDialog(
                param, context
            ) { result ->
                if (DialogUtil.CONFIRM == result) {
                    finish()
                    val intent = Intent(context, LoginActivity::class.java)
                    startActivity(intent)
                }
            }
        } else {
            DialogUtil.showNotiDialog(
                context,
                getString(R.string.connect_error),
                getString(R.string.connect_error_please_reorder)
            )
        }
    }
}