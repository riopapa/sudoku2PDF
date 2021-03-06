package com.urrecliner.sudoku2pdf;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    static Context mContext;
    static int blankCount = 5;
    static int puzzleCount = 0;
    static TextView statusTV;
    static String fileDate;
    List<String> levelList, countList;
    final static int MINIMUM_BLANK = 27, MAXIMUM_BLANK = 64;
    final static int MINIMUM_COUNT = 4, MAXIMUM_COUNT = 20;
    static ProgressBar progressBar;
    static FrameLayout frameLayout;
    static ConstraintLayout mainLayout;
    static TextView horizontalLineView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.w("start"," ------------------");

        statusTV = findViewById(R.id.status);
        statusTV.setVisibility(View.INVISIBLE);

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

        mContext = getApplicationContext();
        FloatingActionButton fab = findViewById(R.id.start);
        fab.setOnClickListener(new View.OnClickListener() {
            boolean isRunning = false;
            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    isRunning = true;
                    Snackbar.make(view, "Starting generation", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    final SimpleDateFormat sdfDate = new SimpleDateFormat("yy-MM-dd HH.mm.ss", Locale.US);
                    fileDate = "sudoku_" + sdfDate.format(System.currentTimeMillis()) + " with "+ blankCount +" Blanks";
                    MakeSudoku makeSudoku = new MakeSudoku();
                    makeSudoku.run(puzzleCount, blankCount);
                    isRunning = false;
                }
            }
        });
        levelList = new ArrayList<>();
        for (int level = MINIMUM_BLANK; level <= MAXIMUM_BLANK; level++) {
            levelList.add(level + "");
        }
        countList = new ArrayList<>();
        for (int page = MINIMUM_COUNT; page <= MAXIMUM_COUNT; page++) {
            countList.add(page + "");
        }

        buildLevelWheel();
        buildCountWheel();

    }

    private void buildLevelWheel() {

        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = mSettings.edit();
        final TextView tV = findViewById(R.id.levelText);
        blankCount = mSettings.getInt("blankCount", 40);
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
                blankCount = Integer.parseInt(levelList.get(newPosition));
                showLevelDegree(tV,""+ blankCount);
                editor.putInt("blankCount", blankCount).apply();
                float vol = (float) blankCount / (float) MAXIMUM_BLANK / 2;
                wheelView.setPlayVolume(vol);
            }

            @Override
            public void onWheelSelected(int position) {
                blankCount = Integer.parseInt(levelList.get(position));
                showLevelDegree(tV,""+ blankCount);
                editor.putInt("blankCount", blankCount).apply();
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
//                Log.w(TAG, "onWheelScrollStateChanged: state=" + state);
            }
        });

        wheelView.setData(levelList);
        wheelView.setSelectedItemPosition(blankCount - MINIMUM_BLANK, true);
        wheelView.setSoundEffect(true);
        wheelView.setSoundEffectResource(R.raw.level_degree);
        wheelView.setPlayVolume(0.1f);

//        SwitchCompat soundSc = findViewById(R.id.sc_turn_on_sound);
//        soundSc.setChecked(wheelView.isSoundEffect());
//        soundSc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                wheelView.setSoundEffect(isChecked);
//            }
//        });
//        final AppCompatSeekBar soundVolumeSb = findViewById(R.id.sb_sound_effect);
//        soundVolumeSb.setMax(100);
//        soundVolumeSb.setProgress((int) (wheelView.getPlayVolume() * 100));
//        AppCompatButton setSoundVolumeBtn = findViewById(R.id.btn_set_sound_effect);
//        setSoundVolumeBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                wheelView.setPlayVolume(soundVolumeSb.getProgress() * 1.0f / 100);
//            }
//        });
//
//        SwitchCompat cyclicSc = findViewById(R.id.sc_turn_on_cyclic);
//        cyclicSc.setChecked(wheelView.isCyclic());
//        cyclicSc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                wheelView.setCyclic(isChecked);
//            }
//        });
//

    }

    private void showLevelDegree(TextView tV, String s) {
        tV.setText(s);
    }

    private void buildCountWheel() {

        SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = mSettings.edit();
        final TextView tV = findViewById(R.id.countText);
        puzzleCount = mSettings.getInt("puzzleCount", 16);
        tV.setText(""+ puzzleCount);

        final WheelView<String> wheelView = findViewById(R.id.wheel_count);
//        List<String> list = new ArrayList<>(1);
//        wheelView.setIntegerNeedFormat(true);
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
                puzzleCount = Integer.parseInt(countList.get(newPosition));
                showCountPage(tV, ""+ puzzleCount);
                editor.putInt("puzzleCount", puzzleCount).apply();
                float vol = (float) puzzleCount / (float)MAXIMUM_COUNT / 2;
                wheelView.setPlayVolume(vol);
            }

            @Override
            public void onWheelSelected(int position) {
                puzzleCount = Integer.parseInt(countList.get(position));
                showCountPage(tV, ""+ puzzleCount);
                editor.putInt("puzzleCount", puzzleCount).apply();
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
//                Log.w(TAG, "onWheelScrollStateChanged: state=" + state);
            }
        });

        wheelView.setData(countList);
        wheelView.setSelectedItemPosition(puzzleCount - MINIMUM_COUNT, true);
        wheelView.setSoundEffect(true);
        wheelView.setSoundEffectResource(R.raw.puzzle_count);
        wheelView.setPlayVolume(0.1f);
    }

    private void showCountPage(TextView tV, String s) {
        tV.setText(s);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
        System.exit(0);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
