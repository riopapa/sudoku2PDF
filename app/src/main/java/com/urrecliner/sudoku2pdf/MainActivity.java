package com.urrecliner.sudoku2pdf;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    static Context mContext;
    static int levelDegree = 5;
    static int puzzleCount = 0;
    static TextView statusTV;
    static String fileDate;
    List<String> levelList, countList;
    final static int MINIMUM_LEVEL = 6, MAXIMUM_LEVEL = 24;
    final static int MINIMUM_COUNT = 4, MAXIMUM_COUNT = 20;
    static ProgressBar progressBar;

    String TAG = "WHEEL";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.w("start"," ------------------");

        statusTV = findViewById(R.id.status);
        statusTV.setVisibility(View.INVISIBLE);

        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.circle);
        progressBar = findViewById(R.id.progress_circle);
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.setProgressDrawable(drawable);

        mContext = getApplicationContext();
        FloatingActionButton fab = findViewById(R.id.start);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Starting generation", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                final SimpleDateFormat sdfDate = new SimpleDateFormat("yy-MM-dd HH.mm.ss", Locale.US);
                fileDate = "sudoku_" + sdfDate.format(System.currentTimeMillis())+" Level_"+levelDegree ;
                MakeSudoku makeSudoku = new MakeSudoku();
                makeSudoku.run(puzzleCount, levelDegree);
            }
        });
        levelList = new ArrayList<>();
        for (int level = MINIMUM_LEVEL; level <= MAXIMUM_LEVEL; level++) {
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
        levelDegree = mSettings.getInt("levelDegree", 6);
        tV.setText(""+levelDegree);

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
                levelDegree = Integer.parseInt(levelList.get(newPosition));
                showLevelDegree(tV,""+levelDegree);
                editor.putInt("levelDegree", levelDegree).apply();
                float vol = (float) levelDegree / (float)MAXIMUM_LEVEL / 2;
                wheelView.setPlayVolume(vol);
            }

            @Override
            public void onWheelSelected(int position) {
                levelDegree = Integer.parseInt(levelList.get(position));
                showLevelDegree(tV,""+levelDegree);
                editor.putInt("levelDegree", levelDegree).apply();
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
//                Log.w(TAG, "onWheelScrollStateChanged: state=" + state);
            }
        });

        wheelView.setData(levelList);
        wheelView.setSelectedItemPosition(levelDegree - MINIMUM_LEVEL, true);
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
}
