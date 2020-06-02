package com.tss.quikpawn.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewManager
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
import com.google.gson.Gson
import com.tss.quikpawn.BaseK9Activity
import com.tss.quikpawn.R
import com.tss.quikpawn.ScanActivity
import com.tss.quikpawn.SellActivity
import com.tss.quikpawn.models.*
import com.tss.quikpawn.networks.Network
import com.tss.quikpawn.util.DialogUtil
import kotlinx.android.synthetic.main.activity_product_list.*
import kotlinx.android.synthetic.main.item_search.*
import org.json.JSONObject

class ProductListActivity: BaseK9Activity() {
    var orderCode: String? = ""
    var productSellList = mutableListOf<ProductModel>()
    val PAY_REQUEST_CODE = 2001
    val RETURN_REQUEST_CODE = 2002
    val SELECT_ORDER_REQUEST_CODE = 2015
    var orderList = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)
        title = getString(R.string.return_item)

        scan.setOnClickListener {
            val intent = Intent(this@ProductListActivity, ScanActivity::class.java)
            startActivityForResult(intent, SCAN_REQUEST_CODE)
        }

        btn_pay.setOnClickListener {
            val list = getProductModel(ProductStatus.BORRROWED.statusId)
            if (list.isEmpty()) {
                Toast.makeText(this@ProductListActivity, "ไม่พบสินค้าที่ชำระเงินได้", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this@ProductListActivity, SellActivity::class.java)
                intent.putExtra("product_list", Gson().toJson(list))
                intent.putExtra("order_code", orderCode)
                startActivityForResult(intent, PAY_REQUEST_CODE)
            }
        }

        btn_return.setOnClickListener {
            val list = getProductModel(ProductStatus.BORRROWED.statusId)
            if (list.isEmpty()) {
                Toast.makeText(this@ProductListActivity, "ไม่พบสินค้าที่คืนได้", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this@ProductListActivity, ReturnActivity::class.java)
                intent.putExtra("product_list", Gson().toJson(list))
                intent.putExtra("order_code", orderCode)
                startActivityForResult(intent, RETURN_REQUEST_CODE)
            }
        }
        edt_search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (!query.equals("")) {
                        if (checkContains(query)) {
                            Toast.makeText(
                                this@ProductListActivity,
                                "รายการนี้มีอยู่แล้ว",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            loadOrder(query)
                        }
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        initialK9Fast()
    }

    fun checkContains(id: String): Boolean {
        return orderList.contains(id)
    }

    fun getProductModel(statusId: String): MutableList<ProductModel> {
        val productList = mutableListOf<ProductModel>()
        if (productSellList.isNotEmpty()) {
            for (product in productSellList) {
                if (statusId.equals(product.status_id)) {
                    productList.add(product)
                }
            }
        }
        return productList
    }

    override fun setupViewFast(info: ThaiIDSecurityBeen) {
        super.setupViewFast(info)
        orderCode?.let {
            if (it.isNotEmpty()) {
                loadOrder(citizenId)
            }
        }
    }

    fun checkActionContainer() {
        btn_container?.visibility = View.GONE
        if (productSellList.isNotEmpty()) {
            for (product in productSellList) {
                if (ProductStatus.BORRROWED.statusId.equals(product.status_id)) {
                    btn_container?.visibility = View.VISIBLE
                    break
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            orderList = ArrayList<String>()
            if (requestCode == SCAN_REQUEST_CODE) {
                orderCode = data?.getStringExtra("barcode")
                loadOrder(orderCode!!)
            } else if (requestCode == SELECT_ORDER_REQUEST_CODE) {
                orderCode = data?.getStringExtra("order_code")
                loadOrder(orderCode!!)
            } else if (requestCode == RETURN_REQUEST_CODE) {
                orderCode = data?.getStringExtra("order_code")
                loadOrder(orderCode!!)
            } else if (requestCode == PAY_REQUEST_CODE) {
                orderCode = data?.getStringExtra("order_code")
                loadOrder(orderCode!!)
            }

        }
    }

    fun loadOrder(key: String) {
        if (key.length == 13) {
            Network.searchOrderByIdCardAndType(key, OrderType.BORRROWED.typeId, object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e("panya", "onResponse : $response")
                    val status = response.getString("status_code")
                    if (status == "200") {
                        val dataJsonArray = response.getJSONArray("data")
                        val intent = Intent(this@ProductListActivity, OrderListActivity::class.java)
                        intent.putExtra("order_list", dataJsonArray.toString())
                        startActivityForResult(intent, SELECT_ORDER_REQUEST_CODE)
                    } else {
                        DialogUtil.showNotiDialog(
                            this@ProductListActivity,
                            getString(R.string.title_error),
                            getString(R.string.search_error_please_research)
                        )
                    }
                }

                override fun onError(error: ANError) {
                    error.printStackTrace()
                    DialogUtil.showNotiDialog(
                        this@ProductListActivity,
                        getString(R.string.title_error),
                        getString(R.string.connect_error_please_reorder)
                    )
                    Log.e("panya", "onError : " + error.errorCode +", detail "+error.errorDetail+", errorBody"+ error.errorBody)
                }
            } )
        } else if (checkContains(key)) {
            Toast.makeText(
                this@ProductListActivity,
                "รายการนี้มีอยู่แล้ว",
                Toast.LENGTH_LONG
            ).show()
        } else {
            btn_container?.visibility = View.GONE
            productSellList.clear()
            item_container.removeAllViews()
            Network.searchOrder(key, OrderType.BORRROWED.typeId, object :
                JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.e("panya", "onResponse : $response")
                    val status = response.getString("status_code")
                    if (status == "200") {
                        val dataJsonObj = response.getJSONObject("data")
                        orderCode = dataJsonObj.getString("order_code")
                        val orderModel =
                            Gson().fromJson(dataJsonObj.toString(), OrderModel::class.java)
                        addItemView(orderModel!!)
                        checkActionContainer()
                    }else {
                        DialogUtil.showNotiDialog(
                            this@ProductListActivity,
                            getString(R.string.title_error),
                            getString(R.string.search_error_please_research)
                        )
                    }
                }

                override fun onError(error: ANError) {
                    error.printStackTrace()
                    DialogUtil.showNotiDialog(
                        this@ProductListActivity,
                        getString(R.string.title_error),
                        getString(R.string.connect_error_please_reorder)
                    )
                    Log.e(
                        "panya",
                        "onError : " + error.errorCode + ", detail " + error.errorDetail + ", errorBody" + error.errorBody
                    )
                }
            })
        }
    }

    fun addItemView(orderModel: OrderModel) {
        orderList.add(orderModel.order_code)
        for (productModel in orderModel.products) {
            productSellList.add(productModel)
            val inflater = LayoutInflater.from(baseContext)
            val contentView: View = inflater.inflate(R.layout.item_card, null, false)
            val txtStatus = contentView.findViewById<TextView>(R.id.txt_status)
            val image = contentView.findViewById<ImageView>(R.id.img_item)
            val txtId = contentView.findViewById<TextView>(R.id.txt_item_id)
            val txtDetail = contentView.findViewById<TextView>(R.id.txt_detail)
            val txtCost = contentView.findViewById<TextView>(R.id.txt_cost)
            txtStatus.text = getString(R.string.text_status, productModel.status_name)
            val productStatus = ProductStatus.getProductStatus(productModel.status_id)
            txtStatus.setTextColor(productStatus.color)
            txtId.text = productModel.product_id
            txtDetail.text = productModel.detail
            txtCost.text = productModel.cost

            contentView.tag = productModel.product_code
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
    }
}