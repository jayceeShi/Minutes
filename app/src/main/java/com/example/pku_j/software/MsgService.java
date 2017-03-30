package com.example.pku_j.software;

/**
 * Created by pku_j on 2017/3/30.
 */

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;

import static android.os.SystemClock.sleep;

public class MsgService extends Service {

    private int progress = 0;
    private int time = 0;

    private String url = null;

    @Override
    public void onCreate() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        BroadcastRec broadcastReceiver = new BroadcastRec();
        registerReceiver(broadcastReceiver, filter);
        for (int i = 0; i < 3; i++) {
            sleep(10);
            Log.e("trace~", "coming back");
        }
    }

    public int getProgress() {
        return progress;
    }
    private OnProgressListener onProgressListener;
    /**
     * 注册回调接口的方法，供外部调用
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
    public void startDownLoad(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                progress = 0;

                setUrl();
                if(onProgressListener != null){
                    onProgressListener.onProgress(progress);
                }

            }


        }).start();
    }
    public void setUrl(){
        progress = 1;
        url = "http://www.baidu.com";
    }
    public String getUrl(){
        return url;
    }
    public int setTime(int t){
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

    public class MsgBinder extends Binder{
        /**
         * 获取当前Service的实例
         * @return
         */
        public MsgService getService(){
            return MsgService.this;
        }
    }

}