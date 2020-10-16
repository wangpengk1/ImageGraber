package com.newasia.imagegraberlib.Adapter;

import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.newasia.imagegraberlib.Bean.IMGImageViewModel;
import com.newasia.imagegraberlib.R;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class GalleryDirAdapter extends BaseQuickAdapter< Map.Entry<String, List<IMGImageViewModel>>, BaseViewHolder >
{
    private int mSelectPos = 0;

    public GalleryDirAdapter() {
        super(R.layout.grallery_dir_item);
    }


    public Map.Entry<String, List<IMGImageViewModel>> getSelectData()
    {
        if(getItemCount()>0 && mSelectPos<getItemCount())
        {
            return getItem(mSelectPos);
        }else return null;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, Map.Entry<String, List<IMGImageViewModel>> item)
    {
        ImageView imageView = holder.getView(R.id.first_image);
        if(item.getValue().size()>0)
        {
            Glide.with(imageView).load(item.getValue().get(0).getUri())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(false)
                    .into(imageView);
        }

        if(item.getKey().compareToIgnoreCase("WeiXin")==0)
            holder.setText(R.id.dir_name,"微信");
        else if(item.getKey().compareToIgnoreCase("Screenshots")==0)
            holder.setText(R.id.dir_name,"截屏");
        else if(item.getKey().compareToIgnoreCase("Camera")==0)
            holder.setText(R.id.dir_name,"相机");
        else holder.setText(R.id.dir_name,item.getKey());

        holder.setText(R.id.dir_amount,String.format("(%s)",item.getValue().size()));

        if(holder.getAdapterPosition()==mSelectPos)
        {
            holder.setVisible(R.id.img_selected,true);
        }else holder.setVisible(R.id.img_selected,false);

        LinearLayout rootLayout = holder.getView(R.id.root_layout);
        rootLayout.setOnClickListener(v -> {
            if(mSelectPos != holder.getAdapterPosition())
            {
                mSelectPos = holder.getAdapterPosition();
                setOnItemClick(rootLayout,holder.getAdapterPosition());
                notifyDataSetChanged();
            }
        });
    }
}
