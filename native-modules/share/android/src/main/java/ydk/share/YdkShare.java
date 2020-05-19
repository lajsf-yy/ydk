package ydk.share;


import android.app.Activity;
import android.content.Context;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import ydk.annotations.YdkModule;


@YdkModule(export = true)
public class YdkShare {


    public YdkShare(Context context) {

        YdkShareSDK.init(context);
    }

    public Observable<HashMap> share(Context context, String platformType, ShareModel model) {
        return YdkShareSDK.share(context,
                platformType, model);

    }

    public Observable<HashMap> authorizeLogin(Context context, String platformType) {
        return YdkShareSDK.authorizeLogin(platformType);
    }

    public Observable<String> authorize(Activity activity, String platformType) {
        return YdkShareSDK.authorize(activity, platformType);
    }

    public Observable<List<String>> getInstallPlatforms(Context context) {
        List<String> list = YdkShareSDK.getSupportPlatforms(context);
        return Observable.just(list);
    }


}
