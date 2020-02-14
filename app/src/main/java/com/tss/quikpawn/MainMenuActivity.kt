package com.tss.quikpawn

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        buy_btn.setOnClickListener {
            startActivity(Intent(this@MainMenuActivity, BuyActivity::class.java))
        }
        sell_btn.setOnClickListener {
            startActivity(Intent(this@MainMenuActivity, SellActivity::class.java))
        }
        consignment_btn.setOnClickListener {
            startActivity(Intent(this@MainMenuActivity, ConsignmentActivity::class.java))
        }
        order_btn.setOnClickListener {
            startActivity(Intent(this@MainMenuActivity, MainActivity::class.java))
        }
    }
}
