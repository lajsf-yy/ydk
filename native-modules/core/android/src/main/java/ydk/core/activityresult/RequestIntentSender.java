package ydk.core.activityresult;


import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import androidx.annotation.Nullable;

public class RequestIntentSender extends Request {
    private final IntentSender intentSender;
    private final Intent fillInIntent;
    private final int flagsMask, flagsValues, extraFlags;
    private final Bundle options;

    public RequestIntentSender(IntentSender intentSender, @Nullable Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, @Nullable Bundle options) {
        super(null);
        this.intentSender = intentSender;
        this.fillInIntent = fillInIntent;
        this.flagsMask = flagsMask;
        this.flagsValues = flagsValues;
        this.extraFlags = extraFlags;
        this.options = options;
    }

    public IntentSender getIntentSender() {
        return intentSender;
    }

    public Intent getFillInIntent() {
        return fillInIntent;
    }

    public int getFlagsMask() {
        return flagsMask;
    }

    public int getFlagsValues() {
        return flagsValues;
    }

    public int getExtraFlags() {
        return extraFlags;
    }

    public Bundle getOptions() {
        return options;
    }
}
