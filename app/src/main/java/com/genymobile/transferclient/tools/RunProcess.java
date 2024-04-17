package com.genymobile.transferclient.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RunProcess {
    public static String runProcess(String terminal) throws Exception {
        Process process = Runtime.getRuntime().exec(terminal);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        return output.toString();
    }

    public static void runProcessAsync(String terminal) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process process = Runtime.getRuntime().exec(terminal);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
