package com.riopapa.sudoku2pdf;

import static com.riopapa.sudoku2pdf.ActivityMain.sudokus;

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
        su = new Sudoku(); su.name = "이정도야"; su.nbrOfBlank = 16; su.nbrOfQuiz = 4; su.mesh = 0;
        su.nbrPage = 2; su.opacity = 255; su.answer = true; sudokus.add(su);

        su = new Sudoku(); su.name = "할만하네"; su.nbrOfBlank = 28; su.nbrOfQuiz = 6; su.mesh = 0;
        su.nbrPage = 2; su.opacity = 255; su.answer = true; sudokus.add(su);

        su = new Sudoku(); su.name = "어렵군"; su.nbrOfBlank = 40; su.nbrOfQuiz = 12; su.mesh = 1;
        su.nbrPage = 6; su.opacity = 255; su.answer = true; sudokus.add(su);

        su = new Sudoku(); su.name = "풀리긴 함"; su.nbrOfBlank = 50; su.nbrOfQuiz = 6; su.mesh = 2;
        su.nbrPage = 6; su.opacity = 255; su.answer = true; sudokus.add(su);

    }

    public void put(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(su, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(sudokus);
        prefsEditor.putString(su, json);
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
    }

}
