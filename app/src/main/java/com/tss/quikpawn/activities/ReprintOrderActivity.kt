package com.tss.quikpawn.activities

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.tss.quikpawn.BaseK9Activity
import com.tss.quikpawn.R
import com.tss.quikpawn.adapter.ImagesAdapter
import com.tss.quikpawn.adapter.OrderListAdapter
import kotlinx.android.synthetic.main.activity_form_detail.*
import kotlinx.android.synthetic.main.activity_reprint_order.*
import kotlinx.android.synthetic.main.activity_reprint_order.recycler_view

class ReprintOrderActivity: BaseK9Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reprint_order)

        title = getString(R.string.list_order)
//        val orderList = OrderListAdapter(this, photos)
        recycler_view.setLayoutManager(LinearLayoutManager(this))
        recycler_view.setHasFixedSize(true)
//        recycler_view.setAdapter(orderList)
    }
}