package ydk.audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;


import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ydk.core.Ydk;

/**
 * Created by Kyz on 2018/10/25.
 */
public class PlayerManager {

    private static PlayerManager manager;


    /**
     * 播放器,
     */
    private MediaPlayer mMediaPlayer;

    /**
     * 进度缓存，播放回调 key 为tagID，vaule 为这个 tagId 下的所有音乐的进度缓存
     */
    private AudioModel mAudioModel = null;

    /**
     * 当前正在播放的音乐的记录
     */
    private AudioConfig mRecordConfig = new AudioConfig();


    //定时器对象
    private Disposable mTimeDisposable;

    private Disposable mPlayDisposable;

    private PlayerManager() {
    }

    public static PlayerManager getInstance() {

        if (manager == null) {
            manager = new PlayerManager();
        }
        return manager;
    }

//    PlayerManager.AudioPalyListener mAudioPalyListener = (action, audioModel) -> {
//        if (audioModel == null) {
//            return;
//        }
//        if (mPalyListenerCall == null) {
//            return;
//        }
//        if (AudioModel.COMPLETION.equals(action)) {
//            //解决完成后，进度不准确的问题
//            Log.e("hh", "###发送一次完成的事件,解决进度不准确的问题");
//            audioModel.setProgress(audioModel.getDuration());
//            mPalyListenerCall.onCallBack(AudioModel.PROGRESS, audioModel);
//        }
//        mPalyListenerCall.onCallBack(action, audioModel);
//
//
//    };


    /**
     * 请求权限
     */
    private void requestAudioFocus(ObservableEmitter<AudioModelEmitter> emitter) {

        AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = focusChange -> {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS | AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:


                    if (mAudioModel != null && !emitter.isDisposed()) {
                        emitter.onNext(new AudioModelEmitter(Constant.BACKSTALLED, mAudioModel));
                    }
                    if (mMediaPlayer != null) {
                        mMediaPlayer.pause();
                    }

                    //   mAudioManager.abandonAudioFocus(mAudioFocusChange);
                    break;
            }
        };


