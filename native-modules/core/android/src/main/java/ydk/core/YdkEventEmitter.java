package ydk.core;



public interface YdkEventEmitter {

    void emit(String eventName, Object data);

}
