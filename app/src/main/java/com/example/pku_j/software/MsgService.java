package com.example.pku_j.software;

/**
 * Created by pku_j on 2017/3/30.
 */

import android.app.ListActivity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.*;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.yancloud.android.reflection.get.YanCloudGet;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;
import java.util.regex.Matcher;

import static android.os.SystemClock.sleep;

public class MsgService extends Service {

    private int progress = 0;
    private int time = 0;

    private String url = null;

    private Thread _backThread = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        progress = 0;
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        BroadcastRec broadcastReceiver = new BroadcastRec();
        registerReceiver(broadcastReceiver, filter);

        // One-time initializations

        if (_backThread == null || !_backThread.isAlive()) {
            Log.v("trace~","Start background!");
            _backThread = new Thread(new Runnable() {
                @Override
                public void run() {
                while (true) {
                    doBackground();
                    sleep(10 * 1000);
                }
                }
            });
            _backThread.start();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public static YanCloudGet myFromPackageName(String ip, int maxPort, String[] pkgNames) {
        for (int port = 1716; port < maxPort; ++port) {
            try {
                YanCloudGet ret = new YanCloudGet(ip, port);
                String processName = ret.get("APIPorter", "getProcessName", (String)null);
                for (String pkgName: pkgNames) {
                    if (processName.equals(pkgName)) {
                        for (int i = 0; i < pkgNames.length; ++i) {
                            if (pkgNames[i] != pkgName) pkgNames[i] = null;
                        }
                        return ret;
                    }
                }
            } catch (Exception var6) {
                var6.printStackTrace();
            }
        }
        return null;
    }


    public ArrayList<Recommendation> Recommendations = new ArrayList<Recommendation>();


    private void doBackground()
    {
        Log.v("trace~","doBackground!");

        // Get Current IP
        WifiManager wifiMan = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiMan.getConnectionInfo();
        int ipAddress = info.getIpAddress();
        String ip = "";
        if (ipAddress == 0) {
            Log.e("trace~", "Not connecting to wifi! do nothing!");
            return;
        }
        else {
            ip = ((ipAddress & 0xFF) + "." + (ipAddress >> 8 & 0xFF) + "." + (ipAddress >> 16 & 0xFF) + "." + (ipAddress >> 24 & 0xFF));
            Log.e("trace~", String.format("Wifi IP: %s", ip));
        }


        try {
            /*
            //final String PKG_TAOBAO = "com.taobao.taobao";
            final String PKG_TAOBAO = "cn.kuwo.player";

            String[] pkgs = new String[] { PKG_TAOBAO };
            YanCloudGet api = myFromPackageName(ip, 1800, pkgs);
            if (api != null) {
                String result = api.get("comm", "getMyFavor", "{\"offset\":\"0\"}");
                Log.v("trace~", result);
            }
            */

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection conn = (Connection) DriverManager.getConnection("jdbc:mysql://minutes.catchyrime.com:3306/minutes","minutes","minutes");
            // 连接URL为: jdbc:mysql//服务器地址/数据库名 后面的2个参数分别是登陆用户名和密码
            Log.v("trace~", "MySQL connected");

            ArrayList<Recommendation> recommendations = new ArrayList<>();

            Statement st = (Statement)conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM recommendation");
            while (rs.next()) {

                Recommendation rec = new Recommendation();
                rec.DeepLink = rs.getString("deeplink");
                rec.Source = rs.getString("source");
                rec.Title = rs.getString("title");
                rec.Period = rs.getInt("period");
                rec.DateTime = rs.getDate("datetime");
                rec.Abstract = rs.getString("abstract");
                rec.ThumbnailURL = rs.getString("thumbnail");
                rec.DeepLinkSchema = rs.getString("deeplink_schema");
                recommendations.add(rec);

                //Log.v("trace~", rec.DeepLink);
                //Log.v("trace~", rec.Source);
                //Log.v("trace~", rec.Title);
                //Log.v("trace~", Integer.toString(rec.Period));
                //Log.v("trace~", rec.DateTime.toString());
                //Log.v("trace~", rec.Abstract);
                //Log.v("trace~", rec.ThumbnailURL);
                //Log.v("trace~", rec.DeepLinkSchema);
            }

            // Prepare the recommendations
            Log.d("trace~", "Do prepareRecommendations");
            prepareRecommendations(recommendations);

            this.Recommendations = recommendations;

            conn.close();
        }
        catch (Exception ex) {
            Log.e("trace~", ex.getMessage());
        }

        Recommendation rec = getRecommendation(34);
        Log.e("trace~", String.format("Recommend: %s(%d min)", rec.Source, rec.Period));
    }


    private void prepareRecommendations(ArrayList<Recommendation> recommendations)
    {
        /* This is not OK:
           Download so many thumbnails in one thread
        for (Recommendation rec : recommendations) {
            rec.getThumbnail();
        }
        */
    }

    public double getPeriodWeight(int expected, int actual)
    {
        // ASSERT(expected >= actual);
        double rate = (double)(expected - actual) / (double)expected;
        if (rate >= 0.50) return -10000;  // -infinity
        if (rate >= 0.30) return 0;
        return Math.cos(Math.PI * rate / 0.6);
    }

    public Recommendation getRecommendation(int period)
    {
        ArrayList<Recommendation> candidates = new ArrayList<>();
        ArrayList<Double> probability = new ArrayList<>();
        double sum = 0;
        for (Recommendation rec : this.Recommendations) {
            if (rec.Period <= period) {
                double prob = Math.pow((0.3 * getPeriodWeight(period, rec.Period) + 0.7 * rec.getPriority()), 5);
                if (prob > 0) {
                    sum += prob;
                    probability.add(prob);
                    candidates.add(rec);
                }
            }
        }

        Log.v("trace~", String.format("Expect: %d min", period));
        for (int i = 0; i < candidates.size(); ++i) {
            Recommendation rec = candidates.get(i);
            double prob = probability.get(i);
            Log.v("trace~", String.format("Candidate: %s(%d min) = %f", rec.Source, rec.Period, prob));
        }

        if (candidates.size() == 0) return null;

        Random rand = new Random();
        while (true) {
            for (int i = 0; i < candidates.size(); ++i) {
                double prob = probability.get(i);
                if (rand.nextDouble() <= prob / sum) {
                    return candidates.get(i);
                }
            }
        }

    }

    public int getProgress() {
        return progress;
    }

    private OnProgressListener onProgressListener;

    /**
     * 注册回调接口的方法，供外部调用
     *
     * @param onProgressListener
     */
    public void setOnProgressListener(OnProgressListener onProgressListener) {
        this.onProgressListener = onProgressListener;
    }

    /**
     * 模拟下载任务，每秒钟更新一次
     */
    public interface OnProgressListener {
        void onProgress(int progress);
    }

    public void startDownLoad() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                /*
                progress = 0;
                setUrl();
                if (onProgressListener != null) {
                    onProgressListener.onProgress(progress);
                }
                */

                while (true) {
                    Log.e("trace~", "run: Work now!");
                    sleep(2000);
                }

            }
        }).start();
    }

    public void setUrl() {
        progress = 1;
        url = "http://www.jianshu.com/p/da8a68354caa";
    }

    public String getUrl() {
        return url;
    }

    public int setTime(int t) {
        time = t;
        return 1;
    }

    public void resetpro() {
        progress = 0;
    }

    /**
     * 返回一个Binder对象
     */
    @Override
    public IBinder onBind(Intent intent) {
        return new MsgBinder();
    }

    public class MsgBinder extends Binder {
        /**
         * 获取当前Service的实例
         *
         * @return
         */
        public MsgService getService() {
            return MsgService.this;
        }
    }



}
