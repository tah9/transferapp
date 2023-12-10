package com.genymobile.myapplication;

import android.system.ErrnoException;
import android.system.Os;

import java.io.FileDescriptor;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;

public class IO {
    public static int readFully(FileDescriptor fileDescriptor, ByteBuffer buffer,int len) {
        int bytesRead = 0;
        System.out.println("readFully len "+len);
        while (bytesRead < len) {
            System.out.println("byteRead start " + bytesRead);

            int result = 0;
            try {
                result = Os.read(fileDescriptor, buffer);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (result == -1) {
                // 处理读取错误
                break;
            } else {
                bytesRead += result; // 更新已读取字节数
            }
        }
        System.out.println("byteRead end " + bytesRead);
        return bytesRead;
    }
}
