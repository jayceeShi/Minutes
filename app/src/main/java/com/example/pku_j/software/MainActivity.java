package com.example.pku_j.software;

        import android.content.Intent;
        import android.content.ServiceConnection;
        import android.graphics.BitmapFactory;
        import android.net.Uri;
        import android.os.IBinder;
        import android.widget.AdapterView;
        import android.widget.Button;
        import android.graphics.Bitmap;
        import android.graphics.PorterDuff;
        import android.graphics.drawable.BitmapDrawable;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.MotionEvent;
        import android.view.View;
        import android.view.Window;
        import android.view.WindowManager;
        import android.widget.EditText;
        import android.widget.ImageView;
        import android.content.Context;
        import android.graphics.Canvas;
        import android.graphics.Color;
        import android.graphics.Paint;
        import android.widget.Spinner;
        import android.widget.TextView;
        import android.widget.ListView;
        import android.widget.PopupWindow;
        import android.graphics.drawable.ColorDrawable;
        import android.widget.ArrayAdapter;
        import android.app.Activity;
        import android.app.ActivityManager;
        import android.app.ActivityManager.RunningTaskInfo;
        import android.content.ComponentName;
        import android.os.Bundle;
        import android.os.SystemClock;
        import android.util.Log;
        import android.view.Menu;

        import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private float startx, starty, endx, endy;
    private float centerx, centery;
    private float radius = 180.0f;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;
    private ImageView clock;
    private MsgService msgService;
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

    ServiceConnection conn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);


        conn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {

            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                //返回一个MsgService对象
                msgService = ((MsgService.MsgBinder)service).getService();
            }
        };

        Intent intent = new Intent(this, MsgService.class);
        //intent.setAction("com.example.pku_j.software.MsgService");
        //intent.setPackage("com.example.pku_j.software.MsgService"); //指定启动的是那个应用（lq.cn.twoapp）中的Action(b.aidl.DownLoadService)指向的服务组件
        bindService(intent, conn, BIND_AUTO_CREATE);



        clock = (ImageView)findViewById(R.id.clock);
        list.add("exit");
        paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setColor(Color.RED);

        clock.setOnTouchListener(new PicOnTouchListener());

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
                        if(list.get(arg2).equals("exit"))System.exit(0);
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

                    ((TextView)(findViewById(R.id.Time))).setText("00:" + String.format("%02d", calcuTime(Math.toDegrees(angle))));

                    break;
                case MotionEvent.ACTION_UP:

                    int time = calcuTime(Math.toDegrees(angle));
                    passAng(time);
                    break;
                default:
                    break;
            }

            return true;
        }

        public void passAng(int time){
            final String url;
            msgService.setTime(time);
            msgService.startDownLoad();
            while(msgService.getProgress() != 1)continue;
            ImageView dis=(ImageView)(findViewById(R.id.display));
            Bitmap bmp= BitmapFactory.decodeResource(getResources(), R.drawable.abc);
            dis.setImageBitmap(bmp);
            url = msgService.getUrl();
            dis.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Uri uri = Uri.parse(url);
                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(it);
                    return true;
                }
            });
        }
    }
    @Override
    protected void onDestroy() {
        unbindService(conn);
        super.onDestroy();
    }

}
