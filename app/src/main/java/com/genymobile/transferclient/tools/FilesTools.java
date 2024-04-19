package com.genymobile.transferclient.tools;

import android.webkit.MimeTypeMap;

import java.io.File;

public class FilesTools {
    public static String getMimeType(File file) {
        String extension = getFileExtension(file.getName());
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getMimeTypeFromExtension(extension);
        }
        return null;
    }

    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return null;
        }
        return fileName.substring(dotIndex + 1);
    }
}
