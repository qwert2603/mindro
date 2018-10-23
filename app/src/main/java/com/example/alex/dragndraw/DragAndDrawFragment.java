package com.example.alex.dragndraw;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DragAndDrawFragment extends Fragment {

    private static final String PREF_PIC_CNT = "picCnt";
    private static final int REQUEST_COLOR = 1;
    private static final int REQUEST_FIGURE_TYPE = 2;
    private static final int REQUEST_CLEAR_ALL = 3;
    private static final int REQUEST_PERMISSION_STORAGE = 4;

    private FigureDrawingView mFigureDrawingView;

    private MenuItem mChangeFigureTypeMenuItem;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drag_and_draw, container, false);
        mFigureDrawingView = view.findViewById(R.id.box_drawing_view_1);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateActionBarColor();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_drag_and_draw_fragment, menu);
        mChangeFigureTypeMenuItem = menu.findItem(R.id.action_change_figure);
        updateChangeFigureTypeMenuItemIcon();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_clear_figures) {
            final ClearAllDialog clearAllDialog = new ClearAllDialog();
            clearAllDialog.setTargetFragment(this, REQUEST_CLEAR_ALL);
            clearAllDialog.show(requireFragmentManager(), null);
            return true;
        }
        if (item.getItemId() == R.id.action_save_boxes) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                saveBoxes();
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
            }
            return true;
        }
        if (item.getItemId() == R.id.action_share) {
            try {
                File imagesFolder = new File(requireContext().getCacheDir(), "images");
                if (!imagesFolder.mkdirs()) Log.e("AASSDD", "file#mkdirs returned false");
                File file = new File(imagesFolder, "shared_image.png");

                FileOutputStream stream = new FileOutputStream(file);
                Bitmap image = mFigureDrawingView.getPicture();
                image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.flush();
                stream.close();
                Uri uri = FileProvider.getUriForFile(requireContext(), "com.alex.dragndraw", file);
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/png");
                startActivity(intent);
            } catch (IOException e) {
                Log.e("AASSDD", e.toString(), e);
            }
        }
        if (item.getItemId() == R.id.action_undo) {
            mFigureDrawingView.removeLast();
            return true;
        }
        if (item.getItemId() == R.id.action_choose_color) {
            ChooseColorDialog dialog = ChooseColorDialog.newInstance(mFigureDrawingView.getCurrentColor());
            dialog.setTargetFragment(this, REQUEST_COLOR);
            dialog.show(requireFragmentManager(), null);
            return true;
        }
        if (item.getItemId() == R.id.action_change_figure) {
            String figureType = mFigureDrawingView.getCurrentFigureType().toString();
            ChooseFigureTypeDialog dialog = ChooseFigureTypeDialog.newInstance(figureType);
            dialog.setTargetFragment(this, REQUEST_FIGURE_TYPE);
            dialog.show(requireFragmentManager(), null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_STORAGE && grantResults.length == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveBoxes();
            } else {
                Toast.makeText(getActivity(), R.string.text_no_permission, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // название метода пугает, но ничего не поделаешь :)
    private void updateChangeFigureTypeMenuItemIcon() {
        Figure.FigureType figureType = mFigureDrawingView.getCurrentFigureType();
        mChangeFigureTypeMenuItem.setIcon(figureType.getIconId());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_COLOR) {
            int color = data.getIntExtra(ChooseColorDialog.EXTRA_COLOR, -1);
            mFigureDrawingView.setCurrentColor(color);
            updateActionBarColor();
        } else if (requestCode == REQUEST_FIGURE_TYPE) {
            String stringFigureType = data.getStringExtra(ChooseFigureTypeDialog.EXTRA_FIGURE_TYPE);
            Figure.FigureType figureType = Figure.FigureType.valueOf(stringFigureType);
            mFigureDrawingView.setFigureType(figureType);
            updateChangeFigureTypeMenuItemIcon();
        } else if (requestCode == REQUEST_CLEAR_ALL) {
            mFigureDrawingView.clearBoxes();
        }
    }

    private void updateActionBarColor() {
        // ActionBar всегда яркий, поэтому добавляем 0xff000000
        int color = mFigureDrawingView.getCurrentColor() | 0xff000000;
        // если белый, то черный
        if (Color.red(color) == 0xff && Color.green(color) == 0xff && Color.blue(color) == 0xff) {
            color = 0xff000000;
        }
        final ActionBar supportActionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setBackgroundDrawable(new ColorDrawable(color));
        }
    }

    private void saveBoxes() {
        int index = getNextIndex();
        File picFile = getNewFile(index);
        try (FileOutputStream fos = new FileOutputStream(picFile)) {
            Bitmap b = mFigureDrawingView.getPicture();
            b.compress(Bitmap.CompressFormat.PNG, 100, fos);
            Toast.makeText(getActivity(), getString(R.string.text_saved, picFile.getAbsolutePath()), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("AASSDD", e.toString(), e);
        }
    }

    private File getNewFile(int index) {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File alb = new File(dir, getString(R.string.app_name));
        if (!alb.mkdirs()) Log.e("AASSDD", "file#mkdirs returned false");
        String fileName = "pic #" + index + ".png";
        return new File(alb, fileName);
    }

    private int getNextIndex() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int picNumber = sp.getInt(PREF_PIC_CNT, 0);
        sp.edit()
                .putInt(PREF_PIC_CNT, picNumber + 1)
                .apply();
        return picNumber;
    }
}