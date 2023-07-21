package com.riopapa.sudoku2pdf;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

public class ShareFile {
    public void show(Context context, String folder) {

        Uri selectedUri = Uri.parse(folder);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "resource/folder");
        context.startActivity(intent);
    }
}