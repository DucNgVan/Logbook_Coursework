package com.example.logbook3_contactdatabase;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

/**
 * Main activity that displays the contact list.
 */
public class MainActivity extends AppCompatActivity implements ContactAdapter.OnContactClickListener {

    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private DatabaseHelper db;
    private FloatingActionButton fabAddContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        fabAddContact = findViewById(R.id.fabAddContact);
        fabAddContact.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddEditContactActivity.class);
            startActivity(intent);
        });

        db = new DatabaseHelper(this);
        db.seedDefaultDataIfEmpty();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshContactList();
    }

    /**
     * Refreshes the contact list from the database.
     */
    private void refreshContactList() {
        List<Contact> contacts = db.getAllContacts();
        adapter = new ContactAdapter(contacts, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onContactClick(Contact contact) {
        Intent intent = new Intent(MainActivity.this, AddEditContactActivity.class);
        intent.putExtra(AddEditContactActivity.EXTRA_ID, contact.getId());
        intent.putExtra(AddEditContactActivity.EXTRA_NAME, contact.getName());
        intent.putExtra(AddEditContactActivity.EXTRA_PHONE, contact.getPhoneNumber());
        intent.putExtra(AddEditContactActivity.EXTRA_IMAGE_URI, contact.getImageUri());
        startActivity(intent);
    }
}