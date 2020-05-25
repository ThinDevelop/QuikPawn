package com.tss.quikpawn

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.centerm.centermposoversealib.thailand.AidlIdCardTha
import com.centerm.centermposoversealib.thailand.AidlIdCardThaListener
import com.centerm.centermposoversealib.thailand.ThiaIdInfoBeen
import com.centerm.centermposoversealib.util.Utility.toGrayscale
import com.centerm.smartpos.aidl.iccard.AidlICCard
import com.centerm.smartpos.aidl.printer.AidlPrinter
import com.centerm.smartpos.aidl.printer.AidlPrinterStateChangeListener
import com.centerm.smartpos.aidl.printer.PrinterParams
import com.centerm.smartpos.aidl.sys.AidlDeviceManager
import com.centerm.smartpos.constant.Constant
import com.tss.quikpawn.adapter.ImagesAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_form_detail.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.MediaFile
import pl.aprilapps.easyphotopicker.MediaSource
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class FormDetailActivity : BaseActivity() {
    private val CHOOSER_PERMISSIONS_REQUEST_CODE = 7459
    private val CAMERA_REQUEST_CODE = 7500
    private val CAMERA_VIDEO_REQUEST_CODE = 7501
    private val GALLERY_REQUEST_CODE = 7502
    private val DOCUMENTS_REQUEST_CODE = 7503
    lateinit var easyImage : EasyImage
    protected var recyclerView: RecyclerView? = null
    private var imagesAdapter: ImagesAdapter? = null
    private val photos = ArrayList<MediaFile>()
    private var printDev: AidlPrinter? = null
    private val callback = PrinterCallback()
    private var aidlIdCardTha: AidlIdCardTha? = null
    private var aidlIcCard: AidlICCard? = null
    private var aidlReady = false
    var process = false
    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_detail)

        imagesAdapter = ImagesAdapter(this, photos)
        recycler_view.setLayoutManager(LinearLayoutManager(this))
        recycler_view.setHasFixedSize(true)
        recycler_view.setAdapter(imagesAdapter)

        photo.setOnClickListener {
            val necessaryPermissions = arrayOf(Manifest.permission.CAMERA)
            if (arePermissionsGranted(necessaryPermissions)) {
                easyImage.openCameraForImage(this@FormDetailActivity)
            } else {
                requestPermissionsCompat(necessaryPermissions, CAMERA_REQUEST_CODE)
            }
        }

        easyImage = EasyImage.Builder(this)
            .setCopyImagesToPublicGalleryFolder(false)
            .setFolderName("EasyImage sample")
            .allowMultiple(true)
            .build()

        initialK9()
    }

    fun initialK9() {
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
                                   p0?.let {
                                       runOnUiThread {
                                           runOnUiThread { dialog?.dismiss() }
                                           val currentYear = Calendar.getInstance().get(Calendar.YEAR) + 543
                                           val year = Integer.valueOf(it.birthDate.substring(it.birthDate.lastIndex-3))
                                           val age = (currentYear - year).toString()
                                           txt_2.setText(it.thaiFirstName + " " + it.thaiLastName)
                                           txt_4.setText(age)
                                           txt_citizenId.setText(it.citizenId.substring(0, it.citizenId.length-3) + "XXX")
                                           txt_7.setText(it.homeNumber)
                                           txt_district.setText(it.subDistrict)
                                           txt_10.setText(it.district)
                                           txt_12.setText(it.province)
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
        val builder = AlertDialog.Builder(this@FormDetailActivity)
        builder.setCancelable(false) // if you want user to wait for some process to finish,
        builder.setView(view)
        return builder.create()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.my_option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.print -> {
                printdata()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun printdata() {
        var imageicon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.gold_b)
        imageicon = Bitmap.createScaledBitmap(imageicon, 130, 130, false)
        imageicon = toGrayscale(imageicon)

        var qr: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.gold_a)
        qr = Bitmap.createScaledBitmap(qr, 200, 250, false)
        qr = toGrayscale(qr)



        val textList = java.util.ArrayList<PrinterParams>()
        var printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("ใบฝากขาย")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("เลขที่ 3982")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.RIGHT)
        printerParams1.setTextSize(22)
        printerParams1.setText("วันที่ 1 มกราคม พ.ศ. 2563")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("ข้าพเจ้าปัญญา นาคนพคุณ \nอายุ 30 ปี\nบัตรประชาชน ")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("ได้ทำหนังสือขายฝากนี้ให้แก่ นายสุรศักดิ์ ขจิตธรรมกุล ดังมีข้อความดังต่อไปนี้\n" + "   ข้อ 1. ผู้ขายฝากได้นำทรัพย์สินปรากฎตามรายการดังนี้\n\n")
        textList.add(printerParams1)


        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n 1. ")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\nมาขายฝากให้เป็นจำนวนเงิน xxx บาท\nและได้รับเงินไปเสร็จเรียบร้อยแล้ว จึงลงลายมือชื่อไว้เป็นหลักฐาน")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n\n\n___________________")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("ผู้ขายฝาก")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n\n\n___________________")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("ผู้รับซื้อฝาก")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n\n\n___________________")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("พยาน")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n\n\n___________________")
        textList.add(printerParams1)

        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.CENTER)
        printerParams1.setTextSize(22)
        printerParams1.setText("พยาน/ผู้พิมพ์")
        textList.add(printerParams1)


        printerParams1 = PrinterParams()
        printerParams1.setAlign(PrinterParams.ALIGN.LEFT)
        printerParams1.setTextSize(22)
        printerParams1.setText("\n\n\n\n\n")
        textList.add(printerParams1)

        printDev?.printDatas(textList, callback)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        easyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
            override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                onPhotosReturned(imageFiles)
            }

            override fun onImagePickerError(error: Throwable, source: MediaSource) {
                //Some error handling
                error.printStackTrace()
            }

            override fun onCanceled(source: MediaSource) {
                //Not necessary to remove any files manually anymore
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CHOOSER_PERMISSIONS_REQUEST_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openChooser(this@FormDetailActivity)
        } else if (requestCode == CAMERA_REQUEST_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openCameraForImage(this@FormDetailActivity)
        } else if (requestCode == CAMERA_VIDEO_REQUEST_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openCameraForVideo(this@FormDetailActivity)
        } else if (requestCode == GALLERY_REQUEST_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openGallery(this@FormDetailActivity)
        } else if (requestCode == DOCUMENTS_REQUEST_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            easyImage.openDocuments(this@FormDetailActivity)
        }
    }

    private fun onPhotosReturned(returnedPhotos: Array<MediaFile>) {
        photos.addAll(returnedPhotos)
        imagesAdapter?.notifyDataSetChanged()
        recyclerView?.scrollToPosition(photos.size - 1)
    }

    private fun arePermissionsGranted(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }

    private fun requestPermissionsCompat(permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(this@FormDetailActivity, permissions, requestCode)
    }

}
