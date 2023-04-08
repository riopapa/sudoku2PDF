package com.riopapa.sudoku2pdf;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.riopapa.sudoku2pdf.Model.SudokuInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Context context;
    int blankCount = 5, blankCount1, blankCount2;
    int pageCount = 0, pageCount1, pageCount2;
    int twoThree = 2;
    int meshType = 1;
    Boolean makeAnswer = false;
    static String fileDate;
    List<String> blankList, pageList;
    final static int MINIMUM_BLANK = 10, MAXIMUM_BLANK = 54;
    final static int MINIMUM_PAGE = 4, MAXIMUM_PAGE = 20;
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
        meshType = mSettings.getInt("mesh", 1);
        twoThree = mSettings.getInt("twoThree", 2);
        makeAnswer = mSettings.getBoolean("makeAnswer", false);

        pageCount1 = mSettings.getInt("pageCount1", 6);
        blankCount1 = mSettings.getInt("blankCount1", 12);

        pageCount2 = mSettings.getInt("pageCount2", 16);
        blankCount2 = mSettings.getInt("blankCount2", 30);

//        tvStatus = findViewById(R.id.status);
//        tvStatus.setVisibility(View.INVISIBLE);


        context = getApplicationContext();
        ImageButton mesh = findViewById(R.id.mesh);
        if (meshType == 0)
            mesh.setImageResource(R.drawable.mesh0_off);
        else if (meshType == 1)
            mesh.setImageResource(R.drawable.mesh1_top);
        else
            mesh.setImageResource(R.drawable.mesh2_on);
        mesh.setOnClickListener(view -> {
            meshType = (meshType+1) % 3;
            if (meshType == 0)
                mesh.setImageResource(R.drawable.mesh0_off);
            else if (meshType == 1)
                mesh.setImageResource(R.drawable.mesh1_top);
            else
                mesh.setImageResource(R.drawable.mesh2_on);
            saveValues();
        });
        TextView tv23 = findViewById(R.id.two_three);
        tv23.setText(twoThree+"/Page");
        tv23.setOnClickListener(view -> {
            if (twoThree == 2) {
                twoThree = 3;
            } else {
                twoThree = 2;
            }
            tv23.setText(twoThree+"/Page");
            editor.putInt("twoThree", twoThree).apply();
        });

        SwitchCompat swAnswer = findViewById(R.id.makeAnswer);
        swAnswer.setChecked(makeAnswer);
        swAnswer.setOnClickListener(v -> makeAnswer = !makeAnswer);

        ImageButton generate = findViewById(R.id.generate);
        generate.setOnClickListener(new View.OnClickListener() {
            boolean isRunning = false;
            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    saveValues();
                    isRunning = true;
                    Snackbar.make(view, "Starting generation", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    final SimpleDateFormat sdfDate = new SimpleDateFormat("yy-MM-dd HH.mm.ss", Locale.US);
                    fileDate =  "sudoku_"+sdfDate.format(System.currentTimeMillis())+" ("+twoThree+"."+blankCount+")";

                    Activity activity = MainActivity.this;
                    SudokuInfo sudokuInfo = new SudokuInfo();
                    sudokuInfo.dateTime = sdfDate.format(System.currentTimeMillis());
                    sudokuInfo.blankCount = blankCount;
                    sudokuInfo.pageCount = pageCount;
                    sudokuInfo.twoThree = twoThree;
                    sudokuInfo.context = context;
                    sudokuInfo.activity = activity;
                    sudokuInfo.meshType = meshType;
                    sudokuInfo.makeAnswer = makeAnswer;
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

        String s;
        TextView tvCase1 = findViewById(R.id.case1);
        s = blankCount1+" blanks, "+pageCount1+" pages";
        tvCase1.setText(s);
        TextView tvCase1Load = findViewById(R.id.case1Load);
        tvCase1Load.setOnClickListener(view -> {
            blankCount = blankCount1;
            pageCount = pageCount1;
            buildBlankWheel();
            buildPageWheel();
        });
        TextView tvCase1Save = findViewById(R.id.case1Save);
        tvCase1Save.setOnClickListener(view -> {
            blankCount1 = blankCount;
            pageCount1 =  pageCount;
            String s1 = blankCount+" blanks, "+pageCount+" pages";
            tvCase1.setText(s1);
            saveValues();
        });
        TextView tvCase2 = findViewById(R.id.case2);
        s = blankCount2+" blanks, "+pageCount2+" pages";
        tvCase2.setText(s);
        TextView tvCase2Load = findViewById(R.id.case2Load);
        tvCase2Load.setOnClickListener(view -> {
            blankCount = blankCount2;
            pageCount = pageCount2;
            buildBlankWheel();
            buildPageWheel();
        });
        TextView tvCase2Save = findViewById(R.id.case2Save);
        tvCase2Save.setOnClickListener(view -> {
            blankCount2 = blankCount;
            pageCount2 =  pageCount;
            String s12 = blankCount+" blanks, "+pageCount+" pages";
            tvCase2.setText(s12);
            saveValues();
        });
    }
    private void saveValues() {
        editor.putInt("blankCount", blankCount).apply();
        editor.putInt("pageCount", pageCount).apply();
        editor.putInt("blankCount1", blankCount1).apply();
        editor.putInt("pageCount1", pageCount1).apply();
        editor.putInt("blankCount2", blankCount2).apply();
        editor.putInt("pageCount2", pageCount2).apply();
        editor.putInt("mesh", meshType).apply();
        editor.putBoolean("makeAnswer", makeAnswer).apply();
    }

    private void buildBlankWheel() {

        final WheelView<String> wheelView = findViewById(R.id.wheel_level);
        wheelView.setOnItemSelectedListener((wheelView1, data, position) -> {
//                Log.w(TAG, "onItemSelected: data=" + data + ",position=" + position);
        });
        wheelView.setOnWheelChangedListener(new WheelView.OnWheelChangedListener() {
            @Override
            public void onWheelScroll(int scrollOffsetY) {
//                Log.w(TAG, "onWheelScroll: scrollOffsetY=" + scrollOffsetY);
            }

            @Override
            public void onWheelItemChanged(int oldPosition, int newPosition) {
                blankCount = Integer.parseInt(blankList.get(newPosition));
                float vol = (float) blankCount / (float) MAXIMUM_BLANK / 2;
                wheelView.setPlayVolume(vol);
            }

            @Override
            public void onWheelSelected(int position) {
                blankCount = Integer.parseInt(blankList.get(position));
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

        final WheelView<String> wheelView = findViewById(R.id.wheel_count);
        wheelView.setOnItemSelectedListener((wheelView1, data, position) -> {
//                Log.w(TAG, "onItemSelected: data=" + data + ",position=" + position);
        });
        wheelView.setOnWheelChangedListener(new WheelView.OnWheelChangedListener() {
            @Override
            public void onWheelScroll(int scrollOffsetY) {
//                Log.w(TAG, "onWheelScroll: scrollOffsetY=" + scrollOffsetY);
            }

            @Override
            public void onWheelItemChanged(int oldPosition, int newPosition) {
                pageCount = Integer.parseInt(pageList.get(newPosition));
                editor.putInt("pageCount", pageCount).apply();
                float vol = (float) pageCount / (float) MAXIMUM_PAGE / 2;
                wheelView.setPlayVolume(vol);
            }

            @Override
            public void onWheelSelected(int position) {
                pageCount = Integer.parseInt(pageList.get(position));
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