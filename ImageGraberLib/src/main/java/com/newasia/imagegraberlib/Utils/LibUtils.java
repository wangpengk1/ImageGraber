package com.newasia.imagegraberlib.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import androidx.core.app.ActivityCompat;

public class LibUtils
{
    public static boolean isPermissionGranted(Context context, String permission) {
        return checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isPermissionsGranted(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static int checkSelfPermission(Context context, String permission) {
        return ActivityCompat.checkSelfPermission(context, permission);
    }
}
