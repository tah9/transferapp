package com.genymobile.transferclient;

import android.system.Os;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class IO {
    /*
    before read must set byteBuffer limit
     */
    public static void readFully(FileDescriptor fileDescriptor, ByteBuffer buffer) {
        int remaining = buffer.remaining();
//        System.out.println("remaining "+remaining);
        while (remaining > 0) {
            try {
                remaining -= Os.read(fileDescriptor, buffer);
            } catch (Exception e) {
                try {
                    System.out.println(" read bad ...sleep..." + e);
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
//        System.out.println("read finish ");
    }

    private static final String TAG = "IO";
    public static void readBytes(InputStream inputStream, byte[] volume, int size) {
        int finishSize = 0;
        Log.d(TAG, "readBytes: size "+size);
        while (finishSize < size) {
            try {
                finishSize += inputStream.read(volume, finishSize, size - finishSize);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Log.d(TAG, "readBytes: finishSize "+finishSize);

    }
}
