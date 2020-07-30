package com.tss.quikpawn.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.centerm.smartpos.aidl.sys.AidlDeviceManager
import com.tss.quikpawn.*
import com.tss.quikpawn.networks.Network
import kotlinx.android.synthetic.main.activity_main_menu.*
import org.json.JSONObject

class MainMenuActivity : BaseActivity() {
    override fun onDeviceConnected(deviceManager: AidlDeviceManager?, capy: Boolean) {
    }

    override fun onPrintDeviceConnected(deviceManager: AidlDeviceManager?) {
    }

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
        btn_reprint.setOnClickListener {
            startActivity(Intent(this@MainMenuActivity, ReprintOrderActivity::class.java))
        }
        btn_getall.setOnClickListener {
            startActivity(Intent(this@MainMenuActivity, SelectItemActivity::class.java))
        }

        Network.getCategory(object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                Log.e("panya", response.toString())
            }

            override fun onError(error: ANError?) {
                error?.errorBody?.let {
                    val jObj = JSONObject(it)
                    if (jObj.has("status_code")) {
                        val status = jObj.getString("status_code")
                        showResponse(status, this@MainMenuActivity)
                    }
                }
                error?.let {
                    showResponse(error.errorCode.toString(), this@MainMenuActivity)
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
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
