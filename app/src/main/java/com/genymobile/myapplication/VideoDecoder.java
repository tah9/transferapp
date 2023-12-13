package com.genymobile.myapplication;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import android.os.Build;
import android.view.Surface;

import androidx.annotation.NonNull;

public class VideoDecoder {
    private MediaCodec _mediaCodec;
    ByteBuffer header = ByteBuffer.allocate(12);
    int gross = 0;


    public ByteBuffer getData(FileDescriptor fileDescriptor) {
        header.clear();
        IO.readFully(fileDescriptor, header);
        header.flip();
////             从 byte 数组中读取 int 值
        int size = header.getInt();
        long time = header.getLong();
//            System.out.println("size " + size);
        ByteBuffer inputBuffer = ByteBuffer.allocate(size);
//                    System.out.println("inputBufferId=" + inputBufferId);
        System.out.println("position=" + inputBuffer.position() + ";limit=" + inputBuffer.limit() + ";size=" + inputBuffer.capacity());
        try {
            inputBuffer.limit(size);
        } catch (Exception e) {
            System.out.println("limit incurred");
        }
        IO.readFully(fileDescriptor, inputBuffer);
        return inputBuffer;
    }

    public VideoDecoder(FileDescriptor fileDescriptor, InputStream inputStream,Surface surface, int w, int h) {
//        try {
//            H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl(new File("/data/data/com.genymobile.myapplication/files/test.h264")));
//            Movie movie = new Movie();
//            movie.addTrack(h264Track);
//            Container build = new DefaultMp4Builder().build(movie);
//            FileChannel channel = new FileOutputStream("/data/data/com.genymobile.myapplication/files/test.mp4").getChannel();
//            build.writeContainer(channel);
//            channel.close();
//            Log.d("TAG", "InputStreamDecoder: ");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }


        try {
            // 创建解码器并配置参数
            _mediaCodec = MediaCodec.createDecoderByType("video/avc");
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, w, h); // 根据实际编码的视频分辨率设置

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

                    //todo header maybe should use new
                    header.clear();
                    IO.readBytes(inputStream,header.array(),12);
//                    IO.readFully(fileDescriptor, header);
                    header.flip();
////             从 byte 数组中读取 int 值
                    int size = header.getInt();
                    long time = header.getLong();
//            System.out.println("size " + size);
                    ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inIndex);
//                    System.out.println("inputBufferId=" + inputBufferId);
                    System.out.println("position=" + inputBuffer.position() + ";limit=" + inputBuffer.limit() + ";size=" + inputBuffer.capacity());
//                    try {
//                        inputBuffer.limit(size);//限制这里可能不合适
//                    } catch (Exception e) {
//                        System.out.println("limit incurred");
//                    }
//                    IO.readFully(fileDescriptor, inputBuffer);
                    byte[] temp = new byte[size];
                    IO.readBytes(inputStream,temp,size);
                    inputBuffer.put(temp);
                    // into decodeQueue
                    _mediaCodec.queueInputBuffer(inIndex, 0, size, time, 0);
                    System.out.println("size " + size + " gross " + (gross += size));

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String TAG = "InputStreamDecoder";


    public void decode(FileDescriptor fileDescriptor) throws Exception {
        boolean eof = false;
        while (!eof) {


//            mediaCodec.releaseOutputBuffer(mediaCodec.dequeueOutputBuffer(bufferInfo, 0), bufferInfo.presentationTimeUs);

//            int outputBufferId = _mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
//            eof = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
//
//            if (outputBufferId < 0) {
//                continue;
//            }
//            // 将解码后的数据显示在SurfaceView上
//            _mediaCodec.releaseOutputBuffer(outputBufferId, true);
        }
    }

    public void release() {
        if (_mediaCodec != null) {
            _mediaCodec.stop();
            _mediaCodec.release();
        }
    }
}
