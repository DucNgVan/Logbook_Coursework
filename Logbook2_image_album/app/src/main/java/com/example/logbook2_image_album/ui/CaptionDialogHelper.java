package com.example.logbook2_image_album.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.logbook2_image_album.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Helper component responsible for inflating and displaying caption edit and creation dialogs.
 */
public class CaptionDialogHelper {

    public interface OnCaptionSavedListener {
        void onCaptionSaved(String caption);
    }

    public static void showCaptionDialog(
            Context context,
            LayoutInflater inflater,
            String initialCaption,
            boolean isNewImage,
            OnCaptionSavedListener listener) {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(isNewImage ? R.string.add_image : R.string.edit_caption);

        View view = inflater.inflate(R.layout.dialog_caption_input, null);
        EditText etCaption = view.findViewById(R.id.etCaption);

        if (initialCaption != null) {
            etCaption.setText(initialCaption);
        }

        builder.setView(view);
        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            String caption = etCaption.getText().toString().trim();
            if (caption.isEmpty()) {
                caption = "Untitled";
            }
            if (listener != null) {
                listener.onCaptionSaved(caption);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }
}
