package com.tss.quikpawn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_sell.setOnClickListener {
            startActivity(Intent(this@MainActivity, SearchIItemActivity1::class.java))
        }

        btn_buy.setOnClickListener {
            startActivity(Intent(this@MainActivity, SearchIItemActivity::class.java))
        }
    }
}
