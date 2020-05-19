package ydk.video;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;

/**
 * Created by Gsm on 2018/6/28.
 */
public class VideoPlay {
    private static DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private Activity mActivity;
    private SimpleExoPlayer player;
    private ExoCacheDataSourceFactory dataSourceFactory;
    private MediaSource mediaSource;
    private VideoPlayLayout playLayout;
    private NetWorkBroadcastReceiver networkReceiver;
    private PlayerLifecycleCallbacks playLifecycleCallbacks;
    private boolean hasRegister = false;

    public VideoPlay(Activity activity) {
        mActivity = activity;
    }

    public void onDestroy() {
        if (playLayout != null) {
            playLayout.onDestroyed();
        }
        if (playLifecycleCallbacks != null) {
            mActivity.getApplication().unregisterActivityLifecycleCallbacks(playLifecycleCallbacks);
        }
        if (networkReceiver != null) {
            mActivity.unregisterReceiver(networkReceiver);
        }
    }

    public void setVideoUrl(VideoPlayLayout playLayout, String url) {
        this.playLayout = playLayout;
        if (!TextUtils.isEmpty(url)) {
            if (player == null) {
                DefaultTrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(BANDWIDTH_METER));
                player = ExoPlayerFactory.newSimpleInstance(mActivity, trackSelector);
                dataSourceFactory = new ExoCacheDataSourceFactory(mActivity, BANDWIDTH_METER);
            }
            this.playLayout.setPlayer(player);
            mediaSource = buildMediaSource(Uri.parse(url));
            player.prepare(mediaSource);
            player.setPlayWhenReady(false);
            register();
        } else if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    private MediaSource buildMediaSource(Uri uri) {
        int type = Util.inferContentType(uri.getLastPathSegment());
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(dataSourceFactory), new ExoCacheDataSourceFactory(mActivity, null)).createMediaSource(uri);
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(dataSourceFactory), new ExoCacheDataSourceFactory(mActivity, null)).createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);

        }
        return null;
    }

    private void register() {
        if (hasRegister) return;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        networkReceiver = new NetWorkBroadcastReceiver();
        mActivity.registerReceiver(networkReceiver, filter);
        playLifecycleCallbacks = new PlayerLifecycleCallbacks();
        mActivity.getApplication().registerActivityLifecycleCallbacks(playLifecycleCallbacks);
        hasRegister = true;
    }

    private class NetWorkBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                if (player.getPlaybackState() == Player.STATE_IDLE) {
                    player.prepare(mediaSource, false, true);
                    player.setPlayWhenReady(true);
                }
            }
        }
    }

    private class PlayerLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            if (mActivity == activity) playLayout.onResumed();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            if (mActivity == activity) playLayout.onPaused();
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            if (mActivity == activity) {
                onDestroy();
            }
        }
    }
}
