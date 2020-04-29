package com.tss.quikpawn.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
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
import kotlinx.android.synthetic.main.item_interest_detail.*
import kotlinx.android.synthetic.main.item_order_list.view.*
import java.lang.StringBuilder

class OrderListAdapter(val items : ArrayList<OrderModel>, val context: Context, val listener: OnItemClickListener) : RecyclerView.Adapter<ViewHolder>() {

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
        val orderStatus = OrderStatus.getOrderStatus(orderModel.status_id)
        holder.orderNumber?.text = orderModel.order_code
        holder.orderCreate?.text = orderModel.date_expire
        holder.orderType?.text = orderStatus.statusName
        holder.orderType?.setTextColor(orderStatus.color)
        holder.orderImage.shape = MultiImageView.Shape.RECTANGLE
        holder.orderImage.rectCorners = 10
        val orderName = StringBuilder()
        for (product in orderModel.products) {
            orderName.append(product.product_name +",")
                Glide.with(context) //1
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
                            holder.orderImage.addImage(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }
                    })
        }


        holder.orderName.text = orderName.substring(0, orderName.lastIndex)
        holder.orderContainer.tag = orderModel.order_code

        holder.orderContainer.setOnClickListener {
            listener.onItemClick(it.tag as String)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(orderCode: String)
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
}