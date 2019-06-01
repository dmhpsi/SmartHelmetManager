package com.darkha.smarthelmetmanager;

public class Constants {
    public interface ACTION {
        String MAIN_ACTION = "main_action";
        String STARTFOREGROUND_ACTION = "start_foreground";
        String STOPFOREGROUND_ACTION = "stop_foreground";
    }

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 111;
    }

    public interface BARCODE {
        String NAME = "BAR_NAME";
        String ADDRESS = "BAR_ADDRESS";
    }
}
