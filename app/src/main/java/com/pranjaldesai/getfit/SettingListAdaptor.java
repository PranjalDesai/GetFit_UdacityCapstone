package com.pranjaldesai.getfit;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingListAdaptor extends RecyclerView.Adapter<SettingListAdaptor.SettingItemViewHolder> {

    final private ItemClickListener mOnClickListener;
    private ArrayList<String> mTitle, mSubTitle;
    private ArrayList<Drawable> mDrawable;
    private String tag;

    public SettingListAdaptor(ItemClickListener mOnClickListener, ArrayList<String> title, ArrayList<String> subTitle, ArrayList<Drawable> drawable) {
        this.mOnClickListener = mOnClickListener;
        this.mTitle = title;
        this.mSubTitle= subTitle;
        this.mDrawable= drawable;
    }


    @NonNull
    @Override
    public SettingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        int layoutID = R.layout.setting_list_item;
        LayoutInflater inflater= LayoutInflater.from(context);

        View view= inflater.inflate(layoutID, parent, false);

        return new SettingItemViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingItemViewHolder holder, int position) {
        holder.bind(mTitle.get(position), mDrawable.get(position), mSubTitle.get(position));
    }

    /*
     *   removes all the current data
     */
    public void removeData(){
        mTitle=null;
        mDrawable=null;
        mSubTitle=null;
    }

    public void updateList(ArrayList<String> title, ArrayList<Drawable> image, ArrayList<String> subtitle){
        mTitle= new ArrayList<>();
        mSubTitle= new ArrayList<>();
        mDrawable= new ArrayList<>();
        mTitle.addAll(title);
        mDrawable.addAll(image);
        mSubTitle.addAll(subtitle);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mTitle.size();
    }

    public interface ItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    class SettingItemViewHolder extends  RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.setting_drawable)
        ImageView settingImageView;
        @BindView(R.id.setting_title)
        TextView titleView;
        @BindView(R.id.setting_subtitle)
        TextView subtitleView;
        Context mContext;

        public SettingItemViewHolder(View itemView, Context context) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mContext=context;
            itemView.setOnClickListener(this);
        }

        /*
         *   binds the title and image to the recyclerview
         */
        void bind(String title, Drawable drawable, String subtitle){
            titleView.setText(title);
            subtitleView.setText(subtitle);
            if(drawable!=null){
                settingImageView.setImageDrawable(drawable);
            }
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }
}
