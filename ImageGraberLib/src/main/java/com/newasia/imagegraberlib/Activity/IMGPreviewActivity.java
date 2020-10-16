package com.newasia.imagegraberlib.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.newasia.imagegraberlib.Adapter.ImagePreViewAdapter;
import com.newasia.imagegraberlib.Core.util.UriUtils;
import com.newasia.imagegraberlib.R;
import com.newasia.imagegraberlib.Utils.StatusBarUtils;
import com.newasia.imagegraberlib.databinding.ActivityImgPreviewLayoutBinding;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IMGPreviewActivity extends AppCompatActivity
{
    public static final int REQ_PREVIEW_OPEN_CODE =  3251;
    public static final int REQ_PREVIEW_RESULT_CODE =  3252;

    public static final String START_PARAM_IMAGES =  "images";
    public static final String START_PARAM_SELECT_LIST =  "select_list";
    public static final String START_PARAM_INDEX =  "index";
    public static final String START_PARAM_MAX_SELECT =  "max";
    public static final String RESULT_PARAM_LIST =  "result_list";
    public static final String RESULT_PARAM_TYPE =  "result_type";

    private ActivityImgPreviewLayoutBinding mBinding;
    private ImagePreViewAdapter mPreAdapter;
    private ImageShowAdapter mSelectAdapter;
    private ArrayList<Uri> mImageList = new ArrayList<>();
    private ArrayList<Uri> mSelectList = new ArrayList<>();
    private int mPreIndex = -1;
    private int mSelectMax = 9;

    private File mEditFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        StatusBarUtils.setStatusBarTranslucent(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_img_preview_layout);
        mBinding.rootLayout.setPadding(0, StatusBarUtils.getStatusBarHeight(this),0,0);
        mSelectAdapter = new ImageShowAdapter(R.layout.select_img_item_layout);
        mBinding.selectedList.setAdapter(mSelectAdapter);

        if(getIntent().getParcelableArrayListExtra(START_PARAM_IMAGES)!= null)
        {
            mImageList = getIntent().getParcelableArrayListExtra(START_PARAM_IMAGES);
            mSelectList = getIntent().getParcelableArrayListExtra(START_PARAM_SELECT_LIST);
            mPreIndex = getIntent().getIntExtra(START_PARAM_INDEX,-1);
            mSelectMax = getIntent().getIntExtra(START_PARAM_MAX_SELECT,9);
            if(mSelectList==null) mSelectList = new ArrayList<>();
            if(mImageList!=null && mImageList.size()>0)
            {
                mPreAdapter = new ImagePreViewAdapter(this,mImageList);
                mBinding.preImgList.setAdapter(mPreAdapter);
            }

            if(mSelectList.size()>0)
            {
                mSelectAdapter.addData(mSelectList);
                mSelectAdapter.notifyDataSetChanged();
            }

            if(mPreIndex>=0 && mPreIndex<mImageList.size())
            {
                mBinding.preImgList.setCurrentItem(mPreIndex);
                onChangePreImage();
            }


            //切换预览的图片监听
            mBinding.preImgList.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
                @Override
                public void onPageSelected(int position) {
                    onChangePreImage();
                }
                @Override
                public void onPageScrollStateChanged(int state) { }
            });


            //CheckBos监听
            mBinding.checkbox.setOnClickListener(v -> {
                if(mBinding.checkbox.isChecked())
                {
                    //如果CheckBox选中  检查选择是否满了，如果未满加到选择列表
                    if(mSelectAdapter.getData().size()>=mSelectMax)
                    {
                        Toast.makeText(this,String.format("你最多可以选择%s个图片",mSelectMax),Toast.LENGTH_SHORT).show();
                        mBinding.checkbox.setChecked(false);
                    }
                    else
                    {
                        mSelectAdapter.addData(mImageList.get(mBinding.preImgList.getCurrentItem()));
                        mSelectAdapter.setSelectPos(mSelectAdapter.getItemCount()-1);
                        mBinding.selectedList.scrollToPosition(mSelectAdapter.getItemCount()-1);
                    }
                }
                else  //如果CheckBox取消选中，则从选择列表移除该图片
                {
                    mSelectAdapter.removeImage(mImageList.get(mBinding.preImgList.getCurrentItem()));
                }
                updateSendButton();
            });


            //已选择列表中的Item点击事件  //检查当前预览的文件内是否包含，如果包含切换到该图片的预览
            mSelectAdapter.setOnItemClickListener((adapter, view, position) -> {
                Uri uri = mSelectAdapter.getItem(position);
                for(int i=0;i<mImageList.size();++i)
                {
                    if(mImageList.get(i).hashCode()==uri.hashCode())
                    {
                        mBinding.preImgList.setCurrentItem(i);
                        mSelectAdapter.setSelectPos(position);
                    }
                }
            });

            //点击跳转到编辑界面
            mBinding.textEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this,IMGEditActivity.class);
                Uri curUri = mImageList.get(mBinding.preImgList.getCurrentItem());
                Log.e("test",curUri.toString());
                Log.e("test",UriUtils.getFilePathByUri(this,curUri));
                intent.putExtra(IMGEditActivity.EXTRA_IMAGE_URI,curUri);
                intent.putExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH, UriUtils.getFilePathByUri(this,curUri));
                startActivityForResult(intent,IMGEditActivity.REQ_IMAGE_EDIT_OPEN_CODE);
            });


            mBinding.btnSend.setOnClickListener(v -> {
                returnSelectedResult(1);
                finish();
            });

            mBinding.imageBack.setOnClickListener(v -> {
                returnSelectedResult(0);
                finish();
            });

            updateSendButton();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMGEditActivity.REQ_IMAGE_EDIT_OPEN_CODE && resultCode==IMGEditActivity.REQ_IMAGE_EDIT_OPEN_RESULT_CODE)
        {
            Glide.get(this).clearMemory();
            int pos = mBinding.preImgList.getCurrentItem();
            mBinding.preImgList.setAdapter(mPreAdapter);
            mBinding.preImgList.setCurrentItem(pos);
            mBinding.selectedList.setAdapter(mSelectAdapter);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            returnSelectedResult(0);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void onChangePreImage()
    {
        mBinding.textCount.setText(String.format("%s/%s",mBinding.preImgList.getCurrentItem(),mImageList.size()));
        boolean bFound = mSelectAdapter.setSelectedUri(mImageList.get(mBinding.preImgList.getCurrentItem()));
        if(bFound)
        {
            mBinding.checkbox.setChecked(true);
            mBinding.selectedList.scrollToPosition(mSelectAdapter.getSelectedPos());
        }
        else mBinding.checkbox.setChecked(false);
    }


    private void updateSendButton()
    {
        int count = mSelectAdapter.getItemCount();
        if(count>0)
        {
            if(mBinding.selectedList.getVisibility()!= View.VISIBLE) mBinding.selectedList.setVisibility(View.VISIBLE);
            mBinding.btnSend.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.send_btn_bg,null));
            mBinding.btnSend.setText(String.format("发送(%s/%s)",count,mSelectMax));
            mBinding.btnSend.setEnabled(true);
        }
        else
        {
            mBinding.selectedList.setVisibility(View.GONE);
            mBinding.btnSend.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.send_btn_disable,null));
            mBinding.btnSend.setText("发送");
            mBinding.btnSend.setEnabled(false);
        }
    }

    private void returnSelectedResult(int type)
    {
        Intent intent = new Intent();
        List<Uri> list = mSelectAdapter.getData();
        mSelectList.clear();
        for(Uri uri:list)
        {
            mSelectList.add(uri);
        }
        intent.putExtra(RESULT_PARAM_TYPE,type);
        intent.putParcelableArrayListExtra(RESULT_PARAM_LIST,mSelectList);
        setResult(REQ_PREVIEW_RESULT_CODE,intent);
    }



    private class ImageShowAdapter extends BaseQuickAdapter<Uri, BaseViewHolder>
    {
        public ImageShowAdapter(int layoutResId) {
            super(layoutResId);
        }
        private int selectedPos = -1;

        public void setSelectPos(int pos)
        {
            selectedPos = pos;
            notifyDataSetChanged();
        }

        public int getSelectedPos()
        {
            return selectedPos;
        }


        public void removeImage(Uri uri)
        {
            List<Uri> list = getData();
            if(list.size()<=0) return;
            for(int i=0;i<list.size();++i)
            {
                if(list.get(i).hashCode()==uri.hashCode())
                {
                    selectedPos = -1;
                    removeAt(i);
                    break;
                }
            }
        }

        //如果正在预览的图片包含在已选择的图片中，选中它
        public boolean setSelectedUri(Uri uri)
        {
            List<Uri> list = getData();
            int pos = 0;
            for(Uri u:list)
            {
                if(u.hashCode()==uri.hashCode())
                {
                    selectedPos = pos;
                    notifyDataSetChanged();
                    break;
                }
                ++pos;
            }

            //返回已选择的中 有没有包含正在预览的图片
            if(pos>=list.size()) return false;
            else return true;
        }

        @Override
        protected void convert(@NotNull BaseViewHolder holder, Uri uri)
        {
            LinearLayout rootLayout = holder.getView(R.id.root_layout);
            if(holder.getAdapterPosition()==selectedPos)
            {
                rootLayout.setBackgroundResource(R.drawable.green_border_bg);
            }else rootLayout.setBackgroundResource(R.drawable.empty_bg);
            ImageView imageView = holder.getView(R.id.image_item);
            Glide.with(imageView).load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(false)
                    .signature(new ObjectKey(System.currentTimeMillis()))
                    .into(imageView);
        }

    }

}
