package com.darkha.smarthelmetmanager;

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
}
