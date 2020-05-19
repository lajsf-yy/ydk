package ydk.ui.pickview.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import ydk.ui.R;
import ydk.ui.pickview.configure.PickerOptions;

/**
 * 条件选择器
 * Created by Sai on 15/11/22.
 */
public class SingleOptionsPickerView<T> extends OptionsPickerView<T> implements View.OnClickListener {

    public SingleOptionsPickerView(PickerOptions pickerOptions) {
        super(pickerOptions);
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPickerOptions.titleClickListener != null) {
                    mPickerOptions.titleClickListener.onClick(v);
                }
            }
        });
    }



}
