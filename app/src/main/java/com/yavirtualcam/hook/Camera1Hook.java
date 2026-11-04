package com.yavirtualcam.hook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import java.io.InputStream;

public class Camera1Hook {

    private static final Uri CONFIG =
        Uri.parse("content://com.yavirtualcam.config");

    private static final Uri MEDIA =
        Uri.parse("content://com.yavirtualcam.config/media");

    private static Bitmap cachedBitmap;
    private static String cachedUri = "";
    private static long lastVideoFrame;
    private static long videoPosition;
    private static long videoDuration;
    private static MediaMetadataRetriever retriever;

    public static void install() {
        hookCallback("setPreviewCallback");
        hookCallback("setPreviewCallbackWithBuffer");
        hookCallback("setOneShotPreviewCallback");
        XposedBridge.log("YavirtualCam: Camera1 replacement hooks installed");
    }

    private static void hookCallback(String method) {
        try {
            XposedHelpers.findAndHookMethod(
                Camera.class,
                method,
                Camera.PreviewCallback.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Object original = param.args[0];

                        if (!(original instanceof Camera.PreviewCallback)) {
                            return;
                        }

                        Camera.PreviewCallback callback =
                            (Camera.PreviewCallback) original;

                        param.args[0] = new Camera.PreviewCallback() {
                            @Override
                            public void onPreviewFrame(byte[] data,
                                                       Camera camera) {
                                try {
                                    replaceFrame(data, camera);
                                } catch (Throwable e) {
                                    XposedBridge.log(
                                        "YavirtualCam frame error: " + e);
                                }

                                callback.onPreviewFrame(data, camera);
                            }
                        };
                    }
                }
            );
        } catch (Throwable e) {
            XposedBridge.log("YavirtualCam hook error: " + e);
        }
    }

    private static void replaceFrame(byte[] data, Camera camera)
            throws Exception {

        if (data == null || camera == null) return;

        Context context;
        try {
            Class<?> activityThread = XposedHelpers.findClass(
                "android.app.ActivityThread", null);
            context = (Context) XposedHelpers.callStaticMethod(
                activityThread, "currentApplication");
        } catch (Throwable error) {
            return;
        }

        if (context == null) return;

        Camera.Parameters params = camera.getParameters();
        Camera.Size size = params.getPreviewSize();

        if (size == null) return;

        int width = size.width;
        int height = size.height;

        if (data.length < width * height * 3 / 2) return;

        Bundle config = context.getContentResolver().call(
            CONFIG, "getConfig", null, null);

        if (config == null || !config.getBoolean("enabled", false)) return;

        String targets = config.getString("targets", "");
        String packageName = context.getPackageName();

        if (targets == null ||
            !java.util.Arrays.asList(targets.split(","))
                .contains(packageName)) return;

        String media = config.getString("media_uri", "");
        String type = config.getString("media_type", "");

        if (media == null || media.isEmpty()) return;

        Bitmap bitmap = loadFrame(context, type, media);

        if (bitmap == null) return;

        Bitmap scaled = Bitmap.createScaledBitmap(
            bitmap, width, height, true);

        bitmapToNv21(scaled, data, width, height);

        if (scaled != bitmap) scaled.recycle();
    }

    private static synchronized Bitmap loadFrame(
            Context context, String type, String media) throws Exception {

        if (type != null && type.startsWith("video/")) {
            long now = SystemClock.elapsedRealtime();

            if (retriever == null || !media.equals(cachedUri)) {
                releaseVideo();

                retriever = new MediaMetadataRetriever();
                retriever.setDataSource(context, MEDIA);

                String duration = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION);

                videoDuration = duration == null
                    ? 0 : Long.parseLong(duration) * 1000;

                videoPosition = 0;
                cachedUri = media;
            }

            if (cachedBitmap == null || now - lastVideoFrame >= 100) {
                Bitmap frame = retriever.getFrameAtTime(
                    videoPosition,
                    MediaMetadataRetriever.OPTION_CLOSEST);

                if (frame != null) {
                    if (cachedBitmap != null) cachedBitmap.recycle();
                    cachedBitmap = frame;
                    lastVideoFrame = now;
                }

                videoPosition += 100000;

                if (videoDuration > 0 && videoPosition >= videoDuration) {
                    videoPosition = 0;
                }
            }

            return cachedBitmap;
        }

        if (cachedBitmap == null || !media.equals(cachedUri)) {
            releaseVideo();

            InputStream input =
                context.getContentResolver().openInputStream(MEDIA);

            if (input == null) return null;

            Bitmap decoded;
            try {
                decoded = BitmapFactory.decodeStream(input);
            } finally {
                input.close();
            }

            if (decoded != null) {
                cachedBitmap = decoded;
                cachedUri = media;
            }
        }

        return cachedBitmap;
    }

    private static void bitmapToNv21(
            Bitmap bitmap, byte[] output, int width, int height) {

        int yIndex = 0;
        int uvIndex = width * height;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = bitmap.getPixel(x, y);

                int r = (color >> 16) & 255;
                int g = (color >> 8) & 255;
                int b = color & 255;

                int yy = ((66*r + 129*g + 25*b + 128) >> 8) + 16;
                output[yIndex++] = (byte) Math.max(0, Math.min(255, yy));

                if ((y & 1) == 0 && (x & 1) == 0) {
                    int v = ((112*r - 94*g - 18*b + 128) >> 8) + 128;
                    int u = ((-38*r - 74*g + 112*b + 128) >> 8) + 128;

                    output[uvIndex++] =
                        (byte) Math.max(0, Math.min(255, v));

                    output[uvIndex++] =
                        (byte) Math.max(0, Math.min(255, u));
                }
            }
        }
    }

    private static synchronized void releaseVideo() {
        if (retriever != null) {
            try {
                retriever.release();
            } catch (Exception ignored) {}
        }

        retriever = null;

        if (cachedBitmap != null) {
            cachedBitmap.recycle();
            cachedBitmap = null;
        }

        cachedUri = "";
        videoPosition = 0;
        videoDuration = 0;
    }
}
