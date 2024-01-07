package com.riopapa.sudoku2pdf;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.riopapa.sudoku2pdf.Model.SudokuInfo;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public Context mContext;
    public Activity mActivity;
    List<String> blankList, pageList;
    final static int MINIMUM_BLANK = 10, MAXIMUM_BLANK = 54;
    final static int MINIMUM_PAGE = 4, MAXIMUM_PAGE = 20;
    ImageButton btnMesh;
    TextView tv23;
    EditText alpha;    // 255 : real black

    SudokuInfo su, su1, su2;

    @SuppressLint("SetTextI18n")
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

        btnMesh = findViewById(R.id.mesh);
        btnMesh.setOnClickListener(view -> {
            su.meshType = (su.meshType+1) % 3;
            showMesh(su.meshType);
            new ParamsShare().put(su, mContext, "su");
        });
        showMesh(su.meshType);

        alpha = findViewById(R.id.dark);
        alpha.setText(String.valueOf(su.alpha));
        alpha.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() > 0) {
                    su.alpha = Integer.parseInt(s.toString());
                    new ParamsShare().put(su, mContext, "su");
                }
            }
        });

        tv23 = findViewById(R.id.two_three);
        tv23.setOnClickListener(view -> {
            su.twoThree = (su.twoThree == 2) ? 3:2;
            tv23.setText(su.twoThree+" qz");
            new ParamsShare().put(su, mContext, "su");
        });
        tv23.setText(su.twoThree+" qz");


        SwitchCompat makeAnswer = findViewById(R.id.makeAnswer);
        makeAnswer.setChecked(su.makeAnswer);
        makeAnswer.setOnCheckedChangeListener((compoundButton, b) -> su.makeAnswer = b);
        ImageButton generate = findViewById(R.id.generate);
        generate.setOnClickListener(new View.OnClickListener() {
            boolean isRunning = false;
            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    isRunning = true;
                    su.alpha = Integer.parseInt(alpha.getText().toString());
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
            blankList.add(String.valueOf(level));
        }
        pageList = new ArrayList<>();
        for (int page = MINIMUM_PAGE; page <= MAXIMUM_PAGE; page += 2) {
            pageList.add(String.valueOf(page));
        }

        buildBlankWheel();
        buildPageWheel();

        String s;
        TextView tvCase1 = findViewById(R.id.case1);
        s = su1.blankCount+" blanks\n"+su1.quizCount +" quiz";
        tvCase1.setText(s);
        TextView tvCase1Load = findViewById(R.id.case1Load);
        tvCase1Load.setOnClickListener(view -> {
            copySu(su1,su);
            buildBlankWheel();
            buildPageWheel();
            toastMsg("Case One Loaded");
            showMesh(su.meshType);
            alpha.setText(String.valueOf(su.alpha));
            tv23.setText(su.twoThree+" qz");
        });
        TextView tvCase1Save = findViewById(R.id.case1Save);
        tvCase1Save.setOnClickListener(view -> {
            copySu(su, su1);
            String s1 = su.blankCount+" blanks\n"+su.quizCount +" quiz";
            tvCase1.setText(s1);
            new ParamsShare().put(su1, mContext, "su1");
            toastMsg("Saved to Case One");
            showMesh(su.meshType);
        });
        TextView tvCase2 = findViewById(R.id.case2);
        s = su2.blankCount+" blanks\n"+su2.quizCount +" quiz";
        tvCase2.setText(s);
        TextView tvCase2Load = findViewById(R.id.case2Load);
        tvCase2Load.setOnClickListener(view -> {
            copySu(su2, su);
            buildBlankWheel();
            buildPageWheel();
            toastMsg("Case Two Loaded");
            showMesh(su.meshType);
            alpha.setText(String.valueOf(su.alpha));
            tv23.setText(su.twoThree+" qz");
        });
        TextView tvCase2Save = findViewById(R.id.case2Save);
        tvCase2Save.setOnClickListener(view -> {
            copySu(su, su2);
            String s2 = su.blankCount+" blanks\n"+su.quizCount +" pages";
            tvCase2.setText(s2);
            new ParamsShare().put(su2, mContext, "su2");
            toastMsg("Saved to Case Two");
            showMesh(su.meshType);
        });
    }

    private void toastMsg(String s) {
        Toast toast = Toast.makeText(mContext, s, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void showMesh(int mesh) {
        if (mesh == 0)
            btnMesh.setImageResource(R.drawable.mesh0_off);
        else if (mesh == 1)
            btnMesh.setImageResource(R.drawable.mesh1_top);
        else
            btnMesh.setImageResource(R.drawable.mesh2_on);
    }

    private void copySu(SudokuInfo suFrom, SudokuInfo suTo) {
        suTo.quizCount = suFrom.quizCount;
        suTo.blankCount = suFrom.blankCount;
        suTo.meshType = suFrom.meshType;
        suTo.twoThree = suFrom.twoThree;
        suTo.makeAnswer = suFrom.makeAnswer;
        suTo.alpha = suFrom.alpha;
    }
    private void buildBlankWheel() {

        final WheelView<String> wheelView = findViewById(R.id.wheel_blanks);
        wheelView.setOnItemSelectedListener((wheelView1, data, position) -> {
                Log.w("setOnItemSelectedListener", "setOnItemSelectedListener: data=" + data + ",position=" + position);
                su.blankCount = Integer.parseInt(blankList.get(position));
                new ParamsShare().put(su, mContext, "su");
        });

        wheelView.setData(blankList);
        wheelView.setSelectedItemPosition(su.blankCount - MINIMUM_BLANK, true);
        wheelView.setSoundEffect(true);
        wheelView.setSoundEffectResource(R.raw.level_degree);
        wheelView.setPlayVolume(0.1f);

    }

    private void buildPageWheel() {

        final WheelView<String> wheelView = findViewById(R.id.wheel_quiz);
        wheelView.setOnItemSelectedListener((wheelView1, data, position) -> {
            su.quizCount = Integer.parseInt(data);
            new ParamsShare().put(su, mContext, "su");
        });

        wheelView.setData(pageList);
        wheelView.setSelectedItemPosition((su.quizCount - MINIMUM_PAGE)/2, true);
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