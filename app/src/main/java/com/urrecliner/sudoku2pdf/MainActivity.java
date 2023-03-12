package com.urrecliner.sudoku2pdf;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.snackbar.Snackbar;
import com.urrecliner.sudoku2pdf.Model.SudokuInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Context context;
    int blankCount = 5;
    int pageCount = 0;
    int twoThree = 2;
    boolean meshable;
    static TextView tvSstatus;
    static String fileDate;
    List<String> blankList, pageList;
    final static int MINIMUM_BLANK = 10, MAXIMUM_BLANK = 58;
    final static int MINIMUM_PAGE = 4, MAXIMUM_PAGE = 20;
    static ProgressBar progressBar;
    static FrameLayout frameLayout;
    static ConstraintLayout mainLayout;
    static TextView horizontalLineView;
    SharedPreferences mSettings = null;
    SharedPreferences.Editor editor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        editor = mSettings.edit();
        pageCount = mSettings.getInt("pageCount", 16);
        blankCount = mSettings.getInt("blankCount", 16);
        meshable = mSettings.getBoolean("mesh", true);
        twoThree = mSettings.getInt("twoThree", 2);

        tvSstatus = findViewById(R.id.status);
        tvSstatus.setVisibility(View.INVISIBLE);

        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.circle, null);
        progressBar = findViewById(R.id.progress_circle);
        progressBar.setVisibility(View.INVISIBLE);

        progressBar.setProgress(0);   // Main Progress
        progressBar.setSecondaryProgress(100); // Secondary Progress
        progressBar.setMax(100); // Maximum Progress
        progressBar.setProgressDrawable(drawable);

        frameLayout = findViewById(R.id.progress_frame);

        mainLayout = findViewById(R.id.mainLayout);
        horizontalLineView = findViewById(R.id.horizontal_line);

        context = getApplicationContext();
        ImageButton mesh = findViewById(R.id.mesh);
        mesh.setImageResource((meshable)? R.drawable.mesh_on : R.drawable.mesh_off);
        mesh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                meshable = !meshable;
                mesh.setImageResource((meshable) ? R.drawable.mesh_on : R.drawable.mesh_off);
                editor.putBoolean("mesh", meshable).apply();
            }
        });
        TextView tv23 = findViewById(R.id.two_three);
        tv23.setText(twoThree+"/Page");
        tv23.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (twoThree == 2) {
                    twoThree = 3;
                } else {
                    twoThree = 2;
                }
                tv23.setText(twoThree+"/Page");
                editor.putInt("twoThree", twoThree).apply();
            }
        });

        ImageButton generate = findViewById(R.id.generate);
        generate.setOnClickListener(new View.OnClickListener() {
            boolean isRunning = false;
            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    editor.putInt("blankCount", blankCount).apply();
                    editor.putInt("pageCount", pageCount).apply();
                    isRunning = true;
                    Snackbar.make(view, "Starting generation", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    final SimpleDateFormat sdfDate = new SimpleDateFormat("yy-MM-dd HH.mm.ss", Locale.US);
                    fileDate =  "sudoku_"+sdfDate.format(System.currentTimeMillis())+" ("+twoThree+"."+blankCount+")";

                    SudokuInfo sudokuInfo = new SudokuInfo();
                    sudokuInfo.dateTime = sdfDate.format(System.currentTimeMillis());
                    sudokuInfo.blankCount = blankCount;
                    sudokuInfo.pageCount = pageCount;
                    sudokuInfo.twoThree = twoThree;
                    sudokuInfo.context = context;
                    sudokuInfo.meshable = meshable;
                    new MakeSudoku().make(sudokuInfo);
                    isRunning = false;
                }
            }
        });

        blankList = new ArrayList<>();
        for (int level = MINIMUM_BLANK; level <= MAXIMUM_BLANK; level++) {
            blankList.add(level + "");
        }
        pageList = new ArrayList<>();
        for (int page = MINIMUM_PAGE; page <= MAXIMUM_PAGE; page += 2) {
            pageList.add(page + "");
        }

        buildBlankWheel();
        buildPageWheel();

    }

    private void buildBlankWheel() {

        final TextView tV = findViewById(R.id.blank_count);
        tV.setText(""+ blankCount);

        final WheelView<String> wheelView = findViewById(R.id.wheel_level);
        wheelView.setOnItemSelectedListener(new WheelView.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(WheelView<String> wheelView, String data, int position) {
//                Log.w(TAG, "onItemSelected: data=" + data + ",position=" + position);
            }
        });
        wheelView.setOnWheelChangedListener(new WheelView.OnWheelChangedListener() {
            @Override
            public void onWheelScroll(int scrollOffsetY) {
//                Log.w(TAG, "onWheelScroll: scrollOffsetY=" + scrollOffsetY);
            }

            @Override
            public void onWheelItemChanged(int oldPosition, int newPosition) {
                blankCount = Integer.parseInt(blankList.get(newPosition));
                tV.setText(""+ blankCount);
                float vol = (float) blankCount / (float) MAXIMUM_BLANK / 2;
                wheelView.setPlayVolume(vol);
            }

            @Override
            public void onWheelSelected(int position) {
                blankCount = Integer.parseInt(blankList.get(position));
                tV.setText(""+ blankCount);
                editor.putInt("blankCount", blankCount).apply();
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
//                Log.w(TAG, "onWheelScrollStateChanged: state=" + state);
            }
        });

        wheelView.setData(blankList);
        wheelView.setSelectedItemPosition(blankCount - MINIMUM_BLANK, true);
        wheelView.setSoundEffect(true);
        wheelView.setSoundEffectResource(R.raw.level_degree);
        wheelView.setPlayVolume(0.1f);

    }

    private void buildPageWheel() {

        final TextView tVPage = findViewById(R.id.page_count);
        tVPage.setText(""+ pageCount);

        final WheelView<String> wheelView = findViewById(R.id.wheel_count);
        wheelView.setOnItemSelectedListener(new WheelView.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(WheelView<String> wheelView, String data, int position) {
//                Log.w(TAG, "onItemSelected: data=" + data + ",position=" + position);
            }
        });
        wheelView.setOnWheelChangedListener(new WheelView.OnWheelChangedListener() {
            @Override
            public void onWheelScroll(int scrollOffsetY) {
//                Log.w(TAG, "onWheelScroll: scrollOffsetY=" + scrollOffsetY);
            }

            @Override
            public void onWheelItemChanged(int oldPosition, int newPosition) {
                pageCount = Integer.parseInt(pageList.get(newPosition));
                tVPage.setText(""+ pageCount);
                editor.putInt("pageCount", pageCount).apply();
                float vol = (float) pageCount / (float) MAXIMUM_PAGE / 2;
                wheelView.setPlayVolume(vol);
            }

            @Override
            public void onWheelSelected(int position) {
                pageCount = Integer.parseInt(pageList.get(position));
                tVPage.setText(""+ pageCount);
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
//                Log.w(TAG, "onWheelScrollStateChanged: state=" + state);
            }
        });

        wheelView.setData(pageList);
        wheelView.setSelectedItemPosition((pageCount- MINIMUM_PAGE)/2, true);
        wheelView.setSoundEffect(true);
        wheelView.setSoundEffectResource(R.raw.page_count);
        wheelView.setPlayVolume(0.1f);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}