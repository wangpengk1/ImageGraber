package com.newasia.imagegraberlib.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;


import com.newasia.imagegraberlib.Core.util.UriUtils;
import com.newasia.imagegraberlib.R;
import com.newasia.imagegraberlib.Utils.StatusBarUtils;
import com.newasia.imagegraberlib.databinding.ActivityCameraLayoutBinding;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import static com.wonderkiln.camerakit.CameraKit.Constants.FLASH_AUTO;
import static com.wonderkiln.camerakit.CameraKit.Constants.FLASH_OFF;
import static com.wonderkiln.camerakit.CameraKit.Constants.FLASH_ON;

public class IMGCameraActivity extends AppCompatActivity
{
    private ActivityCameraLayoutBinding mBinding;
    private Uri mImageUri;

    public static final int CAMERA_ACTIVITY_OPEN_CODE = 1547;
    public static final int CAMERA_ACTIVITY_RESULT_CODE = 1548;

    public static final String CAMERA_ACTIVITY_RESULT_PARAM = "URI";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getActionBar()!=null) getSupportActionBar().hide();
        StatusBarUtils.setStatusBarTranslucent(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera_layout);
        mBinding.rootLayout.setPadding(0, StatusBarUtils.getStatusBarHeight(this),0,0);

        initView();
    }


    private void initView()
    {
        switchFlashIcon(mBinding.cameraView.getFlash());

        mBinding.cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                Bitmap bitmap = cameraKitImage.getBitmap();
                mBinding.cameraView.post(()->{
                    updatePhoto(bitmap);
                });
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        mBinding.ivFlashLight.setOnClickListener(v -> {
            switchFlashIcon(mBinding.cameraView.toggleFlash());
        });

        mBinding.ivFace.setOnClickListener(v -> {
            mBinding.cameraView.toggleFacing();
        });

        mBinding.btnTakePhoto.setOnClickListener(v -> {
            new Thread(()->{
                mBinding.cameraView.captureImage();
            }).start();
        });
    }



    private void updatePhoto(Bitmap bitmap)
    {
        String path = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)+ "/"+UUID.randomUUID().toString() + ".jpg";
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fout);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        Intent intent = new Intent(this,IMGEditActivity.class);
        mImageUri = Uri.fromFile(new File(path));
        intent.putExtra(IMGEditActivity.EXTRA_IMAGE_URI,mImageUri);
        intent.putExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH, UriUtils.getFilePathByUri(this,mImageUri));
        startActivityForResult(intent,IMGEditActivity.REQ_IMAGE_EDIT_OPEN_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMGEditActivity.REQ_IMAGE_EDIT_OPEN_CODE && resultCode==IMGEditActivity.REQ_IMAGE_EDIT_OPEN_RESULT_CODE && mImageUri!=null)
        {
            Intent result = new Intent();
            result.putExtra(CAMERA_ACTIVITY_RESULT_PARAM,mImageUri);
            setResult(CAMERA_ACTIVITY_RESULT_CODE,result);
            finish();
        }
    }




    private void switchFlashIcon(int flash) {
        switch (flash) {
            case FLASH_OFF:
                mBinding.ivFlashLight.setImageResource(R.drawable.ic_flash_off);
                break;
            case FLASH_ON:
                mBinding.ivFlashLight.setImageResource(R.drawable.ic_flash_on);
                break;
            case FLASH_AUTO:
                mBinding.ivFlashLight.setImageResource(R.drawable.ic_flash_auto);
                break;
            default:
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mBinding.cameraView.start();
    }

    @Override
    public void onPause() {
        mBinding.cameraView.stop();
        super.onPause();
    }
}
