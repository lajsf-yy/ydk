package ydk.react.image;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.views.image.ReactImageManager;
import com.facebook.react.views.image.ReactImageView;
import javax.annotation.Nullable;

public class YdkImageViewManager extends ReactImageManager {

    private static final String REACT_CLASS = "YdkImageView";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public YdkImageView createViewInstance(ThemedReactContext reactContext) {
        return new YdkImageView(reactContext, getDraweeControllerBuilder(), null, getCallerContext());
    }

    // In JS this is Image.props.defaultSource
    @ReactProp(name = "defaultSrc")
    public void setDefaultSource(ReactImageView view, @Nullable String source) {
        view.setDefaultSource(source);

    }

}
