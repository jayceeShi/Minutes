package com.example.pku_j.software;

import android.Manifest;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.AdapterView;
import android.widget.Button;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ArrayAdapter;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.view.ViewGroup.LayoutParams;
import com.yancloud.android.reflection.get.YanCloudGet;


public class MainActivity extends AppCompatActivity {
    private float startx, starty, endx, endy;
    private float centerx, centery;
    private float radius = 180.0f;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;
    private ImageView clock;
    private MsgService msgService;
    private int timeT;
    private int canUrl = 0;
    private Recommendation recRec = null;
    public double calcuAng(float x, float y){

        if(x - centerx < 0.001 && x - centerx > -0.001){
            if (y > centery)
                return Math.PI/2;
            else
                return -Math.PI/2;
        }
        return Math.atan2(y-centery, x-centerx);
    }
    private Button menu;
    private ArrayList<String> list = new ArrayList<String>();
    private String url;
    private boolean changeFlag = false;
    public ImageView dis;
    private Bitmap bmp;
    private TextView name;
    private Handler handler=null;
    private ImageButton hanImg;
    ServiceConnection conn;
    private SlidingDrawer sd;
    Runnable   runnableUi=new  Runnable(){
        @Override
        public void run() {
            url = recRec.DeepLink;
            Log.v("trace~",url);
            canUrl = 1;
            ImageView logo = (ImageView)findViewById(R.id.logo);
            TextView txt = (TextView)findViewById(R.id.Rtime);
            Bitmap logo2;
            txt.setText("00:" + String.format("%02d", recRec.Period));
            TextView title = (TextView)findViewById(R.id.Name);
            title.setText(recRec.Title);

            switch(recRec.Source){
                case "keep":
                    //dis.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.shopping));
                    logo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.keep));break;
                case "jd":
                    //dis.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.shopping));
                    logo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.shopping_icon));break;
                case "youku":
                    //dis.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.video));
                    logo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.video_icon));break;
                case "zhihu":
                    //dis.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.video));
                    logo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.news_icon));break;
                case "toutiao":
                    //dis.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.video));
                    logo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.news_icon));break;
                default:
                    //dis.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.video));
                    logo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.keep));break;
            }
            logo2 = recRec.getThumbnail();
            //if(logo2 != null)
                dis.setImageBitmap(logo2);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if (getSupportActionBar() != null){
        //    getSupportActionBar().hide();
        //}
        verifyStoragePermissions(this);

        setContentView(R.layout.recommendlay);

        handler=new Handler();


        sd = (SlidingDrawer)findViewById(R.id.sliding);
        final ImageButton imbg = (ImageButton)findViewById(R.id.handle);
        sd.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener()
        {
            @Override
            public void onDrawerOpened() {
                imbg.getBackground().setAlpha(0);
            }

        });

        sd.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
                                        @Override
                                        public void onDrawerClosed() {
                                            imbg.getBackground().setAlpha(1);
                                            imbg.setImageResource(R.drawable.uprow);
                                        }
                                    });
        clock = (ImageView)findViewById(R.id.clock);

        list.add("exit");
        paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setColor(Color.RED);

        clock.setOnTouchListener(new PicOnTouchListener());

        name = (TextView)findViewById(R.id.Name);
        name.setText("等待推荐中");

        Log.v("trace~",""+this.getWindowManager().getDefaultDisplay().getHeight());
        dis=(ImageView)(findViewById(R.id.display));
        bmp= BitmapFactory.decodeResource(getResources(), R.drawable.renyue);


        Bitmap lot1 = BitmapFactory.decodeResource(getResources(), R.drawable.keep);

        ImageView tempDis = (ImageView)findViewById(R.id.rec2);
        LayoutParams para = tempDis.getLayoutParams();


        int height=para.height;

        int width=para.width;

        Bitmap icon = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); //建立一个空的图画板
        Canvas canvas = new Canvas(icon);//初始化画布绘制的图像到icon上

        Paint photoPaint = new Paint(); //建立画笔
        Paint photoPaint2 = new Paint();
        photoPaint2.setTextSize(50);
        //paint.setTypeface();
        Rect dst = new Rect(30, 15, height-20, height-25);//创建一个指定的新矩形的坐标
        canvas.drawBitmap(lot1, null, dst, photoPaint);//将photo 缩放或则扩大到 dst使用的填充区photoPain

        //canvas.drawText("new recommendation to do ahhhhhhhhhh", height, height/2, photoPaint2);//将photo 缩放或则扩大到 dst使用的填充区photoPaint

        canvas.drawText("time",60,height-10,photoPaint);

        TextPaint textPaint = new TextPaint();
        //textPaint.setARGB(0xFF, 0xFF, 0, 0);
        textPaint.setTextSize(40.0F);
        String aboutTheGame = "new recommendation to do ahhhhhhhhhh ";
/** * aboutTheGame ：要 绘制 的 字符串 ,textPaint(TextPaint 类型)设置了字符串格式及属性 的画笔,240为设置 画多宽后 换行，后面的参数是对齐方式... */
        StaticLayout layout = new StaticLayout(aboutTheGame,textPaint,width-height-10, Layout.Alignment.ALIGN_NORMAL,1.0F,0.0F,true);
