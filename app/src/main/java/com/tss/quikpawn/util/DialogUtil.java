package com.tss.quikpawn.util;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;


import androidx.appcompat.app.AlertDialog;

import com.tss.quikpawn.R;
import com.tss.quikpawn.models.DialogParamModel;

public class DialogUtil {

    public static String CONFIRM = "confirm";
    public static String CANCEL = "cancel";
    public static void showInputDialog(Context context, final InputTextBackListerner inputTextBackListerner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("ปรับปรุงราคาขาย");

// Set up the input
        final EditText input = new EditText(context);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.addTextChangedListener(new NumberTextWatcherForThousand(input));
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputTextBackListerner.onClickConfirm(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public static void showNotiDialog(Context context, String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setNegativeButton(context.getString(R.string.text_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public static void showConfirmDialog(DialogParamModel dialogParamModel, Context context, final InputTextBackListerner inputTextBackListerner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(dialogParamModel.getTitle());
        StringBuilder message = new StringBuilder();
        for (String msg : dialogParamModel.getMsg()) {
            message.append(msg+"\n");
        }
        builder.setMessage(message);
        builder.setPositiveButton(dialogParamModel.getActionMsgP(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputTextBackListerner.onClickConfirm(CONFIRM);
            }
        });
        builder.setCancelable(false);
        builder.setNegativeButton(dialogParamModel.getActionMsgN(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputTextBackListerner.onClickConfirm(CANCEL);
                dialog.cancel();
            }
        });

        builder.show();
    }



    public interface InputTextBackListerner {
        void onClickConfirm(String result);
    }
}
