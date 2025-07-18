package com.riopapa.sudoku2pdf;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.riopapa.sudoku2pdf.Model.QuizAnswers;
import com.riopapa.sudoku2pdf.Model.Sudoku;

import java.util.ArrayList;

public class ActivityMain extends AppCompatActivity {

    public static Activity mActivity;
    public static ArrayList<Sudoku> sudokus;
    public static int shareTo = 0;   // 0 : folder, 1: printer
    public static int onePos;
    static MenuItem shareToMenu;
    RecyclerView oneRecyclerView;
    public static OneAdapter oneAdapter;
    String downloadFolder;
    public static QuizAnswers quizAnswers = new QuizAnswers();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActivity = this;
        new SharedSudoku().get(this);   // read sudokus
        downloadFolder = Environment.getExternalStorageDirectory().getPath()+"/download";
        new DeleteOldFile(this).del(downloadFolder, "", "su_", 2 * 24 * 60 * 60 * 1000);

        oneAdapter = new OneAdapter();
        oneRecyclerView = findViewById(R.id.one_list);
        oneRecyclerView.setAdapter(oneAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        shareToMenu = menu.findItem(R.id.share_to_main);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int menuItem = item.getItemId();
        if (menuItem == R.id.add_new) {
            Sudoku su = new Sudoku();
            su.name = "새로운 분";
            su.nbrOfBlank = 24;
            su.opacity = 255;
            su.mesh = 0;
            su.nbrOfQuiz = 6;
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