//从 (20,80)的位置开始绘制
        canvas.translate(height,20);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        layout.draw(canvas);
        tempDis.setImageBitmap(icon);

        conn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName namec) {

            }

            @Override
            public void onServiceConnected(ComponentName namec, IBinder service) {
                //返回一个MsgService对象

                msgService = ((MsgService.MsgBinder)service).getService();
                msgService.setOnProgressListener(new MsgService.OnProgressListener() {

                    @Override
                    public void onProgress(int progress) {
                        if(progress == 1) {
                            Log.v("trace~","have got recommendation");
                            recRec = msgService.getRec();
                            handler.post(runnableUi);
                        }
                        msgService.resetpro();
                    }
                });
            }
        };

        Intent intent = null;
        if(intent == null) {
            intent = new Intent(this, MsgService.class);
            Log.e("msgService","failed to open service");
            startService(intent);
        }

        //intent.setAction("com.example.pku_j.software.MsgService");
        //intent.setPackage("com.example.pku_j.software.MsgService"); //指定启动的是那个应用（lq.cn.twoapp）中的Action(b.aidl.DownLoadService)指向的服务组件
        bindService(intent, conn, BIND_AUTO_CREATE);
        dis.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //dis.setImageBitmap(bmp);
        dis.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(canUrl != 0) {
                    Uri uri = Uri.parse(url);
                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(it);
                }
            }
        });
        ImageView up = (ImageView)findViewById(R.id.up);


        menu = (Button)(findViewById(R.id.menu));
        //spin = (Spinner)(findViewById(R.id.spinner));
        menu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                View popupView = MainActivity.this.getLayoutInflater().inflate(R.layout.popupwindow, null);

                ListView lsvMore = (ListView) popupView.findViewById(R.id.lsvMore);
                lsvMore.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, list));

                lsvMore.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                            long arg3) {
                        if(list.get(arg2).equals("exit")){
                            unbindService(conn);
                            System.exit(0);
                        }
                        System.out.print(arg2);
                    }
                });

                PopupWindow window = new PopupWindow(popupView, 120, 100);

                window.setFocusable(true);
                window.setOutsideTouchable(true);
                window.update();
                window.showAsDropDown(menu, 0, 20);

                return true;
            }
        });


    }

    public int calcuTime(double angle){
        angle = -angle;
        if (angle > -0.001 && angle < 90)
            return (int) ((90 - angle)/90 * 15);
        if (angle > 89.999 && angle < 180.1)
            return (int) ((180 - angle) / 90 * 15 + 45);
        if (angle < 0.001 && angle > -90.001)
            return (int) ((-angle) / 90 * 15 + 15);
        return (int) ((-angle - 90)/90 * 15 + 30);
    }
    private class PicOnTouchListener implements View.OnTouchListener {
        float startX, startY;
        Bitmap q;
        double angle;
        @Override
        public boolean onTouch(View v, MotionEvent e) {
            switch (e.getAction()) {
                // 用户按下动作
                case MotionEvent.ACTION_DOWN:
                    //baseBitmap = Bitmap.createBitmap(clock.getWidth(), clock.getHeight(), Bitmap.Config.ARGB_8888);

                    if(baseBitmap == null) {
                        clock.setDrawingCacheEnabled(true);
                        baseBitmap = Bitmap.createBitmap(clock.getDrawingCache());
                        q = Bitmap.createBitmap(clock.getDrawingCache());
                        clock.setDrawingCacheEnabled(false);
                        canvas = new Canvas(baseBitmap);
                        canvas.drawColor(Color.TRANSPARENT);
                    }
                    else {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        canvas.drawBitmap(q,0,0,paint);
                        clock.setImageBitmap(baseBitmap);
                        clock.invalidate();
                    }
                    centerx = clock.getWidth() / 2;
                    centery =  clock.getHeight() / 2;
                    startx = e.getX();
                    starty = e.getY();

                case MotionEvent.ACTION_MOVE:
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    canvas.drawBitmap(q,0,0,paint);
                    startx = e.getX();
                    starty = e.getY();

                    angle = calcuAng(startx, starty);
                    //System.out.print(startx + " " + starty + "\n");
                    //System.out.print(centerx + " " + centery + "\n");
                    //System.out.print("****************");
                    startx = (int) (centerx + radius * Math.cos(angle));
                    starty = (int) (centery + radius * Math.sin(angle));
                    canvas.drawCircle(startx, starty, 10.0f, paint);
                    clock.setImageBitmap(baseBitmap);
                    clock.invalidate();

                    ((TextView)(findViewById(R.id.Time))).setText("00:" + String.format("%02d", 2*calcuTime(Math.toDegrees(angle))));

                    break;
                case MotionEvent.ACTION_UP:

                    int time = 2*calcuTime(Math.toDegrees(angle));
                    timeT = time;
                    ((TextView)(findViewById(R.id.TTime))).setText("00:" + String.format("%02d", timeT));
                    passAng(time);

                    break;
                default:
                    break;
            }

            return true;
        }

        public void passAng(int time){
            canUrl = 0;
            msgService.setTime(time);
            msgService.startDownLoad();

        }
    }

    @Override
    protected void onDestroy() {
        unbindService(conn);
        super.onDestroy();
    }

    public boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

}