        AudioManager audioManager = (AudioManager) Ydk.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        //申请焦点
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 征对于Android 8.0+
            AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setOnAudioFocusChangeListener(onAudioFocusChangeListener).build();
            audioFocusRequest.acceptsDelayedFocusGain();
            result = audioManager.requestAudioFocus(audioFocusRequest);

        } else { // 小于Android 8.0
            result = audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.e("hh", "AudioManager请求焦点失败");
        }
    }

    /**
     * 判断URL 类型
     *
     * @param path
     * @return
     */
    private boolean isNetUrl(String path) {
        return path.startsWith("http") || path.startsWith("https");
    }

    /**
     * 本地文件
     *
     * @param path
     * @return
     */
    private boolean isLocalFile(String path) {

        return path.startsWith("file://") || path.startsWith("/storage");
    }

    /**
     * RN assets 目录下的文件，在正式包，文件位于raw 目录下面
     *
     * @param path
     * @return
     */
    private boolean isRNRaw(String path) {

        return path.startsWith("src_assets");
    }

    public void setRecordConfig(AudioConfig audioConfig) {
        mRecordConfig.setTagId(audioConfig.getTagId());
        mRecordConfig.setUrl(audioConfig.getUrl());
    }

    public void resetRecordConfig() {
        mRecordConfig.setUrl("");
        mRecordConfig.setTagId(0d);
    }


    /**
     * 获取播放器 Id
     *
     * @param tagId
     * @param url
     * @return
     */
    private String getMediaPlayId(double tagId, String url) {

        return String.format("%s%s", String.valueOf(tagId), url);
    }


    /**
     * 初始化
     *
     * @param audioConfig
     */
    private void initMediaPlayer(AudioConfig audioConfig, ObservableEmitter<AudioModelEmitter> emitter) {

        //播放器为空
        if (mMediaPlayer == null) {
            prepareMediaPlayer(audioConfig, emitter);
            return;
        }
        // RN端的播放器不是同一个对象
        if (mRecordConfig.getTagId() != audioConfig.getTagId()) {

            if (mAudioModel != null) {
                emitter.onNext(new AudioModelEmitter(Constant.BACKSTALLED, mAudioModel));
            }
            onReleaseMediaPlayer();
            prepareMediaPlayer(audioConfig, emitter);
            return;
        }
        //RN端的播放器是同一个对象，但播放的不是同一首歌曲
        if (!mRecordConfig.getUrl().equals(audioConfig.getUrl())) {
            onReleaseMediaPlayer();
            prepareMediaPlayer(audioConfig, emitter);
            return;
        }
        if (!mMediaPlayer.isPlaying()) {
            emitter.onNext(new AudioModelEmitter(Constant.PREPARED, mAudioModel));
            mMediaPlayer.start();
            startTimer(emitter);
        }

    }

    /**
     * 销毁播放器
     */
    private void onReleaseMediaPlayer() {
        if (mMediaPlayer == null) {
            return;
        }
        try {
            mMediaPlayer.stop();
        } catch (IllegalStateException e) {
        }
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    /**
     * 异步缓存
     *
     * @param audioConfig
     */
    private void prepareMediaPlayer(AudioConfig
                                            audioConfig, ObservableEmitter<AudioModelEmitter> emitter) {

        mAudioModel = new AudioModel(audioConfig.getTagId());

        setRecordConfig(audioConfig);

        mMediaPlayer = new MediaPlayer();

        MediaPlayListener mediaPlayListener = new MediaPlayListener(audioConfig.getTagId(), emitter);
        mMediaPlayer.setOnBufferingUpdateListener(mediaPlayListener);
        mMediaPlayer.setOnErrorListener(mediaPlayListener);
        mMediaPlayer.setOnCompletionListener(mediaPlayListener);
        mMediaPlayer.setOnPreparedListener(mediaPlayListener);
        mMediaPlayer.setOnSeekCompleteListener(mediaPlayListener);


        mMediaPlayer.reset();
        String url = audioConfig.getUrl();
        try {
            if (isNetUrl(url)) {
                mMediaPlayer.setDataSource(url);
            } else if (isLocalFile(url)) {
                File file = new File(url);
                url = file.getAbsolutePath();
                mMediaPlayer.setDataSource(url);
            } else if (isRNRaw(url)) {
                Context applicationContext = Ydk.getApplicationContext();
                int resID = applicationContext.getResources().getIdentifier(url, "raw", applicationContext.getPackageName());
                AssetFileDescriptor file = applicationContext.getResources().openRawResourceFd(resID);
                mMediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(),
                        file.getLength());
                mAudioModel.setPlayableDuration(100d);
                //预防raw文件获取不到播放的时长
                Uri uri = Uri.parse("android.resource://" + applicationContext.getPackageName() + "/raw/" + resID);
                long duration = getDuration(applicationContext, uri);
                if (duration != 0l) {
                    mAudioModel.setDuration(duration);
                }

            } else {
                mMediaPlayer.setDataSource(url);
            }
        } catch (IOException e) {
            Log.e("hh", "prepareMediaPlayer setDataSource error " + e.getMessage());
            emitter.onError(e);
        }
        try {
            mMediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            Log.e("hh", "prepareMediaPlayer prepareAsync error " + e.getMessage());
            emitter.onError(e);
        }

    }

    private long getDuration(Context context, Uri uri) {

        MediaMetadataRetriever retriever = null;
        long timeInMillisec = 0;
        try {
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, uri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            timeInMillisec = Long.parseLong(time);
        } catch (Exception e) {

        } finally {
            if (retriever != null) {
                retriever.release();
            }
        }
        return timeInMillisec;
    }


    /**
     * 遗传监听
     *
     * @param mediaPlayer
     */
    private void removeMediaPlayListener(MediaPlayer mediaPlayer) {
        mediaPlayer.setOnBufferingUpdateListener(null);
        mediaPlayer.setOnErrorListener(null);
        mediaPlayer.setOnCompletionListener(null);
        mediaPlayer.setOnPreparedListener(null);
        mediaPlayer.setOnSeekCompleteListener(null);
    }


    public void play(String url) {
        AudioConfig audioConfig = new AudioConfig();
        audioConfig.setTagId(System.currentTimeMillis());
        audioConfig.setUrl(url);
        play(audioConfig);
    }


    public Observable<AudioModelEmitter> play(AudioConfig audioConfig) {

        if (mPlayDisposable != null && !mPlayDisposable.isDisposed()) {
            mPlayDisposable.dispose();
            stopTimer();
        }

        return Observable.create(emitter ->

                Observable.create((ObservableOnSubscribe<AudioModelEmitter>) emitter1 -> {

                    requestAudioFocus(emitter1);

                    initMediaPlayer(audioConfig, emitter1);

                }).subscribe(new Observer<AudioModelEmitter>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mPlayDisposable = d;
                    }

                    @Override
                    public void onNext(AudioModelEmitter audioModelEmitter) {
                        String action = audioModelEmitter.action;
                        //准备好了，启动定时器
                        emitter.onNext(audioModelEmitter);
                        if (action == Constant.PREPARED) {
                            startTimer(emitter);
                        }
                        if (action == Constant.COMPLETION) {
                            stopTimer();
                            emitter.onComplete();
                        }
                        if (action == Constant.ERROR) {
                            stopTimer();
                            emitter.onComplete();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));


    }

    /**
     * 启动定时器
     *
     * @param emitter
     */
    private void startTimer(ObservableEmitter<AudioModelEmitter> emitter) {

        if (mTimeDisposable == null || mTimeDisposable.isDisposed()) {
            Observable.interval(0, 500, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Observer<Long>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            mTimeDisposable = d;
                        }

                        @Override
                        public void onNext(Long time) {
                            try {
                                AudioModel audioModel = mAudioModel;

                                if (audioModel == null) {
                                    return;
                                }
                                if (mMediaPlayer == null) {
                                    return;
                                }
                                //在播放中，回调进度
                                if (!mMediaPlayer.isPlaying()) {
                                    return;
                                }
                                int currentPosition = mMediaPlayer.getCurrentPosition();
                                audioModel.setProgress(currentPosition);
                                int duration = mMediaPlayer.getDuration();
                                if (audioModel.getDuration() == 0) {
                                    audioModel.setDuration(duration);
                                } else {
                                    if (duration != 0 && audioModel.getDuration() < duration) {
                                        audioModel.setDuration(duration);
                                    }
                                }
                                if (!emitter.isDisposed()) {
                                    emitter.onNext(new AudioModelEmitter(Constant.PROGRESS, audioModel));
                                }
                            } catch (Exception e) {

                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("hh", "定时器 onError >>> " + e.getMessage());
                        }

                        @Override
                        public void onComplete() {
                            Log.e("hh", "onComplete >>> ");
                        }
                    });
        }

    }

    /**
     * 取消定时器
     */
    private void stopTimer() {

        if (mTimeDisposable != null && !mTimeDisposable.isDisposed()) {
            mTimeDisposable.dispose();
        }

        mTimeDisposable = null;
    }

    /**
     * 暂停
     *
     * @param audioConfig
     */
    public void pause(AudioConfig audioConfig) {
        if (audioConfig == null) {
            return;
        }
        if (audioConfig.getTagId() != mRecordConfig.getTagId()) {
            return;
        }
        if (mMediaPlayer == null) {
            return;
        }
        if (!mMediaPlayer.isPlaying()) {
            return;
        }
        mMediaPlayer.pause();
    }

    /**
     * 播放
     *
     * @param audioConfig
     */
    public void resume(AudioConfig audioConfig) {
        if (mRecordConfig.getTagId() == 0d) {
            return;
        }
        if (audioConfig == null) {
            return;
        }
        if (audioConfig.getTagId() != mRecordConfig.getTagId()) {
            return;
        }
        if (mMediaPlayer == null) {
            return;
        }

        if (mMediaPlayer.isPlaying()) {
            return;
        }
        mMediaPlayer.start();
    }

    /**
     * 停止 播放器
     *
     * @param audioConfig
     * @return
     */
    public Observable<AudioModelEmitter> stop(AudioConfig audioConfig) {
        if (audioConfig == null) {
            return Observable.error(new RuntimeException("audioConfig == null"));
        }
        //stop的播放器不是当前的播放器
        if (audioConfig.getTagId() != mRecordConfig.getTagId()) {
            return Observable.error(new RuntimeException("stop的播放器不是当前的播放器"));
        }

        AudioModel audioModel = new AudioModel(audioConfig.getTagId());
        if (mAudioModel != null) {
            audioModel.setDuration(audioModel.getDuration());
            audioModel.setPlayableDuration(audioModel.getPlayableDuration());
            audioModel.setProgress(audioModel.getProgress());
        }

        mAudioModel = null;

        resetRecordConfig();

        stopTimer();

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mPlayDisposable != null && !mPlayDisposable.isDisposed()) {
            mPlayDisposable.dispose();
            mPlayDisposable = null;
        }

        //stop的时候，发送一次stop的事件
        return Observable.just(new AudioModelEmitter(Constant.COMPLETION, audioModel));
    }

    /**
     * 跳转
     *
     * @param audioConfig
     */
    public void seekToPosition(AudioConfig audioConfig) {
        if (audioConfig == null) {
            return;
        }
        Log.e("hh", "seekToPosition 进入" + audioConfig.getPosstion());

        if (audioConfig.getTagId() != mRecordConfig.getTagId()) {
            return;
        }

        AudioModel audioModel = mAudioModel;
        if (audioModel == null) {
            return;
        }

        int seekPosstion = audioConfig.getPosstion() * 1000;

        Log.e("hh", "seekToPosition ### seekPosstion " + seekPosstion);
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.seekTo(seekPosstion);

    }


    private class MediaPlayListener implements MediaPlayer.OnBufferingUpdateListener,
            MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
            MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener {

        private double inkoveId;

        private ObservableEmitter<AudioModelEmitter> emitter;

        private boolean isPrepared;

        private MediaPlayListener(double inkoveId, ObservableEmitter<AudioModelEmitter> emitter) {
            this.inkoveId = inkoveId;
            this.emitter = emitter;
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {


            AudioModel audioModel = mAudioModel;

            if (audioModel == null) {
                return;
            }
            if (new Double(audioModel.getPlayableDuration()).intValue() == percent) {
                return;
            }
            if (isPrepared) {
                audioModel.setPlayableDuration(percent);
                int duration = mp.getDuration();
                if (audioModel.getDuration() == 0) {
                    audioModel.setDuration(duration);
                } else {
                    if (duration != 0 && audioModel.getDuration() != duration) {
                        audioModel.setDuration(duration);
                    }
                }
                audioModel.setProgress(mp.getCurrentPosition());
                emitter.onNext(new AudioModelEmitter(Constant.BUFFER, audioModel));
            }
        }


        @Override
        public void onCompletion(MediaPlayer mp) {
            AudioModel audioModel = mAudioModel;
            if (audioModel != null) {
                audioModel.setProgress(audioModel.getDuration());
                emitter.onNext(new AudioModelEmitter(Constant.PROGRESS, audioModel));
                emitter.onNext(new AudioModelEmitter(Constant.COMPLETION, audioModel));
            }
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.e("hh", "AudioPaly error >>> what " + what + " extra " + extra);
            AudioModel audioModel = mAudioModel;
            ///MediaPlayer: error (1, -2147483648) 错误的网址，在初始化的时候，就会报错
            if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN && extra == -2147483648 ||
                    extra == MediaPlayer.MEDIA_ERROR_IO ||
                    extra == MediaPlayer.MEDIA_ERROR_MALFORMED ||
                    extra == MediaPlayer.MEDIA_ERROR_UNSUPPORTED ||
                    extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {

                if (audioModel != null) {
                    audioModel.setCode(-1008);
                }
            }
            if (audioModel != null) {
                emitter.onNext(new AudioModelEmitter(Constant.ERROR, audioModel));
            }
            return true;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
            isPrepared = true;
            AudioModel audioModel = mAudioModel;
            if (audioModel == null) {
                return;
            }
            emitter.onNext(new AudioModelEmitter(Constant.PREPARED, audioModel));
        }

        @Override
        public void onSeekComplete(MediaPlayer mp) {
            Log.e("hh", "onSeekComplete");
        }

    }


}
