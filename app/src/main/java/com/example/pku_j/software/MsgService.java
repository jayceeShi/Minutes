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
import android.support.annotation.Nullable;
import android.text.LoginFilter;
import android.text.format.DateFormat;
import android.util.Log;

import com.mysql.jdbc.*;
import com.yancloud.android.reflection.get.YanCloudGet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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

    private static final String TAG = "trace~";


    private int progress = 0;
    private int time = 0;
    private ArrayList<Recommendation> returnRec = null;
    private String url = null;

    private Thread _backThread = null;

    private Connection _connection = null;

    private static Object HttpGet(String uri) {
        StringBuffer buffer = new StringBuffer();
        try {
            final String HTTP_HOST = "http://139.199.22.95:8080/minutes";
            URL url = new URL(HTTP_HOST + uri);
            HttpURLConnection httpUrlConn = (HttpURLConnection)url.openConnection();

            httpUrlConn.setDoOutput(false);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            httpUrlConn.setRequestMethod("GET");
            httpUrlConn.connect();

            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();

            httpUrlConn.disconnect();

            str = buffer.toString();
            if (str.charAt(0) == '{') {
                return new JSONObject(str);
            }
            else if (str.charAt(0) == '[') {
                return new JSONArray(str);
            }
        } catch (Exception e) {
            Log.e(TAG, "HttpGet: " + e.toString());
        }
        return null;
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

    private void doBackground()
    {
        /*
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
        */

    }


    private ArrayList<Recommendation> getAll()
    {
        try {
            JSONArray array = (JSONArray)HttpGet("/recommendations");

            ArrayList<Recommendation> recommendations = new ArrayList<>(array.length());

            for (int i = 0; i < array.length(); ++i) {

                JSONObject rs = array.getJSONObject(i);
                Recommendation rec = new Recommendation();

                rec.DeepLink = rs.getString("deeplink");
                rec.Source = rs.getString("source");
                rec.Title = rs.getString("title");
                rec.Period = rs.getInt("period");
                rec.DateTime = Date.valueOf(rs.getString("datetime"));
                rec.Abstract = rs.getString("abstract");
                rec.ThumbnailURL = rs.getString("thumbnail");
                rec.DeepLinkSchema = "";
                rec.ID = rs.getInt("id");
                rec.TopicLevel = rs.getInt("topic_level");
                rec.TopicTag = rs.getString("topic_tag");
                recommendations.add(rec);

                /*
                Log.v(TAG, rec.DeepLink);
                Log.v(TAG, rec.Source);
                Log.v(TAG, rec.Title);
                Log.v(TAG, Integer.toString(rec.Period));
                Log.v(TAG, rec.DateTime.toString());
                Log.v(TAG, rec.Abstract);
                Log.v(TAG, rec.ThumbnailURL);
                Log.v(TAG, rec.TopicTag);
                Log.v(TAG, Integer.toString(rec.TopicLevel));
                Log.v(TAG, "");
                */
            }

            Log.v(TAG, "recommendations: total = " + recommendations.size());
            return recommendations;

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
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ArrayList<Topic> getTopics() {

        try {
            JSONArray array = (JSONArray)HttpGet("/topics");

            ArrayList<Topic> result = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); ++i) {
                JSONObject jTopic = array.getJSONObject(i);

                Topic topic = new Topic();
                topic.Priority = jTopic.getDouble("priority");
                topic.Tag = jTopic.getString("tag");
                topic.ViewedCount = jTopic.getInt("viewed_count");
                topic.Count = jTopic.getInt("count");
                topic.Enable = (jTopic.getInt("enable") != 0);
                topic.Deadline = Date.valueOf(jTopic.getString("deadline"));
                topic.Priority = jTopic.getInt("priority");

                result.add(topic);
            }

            return result;
        }
        catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    @Nullable
    private Topic selectTopic() throws Exception {

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
            JSONObject r = (JSONObject) HttpGet(String.format("/enable/%s/%s", topic, DateFormat.format("yyyy-MM-dd", deadline)));
            Log.i(TAG, "AddTopicTag: updated = " + r.getBoolean("success"));
        }
        catch (Exception ex) {
            Log.e(TAG, "AddTopicTag: " + ex.toString());
        }
    }


    public void removeTopicTag(String topic)
    {
        try {
            JSONObject r = (JSONObject) HttpGet(String.format("/disable/%s", topic));
            Log.i(TAG, "removeTopicTag: updated = " + r.getBoolean("success"));
        }
        catch (Exception ex) {
            Log.e(TAG, "removeTopicTag: " + ex.toString());
        }
    }



    private double getPeriodWeight(int expected, int actual)
    {
        // ASSERT(expected >= actual);
        double rate = (double)(expected - actual) / (double)expected;
        if (rate >= 0.50) {
            if (expected > 5) return -10000;  // -infinity
            else return -1;
        }
        if (rate >= 0.30) return 0;
        return Math.cos(Math.PI * rate / 0.6);
    }


    //=============================================================================

    public void markAsViewed(final String url)
    {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String u = URLEncoder.encode(url, "UTF-8");
                    HttpGet(String.format("/markAsViewed/%s", u));
                    Log.i(TAG, "markAsViewed: " + url);
                }
                catch (Exception ex) {
                    Log.e(TAG, "markAsViewed: " + ex.toString());
                }
            }
        });
        t.start();
        /*
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "markAsViewed: " + e.toString());
        }
        */
    }


    public ArrayList<Recommendation> getRecommendation(int period, int count) throws Exception
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
        Log.e(TAG, "getRecommendationInternal: tagIfTopic = " + tagIfTopic);
        boolean tagIfTopicEmpty = (tagIfTopic == null || tagIfTopic.length() == 0);

        ArrayList<Recommendation> candidates = new ArrayList<>();
        ArrayList<Double> probability = new ArrayList<>();

        double sum = 0;
        for (Recommendation rec : getAll()) {
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
                try {
                    returnRec = getRecommendation(time, 8);
                } catch (Exception e) {
                    e.printStackTrace();
                }

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