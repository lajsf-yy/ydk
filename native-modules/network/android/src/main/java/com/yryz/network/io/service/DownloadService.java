package com.yryz.network.io.service;


import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.memory.PooledByteBuffer;
import com.facebook.common.memory.PooledByteBufferInputStream;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.yryz.network.io.entity.DownloadInfo;


import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ydk.core.Ydk;
import ydk.core.utils.CacheUtils;
import ydk.core.utils.FileUtils;

public class DownloadService {

    private HashMap<String, Call> downCalls;//用来存放各个下载的请求
    private OkHttpClient mClient;//OKHttpClient;

    public DownloadService() {
        downCalls = new HashMap<>();
        mClient = new OkHttpClient.Builder().build();
    }

    /**
     * 通过fresco下载图片,主要用于已经用fresco加载过的图片
     *
     * @param url
     * @return
     */
    public Observable<DownloadInfo> downloadByFresco(String url) {
        return Observable.just(url)
                .map(s -> ImageRequestBuilder.newBuilderWithSource(Uri.parse(s))
                        .setProgressiveRenderingEnabled(true).build())
                .map(build -> Fresco.getImagePipeline().fetchDecodedImage(build, Ydk.getApplicationContext()))
                .flatMap(dataSource -> Observable.create(new FrescoSubscribe(dataSource, createFrescoInfo(url))))
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread());
    }

    private DownloadInfo createFrescoInfo(String url) {
        DownloadInfo downloadInfo = new DownloadInfo(url);
        File file = new File(CacheUtils.getImageFilePath(Ydk.getApplicationContext()), new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()));
        downloadInfo.setFileName(file.getName());
        downloadInfo.setFilePath(file.getAbsolutePath());
        return downloadInfo;
    }

    private class FrescoSubscribe implements ObservableOnSubscribe<DownloadInfo> {
        DataSource dataSource;
        DownloadInfo downloadInfo;

        public FrescoSubscribe(DataSource dataSource, DownloadInfo downloadInfo) {
            this.dataSource = dataSource;
            this.downloadInfo = downloadInfo;
        }

        @Override
        public void subscribe(ObservableEmitter<DownloadInfo> emitter) {
            dataSource.subscribe(new BaseDataSubscriber<CloseableReference>() {
                @Override
                protected void onNewResultImpl(DataSource<CloseableReference> dataSource) {
                    if (!dataSource.isFinished()) {
                        downloadInfo.setProgress((int) (dataSource.getProgress() * 100));
                        DownloadInfo info = new DownloadInfo(downloadInfo.getUrl());
                        info.setProgress(downloadInfo.getProgress());
                        info.setTotal(downloadInfo.getTotal());
                        emitter.onNext(info);
                    } else {
                        CloseableReference reference = dataSource.getResult();
                        if (reference != null && reference.get() != null) {
                            File file = new File(downloadInfo.getFilePath());
                            try {
                                FileOutputStream outputStream = new FileOutputStream(file);
                                if (reference.get() instanceof PooledByteBuffer) {
                                    PooledByteBufferInputStream inputStream = new PooledByteBufferInputStream((PooledByteBuffer) reference.get());

                                    int bytesRead;
                                    byte[] buffer = new byte[8192];
                                    while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
                                        outputStream.write(buffer, 0, bytesRead);
                                    }
                                    inputStream.close();
                                } else if (reference.get() instanceof CloseableStaticBitmap) {
                                    Bitmap bitmap = ((CloseableStaticBitmap) reference.get()).getUnderlyingBitmap();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                                }
                                outputStream.flush();
                                outputStream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                                emitter.onError(e);
                            }
                            setRealSuffix(file, downloadInfo);
                            downloadInfo.setCompleted(true);
                            emitter.onNext(downloadInfo);
                            emitter.onComplete();
                        } else {
                            emitter.onError(new Exception("download fail"));
                        }
                    }
                }

                @Override
                protected void onFailureImpl(DataSource dataSource) {
                    emitter.onError(dataSource.getFailureCause());
                }
            }, CallerThreadExecutor.getInstance());
        }


    }


    /**
     * 开始下载
     *
     * @param url 下载请求的网址
     */
    public Observable<DownloadInfo> download(String url) {
        return Observable.just(url)
                .filter(s -> !downCalls.containsKey(s))//call的map已经有了,就证明正在下载,则这次不下载
                .flatMap(s -> Observable.just(createDownInfo(s)))
                .map(this::getRealFileName)//检测本地文件夹,生成新的文件名
                .flatMap(downloadInfo -> Observable.create(new DownloadSubscribe(downloadInfo)))//下载
                .observeOn(AndroidSchedulers.mainThread())//在主线程回调
                .subscribeOn(Schedulers.io());//在子线程执行

    }

    public void cancel(String url) {
        Call call = downCalls.get(url);
        if (call != null) {
            call.cancel();//取消
        }
        downCalls.remove(url);
    }

    /**
     * 创建DownInfo
     *
     * @param url 请求网址
     * @return DownInfo
     */
    private DownloadInfo createDownInfo(String url) {
        DownloadInfo downloadInfo = new DownloadInfo(url);
        long contentLength = getContentLength(url);//获得文件大小
        downloadInfo.setTotal(contentLength);
        String fileName = url.substring(url.lastIndexOf("/"));
		//"280776391598080.png?w=301&h=306";
        if(!TextUtils.isEmpty(fileName)){
            Pattern compile = Pattern.compile(".(gif|png|jpg|jpeg)[?].*$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = compile.matcher(fileName);
            if(matcher.find()){
                fileName = fileName.substring(0, fileName.lastIndexOf("?"));
               // fileName = fileName.replaceAll("[?].*$", "");
            }
        }
        downloadInfo.setFileName(fileName);
        return downloadInfo;
    }

    private DownloadInfo getRealFileName(DownloadInfo downloadInfo) {
        String fileName = downloadInfo.getFileName();
        long downloadLength = 0, contentLength = downloadInfo.getTotal();
        File file = new File(CacheUtils.getImageFilePath(Ydk.getApplicationContext()), fileName);
        if (file.exists()) {
            //找到了文件,代表已经下载过,则获取其长度
            downloadLength = file.length();
        }
        //之前下载过,需要重新来一个文件
        int i = 1;
        while (downloadLength >= contentLength) {
            int dotIndex = fileName.lastIndexOf(".");
            String fileNameOther;
            if (dotIndex == -1) {
                fileNameOther = fileName + "(" + i + ")";
            } else {
                fileNameOther = fileName.substring(0, dotIndex)
                        + "(" + i + ")" + fileName.substring(dotIndex);
            }
            File newFile = new File(CacheUtils.getImageFilePath(Ydk.getApplicationContext()), fileNameOther);
            file = newFile;
            downloadLength = newFile.length();
            i++;
        }
        //设置改变过的文件名/大小
        downloadInfo.setDownloadBytes(downloadLength);
        downloadInfo.setFileName(file.getName());
        downloadInfo.setFilePath(file.getAbsolutePath());
        return downloadInfo;
    }

    private long getContentLength(String downloadUrl) {
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long contentLength = response.body() != null ? response.body().contentLength() : 0;
                response.close();
                return contentLength == 0 ? DownloadInfo.TOTAL_ERROR : contentLength;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return DownloadInfo.TOTAL_ERROR;
    }

    private class DownloadSubscribe implements ObservableOnSubscribe<DownloadInfo> {
        private DownloadInfo downloadInfo;

        public DownloadSubscribe(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @Override
        public void subscribe(ObservableEmitter<DownloadInfo> emitter) throws Exception {
            String url = downloadInfo.getUrl();
            long downloadLength = downloadInfo.getDownloadBytes();//已经下载好的长度
            long contentLength = downloadInfo.getTotal();//文件的总长度
            emitter.onNext(downloadInfo);//初始进度信息

            Request request = new Request.Builder()
                    //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
                    .addHeader("RANGE", "bytes=" + downloadLength + "-" + contentLength)
                    .url(url)
                    .build();
            Call call = mClient.newCall(request);
            downCalls.put(url, call);//把这个添加到call里,方便取消
            Response response = call.execute();

            File file = new File(CacheUtils.getImageFilePath(Ydk.getApplicationContext()), downloadInfo.getFileName());
            InputStream is = null;
            FileOutputStream fileOutputStream = null;
            try {
                is = response.body().byteStream();
                fileOutputStream = new FileOutputStream(file, true);
                byte[] buffer = new byte[2048];//缓冲数组2kB
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, len);
                    downloadLength += len;
                    DownloadInfo info = new DownloadInfo(downloadInfo.getUrl());
                    info.setDownloadBytes(downloadLength);
                    info.setTotal(downloadInfo.getTotal());
                    emitter.onNext(info);
                }
                fileOutputStream.flush();
                setRealSuffix(file, downloadInfo);
                downloadInfo.setCompleted(true);
                emitter.onNext(downloadInfo);
            } finally {
                closeAll(is, fileOutputStream);   //关闭IO流
                downCalls.remove(url);
            }
            emitter.onComplete();
        }
    }

    private void setRealSuffix(File file, DownloadInfo downloadInfo) {
        if (TextUtils.isEmpty(FileUtils.getSuffix(file.getAbsolutePath()))) {
            String fileType = FileUtils.getFileType(file.getAbsolutePath());
            if (!TextUtils.isEmpty(fileType)) {
                File newFile = new File(file.getAbsolutePath() + "." + fileType);
                file.renameTo(newFile);
                downloadInfo.setFilePath(newFile.getAbsolutePath());
                downloadInfo.setFileName(newFile.getName());
            }
        }
    }

    private void closeAll(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
