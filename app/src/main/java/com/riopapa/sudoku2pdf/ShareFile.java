package com.riopapa.sudoku2pdf;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;

public class ShareFile {

    public void send(Context context, ArrayList<File> fileList) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileList.get(0));
        shareIntent.setType("application/*");

//        ArrayList<Parcelable> list = new ArrayList<>();
//        for(File f : fileList){
//            Uri contentUri = FileProvider.getUriForFile(context, "com.riopapa.sudoku2pdf.provider", f);
////            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, f);
//            list.add(contentUri);
//        }
//        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, list);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, shareIntent, 0);
        try {
            pendingIntent.send();
        } catch(PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    public void print(Context context, File f) {
//        Uri uri = FileProvider.getUriForFile(context, "com.riopapa.sudoku2pdf.provider", f);
        Uri uri = Uri.parse(f.getPath());
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.cxinventor.file.explorer");

//        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setAction(Intent.ACTION_SEND);
//        intent.setType("application/*");
//        intent.putExtra(Intent.EXTRA_STREAM, uri);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
//        context.startActivity(Intent.createChooser(intent, "Share Via"));
    }

    public void show(Context context, File f) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.cxinventor.file.explorer");
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(f.getPath()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("application/*");
//        intent.setAction(Intent.ACTION_SEND);

        context.startActivity(intent);
    }
}