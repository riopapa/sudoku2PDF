package com.riopapa.sudoku2pdf;

import static com.riopapa.sudoku2pdf.MainActivity.shareTo;
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


    final String su ="sudoku";
    void initSudokus() {

        sudokus = new ArrayList<>();
        Sudoku su;
        su = new Sudoku(); su.name = "이정도야"; su.blank = 16; su.quiz = 4; su.mesh = 0;
        su.nbrPage = 2; su.opacity = 255; su.answer = true; sudokus.add(su);

        su = new Sudoku(); su.name = "할만하네"; su.blank = 28; su.quiz = 6; su.mesh = 0;
        su.nbrPage = 2; su.opacity = 255; su.answer = true; sudokus.add(su);

        su = new Sudoku(); su.name = "어렵군"; su.blank = 40; su.quiz = 12; su.mesh = 1;
        su.nbrPage = 6; su.opacity = 255; su.answer = true; sudokus.add(su);

        su = new Sudoku(); su.name = "풀리긴 함"; su.blank = 50; su.quiz = 6; su.mesh = 2;
        su.nbrPage = 6; su.opacity = 255; su.answer = true; sudokus.add(su);

    }

    public void put(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(su, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(sudokus);
        prefsEditor.putString(su, json);
        prefsEditor.putInt("shareTo", shareTo);
        prefsEditor.apply();
    }

    public void get(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(su, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString(su, "");

        if (json.isEmpty()) {
            initSudokus();
        } else {
            Type type = new TypeToken<List<Sudoku>>() {
            }.getType();
            sudokus = gson.fromJson(json, type);
        }
        shareTo = sharedPref.getInt("shareTo",0);
    }

}
