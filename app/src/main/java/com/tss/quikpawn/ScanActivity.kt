package com.tss.quikpawn

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ScanActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private var mScannerView: ZXingScannerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mScannerView = ZXingScannerView(this)   // Programmatically initialize the scanner view
        setContentView(mScannerView)
    }

    public override fun onResume() {
        super.onResume()
        mScannerView!!.setResultHandler(this) // Register ourselves as a handler for scan results.
        mScannerView!!.startCamera()          // Start camera on resume
    }

    public override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera()           // Stop camera on pause
    }

    override fun handleResult(rawResult: Result) {
//        val intent = Intent(this@ScanActivity,BuyActivity::class.java)
//        intent.putExtra("barcode",rawResult.text)
//        startActivity(intent)
////        order_number!!.setText(rawResult.text)
//        onBackPressed()

        val intent = Intent()
        intent.putExtra("barcode", rawResult.text)
        setResult(Activity.RESULT_OK, intent)
        finish()

    }
}
