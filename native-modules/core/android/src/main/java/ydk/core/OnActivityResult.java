package ydk.core;

import android.app.Activity;
import android.content.Intent;

public interface OnActivityResult {
    void onActivityResult(Activity Activity, int requestCode, int resultCode, Intent data);
}
