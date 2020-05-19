package ydk.video;

import android.content.Context;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

import ydk.core.Ydk;
import ydk.core.utils.CacheUtils;

/**
 * Created by Gsm on 2018/6/6.
 */
public class ExoCacheDataSourceFactory implements DataSource.Factory {

    private static SimpleCache simpleCache;
    private final DefaultDataSourceFactory sourceFactory;

    public ExoCacheDataSourceFactory(Context context, TransferListener<? super DataSource> listener) {
        DefaultHttpDataSourceFactory defaultHttpDataSourceFactory = new DefaultHttpDataSourceFactory(
                Util.getUserAgent(context, context.getPackageName()),
                listener,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true
        );
        sourceFactory = new DefaultDataSourceFactory(context, listener, defaultHttpDataSourceFactory);
    }


    @Override
    public DataSource createDataSource() {
        if (simpleCache == null) {
            //设置缓存目录与总缓存size
            simpleCache = new SimpleCache(new File(CacheUtils.getExoFilePath(Ydk.getApplicationContext())),
                    new LeastRecentlyUsedCacheEvictor(1024 * 1024 * 1024));
        }
        return new CacheDataSource(simpleCache, sourceFactory.createDataSource(),
                new FileDataSource(), new CacheDataSink(simpleCache, 10 * 1024 * 1024),
                CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null);
    }

}
