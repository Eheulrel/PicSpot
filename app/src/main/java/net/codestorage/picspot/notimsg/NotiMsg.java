package net.codestorage.picspot.notimsg;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class NotiMsg {
    public static void notiMsg(Context context, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                });
        builder.create();
        builder.show();
    }
}
