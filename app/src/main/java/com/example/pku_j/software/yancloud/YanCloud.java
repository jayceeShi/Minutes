//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.example.pku_j.software.yancloud;

import android.content.Context;

import com.example.pku_j.software.yancloud.YanCloudSet;
import com.example.pku_j.software.yancloud.YanCloudGet;

public class YanCloud {
    YanCloudGet get;
    YanCloudSet set;

    private YanCloud() {
        this.get = null;
        this.set = null;
    }

    public static YanCloud fromGet(String ip, int port) {
        YanCloud ret = new YanCloud();
        ret.get = new YanCloudGet(ip, port);
        return ret;
    }

    public static YanCloud fromSet(Context c) {
        YanCloud ret = new YanCloud();
        ret.set = YanCloudSet.getInstance(c);
        return ret;
    }

    public void set(String pkgName, String type, String params) {
        if(this.set == null) {
            throw new IllegalStateException("Build without configSet");
        } else {
            this.set.set(pkgName, type, params);
        }
    }

    public String get(String pkgName, String type, String params) {
        if(this.get == null) {
            throw new IllegalStateException("Build without configGet");
        } else {
            return this.get.get(pkgName, type, params);
        }
    }

    public static class Builder {
        YanCloud ret = new YanCloud();

        public Builder() {
        }

        public YanCloud.Builder configSet(Context c) {
            this.ret.set = YanCloudSet.getInstance(c);
            return this;
        }

        public YanCloud.Builder configGetByIP(String ip, int port) {
            this.ret.get = new YanCloudGet(ip, port);
            return this;
        }

        public YanCloud build() {
            return this.ret;
        }
    }
}
