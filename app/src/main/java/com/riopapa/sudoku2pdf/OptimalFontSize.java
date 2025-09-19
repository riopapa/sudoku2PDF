package com.riopapa.sudoku2pdf;

import android.graphics.Paint;
import android.graphics.Rect;

public class OptimalFontSize {

    float calc(Paint paint, float boxSize) {
        // We want the text to have a bit of padding inside the box.
        // 85% is a good starting point, adjust as needed.
        float targetTextHeight = boxSize * 0.6f;

        // 1. Set an arbitrary text size for measurement
        paint.setTextSize(100f); // Use a large size like 100px for better precision

        // 2. Measure the bounds of a sample character. '8' is a good choice as it's
        //    typically one of the tallest and widest digits.
        Rect textBounds = new Rect();
        paint.getTextBounds("8", 0, 1, textBounds);

        // 3. Calculate the height of the measured text
        float measuredTextHeight = textBounds.height();

        // 4. Calculate the new text size by scaling
        float newTextSize = (targetTextHeight / measuredTextHeight) * 100f;

        return newTextSize;
    }

}
