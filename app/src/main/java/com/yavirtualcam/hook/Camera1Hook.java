package com.yavirtualcam.hook;

import android.hardware.Camera;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class Camera1Hook {

    public static void install() {
        try {
            XposedHelpers.findAndHookMethod(
                Camera.class,
                "setPreviewCallback",
                Camera.PreviewCallback.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        XposedBridge.log(
                            "YavirtualCam: Camera1 preview callback detected"
                        );
                    }
                }
            );

            XposedBridge.log("YavirtualCam: Camera1 hook installed");

        } catch (Throwable error) {
            XposedBridge.log("YavirtualCam Camera1 error: " + error);
        }
    }
}
