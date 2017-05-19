package com.example.pku_j.software;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.LinearLayout;
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
import android.widget.Toast;

import static android.graphics.Typeface.SANS_SERIF;


public class MainActivity extends AppCompatActivity {
    private float startx, starty;
    private float centerx, centery;
    private float radius = 180.0f;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;
    private ImageView clock;
    private MsgService msgService;
    private int timeT;
    private int canUrl = 0;
    private EditText TopicEndTime;

    private ArrayList<Recommendation> recRec = null;
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
    private ArrayList<String> listCreate = new ArrayList<String>();
    private String url;
    public ImageView dis;
    private TextView name;
    private Handler handler=null;
    private Handler handleri = null;
    private PopupWindow window = null;

    ServiceConnection conn;
    private SlidingDrawer sd;
    private int mYear, mMonth, mDay;
    public void FirstLayout(){
        setContentView(R.layout.recommendlay);

        if(window != null && window.isShowing())
            window.dismiss();

        handler=new Handler();
        handleri = new Handler();

        sd = (SlidingDrawer)findViewById(R.id.sliding);
        final ImageButton imbg = (ImageButton)findViewById(R.id.handle);
        sd.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener()
        {
            @Override
            public void onDrawerOpened() {
                imbg.getBackground().setAlpha(255);
            }

        });

