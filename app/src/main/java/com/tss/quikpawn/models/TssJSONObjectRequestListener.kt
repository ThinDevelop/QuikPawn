package com.tss.quikpawn.models

import android.content.Context
import android.widget.Toast
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.tss.quikpawn.R
import com.tss.quikpawn.app.QuikPawnApplication
import org.json.JSONObject

class TssJSONObjectRequestListener(var context: Context) : JSONObjectRequestListener {

    override fun onResponse(response: JSONObject?) {
        response?.let {
            val status = it.getString("status_code")
            if (status == "200") {

            } else if (status == "401") {
                context
            } else if (status == "201") {

            } else if (status == "202") {

            } else {

            }
//            1  user pw  ไม่มี จะ  รีเทิน
//            'status_code' =>401,
//            'message' => 'Unauthorized',
//
//            2  เครื่อง  k9  ไม่มีในระบบ  จะรีเทิน
//            'status_code' => ,
//            'message' => 'serial_number is not'
//
//            3  ร้านหมดอายุ  จะรีเทิน
//                    'status_code' => 202,
//            'message' => 'company expire'
        }
    }

    override fun onError(anError: ANError?) {
        Toast.makeText(QuikPawnApplication.getAppContext(), QuikPawnApplication.getAppContext().getString(R.string.connect_error), Toast.LENGTH_SHORT).show()
    }
}