package com.darkha.smarthelmetmanager;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

class Functions {
    private static final Functions ourInstance = new Functions();
    private final char[] hexArray = "0123456789ABCDEF".toCharArray();

    private Functions() {
    }

    static Functions getInstance() {
        return ourInstance;
    }

    byte[] muxWrap(String cmd, int link) {
        byte[] outbuf = new byte[cmd.length() + 5];
        int pos = 0; //0xFF for control channel
        int len = cmd.length();//Calc. length of ASCII command
        //Generate packet
        outbuf[pos++] = (byte) 0xbf;//SOF
        outbuf[pos++] = (byte) link;//Link  (0xFF=Control,  0x00  =  connection  1, etc.)
        outbuf[pos++] = (byte) 0;//Flags
        outbuf[pos++] = (byte) len;//Length
        //Insert data into correct position in the frame
        for (byte e : cmd.getBytes()) {
            outbuf[pos++] = e;
        }
        outbuf[pos] = (byte) (link ^ 0xff);//nlink
        return outbuf;
    }

    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public String bytesToHex(byte[] bytes, int length) {
        char[] hexChars = new char[length * 2];
        for (int j = 0; j < length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public JSONArray wrapList(List<ListDataWrapper> data) {
        JSONArray array = new JSONArray();
        for (ListDataWrapper item : data) {
            array.put(item.toJSON());
        }
        return array;
    }

    public List<ListDataWrapper> dewrapList(JSONArray array) {
        List<ListDataWrapper> data = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                data.add(new ListDataWrapper(array.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View f = activity.getCurrentFocus();
        if (null != f && null != f.getWindowToken() && EditText.class.isAssignableFrom(f.getClass()))
            imm.hideSoftInputFromWindow(f.getWindowToken(), 0);
        else
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
