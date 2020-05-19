package ydk.ui.pickview.react;

public class DatePickerConfig {
    /**
     * title
     */
    private String pickerTitle;
    /**
     * 当前值
     */
    private String dateValue;
    /**
     * 最小值
     */
    private String minValue;
    /**
     * 最大值
     */
    private String maxValue;

    private String timeFormat;

    private String colorConfirm;

    private String colorCancel;

    public String getPickerTitle() {
        return pickerTitle;
    }

    public String getDateValue() {
        return dateValue;
    }

    public String getMinValue() {
        return minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public String getColorConfirm() {
        return colorConfirm;
    }

    public String getColorCancel() {
        return colorCancel;
    }
}
