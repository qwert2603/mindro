package com.example.alex.dragndraw;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class FigureDrawingView extends View {

    private static final String boxesKey = "boxesKey";
    private static final String superStateKey = "superStateKey";
    private static final String colorNumberKey = "colorNumberKey";
    private static final String figureTypeKey = "figureTypeKey";

    public static final int DEFAULT_COLOR = 0x3fff0000;

    private ArrayList<Figure> mFigures = new ArrayList<>();

    private Figure mCurrentFigure;
    private Figure.FigureType mCurrentFigureType = Figure.FigureType.FILL_RECT;
    private int mCurrentColor;

    // используются при прорисовке
    private RectF mRectF = new RectF();
    private Path mPath = new Path();

    // начальный угол поворота
    private float mR0;

    private int mFirstPointerId = -1;
    private int mSecondPointerId = -1;

    private Paint mFigurePaint;
    private Paint mBackgroundPaint;

    public FigureDrawingView(Context context) {
        this(context, null);
    }

    public FigureDrawingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        mFigurePaint = new Paint();
        // by default
        setCurrentColor(DEFAULT_COLOR);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(getResources().getColor(android.R.color.white));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPaint(mBackgroundPaint);

        int lastColorNumber = mCurrentColor;

        for (Figure f : mFigures) {
            float left = Math.min(f.mX1, f.mX2);
            float right = Math.max(f.mX1, f.mX2);
            float top = Math.min(f.mY1, f.mY2);
            float bottom = Math.max(f.mY1, f.mY2);

            mRectF.left = left;
            mRectF.right = right;
            mRectF.top = top;
            mRectF.bottom = bottom;

            setCurrentColor(f.mColorNumber);

            if (Math.abs(f.mA) > 1) {
                canvas.rotate(-f.mA, f.mX2, f.mY2);
            }

            switch (f.mType) {
                case FILL_RECT:
                    mFigurePaint.setStyle(Paint.Style.FILL);
                    canvas.drawRect(left, top, right, bottom, mFigurePaint);
                    break;
                case OVAL:
                    mFigurePaint.setStyle(Paint.Style.FILL);
                    canvas.drawOval(mRectF, mFigurePaint);
                    break;
                case LINE_4:
                    mFigurePaint.setStrokeWidth(4);
                    canvas.drawLine(f.mX1, f.mY1, f.mX2, f.mY2, mFigurePaint);
                    break;
                case LINE_9:
                    mFigurePaint.setStrokeWidth(9);
                    canvas.drawLine(f.mX1, f.mY1, f.mX2, f.mY2, mFigurePaint);
                    break;
                case LINE_14:
                    mFigurePaint.setStrokeWidth(14);
                    canvas.drawLine(f.mX1, f.mY1, f.mX2, f.mY2, mFigurePaint);
                    break;
                case ROUND_RECT:
                    mFigurePaint.setStyle(Paint.Style.FILL);
                    canvas.drawRoundRect(mRectF, (right - left) / 8, (bottom - top) / 8, mFigurePaint);
                    break;
                case FILL_TRIANGLE_1:
                    mFigurePaint.setStyle(Paint.Style.FILL);
                    drawTriangle1(canvas, f);
                    break;
                case FILL_TRIANGLE_2:
                    mFigurePaint.setStyle(Paint.Style.FILL);
                    mPath.reset();
                    mPath.moveTo(f.mX1, f.mY1);
                    mPath.lineTo(f.mX2, f.mY2);
                    mPath.lineTo(f.mX1, f.mY2);
                    mPath.lineTo(f.mX1, f.mY1);
                    canvas.drawPath(mPath, mFigurePaint);
                    break;
                case RECT:
                    mFigurePaint.setStyle(Paint.Style.STROKE);
                    mFigurePaint.setStrokeWidth(9);
                    canvas.drawRect(left, top, right, bottom, mFigurePaint);
                    break;
                case TRIANGLE_1:
                    mFigurePaint.setStyle(Paint.Style.STROKE);
                    mFigurePaint.setStrokeWidth(9);
                    drawTriangle1(canvas, f);
                    break;
            }


            if (Math.abs(f.mA) > 1) {
                canvas.rotate(f.mA, f.mX2, f.mY2);
            }
        }

        setCurrentColor(lastColorNumber);
    }

    private void drawTriangle1(Canvas canvas, Figure f) {
        float a = (f.mX1 + f.mX2) / 2;
        mPath.reset();
        mPath.moveTo(a, f.mY1);
        mPath.lineTo(f.mX2, f.mY2);
        mPath.lineTo(f.mX1, f.mY2);
        mPath.lineTo(a, f.mY1);
        canvas.drawPath(mPath, mFigurePaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int index = event.getActionIndex();
        int id = event.getPointerId(index);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mFirstPointerId = id;
                mCurrentFigure = new Figure();
                mCurrentFigure.mType = mCurrentFigureType;
                mCurrentFigure.mX1 = mCurrentFigure.mX2 = event.getX(index);
                mCurrentFigure.mY1 = mCurrentFigure.mY2 = event.getY(index);
                mCurrentFigure.mA = 0.0f;
                mCurrentFigure.mColorNumber = mCurrentColor;
                mFigures.add(mCurrentFigure);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mFirstPointerId = -1;
                mSecondPointerId = -1;
                if (mCurrentFigure != null) {
                    if (mCurrentFigure.mX1 == event.getX(index) || mCurrentFigure.mY1 == event.getY(index)) {
                        mFigures.remove(mCurrentFigure);
                    }
                    mCurrentFigure = null;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mSecondPointerId = id;
                mR0 = (float) (-180 / 3.1415926 * Math.atan2(event.getY(index) - mCurrentFigure.mY2,
                        event.getX(index) - mCurrentFigure.mX2)) - mCurrentFigure.mA;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (id == mFirstPointerId) {
                    mFirstPointerId = -1;
                } else if (id == mSecondPointerId) {
                    mSecondPointerId = -1;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCurrentFigure != null) {
                    for (int i = 0; i != event.getPointerCount(); ++i) {
                        int d = event.getPointerId(i);
                        if (d == mFirstPointerId) {
                            if (mCurrentFigure.mA == 0) {
                                mCurrentFigure.mX2 = event.getX(i);
                                mCurrentFigure.mY2 = event.getY(i);
                            }
                        } else if (d == mSecondPointerId) {
                            mCurrentFigure.mA = (float) (-180 / 3.1415926 * Math.atan2(event.getY(i) - mCurrentFigure.mY2,
                                    event.getX(i) - mCurrentFigure.mX2)) - mR0;
                        }
                    }
                    invalidate();
                }
                break;
        }
        return true;
    }

    public void clearBoxes() {
        mFigures.clear();
        invalidate();
    }

    public void removeLast() {
        if (!mFigures.isEmpty()) {
            mFigures.remove(mFigures.size() - 1);
            invalidate();
        }
    }

    public int getCurrentColor() {
        return mCurrentColor;
    }

    public void setCurrentColor(int clr) {
        mCurrentColor = clr;
        mFigurePaint.setColor(mCurrentColor);
    }

    public void setFigureType(Figure.FigureType figureType) {
        mCurrentFigureType = figureType;
    }

    public Figure.FigureType getCurrentFigureType() {
        return mCurrentFigureType;
    }

    public Bitmap getPicture() {
        Bitmap b = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        draw(c);
        return b;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        state.putParcelable(superStateKey, super.onSaveInstanceState());
        state.putSerializable(boxesKey, mFigures);
        state.putInt(colorNumberKey, mCurrentColor);
        state.putString(figureTypeKey, mCurrentFigureType.toString());
        return state;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = ((Bundle) state);
        Parcelable superState = bundle.getParcelable(superStateKey);
        super.onRestoreInstanceState(superState);
        mFigures = (ArrayList<Figure>) bundle.getSerializable(boxesKey);
        setCurrentColor(bundle.getInt(colorNumberKey));
        mCurrentFigureType = Figure.FigureType.valueOf(bundle.getString(figureTypeKey));
    }
}