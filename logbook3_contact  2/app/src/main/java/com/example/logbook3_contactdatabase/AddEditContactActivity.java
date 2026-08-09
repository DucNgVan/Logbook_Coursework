package com.example.logbook3_contactdatabase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity to add a new contact or edit an existing one.
 */
public class AddEditContactActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "EXTRA_ID";
    public static final String EXTRA_NAME = "EXTRA_NAME";
    public static final String EXTRA_PHONE = "EXTRA_PHONE";
    public static final String EXTRA_IMAGE_URI = "EXTRA_IMAGE_URI";

    private TextInputEditText etName, etPhone;
    private ImageView imgAvatarPreview;
    private MaterialButton btnSave, btnDelete;
    private DatabaseHelper db;
    private int contactId = -1;
    private String selectedImageUri = "";

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(uri, 
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignored) {}
                    selectedImageUri = uri.toString();
                    imgAvatarPreview.setImageURI(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_contact);

        db = new DatabaseHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        imgAvatarPreview = findViewById(R.id.imgAvatarPreview);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);

        imgAvatarPreview.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Check if we are editing an existing contact
        if (getIntent().hasExtra(EXTRA_ID)) {
            contactId = getIntent().getIntExtra(EXTRA_ID, -1);
            etName.setText(getIntent().getStringExtra(EXTRA_NAME));
            etPhone.setText(getIntent().getStringExtra(EXTRA_PHONE));
            selectedImageUri = getIntent().getStringExtra(EXTRA_IMAGE_URI);
            
            if (selectedImageUri != null && !selectedImageUri.isEmpty()) {
                if (selectedImageUri.startsWith("drawable/")) {
                    String drawableName = selectedImageUri.substring("drawable/".length());
                    int resId = getResources().getIdentifier(drawableName, "drawable", getPackageName());
                    if (resId != 0) {
                        imgAvatarPreview.setImageResource(resId);
                    } else {
                        imgAvatarPreview.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                } else {
                    try {
                        imgAvatarPreview.setImageURI(Uri.parse(selectedImageUri));
                    } catch (Exception e) {
                        imgAvatarPreview.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                }
            }

            setTitle(R.string.edit_contact);
            toolbar.setTitle(R.string.edit_contact);
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            setTitle(R.string.add_contact);
            toolbar.setTitle(R.string.add_contact);
            btnDelete.setVisibility(View.GONE);
        }

        btnSave.setOnClickListener(v -> saveContact());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void saveContact() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (contactId == -1) {
            db.addContact(new Contact(name, phone, selectedImageUri));
        } else {
            db.updateContact(new Contact(contactId, name, phone, selectedImageUri));
        }

        Toast.makeText(this, R.string.contact_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    db.deleteContact(contactId);
                    Toast.makeText(this, R.string.contact_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}