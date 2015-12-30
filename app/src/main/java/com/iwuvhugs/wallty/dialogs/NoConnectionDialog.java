package com.iwuvhugs.wallty.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.iwuvhugs.wallty.R;

/**
 * Created by wchgs on 13.05.15.
 */
public class NoConnectionDialog extends AlertDialog {

    private AlertDialog alertDialog;

    public NoConnectionDialog(Context context) {
        super(context);
        init(context);
    }

    public NoConnectionDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    public NoConnectionDialog(Context context, int theme) {
        super(context, theme);
        init(context);
    }

    private void init(Context context) {

        alertDialog = new AlertDialog.Builder(context).create();

        alertDialog.setTitle(context.getResources().getString(R.string.info));
        alertDialog.setMessage(context.getResources().getString(R.string.check_internet));
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
    }

    public AlertDialog getAlertDialog() {
        return alertDialog;
    }

}
