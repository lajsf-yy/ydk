package ydk.mqtt.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import ydk.mqtt.engine.observer.IMObserver;
import ydk.mqtt.engine.service.IMServer;

public class MqttServerProxy {
    /**
     * 观察者缓存
     */
    private static Map<Class, IMObserver> iMObserverMap = new HashMap();
    /**
     * 服务缓存
     */
    private static Map<Class, IMServer> iMServerMap = new HashMap();


    /**
     * 获取观察者的代理
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T extends IMObserver> T getIMObserver(final Class<T> clazz) {

        Class<?>[] interfaces = new Class[1];

        interfaces[0] = clazz;

        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


                return method.invoke(getMapObserver(clazz), args);
            }
        });
    }

    private static IMObserver getMapObserver(Class clazz) {

        IMObserver mqttObserver = iMObserverMap.get(clazz);

        if (mqttObserver != null) {
            return mqttObserver;
        }
        //获取注解
        ObserverAnnotation annotation = (ObserverAnnotation) clazz.getAnnotation(ObserverAnnotation.class);

        Class<IMObserver> clazzM = annotation.clazz();

        try {
            IMObserver newMqttObserver = clazzM.newInstance();
            iMObserverMap.put(clazz, newMqttObserver);
            return newMqttObserver;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        throw new IllegalArgumentException("IMObserver 反射异常");
    }


    /**
     * 获取服务的代理
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T extends IMServer> T getIMServer(final Class<T> clazz) {

        Class<?>[] interfaces = new Class[1];

        interfaces[0] = clazz;

        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


                return method.invoke(getMapServer(clazz), args);
            }
        });
    }

    private static IMServer getMapServer(Class clazz) {

        IMServer imServer = iMServerMap.get(clazz);

        if (imServer != null) {
            return imServer;
        }
        //获取注解
        ServerAnnotation annotation = (ServerAnnotation) clazz.getAnnotation(ServerAnnotation.class);

        Class<IMServer> clazzM = annotation.clazz();

        try {
            IMServer newIMServer = clazzM.newInstance();
            iMServerMap.put(clazz, newIMServer);
            return newIMServer;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        throw new IllegalArgumentException("IMServer 反射异常");
    }

}
