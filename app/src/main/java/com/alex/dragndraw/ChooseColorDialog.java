package com.alex.dragndraw;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;

public class ChooseColorDialog extends DialogFragment {

    public static final String EXTRA_COLOR = "com.alex.dragndraw.COLOR";
    private static final String colorKey = "color";

    private View mView;

    // названия цветов
    private String[] mColorNames;

    private ArrayList<RadioButton> mRadioButtons = new ArrayList<>();

    // текущий выбранный цвет (яркость максимальна)
    private int mCurrentColor;

    // яркость выбранного цвета (от 1 до 8)
    private int mColorMaskNumber;

    public static ChooseColorDialog newInstance(int color) {
        ChooseColorDialog result = new ChooseColorDialog();
        Bundle args = new Bundle();
        args.putInt(colorKey, color);
        result.setArguments(args);
        return result;
    }

    @SuppressLint("InflateParams")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mColorNames = getResources().getStringArray(R.array.array_colors);

        mView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_choose_color, null);

        mRadioButtons.add(mView.<RadioButton>findViewById(R.id.radioButton0));
        mRadioButtons.add(mView.<RadioButton>findViewById(R.id.radioButton1));
        mRadioButtons.add(mView.<RadioButton>findViewById(R.id.radioButton2));
        mRadioButtons.add(mView.<RadioButton>findViewById(R.id.radioButton3));
        mRadioButtons.add(mView.<RadioButton>findViewById(R.id.radioButton4));
        mRadioButtons.add(mView.<RadioButton>findViewById(R.id.radioButton5));
        mRadioButtons.add(mView.<RadioButton>findViewById(R.id.radioButton6));
        mRadioButtons.add(mView.<RadioButton>findViewById(R.id.radioButton7));
        mRadioButtons.add(mView.<RadioButton>findViewById(R.id.radioButton8));

        // получаем текущий цвет
        mCurrentColor = getArguments().getInt(colorKey);

        // получаем яркость цвета
        int alpha = Color.alpha(mCurrentColor);
        mColorMaskNumber = (alpha + 1) / 0x20;

        // цвет RadioButton'ов имеет максимальную яркость
        int checkedColor = mCurrentColor | 0xff000000;

        for (int i = 0; i != mRadioButtons.size(); ++i) {
            final RadioButton radioButton = mRadioButtons.get(i);

            // ищем нужный RadioButton и отмечаем его
            // у RadioButton всегда цвет максимально яркий
            if (radioButton.getCurrentTextColor() == checkedColor) {
                radioButton.setChecked(true);
            }

            // назначаем обработчик нажатия для этого radioButton,
            // чтобы менять номер маски и отображать его около названия цвета
            radioButton.setClickable(true);
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ++mColorMaskNumber;
                    if (mColorMaskNumber == 9) {
                        mColorMaskNumber = 1;
                    }
                    updateRadioButtonsText();
                }
            });
        }

        updateRadioButtonsText();

        // назначаем обработчик в конце, чтобы не сбросить значения mCurrentColor и mColorMaskNumber
        // при программном назвачении выделенного RadioButton
        RadioGroup radioGroup = mView.findViewById(R.id.choose_color_radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mCurrentColor = ((RadioButton) group.findViewById(checkedId)).getCurrentTextColor();

                // будет увеличено до 1 в обработчике нажатия для конкретной RadioButton
                mColorMaskNumber = 0;
            }
        });
    }

    // обновить текст RadioButton'ов и дописать цифру-маску около нужного RadioButton'а
    @SuppressLint("SetTextI18n")
    private void updateRadioButtonsText() {
        for (int i = 0; i != mRadioButtons.size(); ++i) {
            RadioButton radioButton = mRadioButtons.get(i);
            radioButton.setText(mColorNames[i]);
            if (radioButton.isChecked()) {
                radioButton.setText(mColorNames[i] + "  " + mColorMaskNumber);
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setView(mView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent();
                        // применяем яркость цвета и возвращаем результат
                        mCurrentColor = Color.argb(0x20 * mColorMaskNumber - 1,
                                Color.red(mCurrentColor),
                                Color.green(mCurrentColor),
                                Color.blue(mCurrentColor));
                        i.putExtra(EXTRA_COLOR, mCurrentColor);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
                    }
                })
                .create();
    }
}