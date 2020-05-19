package ydk.location;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationHelper {

    private LocationHelper(){}

    private static LocationHelper helper;

    public static LocationHelper getInstance() {
        synchronized (LocationHelper.class) {
            if (null == helper) {
                helper = new LocationHelper();
            }
        }
        return helper;
    }

    public interface LocationCallback{
        void onError(Exception msg);

        void onResult(double longitude, double latitude, String provinceName,
                      String cityName, String regionName, String addressName,
                      String name, String cityCode, String adCode);
    }

    //声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient = null;

    public void startLocation(Context context, LocationCallback callback) {
        if (null == context) {
            callback.onError(new RuntimeException("context is null"));
            return;
        }
        startLocation(context, null, callback);
    }

    public void startLocation(Context context, AMapLocationClientOption option, LocationCallback callback) {
        //初始化定位
        mLocationClient = new AMapLocationClient(context.getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {

                if (amapLocation != null) {
                    if (amapLocation.getErrorCode() == 0) {
                        //可在其中解析amapLocation获取相应内容。
//                        parseLocation(amapLocation);
                        String adCode = amapLocation.getAdCode();
                        String newadCode = "";
                        if (!TextUtils.isEmpty(adCode)) {
                            int length = adCode.length();
                            if (length >= 12) {
                                newadCode = adCode.substring(0, 12);
                            } else {
                                int apend = 12 - length;
                                while (apend > 0) {
                                    adCode += "0";
                                    apend--;
                                }
                                newadCode = adCode;
                            }
                        }
                        callback.onResult(amapLocation.getLongitude(), amapLocation.getLatitude(),
                                amapLocation.getProvince(), amapLocation.getCity(),
                                amapLocation.getDistrict(), amapLocation.getAddress(),
                                amapLocation.getPoiName(), amapLocation.getCityCode(),
                                newadCode);
                    }else {
                        //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                        Log.e("AmapError","location Error, ErrCode:"
                                + amapLocation.getErrorCode() + ", errInfo:"
                                + amapLocation.getErrorInfo());
                        String msg = "定位失败, ErrCode:"
                                + amapLocation.getErrorCode();
                        if (amapLocation.getErrorCode() == 12) {
                            msg = "定位失败，请开启定位权限";
                        } else if (amapLocation.getErrorCode() == 13) {
                            msg = "定位失败，请接入网络并开启定位权限";
                        }
                        callback.onError(new Exception(msg));
                    }
                }

            }
        });

        if (null == option) {
            option = prepareDefaultOptions();
        }

        mLocationClient.setLocationOption(option);

        //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
        mLocationClient.stopLocation();

        mLocationClient.startLocation();

    }

    public AMapLocationClientOption prepareOptions(boolean highAccuracy, boolean locationOnce,
                                                   boolean withAddrInfo, boolean enableCache) {
        //声明AMapLocationClientOption对象
        AMapLocationClientOption locationOption = new AMapLocationClientOption();

//        /**
//         * 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
//         */
//        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);

        if (highAccuracy) {
            //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        } else {
            //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        }

        if (locationOnce) {
            //获取一次定位结果：
            //该方法默认为false。
            locationOption.setOnceLocation(true);

            //获取最近3s内精度最高的一次定位结果：
            //设置setOnceLocationLatest(boolean b)接口为true，
            // 启动定位时SDK会返回最近3s内精度最高的一次定位结果。
            // 如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
            locationOption.setOnceLocationLatest(true);

        } else {
            locationOption.setOnceLocation(false);
            //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
            locationOption.setInterval(1000);
        }

        //设置是否返回地址信息（默认返回地址信息）
        locationOption.setNeedAddress(withAddrInfo);

        //关闭缓存机制
        locationOption.setLocationCacheEnable(enableCache);

        return locationOption;
    }

    private AMapLocationClientOption prepareDefaultOptions() {
        //声明AMapLocationClientOption对象
        AMapLocationClientOption locationOption = new AMapLocationClientOption();

//        /**
//         * 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
//         */
//        option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);

        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//        //设置定位模式为AMapLocationMode.Battery_Saving，低功耗模式。
//        mLocationOption.setLocationMode(AMapLocationMode.Battery_Saving);

        //获取一次定位结果：
        //该方法默认为false。
        locationOption.setOnceLocation(true);

//        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
//        mLocationOption.setInterval(1000);//with: mLocationOption.setOnceLocation(false);

        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，
        // 启动定位时SDK会返回最近3s内精度最高的一次定位结果。
        // 如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        locationOption.setOnceLocationLatest(true);

        //设置是否返回地址信息（默认返回地址信息）
        locationOption.setNeedAddress(true);
        //关闭缓存机制
        locationOption.setLocationCacheEnable(false);
        return locationOption;
    }

    public void stopLocation() {
        if (null == mLocationClient) {
            return;
        }
        mLocationClient.stopLocation();//停止定位后，本地定位服务并不会被销毁
        shutdownLocation();
    }


    private void shutdownLocation() {
        mLocationClient.onDestroy();//销毁定位客户端，同时销毁本地定位服务。
    }

    private void parseLocation(AMapLocation amapLocation) {
        amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
        amapLocation.getLatitude();//获取纬度
        amapLocation.getLongitude();//获取经度
        amapLocation.getAccuracy();//获取精度信息
        amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
        amapLocation.getCountry();//国家信息
        amapLocation.getProvince();//省信息
        amapLocation.getCity();//城市信息
        amapLocation.getDistrict();//城区信息
        amapLocation.getStreet();//街道信息
        amapLocation.getStreetNum();//街道门牌号信息
        amapLocation.getCityCode();//城市编码
        amapLocation.getAdCode();//地区编码
        amapLocation.getAoiName();//获取当前定位点的AOI信息
        amapLocation.getBuildingId();//获取当前室内定位的建筑物Id
        amapLocation.getFloor();//获取当前室内定位的楼层
        amapLocation.getGpsAccuracyStatus();//获取GPS的当前状态
        //获取定位时间
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(amapLocation.getTime());
        df.format(date);
    }

}
