//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.example.pku_j.software;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.json.JSONArray;
import org.json.JSONObject;

public class ACache {
    public static final int TIME_HOUR = 3600;
    public static final int TIME_DAY = 86400;
    private static final int MAX_SIZE = 50000000;
    private static final int MAX_COUNT = 2147483647;
    private static Map<String, ACache> mInstanceMap = new HashMap();
    private ACache.ACacheManager mCache;

    public static ACache get(Context ctx) {
        return get(ctx, "ACache");
    }

    public static ACache get(Context ctx, String cacheName) {
        File f = new File(ctx.getCacheDir(), cacheName);
        return get(f, 50000000L, 2147483647);
    }

    public static ACache get(File cacheDir) {
        return get(cacheDir, 50000000L, 2147483647);
    }

    public static ACache get(Context ctx, long max_zise, int max_count) {
        File f = new File(ctx.getCacheDir(), "ACache");
        return get(f, max_zise, max_count);
    }

    public static ACache get(File cacheDir, long max_zise, int max_count) {
        ACache manager = (ACache)mInstanceMap.get(cacheDir.getAbsoluteFile() + myPid());
        if(manager == null) {
            manager = new ACache(cacheDir, max_zise, max_count);
            mInstanceMap.put(cacheDir.getAbsolutePath() + myPid(), manager);
        }

        return manager;
    }

    private static String myPid() {
        return "_acache";
    }

    private ACache(File cacheDir, long max_size, int max_count) {
        if(!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new RuntimeException("can\'t make dirs in " + cacheDir.getAbsolutePath());
        } else {
            this.mCache = new ACache.ACacheManager(cacheDir, max_size, max_count);
        }
    }

