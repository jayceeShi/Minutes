//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.example.pku_j.software;

import java.io.File;
import java.util.List;

public class Utils {
    public Utils() {
    }

    public static ACache getACache() {
        String path = "/sdcard/ias/";
        File tmp = new File(path);
        if(!tmp.exists()) {
            tmp.mkdir();
        }

        ACache aCache = ACache.get(new File(path, "ias"));
        return aCache;
    }

    public static boolean isEmpty(List l) {
        return l == null || l.size() == 0;
    }
}
