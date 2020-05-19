package com.luck.picture.lib.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;

import ydk.album.R;


public class PictureDialog extends Dialog {
    public Context context;

    public PictureDialog(Context context) {
        super(context, R.style.yui_picture_alert_dialog);
        this.context = context;
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        Window window = getWindow();
        window.setWindowAnimations(R.style.yui_DialogWindowStyle);
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.yui_picture_alert_dialog);
    }
}