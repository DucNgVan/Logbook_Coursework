package com.example.logbook2_image_album.repository;

import android.content.Context;
import android.net.Uri;

import com.example.logbook2_image_album.R;
import com.example.logbook2_image_album.model.ImageItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository class managing album dataset, item operations, and navigation state.
 */
public class AlbumRepository {
    private final List<ImageItem> albumItems = new ArrayList<>();
    private int currentIndex = 0;

    public AlbumRepository(Context context) {
        initSampleData(context);
    }

    private void initSampleData(Context context) {
        albumItems.add(new ImageItem(R.drawable.sample_image_1, context.getString(R.string.caption_1)));
        albumItems.add(new ImageItem(R.drawable.sample_image_2, context.getString(R.string.caption_2)));
        albumItems.add(new ImageItem(R.drawable.sample_image_3, context.getString(R.string.caption_3)));
        albumItems.add(new ImageItem(R.drawable.sample_image_4, context.getString(R.string.caption_4)));
        albumItems.add(new ImageItem(R.drawable.sample_image_5, context.getString(R.string.caption_5)));
    }

    public List<ImageItem> getAlbumItems() {
        return Collections.unmodifiableList(albumItems);
    }

    public boolean isEmpty() {
        return albumItems.isEmpty();
    }

    public int getCount() {
        return albumItems.size();
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public ImageItem getCurrentImage() {
        if (isEmpty()) return null;
        return albumItems.get(currentIndex);
    }

    public void navigateNext() {
        if (isEmpty()) return;
        currentIndex = (currentIndex + 1) % albumItems.size();
    }

    public void navigatePrevious() {
        if (isEmpty()) return;
        currentIndex = (currentIndex - 1 + albumItems.size()) % albumItems.size();
    }

    public void addImage(Uri uri, String caption) {
        albumItems.add(new ImageItem(uri, caption));
        currentIndex = albumItems.size() - 1;
    }

    public void updateCaption(int index, String caption) {
        if (index >= 0 && index < albumItems.size()) {
            albumItems.get(index).setCaption(caption);
        }
    }

    public boolean removeCurrentIndex() {
        if (isEmpty()) return false;
        albumItems.remove(currentIndex);
        if (!isEmpty() && currentIndex >= albumItems.size()) {
            currentIndex = albumItems.size() - 1;
        }
        return true;
    }
}
