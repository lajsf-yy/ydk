package ydk.navigation.react;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class NativePageLinker {

    private static final String needLoginPages = "SelfQuestionScreen,";
    private String currentNeedLoginPage = "";

    public static HashMap<String, Boolean> needLoginPagesStatus = new HashMap<>();
    public static HashMap<String, Uri> needLoginPagesPaths = new HashMap<>();

    private static NativePageLinker sInstance;

    public static NativePageLinker getInstance() {
        synchronized (NativePageLinker.class) {
            if (null == sInstance) {
                sInstance = new NativePageLinker();
            }
        }
        return sInstance;
    }

    private NativePageLinker() {
    }

//    public void open(Context context, String url) {
//        if (TextUtils.isEmpty(url)) {
//            return;
//        }
//
//        Uri uri = Uri.parse(url);
//        open(context, uri);
//    }

//    public void open(Context context, Uri uri) {
//
//        if (!checklegal(uri)) {
//            return;
//        }
//
//        prepareConfig(context);
//
//        handleJump(context, uri);
//
//    }

    public boolean isNativePages(Activity context, String pageName) {
        prepareConfig(context);
        if (config == null) {
            return false;
        }
        Map<String, String> valueMap = (Map) config.get("/" + pageName);
        if (null == valueMap) {
            return false;
        }
        return true;
    }


    public void directOpen(Activity context, String pageName, Bundle args) {
        prepareConfig(context);
        if (config == null) {
            return;
        }
        Map<String, String> valueMap = (Map) config.get("/" + pageName);
        if (null == valueMap) {
            return;
        }

        Intent intent = new Intent();
        intent.setAction(valueMap.get("action"));//use action to start activity
        if (null != args) {
            intent.putExtras(args);
        }

        //  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            // context.startActivity(intent);
            context.startActivityForResult(intent, 1001);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void handleJump(Context context, Uri uri) {
//        String path = uri.getPath();
//
//        if (!DAOManager.getInstance().getLoginServ().isLogin()) {
//            if (path!=null) {
//                String pageName = path.replace("/", "");
//                if (needLoginPages.contains(pageName)) {
//                    needLoginPagesStatus.put(pageName, true);
//                    needLoginPagesPaths.put(pageName, uri);
//                    currentNeedLoginPage = pageName;
//                    RNUtil.openRNModal(context, "Login", null);
//                    return;
//                }
//            }
//        }
//
//
//        Intent intent = new Intent();
//
//        if (config.containsKey(path)) {
//            Map<String, String> valueMap = (Map) config.get(path);
//            intent.setAction(valueMap.get("action"));//use action to start activity
//            if (valueMap.containsKey("key")) {
//                String keyStr = valueMap.get("key");
//                if (!TextUtils.isEmpty(keyStr)) {
//                    String[] keys;
//                    if (!keyStr.contains(",")) {
//                        keys = new String[]{keyStr};
//                    } else {
//                        keys = keyStr.split(",");
//                    }
//                    for (String key : keys) {
//                        String param = uri.getQueryParameter(key);
//                        if (null == param) continue;
//                        try {
//                            Double kid = Double.parseDouble(param);
//                            intent.putExtra(key, kid);
//                        } catch (NumberFormatException e) {
//                            e.printStackTrace();
//                            intent.putExtra(key, param);
//                        }
//                    }
//                }
//
//            }
//        } else {
//            Set<String> keys = uri.getQueryParameterNames();
//            for (String key : keys) {
//                intent.putExtra(key, uri.getQueryParameter(key));
//            }
//            intent.setClassName(context.getPackageName(), "com.nutritionplan.react.RNPageActivity");
//            intent.putExtra(RNUtil.KEY_COMPONENT, uri.getPath().replace("/", ""));
//            intent.putExtra(RNUtil.KEY_ID_COMPONENT, System.currentTimeMillis() + "");
//        }
//
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        try {
//            context.startActivity(intent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    private Map<String, Object> config = null;

    private void prepareConfig(Context context) {
        if (null != config) {
            return;
        }
        try (InputStreamReader linkReader =
                     new InputStreamReader(context
                             .getAssets().open("pagelink.json"))) {

            config = new Gson().fromJson(linkReader, HashMap.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean checklegal(Uri uri) {
        if (null == uri) {
            return false;
        }
        String schema = uri.getScheme();
        if (TextUtils.isEmpty(schema) || !"nutritionplan".equalsIgnoreCase(schema)) {
            return false;
        }
        String host = uri.getHost();
//        if (TextUtils.isEmpty(host)
////                || !("react-native".equalsIgnoreCase(host)
////                    || "native".equalsIgnoreCase(host))) {
////            return false;
////        }
        if (TextUtils.isEmpty(host) || !"route".equalsIgnoreCase(host)) {
            return false;
        }
        String path = uri.getPath();
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        return true;
    }

    public String getCurrentNeedLoginPage() {
        return currentNeedLoginPage;
    }

    public void setCurrentNeedLoginPage(String currentNeedLoginPage) {
        this.currentNeedLoginPage = currentNeedLoginPage;
    }
}
