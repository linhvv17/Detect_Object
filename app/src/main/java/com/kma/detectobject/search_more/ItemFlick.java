package com.kma.detectobject.search_more;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;


import androidx.recyclerview.widget.RecyclerView;

import com.kma.detectobject.R;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Random;

public class ItemFlick extends AbstractItem<ItemFlick, ItemFlick.ViewHolder> {
    public String title;
    public String link;
    public FlickrModel.Media media;
    public String dateTaken;
    public String description;
    public String published;
    public String author;
    public String authorId;
    public String tags;
    public long id;

    public long getId() {
        try {
            id = Long.valueOf(Uri.parse(link).getLastPathSegment() + "");
        } catch (NullPointerException npe) {
            id = new Random().nextLong();
            npe.printStackTrace();
        }
        return id;
    }

    @Override
    public int getType() {
        return R.id.recycler;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.grid_item;
    }

    @Override
    public void bindView(ItemFlick.ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        Picasso.get().load(media.m).into(holder.img);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            img.getLayoutParams().width = DeviceData.getInstance().getDisplayWidth() / 2;
            img.getLayoutParams().height = DeviceData.getInstance().getDisplayWidth() / 2;
        }
    }
}
