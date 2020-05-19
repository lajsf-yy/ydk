package com.example;

import android.content.Intent;

import com.facebook.react.ReactActivity;
import ydk.core.Ydk;

public class MainActivity extends ReactActivity {

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    @Override
    protected String getMainComponentName() {
        return "index";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Ydk.onActivityResult(this,requestCode,resultCode,data);
    }

}
