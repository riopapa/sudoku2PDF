package com.riopapa.sudoku2pdf;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class ShareFile {
    public void show(Context context, String folder) {

        Uri selectedUri = Uri.parse(folder);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "resource/folder");
        context.startActivity(intent);
    }
}