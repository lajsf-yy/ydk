package ydk.audio;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.util.Log;
import android.widget.Toast;


import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import ydk.core.Ydk;
import ydk.core.utils.CacheUtils;

/**
 * Created by Kyz on 2018/10/25.
 */
public class RecorderManager {

    private static RecorderManager manager;
    private RecorderListener lis;
    private MediaRecorder mMediaRecorder;
    private File mAudioFile;
    private long startTime;
    private long endTime;
    //安卓6.0以上手机权限处理
    public static final int PERMISSIONS_REQUEST_FOR_AUDIO = 1;
    private Timer timer;
    private TimerTask task;
    private int sunTime;
    private JSONObject json;
    private static final String RESULT = "1";
    private static final String ERROR = "2";
    private static final String PROGRESS = "3";
    private static final String MINDURATION = "3";
    private static final String MAXDURATION = "180";
    public static final int STATE_RECORDING = -1;
    public static final int STATE_NO_PERMISSION = -2;
    public static final int STATE_SUCCESS = 1;
    private String minDuration;
    private String maxDuration;

    private RecorderManager() {

    }

    public static RecorderManager getInstance() {
        if (manager == null) {
            manager = new RecorderManager();
        }
        return manager;
    }

    public void startRecord(int min, int max) {

        if (min <= 0) {
            minDuration = MINDURATION;
        } else {
            minDuration = String.valueOf(min);
        }
        if (max <= 0) {
            maxDuration = MAXDURATION;
        } else {
            maxDuration = String.valueOf(max);
        }
        start();
    }

    private void start() {

        releaseRecorder();

        //创建MediaRecorder对象
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }

        String audioFilePath = CacheUtils.getAudioFilePath(Ydk.getApplicationContext());


        //创建录音文件,.m4a为MPEG-4音频标准的文件的扩展名
        //mAudioFile = new File(audioFilePath + "/" + System.currentTimeMillis() + ".amr");
        mAudioFile = new File(audioFilePath + "/" + System.currentTimeMillis() + ".aac");
        //创建父文件夹
        mAudioFile.getParentFile().mkdirs();
        try {
            //创建文件
            mAudioFile.createNewFile();
            //配置mMediaRecorder相应参数
            //从麦克风采集声音数据
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置保存文件格式为MP4
            //mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            //mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            //设置采样频率,44100是所有安卓设备都支持的频率,频率越高，音质越好，当然文件越大
            mMediaRecorder.setAudioSamplingRate(11025);
            //设置声音数据编码格式,音频通用格式是AAC
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            //设置编码频率
            mMediaRecorder.setAudioEncodingBitRate(96000);
            //设置录音保存的文件
            mMediaRecorder.setOutputFile(mAudioFile.getAbsolutePath());
            //设置录制允许的最大时长 单位是毫秒。必须在setOutFormat方法之后，prepare方法之前使用。
            mMediaRecorder.setMaxDuration(Integer.valueOf(maxDuration) * 1000);
            //开始录音
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            this.isRecorder = true;
            //记录开始录音时间
            startTime = System.currentTimeMillis();

            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    long time = System.currentTimeMillis() - startTime;
                    if (time <= Integer.valueOf(maxDuration) * 1000 && lis != null) {
                        int round = Math.round(time / 1000);
                        if (round >= Integer.valueOf(maxDuration)) {
                            round = Integer.valueOf(maxDuration);
                        }
                        lis.getSatate(PROGRESS, 0, "", 0, round, getDB(), Integer.valueOf(maxDuration));
                    } else {
                        lis.getSatate(PROGRESS, 0, "", 0, Integer.valueOf(maxDuration), getDB(), Integer.valueOf(maxDuration));
                        stopRecord();
                    }
                }
            };
            //delay  延时的时间 第一次执行的时间
            //period  每隔多长时间执行一次
            timer.schedule(task, 100, 100);
        } catch (Exception e) {
            Log.e("hh", "RecorderManager >>>  " + e.getMessage());

            recordFail();
        }

    }

    private void recordFail() {

        releaseRecorder();

        mAudioFile = null;
        if (lis != null) {
            lis.getSatate(ERROR, 0, "", 0, 0, 0, 0);
        }
    }

    public void startRecordPlay() {
        PlayerManager playerManager = PlayerManager.getInstance();
        playerManager.play(getdir());
    }

    public String getdir() {
        return mAudioFile == null ? "" : mAudioFile.getAbsolutePath();

    }

    public String getSuTimem() {
        String timeFromInt = getTimeFromInt((int) sunTime * 1000);
        return timeFromInt;
    }

    //文件大小
    public static long getFileSize(String path) {
        File mFile = new File(path);
        if (!mFile.exists())
            return -1;
        long length = mFile.length();
        return length / 1000;
    }

    public void stopRecord() {
        if (mMediaRecorder == null) {
            return;
        }

        //added by ouyang start
        try {
            //下面三个参数必须加，不加的话会奔溃，在mediarecorder.stop();
            //报错为：RuntimeException:stop failed
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setOnInfoListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            //停止录音
            this.isRecorder = false;
            mMediaRecorder.stop();
        } catch (Exception e) {
            Log.i("Exception", Log.getStackTraceString(e));
        }

        //记录停止时间
        endTime = System.currentTimeMillis();
        //录音时间处理，比如只有大于2秒的录音才算成功
        sunTime = Math.round((endTime - startTime) / 1000);
        if (sunTime >= Integer.valueOf(maxDuration)) {
            sunTime = Integer.valueOf(maxDuration);
        }
        Log.e("hh", "sunTime " + sunTime);
        //录音成功,添加数据
        long fileSize = getFileSize(mAudioFile == null ? "" : mAudioFile.getAbsolutePath());
        if (lis != null) {
            lis.getSatate(RESULT, sunTime, getdir(), fileSize, 0, getDB(), 0);
        }
        if (sunTime < Integer.valueOf(minDuration)) {
            mAudioFile = null;
        }
        //录音完成释放资源
        releaseRecorder();
    }

    public void releaseRecorder() {
        if (null != mMediaRecorder) {
            this.isRecorder = false;
            mMediaRecorder.release();
            mMediaRecorder = null;
            if (timer != null) timer.cancel();
            if (timer != null) task.cancel();
        }
    }

    private static final int BASE = 1;

    /**
     * 获取录音的声音分贝值
     *
     * @return
     */
    private int db;

    private boolean isRecorder;

    private int getDB() {
        if (mMediaRecorder == null) {
            return db;
        }
        if (!this.isRecorder) {
            return db;
        }
        try {
            double ratio = (double) mMediaRecorder.getMaxAmplitude() / BASE;
            double db = 0;// 分贝
            if (ratio > 1)
                db = 20 * Math.log10(ratio);
            this.db = (int) db;
        } catch (Exception e) {
            Log.e("hh", "RecorderManager getDB >>>  " + e.getMessage());

        }
        return this.db;
    }

    private String formateTime(double time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(time);
        return dateString;
    }

    public static String getTimeFromInt(int time) {

        if (time <= 0) {
            return "0:00";
        }
        int secondnd = (time / 1000) / 60;
        int million = (time / 1000) % 60;
        String f = String.valueOf(secondnd);
        String m = million >= 10 ? String.valueOf(million) : "0"
                + String.valueOf(million);
        return f + ":" + m;
    }

    public interface RecorderListener {
        void getSatate(String flag, int duration, String filePath, long size, int progress, int db, int maxDuration);
    }

    public void setRecorderListener(RecorderManager.RecorderListener lis) {
        this.lis = lis;

    }
}
