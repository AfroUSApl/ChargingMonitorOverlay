package com.thomas.chargingoverlay;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SysReader {

    public static String read(String path) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "cat " + path});
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            return reader.readLine();
        } catch (Exception e) {
            return null;
        }
    }
}