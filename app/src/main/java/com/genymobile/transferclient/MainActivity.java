package com.genymobile.transferclient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
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




        new Thread(()->{
            String targetJarFileName = "transfer.jar";
            FileUtils.copyAssetsFileToAdbPath(this, "finish.dex", targetJarFileName);
            try {

                String path = getFilesDir().getAbsolutePath() + "/" + targetJarFileName;

                String terminal = "CLASSPATH=" + path + " app_process / com.genymobile.transfer.Server ";

                String suTerminal = "su -c " + terminal;
                Log.d(TAG, "onCreate: suTerminal=" + suTerminal);
//                Process process = Runtime.getRuntime().exec(suTerminal);
                String result = RunProcess.runProcess("su -c cmd package resolve-activity --brief -c android.intent.category.LAUNCHER bin.mt.plus");

                Log.d(TAG, "onCreate: finish process");
                Log.d(TAG, "result="+result);

                //息屏代码
//            Process process = Runtime.getRuntime().exec("su -c input keyevent KEYCODE_POWER");



            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();



//        Process process;
//        try {
//            process = Runtime.getRuntime().exec("su");
//            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
//
//            // 运行 adb 命令直接打印屏幕信息
////            outputStream.writeBytes("dumpsys window\n");
//
//            String terminal = "CLASSPATH=/data/data/com.genymobile.transferclient/files/transfer.bak app_process / com.genymobile.transfer.Server";
//            Log.d(TAG, "onCreate: "+terminal);
////            outputStream.writeBytes("export CLASSPATH=/data/data/com.genymobile.transferclient/files/transfer.bak");
////            outputStream.writeBytes("export CLASSPATH=/data/data/com.genymobile.transferclient/files/transfer.bak;exec app_process /system/bin/ com.genymobile.transfer.Server");
//            outputStream.writeBytes(terminal);
//
//            // 退出 root 权限
////            outputStream.writeBytes("exit\n");
//            outputStream.flush();
//            process.waitFor();
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            StringBuilder output = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                output.append(line).append("\n");
//            }
//
//            // 打印屏幕信息
//            setTitle(output);
//            System.out.println(output.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


//        new Thread(()->{
//
//        try {
//            Socket socket = new Socket("192.168.43.1", 5555);
//            Log.d(TAG, "onCreate: ++++++++");
//
//            Socket socket2 = new Socket("192.168.43.248", 42663);
//            Log.d(TAG, "onCreate: 2++++++++");
//
//
//        } catch (IOException e) {
//            Log.d(TAG, "onCreate: ---------");
////            throw new RuntimeException(e);
//        }  }).start();

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        Log.d(TAG, "onCreate: " + displayMetrics);
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
                int port = 20001;
                ServerSocket serverSocket = new ServerSocket(port);
                Log.d(TAG, "socket create finish " + port);

                Socket videoSocket = serverSocket.accept();
                Log.d(TAG, "onSurfaceTextureAvailable: videoSocket");
                InputStream inputStream = videoSocket.getInputStream();
                FileDescriptor fileDescriptor = ParcelFileDescriptor.fromSocket(videoSocket).getFileDescriptor();
//                videoSocket.close();

                WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics displayMetrics = new DisplayMetrics();
                windowManager.getDefaultDisplay().getMetrics(displayMetrics);
                VideoDecoder videoDecoder = new VideoDecoder(new Surface(surface),
//                        # 注意宽高要传入2的倍数
//                        displayMetrics.widthPixels,
//                        displayMetrics.heightPixels,
                        1080, 2326,
                        fileDescriptor, inputStream);
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
