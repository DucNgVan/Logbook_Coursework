package com.example.logbook3_contactdatabase;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Adapter for the RecyclerView to display contact items.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<Contact> contactList;
    private OnContactClickListener listener;

    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    public ContactAdapter(List<Contact> contactList, OnContactClickListener listener) {
        this.contactList = contactList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.tvName.setText(contact.getName());
        holder.tvPhone.setText(contact.getPhoneNumber());

        // Load avatar image from URI or drawable resource
        if (contact.getImageUri() != null && !contact.getImageUri().isEmpty()) {
            String uriStr = contact.getImageUri();
            if (uriStr.startsWith("drawable/")) {
                String drawableName = uriStr.substring("drawable/".length());
                int resId = holder.itemView.getContext().getResources().getIdentifier(
                        drawableName, "drawable", holder.itemView.getContext().getPackageName());
                if (resId != 0) {
                    holder.imgAvatar.setImageResource(resId);
                } else {
                    holder.imgAvatar.setImageResource(R.drawable.ic_launcher_foreground);
                }
            } else {
                try {
                    holder.imgAvatar.setImageURI(Uri.parse(uriStr));
                } catch (Exception e) {
                    holder.imgAvatar.setImageResource(R.drawable.ic_launcher_foreground);
                }
            }
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_launcher_foreground);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick(contact);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName;
        TextView tvPhone;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
        }
    }
}