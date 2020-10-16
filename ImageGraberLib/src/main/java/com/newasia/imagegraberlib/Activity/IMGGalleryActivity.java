package com.newasia.imagegraberlib.Activity;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;

import com.newasia.imagegraberlib.Adapter.GalleryDirAdapter;
import com.newasia.imagegraberlib.Adapter.GalleryImageAdapter;
import com.newasia.imagegraberlib.Bean.IMGImageViewModel;
import com.newasia.imagegraberlib.FileTool.IMGScanTask;
import com.newasia.imagegraberlib.FileTool.IMGScanner;
import com.newasia.imagegraberlib.R;
import com.newasia.imagegraberlib.Utils.LibUtils;
import com.newasia.imagegraberlib.Utils.StatusBarUtils;
import com.newasia.imagegraberlib.databinding.ActivityImageGalleryBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class IMGGalleryActivity extends AppCompatActivity
{
    public static final int REQ_GET_IAMGE_OPEN = 1258;
    public static final int REQ_GET_IAMGE_RESULT = 1258;

    private static final int REQ_PERMISSION = 1;

    /////////////////////////////////////////////

    private ActivityImageGalleryBinding mBinding;
    private int mListHeight = 0;
    private Map<String, List<IMGImageViewModel>> mImages;
    private GalleryImageAdapter mImageAdapter;
    private GalleryDirAdapter mDirAdapter = new GalleryDirAdapter();

    private int mSelectMax = 9;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getActionBar()!=null) getSupportActionBar().hide();
        StatusBarUtils.setStatusBarTranslucent(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_image_gallery);
        mBinding.rootLayout.setPadding(0, StatusBarUtils.getStatusBarHeight(this),0,0);

       mSelectMax = getIntent().getIntExtra("select_max",9);

       mImageAdapter = new GalleryImageAdapter(mSelectMax);
       mBinding.imageList.setAdapter(mImageAdapter);
       mBinding.dirList.setAdapter(mDirAdapter);

       initListener();
    }


    private void initListener()
    {
        //关闭按钮
        mBinding.imageCancel.setOnClickListener(v -> {finish();});

        //文件夹选择列表收齐和展开动画
        mBinding.selectDriArea.setOnClickListener(v -> {
            animationDirList();
        });


        //选择图片文件夹的监听
        mDirAdapter.setOnItemClickListener((adapter, view, position) -> {
            Map.Entry<String, List<IMGImageViewModel>> dirInfo = mDirAdapter.getItem(position);
            mBinding.imgDriText.setText(dirInfo.getKey());
            onQuicklyImages(dirInfo.getValue());
            animationDirList();
        });

        //选择图片的监听
        mImageAdapter.setSelectItemListener(selectList -> {
            if(selectList!=null && selectList.size()>0)
            {
                mBinding.btnSend.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.send_btn_bg,null));
                mBinding.btnSend.setText(String.format("发送(%s/%s)",selectList.size(),mSelectMax));
                mBinding.actionPreview.setText(String.format("预览(%s)",selectList.size()));
                mBinding.btnSend.setEnabled(true);
            }
            else
            {
                mBinding.btnSend.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.send_btn_disable,null));
                mBinding.btnSend.setText("发送");
                mBinding.actionPreview.setText("预览");
                mBinding.btnSend.setEnabled(false);
            }
        });


        //点击开始预览
        mImageAdapter.setOnItemClickListener((adapter, view, position) -> {
            Map.Entry<String, List<IMGImageViewModel>> data = mDirAdapter.getSelectData();
            if(data!=null)
            {
                ArrayList<Uri> list = new ArrayList<>();
                for(IMGImageViewModel model:data.getValue())
                {
                    list.add(model.getUri());
                }
                Intent intent = new Intent(this,IMGPreviewActivity.class);
                intent.putParcelableArrayListExtra(IMGPreviewActivity.START_PARAM_IMAGES,list);
                intent.putParcelableArrayListExtra(IMGPreviewActivity.START_PARAM_SELECT_LIST,mImageAdapter.getSelectImages());
                intent.putExtra(IMGPreviewActivity.START_PARAM_MAX_SELECT,mSelectMax);
                intent.putExtra(IMGPreviewActivity.START_PARAM_INDEX,position);
                startActivityForResult(intent,IMGPreviewActivity.REQ_PREVIEW_OPEN_CODE);
            }
        });


        //选择完成监听
        mBinding.btnSend.setOnClickListener(v -> {
            retrunActivityResult();
        });


        if (!LibUtils.isPermissionGranted(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_PERMISSION);
        }else {new IMGScanTask(this).execute();}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMGPreviewActivity.REQ_PREVIEW_OPEN_CODE && resultCode==IMGPreviewActivity.REQ_PREVIEW_RESULT_CODE)
        {

            ArrayList<Uri> selectList = data.getParcelableArrayListExtra(IMGPreviewActivity.RESULT_PARAM_LIST);
            int type = data.getIntExtra(IMGPreviewActivity.RESULT_PARAM_TYPE,0);
            mImageAdapter.setSelectedList(selectList);
            if(type==1)  retrunActivityResult();
        }
    }

    public void onImages(Map<String, List<IMGImageViewModel>> images) {
        mImages = images;
        if (images != null) {
            mImageAdapter.setModels(images.get(IMGScanner.ALL_IMAGES));
            mDirAdapter.addData(images.entrySet());
            mDirAdapter.notifyDataSetChanged();
        }
    }

    public void onQuicklyImages(List<IMGImageViewModel> images) {
        mImageAdapter.setModels(images);
    }


    private void retrunActivityResult()
    {
        Intent returnResult = new Intent();
        returnResult.putParcelableArrayListExtra("result",mImageAdapter.getSelectImages());
        setResult(REQ_GET_IAMGE_RESULT,returnResult);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("+++++++", Arrays.toString(grantResults));
        if (!LibUtils.isPermissionsGranted(this, permissions)) {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("请授权存储权限")
                    .setPositiveButton("去授权", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(Uri.fromParts("package", getApplicationContext().getPackageName(), null)));
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }else { new IMGScanTask(this).execute();}
    }




    private void animationDirList()
    {
        ValueAnimator animator1 = null;
        ValueAnimator animator2 = null;
        AnimatorSet animatorSet = new AnimatorSet();

        if(mBinding.dirList.getVisibility()!=View.VISIBLE)
        {

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)mBinding.dirList.getLayoutParams();
            params.height = 1;
            mBinding.dirList.setLayoutParams(params);
            mBinding.dirList.setVisibility(View.VISIBLE);

            animator1 = ValueAnimator.ofInt(1,mListHeight);
            animator2 = ValueAnimator.ofInt(0,180);
        }
        else
        {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)mBinding.dirList.getLayoutParams();
            params.height = mListHeight;
            mBinding.dirList.setLayoutParams(params);

            animator1 = ValueAnimator.ofInt(mListHeight,0);
            animator2 = ValueAnimator.ofInt(180,360);
        }



        animatorSet.setDuration(500);
        animator1.addUpdateListener(animation -> {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)mBinding.dirList.getLayoutParams();
            params.height = (Integer) animation.getAnimatedValue();
            mBinding.dirList.setLayoutParams(params);
            if((Integer) animation.getAnimatedValue()==0)
            {
                mBinding.dirList.setVisibility(View.GONE);
            }
        });
        animator2.addUpdateListener(animation -> {
            mBinding.downImageArea.setRotation(((int)animation.getAnimatedValue()));
        });
        animatorSet.playTogether(animator1,animator2);
        animatorSet.start();

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mListHeight = mBinding.imageList.getHeight();
    }

}
