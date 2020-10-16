package com.newasia.imagegraber;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.newasia.imagegraberlib.Activity.IMGCameraActivity;
import com.newasia.imagegraberlib.Activity.IMGEditActivity;
import com.newasia.imagegraberlib.Activity.IMGGalleryActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, IMGCameraActivity.class);
        startActivityForResult(intent, IMGCameraActivity.CAMERA_ACTIVITY_OPEN_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMGCameraActivity.CAMERA_ACTIVITY_OPEN_CODE && resultCode==IMGCameraActivity.CAMERA_ACTIVITY_RESULT_CODE)
        {
            Uri uri = data.getParcelableExtra(IMGCameraActivity.CAMERA_ACTIVITY_RESULT_PARAM);
            Log.e("test",uri.toString());
        }
    }

    //    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode==IMGGalleryActivity.REQ_GET_IAMGE_OPEN && requestCode==IMGGalleryActivity.REQ_GET_IAMGE_RESULT)
//        {
//            ArrayList<Uri> list = data.getParcelableArrayListExtra("result");
//            for(Uri uri:list)
//            {
//                Log.e("test",uri.toString());
//            }
//        }
//    }
}