        sd.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                imbg.getBackground().setAlpha(255);
                imbg.setImageResource(R.drawable.uprow);
            }
        });
        clock = (ImageView)findViewById(R.id.clock);


        paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setColor(Color.RED);

        clock.setOnTouchListener(new PicOnTouchListener());

        name = (TextView)findViewById(R.id.Name);
        name.setText("等待推荐中");

        Log.v("trace~",""+this.getWindowManager().getDefaultDisplay().getHeight());
        dis=(ImageView)(findViewById(R.id.display));


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
                            for(int i = 0; i < 8; i++) {
                                recRec.get(i).getThumbnail();
                            }
                            handler.post(runnableUi);
                            handleri.post(runnableUii);
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

        ImageView tmpRec = (ImageView)findViewById(R.id.rec1);
        tmpRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("trace~","click first rec");
                if(canUrl != 0) {
                    Uri uri = Uri.parse(recRec.get(1).DeepLink);
                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(it);
                }
            }
        });
        tmpRec = (ImageView)findViewById(R.id.rec2);
        tmpRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canUrl != 0) {
                    Uri uri = Uri.parse(recRec.get(2).DeepLink);
                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(it);
                }
            }
        });
        tmpRec = (ImageView)findViewById(R.id.rec3);
        tmpRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canUrl != 0) {
                    Uri uri = Uri.parse(recRec.get(3).DeepLink);
                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(it);
                }
            }
        });
        tmpRec = (ImageView)findViewById(R.id.rec4);
        tmpRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canUrl != 0) {
                    Uri uri = Uri.parse(recRec.get(4).DeepLink);
                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(it);
                }
            }
        });
        tmpRec = (ImageView)findViewById(R.id.rec5);
        tmpRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canUrl != 0) {
                    Uri uri = Uri.parse(recRec.get(5).DeepLink);
                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(it);
                }
            }
        });
        tmpRec = (ImageView)findViewById(R.id.rec6);
        tmpRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canUrl != 0) {
                    Uri uri = Uri.parse(recRec.get(6).DeepLink);
                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(it);
                }
            }
        });
        tmpRec = (ImageView)findViewById(R.id.rec7);
        tmpRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canUrl != 0) {
                    Uri uri = Uri.parse(recRec.get(7).DeepLink);
                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(it);
                }
            }
        });


        menu = (Button)(findViewById(R.id.Menu));
        //spin = (Spinner)(findViewById(R.id.spinner));
        menu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    View popupView = MainActivity.this.getLayoutInflater().inflate(R.layout.popupwindow, null, false);

                    ListView lsvMore = (ListView) popupView.findViewById(R.id.lsvMore);


                    window = new PopupWindow(popupView, 150, 300, true);

                    window.setTouchable(true);
                    window.setFocusable(true);
                    window.setOutsideTouchable(true);
                    //window.update();
                    window.setBackgroundDrawable(getResources().getDrawable(
                            R.drawable.winback));
                    window.showAsDropDown(menu, 0, 20);
                    lsvMore.setAdapter(new ArrayAdapter<String>(MainActivity.this, R.layout.list_item_1, list));

                    lsvMore.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                                long arg3) {
                            if (list.get(arg2).equals("退出")) {
                                unbindService(conn);
                                System.exit(0);
                            }
                            if (list.get(arg2).equals("随时")) {
                                window.dismiss();
                                FirstLayout();
                            }
                            if (list.get(arg2).equals("目标")) {
                                window.dismiss();
                                ViewLayout();
                            }

                        }
                    });
                }

                return true;
            }
        });

    }
    public void ViewLayout(){
        setContentView(R.layout.view);
        if(window.isShowing()) {
            window.dismiss();
        }

        menu = (Button)(findViewById(R.id.Menu));
        //spin = (Spinner)(findViewById(R.id.spinner));
        menu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if(event.getAction() ==  MotionEvent.ACTION_UP) {
                    View popupView = MainActivity.this.getLayoutInflater().inflate(R.layout.popupwindow, null, false);

                    ListView lsvMore = (ListView) popupView.findViewById(R.id.lsvMore);
                    lsvMore.setAdapter(new ArrayAdapter<String>(MainActivity.this, R.layout.list_item_1, list));

                    window = new PopupWindow(popupView, 150, 300, true);
                    window.setBackgroundDrawable(getResources().getDrawable(
                            R.drawable.winback));

                    window.setTouchable(true);
                    window.setFocusable(true);
                    window.setOutsideTouchable(true);
                    window.showAsDropDown(menu, 0, 20);
                    //window.update();
                    lsvMore.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                                long arg3) {
                            if (list.get(arg2).equals("退出")) {
                                unbindService(conn);
                                System.exit(0);
                            }
                            if (list.get(arg2).equals("随时")) {

                                window.dismiss();

                                FirstLayout();
                            }
                            if (list.get(arg2).equals("目标")) {

                                window.dismiss();

                                ViewLayout();
                            }

                        }
                    });
                }

                return true;

            }

        });
        DeletableAdapter adapter = new DeletableAdapter(this, listCreate);
        ListView listCreated = (ListView)findViewById(R.id.created);
        listCreated.setAdapter(adapter);
        ImageView create = (ImageView)findViewById(R.id.create);
        listCreate.add("#马刺，每天");
        adapter.notifyDataSetChanged();
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TopicLayout();
            }
        });




    }
    public void TopicLayout() {
        setContentView(R.layout.topic);
        if(window != null && window.isShowing())
            window.dismiss();
        if(window != null && window.isShowing())
            window.dismiss();
        if(window != null && window.isShowing())
            window.dismiss();

        menu = (Button) (findViewById(R.id.Menu));
        //spin = (Spinner)(findViewById(R.id.spinner));
        menu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction() ==  MotionEvent.ACTION_UP) {
                    View popupView = MainActivity.this.getLayoutInflater().inflate(R.layout.popupwindow, null, false);

                    ListView lsvMore = (ListView) popupView.findViewById(R.id.lsvMore);
                    lsvMore.setAdapter(new ArrayAdapter<String>(MainActivity.this, R.layout.list_item_1, list));


                    window = new PopupWindow(popupView, 150, 300, true);
                    window.setBackgroundDrawable(getResources().getDrawable(
                            R.drawable.winback));
                    window.setTouchable(true);
                    window.setFocusable(true);
                    window.setOutsideTouchable(true);
                    window.showAsDropDown(menu, 0, 20);
                    //window.update();
                    lsvMore.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                                long arg3) {
                            if (list.get(arg2).equals("退出")) {
                                unbindService(conn);
                                System.exit(0);
                            }
                            if (list.get(arg2).equals("随时")) {
                                window.dismiss();

                                FirstLayout();
                            }
                            if (list.get(arg2).equals("目标")) {

                                window.dismiss();

                                ViewLayout();
                            }

                        }
                    });

                }
                return false;
            }
        });


        TopicEndTime = (EditText)findViewById(R.id.editTime);
        TopicEndTime.setInputType(InputType.TYPE_NULL);
        TopicEndTime.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                showDialog(0);
            }
        });
        TopicEndTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus == true) {
                    hideIM(v);
                    showDialog(0);
                }
            }
        });
    }
    public class DeletableAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<String> text;

        public DeletableAdapter(Context context, ArrayList<String> text) {
            this.context = context;
            this.text = text;
        }

        @Override
        public int getCount() {
// TODO Auto-generated method stub
            return text.size();
        }

        @Override
        public Object getItem(int position) {
// TODO Auto-generated method stub
            return text.get(position);
        }

        @Override
        public long getItemId(int position) {
// TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
// TODO Auto-generated method stub
            final int index = position;
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.list_sch, null);
            }
            final TextView textView = (TextView) view
                    .findViewById(R.id.lv_item_tv);
            textView.setText(text.get(position));
            final ImageView imageView = (ImageView) view.findViewById(R.id.lv_item_bt);
            //imageView.setBackgroundResource(android.R.drawable.ic_delete);
            imageView.setTag(position);
            imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
