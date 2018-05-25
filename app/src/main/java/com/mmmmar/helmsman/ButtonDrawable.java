package com.mmmmar.helmsman;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ButtonDrawable extends Drawable {

    private static final float RATIO = 7.0f / 8.0f;

    private Paint paint;
    private Path trianglePath;

    public ButtonDrawable(int color) {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(color);

        trianglePath = new Path();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect rect = getBounds();

        int rectangle = (int) (rect.width() * RATIO);
        canvas.drawRect(rect.left, rect.top, rect.left + rectangle, rect.bottom, paint);

        trianglePath.reset();
        trianglePath.moveTo(rect.left + rectangle, rect.top);
        trianglePath.lineTo(rect.right, rect.height() / 2.0f);
        trianglePath.lineTo(rect.left + rectangle, rect.bottom);
        trianglePath.close();
        canvas.drawPath(trianglePath, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        // not support.
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        // not support.
    }

    @Override
    public int getOpacity() {
        // not support.
        return PixelFormat.UNKNOWN;
    }
}
