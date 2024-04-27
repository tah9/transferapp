package com.genymobile.transferclient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.genymobile.transferclient.tools.FileUtils;
import com.genymobile.transferclient.tools.RunProcess;
import com.genymobile.transferclient.tools.ScreenUtil;
import com.genymobile.transferclient.tools.ScreenUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends Activity implements TextureView.SurfaceTextureListener {

    private TextureView textureView;
    private int decoderHeight;
    private int decoderWidth;
    private int dynamicPort;
    private String host;
    private String type;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScreenUtil.changeFullScreen(this, true);

        Intent intent = getIntent();
        host = intent.getStringExtra("host");
        dynamicPort = intent.getIntExtra("dynamicPort", -1);
        if (dynamicPort == -1) finish();
        type = intent.getStringExtra("type");
        if (type.equals("appRelay")) {
            int realWidth = ScreenUtils.getScreenWidth(this);
            int realHeight = ScreenUtils.getScreenHeight(this);

            decoderWidth = realWidth % 2 != 0 ? realWidth - 1 : realWidth;
            decoderHeight = realHeight % 2 != 0 ? realHeight - 1 : realHeight;

        } else {
            decoderWidth = intent.getIntExtra("width", 0);
            decoderHeight = intent.getIntExtra("height", 0);
        }

        Log.d(TAG, "onCreate: width=" + decoderWidth);
        Log.d(TAG, "onCreate: decoderHeight=" + decoderHeight);

        textureView = new TextureView(this);
        textureView.setSurfaceTextureListener(this);
        FrameLayout layout = new FrameLayout(this);
//        layout.setGravity(Gravity.CENTER);
        layout.addView(textureView);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) textureView.getLayoutParams();
        layoutParams.width = decoderWidth;
        layoutParams.height = decoderHeight;
        textureView.setLayoutParams(layoutParams);

        setContentView(layout);


    }

    private final LinkedBlockingQueue<MotionEvent> motionEvents = new LinkedBlockingQueue<>();

    private static final String TAG = "MainActivity";


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable: ");


        new Thread(() -> {
            try {
                Socket videoSocket = new Socket(host, dynamicPort);
                Log.d(TAG, "socket connection finish " + dynamicPort);
                InputStream inputStream = videoSocket.getInputStream();
                FileDescriptor fileDescriptor = ParcelFileDescriptor.fromSocket(videoSocket).getFileDescriptor();

                VideoDecoder videoDecoder = new VideoDecoder(new Surface(surface),
                        decoderWidth,
                        decoderHeight,
                        fileDescriptor, inputStream);

                Socket controlSocket = new Socket(host, dynamicPort);
                Log.d(TAG, "onSurfaceTextureAvailable: controlSocket");

                DataOutputStream oos = new DataOutputStream(controlSocket.getOutputStream());
                DataInputStream ois = new DataInputStream(controlSocket.getInputStream());
                textureView.setOnTouchListener((v, event) -> {
                    motionEvents.offer(event);
                    return true;
                });
                new Thread(() -> {
                    while (true) {
                        try {
                            MotionEvent event = motionEvents.take();
                            try {
                                oos.writeInt((int) (event.getDownTime()));
                                oos.writeByte((byte)event.getAction());
                                oos.writeByte((byte)event.getPointerCount());

                                for (int i = 0; i < event.getPointerCount(); i++) {
                                    oos.writeByte(event.getPointerId(i));
                                    oos.writeFloat(event.getX(i));
                                    oos.writeFloat(event.getY(i));
                                }
                                oos.flush();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

    }

//    public static int packCoordinates(int x, int y) {
//        return (x & 0xFFFF) | ((y & 0xFFFF) << 16);
//    }

}
