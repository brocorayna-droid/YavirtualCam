package com.yavirtualcam;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    SharedPreferences prefs;
    TextView status, mediaLabel, targetsLabel;
    Switch cameraSwitch;
    final ArrayList<String> packages = new ArrayList<>();
    final ArrayList<String> names = new ArrayList<>();
    final ArrayList<Boolean> checked = new ArrayList<>();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("YavirtualCam", MODE_PRIVATE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 30);
        root.setBackgroundColor(Color.rgb(12, 20, 38));

        TextView title = new TextView(this);
        title.setText("YAVIRTUALCAM");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);\n        title.setPadding(8, 16, 8, 16);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        status = label("Virtual camera is OFF");
        status.setGravity(Gravity.CENTER);
        root.addView(status);

        cameraSwitch = new Switch(this);
        cameraSwitch.setText("  Virtual Camera");
        cameraSwitch.setTextColor(Color.WHITE);
        cameraSwitch.setTextSize(18);\n        cameraSwitch.setPadding(8, 16, 8, 16);
        cameraSwitch.setChecked(prefs.getBoolean("enabled", false));
        root.addView(cameraSwitch);

        cameraSwitch.setOnCheckedChangeListener((button, enabled) -> {
            prefs.edit().putBoolean("enabled", enabled).apply();
            status.setText(enabled ? "Virtual camera is ON" : "Virtual camera is OFF");
        });

        addSpace(root);
        addButton(root, "SELECT TARGET APPS", v -> selectTargets());
        targetsLabel = label("Selected apps: " + selectedCount());
        root.addView(targetsLabel);

        addSpace(root);
        addButton(root, "SELECT PHOTO", v -> chooseMedia("image/*"));
        addButton(root, "SELECT VIDEO", v -> chooseMedia("video/*"));

        mediaLabel = label(currentMedia());
        root.addView(mediaLabel);

        addSpace(root);
        addButton(root, "RESET SETTINGS", v -> {
            prefs.edit().clear().apply();
            cameraSwitch.setChecked(false);
            targetsLabel.setText("Selected apps: 0");
            mediaLabel.setText("No media selected");
            status.setText("Virtual camera is OFF");
        });

        TextView note = label("Choose your target apps and media, then switch the virtual camera ON. Camera injection still requires the MochiCloner hook engine.");
        note.setTextSize(13);
        note.setPadding(0, 25, 0, 0);
        root.addView(note);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);

        status.setText(cameraSwitch.isChecked()
            ? "Virtual camera is ON" : "Virtual camera is OFF");
    }

    TextView label(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(Color.WHITE);
        t.setTextSize(16);\n        t.setGravity(Gravity.CENTER_VERTICAL);\n        t.setMaxLines(4);
        t.setPadding(0, 12, 0, 12);
        return t;
    }

    void addSpace(LinearLayout root) {
        View space = new View(this);
        root.addView(space, new LinearLayout.LayoutParams(1, 12));
    }

    void addButton(LinearLayout root, String text, View.OnClickListener click) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setOnClickListener(click);
        root.addView(b, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 54));
    }

    int selectedCount() {
        String s = prefs.getString("targets", "");
        if (s.isEmpty()) return 0;
        return s.split(",").length;
    }

    String currentMedia() {
        String s = prefs.getString("media_uri", "");
        return s.isEmpty() ? "No media selected" : "Selected media: " +
            prefs.getString("media_type", "file");
    }

    void selectTargets() {
        packages.clear();
        names.clear();
        checked.clear();

        PackageManager pm = getPackageManager();
        Intent launchIntent = new Intent(Intent.ACTION_MAIN, null);
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<android.content.pm.ResolveInfo> apps =
            pm.queryIntentActivities(launchIntent, 0);

        String saved = prefs.getString("targets", "");
        Set<String> selected = new HashSet<>();
        if (!saved.isEmpty()) selected.addAll(Arrays.asList(saved.split(",")));

        for (android.content.pm.ResolveInfo r : apps) {
            String pkg = r.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            if (packages.contains(pkg)) continue;
            packages.add(pkg);
            names.add(r.loadLabel(pm).toString());
            checked.add(selected.contains(pkg));
        }

        String[] labels = names.toArray(new String[0]);
        boolean[] states = new boolean[checked.size()];
        for (int i = 0; i < states.length; i++) states[i] = checked.get(i);

        new AlertDialog.Builder(this)
            .setTitle("Choose target apps")
            .setMultiChoiceItems(labels, states, (dialog, which, value) ->
                checked.set(which, value))
            .setPositiveButton("SAVE", (dialog, which) -> {
                ArrayList<String> chosen = new ArrayList<>();
                for (int i = 0; i < packages.size(); i++)
                    if (checked.get(i)) chosen.add(packages.get(i));

                prefs.edit().putString("targets",
                    android.text.TextUtils.join(",", chosen)).apply();
                targetsLabel.setText("Selected apps: " + chosen.size());
            })
            .setNegativeButton("CANCEL", null)
            .show();
    }

    void chooseMedia(String type) {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType(type);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                   Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(i, 50);
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request != 50 || result != RESULT_OK || data == null) return;

        Uri uri = data.getData();
        if (uri == null) return;

        try {
            getContentResolver().takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {}

        String type = getContentResolver().getType(uri);
        prefs.edit()
            .putString("media_uri", uri.toString())
            .putString("media_type", type == null ? "unknown" : type)
            .apply();

        mediaLabel.setText("Selected media: " +
            (type == null ? "unknown file" : type));
    }
}
