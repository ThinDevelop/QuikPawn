package com.tss.quikpawn.adapter

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.util.Log
import com.tss.quikpawn.activities.ReprintOrderActivity
import java.io.*

class PdfDocumentAdapter(var activity: Activity, var path: String) :
    PrintDocumentAdapter() {
    override fun onLayout(
        printAttributes: PrintAttributes,
        printAttributes1: PrintAttributes,
        cancellationSignal: CancellationSignal,
        layoutResultCallback: LayoutResultCallback,
        bundle: Bundle
    ) {
        if (cancellationSignal.isCanceled) {
            layoutResultCallback.onLayoutCancelled()
        } else {
            val builder = PrintDocumentInfo.Builder("file name")
            builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                .build()
            layoutResultCallback.onLayoutFinished(
                builder.build(),
                printAttributes1 != printAttributes
            )
        }
    }

    override fun onStart() {
        Log.e("PdfDocumentAdapter", "onStart")
        super.onStart()
    }
    override fun onWrite(
        pageRanges: Array<PageRange>,
        parcelFileDescriptor: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        writeResultCallback: WriteResultCallback
    ) {
        var `in`: InputStream? = null
        var out: OutputStream? = null
        try {
            val file = File(path)
            `in` = FileInputStream(file)
            out = FileOutputStream(parcelFileDescriptor.fileDescriptor)
            val buff = ByteArray(16384)
            var size: Int
            while (`in`.read(buff).also { size = it } >= 0 && !cancellationSignal.isCanceled) {
                out.write(buff, 0, size)
            }
            if (cancellationSignal.isCanceled) writeResultCallback.onWriteCancelled() else {
                writeResultCallback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }
        } catch (e: FileNotFoundException) {
            writeResultCallback.onWriteFailed(e.message)
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `in`!!.close()
                out!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    override fun onFinish() {
        super.onFinish()
        if (activity !is ReprintOrderActivity) {
            activity.finish()
        }
        Log.e("PdfDocumentAdapter", "onFinish")
    }

}