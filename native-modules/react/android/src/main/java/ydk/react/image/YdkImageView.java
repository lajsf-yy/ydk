package ydk.react.image;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.facebook.drawee.controller.AbstractDraweeControllerBuilder;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.views.image.GlobalImageLoadListener;
import com.facebook.react.views.image.ReactImageView;
import com.facebook.react.views.imagehelper.ResourceDrawableIdHelper;


import javax.annotation.Nullable;

class YdkImageView extends ReactImageView {
    private @Nullable
    Drawable mDefaultImageDrawable;
    public YdkImageView(Context context, AbstractDraweeControllerBuilder draweeControllerBuilder, @Nullable GlobalImageLoadListener globalImageLoadListener, @Nullable Object callerContext) {
        super(context, draweeControllerBuilder, globalImageLoadListener, callerContext);
    }

    public void setDefaultSource(@Nullable String name) {
        mDefaultImageDrawable = ResourceDrawableIdHelper.getInstance().getResourceDrawable(getContext(), name);

    }

    @Override
    public void maybeUpdateView() {
        super.maybeUpdateView();
        GenericDraweeHierarchy hierarchy = getHierarchy();
        if (mDefaultImageDrawable != null) {
            hierarchy.setPlaceholderImage(mDefaultImageDrawable, ScalingUtils.ScaleType.CENTER);
        }
    }
    

}
