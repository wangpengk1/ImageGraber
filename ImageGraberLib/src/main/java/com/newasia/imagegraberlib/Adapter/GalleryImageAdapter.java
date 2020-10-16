package com.newasia.imagegraberlib.Adapter;

import android.net.Uri;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.newasia.imagegraberlib.Bean.IMGImageViewModel;
import com.newasia.imagegraberlib.Core.util.UriUtils;
import com.newasia.imagegraberlib.R;
import com.newasia.imagegraberlib.View.NumberCheckView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GalleryImageAdapter extends BaseQuickAdapter<IMGImageViewModel, BaseViewHolder> {

    public interface OnSelectedImages
    {
        void onSelect(ArrayList<Uri> selectList);
    }


    private ArrayList<Uri> mSelectedList = new ArrayList<>();
    private OnSelectedImages mSelectListener;
    private int mSelectMax = 1;


    public void setSelectItemListener(OnSelectedImages listener)
    {
        mSelectListener = listener;
    }

    public GalleryImageAdapter(int selectMax) {
        super(R.layout.grallery_image_item);
        mSelectMax = selectMax;
    }



    public ArrayList<Uri>  getSelectImages()
    {
        return mSelectedList;
    }

    public void setSelectedList(ArrayList<Uri> list)
    {
        mSelectedList = list;
        notifyDataSetChanged();
    }

    public void setModels(List<IMGImageViewModel> models)
    {
        getData().clear();
        addData(models);
        notifyDataSetChanged();
    }

    @Override
    protected void convert(@NotNull BaseViewHolder holder, IMGImageViewModel item)
    {
        ImageView imageView  = holder.getView(R.id.sdv_image);

        Glide.with(imageView).load(item.getUri())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(false)
                .into(imageView);

        NumberCheckView numberCheckView = holder.getView(R.id.cb_box);
        if(isSelected(item.getUri()))
        {
            numberCheckView.setNumber(indexOfSelected(item.getUri())+1);
        }
        else numberCheckView.setNumber(-1);

        numberCheckView.setOnClickListener(v -> {

            if(isSelected(item.getUri()))
            {
                removeSelected(item.getUri());
            }
            else
            {
                if(mSelectedList.size()>=mSelectMax) Toast.makeText(getContext(),String.format("你最多可以选择%s个图片",mSelectMax),Toast.LENGTH_SHORT).show();
                else  mSelectedList.add(item.getUri());
            }

            if(mSelectListener!=null)  mSelectListener.onSelect(mSelectedList);

            notifyDataSetChanged();
        });
    }


    private boolean isSelected(Uri toTest)
    {
        return indexOfSelected(toTest) != -1;
    }

    private void removeSelected(Uri toRemove)
    {
        int index = indexOfSelected(toRemove);
        if(index!=-1) mSelectedList.remove(index);
    }


    private int indexOfSelected(Uri uri)
    {
        int index = -1;
        for(int i=0;i<mSelectedList.size();++i)
        {
            if(mSelectedList.get(i).hashCode()==uri.hashCode())
            {
                index = i;
                break;
            }
        }

        return index;
    }

}
