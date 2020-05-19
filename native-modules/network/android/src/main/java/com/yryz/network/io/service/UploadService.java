package com.yryz.network.io.service;


import com.yryz.network.io.entity.UploadInfo;

import io.reactivex.Observable;

/**
 * Created by Administrator on 2018/2/27.
 */

public interface UploadService {

    Observable<UploadInfo> upload(String localFile);

    Observable<UploadInfo> uploadHead(String localFile);
}