    public void put(String key, String value) {
        File file = this.mCache.newFile(key);
        BufferedWriter out = null;

        try {
            out = new BufferedWriter(new FileWriter(file), 1024);
            out.write(value);
        } catch (IOException var14) {
            var14.printStackTrace();
        } finally {
            if(out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException var13) {
                    var13.printStackTrace();
                }
            }

            this.mCache.put(file);
        }

    }

    public void put(String key, String value, int saveTime) {
        this.put(key, ACache.Utils.newStringWithDateInfo(saveTime, value));
    }

    public String getAsString(String key) {
        File file = this.mCache.get(key);
        if(!file.exists()) {
            return null;
        } else {
            boolean removeFile = false;
            BufferedReader in = null;

            try {
                in = new BufferedReader(new FileReader(file));

                String e;
                String currentLine;
                for(e = ""; (currentLine = in.readLine()) != null; e = e + currentLine) {
                    ;
                }

                if(ACache.Utils.isDue(e)) {
                    removeFile = true;
                    return null;
                }

                String var8 = ACache.Utils.clearDateInfo(e);
                return var8;
            } catch (IOException var17) {
                var17.printStackTrace();
            } finally {
                if(in != null) {
                    try {
                        in.close();
                    } catch (IOException var16) {
                        var16.printStackTrace();
                    }
                }

                if(removeFile) {
                    this.remove(key);
                }

            }

            return null;
        }
    }

    public void put(String key, JSONObject value) {
        this.put(key, value.toString());
    }

    public void put(String key, JSONObject value, int saveTime) {
        this.put(key, value.toString(), saveTime);
    }

    public JSONObject getAsJSONObject(String key) {
        String JSONString = this.getAsString(key);

        try {
            JSONObject e = new JSONObject(JSONString);
            return e;
        } catch (Exception var4) {
            var4.printStackTrace();
            return null;
        }
    }

    public void put(String key, JSONArray value) {
        this.put(key, value.toString());
    }

    public void put(String key, JSONArray value, int saveTime) {
        this.put(key, value.toString(), saveTime);
    }

    public JSONArray getAsJSONArray(String key) {
        String JSONString = this.getAsString(key);

        try {
            JSONArray e = new JSONArray(JSONString);
            return e;
        } catch (Exception var4) {
            var4.printStackTrace();
            return null;
        }
    }

    public void put(String key, byte[] value) {
        File file = this.mCache.newFile(key);
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(file);
            out.write(value);
        } catch (Exception var14) {
            var14.printStackTrace();
        } finally {
            if(out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException var13) {
                    var13.printStackTrace();
                }
            }

            this.mCache.put(file);
        }

    }

    public void put(String key, byte[] value, int saveTime) {
        this.put(key, ACache.Utils.newByteArrayWithDateInfo(saveTime, value));
    }

    public byte[] getAsBinary(String key) {
        RandomAccessFile RAFile = null;
        boolean removeFile = false;

        try {
            File e = this.mCache.get(key);
            if(e.exists()) {
                RAFile = new RandomAccessFile(e, "r");
                byte[] byteArray = new byte[(int)RAFile.length()];
                RAFile.read(byteArray);
                if(!ACache.Utils.isDue(byteArray)) {
                    byte[] var7 = ACache.Utils.clearDateInfo(byteArray);
                    return var7;
                }

                removeFile = true;
                return null;
            }
        } catch (Exception var17) {
            var17.printStackTrace();
            return null;
        } finally {
            if(RAFile != null) {
                try {
                    RAFile.close();
                } catch (IOException var16) {
                    var16.printStackTrace();
                }
            }

            if(removeFile) {
                this.remove(key);
            }

        }

        return null;
    }

    public void put(String key, Serializable value) {
        this.put(key, (Serializable)value, -1);
    }

    public void put(String key, Serializable value, int saveTime) {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;

        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            byte[] e = baos.toByteArray();
            if(saveTime != -1) {
                this.put(key, e, saveTime);
            } else {
                this.put(key, e);
            }
        } catch (Exception var15) {
            var15.printStackTrace();
        } finally {
            try {
                oos.close();
            } catch (IOException var14) {
                ;
            }

        }

    }

    public Object getAsObject(String key) {
        byte[] data = this.getAsBinary(key);
        if(data != null) {
            ByteArrayInputStream bais = null;
            ObjectInputStream ois = null;

            try {
                bais = new ByteArrayInputStream(data);
                ois = new ObjectInputStream(bais);
                Object e = ois.readObject();
                Object var7 = e;
                return var7;
            } catch (Exception var19) {
                var19.printStackTrace();
            } finally {
                try {
                    if(bais != null) {
                        bais.close();
                    }
                } catch (IOException var18) {
                    var18.printStackTrace();
                }

                try {
                    if(ois != null) {
                        ois.close();
                    }
                } catch (IOException var17) {
                    var17.printStackTrace();
                }

            }

            return null;
        } else {
            return null;
        }
    }

    public void put(String key, Bitmap value) {
        this.put(key, ACache.Utils.Bitmap2Bytes(value));
    }

    public void put(String key, Bitmap value, int saveTime) {
        this.put(key, ACache.Utils.Bitmap2Bytes(value), saveTime);
    }

    public Bitmap getAsBitmap(String key) {
        return this.getAsBinary(key) == null?null:ACache.Utils.Bytes2Bimap(this.getAsBinary(key));
    }

    public void put(String key, Drawable value) {
        this.put(key, ACache.Utils.drawable2Bitmap(value));
    }

    public void put(String key, Drawable value, int saveTime) {
        this.put(key, ACache.Utils.drawable2Bitmap(value), saveTime);
    }

    public Drawable getAsDrawable(String key) {
        return this.getAsBinary(key) == null?null:ACache.Utils.bitmap2Drawable(ACache.Utils.Bytes2Bimap(this.getAsBinary(key)));
    }

    public File file(String key) {
        File f = this.mCache.newFile(key);
        return f.exists()?f:null;
    }

    public boolean remove(String key) {
        return this.mCache.remove(key);
    }

    public void clear() {
        this.mCache.clear();
    }

    public class ACacheManager {
        private final AtomicLong cacheSize;
        private final AtomicInteger cacheCount;
        private final long sizeLimit;
        private final int countLimit;
        private final Map<File, Long> lastUsageDates;
        protected File cacheDir;

        private ACacheManager(File cacheDir, long sizeLimit, int countLimit) {
            this.lastUsageDates = Collections.synchronizedMap(new HashMap());
            this.cacheDir = cacheDir;
            this.sizeLimit = sizeLimit;
            this.countLimit = countLimit;
            this.cacheSize = new AtomicLong();
            this.cacheCount = new AtomicInteger();
            this.calculateCacheSizeAndCacheCount();
        }

        private void calculateCacheSizeAndCacheCount() {
            (new Thread(new Runnable() {
                public void run() {
                    int size = 0;
                    int count = 0;
                    File[] cachedFiles = ACacheManager.this.cacheDir.listFiles();
                    if(cachedFiles != null) {
                        File[] var7 = cachedFiles;
                        int var6 = cachedFiles.length;

                        for(int var5 = 0; var5 < var6; ++var5) {
                            File cachedFile = var7[var5];
                            size = (int)((long)size + ACacheManager.this.calculateSize(cachedFile));
                            ++count;
                            ACacheManager.this.lastUsageDates.put(cachedFile, Long.valueOf(cachedFile.lastModified()));
                        }

                        ACacheManager.this.cacheSize.set((long)size);
                        ACacheManager.this.cacheCount.set(count);
                    }

                }
            })).start();
        }

        private void put(File file) {
            long valueSize;
            for(int curCacheCount = this.cacheCount.get(); curCacheCount + 1 > this.countLimit; curCacheCount = this.cacheCount.addAndGet(-1)) {
                valueSize = this.removeNext();
                this.cacheSize.addAndGet(-valueSize);
            }

            this.cacheCount.addAndGet(1);
            valueSize = this.calculateSize(file);

            long currentTime;
            for(long curCacheSize = this.cacheSize.get(); curCacheSize + valueSize > this.sizeLimit; curCacheSize = this.cacheSize.addAndGet(-currentTime)) {
                currentTime = this.removeNext();
            }

            this.cacheSize.addAndGet(valueSize);
            Long currentTime1 = Long.valueOf(System.currentTimeMillis());
            file.setLastModified(currentTime1.longValue());
            this.lastUsageDates.put(file, currentTime1);
        }

        private File get(String key) {
            File file = this.newFile(key);
            Long currentTime = Long.valueOf(System.currentTimeMillis());
            file.setLastModified(currentTime.longValue());
            this.lastUsageDates.put(file, currentTime);
            return file;
        }

        private File newFile(String key) {
            return new File(this.cacheDir, String.valueOf(key.hashCode()));
        }

        private boolean remove(String key) {
            File image = this.get(key);
            return image.delete();
        }

        private void clear() {
            this.lastUsageDates.clear();
            this.cacheSize.set(0L);
            File[] files = this.cacheDir.listFiles();
            if(files != null) {
                File[] var5 = files;
                int var4 = files.length;

                for(int var3 = 0; var3 < var4; ++var3) {
                    File f = var5[var3];
                    f.delete();
                }
            }

        }

        private long removeNext() {
            if(this.lastUsageDates.isEmpty()) {
                return 0L;
            } else {
                Long oldestUsage = null;
                File mostLongUsedFile = null;
                Set entries = this.lastUsageDates.entrySet();
                Map fileSize = this.lastUsageDates;
                synchronized(this.lastUsageDates) {
                    Iterator var6 = entries.iterator();

                    while(true) {
                        if(!var6.hasNext()) {
                            break;
                        }

                        Entry entry = (Entry)var6.next();
                        if(mostLongUsedFile == null) {
                            mostLongUsedFile = (File)entry.getKey();
                            oldestUsage = (Long)entry.getValue();
                        } else {
                            Long lastValueUsage = (Long)entry.getValue();
                            if(lastValueUsage.longValue() < oldestUsage.longValue()) {
                                oldestUsage = lastValueUsage;
                                mostLongUsedFile = (File)entry.getKey();
                            }
                        }
                    }
                }

                long fileSize1 = this.calculateSize(mostLongUsedFile);
                if(mostLongUsedFile.delete()) {
                    this.lastUsageDates.remove(mostLongUsedFile);
                }

                return fileSize1;
            }
        }

        private long calculateSize(File file) {
            return file.length();
        }
    }

    private static class Utils {
        private static final char mSeparator = ' ';

        private Utils() {
        }

        private static boolean isDue(String str) {
            return isDue(str.getBytes());
        }

        private static boolean isDue(byte[] data) {
            String[] strs = getDateInfoFromDate(data);
            if(strs != null && strs.length == 2) {
                String saveTimeStr;
                for(saveTimeStr = strs[0]; saveTimeStr.startsWith("0"); saveTimeStr = saveTimeStr.substring(1, saveTimeStr.length())) {
                    ;
                }

                long saveTime = Long.valueOf(saveTimeStr).longValue();
                long deleteAfter = Long.valueOf(strs[1]).longValue();
                if(System.currentTimeMillis() > saveTime + deleteAfter * 1000L) {
                    return true;
                }
            }

            return false;
        }

        private static String newStringWithDateInfo(int second, String strInfo) {
            return createDateInfo(second) + strInfo;
        }

        private static byte[] newByteArrayWithDateInfo(int second, byte[] data2) {
            byte[] data1 = createDateInfo(second).getBytes();
            byte[] retdata = new byte[data1.length + data2.length];
            System.arraycopy(data1, 0, retdata, 0, data1.length);
            System.arraycopy(data2, 0, retdata, data1.length, data2.length);
            return retdata;
        }

        private static String clearDateInfo(String strInfo) {
            if(strInfo != null && hasDateInfo(strInfo.getBytes())) {
                strInfo = strInfo.substring(strInfo.indexOf(32) + 1, strInfo.length());
            }

            return strInfo;
        }

        private static byte[] clearDateInfo(byte[] data) {
            return hasDateInfo(data)?copyOfRange(data, indexOf(data, ' ') + 1, data.length):data;
        }

        private static boolean hasDateInfo(byte[] data) {
            return data != null && data.length > 15 && data[13] == 45 && indexOf(data, ' ') > 14;
        }

        private static String[] getDateInfoFromDate(byte[] data) {
            if(hasDateInfo(data)) {
                String saveDate = new String(copyOfRange(data, 0, 13));
                String deleteAfter = new String(copyOfRange(data, 14, indexOf(data, ' ')));
                return new String[]{saveDate, deleteAfter};
            } else {
                return null;
            }
        }

        private static int indexOf(byte[] data, char c) {
            for(int i = 0; i < data.length; ++i) {
                if(data[i] == c) {
                    return i;
                }
            }

            return -1;
        }

        private static byte[] copyOfRange(byte[] original, int from, int to) {
            int newLength = to - from;
            if(newLength < 0) {
                throw new IllegalArgumentException(from + " > " + to);
            } else {
                byte[] copy = new byte[newLength];
                System.arraycopy(original, from, copy, 0, Math.min(original.length - from, newLength));
                return copy;
            }
        }

        private static String createDateInfo(int second) {
            String currentTime;
            for(currentTime = String.valueOf(System.currentTimeMillis()); currentTime.length() < 13; currentTime = "0" + currentTime) {
                ;
            }

            return currentTime + "-" + second + ' ';
        }

        private static byte[] Bitmap2Bytes(Bitmap bm) {
            if(bm == null) {
                return null;
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bm.compress(CompressFormat.PNG, 100, baos);
                return baos.toByteArray();
            }
        }

        private static Bitmap Bytes2Bimap(byte[] b) {
            return b.length == 0?null:BitmapFactory.decodeByteArray(b, 0, b.length);
        }

        private static Bitmap drawable2Bitmap(Drawable drawable) {
            if(drawable == null) {
                return null;
            } else {
                int w = drawable.getIntrinsicWidth();
                int h = drawable.getIntrinsicHeight();
                Config config = drawable.getOpacity() != -1?Config.ARGB_8888:Config.RGB_565;
                Bitmap bitmap = Bitmap.createBitmap(w, h, config);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, w, h);
                drawable.draw(canvas);
                return bitmap;
            }
        }

        private static Drawable bitmap2Drawable(Bitmap bm) {
            return bm == null?null:new BitmapDrawable(bm);
        }
    }
}
