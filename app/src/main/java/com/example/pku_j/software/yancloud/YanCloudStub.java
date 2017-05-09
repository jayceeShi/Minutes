//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.example.pku_j.software.yancloud;

import android.content.Context;
import android.util.Log;
import com.example.pku_j.software.yancloud.Server;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class YanCloudStub {
    public static final String PATH = "/sdcard/apiminier/";
    private static final String TAG = "YanCloudStub_v12.0.3";
    private static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static Method YanCloudGet;
    private static Method YanCloudSet;
    private static Method YanCloudOnResume;
    private static String NOTSUPPORT = "{\"code\":-1, \"msg\":\"Unsupported Exception\"}";

    public YanCloudStub() {
    }

    public static void init(Context c) {
        try {
            Log.d("YanCloudStub_v12.0.3", "init start:" + c.getPackageName());
            String e = calMD5(c.getPackageResourcePath());
            String dexName = e + ".dex";
            boolean contains = (new File("/sdcard/apiminier/", e + ".dex")).exists();
            Log.d("YanCloudStub_v12.0.3", " contains:" + contains);
            if(contains) {
                loadClass(c, dexName);
            }
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    private static String calMD5(String zipPath) {
        try {
            ZipFile e = new ZipFile(zipPath);
            ZipEntry ze = e.getEntry("META-INF/MANIFEST.MF");
            InputStream in = e.getInputStream(ze);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[4096];

            int numRead;
            while((numRead = in.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }

            e.close();
            return toHexString(md5.digest());
        } catch (Exception var7) {
            var7.printStackTrace();
            return "0000000000000000";
        }
    }

    private static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);

        for(int i = 0; i < b.length; ++i) {
            sb.append(HEX_DIGITS[(b[i] & 240) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 15]);
        }

        return sb.toString();
    }

    private static void loadClass(Context c, String dexName) {
        try {
            String e = "/sdcard/apiminier/" + dexName;
            File fdir = c.getFilesDir();
            Log.d("YanCloudStub_v12.0.3", "LoadClass! dataDir is:" + fdir.getAbsolutePath() + " dexPath:" + e);
            if(!fdir.exists()) {
                fdir.mkdirs();
            }

            File cacheDir = new File(fdir, "app_cache/");
            if(!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            File dir = new File(fdir, "app_dex/");
            if(!dir.exists()) {
                dir.mkdirs();
            }

            File fromFile = new File(e);
            File toFile = new File(dir, "apiminier.dex");
            copyTo(fromFile, toFile);
            DexClassLoader loader = new DexClassLoader(toFile.getAbsolutePath(), cacheDir.getAbsolutePath(), (String)null, DexClassLoader.getSystemClassLoader());
            Class cls = loader.loadClass("cn.edu.pku.apiminier.Installer");
            Method m = cls.getDeclaredMethod("shortInstall", new Class[]{Context.class});
            m.invoke((Object)null, new Object[]{c});
            Class contextService = c.getClassLoader().loadClass("cn.edu.pku.apiminier.YanCloud");
            m = contextService.getMethod("init", new Class[]{Object.class});
            Method[] methods = (Method[])m.invoke((Object)null, new Object[]{c});
            YanCloudGet = methods[0];
            YanCloudSet = methods[1];
            YanCloudOnResume = methods[2];
            Server.main((String[])null);
        } catch (Exception var13) {
            Log.e("YanCloudStub_v12.0.3", "LoadClass Meet Exception.");
            var13.printStackTrace();
        }

    }

    private static void copyTo(File fromFile, File toFile) {
        try {
            Log.d("YanCloudStub_v12.0.3", "CopyTo:" + fromFile.getAbsolutePath() + " --> " + toFile.getAbsolutePath());
            FileOutputStream e = new FileOutputStream(toFile);
            FileInputStream fin = new FileInputStream(fromFile);
            FileChannel inputChannel = fin.getChannel();
            FileChannel outputChannel = e.getChannel();
            outputChannel.transferFrom(inputChannel, 0L, inputChannel.size());
            fin.close();
            e.close();
        } catch (Exception var6) {
            Log.e("YanCloudStub_v12.0.3", "copyTo Meet Exception:" + fromFile.getAbsolutePath() + "  " + toFile.getAbsolutePath());
            var6.printStackTrace();
        }

    }

    public static String get(String pkgName, String method, String args) {
        try {
            return (String)YanCloudGet.invoke((Object)null, new Object[]{pkgName, method, args});
        } catch (Exception var4) {
            return NOTSUPPORT;
        }
    }

    public static String set(String pkgName, String method, String args) {
        try {
            return (String)YanCloudSet.invoke((Object)null, new Object[]{pkgName, method, args});
        } catch (Exception var4) {
            return NOTSUPPORT;
        }
    }

    public static void onResume(Context c) {
        try {
            Log.d("YanCloudStub_v12.0.3", "onResume is called");
            YanCloudOnResume.invoke((Object)null, new Object[]{c});
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }
}
