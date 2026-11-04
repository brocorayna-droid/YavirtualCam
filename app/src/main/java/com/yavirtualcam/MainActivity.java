package com.yavirtualcam;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.graphics.Color;
import android.view.ViewGroup;

public class MainActivity extends Activity {

    FrameLayout preview;
    ImageView image;
    VideoView video;
    View selected;
    float x = 0, y = 0, sx = 1, sy = 1;
    int step = 30;
    int expandStep = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 16, 16, 16);
        root.setBackgroundColor(Color.rgb(12, 20, 38));

        TextView title = new TextView(this);
        title.setText("YAVIRTUALCAM");
        title.setTextSize(24);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        preview = new FrameLayout(this);
        preview.setBackgroundColor(Color.rgb(30, 42, 62));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 300);
        pp.setMargins(0, 18, 0, 18);
        root.addView(preview, pp);

        image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        video = new VideoView(this);

        LinearLayout gallery = new LinearLayout(this);
        Button photo = button("Select Photo");
        Button movie = button("Select Video");
        gallery.addView(photo, new LinearLayout.LayoutParams(0, 55, 1));
        gallery.addView(movie, new LinearLayout.LayoutParams(0, 55, 1));
        root.addView(gallery);

        photo.setOnClickListener(v -> chooseMedia("image/*"));
        movie.setOnClickListener(v -> chooseMedia("video/*"));

        addLabel(root, "POSITION");
        LinearLayout movement = new LinearLayout(this);
        Button up = button("↑");
        Button down = button("↓");
        Button left = button("←");
        Button right = button("→");
        movement.addView(left, new LinearLayout.LayoutParams(0, 55, 1));
        movement.addView(up, new LinearLayout.LayoutParams(0, 55, 1));
        movement.addView(down, new LinearLayout.LayoutParams(0, 55, 1));
        movement.addView(right, new LinearLayout.LayoutParams(0, 55, 1));
        root.addView(movement);

        left.setOnClickListener(v -> move(-step, 0));
        right.setOnClickListener(v -> move(step, 0));
        up.setOnClickListener(v -> move(0, -step));
        down.setOnClickListener(v -> move(0, step));

        addLabel(root, "EXPANSION");
        LinearLayout expansion = new LinearLayout(this);
        Button exLeft = button("Expand ←");
        Button exRight = button("Expand →");
        Button exUp = button("Expand ↑");
        Button exDown = button("Expand ↓");
        expansion.addView(exLeft, new LinearLayout.LayoutParams(0, 55, 1));
        expansion.addView(exRight, new LinearLayout.LayoutParams(0, 55, 1));
        expansion.addView(exUp, new LinearLayout.LayoutParams(0, 55, 1));
        expansion.addView(exDown, new LinearLayout.LayoutParams(0, 55, 1));
        root.addView(expansion);

        exLeft.setOnClickListener(v -> resize(-0.1f, 0));
        exRight.setOnClickListener(v -> resize(0.1f, 0));
        exUp.setOnClickListener(v -> resize(0, -0.1f));
        exDown.setOnClickListener(v -> resize(0, 0.1f));

        LinearLayout actions = new LinearLayout(this);
        Button play = button("Play / Pause");
        Button reset = button("Reset");
        Button remove = button("Remove");
        actions.addView(play, new LinearLayout.LayoutParams(0, 55, 1));
        actions.addView(reset, new LinearLayout.LayoutParams(0, 55, 1));
        actions.addView(remove, new LinearLayout.LayoutParams(0, 55, 1));
        root.addView(actions);

        play.setOnClickListener(v -> {
            if (selected == video) {
                if (video.isPlaying()) video.pause();
                else video.start();
            }
        });

        reset.setOnClickListener(v -> resetMedia());
        remove.setOnClickListener(v -> {
            preview.removeAllViews();
            selected = null;
            x = y = 0;
            sx = sy = 1;
        });

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }

    Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(10);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setPadding(2, 0, 2, 0);
        b.setAllCaps(false);
        return b;
    }

    void addLabel(LinearLayout root, String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(Color.WHITE);
        label.setTextSize(15);
        label.setPadding(0, 15, 0, 5);
        root.addView(label);
    }

    void chooseMedia(String type) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType(type);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String type = getContentResolver().getType(uri);

            preview.removeAllViews();

            if (type != null && type.startsWith("video/")) {
                video = new VideoView(this);
                video.setVideoURI(uri);
                preview.addView(video, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
                selected = video;
                video.start();
            } else {
                image = new ImageView(this);
                image.setImageURI(uri);
                image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                preview.addView(image, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
                selected = image;
            }

            resetMedia();
        }
    }

    void move(float dx, float dy) {
        if (selected == null) return;
        x += dx;
        y += dy;
        updateMedia();
    }

    void resize(float dx, float dy) {
        if (selected == null) return;
        sx = Math.max(0.2f, Math.min(4f, sx + dx));
        sy = Math.max(0.2f, Math.min(4f, sy + dy));
        updateMedia();
    }

    void updateMedia() {
        selected.setTranslationX(x);
        selected.setTranslationY(y);
        selected.setScaleX(sx);
        selected.setScaleY(sy);
    }

    void resetMedia() {
        x = y = 0;
        sx = sy = 1;
        if (selected != null) updateMedia();
    }
}
