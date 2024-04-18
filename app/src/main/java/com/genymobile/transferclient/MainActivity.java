package com.genymobile.transferclient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.genymobile.transferclient.tools.FileUtils;
import com.genymobile.transferclient.tools.RunProcess;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textureView = new TextureView(this);
        textureView.setSurfaceTextureListener(this);
        setContentView(textureView);
    }

    private final LinkedBlockingQueue<MotionEvent> motionEvents = new LinkedBlockingQueue<>();

    private static final String TAG = "MainActivity";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable: ");
        Intent intent = getIntent();
        String host = intent.getStringExtra("host");
        int dynamicPort = intent.getIntExtra("dynamicPort", -1);
        if (dynamicPort == -1) finish();
        new Thread(() -> {
            try {
                Socket videoSocket = new Socket(host,dynamicPort);
                Log.d(TAG, "socket connection finish " + dynamicPort);
                InputStream inputStream = videoSocket.getInputStream();
                FileDescriptor fileDescriptor = ParcelFileDescriptor.fromSocket(videoSocket).getFileDescriptor();
//                videoSocket.close();

                WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                windowManager.getDefaultDisplay().getMetrics(displayMetrics);
                VideoDecoder videoDecoder = new VideoDecoder(new Surface(surface),
                        displayMetrics.widthPixels%2!=0?displayMetrics.widthPixels-1:displayMetrics.widthPixels,
                        displayMetrics.heightPixels%2!=0?displayMetrics.heightPixels-1:displayMetrics.heightPixels,
//                        1080, 2326,
                        fileDescriptor, inputStream);
//                Socket controlSocket = serverSocket.accept();
//                Log.d(TAG, "onSurfaceTextureAvailable: controlSocket");
//
//                DataOutputStream oos = new DataOutputStream(controlSocket.getOutputStream());
//                DataInputStream ois = new DataInputStream(controlSocket.getInputStream());
//                textureView.setOnTouchListener((v, event) -> {
//                    motionEvents.offer(event);
//                    return true;
//                });
//                new Thread(() -> {
//                    while (true) {
//                        try {
//                            MotionEvent event = motionEvents.take();
//                            try {
//                                oos.writeInt((int) (event.getDownTime()));
//                                oos.writeInt(event.getAction());
//                                oos.writeInt(event.getPointerCount());
//
//                                for (int i = 0; i < event.getPointerCount(); i++) {
//                                    oos.writeInt(event.getPointerId(i));
//                                    oos.writeInt((int) event.getX(i));
//                                    oos.writeInt((int) event.getY(i));
//                                    oos.writeFloat(event.getPressure(i));
//                                }
//                                oos.writeInt(event.getButtonState());
//                                oos.writeInt(event.getSource());
//                                oos.flush();
//                            } catch (IOException e) {
//                                throw new RuntimeException(e);
//                            }
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                    }
//                }).start();

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

}
