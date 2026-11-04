package com.yavirtualcam.hook;

import android.hardware.Camera;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class Camera1Hook {

    public static void install() {
        hookCallback("setPreviewCallback");
        hookCallback("setPreviewCallbackWithBuffer");
        hookCallback("setOneShotPreviewCallback");
    }

    private static void hookCallback(String method) {
        XposedBridge.hookAllMethods(Camera.class, method,
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (param.args.length == 0 ||
                        !(param.args[0] instanceof Camera.PreviewCallback)) {
                        return;
                    }

                    Camera.PreviewCallback original =
                        (Camera.PreviewCallback) param.args[0];

                    param.args[0] = new Camera.PreviewCallback() {
                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera) {
                            replaceFrame(data, camera);
                            original.onPreviewFrame(data, camera);
                        }
                    };
                }
            });
    }

    private static void replaceFrame(byte[] data, Camera camera) {
        if (data == null || camera == null) return;

        try {
            Camera.Size size = camera.getParameters().getPreviewSize();
            int ySize = size.width * size.height;

            if (data.length < ySize * 3 / 2) return;

            for (int i = 0; i < ySize; i++) {
                data[i] = (byte) 128;
            }

            for (int i = ySize; i < ySize * 3 / 2; i++) {
                data[i] = (byte) 128;
            }

            XposedBridge.log("YavirtualCam: Test frame injected");
        } catch (Throwable error) {
            XposedBridge.log("YavirtualCam frame error: " + error);
        }
    }
}
