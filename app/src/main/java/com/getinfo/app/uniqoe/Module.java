package com.getinfo.app.uniqoe;
import com.getinfo.app.uniqoe.utils.ScreenRecorder;
import com.getinfo.sdk.qoemaster.*;

public class Module {
   private static ScreenRecorder screenRecorder;

    public static ScreenRecorder getScreenRecorder() {
        return screenRecorder;
    }

    public static void setScreenRecorder(ScreenRecorder screenRecorder) {
        Module.screenRecorder = screenRecorder;
    }
}
