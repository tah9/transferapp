package com.genymobile.transferclient;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private TextureView textureView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
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
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(20002);
                Socket videoSocket = serverSocket.accept();
                Log.d(TAG, "onSurfaceTextureAvailable: videoSocket");
                InputStream inputStream = videoSocket.getInputStream();
                FileDescriptor fileDescriptor = ParcelFileDescriptor.fromSocket(videoSocket).getFileDescriptor();
//                videoSocket.close();
                VideoDecoder videoDecoder = new VideoDecoder(new Surface(surface), 1080, 2160, fileDescriptor, inputStream);
                Socket controlSocket = serverSocket.accept();
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
                                oos.writeInt(event.getAction());
                                oos.writeInt(event.getPointerCount());

                                for (int i = 0; i < event.getPointerCount(); i++) {
                                    oos.writeInt(event.getPointerId(i));
                                    oos.writeInt((int) event.getX(i));
                                    oos.writeInt((int) event.getY(i));
                                    oos.writeFloat(event.getPressure(i));
                                }
                                oos.writeInt(event.getButtonState());
                                oos.writeInt(event.getSource());
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

}
