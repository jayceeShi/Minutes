//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.example.pku_j.software.yancloud;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ActivityUtil {
    public ActivityUtil() {
    }

    public static void doStartApplicationWithPackageName(Context context, String packagename) {
        try {
            Intent e = context.getPackageManager().getLaunchIntentForPackage(packagename);
            context.startActivity(e);
        } catch (Exception var3) {
            Toast.makeText(context, "没有安装", 1).show();
        }

    }
}
