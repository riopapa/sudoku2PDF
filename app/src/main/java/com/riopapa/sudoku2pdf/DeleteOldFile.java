package com.riopapa.sudoku2pdf;

import android.util.Log;

import java.io.File;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DeleteOldFile {

    final String FORMAT_DATE = "yy-MM-dd";
    final String folder = "sudoku";
    final String prefix = "su_";
    final int days = 2;

    public DeleteOldFile(String downloadFolder) {
        final SimpleDateFormat sdfDate = new SimpleDateFormat(FORMAT_DATE, Locale.US);
        String oldDate = prefix
                + sdfDate.format(System.currentTimeMillis() - days*24*60*60*1000L);
        File[] filesAndFolders = new File(downloadFolder, folder).listFiles();
        if (filesAndFolders != null) {
            Collator myCollator = Collator.getInstance();
            for (File oneFile : filesAndFolders) {      // isFile check required
                String shortFileName = oneFile.getName();
                if (shortFileName.startsWith(prefix) &&
                        myCollator.compare(shortFileName, oldDate) < 0) {
                    if (!oneFile.delete())
                        Log.e("file", "Delete Error " + oneFile);
                }
            }
        }
    }
}
