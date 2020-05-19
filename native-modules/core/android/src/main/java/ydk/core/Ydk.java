package ydk.core;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ydk.annotations.YdkConfigNode;
import ydk.annotations.YdkModule;
import ydk.core.activityresult.RxActivityResult;
import ydk.core.permissions.Permission;
import ydk.core.permissions.RxPermissions;

public class Ydk {
    private static Application application;
    static HashMap<String, Object> modules = new HashMap<>();
    private static boolean setup = false;

    public static void setup(Application application, String ydkConfig) {
        Ydk.application = application;
        Ydk.setup = true;
        YdkConfigManager.setup(ydkConfig);
        RxActivityResult.register(application);

    }

    public static Application getApplicationContext() {
        if (!setup) {
            throw new IllegalArgumentException("应用 没有调用 setup(Application application, String ydkConfig) 方法");
        }
        return application;
    }

    public static void onActivityResult(Activity Activity, int requestCode, int resultCode, Intent data) {
        for (Map.Entry<?, ?> entry : modules.entrySet()) {
            if (entry.getValue() instanceof OnActivityResult) {
                ((OnActivityResult) entry.getValue()).onActivityResult(Activity, requestCode, resultCode, data);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getModule(Class<T> cls) {
        String className = cls.getName();
        if (modules.containsKey(className)) {
            return (T) modules.get(className);
        }
        Constructor constructor = cls.getConstructors()[0];

        Class<?>[] paramClasses = constructor.getParameterTypes();
        Object[] paramsObjects = new Object[paramClasses.length];
        for (int i = 0; i < paramClasses.length; i++) {
            Class<?> paramClass = paramClasses[i];
            if (paramClass.isAssignableFrom(Application.class) || paramClass.isAssignableFrom(Context.class)) {
                paramsObjects[i] = application;
                continue;
            }

            YdkConfigNode ydkConfigNode = paramClass.getAnnotation(YdkConfigNode.class);
            if (ydkConfigNode != null) {
                paramsObjects[i] = YdkConfigManager.getConfig(paramClass, ydkConfigNode.name());
                continue;
            }
            //处理YdkConfigValue


            YdkModule ydkModule = paramClass.getAnnotation(YdkModule.class);
            if (ydkModule != null) {
                paramsObjects[i] = getModule(paramClass);
            }
        }
        T module = null;
        try {
            module = (T) constructor.newInstance(paramsObjects);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        modules.put(className, module);
        return module;
    }


    public static Observable<Boolean> getPermissions(Activity activity, final String... permissions) {
        //单权限请求在vivo上会出现不返回情况
        String[] realPermission;
        if (permissions.length == 1) {
            realPermission = new String[]{permissions[0], Manifest.permission.WRITE_EXTERNAL_STORAGE};
        } else {
            realPermission = permissions;
        }
        return Observable.just(permissions)
                .flatMap(per -> Observable.just(new RxPermissions(activity)))
                .flatMap(rxPermissions -> rxPermissions.request(realPermission))
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<List<Permission>> getPermissionsInfo(Activity activity, final String... permissions) {
        //单权限请求在vivo上会出现不返回情况
        String[] realPermission;
        if (permissions.length == 1) {
            realPermission = new String[]{permissions[0], Manifest.permission.WRITE_EXTERNAL_STORAGE};
        } else {
            realPermission = permissions;
        }
        return Observable.just(permissions)
                .flatMap(per -> Observable.just(new RxPermissions(activity)))
                .flatMap(rxPermissions -> rxPermissions.requestEach(realPermission))
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    private static YdkEventEmitter eventEmitter;

    public static YdkEventEmitter getEventEmitter() {
        return eventEmitter;
    }

    public static void setEventEmitter(YdkEventEmitter emitter) {
        eventEmitter = emitter;
    }

}
