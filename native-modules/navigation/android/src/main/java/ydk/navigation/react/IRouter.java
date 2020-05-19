package ydk.navigation.react;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReadableMap;

public interface IRouter {
    void push(String componentName, String componentId, Bundle options);
    void pop(String componentId);
    void popTo(String componentId);
    void popToRoot();
    void popToRoot(Integer tabIndex);
    void showModal(String componentName, @Nullable ReadableMap options);
    void dismissModal();
    void setResult(String componentId, String targetComponentId, ReadableMap data);
}
