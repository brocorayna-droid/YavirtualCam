package com.yavirtualcam.hook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedInit implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam app) {
        if (app.packageName.equals("com.yavirtualcam")) return;

        XposedBridge.log("YavirtualCam: loaded in " + app.packageName);
        Camera1Hook.install();
        Camera2Hook.install();
    }
}
