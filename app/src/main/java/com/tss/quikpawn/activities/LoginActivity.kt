package com.tss.quikpawn.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.centerm.smartpos.aidl.sys.AidlDeviceManager
import com.centerm.smartpos.aidl.sys.AidlSystemSettingService
import com.centerm.smartpos.aidl.sys.IPackageInstallListener
import com.centerm.smartpos.constant.Constant
import com.tss.quikpawn.BaseActivity
import com.tss.quikpawn.BuildConfig
import com.tss.quikpawn.PreferencesManager
import com.tss.quikpawn.R
import com.tss.quikpawn.models.DialogParamModel
import com.tss.quikpawn.networks.Network
import com.tss.quikpawn.util.DialogUtil
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject
import java.io.*
import java.util.*


class LoginActivity : BaseActivity() {
    var sysSetting: AidlSystemSettingService? = null
    val PERMISSIONS_REQUEST_CODE = 103

    override fun onDeviceConnected(deviceManager: AidlDeviceManager?, capy: Boolean) {
        try {
            sysSetting = AidlSystemSettingService.Stub
                .asInterface(deviceManager?.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_SYS))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPrintDeviceConnected(deviceManager: AidlDeviceManager?) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        logout()
        setupPermissions()
        loadApk()
        Log.e("panya", "SERIAL : " + android.os.Build.SERIAL)
        version.text = "version " + BuildConfig.VERSION_NAME
    }

    private fun setupPermissions() {
        val cameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )
        val readPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val writePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (cameraPermission != PackageManager.PERMISSION_GRANTED ||
            readPermission != PackageManager.PERMISSION_GRANTED ||
            writePermission != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("CAMERA_REQUEST_CODE", "Permission to record denied")
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            PERMISSIONS_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
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
        Network.logout(object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                Log.e("panya", "onResponse : $response")
            }

            override fun onError(error: ANError) {
                Log.e("panya", "onError : " + error.message)
            }
        })
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_login, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.system5 -> {
                installAppSystemV5()
                return true
            }
            R.id.xprinter -> {
                installAppXprinter()
                return true
            }
            R.id.brother -> {
                installAppBrother()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun loadApk() {

        val file = File(
            Environment.getExternalStorageDirectory()
                .toString() + "/brother_print_service_2020-09-09_sign.apk"
        )
        if (!file.exists()) {
            try {
                // 把准备安装的APK文件写入sd卡中等待安装
                val ins = assets.open("brother_print_service_2020-09-09_sign.apk")
                file.createNewFile()
                file.setReadable(true, false)
                val ous: OutputStream = FileOutputStream(file) // 打开文件输出流
                val buffer = ByteArray(4 * 1024)
                while (ins.read(buffer) != -1) {
                    ous.write(buffer)
                }
                ous.flush()
                ous.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val fileSystem = File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/PrintSpoolerNew_2020-09-09_sign.apk"
            )

            if (!fileSystem.exists()) {

                try {
                    // 把准备安装的APK文件写入sd卡中等待安装
                    val ins = assets.open("PrintSpoolerNew_2020-09-09_sign.apk")

                    fileSystem.createNewFile()
                    fileSystem.setReadable(true, false)
                    val ous: OutputStream = FileOutputStream(fileSystem) // 打开文件输出流
                    val buffer = ByteArray(4 * 1024)
                    while (ins.read(buffer) != -1) {
                        ous.write(buffer)
                    }
                    ous.flush()
                    ous.close()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            val fileXprinter = File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/xprinter_driver_service_2020-09-09_sign.apk"
            )
            if (!fileXprinter.exists()) {
                try {
                    // 把准备安装的APK文件写入sd卡中等待安装
                    val ins = assets.open("xprinter_driver_service_2020-09-09_sign.apk")

                    fileXprinter.createNewFile()
                    fileXprinter.setReadable(true, false)
                    val ous: OutputStream = FileOutputStream(fileXprinter) // 打开文件输出流
                    val buffer = ByteArray(4 * 1024)
                    while (ins.read(buffer) != -1) {
                        ous.write(buffer)
                    }
                    ous.flush()
                    ous.close()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun installAppXprinter() {
        try {
            Toast.makeText(baseContext, "Installing", Toast.LENGTH_LONG).show()
            this.sysSetting?.installApkBack(Environment.getExternalStorageDirectory().absolutePath + "/xprinter_driver_service_2020-09-09_sign.apk",
                object : IPackageInstallListener.Stub() {
                    override fun onInstallFinished() {
                        showMessage(getString(R.string.sys_install_success))
                    }

                    override fun onInstallError(p0: Int) {
                        showMessage(getString(R.string.sys_install_failed)+" :"+p0)
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace();
            showMessage(
                getString(R.string.sys_install_back_exception),
                e.getLocalizedMessage(),
                Color.RED
            );
        }
    }

    fun installAppSystemV5() {
        try {
            Toast.makeText(baseContext, "Installing", Toast.LENGTH_LONG).show()
            this.sysSetting?.installApkBack(Environment.getExternalStorageDirectory().absolutePath + "/PrintSpoolerNew_2020-09-09_sign.apk",
                object : IPackageInstallListener.Stub() {
                    override fun onInstallFinished() {
                        showMessage(getString(R.string.sys_install_success))
                    }

                    override fun onInstallError(p0: Int) {
                        showMessage(getString(R.string.sys_install_failed)+" :"+p0)
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace();
            showMessage(
                getString(R.string.sys_install_back_exception),
                e.getLocalizedMessage(),
                Color.RED
            );
        }
    }

    fun installAppBrother() {
        try {
            Toast.makeText(baseContext, "Installing", Toast.LENGTH_LONG).show()
            this.sysSetting?.installApkBack(Environment.getExternalStorageDirectory().absolutePath + "/brother_print_service_2020-09-09_sign.apk",
                object : IPackageInstallListener.Stub() {
                    override fun onInstallFinished() {
                        showMessage(getString(R.string.sys_install_success))
                    }

                    override fun onInstallError(p0: Int) {
                        showMessage(getString(R.string.sys_install_failed)+" :"+p0)

                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            showMessage(
                getString(R.string.sys_install_back_exception),
                e.getLocalizedMessage(),
                Color.RED
            );
        }
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
            Toast.makeText(
                this@LoginActivity,
                "กรุณาระบุ username และ password",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        loading.visibility = View.VISIBLE
        login_btn.isClickable = false
        user_name.isClickable = false
        password.isClickable = false
        Network.login(
            user_name.text.toString(),
            password.text.toString(),
            object : JSONObjectRequestListener {
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

                        PreferencesManager.getInstance().token =
                            response.getString("access_token")
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