package com.tss.quikpawn.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.tss.quikpawn.*
import com.tss.quikpawn.networks.Network
import kotlinx.android.synthetic.main.activity_main_menu.*
import org.json.JSONObject

class MainMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        btn_buy.setOnClickListener {
            startActivity(Intent(this@MainMenuActivity, BuyActivity::class.java))
        }
        btn_sell.setOnClickListener {
            startActivity(Intent(this@MainMenuActivity, SellActivity::class.java))
        }
        btn_consignment.setOnClickListener {
            startActivity(Intent(this@MainMenuActivity, ConsignmentActivity::class.java))
        }
        btn_interest.setOnClickListener {
            startActivity(Intent(this@MainMenuActivity, InterestActivity::class.java))
        }
        btn_getback.setOnClickListener {
            startActivity(Intent(this@MainMenuActivity, RedeemActivity::class.java))
        }
        btn_borrow.setOnClickListener {
            startActivity(Intent(this@MainMenuActivity, BorrowActivity::class.java))
        }
        btn_return.setOnClickListener {
            startActivity(Intent(this@MainMenuActivity, ProductListActivity::class.java))
        }

        Network.getCategory(object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                Log.e("panya", response.toString())
            }

            override fun onError(anError: ANError?) {
                Log.e("panya", "error code :" + anError?.errorCode + " body :" + anError?.errorBody)
                anError?.let {
                    if (anError.errorCode == 401) {
                        startActivity(Intent(this@MainMenuActivity, LoginActivity::class.java))
                        this@MainMenuActivity.finish()
                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                startActivity(Intent(this@MainMenuActivity, LoginActivity::class.java))
                this@MainMenuActivity.finish()
                return true
            }
            R.id.reprint_by_order-> {
                startActivity(Intent(this@MainMenuActivity, LoginActivity::class.java))
                this@MainMenuActivity.finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
