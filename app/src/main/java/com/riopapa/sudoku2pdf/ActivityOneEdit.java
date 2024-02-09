package com.riopapa.sudoku2pdf;


import static com.riopapa.sudoku2pdf.MainActivity.onePos;
import static com.riopapa.sudoku2pdf.MainActivity.sudokus;

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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.riopapa.sudoku2pdf.Model.Sudoku;

import java.util.ArrayList;
import java.util.List;

public class ActivityOneEdit extends AppCompatActivity {

    public Context mContext;
    public Activity mActivity;
    List<String> blankList, pageList;
    final static int MINIMUM_BLANK = 12, MAXIMUM_BLANK = 55;
    final static int MINIMUM_PAGE = 4, MAXIMUM_PAGE = 20;
    ImageButton btnMesh;
    TextView tv23;
    EditText eOpacity;    // 255 : real black
    Sudoku su;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one);
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
        su = sudokus.get(onePos);
        btnMesh = findViewById(R.id.mesh);
        btnMesh.setOnClickListener(view -> {
            su.meshType = (su.meshType+1) % 3;
            showMesh(su.meshType);
            sudokus.set(onePos, su);
        });
        showMesh(su.meshType);

        eOpacity = findViewById(R.id.opacity);
        eOpacity.setText(String.valueOf(su.opacity));
        eOpacity.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() > 0) {
                    su.opacity = Integer.parseInt(s.toString());
                    sudokus.set(onePos, su);
                }
            }
        });

        tv23 = findViewById(R.id.two_three);
        tv23.setOnClickListener(view -> {
            su.nbrPage = (su.nbrPage == 2) ? 3:2;
            tv23.setText(su.nbrPage +" qz");
            sudokus.set(onePos, su);
        });
        tv23.setText(su.nbrPage +" qz");

        SwitchCompat makeAnswer = findViewById(R.id.makeAnswer);
        makeAnswer.setChecked(su.answer);
        makeAnswer.setOnCheckedChangeListener((compoundButton, b) -> su.answer = b);
        ImageButton generate = findViewById(R.id.generate);
        generate.setOnClickListener(new View.OnClickListener() {
            boolean isRunning = false;
            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    isRunning = true;
                    su.opacity = Integer.parseInt(eOpacity.getText().toString());
                    sudokus.set(onePos, su);
                    new ParamsShare().put(mContext);
                    Snackbar.make(view, "Starting generation", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
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

    }

    private void showMesh(int mesh) {
        if (mesh == 0)
            btnMesh.setImageResource(R.drawable.mesh0_off);
        else if (mesh == 1)
            btnMesh.setImageResource(R.drawable.mesh1_top);
        else
            btnMesh.setImageResource(R.drawable.mesh2_on);
    }

    private void buildBlankWheel() {

        final WheelView<String> wheelView = findViewById(R.id.wheel_blanks);
        wheelView.setOnItemSelectedListener((wheelView1, data, position) -> {
                Log.w("setOnItemSelectedListener", "setOnItemSelectedListener: data=" + data + ",position=" + position);
                su.blank = Integer.parseInt(blankList.get(position));
        });

        wheelView.setData(blankList);
        wheelView.setSelectedItemPosition(su.blank - MINIMUM_BLANK, true);
        wheelView.setSoundEffect(true);
        wheelView.setSoundEffectResource(R.raw.level_degree);
        wheelView.setPlayVolume(0.1f);

    }

    private void buildPageWheel() {

        final WheelView<String> wheelView = findViewById(R.id.wheel_quiz);
        wheelView.setOnItemSelectedListener((wheelView1, data, position) -> {
            su.quiz = Integer.parseInt(data);
        });

        wheelView.setData(pageList);
        wheelView.setSelectedItemPosition((su.quiz - MINIMUM_PAGE)/2, true);
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