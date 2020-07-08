package com.tss.quikpawn.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tss.quikpawn.R
import com.tss.quikpawn.models.ConsignmentProductModel
import com.tss.quikpawn.util.NumberTextWatcherForThousand.getDecimalFormattedString
import com.tss.quikpawn.util.Util
import kotlinx.android.synthetic.main.item_detail_consignment_view.view.*


class ConsignListAdapter(val context: Context, val listener: OnItemClickListener) :
    RecyclerView.Adapter<ConsignListAdapter.ConViewHolder>() {

    val items = ArrayList<ConsignmentProductModel>()

    fun updateImage(index: Int, refImg: String, url: String) {
        items.get(index).ref_image = refImg
        items.get(index).url = url
        notifyDataSetChanged()
    }

    fun newItem() {
        items.add(ConsignmentProductModel("", "5", "", "", "", "", ""))
        notifyDataSetChanged()
    }

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConViewHolder {
        return ConViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_detail_consignment_view, parent, false)
        )
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: ConViewHolder, position: Int) {
        val buyProductModel = items.get(position)
        holder.productImage.setImageResource(R.drawable.camera_big)
        holder.productDelete.visibility = View.VISIBLE

        holder.productDetail.setText(buyProductModel.detail)
        holder.productCost.setText(Util.addComma(buyProductModel.cost))
        holder.productName.setText(buyProductModel.name)

        holder.productDelete.setOnClickListener {
            if (items.contains(buyProductModel)) {
                items.remove(buyProductModel)
                notifyDataSetChanged()
            }
        }
        holder.productImage.setOnClickListener {
            listener.onTakePhotoClick(position)
            holder.productLoading.visibility = View.VISIBLE
        }


        holder.productLoading.visibility = View.GONE
        Glide.with(context) //1
            .asBitmap()
            .load(buyProductModel.url)
            .placeholder(R.drawable.ic_image_black_24dp)
            .error(R.drawable.ic_broken_image_black_24dp)
            .centerCrop()
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    holder.productLoading.visibility = View.GONE
                    holder.productImage.setImageBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }
            })
    }

    fun getConProductItems(): ArrayList<ConsignmentProductModel> {
        return items
    }

    interface OnItemClickListener {
        fun onTakePhotoClick(index: Int)
    }

    inner class ConViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each animal to
        val productDelete = view.delete_detail_btn
        val productImage = view.img_view1
        val productLoading = view.loading_photo_consignment
        val productName = view.edt_product_name
        val productDetail = view.edt_detail
        val productCost = view.edt_cost
//        val productPrice = view.edt_price

        init {
            Log.e("panya", "adapterPosition" + adapterPosition)
            productName.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {

                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    items.get(position).name = productName.text.toString()
                }
            })

            productDetail.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {

                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    items.get(position).detail = productDetail.text.toString()
                }
            })

            productCost.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editText: Editable?) {
                    try {
                        productCost.removeTextChangedListener(this)
                        val value: String = productCost.getText().toString()
                        if (value != null && value != "") {
                            if (value.startsWith(".")) {
                                productCost.setText("0.")
                            }
                            if (value.startsWith("0") && !value.startsWith("0.")) {
                                productCost.setText("")
                            }
                            val str: String =
                                productCost.getText().toString().replace(",", "")
                            if (value != "") productCost.setText(
                                getDecimalFormattedString(
                                    str
                                )
                            )
                            productCost.setSelection(productCost.getText().toString().length)
                        }
                        productCost.addTextChangedListener(this)
                        return
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        productCost.addTextChangedListener(this)
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    items.get(position).cost = productCost.text.toString()
                }
            })
        }
    }
}