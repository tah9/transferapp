package com.genymobile.transferclient;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class VideoDecoder {
    private MediaCodec _mediaCodec;
    ByteBuffer header = ByteBuffer.allocate(12);
    int gross = 0;
    private final LinkedBlockingQueue<Integer> inputBufferQueue = new LinkedBlockingQueue<>();

    private final Lock lock = new ReentrantLock();


    VideoPacketCache videoPacketCache = new VideoPacketCache();
    VideoPacketCache completePacket = new VideoPacketCache();


    public void startReceiver() {
        new Thread(() -> {
            while (true) {
                try {
                    videoPacketCache.setSize(dataInputStream.readInt());
                    videoPacketCache.setTime(dataInputStream.readLong());
                    dataInputStream.readFully(videoPacketCache.getFullyArray(), 0, videoPacketCache.getSize());
                    Integer inIndex = inputBufferQueue.take();
                    _mediaCodec.getInputBuffer(inIndex)
                            .put(videoPacketCache.getFullyArray(), 0, videoPacketCache.getSize());
                    int size = videoPacketCache.getSize();
                    long time = videoPacketCache.getTime();
                    _mediaCodec.queueInputBuffer(inIndex, 0, size, time, 0);
                } catch (Exception ignored) {
                }
            }
        }).start();
    }

    private InputStream inputStream;
    private DataInputStream dataInputStream;
    private FileDescriptor fileDescriptor;

    int curIndex = 0;

    public VideoDecoder(Surface surface, int w, int h, FileDescriptor fileDescriptor, InputStream inputStream) throws FileNotFoundException {
        this.inputStream = inputStream;
        this.fileDescriptor = fileDescriptor;
        this.dataInputStream = new DataInputStream(inputStream);
        startReceiver();
        try {
            // 创建解码器并配置参数
            Log.d(TAG, "VideoDecoder: create...");
            _mediaCodec = MediaCodec.createDecoderByType("video/avc");
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, w, h); // 根据实际编码的视频分辨率设置
//            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1*1024 * 1024);
//            format.setByteBuffer("csd-0", getData(fileDescriptor));
//            format.setByteBuffer("csd-1", getData(fileDescriptor));

            /*
            async callback must set before configure (mediaCodec.configure)
            When asynchronous callback is enabled, the client should not call
            {@link #getInputBuffers}, {@link #getOutputBuffers},
            {@link #dequeueInputBuffer(long)} or {@link #dequeueOutputBuffer(BufferInfo, long)}.
             */
            _mediaCodec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int inIndex) {
                    inputBufferQueue.offer(inIndex);
                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int outIndex, @NonNull MediaCodec.BufferInfo bufferInfo) {
                    _mediaCodec.releaseOutputBuffer(outIndex, bufferInfo.presentationTimeUs);
                }

                @Override
                public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {
                }

                @Override
                public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat format) {
                }
            });


            _mediaCodec.configure(format, surface, null, 0);
            _mediaCodec.start();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static final String TAG = "InputStreamDecoder";


    public void release() {
        if (_mediaCodec != null) {
            _mediaCodec.stop();
            _mediaCodec.release();
        }
    }
}
