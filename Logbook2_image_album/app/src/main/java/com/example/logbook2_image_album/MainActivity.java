package com.example.logbook2_image_album;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.transition.Fade;
import androidx.transition.TransitionManager;

import com.example.logbook2_image_album.model.ImageItem;
import com.example.logbook2_image_album.repository.AlbumRepository;
import com.example.logbook2_image_album.ui.CaptionDialogHelper;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Main Activity responsible strictly for UI interaction, view bindings, and rendering.
 */
public class MainActivity extends AppCompatActivity {

    private ImageView ivAlbum;
    private TextView tvCounter;
    private TextView tvCaption;
    private ViewGroup mainLayout;

    private AlbumRepository albumRepository;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    CaptionDialogHelper.showCaptionDialog(
                            this,
                            getLayoutInflater(),
                            null,
                            true,
                            caption -> {
                                albumRepository.addImage(uri, caption);
                                updateUI(true);
                            });
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DynamicColors.applyToActivitiesIfAvailable(getApplication());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        albumRepository = new AlbumRepository(this);

        initViews();
        updateUI(false);
    }

    private void initViews() {
        mainLayout = findViewById(R.id.main);
        ivAlbum = findViewById(R.id.ivAlbum);
        tvCounter = findViewById(R.id.tvCounter);
        tvCaption = findViewById(R.id.tvCaption);

        findViewById(R.id.btnNext).setOnClickListener(v -> {
            albumRepository.navigateNext();
            updateUI(true);
        });

        findViewById(R.id.btnPrevious).setOnClickListener(v -> {
            albumRepository.navigatePrevious();
            updateUI(true);
        });

        findViewById(R.id.btnEdit).setOnClickListener(v -> showEditDialog());
        findViewById(R.id.btnDelete).setOnClickListener(v -> showDeleteConfirmation());

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    private void showEditDialog() {
        if (albumRepository.isEmpty()) return;
        ImageItem current = albumRepository.getCurrentImage();
        if (current == null) return;

        CaptionDialogHelper.showCaptionDialog(
                this,
                getLayoutInflater(),
                current.getCaption(),
                false,
                caption -> {
                    albumRepository.updateCaption(albumRepository.getCurrentIndex(), caption);
                    updateUI(true);
                });
    }

    private void showDeleteConfirmation() {
        if (albumRepository.isEmpty()) return;

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_image)
                .setMessage(R.string.delete_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    boolean removed = albumRepository.removeCurrentIndex();
                    if (removed && albumRepository.isEmpty()) {
                        finish();
                    } else {
                        updateUI(true);
                    }
                    Toast.makeText(this, R.string.image_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateUI(boolean animate) {
        if (albumRepository.isEmpty()) return;

        if (animate) {
            Fade fade = new Fade();
            fade.setDuration(300);
            TransitionManager.beginDelayedTransition(mainLayout, fade);
        }

        ImageItem currentItem = albumRepository.getCurrentImage();
        if (currentItem == null) return;

        if (currentItem.isResourceBased()) {
            ivAlbum.setImageResource(currentItem.getResId());
        } else if (currentItem.getUri() != null) {
            ivAlbum.setImageURI(currentItem.getUri());
        }

        tvCaption.setText(currentItem.getCaption());

        String counterText = getString(
                R.string.image_counter_template,
                albumRepository.getCurrentIndex() + 1,
                albumRepository.getCount());
        tvCounter.setText(counterText);
    }
}