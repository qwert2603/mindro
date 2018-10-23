package com.example.alex.dragndraw;

import android.support.annotation.DrawableRes;

import java.io.Serializable;

public class Figure implements Serializable {

    public enum FigureType {
        FILL_RECT(R.drawable.ic_fill_rect),
        OVAL(R.drawable.ic_oval),
        LINE_4(R.drawable.ic_line_4),
        LINE_9(R.drawable.ic_line_9),
        LINE_14(R.drawable.ic_line_14),
        ROUND_RECT(R.drawable.ic_round_rect),
        FILL_TRIANGLE_1(R.drawable.ic_fill_triangle_1),   // равнобедренный
        FILL_TRIANGLE_2(R.drawable.ic_fill_triangle_2),   // прямоугольный
        RECT(R.drawable.ic_rect),
        TRIANGLE_1(R.drawable.ic_triangle_1)
        ;

        @DrawableRes private final int mIconId;

        FigureType(@DrawableRes int iconId) {
            mIconId = iconId;
        }

        @DrawableRes
        public int getIconId() {
            return mIconId;
        }
    }

    // type of this figure
    public FigureType mType;

    // point1
    public float mX1;
    public float mY1;

    // point2
    public float mX2;
    public float mY2;

    // rotation (around point2)
    public float mA;

    // color of this figure
    public int mColorNumber;

}
