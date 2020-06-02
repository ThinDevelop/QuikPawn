package com.tss.quikpawn.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tss.quikpawn.R
import com.tss.quikpawn.adapter.OrderListAdapter
import com.tss.quikpawn.models.OrderModel
import kotlinx.android.synthetic.main.activity_order_list.*
import java.lang.reflect.Type

class OrderListActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView  (R.layout.activity_order_list)
        title = getString(R.string.select_order)
        val orders = intent.getStringExtra("order_list")
        val orderListType: Type = object : TypeToken<ArrayList<OrderModel>?>() {}.type
        val orderArray: ArrayList<OrderModel> = Gson().fromJson(orders, orderListType)
        val orderListAdapter = OrderListAdapter(this, object :
            OrderListAdapter.OnItemClickListener {
            override fun onItemClick(orderModel: OrderModel) {
                val intent = Intent()
                intent.putExtra("order_code", orderModel.order_code)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        })
        rv_order_list.layoutManager = LinearLayoutManager(this)
        rv_order_list.adapter = orderListAdapter
        orderListAdapter.updateData(orderArray)
    }
}