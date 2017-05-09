//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.example.pku_j.software.yancloud;

import android.content.Context;

public class YanCloudSet {
    static YanCloudSet instance;
    ACache aCache = Utils.getACache();
    Context applicationContext;

    public YanCloudSet(Context context) {
        this.applicationContext = context;
    }

    public static YanCloudSet getInstance(Context applicationContext) {
        Class var1 = YanCloudSet.class;
        synchronized(YanCloudSet.class) {
            if(instance == null) {
                instance = new YanCloudSet(applicationContext);
            }
        }

        return instance;
    }

    public void set(String package_name, String type, String params) {
        this.aCache.put("now", "0");
        this.aCache.put("onTask", "yes");
        this.aCache.put("deep_link_type", type);
        this.aCache.put("params", params);
        this.aCache.put("preClass", "");
        ActivityUtil.doStartApplicationWithPackageName(this.applicationContext, package_name);
    }
}
