package com.riopapa.sudoku2pdf;


import static com.riopapa.sudoku2pdf.ActivityMain.oneAdapter;
import static com.riopapa.sudoku2pdf.ActivityMain.onePos;
import static com.riopapa.sudoku2pdf.ActivityMain.shareTo;
import static com.riopapa.sudoku2pdf.ActivityMain.sudokus;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.snackbar.Snackbar;
import com.riopapa.sudoku2pdf.Model.Sudoku;

import java.util.ArrayList;
import java.util.List;

public class ActivityOneEdit extends AppCompatActivity {

    Context oneContext;
    Activity oneActivity;
    List<String> blankList, pageList;
    final static int MINIMUM_BLANK = 12, MAXIMUM_BLANK = 55;
    final static int MINIMUM_PAGE = 4, MAXIMUM_PAGE = 60;
    ImageButton btnMesh, toPrinter, toFile;
    TextView tv2or6, tMessage;
    EditText eOpacity, eName;    // 255 : real black
    Sudoku su;
    MenuItem shareToMenu;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        oneContext = this;
        oneActivity = this;

        setContentView(R.layout.activity_one);
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
            su.mesh = (su.mesh +1) % 4;
            showMesh(su.mesh);
            sudokus.set(onePos, su);
        });
        showMesh(su.mesh);

        eOpacity = findViewById(R.id.opacity);
        eOpacity.setText(String.valueOf(su.opacity));
        eOpacity.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                int i = Integer.parseInt(s.toString());
                if (i > 255 || i < 120) {
                    tMessage.setText("120 과 255 사이의 값을 넣어 주세요");
                } else {
                    tMessage.setText("");
                    su.opacity = i;
                    sudokus.set(onePos, su);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        eName = findViewById(R.id.name);
        eName.setText(String.valueOf(su.name));
        eName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() > 0) {
                    su.name = s.toString();
                    sudokus.set(onePos, su);
                }
            }
        });

        tv2or6 = findViewById(R.id.two_six);
        tv2or6.setOnClickListener(view -> {
            if (su.nbrPage == 2)
                su.nbrPage = 6;
            else if (su.nbrPage == 6)
                    su.nbrPage = 1;
            else
                su.nbrPage = 2;
            tv2or6.setText(su.nbrPage +" 문제");
            sudokus.set(onePos, su);
        });
        tv2or6.setText(su.nbrPage +"문제");

        tMessage = findViewById(R.id.message);

        SwitchCompat makeAnswer = findViewById(R.id.makeAnswer);
        makeAnswer.setChecked(su.answer);
        makeAnswer.setOnCheckedChangeListener((compoundButton, b) ->
            {su.answer = b; sudokus.set(onePos, su);});

        toPrinter = findViewById(R.id.to_printer);
        toPrinter.setOnClickListener(new View.OnClickListener() {
            boolean isRunning = false;
            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    isRunning = true;
                    Snackbar.make(view, "Starting generation", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    letUsGo("p");
                    isRunning = false;
                }
            }
        });

        toFile = findViewById(R.id.to_file);
        toFile.setOnClickListener(new View.OnClickListener() {
            boolean isRunning = false;
            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    isRunning = true;
                    Snackbar.make(view, "Starting generation", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    letUsGo("f");
                    isRunning = false;
                }
            }
        });

        blankList = new ArrayList<>();
        for (int level = MINIMUM_BLANK; level <= MAXIMUM_BLANK; level++) {
            blankList.add(String.valueOf(level));
        }
        pageList = new ArrayList<>();
        for (int page = MINIMUM_PAGE; page <= MAXIMUM_PAGE; page++) {
            pageList.add(String.valueOf(page));
        }

        buildBlankWheel();
        buildPageWheel();

    }
    private void letUsGo(String fileOrPrint) {
        sudokus.set(onePos, su);
        new SharedSudoku().put(getApplicationContext());

        new MakeSudoku().make(su, fileOrPrint,
                findViewById(R.id.message),
                findViewById(R.id.progress_circle),
                ResourcesCompat.getDrawable(getResources(), R.drawable.circle, null)
        );
    }


    private void showMesh(int mesh) {
        if (mesh == 0)
            btnMesh.setImageResource(R.drawable.mesh0_off);
        else if (mesh == 1)
            btnMesh.setImageResource(R.drawable.mesh1_top);
        else if (mesh == 2)
            btnMesh.setImageResource(R.drawable.mesh2_on);
        else
            btnMesh.setImageResource(R.drawable.mesh3_top);
    }

    private void buildBlankWheel() {

        final WheelView<String> wheelView = findViewById(R.id.wheel_blanks);
        wheelView.setOnItemSelectedListener((wheelView1, data, position) -> {
                su.blank = Integer.parseInt(blankList.get(position));
        });

        wheelView.setData(blankList);
        wheelView.setSelectedItemPosition(su.blank - MINIMUM_BLANK, true);
        wheelView.setSoundEffect(true);
        wheelView.setSoundEffectResource(R.raw.level_degree);
        wheelView.setPlayVolume(0.04f);

    }

    private void buildPageWheel() {

        final WheelView<String> wheelView = findViewById(R.id.wheel_quiz);
        wheelView.setOnItemSelectedListener((wheelView1, data, position) -> su.quiz = Integer.parseInt(data));

        wheelView.setData(pageList);
        wheelView.setSelectedItemPosition((su.quiz - MINIMUM_PAGE), true);
        wheelView.setSoundEffect(true);
        wheelView.setSoundEffectResource(R.raw.page_count);
        wheelView.setPlayVolume(0.04f);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int menuItem = item.getItemId();
        if (menuItem == R.id.delete) {
            sudokus.remove(onePos);
            new SharedSudoku().put(getApplicationContext());
            oneAdapter.notifyItemRemoved(onePos);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new SharedSudoku().put(getApplicationContext());
        oneAdapter.notifyItemChanged(onePos);
        super.onBackPressed();
    }
}