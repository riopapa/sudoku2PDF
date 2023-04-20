package com.riopapa.sudoku2pdf;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

    public Context mContext;
    public Activity mActivity;
    List<String> blankList, pageList;
    final static int MINIMUM_BLANK = 10, MAXIMUM_BLANK = 54;
    final static int MINIMUM_PAGE = 4, MAXIMUM_PAGE = 20;

    SudokuInfo su, su1, su2;

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
        mContext = getApplicationContext();
        mActivity = this;

        su = new ParamsShare().get(mContext, "su");
        su1 = new ParamsShare().get(mContext, "su1");
        su2 = new ParamsShare().get(mContext, "su2");

        ImageButton mesh = findViewById(R.id.mesh);
        if (su.meshType == 0)
            mesh.setImageResource(R.drawable.mesh0_off);
        else if (su.meshType == 1)
            mesh.setImageResource(R.drawable.mesh1_top);
        else
            mesh.setImageResource(R.drawable.mesh2_on);
        mesh.setOnClickListener(view -> {
            su.meshType = (su.meshType+1) % 3;
            if (su.meshType == 0)
                mesh.setImageResource(R.drawable.mesh0_off);
            else if (su.meshType == 1)
                mesh.setImageResource(R.drawable.mesh1_top);
            else
                mesh.setImageResource(R.drawable.mesh2_on);
        });
        TextView tv23 = findViewById(R.id.two_three);
        tv23.setText(su.twoThree+"/Page");
        tv23.setOnClickListener(view -> {
            if (su.twoThree == 2) {
                su.twoThree = 3;
            } else {
                su.twoThree = 2;
            }
            tv23.setText(su.twoThree+"/Page");
            new ParamsShare().put(su, mContext, "su");
        });

        SwitchCompat swAnswer = findViewById(R.id.makeAnswer);
        swAnswer.setChecked(su.makeAnswer);
        swAnswer.setOnClickListener(v -> su.makeAnswer = !su.makeAnswer);

        ImageButton generate = findViewById(R.id.generate);
        generate.setOnClickListener(new View.OnClickListener() {
            boolean isRunning = false;
            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    isRunning = true;
                    Snackbar.make(view, "Starting generation", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    new ParamsShare().put(su, mContext, "su");
                    new MakeSudoku().make(su, mContext, mActivity);
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
        s = su1.blankCount+" blanks, "+su1.pageCount+" pages";
        tvCase1.setText(s);
        TextView tvCase1Load = findViewById(R.id.case1Load);
        tvCase1Load.setOnClickListener(view -> {
            su = su1;
            buildBlankWheel();
            buildPageWheel();
        });
        TextView tvCase1Save = findViewById(R.id.case1Save);
        tvCase1Save.setOnClickListener(view -> {
            su1 = su;
            String s1 = su.blankCount+" blanks, "+su.pageCount+" pages";
            tvCase1.setText(s1);
            new ParamsShare().put(su1, mContext, "su1");
        });
        TextView tvCase2 = findViewById(R.id.case2);
        s = su2.blankCount+" blanks, "+su2.pageCount+" pages";
        tvCase2.setText(s);
        TextView tvCase2Load = findViewById(R.id.case2Load);
        tvCase2Load.setOnClickListener(view -> {
            su = su2;
            buildBlankWheel();
            buildPageWheel();
        });
        TextView tvCase2Save = findViewById(R.id.case2Save);
        tvCase2Save.setOnClickListener(view -> {
            su2 = su;
            String s2 = su.blankCount+" blanks, "+su.pageCount+" pages";
            tvCase2.setText(s2);
            new ParamsShare().put(su2, mContext, "su2");
        });
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
                su.blankCount = Integer.parseInt(blankList.get(newPosition));
                float vol = (float) su.blankCount / (float) MAXIMUM_BLANK / 2;
                wheelView.setPlayVolume(vol);
            }

            @Override
            public void onWheelSelected(int position) {
                su.blankCount = Integer.parseInt(blankList.get(position));
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
//                Log.w(TAG, "onWheelScrollStateChanged: state=" + state);
            }
        });

        wheelView.setData(blankList);
        wheelView.setSelectedItemPosition(su.blankCount - MINIMUM_BLANK, true);
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
                su.pageCount = Integer.parseInt(pageList.get(newPosition));
                float vol = (float) su.pageCount / (float) MAXIMUM_PAGE / 2;
                wheelView.setPlayVolume(vol);
            }

            @Override
            public void onWheelSelected(int position) {
                su.pageCount = Integer.parseInt(pageList.get(position));
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
//                Log.w(TAG, "onWheelScrollStateChanged: state=" + state);
            }
        });

        wheelView.setData(pageList);
        wheelView.setSelectedItemPosition((su.pageCount- MINIMUM_PAGE)/2, true);
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