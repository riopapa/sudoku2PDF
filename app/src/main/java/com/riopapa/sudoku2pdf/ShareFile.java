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

        if (intent.resolveActivityInfo(context.getPackageManager(), 0) != null)
        {
            context.startActivity(intent);
        }
        else
        {
            // if you reach this place, it means there is no any file
            // explorer app installed on your device
        }

    }
}