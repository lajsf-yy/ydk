package ydk.video;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.video.VideoListener;

import java.util.List;

/**
 * Created by Gsm on 2018/6/6.
 */
public class VideoPlayLayout extends FrameLayout {
    private Activity mActivity;
    private TextureView textureView;
    private ComponentListener componentListener;
    private AspectRatioFrameLayout contentFrame;
    private View shutterView;
    private FrameLayout overlayFrameLayout;
    private SubtitleView subtitleView;
    private ExoPlayer player;
    private int textureViewRotation;
    private boolean isAttachedToWindow = false;
    private VideoPlayListener playListener;
    private Timeline.Window window;
    private boolean previousWhenReady = false;

    public VideoPlayLayout(@NonNull Activity activity) {
        this(activity, null);
    }

    public VideoPlayLayout(@NonNull Activity activity, @Nullable AttributeSet attrs) {
        this(activity, attrs, 0);
    }

    public VideoPlayLayout(@NonNull Activity activity, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(activity, attrs, defStyleAttr);
        this.mActivity = activity;
        setBackgroundColor(Color.BLACK);
        init();
    }

    public void setPlayListener(VideoPlayListener playListener) {
        this.playListener = playListener;
    }

    private void init() {
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        LayoutInflater.from(mActivity).inflate(R.layout.layout_video_play, this);
        componentListener = new ComponentListener();
        shutterView = findViewById(R.id.video_shutter);
        contentFrame = findViewById(R.id.video_content_frame);
        if (contentFrame != null) {
            contentFrame.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);//设置框高自动变化以适应宽高比
            textureView = new TextureView(mActivity);
            textureView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
            contentFrame.addView(textureView, 0);
        }
        overlayFrameLayout = findViewById(R.id.video_overlay);
        subtitleView = findViewById(R.id.video_subtitles);//字幕
        if (subtitleView != null) {
            subtitleView.setUserDefaultStyle();
            subtitleView.setUserDefaultTextSize();
        }
        window = new Timeline.Window();
        overlayFrameLayout.setKeepScreenOn(true);
    }

    private final Runnable measureAndLayout = () -> {
        measure(
                MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
        layout(getLeft(), getTop(), getRight(), getBottom());
    };

    public void setPlayer(ExoPlayer player) {
        if (this.player == player) return;
        if (this.player != null) {
            this.player.removeListener(componentListener);
            Player.VideoComponent oldVideoComponent = this.player.getVideoComponent();
            if (oldVideoComponent != null) {
                oldVideoComponent.clearVideoTextureView(textureView);
                oldVideoComponent.removeVideoListener(componentListener);
            }
            Player.TextComponent oldTextComponent = this.player.getTextComponent();
            if (oldTextComponent != null) {
                oldTextComponent.removeTextOutput(componentListener);
            }
        }
        this.player = player;
        if (shutterView != null) shutterView.setVisibility(View.VISIBLE);
        if (subtitleView != null) subtitleView.setCues(null);
        if (player != null) {
            Player.VideoComponent newVideoComponent = player.getVideoComponent();
            if (newVideoComponent != null) {
                newVideoComponent.setVideoTextureView(textureView);
                newVideoComponent.addVideoListener(componentListener);
            }
            Player.TextComponent newTextComponent = player.getTextComponent();
            if (newTextComponent != null) {
                newTextComponent.addTextOutput(componentListener);
            }
            player.addListener(componentListener);
        }
    }

    private boolean isPlayingAd() {
        return player != null && player.isPlayingAd() && player.getPlayWhenReady();
    }

    public boolean isPlaying() {
        return player != null
                && player.getPlaybackState() != Player.STATE_ENDED
                && player.getPlaybackState() != Player.STATE_IDLE
                && player.getPlayWhenReady();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
        onDestroyed();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (player != null && player.isPlayingAd()) {
            overlayFrameLayout.requestFocus();
        }
        return super.dispatchKeyEvent(event);
    }


    private static void applyTextureViewRotation(TextureView textureView, int textureViewRotation) {
        int textureViewWidth = textureView.getWidth();
        int textureViewHeight = textureView.getHeight();
        if (textureViewWidth == 0 || textureViewHeight == 0 || textureViewRotation == 0) {
            textureView.setTransform(null);
        } else {
            Matrix transformMatrix = new Matrix();
            int pivotX = textureViewWidth / 2;
            int pivotY = textureViewHeight / 2;
            transformMatrix.postRotate(textureViewRotation, pivotX, pivotY);
            RectF originalTextureRect = new RectF(0, 0, textureViewWidth, textureViewHeight);
            RectF rotatedTextureRect = new RectF();
            transformMatrix.mapRect(rotatedTextureRect, originalTextureRect);
            transformMatrix.postScale(textureViewWidth / rotatedTextureRect.width(), textureViewHeight / rotatedTextureRect.height(), pivotX, pivotY);
            textureView.setTransform(transformMatrix);
        }
    }

    public long getCurrentPosition() {
        if (player != null) {
            return player.getCurrentPosition();
        }
        return 0;
    }

    public void onResumed() {
        if (player != null && previousWhenReady) {
            player.setPlayWhenReady(true);
        }
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void onPaused() {
        if (player != null) {
            previousWhenReady = player.getPlayWhenReady();
            player.setPlayWhenReady(false);
        }
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void onDestroyed() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.release();
        }
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void seekTo(long positionMs) {
        if (player != null) {
            player.seekTo(player.getCurrentWindowIndex(), positionMs);
        }
    }

    public void startPlay() {
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    public void pausePlay() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    private final Runnable updateProgressAction = this::updateProgress;

    private void updateProgress() {
        if (!isAttachedToWindow || player == null || !player.getPlayWhenReady() || player.getPlaybackState() == Player.STATE_ENDED)
            return;
        long position = player.getCurrentPosition();
        int index = player.getCurrentWindowIndex();
        int windowCount = player.getCurrentTimeline().getWindowCount();
        if (index > windowCount - 1) return;
        player.getCurrentTimeline().getWindow(index, window);
        long duration = C.usToMs(window.durationUs);
        if (playListener != null) playListener.progress(position, duration);
        removeCallbacks(updateProgressAction);
        int playbackState = player == null ? Player.STATE_IDLE : player.getPlaybackState();
        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
            long delayMs;
            if (player.getPlayWhenReady() && playbackState == Player.STATE_READY) {
                float playbackSpeed = player.getPlaybackParameters().speed;
                if (playbackSpeed <= 0.1f) {
                    delayMs = 500;
                } else if (playbackSpeed <= 5f) {
                    long mediaTimeUpdatePeriodMs = 1000 / Math.max(1, Math.round(1 / playbackSpeed));
                    long mediaTimeDelayMs = mediaTimeUpdatePeriodMs - (position % mediaTimeUpdatePeriodMs);
                    if (mediaTimeDelayMs < (mediaTimeUpdatePeriodMs / 5)) {
                        mediaTimeDelayMs += mediaTimeUpdatePeriodMs;
                    }
                    delayMs =
                            playbackSpeed == 1 ? mediaTimeDelayMs : (long) (mediaTimeDelayMs / playbackSpeed);
                } else {
                    delayMs = 200;
                }
            } else {
                delayMs = 500;
            }
            postDelayed(updateProgressAction, delayMs);
        }
    }


    private class ComponentListener implements VideoListener, TextOutput, Player.EventListener, OnLayoutChangeListener {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
            updateProgress();
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (isPlayingAd()) {
                return;
            }
            if (playListener == null) return;
            updateProgress();
            switch (playbackState) {
                case Player.STATE_IDLE:
                    playListener.stalled();
                    break;
                case Player.STATE_BUFFERING:
                    playListener.buffering();
                    break;
                case Player.STATE_READY:
                    playListener.ready();
                    break;
                case Player.STATE_ENDED:
                    playListener.end();
                    break;
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            if (playListener != null && player.getBufferedPosition() == 0) {
                playListener.error(new Exception());
            }

        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            updateProgress();
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {

        }

        @Override
        public void onCues(List<Cue> cues) {
            if (subtitleView != null) subtitleView.onCues(cues);
        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            if (contentFrame == null) return;
            float videoAspectRatio = (height == 0 || width == 0) ? 1 : (width * pixelWidthHeightRatio) / height;
            if (unappliedRotationDegrees == 90 || unappliedRotationDegrees == 270) {
                videoAspectRatio = 1 / videoAspectRatio;
            }
            if (textureViewRotation != 0) {
                textureView.removeOnLayoutChangeListener(this);
            }
            textureViewRotation = unappliedRotationDegrees;
            if (textureViewRotation != 0) {
                textureView.addOnLayoutChangeListener(this);
            }
            contentFrame.setAspectRatio(videoAspectRatio);
            post(measureAndLayout);
        }

        @Override
        public void onRenderedFirstFrame() {
            if (shutterView != null) {
                shutterView.setVisibility(INVISIBLE);
            }
        }

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            applyTextureViewRotation((TextureView) v, textureViewRotation);
        }
    }

}
