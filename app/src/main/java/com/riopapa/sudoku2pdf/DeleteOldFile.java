package com.riopapa.sudoku2pdf;

import static com.riopapa.sudoku2pdf.ActivityMain.prefix;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import java.io.File;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DeleteOldFile {


    public DeleteOldFile(Activity activity) {
        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            intent.setData(uri);
            activity.startActivity(intent);
        }
    }

    public void del(long backTime) {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yy-MM-dd", Locale.US);
        String oldFileName = prefix + sdfDate.format(System.currentTimeMillis() - backTime);
        File[] folderName = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "temp").listFiles();
        if (folderName != null) {
            Collator myCollator = Collator.getInstance();
            for (File oneFile : folderName) {      // isFile check required
                String shortFileName = oneFile.getName();
                if (shortFileName.startsWith(prefix) &&
                        myCollator.compare(shortFileName, oldFileName) < 0) {
                    if (!oneFile.delete())
                        Log.e("file", "Delete Error " + oneFile);
                }
            }
        }
    }
}
