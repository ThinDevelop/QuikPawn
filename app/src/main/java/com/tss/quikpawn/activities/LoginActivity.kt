package com.tss.quikpawn.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.centerm.smartpos.aidl.sys.AidlDeviceManager
import com.tss.quikpawn.*
import com.tss.quikpawn.models.DialogParamModel
import com.tss.quikpawn.networks.Network
import com.tss.quikpawn.util.DialogUtil
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.io.File
import java.util.*


class LoginActivity: BaseActivity() {

    val PERMISSIONS_REQUEST_CODE = 103

    override fun onDeviceConnected(deviceManager: AidlDeviceManager?, capy: Boolean) {
    }

    override fun onPrintDeviceConnected(deviceManager: AidlDeviceManager?) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        logout()
        setupPermissions()
        Log.e("panya", "SERIAL : "+ android.os.Build.SERIAL)
        version.text = "version "+BuildConfig.VERSION_NAME
    }

    private fun setupPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.CAMERA)
        val readPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        val writePermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (cameraPermission != PackageManager.PERMISSION_GRANTED ||
            readPermission != PackageManager.PERMISSION_GRANTED ||
            writePermission != PackageManager.PERMISSION_GRANTED ) {
            Log.i("CAMERA_REQUEST_CODE", "Permission to record denied")
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSIONS_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("CAMERA_REQUEST_CODE", "Permission has been denied by user")
                    this.finish()
                } else {
                    Log.i("CAMERA_REQUEST_CODE", "Permission has been granted by user")
                }
            }
        }
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

    fun testPrint() {
        val outputFile = File(
            Environment.getExternalStorageDirectory().absolutePath,
            "insurancePDF.pdf"
        )
        val uri: Uri = Uri.fromFile(outputFile)

//        val printIntent = Intent(this, PrintDialogActivity::class.java)
//        printIntent.setDataAndType(uri, "application/pdf")
//        printIntent.putExtra("title", "insurancePDF")
//        startActivity(printIntent)



        val share = Intent()
        share.action = Intent.ACTION_SEND
//        share.type = "text/plain"
        share.type = "application/pdf"
        share.putExtra(Intent.EXTRA_STREAM, uri)
//        share.setPackage("com.tss.quikpawn")
        startActivity(share)
    }

    fun login(view: View) {
//        testPrint()
        if (user_name.text.toString().isEmpty() || password.text.toString().isEmpty()) {
            Toast.makeText(this@LoginActivity, "กรุณาระบุ username และ password", Toast.LENGTH_SHORT).show()
            return
        }
        loading.visibility = View.VISIBLE
        login_btn.isClickable = false
        user_name.isClickable = false
        password.isClickable = false
        Network.login(user_name.text.toString(), password.text.toString(),  object : JSONObjectRequestListener {
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
                    val tid = data.getString("tid")
                    val contact = shop.getString("contact")
                    val contactPhone = shop.getString("contact_phone")
                    val contactEmail = shop.getString("contact_email")
                    val shopTaxId = shop.getString("tax_id_number")
                    val shopNumber = shop.getString("number")
                    val shopBuilding = shop.getString("building")
                    val shopAlley = shop.getString("alley")
                    val shopRoad = shop.getString("road")
                    val shopDistrict = shop.getString("district")
                    val shopAmphure = shop.getString("amphure")
                    val shopProvince = shop.getString("province")
                    val shopTypeCode = shop.getString("type_code")
                    val shopTypeName = shop.getString("type_name")
                    val shopZipCode = shop.getString("zipcode")
                    val paperSize = shop.getString("paper_size")

                    PreferencesManager.getInstance().token = response.getString("access_token")
                    PreferencesManager.getInstance().userId = id
                    PreferencesManager.getInstance().companyId = companyId
                    PreferencesManager.getInstance().companyBranchId = companyBranchId
                    PreferencesManager.getInstance().companyBranchName = companyBranchName
                    PreferencesManager.getInstance().companyName = companyName
                    PreferencesManager.getInstance().tid = tid
                    PreferencesManager.getInstance().contact = contact
                    PreferencesManager.getInstance().contactPhone = contactPhone
                    PreferencesManager.getInstance().contactEmail = contactEmail

                    PreferencesManager.getInstance().alley = shopAlley
                    PreferencesManager.getInstance().taxId = shopTaxId
                    PreferencesManager.getInstance().number = shopNumber
                    PreferencesManager.getInstance().building = shopBuilding
                    PreferencesManager.getInstance().road = shopRoad
                    PreferencesManager.getInstance().district = shopDistrict
                    PreferencesManager.getInstance().amphure = shopAmphure
                    PreferencesManager.getInstance().province = shopProvince
                    PreferencesManager.getInstance().typeCode = shopTypeCode
                    PreferencesManager.getInstance().typeName = shopTypeName
                    PreferencesManager.getInstance().zipCode = shopZipCode
                    PreferencesManager.getInstance().paperSize = paperSize

                    startActivity(Intent(this@LoginActivity, MainMenuActivity::class.java))
                    this@LoginActivity.finish()
                } else {
                    val msg = response.getString("message")
                    Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                    showResponse(status, this@LoginActivity)
                }
            }

            override fun onError(error: ANError) {
                var status = error.errorCode.toString()
                error.errorBody?.let {
                    val jObj = JSONObject(it)
                    if (jObj.has("status_code")) {
                        status = jObj.getString("status_code")
                    }
                }
                showResponse(status, this@LoginActivity)

                loading.visibility = View.GONE
                login_btn.isClickable = true
                user_name.isClickable = true
                password.isClickable = true
            }
        })
    }

    override fun showResponse(status: String, context: Context) {
        if (status == "401") {
            val msg = ArrayList<String>()
            msg.add("การยืนยันตัวตนล้มเหลว")
            val param = DialogParamModel(
                "ปัญหายืนยันตัวตน", msg,
                getString(R.string.text_ok), ""
            )
            DialogUtil.showConfirmDialog(
                param, this@LoginActivity
            ) { result ->
                if (DialogUtil.CONFIRM == result) {

                }
            }
        } else if (status == "201") {
            val msg = ArrayList<String>()
            msg.add("กรุณาเพิ่มอุปกรณ์ในระบบ")
            val param = DialogParamModel(
                "ไม่พบอุปกรณ์ในระบบ", msg,
                getString(R.string.text_ok), ""
            )
            DialogUtil.showConfirmDialog(
                param, this@LoginActivity
            ) { result ->
                if (DialogUtil.CONFIRM == result) {

                }
            }
        } else if (status == "202") {
            val msg = ArrayList<String>()
            msg.add("แพ็คเกจหมดอายุ")
            val param = DialogParamModel(
                "กรุณาติดต่อผู้ดูแลระบบ", msg,
                getString(R.string.text_ok), ""
            )
            DialogUtil.showConfirmDialog(
                param, this@LoginActivity
            ) { result ->
                if (DialogUtil.CONFIRM == result) {

                }
            }
        } else {
            DialogUtil.showNotiDialog(
                this@LoginActivity,
                getString(R.string.connect_error),
                getString(R.string.connect_error_please_reorder)
            )
        }
    }
}