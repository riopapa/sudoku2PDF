package com.riopapa.sudoku2pdf;

import android.util.Log;

import java.io.File;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DeleteOldFile {

    final String FORMAT_DATE = "yy-MM-dd";

    // mainFolder : /storage/emulated/0/Download
    // subFolder : maze
    // prefix : maze_
    // backTime : 2 * 24 * 60 * 60 * 1000 (2 days)

    public DeleteOldFile(String downloadFolder, String subFolder,
                         String prefix, long backTime) {
        final SimpleDateFormat sdfDate = new SimpleDateFormat(FORMAT_DATE, Locale.US);
        String oldFileName = prefix
                + sdfDate.format(System.currentTimeMillis() - backTime);
        File[] filesAndFolders = new File(downloadFolder, subFolder).listFiles();
        if (filesAndFolders != null) {
            Collator myCollator = Collator.getInstance();
            for (File oneFile : filesAndFolders) {      // isFile check required
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
