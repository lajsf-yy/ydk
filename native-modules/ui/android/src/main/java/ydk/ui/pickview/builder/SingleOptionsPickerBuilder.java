package ydk.ui.pickview.builder;

import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.ColorInt;
import android.view.View;
import android.view.ViewGroup;

import ydk.ui.pickview.configure.PickerOptions;
import ydk.ui.pickview.listener.CustomListener;
import ydk.ui.pickview.listener.OnOptionsSelectChangeListener;
import ydk.ui.pickview.listener.OnOptionsSelectListener;
import ydk.ui.pickview.view.OptionsPickerView;
import ydk.ui.pickview.view.SingleOptionsPickerView;
import ydk.ui.pickview.view.WheelView;


/**
 * Created by xiaosongzeem on 2018/3/20.
 */

public class SingleOptionsPickerBuilder extends OptionsPickerBuilder {

    //Required
    public SingleOptionsPickerBuilder(Context context, OnOptionsSelectListener listener) {
        super(context, listener);
    }

    public SingleOptionsPickerBuilder setOnTitleClickListener(View.OnClickListener clickListener) {
        mPickerOptions.titleClickListener = clickListener;
        return this;
    }

    public <T> SingleOptionsPickerView<T> build() {
        return new SingleOptionsPickerView<>(mPickerOptions);
    }

}
