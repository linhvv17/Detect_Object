package com.kma.detectobject.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kma.detectobject.R;
import com.kma.detectobject.activity.DetailObjectActivity;
import com.kma.detectobject.database.DatabaseHandler;
import com.kma.detectobject.database.Item;
import com.kma.detectobject.database.ItemClickListener;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Item> itemArrayList;

    private DatabaseHandler databaseHandler;


    public ItemAdapter(Context mContext, ArrayList<Item> itemArrayList,DatabaseHandler databaseHandler) {
        this.mContext = mContext;
        this.itemArrayList = itemArrayList;
        this.databaseHandler = databaseHandler;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.row_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemArrayList.get(position);
        Glide.with(mContext)
                .load(item.getPath())
                .into(holder.mImageItem);
        holder.mTextName.setText(item.getName());
        holder.mTextMean.setText(item.getMean());
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                if(isLongClick){
                    Toast.makeText(mContext, "Long Click: "+itemArrayList.get(position), Toast.LENGTH_SHORT).show();
                    databaseHandler.deleteItem(itemArrayList.get(position).getId());
                    notifyDataSetChanged();
                }
                else{
                    Toast.makeText(mContext, " "+itemArrayList.get(position).getId(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(mContext, DetailObjectActivity.class);
                    intent.putExtra("idItem",itemArrayList.get(position).getId());
                    mContext.startActivity(intent);
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return itemArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {
        private ImageView mImageItem;
        private TextView mTextName;
        private TextView mTextMean;
        private ItemClickListener itemClickListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageItem = itemView.findViewById(R.id.img_view_item);
            mTextName = itemView.findViewById(R.id.tv_english_item);
            mTextMean = itemView.findViewById(R.id.tv_mean_item);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener)
        {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v,getAdapterPosition(),false);
        }

        @Override
        public boolean onLongClick(View v) {
            itemClickListener.onClick(v,getAdapterPosition(),true);
            return true;
        }
    }
}
