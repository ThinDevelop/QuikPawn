package com.tss.quikpawn

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.centerm.centermposoversealib.thailand.AidlIdCardTha
import com.centerm.centermposoversealib.thailand.AidlIdCardThaListener
import com.centerm.centermposoversealib.thailand.ThiaIdInfoBeen
import com.centerm.centermposoversealib.util.Utility
import com.centerm.smartpos.aidl.iccard.AidlICCard
import com.centerm.smartpos.aidl.printer.AidlPrinter
import com.centerm.smartpos.aidl.printer.AidlPrinterStateChangeListener
import com.centerm.smartpos.aidl.printer.PrinterParams
import com.centerm.smartpos.aidl.sys.AidlDeviceManager
import com.centerm.smartpos.constant.Constant
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_sell.*
import java.util.concurrent.TimeUnit

open class BaseK9Activity: BaseActivity() {

    private val IMAGE_CAPTURE_CODE = 1001
    private val PERMISSION_CODE = 1000
    private var printDev: AidlPrinter? = null
    private val callback = PrinterCallback()
    private var aidlIdCardTha: AidlIdCardTha? = null
    private var aidlIcCard: AidlICCard? = null
    private var aidlReady = false
    var process = false
    var disposable: Disposable? = null
    var image_uri: Uri? = null
    var imgView: ImageView? = null

    open fun initialK9() {
        val dialog = createProgressDialog("Loading...")
        disposable = Observable.interval(1, TimeUnit.SECONDS)
            .filter {
                aidlIcCard?.open()
                aidlIdCardTha != null && !process && aidlIcCard?.status()?.toInt() == 1
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                process = true
                runOnUiThread { dialog?.show() }
                aidlIdCardTha?.searchIDCard(6000, object : AidlIdCardThaListener.Stub() {
                    override fun onFindIDCard(p0: ThiaIdInfoBeen?) {
                        runOnUiThread {
                            dialog?.dismiss()
                            p0?.let {
                                setupView(p0)
                            }
                        }
                    }

                    override fun onTimeout() {
                        runOnUiThread { dialog?.dismiss() }
                        process = false
                    }

                    override fun onError(p0: Int, p1: String?) {
                        runOnUiThread { dialog?.dismiss() }
                        process = false
                    }
                })
                // this method will be called when action is success
            }, { error ->
                runOnUiThread { dialog?.dismiss() }
                process = false
            })
    }

    private fun createProgressDialog(title: String): AlertDialog {
        val view = layoutInflater.inflate(R.layout.layout_loading_dialog, null)
        val text = view.findViewById<TextView>(R.id.txt_load)
        text.text = title
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false) // if you want user to wait for some process to finish,
        builder.setView(view)
        return builder.create()
    }

    open fun setupView(info: ThiaIdInfoBeen) {

    }

    open fun cameraOpen(img_view: ImageView) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
                val permission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permission,PERMISSION_CODE)
            }
            else{
                openCamera()
            }
        }

        imgView = img_view
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_CODE -> {
                if(grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    openCamera()
                }
                else{
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK){
            imgView?.setImageURI(image_uri)
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE,"New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION,"From the Camera")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    override fun onDeviceConnected(deviceManager: AidlDeviceManager?, cpay: Boolean) {
        try {
            if (cpay) {
                aidlIcCard = AidlICCard.Stub.asInterface(deviceManager?.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_ICCARD))
                if (aidlIcCard != null) {
                    Log.e("MY", "IcCard bind success!")
                    //This is the IC card service object!!!!
                    //I am do nothing now and it is not null.
                    //you can do anything by yourselef later.
                } else {
                    Log.e("MY", "IcCard bind fail!")
                }
            } else {
                aidlIdCardTha = AidlIdCardTha.Stub.asInterface(deviceManager?.getDevice(com.centerm.centermposoversealib.constant.Constant.OVERSEA_DEVICE_CODE.OVERSEA_DEVICE_TYPE_THAILAND_ID))
                aidlReady = aidlIdCardTha != null

            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    override fun onPrintDeviceConnected(deviceManager: AidlDeviceManager?) {
        try {
            printDev = AidlPrinter.Stub.asInterface(deviceManager?.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_PRINTERDEV))
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private inner class PrinterCallback : AidlPrinterStateChangeListener.Stub() {
        @Throws(RemoteException::class)
        override fun onPrintError(arg0: Int) {
            // showMessage("打印机异常" + arg0, Color.RED);
        }

        @Throws(RemoteException::class)
        override fun onPrintFinish() {
        }

        @Throws(RemoteException::class)
        override fun onPrintOutOfPaper() {
        }
    }

    open fun printdata(textList: ArrayList<PrinterParams>) {
        printDev?.printDatas(textList, callback)

    }
}
