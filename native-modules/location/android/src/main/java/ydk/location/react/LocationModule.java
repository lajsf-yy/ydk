package ydk.location.react;

import android.text.TextUtils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import javax.annotation.Nonnull;

import ydk.location.LocationHelper;

public class LocationModule extends ReactContextBaseJavaModule {

    private final String NAME = "YdkLocationModule";

    public LocationModule(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void getCurrentLocation(Promise promise) {
        LocationHelper.getInstance().startLocation(getCurrentActivity(), new LocationHelper.LocationCallback() {
            @Override
            public void onError(Exception msg) {
                promise.reject(msg);
            }

            @Override
            public void onResult(double longitude, double latitude, String provinceName, String cityName, String regionName, String addressName, String name, String cityCode, String adCode) {

                try {
                    WritableMap map = Arguments.createMap();
                    map.putDouble("longitude", longitude);
                    map.putDouble("latitude", latitude);
                    map.putString("provinceName", provinceName);
                    map.putString("cityName", cityName);
                    map.putString("regionName", regionName);
                    map.putString("addressName", addressName);
                    map.putString("name", name);
                    map.putString("cityCode", cityCode);
                    map.putString("adCode", adCode);
//                    if (!TextUtils.isEmpty(adCode)) {
//                        int length = adCode.length();
//                        String newadCode = "";
//                        if (length >= 12) {
//                            newadCode = adCode.substring(0, 12);
//                        } else {
//                            int apend = 12 - length;
//                            while (apend > 0) {
//                                adCode += "0";
//                                apend--;
//                            }
//                            newadCode = adCode;
//                        }
//                        map.putString("adCode", newadCode);
//                    }
                    promise.resolve(map);
                } catch (Exception e) {
                    e.printStackTrace();
                    promise.reject(e);
                }
            }
        });
    }

}
