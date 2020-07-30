package com.tss.quikpawn

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.centerm.smartpos.aidl.sys.AidlDeviceManager
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException


class PrintDialogActivity : BaseActivity() {
    /**
     * Web view element to show the printing dialog in.
     */
    private var dialogWebView: WebView? = null

    /**
     * Intent that started the action.
     */

    var cloudPrintIntent: Intent? = null
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setContentView(R.layout.print_dialog)
        dialogWebView = findViewById<View>(R.id.webview) as WebView
        cloudPrintIntent = this.intent
        val settings = dialogWebView!!.settings
        settings.javaScriptEnabled = true
        dialogWebView!!.webViewClient = PrintDialogWebClient()
//        dialogWebView!!.addJavascriptInterface(
//            PrintDialogJavaScriptInterface(),
//            JS_INTERFACE
//        )
        dialogWebView!!.loadUrl(PRINT_DIALOG_URL)
    }

    override fun onDeviceConnected(deviceManager: AidlDeviceManager?, capy: Boolean) {
    }

    public override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        intent: Intent?
    ) {
        if (requestCode == ZXING_SCAN_REQUEST && resultCode == RESULT_OK) {
            dialogWebView!!.loadUrl(intent?.getStringExtra("SCAN_RESULT"))
        }
    }

    override fun onPrintDeviceConnected(deviceManager: AidlDeviceManager?) {
    }

    private inner class PrintDialogWebClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView,
            url: String
        ): Boolean {
            if (url.startsWith(ZXING_URL)) {
                val intentScan = Intent("com.google.zxing.client.android.SCAN")
                intentScan.putExtra("SCAN_MODE", "QR_CODE_MODE")
                try {
                    startActivityForResult(
                        intentScan,
                        ZXING_SCAN_REQUEST
                    )
                } catch (error: ActivityNotFoundException) {
                    view.loadUrl(url)
                }
            } else {
                view.loadUrl(url)
            }
            return false
        }

        override fun onPageFinished(view: WebView, url: String) {
            if (PRINT_DIALOG_URL == url) {
                // Submit print document.
                view.loadUrl(
                    "javascript:printDialog.setPrintDocument(printDialog.createPrintDocument("
                            + "window." + JS_INTERFACE + ".getType(),window." + JS_INTERFACE + ".getTitle(),"
                            + "window." + JS_INTERFACE + ".getContent(),window." + JS_INTERFACE + ".getEncoding()))"
                )

                // Add post messages listener.
                view.loadUrl(
                    "javascript:window.addEventListener('message',"
                            + "function(evt){window." + JS_INTERFACE + ".onPostMessage(evt.data)}, false)"
                )
            }
        }
    }

    companion object {
        private const val PRINT_DIALOG_URL =
            "https://www.google.com/cloudprint/dialog.html"
        private const val JS_INTERFACE = "AndroidPrintDialog"
        const val encoding = "base64"
        private const val ZXING_URL = "http://zxing.appspot.com"
        private const val ZXING_SCAN_REQUEST = 65743

        /**
         * Post message that is sent by Print Dialog web page when the printing dialog
         * needs to be closed.
         */
        private const val CLOSE_POST_MESSAGE_NAME = "cp-dialog-on-close"
    }
}