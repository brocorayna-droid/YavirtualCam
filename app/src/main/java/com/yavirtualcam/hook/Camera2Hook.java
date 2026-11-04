package com.yavirtualcam.hook;

import android.hardware.camera2.CameraManager;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class Camera2Hook {

    public static void install() {
        try {
            XposedBridge.hookAllMethods(
                CameraManager.class,
                "openCamera",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        XposedBridge.log(
                            "YavirtualCam: Camera2 camera request detected"
                        );
                    }
                }
            );

            XposedBridge.log("YavirtualCam: Camera2 hook installed");

        } catch (Throwable error) {
            XposedBridge.log("YavirtualCam Camera2 error: " + error);
        }
    }
}
