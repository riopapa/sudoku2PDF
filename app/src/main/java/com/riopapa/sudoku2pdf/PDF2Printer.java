package com.riopapa.sudoku2pdf;

import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;

public class PDF2Printer {

    void launch(Context context, String fullPath) {
        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        try {
            PrintDocumentAdapter printAdapter = new PdfDocumentAdapter(context, fullPath);
            printManager.print("Document", printAdapter, new PrintAttributes.Builder().build());
        } catch (Exception e) {
            Log.e("PDF2Printer", "error " + e);
        }
    }
}
