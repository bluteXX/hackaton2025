package com.example.visit;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

public class BitmapHelper {

    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;

        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, size, size);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xFFFFFFFF);

        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rect, paint);

        // Opcjonalnie: biała obwódka
        paint.setXfermode(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(android.graphics.Color.WHITE);
        paint.setStrokeWidth(10);
        canvas.drawCircle(size / 2f, size / 2f, (size / 2f) - 5, paint);

        return output;
    }
}