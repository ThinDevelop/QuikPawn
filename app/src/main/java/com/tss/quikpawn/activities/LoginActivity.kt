package com.tss.quikpawn.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.tss.quikpawn.PreferencesManager
import com.tss.quikpawn.R
import com.tss.quikpawn.networks.Network
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        logout()
    }

    fun logout() {
        Network.logout(object :  JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                Log.e("panya", "onResponse : $response")
            }

            override fun onError(error: ANError) {
                Log.e("panya", "onError : " + error.message)
            }
        })
    }

    fun login(view: View) {
        loading.visibility = View.VISIBLE
        login_btn.isClickable = false
        user_name.isClickable = false
        password.isClickable = false
        Network.login(object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                loading.visibility = View.GONE
                login_btn.isClickable = true
                user_name.isClickable = true
                password.isClickable = true
                Log.e("panya", "onResponse : $response")
                val status = response.getString("status_code")
                if (status == "200") {
                    val data = response.getJSONObject("data")
                    val user = data.getJSONObject("user")
                    val id = user.getString("id")
                    val shop = data.getJSONObject("shop")
                    val companyId = shop.getString("company_id")
                    val companyBranchId = shop.getString("company_branch_id")
                    val companyBranchName = shop.getString("company_branch_name")
                    val companyName = shop.getString("company_name")
                    PreferencesManager.getInstance().token = response.getString("access_token")
                    PreferencesManager.getInstance().userId = id
                    PreferencesManager.getInstance().companyId = companyId
                    PreferencesManager.getInstance().companyBranchId = companyBranchId
                    PreferencesManager.getInstance().companyBranchName = companyBranchName
                    PreferencesManager.getInstance().companyName = companyName


                    startActivity(Intent(this@LoginActivity, MainMenuActivity::class.java))
                    this@LoginActivity.finish()
                }
            }

            override fun onError(error: ANError) {
                Log.e("panya", "onError : " + error.message)
                loading.visibility = View.GONE
                login_btn.isClickable = true
                user_name.isClickable = true
                password.isClickable = true
            }
        })
    }
}