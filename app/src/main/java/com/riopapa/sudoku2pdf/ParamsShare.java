package com.riopapa.sudoku2pdf;

import static android.content.Context.MODE_PRIVATE;

import static com.riopapa.sudoku2pdf.MainActivity.sudokus;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.riopapa.sudoku2pdf.Model.Sudoku;

import java.lang.reflect.Type;

public class ParamsShare {
    public void put(Context context) {

        SharedPreferences sharedPref = context.getSharedPreferences("sudoku", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(sudokus);
        prefsEditor.putString("sudoku", json);
        prefsEditor.apply();
    }

    Sudoku get(Context context, String id) {

        SharedPreferences sharedPref = context.getSharedPreferences("sudoku", MODE_PRIVATE);
        Gson gson = new Gson();
        Sudoku sudoku;
        String json = sharedPref.getString(id, "");
        if (json.isEmpty()) {
            sudoku = new Sudoku();
            sudoku.blank = 13;
            sudoku.quiz = 4;
            sudoku.nbrPage = 2;
            sudoku.mesh = 1;
            sudoku.answer = false;
            sudoku.opacity = 200;
        } else {
            Type type = new TypeToken<Sudoku>() {
            }.getType();
            sudoku = gson.fromJson(json, type);
        }
        return sudoku;
    }

}