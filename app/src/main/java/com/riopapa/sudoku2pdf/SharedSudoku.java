package com.riopapa.sudoku2pdf;

import static com.riopapa.sudoku2pdf.MainActivity.sudokus;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.riopapa.sudoku2pdf.Model.Sudoku;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedSudoku {


    void initSudokus() {

        sudokus = new ArrayList<>();
        Sudoku su = new Sudoku();
        su.name = "처음이예요";
        su.blank = 16;
        su.quiz = 4;
        su.meshType = 0;
        su.nbrPage = 0;
        su.opacity = 250;
        su.answer = true;
        sudokus.add(su);
        su.name="좀 해봤어요";
        su.blank = 24;
        su.nbrPage = 2;
        sudokus.add(su);
        su.name="나름 합니다";
        su.blank = 40;
        su.nbrPage = 3;
        sudokus.add(su);
        su.name="어려워야 하죠";
        su.blank = 50;
        su.nbrPage = 3;
        sudokus.add(su);
    }

    public void put(ArrayList<Sudoku> quietTasks, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("sudoku", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(quietTasks);
        prefsEditor.putString("sudoku", json);
        prefsEditor.apply();
    }

    public void get(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("sudoku", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("sudoku", "");

        if (json.isEmpty()) {
            initSudokus();
        } else {
            Type type = new TypeToken<List<Sudoku>>() {
            }.getType();
            sudokus = gson.fromJson(json, type);
        }
    }

}
