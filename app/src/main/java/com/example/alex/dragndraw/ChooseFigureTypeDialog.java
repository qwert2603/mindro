package com.example.alex.dragndraw;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ChooseFigureTypeDialog extends DialogFragment {

    private static final String keyFigureType = "keyFigureType";
    public static final String EXTRA_FIGURE_TYPE = "com.example.alex.dragndraw.FIGURE_TYPE";

    private Figure.FigureType mFigureType;

    private GridView mGridView;

    public static ChooseFigureTypeDialog newInstance(String figureType) {
        ChooseFigureTypeDialog result = new ChooseFigureTypeDialog();
        Bundle args = new Bundle();
        args.putString(keyFigureType, figureType);
        result.setArguments(args);
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFigureType = Figure.FigureType.valueOf(getArguments().getString(keyFigureType));

        final FigureTypeAdapter adapter = new FigureTypeAdapter(getActivity(), Figure.FigureType.values());
        mGridView = (GridView) getActivity().getLayoutInflater().inflate(R.layout.dialog_choose_figure_type, null);
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mFigureType = adapter.getItem(position);
                adapter.notifyDataSetChanged();
                if (getTargetFragment() != null) {
                    Intent i = new Intent();
                    i.putExtra(EXTRA_FIGURE_TYPE, mFigureType.toString());
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
                    dismiss();
                }
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.text_choose_figure_type)
                .setView(mGridView)
                .setNegativeButton(R.string.text_back, null)
                .create();
    }

    private class FigureTypeAdapter extends ArrayAdapter<Figure.FigureType> {

        private Activity mActivity;

        FigureTypeAdapter(Activity activity, Figure.FigureType[] figureTypes) {
            super(activity, 0, figureTypes);
            mActivity = activity;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = mActivity.getLayoutInflater().inflate(R.layout.item_figure_type, parent, false);
            }

            Figure.FigureType figureType = getItem(position);

            ImageView imageView = convertView.findViewById(R.id.item_figure_type_image_view);
            imageView.setImageResource(figureType.getIconId());

            return convertView;
        }
    }
}