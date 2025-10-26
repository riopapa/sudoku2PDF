package com.riopapa.sudoku2pdf;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.riopapa.sudoku2pdf.Model.Sudoku;

import java.util.ArrayList;

public class ActivityMain extends AppCompatActivity {

    public static Activity mActivity;
    public static ArrayList<Sudoku> sudokus;
    public static int onePos;
    RecyclerView oneRecyclerView;
    public static GroupAdapter groupAdapter;
    public static String prefix = "_su$_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = this;
        new SharedSudoku().get(this);   // read sudokus
        new DeleteOldFile(this).del( "", 3 * 60 * 60 * 1000);

        groupAdapter = new GroupAdapter();
        oneRecyclerView = findViewById(R.id.one_list);
        oneRecyclerView.setAdapter(groupAdapter);
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
            su.group = "새로운 분";
            su.nbrOfBlank = 24;
            su.opacity = 255;
            su.mesh = 0;
            su.nbrOfQuiz = 6;
            su.gridSize = 9;
            su.answer = false;
            su.nbrPage = 2;
            sudokus.add(su);
            mActivity.runOnUiThread(() -> {
                groupAdapter.notifyDataSetChanged();
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}