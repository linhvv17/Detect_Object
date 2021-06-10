package com.kma.detectobject.search_more;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;


import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.kma.detectobject.R;
import com.mikepenz.fastadapter.items.AbstractItem;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Random;

public class ItemFlick extends AbstractItem<ItemFlick, ItemFlick.ViewHolder> {

    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("link")
    @Expose
    private String link;
    @SerializedName("media")
    @Expose
    private Media media;
    @SerializedName("date_taken")
    @Expose
    private String dateTaken;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("published")
    @Expose
    private String published;
    @SerializedName("author")
    @Expose
    private String author;
    @SerializedName("author_id")
    @Expose
    private String authorId;
    @SerializedName("tags")
    @Expose
    private String tags;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public String getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(String dateTaken) {
        this.dateTaken = dateTaken;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

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
        Picasso.get().load(media.getM()).into(holder.img);
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
