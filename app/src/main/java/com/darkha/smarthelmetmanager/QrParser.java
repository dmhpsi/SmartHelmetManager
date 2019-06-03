package com.darkha.smarthelmetmanager;

import android.util.Log;

import java.util.Arrays;

public class QrParser {
    private String name = null;
    private String address = null;

    public QrParser(String input) {
        String[] parts = input.split("//");
        Log.e("Parser", Arrays.toString(parts));
        if (parts.length > 2) {
            if (parts[0].equals("gosunday:")) {
                name = parts[1];
                address = parts[2];
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address.toUpperCase();
    }
}
