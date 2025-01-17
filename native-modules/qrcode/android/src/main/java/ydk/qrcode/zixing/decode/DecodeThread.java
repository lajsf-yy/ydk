/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ydk.qrcode.zixing.decode;

import android.os.Handler;
import android.os.Looper;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.ResultPointCallback;

import ydk.qrcode.zixing.android.ICaptureHandler;

import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

/**
 * This thread does all the heavy lifting of decoding the images.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class DecodeThread extends Thread {

    private ICaptureHandler iCaptureHandler;
    private final Hashtable<DecodeHintType, Object> hints;
    private Handler handler;
    private final CountDownLatch handlerInitLatch;

    public DecodeThread(ICaptureHandler iCaptureHandler, ResultPointCallback resultPointCallback) {

        this.iCaptureHandler = iCaptureHandler;
        handlerInitLatch = new CountDownLatch(1);

        hints = new Hashtable<>();

        Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();

        /*是否解析有条形码（一维码）*/
        if (iCaptureHandler.getZxingConfig().isDecodeBarCode()) {
            decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
        }
        decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
        decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);

        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);

    }

    public Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(iCaptureHandler, hints);
        handlerInitLatch.countDown();
        Looper.loop();
    }

}
