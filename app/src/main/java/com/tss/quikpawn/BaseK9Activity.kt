package com.tss.quikpawn

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.centerm.centermposoversealib.thailand.*
import com.centerm.smartpos.aidl.iccard.AidlICCard
import com.centerm.smartpos.aidl.printer.AidlPrinter
import com.centerm.smartpos.aidl.printer.AidlPrinterStateChangeListener
import com.centerm.smartpos.aidl.printer.PrinterParams
import com.centerm.smartpos.aidl.sys.AidlDeviceManager
import com.centerm.smartpos.constant.Constant
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.tss.quikpawn.models.OrderModel
import com.tss.quikpawn.util.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

open class BaseK9Activity: BaseActivity() {

    val SCAN_REQUEST_CODE = 1002

    open val IMAGE_CAPTURE_CODE = 1001
    private val PERMISSION_CODE = 1000
    var citizenId = ""
    var address = ""
    private var printDev: AidlPrinter? = null
    private val callback = PrinterCallback()
    private var aidlIdCardTha: AidlIdCardTha? = null
    private var aidlIcCard: AidlICCard? = null
    private var aidlReady = false
    var process = false
    var disposable: Disposable? = null
    var image_uri: Uri? = null
    var imgView: ImageView? = null
    var imageFilePath = ""
    var alreadyOpen = true
    var index = 0
    var loadingProgressBar: ProgressBar? = null
    var customerPhoto = ""


    private val size = 660
    private val size_width = 660
    private val size_height = 264

    open fun initialK9() {
        val dialog = createProgressDialog(this, "Loading...")
        disposable = Observable.interval(1, TimeUnit.SECONDS)
            .filter {
                aidlIcCard?.open()
                if (aidlIcCard?.status()?.toInt() != 1) {
                    process = false
                }
                aidlIdCardTha != null && !process && aidlIcCard?.status()?.toInt() == 1
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                process = true
                runOnUiThread { dialog?.show() }
                aidlIdCardTha?.searchIDCard(6000, object : AidlIdCardThaListener.Stub()  {

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

    open fun initialK9Fast() {
        val dialog = createProgressDialog(this, "Loading...")
        disposable = Observable.interval(1, TimeUnit.SECONDS)
            .filter {
                aidlIcCard?.open()
                if (aidlIcCard?.status()?.toInt() != 1) {
                    process = false
                }
                aidlIdCardTha != null && !process && aidlIcCard?.status()?.toInt() == 1
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                process = true
                runOnUiThread { dialog?.show() }

                aidlIdCardTha?.searchIDCardSecurity(6000, object : ThaiIDSecurityListerner.Stub(){
                    override fun onFindIDCard(p0: ThaiIDSecurityBeen?) {
                        runOnUiThread {
                            dialog?.dismiss()
                            p0?.let {
                                setupViewFast(p0)
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

    open fun createProgressDialog(context: Context, title: String): AlertDialog {
        val view = layoutInflater.inflate(R.layout.layout_loading_dialog, null)
        val text = view.findViewById<TextView>(R.id.txt_load)
        text.text = title
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false) // if you want user to wait for some process to finish,
        builder.setView(view)
        return builder.create()
    }

    @CallSuper
    open fun setupView(info: ThiaIdInfoBeen) {
        citizenId = info.citizenId
        address = info.address.replace("#", " ")
        customerPhoto = Util.bitmapToBase64(info.photo)
    }

    @CallSuper
    open fun setupViewFast(info: ThaiIDSecurityBeen) {
        citizenId = info.citizenId
        address = info.address.replace("#", " ")
    }


    @Throws(WriterException::class)
    fun createImageBarcode(message: String?, type: String?): Bitmap? {
        var bitMatrix: BitMatrix? = null
        bitMatrix = when (type) {
            "QR Code" -> MultiFormatWriter().encode(message, BarcodeFormat.QR_CODE, 150, 150)
            "Barcode" -> MultiFormatWriter().encode(message, BarcodeFormat.QR_CODE, 150, 150)
            "Barcode" -> MultiFormatWriter().encode(
                message,
                BarcodeFormat.CODE_128,
                size_width,
                size_height/2
            )
            "Data Matrix" -> MultiFormatWriter().encode(
                message,
                BarcodeFormat.DATA_MATRIX,
                size,
                size
            )
            "PDF 417" -> MultiFormatWriter().encode(
                message,
                BarcodeFormat.PDF_417,
                size_width,
                size_height
            )
            "Barcode-39" -> MultiFormatWriter().encode(
                message,
                BarcodeFormat.CODE_39,
                size_width,
                size_height
            )
            "Barcode-93" -> MultiFormatWriter().encode(
                message,
                BarcodeFormat.CODE_93,
                size_width,
                size_height
            )
            "AZTEC" -> MultiFormatWriter().encode(message, BarcodeFormat.AZTEC, size, size)
            else -> MultiFormatWriter().encode(message, BarcodeFormat.QR_CODE, size, size)
        }
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (i in 0 until height) {
            for (j in 0 until width) {
                if (bitMatrix[j, i]) {
                    pixels[i * width + j] = -0x1000000
                } else {
                    pixels[i * width + j] = -0x1
                }
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    open fun cameraOpen(img_view: ImageView, loading: ProgressBar,imageIndex: Int) {
        if (!alreadyOpen) {
            return
        }
        index = imageIndex
        alreadyOpen = false
        loadingProgressBar = loading
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
                val permission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permission,PERMISSION_CODE)
            }
            else{
                openCamera2()
            }
        } else {
            openCamera2()
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
                    openCamera2()
                }
                else{
                    alreadyOpen = true
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        alreadyOpen = true
        if(resultCode != Activity.RESULT_OK) return
        if (IMAGE_CAPTURE_CODE == requestCode) {
            imgView?.setImageURI(Uri.parse(imageFilePath))
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

    fun openCamera2() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            //Create a file to store the image
            var photoFile: File? = null
            try {

//                val cw = ContextWrapper(applicationContext)
//                // path to /data/data/yourapp/app_data/imageDir
//                // path to /data/data/yourapp/app_data/imageDir
//                val directory: File = cw.getDir("imageDir", Context.MODE_PRIVATE)



                val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES+ "/quikpawn")
                photoFile = File.createTempFile(""+System.currentTimeMillis() ,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */)
                imageFilePath = photoFile.getAbsolutePath()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            photoFile?.let {
                val photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile)
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(pictureIntent, IMAGE_CAPTURE_CODE)
            }
        }
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

    open fun setTagToImageView(id: String) {
        imgView?.tag = id
    }
}
