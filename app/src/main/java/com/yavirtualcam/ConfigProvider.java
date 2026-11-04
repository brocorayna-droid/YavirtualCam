package com.yavirtualcam;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.content.SharedPreferences;

public class ConfigProvider extends ContentProvider {
    @Override
    public boolean onCreate() { return true; }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        SharedPreferences p = getContext().getSharedPreferences("YavirtualCam", 0);
        Bundle b = new Bundle();

        b.putBoolean("enabled", p.getBoolean("enabled", false));
        b.putString("targets", p.getString("targets", ""));
        b.putString("media_uri", p.getString("media_uri", ""));
        b.putString("media_type", p.getString("media_type", ""));

        return b;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) { return null; }

    @Override
    public String getType(Uri uri) { return "application/octet-stream"; }

    @Override
    public Uri insert(Uri uri, ContentValues values) { return null; }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) { return 0; }
}
