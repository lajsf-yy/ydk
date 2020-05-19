/*
 * Copyright (C) 2014 Yuya Tanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.luck.picture.transcoder.format;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;

class Android720pFormatStrategy implements MediaFormatStrategy {
    public static final int AUDIO_BITRATE_AS_IS = -1;
    public static final int AUDIO_CHANNELS_AS_IS = -1;
    private static final String TAG = "720pFormatStrategy";
    private static final int LONGER_LENGTH = 1280;
    private static final int SHORTER_LENGTH = 720;
    private static final int DEFAULT_VIDEO_BITRATE = 8000 * 1000; // From Nexus 4 Camera in 720p
    private final int mVideoBitrate;
    private final int mAudioBitrate;
    private final int mAudioChannels;

    public Android720pFormatStrategy() {
        this(DEFAULT_VIDEO_BITRATE);
    }

    public Android720pFormatStrategy(int videoBitrate) {
        this(videoBitrate, AUDIO_BITRATE_AS_IS, AUDIO_CHANNELS_AS_IS);
    }

    public Android720pFormatStrategy(int videoBitrate, int audioBitrate, int audioChannels) {
        mVideoBitrate = videoBitrate;
        mAudioBitrate = audioBitrate;
        mAudioChannels = audioChannels;
    }

    @Override
    public MediaFormat createVideoOutputFormat(MediaFormat inputFormat) {
        int width = inputFormat.getInteger(MediaFormat.KEY_WIDTH);
        int height = inputFormat.getInteger(MediaFormat.KEY_HEIGHT);
        int longer, shorter, outWidth, outHeight;
        if (width >= height) {
            longer = width;
            shorter = height;
            outWidth = LONGER_LENGTH;
            outHeight = SHORTER_LENGTH;
        } else {
            shorter = width;
            longer = height;
            outWidth = SHORTER_LENGTH;
            outHeight = LONGER_LENGTH;
        }
//        if (longer * 9 != shorter * 16) {
//            throw new OutputFormatUnavailableException("This video is not 16:9, and is not able to transcode. (" + width + "x" + height + ")");
//        }
//        if (shorter <= SHORTER_LENGTH) {
//            Log.d(TAG, "This video is less or equal to 720p, pass-through. (" + width + "x" + height + ")");
//            return null;
//        }
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", outWidth, outHeight);
        // From Nexus 4 Camera in 720p
//        format.setInteger(MediaFormat.KEY_BIT_RATE, mVideoBitrate);
        format.setInteger(MediaFormat.KEY_BIT_RATE, DEFAULT_VIDEO_BITRATE/2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            /**
             * BITRATE_MODE_CQ: 表示完全不控制码率，尽最大可能保证图像质量
             * BITRATE_MODE_CBR: 表示编码器会尽量把输出码率控制为设定值, （mi8报configure异常）
             * BITRATE_MODE_VBR: 表示编码器会根据图像内容的复杂度
             * （实际上是帧间变化量的大小）来动态调整输出码率，图像复杂则码率高，图像简单则码率低
             */
            format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
//            format.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileMain);
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            format.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel41);
//        }
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 24);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        return format;
    }

    @Override
    public MediaFormat createAudioOutputFormat(MediaFormat inputFormat) {
//        if (mAudioBitrate == AUDIO_BITRATE_AS_IS || mAudioChannels == AUDIO_CHANNELS_AS_IS) return null;
//
//        // Use original sample rate, as resampling is not supported yet.
//        final MediaFormat format = MediaFormat.createAudioFormat(MediaFormatExtraConstants.MIMETYPE_AUDIO_AAC,
//                inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE), mAudioChannels);
//        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
//        format.setInteger(MediaFormat.KEY_BIT_RATE, mAudioBitrate);
//        return format;
        return null;//just copy
    }
}
