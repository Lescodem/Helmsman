package com.mmmmar.helmsman;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.InvalidParameterException;


public class ControlView extends View {

    private static final String TAG = "ControlView";

    public interface ProgressChangeListener {
        void onChange(float p);
    }

    private static final int DIRECTION_LEFT = 0;
    private static final int DIRECTION_RIGHT = 1;

    @IntDef({DIRECTION_LEFT, DIRECTION_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Direction {}

    private static final int SIZE_VIEW_DEFAULT = 200;
    private static final int SIZE_TEXT_DEFAULT = 30;
    private static final int COLOR_TEXT_DEFAULT = 0xFFF9BB1F;
    private static final int COLOR_SECTOR_DEFAULT = 0xFFF9BB1F;
    private static final int COLOR_SECTOR_LIGHT_DEFAULT = 0XFFEE7806;

    private static final int NUM_SECTOR = 11;
    private static final float ANGLE_MARGIN = 0;
    private static final float ANGLE_SECTOR = (90.0f - (ANGLE_MARGIN * (NUM_SECTOR - 1))) / NUM_SECTOR;

    private ControlElement[] elementArray = new ControlElement[NUM_SECTOR];
    private int elementIndex = elementArray.length / 2;
    private Point elementCenter = new Point();
    private Paint elementPaint = new Paint();

    private String elementInfo = "0%";
    private Point elementInfoPoint = new Point();
    private Paint elementInfoPaint = new Paint();

    @Direction
    private int direction;

    private ProgressChangeListener progressListener;

    public ControlView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initPaint(elementPaint);
        initPaint(elementInfoPaint);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ControlView);
        // 控件方向。
        direction = typedArray.getInt(R.styleable.ControlView_direction, DIRECTION_LEFT);

        // 字体配置。
        elementInfoPaint.setColor(typedArray.getColor(R.styleable.ControlView_infoColor, COLOR_TEXT_DEFAULT));
        elementInfoPaint.setTextSize(typedArray.getDimensionPixelSize(R.styleable.ControlView_infoSize,
                DensityUtil.sp2px(context, SIZE_TEXT_DEFAULT)));
        // 扇区颜色。
        int colorDark = typedArray.getColor(R.styleable.ControlView_sectorColor, COLOR_SECTOR_DEFAULT);
        int colorLight = typedArray.getColor(R.styleable.ControlView_sectorLightColor, COLOR_SECTOR_LIGHT_DEFAULT);
        // 初始化扇区。
        for (int i = 0; i < elementArray.length; ++i) {
            elementArray[i] = new ControlElement(colorDark, colorLight);
        }
        initSectorState();

        typedArray.recycle();
    }

    public void setProgressListener(ProgressChangeListener listener) {
        progressListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int length = DensityUtil.dip2px(getContext(), SIZE_VIEW_DEFAULT);
        setMeasuredDimension(length, length);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (direction == DIRECTION_LEFT) {
            // 中心点坐标。
            elementCenter.x = 0;
            elementCenter.y = getHeight();
            // 信息坐标。
            elementInfoPoint.x = getWidth() / 4;
            elementInfoPoint.y = getHeight() - getHeight() / 4;
            // 起始扇区角度值。
            float angle = 270;
            for (ControlElement aSectorArray : elementArray) {
                aSectorArray.setPath(angle, ANGLE_SECTOR, getHeight(), elementCenter);
                angle = angle + ANGLE_SECTOR + ANGLE_MARGIN;
            }
        } else if (direction == DIRECTION_RIGHT) {
            // 中心点坐标。
            elementCenter.x = getWidth();
            elementCenter.y = getHeight();
            // 信息坐标。
            elementInfoPoint.x = getWidth() - getWidth() / 4;
            elementInfoPoint.y = getHeight() - getHeight() / 4;
            float angle = 270 - ANGLE_SECTOR;
            for (ControlElement aSectorArray : elementArray) {
                aSectorArray.setPath(angle, ANGLE_SECTOR, getHeight(), elementCenter);
                angle = angle - ANGLE_SECTOR - ANGLE_MARGIN;
            }
        } else {
           throw new InvalidParameterException("unsupported direction!");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (ControlElement element : elementArray) {
            element.draw(canvas, elementPaint);
        }

        float infoWidth = elementInfoPaint.measureText(elementInfo);
        canvas.drawText(elementInfo, elementInfoPoint.x - infoWidth / 2,
                elementInfoPoint.y + SIZE_TEXT_DEFAULT / 2, elementInfoPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN:
                return touchStart(event);
            case MotionEvent.ACTION_UP:
                return touchEnd(event);
        }
        return super.onTouchEvent(event);
    }

    private boolean touchEnd(MotionEvent event) {
        initSectorState();
        calcSectorInfo();
        invalidate();
        return true;
    }

    private boolean touchStart(MotionEvent event) {
        int posMiddle = elementArray.length / 2;
        boolean accept = false;
        for (int i = 0; i <= posMiddle; ++i) {
            ControlElement element = elementArray[i];
            if (element.accept(event.getX(), event.getY()) || accept) {
                // 触点所在的扇区变化时重绘视图。
                if (i != elementIndex && !accept) {
                    elementIndex = i;
                    calcSectorInfo();
                    invalidate();
                }
                accept = true;
                element.mark(true);
            } else {
                element.mark(false);
            }
        }
        if (!accept) {
            // 触点不在上半部分，在下半部分查找。
            for (int i = elementArray.length - 1; i >= posMiddle; --i) {
                ControlElement element = elementArray[i];
                if (element.accept(event.getX(), event.getY()) || accept) {
                    element.mark(true);
                    // 触点所在的扇区变化时重绘视图。
                    if (i != elementIndex && !accept) {
                        elementIndex = i;
                        calcSectorInfo();
                        invalidate();
                    }
                    accept = true;
                } else {
                    element.mark(false);
                }
            }
        } else {
            // 触点在上半部分，将下半部分标记为空闲状态。
            // for (int i = elementArray.length - 1; i > posMiddle; --i) {
            //     elementArray[i].mark(false);
            // }
            // 实际仅需要重新标记下半部分第一个扇区。
            elementArray[posMiddle + 1].mark(false);
        }
        return true;
    }

    private void initPaint(Paint paint) {
        paint.setDither(true);
        paint.setAntiAlias(true);
    }

    private void initSectorState() {
        for (ControlElement element : elementArray) {
            element.mark(false);
        }
        elementIndex = elementArray.length / 2;
        elementArray[elementIndex].mark(true);
    }

    private void calcSectorInfo() {
        int middle = elementArray.length / 2;
        int level = Math.abs(middle - elementIndex);
        int ratio = (int) ((float) level / middle * 100);
        elementInfo = ratio + "%";
        if (progressListener != null) {
            progressListener.onChange((float) elementIndex / NUM_SECTOR);
        }
    }

}
