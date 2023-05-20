package com.riopapa.sudoku2pdf;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.riopapa.sudoku2pdf.Model.SudokuInfo;

import java.lang.reflect.Type;

public class ParamsShare {
    public void put(SudokuInfo sudokuInfo, Context context, String id) {

        SharedPreferences sharedPref = androidx.preference.
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(sudokuInfo);
        prefsEditor.putString(id, json);
        prefsEditor.apply();
    }

    SudokuInfo get(Context context, String id) {

//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sharedPref = androidx.preference.
                PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        SudokuInfo sudokuInfo = null;
        String json = sharedPref.getString(id, "");
        if (json.isEmpty()) {
            sudokuInfo = new SudokuInfo();
            sudokuInfo.blankCount = 13;
            sudokuInfo.quizCount = 4;
            sudokuInfo.twoThree = 2;
            sudokuInfo.meshType = 1;
            sudokuInfo.makeAnswer = false;
        } else {
            Type type = new TypeToken<SudokuInfo>() {
            }.getType();
            sudokuInfo = gson.fromJson(json, type);
        }
        return sudokuInfo;
    }

}