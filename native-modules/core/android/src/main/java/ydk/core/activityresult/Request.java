package ydk.core.activityresult;


import android.content.Intent;
import androidx.annotation.Nullable;

class Request {
    private final Intent intent;
    private OnPreResult onPreResult;
    private OnResult onResult;

    public Request(@Nullable Intent intent) {
        this.intent = intent;
    }

    void setOnPreResult(@Nullable OnPreResult onPreResult) {
        this.onPreResult = onPreResult;
    }

    OnPreResult onPreResult() {
        return onPreResult;
    }

    public void setOnResult(OnResult onResult) {
        this.onResult = onResult;
    }

    public OnResult onResult() {
        return onResult;
    }

    @Nullable public Intent intent() {
        return intent;
    }
}
