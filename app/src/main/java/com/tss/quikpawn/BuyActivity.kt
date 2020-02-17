package com.tss.quikpawn

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_buy.*


class BuyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy)
        val code = intent.getStringExtra("barcode")
        //clear sign button
        clearsign_btn.setOnClickListener{
            signature_pad.clear()
        }
        //get bitmap signature
        ok_btn.setOnClickListener {
            val Bitmap = signature_pad.getSignatureBitmap()
        }
        barcode_btn!!.setOnClickListener {
            val intent = Intent(this@BuyActivity, ScanActivity::class.java)
            startActivity(intent)
        }
        if(code != null){
            order_number!!.setText(code)
        }
    }
}
