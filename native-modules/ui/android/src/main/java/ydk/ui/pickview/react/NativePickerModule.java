package ydk.ui.pickview.react;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import ydk.core.utils.MapUtils;
import ydk.ui.pickview.builder.OptionsPickerBuilder;
import ydk.ui.pickview.builder.SingleOptionsPickerBuilder;
import ydk.ui.pickview.builder.TimePickerBuilder;
import ydk.ui.pickview.interfaces.IPickerViewData;
import ydk.ui.pickview.listener.OnOptionsSelectListener;
import ydk.ui.pickview.listener.OnTimeSelectListener;
import ydk.ui.pickview.view.OptionsPickerView;
import ydk.ui.pickview.view.SingleOptionsPickerView;
import ydk.ui.pickview.view.TimePickerView;

public class NativePickerModule extends ReactContextBaseJavaModule {

    private final String NAME = "NativePicker";

    public NativePickerModule(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void showLocationPicker(ReadableMap data, Promise promise) {

        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            promise.reject(new IllegalArgumentException("currentActivity is null"));
            return;
        }
        Gson gson = new Gson();
        DataBean dataBean = gson.fromJson(gson.toJson(data.toHashMap()), DataBean.class);

        List<LocationBean> options1Items = dataBean.pickerData;
        List<List<String>> options2Items = new ArrayList<>();
        List<List<List<String>>> options3Items = new ArrayList<>();
        for (int i = 0; i < options1Items.size(); i++) {
            List<LocationBean> cityInfo = options1Items.get(i).children;
            List<String> citylist = new ArrayList<>();
            List<List<String>> cityarealist = new ArrayList<>();
            if (null != cityInfo) {
                for (int j = 0; j < cityInfo.size(); j++) {
                    citylist.add(cityInfo.get(j).label);

                    List<LocationBean> areaInfo = cityInfo.get(j).children;
                    List<String> areaList = new ArrayList<>();
                    if (null != areaInfo) {
                        for (int k = 0; k < areaInfo.size(); k++) {
                            areaList.add(areaInfo.get(k).label);
                        }
                    }
                    cityarealist.add(areaList);

                }
            }
            options2Items.add(citylist);
            options3Items.add(cityarealist);

        }
        int options1 = 0, options2 = 0, options3 = 0;
        try {
            List<String> preValue = dataBean.pickerValue;
            if (null != preValue) {
                if (preValue.size() >= 1 && !TextUtils.isEmpty(preValue.get(0))) {
                    int size = options1Items.size();
                    for (int i = 0; i < size; i++) {
                        if (options1Items.get(i).value.equals(preValue.get(0))) {
                            options1 = i;
                            break;
                        }
                    }
                }
                if (preValue.size() >= 2 && !TextUtils.isEmpty(preValue.get(1))) {
                    List<LocationBean> data2 = options1Items.get(options1).children;
                    int size2 = data2.size();
                    for (int i = 0; i < size2; i++) {
                        if (data2.get(i).value.equals(preValue.get(1))) {
                            options2 = i;
                            break;
                        }
                    }
                }
                if (preValue.size() >= 3 && !TextUtils.isEmpty(preValue.get(2))) {
                    List<LocationBean> data3 = options1Items.get(options1).children.get(options2).children;
                    int size3 = data3.size();
                    for (int i = 0; i < size3; i++) {
                        if (data3.get(i).value.equals(preValue.get(1))) {
                            options3 = i;
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        int finalOptions1 = options1;
        int finalOptions2 = options2;
        int finalOptions3 = options3;
        currentActivity.runOnUiThread(() -> showLocationPicker(dataBean.column, dataBean.pickerTitle,
                options1Items, options2Items, options3Items,
                finalOptions1, finalOptions2, finalOptions3,
                dataBean.colorConfirm, dataBean.colorCancel,
                (options11, options21, options31, v) -> {

                    List<LocationBean> data1 = new ArrayList<>();

                    if (dataBean.column >= 1) {
                        LocationBean bean = new LocationBean();
                        bean.value = options1Items.get(options11).value;
                        bean.label = options1Items.get(options11).label;
                        data1.add(bean);
                    }

                    if (dataBean.column >= 2) {
                        try {
                            LocationBean bean = new LocationBean();
                            bean.value = options1Items.get(options11).children.get(options21).value;
                            bean.label = options1Items.get(options11).children.get(options21).label;
                            data1.add(bean);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (dataBean.column >= 3) {
                        try {
                            LocationBean bean = new LocationBean();
                            bean.value = options1Items.get(options11).children.get(options21).children.get(options31).value;
                            bean.label = options1Items.get(options11).children.get(options21).children.get(options31).label;
                            data1.add(bean);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    promise.resolve(new Gson().toJson(data1));

                }));

    }

    private SingleOptionsPickerView pvOptions;

    @ReactMethod
    public void showSimplePicker(ReadableMap data, Promise promise) {

        Gson gson = new Gson();
        DataBean dataBean = gson.fromJson(gson.toJson(data.toHashMap()), DataBean.class);

        OnOptionsSelectListener listener = (options1, options2, options3, v) -> {

            LocationBean bean = dataBean.pickerData.get(options1);

            WritableMap map = Arguments.createMap();
            map.putString("label", bean.label);
            map.putString("value", bean.value);

            promise.resolve(map);
        };
        pvOptions = null;
        View.OnClickListener titleClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null == pvOptions) {
                    return;
                }
                pvOptions.dismiss();
                WritableMap map = Arguments.createMap();
                map.putBoolean("titleClick", true);
                promise.resolve(map);
            }
        };


        getCurrentActivity().runOnUiThread(() -> {
            String colorConfirm = dataBean.colorConfirm;
            if (TextUtils.isEmpty(colorConfirm)) {
                colorConfirm = "#ff3874f5";
            }
            String colorCancel = dataBean.colorCancel;
            if (TextUtils.isEmpty(colorCancel)) {
                colorCancel = "#ff1f1f1f";
            }
            SingleOptionsPickerBuilder builder = new SingleOptionsPickerBuilder(
                    getCurrentActivity(), listener);
            builder.setTitleText(dataBean.pickerTitle);
            builder.setOnTitleClickListener(titleClickListener);
            builder.setCancelColor(Color.parseColor(colorCancel));
            builder.setTitleColor(Color.BLACK);
            builder.setSubmitColor(Color.parseColor(colorConfirm));
            builder.setLineSpacingMultiplier(1.6f);
            builder.setSelectOptions(0);

            pvOptions = builder.build();

            pvOptions.setPicker(dataBean.pickerData);//一级选择器

            pvOptions.show();
        });
    }

    /**
     * { currentKey: vm.key, pickerTitle: vm.title, datePickerValue, reverse:boolean }//datePickerValue: yyyy-MM-dd
     *
     * @param data
     * @param promise
     */
    @ReactMethod
    public void showDateTimePicker(ReadableMap data, Promise promise) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            promise.reject(new IllegalArgumentException("currentActivity is null"));
            return;
        }

        DatePickerConfig datePickerConfig = MapUtils.toObject(data.toHashMap(), DatePickerConfig.class);


        String dateValue = datePickerConfig.getDateValue();
        Calendar nowDate = Calendar.getInstance();
        nowDate.setTime(new Date());

        Calendar dateCalendar = !TextUtils.isEmpty(dateValue) ? getCalendar(dateValue, datePickerConfig.getTimeFormat()) : nowDate;

        String maxValue = datePickerConfig.getMaxValue();
        Calendar maxCalendar = !TextUtils.isEmpty(maxValue) ? getCalendar(maxValue, datePickerConfig.getTimeFormat()) : nowDate;

        String minValue = datePickerConfig.getMinValue();
        Calendar minCalendar = !TextUtils.isEmpty(minValue) ? getCalendar(minValue, datePickerConfig.getTimeFormat()) : getCalendar("1900-01-01", "yyyy-MM-dd");


        currentActivity.runOnUiThread(() -> {

            showDateTimePicker(datePickerConfig.getPickerTitle(),
                    dateCalendar, minCalendar, maxCalendar,
                    datePickerConfig.getTimeFormat(), datePickerConfig.getColorConfirm(), datePickerConfig.getColorCancel(), (date1, v) -> {
                        String formatString = datePickerConfig.getTimeFormat();
                        if (TextUtils.isEmpty(formatString)) {
                            formatString = "yyyy-MM-dd";
                        }
                        formatString = formatString.replace("hh", "HH");
                        SimpleDateFormat format = new SimpleDateFormat(formatString);
                        promise.resolve(format.format(date1));
                    });
        });

    }

    private Calendar getCalendar(String value, String timeFormat) {
        if (TextUtils.isEmpty(timeFormat)) {
            timeFormat = "yyyy-MM-dd";
        }
        timeFormat = timeFormat.replace("hh", "HH");
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(timeFormat);
        try {
            Date date = format.parse(value);
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }


    private void showDateTimePicker(String title, Calendar dataValue, Calendar minValue, Calendar maxValue,
                                    String timeFormat, String colorConfirm, String colorCancel,
                                    OnTimeSelectListener listener) {

        boolean[] type = new boolean[]{true, true, true, false, false, false};
        if (!TextUtils.isEmpty(timeFormat)) {
            type[0] = timeFormat.contains("yyyy");
            type[1] = timeFormat.contains("MM");
            type[2] = timeFormat.contains("dd");
            type[3] = timeFormat.contains("HH") || timeFormat.contains("hh");
            type[4] = timeFormat.contains("mm");
            type[5] = timeFormat.contains("ss");
        }
        if (TextUtils.isEmpty(colorCancel)) {
            colorCancel = "#ff1f1f1f";
        }
        if (TextUtils.isEmpty(colorConfirm)) {
            colorConfirm = "#ff3874f5";
        }
        TimePickerView pvTime = new TimePickerBuilder(getCurrentActivity(), listener)
                .setTitleText(title)
                .setDate(dataValue)
//                .setTitleSize(18)
                .setCancelColor(Color.parseColor(colorCancel))
                .setTitleColor(Color.BLACK)
                .setSubmitColor(Color.parseColor(colorConfirm))
                .setLineSpacingMultiplier(1.6f)
                .setRangDate(minValue, maxValue)
                .setType(type)
//                .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
                .addOnCancelClickListener(view -> {
//                        Log.i("pvTime", "onCancelClickListener");
                })
                .build();

        pvTime.show();
    }

    static class DataBean {
        //{ column: 2, currentKey: vm.key, pickerTitle: vm.title, pickerVisible: true, pickerData, pickerValue }
        Integer column;
        String currentKey;
        String pickerTitle;
        Boolean pickerVisible;
        List<LocationBean> pickerData;
        List<String> pickerValue;
        String colorConfirm;
        String colorCancel;
    }

    static class LocationBean implements IPickerViewData {
        String label;
        String value;
        List<LocationBean> children;

        @Override
        public String getPickerViewText() {
            return this.label;
        }
    }

    private void showLocationPicker(int level, String title, List options1Items,
                                    List<List<String>> options2Items, List<List<List<String>>> options3Items,
                                    int options1, int options2, int options3, String colorConfirm, String colorCancel,
                                    OnOptionsSelectListener listener) {


        if (TextUtils.isEmpty(colorConfirm)) {
            colorConfirm = "#ff3874f5";
        }
        if (TextUtils.isEmpty(colorCancel)) {
            colorCancel = "#ff1f1f1f";
        }
        OptionsPickerBuilder builder = new OptionsPickerBuilder(getCurrentActivity(), listener)
                .setTitleText(title)
//                .setDividerColor(Color.BLACK)
//                .setTextColorCenter(Color.BLACK) //设置选中项文字颜色
//                .setTitleSize(18)
                .setCancelColor(Color.parseColor(colorCancel))
                .setTitleColor(Color.BLACK)
                .setSubmitColor(Color.parseColor(colorConfirm))
                .setLineSpacingMultiplier(1.6f);
//                .setContentTextSize(20);

        switch (level) {
            case 1:
                builder.setSelectOptions(options1);
                break;
            case 2:
                builder.setSelectOptions(options1, options2);
                break;
            case 3:
                builder.setSelectOptions(options1, options2, options3);
                break;
        }

        OptionsPickerView pvOptions = builder.build();

        switch (level) {
            case 1:
                pvOptions.setPicker(options1Items);//一级选择器
                break;
            case 2:
                pvOptions.setPicker(options1Items, options2Items);//二级选择器
                break;
            case 3:
                pvOptions.setPicker(options1Items, options2Items, options3Items);//三级选择器
                break;
        }

        pvOptions.show();
    }

}
