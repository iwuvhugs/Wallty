package com.iwuvhugs.wallty.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.iwuvhugs.wallty.MainActivity;
import com.iwuvhugs.wallty.R;
import com.iwuvhugs.wallty.WalltyApplication;

/**
 * Created by wchgs on 13.05.15.
 */
public class RateAppDialog extends AlertDialog {

    private AlertDialog alertDialog;

    public RateAppDialog(Context context) {
        super(context);
        init(context);
    }

    public RateAppDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    public RateAppDialog(Context context, int theme) {
        super(context, theme);
        init(context);
    }

    private void init(final Context context) {

        alertDialog = new AlertDialog.Builder(context).create();

        alertDialog.setTitle(context.getResources().getString(R.string.rate));
        alertDialog.setMessage(context.getResources().getString(R.string.rate_alert));
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Rate Now", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {
                    ((MainActivity) context).rateApp();
                    WalltyApplication.getInstance().setNeverShowRateDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                alertDialog.dismiss();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Later", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No, thanks", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                WalltyApplication.getInstance().setNeverShowRateDialog();
                alertDialog.dismiss();
            }
        });

    }

    public AlertDialog getAlertDialog() {
        return alertDialog;
    }

}
