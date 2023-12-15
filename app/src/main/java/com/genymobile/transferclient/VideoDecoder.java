package com.genymobile.transferclient;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.io.DataInputStream;
import java.io.File;
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



    private boolean decodeIn(/*int inIndex*/) {
        try {
            Integer inIndex = inputBufferQueue.take();
            ByteBuffer inputBuffer = _mediaCodec.getInputBuffer(inIndex);
//            lock.lock();
            inputBuffer.put(loadingPacket.getFullyArray(), 0, loadingPacket.getSize());
            int size=loadingPacket.getSize();
            long time = loadingPacket.getTime();
//            lock.unlock();
            _mediaCodec.queueInputBuffer(inIndex, 0, size, time, 0);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    VideoPacket loadingPacket = new VideoPacket();
    VideoPacket completePacket = new VideoPacket();

    private void receiver() {
        try {
            loadingPacket.setSize(dataInputStream.readInt());
            loadingPacket.setTime(dataInputStream.readLong());
            dataInputStream.readFully(loadingPacket.getFullyArray(), 0, loadingPacket.getSize());
            //交换缓冲
//            lock.lock();
//            VideoPacket temp = loadingPacket;
//            loadingPacket=completePacket;
//            completePacket = temp;
//            lock.unlock();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startReceiver() {
        new Thread(() -> {
            while (true) {
                receiver();
                decodeIn();
            }
        }).start();
    }

    private InputStream inputStream;
    private DataInputStream dataInputStream;
    private FileDescriptor fileDescriptor;

    int curIndex=0;
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
//                    decodeIn(inIndex);
                    try {
                        Log.d(TAG, "onInputBufferAvailable: put" + inIndex);
                        inputBufferQueue.put(inIndex);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int outIndex, @NonNull MediaCodec.BufferInfo bufferInfo) {
                    _mediaCodec.releaseOutputBuffer(outIndex, true);
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

//            new Thread(()->{
//                while(true){
//                    decodeIn();
//                }
//            }).start();
//
//            Log.d(TAG, "VideoDecoder: configure finish");

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
