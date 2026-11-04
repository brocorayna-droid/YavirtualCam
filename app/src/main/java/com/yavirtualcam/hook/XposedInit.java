package com.yavirtualcam.hook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedInit implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {

        String packageName = lpparam.packageName;

        if (packageName.equals("com.whatsapp")
                || packageName.equals("com.facebook.katana")
                || packageName.equals("org.telegram.messenger")) {

            XposedBridge.log("YavirtualCam loaded in: " + packageName);
            Camera1Hook.install();
            Camera2Hook.install();
        }
    }
}
