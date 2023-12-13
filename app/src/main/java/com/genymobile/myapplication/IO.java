package com.genymobile.myapplication;

import android.system.ErrnoException;
import android.system.Os;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;

public class IO {
    /*
    before read must set byteBuffer limit
     */
    public static void readFully(FileDescriptor fileDescriptor, ByteBuffer buffer) {
        int remaining = buffer.remaining();
        System.out.println("remaining "+remaining);
        while (remaining > 0) {
            try {
                remaining -= Os.read(fileDescriptor, buffer);
            } catch (Exception e) {
//                System.out.println(" read bad ...");

                try {
                    System.out.println("sleep..."+e);
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        System.out.println("read finish ");
    }

    public static void readBytes(InputStream inputStream, byte[] volume,int size)  {
        int finishSize=0;
        while (finishSize<size){
            try {
                finishSize += inputStream.read(volume, finishSize, size - finishSize);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
