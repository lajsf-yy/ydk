package ydk.video;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;

public class YdkVideo {
    /**
     * 打开原生播放页面
     *
     * @param context
     * @param url
     */
    public static void nativePlay(Context context, String url) {

        if (TextUtils.isEmpty(url)) {
            return;
        }
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        Intent mediaIntent = new Intent();
        // Intent mediaIntent = new Intent(context, GsyVideoPlayerActivity.class);
        File file = new File(url);
        boolean isLocalFile = false;
        try {
            if (file.exists()) {
                isLocalFile = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isLocalFile) {
            Uri uri = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                String packageName = context.getPackageName();
                String fileProvider = String.format("%s%s", packageName, ".fileProvider");
                uri = FileProvider.getUriForFile(context,
                        fileProvider, file);
                mediaIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(file);
            }
            mediaIntent.setDataAndType(uri, mimeType);
        } else {
            mediaIntent.setDataAndType(Uri.parse(url), mimeType);
        }
        context.startActivity(mediaIntent);

    }
}
