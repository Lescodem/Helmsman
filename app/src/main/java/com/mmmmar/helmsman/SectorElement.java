package com.mmmmar.helmsman;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;

public class SectorElement {

    private static final int RADIUS_INNER_SEC = 10;
    private static final int RADIUS_OUTER_SEC = 120;
    private static final int MARGIN_SEC = 30;

    private static final float RATIO_TOUCH = 0.5f;
    private static final float ANGLE_PADDING = 0.4f;

    private int colorDark;
    private int colorLight;

    private Path outerSector = new Path();
    private Path innerSector = new Path();
    private Point centerPoint = new Point();

    private int radius;
    private float startAngle;
    private float endAngle;
    private boolean toggle;

    public SectorElement(int colorDark, int colorLight) {
        this.colorDark = colorDark;
        this.colorLight = colorLight;
    }

    public void setPath(float startAngle, float angle, int radius, Point center) {
        this.radius = radius;
        this.startAngle = startAngle;
        this.endAngle = startAngle + angle;

        centerPoint.set(center.x, center.y);

        outerSector.reset();
        innerSector.reset();

        makeSectorPath(outerSector, startAngle + ANGLE_PADDING,  endAngle - ANGLE_PADDING, radius - RADIUS_OUTER_SEC, radius, center);
        int innerSectorOuter = radius - RADIUS_OUTER_SEC - MARGIN_SEC;
        makeSectorPath(innerSector, startAngle + ANGLE_PADDING, endAngle - ANGLE_PADDING, innerSectorOuter - RADIUS_INNER_SEC , innerSectorOuter, center);
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(toggle ? colorLight : colorDark);
        canvas.drawPath(outerSector, paint);
        canvas.drawPath(innerSector, paint);
    }

    public void mark(boolean toggle) {
        this.toggle = toggle;
    }

    public boolean accept(float x, float y) {
        float _x = x - centerPoint.x;
        float _y = y - centerPoint.y;
        // 1、判断是否在半径范围内。
        float len = _x * _x + _y * _y;
        if (len < radius * radius * RATIO_TOUCH * RATIO_TOUCH || len > radius * radius) {
            return false;
        }
        // 2、判断是否在角度范围内。
        double tanAngle = Math.toDegrees(Math.atan2(_y, _x));
        if (tanAngle < 0) tanAngle += 360;
        return (tanAngle >= startAngle) && (tanAngle <= endAngle);
    }

    private void makeSectorPath(Path path, float startAngle, float endAngle, int inner, int outer, Point center) {
        RectF innerRect = new RectF(center.x - inner, center.y - inner,
                center.x + inner, center.y + inner);
        RectF outerRect = new RectF(center.x - outer, center.y - outer,
                center.x + outer, center.y + outer);
        path.reset();
        path.arcTo(innerRect, startAngle, endAngle - startAngle, true);
        path.arcTo(outerRect, endAngle, startAngle - endAngle, false);
        path.close();
    }

    @Override
    public String toString() {
        return "SectorElement{" +
                "centerPoint=" + centerPoint +
                ", radius=" + radius +
                ", startAngle=" + startAngle +
                ", endAngle=" + endAngle +
                ", toggle=" + toggle +
                '}';
    }
}
