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
import android.text.LoginFilter;
import android.util.Log;

import com.mysql.jdbc.*;
import com.yancloud.android.reflection.get.YanCloudGet;

import java.sql.Array;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Matcher;

import static android.os.SystemClock.sleep;

public class MsgService extends Service {

    private final String TAG = "trace~";


    private int progress = 0;
    private int time = 0;
    private ArrayList<Recommendation> returnRec = null;
    private String url = null;

    private Thread _backThread = null;

    private Connection _connection = null;

    private Connection getConn() {
        /*
        if (_connection != null && !_connection.isClosed()) {
            return _connection;
        }
        */

        try {
            // 连接URL为: jdbc:mysql//服务器地址/数据库名 后面的2个参数分别是登陆用户名和密码
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            _connection = (Connection) DriverManager.getConnection("jdbc:mysql://minutes.catchyrime.com:3306/minutes", "root", "MYSQL_root-132585");
            return _connection;
        }
        catch (Exception ex) {
            Log.e(TAG, "getConn(): " + ex);
            return null;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG,"I am back!");
        progress = 0;
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        BroadcastRec broadcastReceiver = new BroadcastRec();
        registerReceiver(broadcastReceiver, filter);

        // One-time initializations

        if (_backThread == null || !_backThread.isAlive()) {
            Log.v(TAG,"Start background!");
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
        Log.v(TAG,"doBackground!");

        // Get Current IP
        WifiManager wifiMan = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiMan.getConnectionInfo();
        int ipAddress = info.getIpAddress();
        String ip = "";
        if (ipAddress == 0) {
            Log.e(TAG, "Not connecting to wifi! SHOULD do nothing!");
            //return;
        }
        else {
            ip = ((ipAddress & 0xFF) + "." + (ipAddress >> 8 & 0xFF) + "." + (ipAddress >> 16 & 0xFF) + "." + (ipAddress >> 24 & 0xFF));
            Log.e(TAG, String.format("Wifi IP: %s", ip));
        }


        try {
            Connection conn = getConn();
            Log.v(TAG, "MySQL connected");

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
                rec.DeepLinkSchema = "";
                rec.ID = rs.getInt("id");
                rec.TopicLevel = rs.getInt("topic_level");
                rec.TopicTag = rs.getString("topic_tag");
                recommendations.add(rec);

                //Log.v(TAG, rec.DeepLink);
                //Log.v(TAG, rec.Source);
                //Log.v(TAG, rec.Title);
                //Log.v(TAG, Integer.toString(rec.Period));
                //Log.v(TAG, rec.DateTime.toString());
                //Log.v(TAG, rec.Abstract);
                //Log.v(TAG, rec.ThumbnailURL);
                //Log.v(TAG, rec.DeepLinkSchema);
            }

            // Prepare the recommendations
            Log.d(TAG, "Do prepareRecommendations");
            prepareRecommendations(recommendations);

            this.Recommendations = recommendations;

            /*addTopicTag("fitness", Date.valueOf("2018-5-8"));
            addTopicTag("IslandTravel", Date.valueOf("2017-9-8"));
            /*
            addTopicTag("machine learning", Date.valueOf("2017-7-5"));
            addTopicTag("ArtHistory", Date.valueOf("2018-1-1"));
            */

            /*ArrayList<Recommendation> rr = getRecommendation(10, 7);
            for (Recommendation r: rr) {
                Log.d(TAG, String.format("[Rec][%d][%s][%s] %s", r.Period, r.TopicTag, r.Source, r.Title));
            }

            removeTopicTag("fitness");
            removeTopicTag("IslandTravel");
            removeTopicTag("machine learning");
            removeTopicTag("ArtHistory");*/
            conn.close();
        }
        catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }

        //Recommendation rec = getRecommendation(34);
        //Log.e(TAG, String.format("Recommend: %s(%d min)", rec.Source, rec.Period));
    }

    public ArrayList<Topic> getTopics()
    {
        ArrayList<Topic> result = new ArrayList<>();
        try {
            Connection conn = getConn();
            Statement st = (Statement)conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM topics");
            while (rs.next()) {
                Topic topic = new Topic();
                topic.Enable = (rs.getInt("enable") != 0);
                topic.Count = rs.getInt("count");
                topic.ViewedCount = rs.getInt("viewed_count");
                topic.Deadline = rs.getDate("deadline");
                topic.Priority = rs.getDouble("priority");
                topic.Tag = rs.getString("topic");

                result.add(topic);
            }
            conn.close();
        }
        catch (Exception ex) {
            Log.e(TAG, "getTopics: " + ex);
        }
        return result;
    }

    private Topic selectTopic()
    {
        ArrayList<Topic> topics = getTopics();
        double sum = 0.0;
        for (Topic topic: topics) {
            Log.i(TAG, String.format("[Topic][enable:%b] %s: %d/%d, priority = %f",
                    topic.Enable, topic.Tag, topic.ViewedCount, topic.Count, topic.Priority));
            if (topic.Enable) {
                sum += topic.Priority;
            }
        }

        if (sum == 0.0) {  // No problem! double equality!
            return null;
        }

        Random rand = new Random();
        for (int r = rand.nextInt(10); r >= 0; --r) rand.nextDouble();
        while (true) {
            for (Topic topic: topics) {
                if (topic.Enable) {
                    if (topic.Priority / sum > rand.nextDouble()) return topic;
                }
            }
        }
    }

    public void addTopicTag(String topic, java.util.Date deadline)
    {
        try {
            Connection conn = getConn();
            PreparedStatement stat = (PreparedStatement)conn.prepareStatement("UPDATE topics SET enable = 1, deadline = ? WHERE topic = ?");
            stat.setDate(1, java.sql.Date.valueOf(deadline.getYear() + "-" + deadline.getMonth() + "-" + deadline.getDay()));
            stat.setString(2, topic);
            int updated = stat.executeUpdate();

            CallableStatement cs = (CallableStatement)conn.prepareCall("{CALL UpdateTopic()}");
            cs.execute();
            conn.close();

            Log.i(TAG, "AddTopicTag: updated = " + updated);
        }
        catch (Exception ex) {
            Log.e(TAG, "AddTopicTag: " + ex.toString());
        }
    }


    public void removeTopicTag(String topic)
    {
        try {
            Connection conn = getConn();
            PreparedStatement stat = (PreparedStatement)conn.prepareStatement("UPDATE topics SET enable = 0 WHERE topic = ?");
            stat.setString(1, topic);
            int updated = stat.executeUpdate();
            Log.i(TAG, "removeTopicTag: updated = " + updated);
            conn.close();
        }
        catch (Exception ex) {
            Log.e(TAG, "removeTopicTag: " + ex.toString());
        }
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

    private double getPeriodWeight(int expected, int actual)
    {
        // ASSERT(expected >= actual);
        double rate = (double)(expected - actual) / (double)expected;
        if (rate >= 0.50) return -10000;  // -infinity
        if (rate >= 0.30) return 0;
        return Math.cos(Math.PI * rate / 0.6);
    }


    //=============================================================================

    public void markAsViewed(int id)
    {
        try {
            Connection conn = getConn();
            PreparedStatement stat = (PreparedStatement)conn.prepareStatement("UPDATE recommendation SET viewed = 1 WHERE id = ?");
            stat.setInt(1, id);
            int updated = stat.executeUpdate();
            Log.i(TAG, "markAsViewed: " + id);
            conn.close();
        }
        catch (Exception ex) {
            Log.e(TAG, "markAsViewed: " + ex.toString());
        }
    }

    public void markAsViewed(final String url)
    {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection conn = getConn();
                    PreparedStatement stat = (PreparedStatement)conn.prepareStatement("UPDATE recommendation SET viewed = 1 WHERE deeplink = ?");
                    stat.setString(1, url);
                    int updated = stat.executeUpdate();
                    Log.i(TAG, "markAsViewed: " + url);
                    conn.close();
                }
                catch (Exception ex) {
                    Log.e(TAG, "markAsViewed: " + ex.toString());
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "markAsViewed: " + e.toString());
        }
    }


    public ArrayList<Recommendation> getRecommendation(int period, int count)
    {
        Topic topic = selectTopic();
        String tag = (topic == null) ? null : topic.Tag;
        final int TOPIC_RECOMMENDATION_COUNT = 4;
        if (count < TOPIC_RECOMMENDATION_COUNT) {

            return getRecommendationInternal(period, count, tag);
        }
        else {
            ArrayList<Recommendation> tmp = new ArrayList<>();
            tmp.addAll(getRecommendationInternal(period, TOPIC_RECOMMENDATION_COUNT, tag));
            tmp.addAll(getRecommendationInternal(period, count - TOPIC_RECOMMENDATION_COUNT, null));
            return tmp;
        }
    }


    public ArrayList<Recommendation> getRecommendationInternal(int period, int count, String tagIfTopic)
    {
        ArrayList<Recommendation> candidates = new ArrayList<>();
        ArrayList<Double> probability = new ArrayList<>();

        double sum = 0;
        for (Recommendation rec : this.Recommendations) {
            boolean tagIfTopicEmpty = (tagIfTopic == null || tagIfTopic.length() == 0);
            boolean recTagEmpty = (rec.TopicTag == null || rec.TopicTag.length() == 0);
            if (tagIfTopicEmpty != recTagEmpty) continue;
            if (!tagIfTopicEmpty && !Objects.equals(tagIfTopic, rec.TopicTag)) continue;
            if (rec.Period <= period) {
                double prob = Math.pow((0.3 * getPeriodWeight(period, rec.Period) + 0.7 * rec.getPriority()), 3);
                if (prob > 0) {
                    sum += prob;
                    probability.add(prob);
                    candidates.add(rec);
                }
            }
        }
        Log.v(TAG, String.format("Expect: %d min", period));
        for (int i = 0; i < candidates.size(); ++i) {
            Recommendation rec = candidates.get(i);
            double prob = probability.get(i);
            Log.v(TAG, String.format("Candidate: %s(%d min) = %f", rec.Source, rec.Period, prob));
        }
        Log.e("trace~","into tag recom " + candidates.size());
        if (candidates.size() < count) return null;

        ArrayList<Recommendation> ret = new ArrayList<Recommendation>();
        ArrayList<Integer> retIdx = new ArrayList<Integer>();
        Random rand = new Random();
        while (ret.size() < count) {
            for (int i = 0; i < candidates.size(); ++i) {
                double prob = probability.get(i);
                if (rand.nextDouble() <= prob / sum) {
                    if (!retIdx.contains(i)) {
                        Recommendation r = candidates.get(i);
                        ret.add(r);
                        retIdx.add(i);
                    }
                }
            }
        }

        return ret;
    }



    //=============================================================================




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
            public void run(){
                returnRec = getRecommendation(time,8);

                for(int i = 0; i < 1; i++){
                    returnRec.get(i).getThumbnail();
                }

                progress = 1;
                if (onProgressListener != null) {
                    onProgressListener.onProgress(progress);
                }
            }
        }).start();
    }
    public ArrayList<Recommendation> getRec(){
        return returnRec;
    }
    public void resetpro(){
        progress = 0;
    }
    public String getUrl() {
        return url;
    }

    public int setTime(int t) {
        time = t;
        return 1;
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