// TODO Auto-generated method stub
                    text.remove(index);
                    notifyDataSetChanged();
                    Toast.makeText(context, textView.getText().toString() + " 再会",
                            Toast.LENGTH_SHORT).show();
                }
            });
            return view;
        }
    }


    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    String mm;
                    String dd;
                    if(monthOfYear<=9)
                    {
                        mMonth = monthOfYear+1;
                        mm="0"+mMonth;
                    }
                    else{
                        mMonth = monthOfYear+1;
                        mm=String.valueOf(mMonth);
                    }
                    if(dayOfMonth<=9)
                    {
                        mDay = dayOfMonth;
                        dd="0"+mDay;
                    }
                    else{
                        mDay = dayOfMonth;
                        dd=String.valueOf(mDay);
                    }
                    mDay = dayOfMonth;
                    TopicEndTime.setText(String.valueOf(mYear)+"-"+mm+"-"+dd);
                }
            };

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
            case 1:
                return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
        }
        return null;
    }

    // 隐藏手机键盘
    private void hideIM(View edt){
        try {
            InputMethodManager im = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            IBinder  windowToken = edt.getWindowToken();
            if(windowToken != null) {
                im.hideSoftInputFromWindow(windowToken, 0);
            }
        } catch (Exception e) {

        }
    }

    Runnable   runnableUi=new  Runnable(){
        @Override
        public void run() {
            url = recRec.get(0).DeepLink;
            Log.v("trace~",url);
            canUrl = 1;
            ImageView logo = (ImageView)findViewById(R.id.logo);
            TextView txt = (TextView)findViewById(R.id.Rtime);
            Bitmap logo2;
            txt.setText("00:" + String.format("%02d", recRec.get(0).Period));
            TextView title = (TextView)findViewById(R.id.Name);
            String titleS = recRec.get(0).Title;
            if(titleS.length() > 20)
                titleS = titleS.substring(0,19) + "...";
            title.setText(titleS);

            switch(recRec.get(0).Source){
                case "keep":
                    //dis.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.shopping));
                    logo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.keep));break;
                case "jd":
                    //dis.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.shopping));
                    logo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.shopping_icon));break;
                case "youku":
                    //dis.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.video));
                    logo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.video_icon));break;
                case "le":
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
            logo2 = recRec.get(0).getThumbnail();
            //if(logo2 != null)
            dis.setImageBitmap(logo2);

        }

    };

    Runnable   runnableUii=new  Runnable(){
        @Override
        public void run() {

            Bitmap logo2 = recRec.get(1).getThumbnail();

            ImageView tempDiss = (ImageView)findViewById(R.id.rec1);
            LayoutParams para = tempDiss.getLayoutParams();


            int height=para.height;

            int width=para.width;
            for(int i = 1; i <= 7; i++) {
                Log.v("trace@","into trace "+i+" " + recRec.get(i).Source);
                Bitmap setLogo;
                ImageView tempDis = (ImageView)findViewById(R.id.rec1);

                switch(i){
                    case 1:tempDis = (ImageView)findViewById(R.id.rec1);
                        break;
                    case 2: tempDis = (ImageView)findViewById(R.id.rec2);
                        break;
                    case 3:tempDis = (ImageView)findViewById(R.id.rec3);
                        break;
                    case 4:tempDis = (ImageView)findViewById(R.id.rec4);
                        break;
                    case 5:tempDis = (ImageView)findViewById(R.id.rec5);
                        break;
                    case 6:tempDis = (ImageView)findViewById(R.id.rec6);
                        break;
                    case 7:tempDis = (ImageView)findViewById(R.id.rec7);
                        break;
                }

                Bitmap icon = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); //建立一个空的图画板
                Canvas canvas = new Canvas(icon);//初始化画布绘制的图像到icon上

                Paint photoPaint = new Paint(); //建立画笔
                Paint photoPaint2 = new Paint();
                photoPaint2.setTextSize(40);
                //paint.setTypeface();
                Rect dst = new Rect(30, 15, height - 20, height - 25);//创建一个指定的新矩形的坐标
                Log.v("trace@","into trace second "+i+" " + recRec.get(i).Source);
                switch(recRec.get(i).Source){
                    case "keep":
                        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.keep), null, dst, photoPaint);break;
                    case "jd":

                        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.shopping_icon), null, dst, photoPaint);break;
                    case "youku":

                        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.video_icon), null, dst, photoPaint);break;
                    case "le":

                        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.video_icon), null, dst, photoPaint);break;
                    case "zhihu":

                        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.news_icon), null, dst, photoPaint);break;
                    case "toutiao":

                        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.news_icon), null, dst, photoPaint);break;

                    default:
                        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.keep), null, dst, photoPaint);break;
                }
                Log.v("trace@","into trace third "+i+" " + recRec.get(i).Source);
                //将photo 缩放或则扩大到 dst使用的填充区photoPain
                //canvas.drawText("new recommendation to do ahhhhhhhhhh", height, height/2, photoPaint2);//将photo 缩放或则扩大到 dst使用的填充区photoPaint

                canvas.drawText("00:" + String.format("%02d", recRec.get(i).Period), 60, height - 10, photoPaint);

                TextPaint textPaint = new TextPaint();
                //textPaint.setARGB(0xFF, 0xFF, 0, 0);
                textPaint.setTextSize(30.0F);
                textPaint.setTypeface(SANS_SERIF);
                String aboutTheGame = recRec.get(i).Title;
                if(aboutTheGame.length() > 17)
                    aboutTheGame = aboutTheGame.substring(0,16) + "...";
/** * aboutTheGame ：要 绘制 的 字符串 ,textPaint(TextPaint 类型)设置了字符串格式及属性 的画笔,240为设置 画多宽后 换行，后面的参数是对齐方式... */
                StaticLayout layout = new StaticLayout(aboutTheGame, textPaint, width - height - 10, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
//从 (20,80)的位置开始绘制

                canvas.translate(height, 20);
                canvas.save(Canvas.ALL_SAVE_FLAG);
                layout.draw(canvas);
                tempDis.setImageBitmap(icon);
            }


        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if (getSupportActionBar() != null){
        //    getSupportActionBar().hide();
        //}
        verifyStoragePermissions(this);

        list.add("目标");
        list.add("随时");
        list.add("退出");

        FirstLayout();


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

                    ((TextView)(findViewById(R.id.Time))).setText("00:" + String.format("%02d", calcuTime(Math.toDegrees(angle))/2));

                    break;
                case MotionEvent.ACTION_UP:

                    int time = calcuTime(Math.toDegrees(angle))/2;
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



