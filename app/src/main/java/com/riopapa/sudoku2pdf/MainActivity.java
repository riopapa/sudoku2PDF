package com.riopapa.sudoku2pdf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.riopapa.sudoku2pdf.Model.Sudoku;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static Context mContext;
    public static Activity mActivity;
    public static ArrayList<Sudoku> sudokus;
    public static int onePos;
    RecyclerView oneRecyclerView;
    public static OneAdapter oneAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
        mContext = getApplicationContext();
        mActivity = this;
        new SharedSudoku().get(mContext);   // read sudokus

        oneAdapter = new OneAdapter();
        oneRecyclerView = findViewById(R.id.one_list);
        oneRecyclerView.setAdapter(oneAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int menuItem = item.getItemId();
        if (menuItem == R.id.add_new) {
            Sudoku su = new Sudoku();
            su.name = "새로운 분";
            su.blank = 24;
            su.opacity = 255;
            su.mesh = 0;
            su.quiz = 6;
            su.answer = false;
            su.nbrPage = 2;
            sudokus.add(su);
            mActivity.runOnUiThread(() -> {
                oneAdapter.notifyDataSetChanged();
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}