package com.tss.quikpawn.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.stfalcon.multiimageview.MultiImageView
import com.tss.quikpawn.R
import com.tss.quikpawn.models.OrderModel
import com.tss.quikpawn.models.OrderStatus
import com.tss.quikpawn.models.OrderType
import com.tss.quikpawn.util.Util
import kotlinx.android.synthetic.main.item_interest_detail.*
import kotlinx.android.synthetic.main.item_order_list.view.*
import java.lang.StringBuilder

class OrderListAdapter(val context: Context, val listener: OnItemClickListener) : RecyclerView.Adapter<ViewHolder>() {

    val items = ArrayList<OrderModel>()

    fun updateData(item: ArrayList<OrderModel>) {
        items.addAll(item)
        notifyDataSetChanged()
    }
    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_order_list, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val orderModel = items.get(position)
        val orderType = OrderType.getOrderType(orderModel.type_id)
        holder.orderNumber?.text = orderModel.order_code
        holder.orderCreate?.text = Util.toDateFormat(orderModel.date_create)
        holder.orderType?.text = "รายการ "+orderType.typeName
//        holder.orderType?.setTextColor(orderStatus.color)
        holder.orderImage.shape = MultiImageView.Shape.RECTANGLE
        holder.orderUserCreate.text = "ผู้ทำรายการ "+orderModel.user_create
        holder.orderImage.rectCorners = 10
        holder.orderImage.setTag(orderModel.order_code)
        val orderName = StringBuilder()
        holder.orderImage.clear()
        for (product in orderModel.products) {
            Log.e("panya", "start Tag :"+ orderModel.order_code + ", size : "+orderModel.products.size)
            orderName.append(product.product_name +",")
                Glide.with(context) //1
                    .asBitmap()
                    .load(product.image_small)
                    .placeholder(R.drawable.ic_image_black_24dp)
                    .error(R.drawable.ic_broken_image_black_24dp)
                    .centerCrop()
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            Log.e("panya", "onResourceReady Tag :"+ holder.orderImage.getTag()+", isSame :"+ holder.orderImage.getTag().equals(orderModel.order_code))
                            if (holder.orderImage.getTag().equals(orderModel.order_code)) {
                                holder.orderImage.addImage(resource)
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }
                    })
        }
        if (orderName.isNotEmpty()) {
            holder.orderName.text = orderName.substring(0, orderName.lastIndex)
        }

        holder.orderContainer.tag = orderModel.order_code

        holder.orderContainer.setOnClickListener {
            listener.onItemClick(items.get(position))
        }
    }

    interface OnItemClickListener {
        fun onItemClick(orderModel: OrderModel)
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val orderNumber = view.order_number
    val orderType = view.order_type
    val orderCreate = view.txt_create
    val orderContainer = view.order_container
    val orderImage = view.img_order
    val orderName = view.txt_name
    val orderUserCreate = view.txt_user_create


}