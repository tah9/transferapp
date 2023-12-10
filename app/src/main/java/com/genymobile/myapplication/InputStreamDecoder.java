package com.genymobile.myapplication;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

import android.net.LocalSocket;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Parcel;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import android.view.Surface;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.h264.H264TrackImpl;

public class InputStreamDecoder {
    private MediaCodec mediaCodec;

    public InputStreamDecoder(Surface surface, int w, int h) {
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
            mediaCodec = MediaCodec.createDecoderByType("video/avc");
            MediaFormat format = createFormat(biteRate, DEFAULT_FRAME_RATE, DEFAULT_I_FRAME_INTERVAL, w, h); // 根据实际编码的视频分辨率设置
            mediaCodec.configure(format, surface, null, 0);
            mediaCodec.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final int DEFAULT_FRAME_RATE = 60; // fps
    int biteRate = 64000000;
    private static final int DEFAULT_I_FRAME_INTERVAL = 10; // seconds


    private MediaFormat createFormat(int bitRate, int frameRate, int iFrameInterval, int w, int h) throws IOException {
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, "video/avc");
        format.setInteger(MediaFormat.KEY_WIDTH, w);
        format.setInteger(MediaFormat.KEY_HEIGHT, h);
//        format.setInteger(MediaFormat.KEY_BIT_RATE,bitRate);
        return format;
    }

    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
    private static final String TAG = "InputStreamDecoder";

    int counter = 0;
    byte[] bak;

    public void decode(FileDescriptor fileDescriptor) throws Exception {
        boolean eof = false;
//        ByteBuffer byteBuffer = ByteBuffer.allocate(10_000_000);
//        byteBuffer.compact();
        int byteSize = 250000 * 10;
        byte[] header = new byte[4];
        while (!eof) {
//            header.clear();
//            IO.readFully(fileDescriptor, header,header.length);
            Os.read(fileDescriptor, header, 0, header.length);
//            header.flip();
            // 从 byte 数组中读取 int 值
            int len = ((header[0] & 0xFF) << 24)
                    | ((header[1] & 0xFF) << 16)
                    | ((header[2] & 0xFF) << 8)
                    | (header[3] & 0xFF);
            System.out.println("len " + len);
            int inputBufferId = mediaCodec.dequeueInputBuffer(-1);
            if (inputBufferId >= 0) {
                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferId);
                inputBuffer.clear();
                byte[] data = new byte[len];
                int okSize = 0;
                while (okSize<len) {
                    okSize+= Os.read(fileDescriptor, data, okSize, len-okSize);
                }
                inputBuffer.put(data);
//                IO.readFully(fileDescriptor, inputBuffer, len);
                mediaCodec.queueInputBuffer(inputBufferId, 0, len, 0, 0);
            }

            int outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            eof = (bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;
            if (outputBufferId >= 0) {
                // 将解码后的数据显示在SurfaceView上
                mediaCodec.releaseOutputBuffer(outputBufferId, true);
            }
//            break;
        }
    }

    public void release() {
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
        }
    }
}
