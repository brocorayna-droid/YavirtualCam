package com.yavirtualcam;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import java.io.FileNotFoundException;

public class ConfigProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        SharedPreferences p =
            getContext().getSharedPreferences("YavirtualCam", 0);

        Bundle b = new Bundle();
        b.putBoolean("enabled", p.getBoolean("enabled", false));
        b.putString("targets", p.getString("targets", ""));
        b.putString("media_uri", p.getString("media_uri", ""));
        b.putString("media_type", p.getString("media_type", ""));
        return b;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {
        if (!"r".equals(mode) || !"media".equals(uri.getLastPathSegment())) {
            throw new FileNotFoundException("Invalid media request");
        }

        SharedPreferences p =
            getContext().getSharedPreferences("YavirtualCam", 0);

        String saved = p.getString("media_uri", "");
        if (saved.isEmpty()) {
            throw new FileNotFoundException("No media selected");
        }

        return getContext().getContentResolver()
            .openFileDescriptor(Uri.parse(saved), "r");
    }

    @Override
    public Cursor query(Uri u, String[] p, String s,
                        String[] a, String o) { return null; }

    @Override
    public String getType(Uri u) {
        return "application/octet-stream";
    }

    @Override
    public Uri insert(Uri u, ContentValues v) { return null; }

    @Override
    public int delete(Uri u, String s, String[] a) { return 0; }

    @Override
    public int update(Uri u, ContentValues v, String s, String[] a) {
        return 0;
    }
}
