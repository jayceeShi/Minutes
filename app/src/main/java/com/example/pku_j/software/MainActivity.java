package com.example.pku_j.software;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.os.Handler;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import static android.graphics.Typeface.SANS_SERIF;


public class MainActivity extends AppCompatActivity {
    Calendar last_time = Calendar.getInstance();
    private float startx, starty;
    private float centerx, centery;
    private float radius = 180.0f;
    private Bitmap baseBitmap;
    public Canvas canvas;
    private ImageView clock;
    private MsgService msgService;
    private int timeT;
    private int canUrl = 0;
    private EditText TopicEndTime;
    private DeletableAdapter adapter;
    private ArrayList<Recommendation> recRec = null;
    private int endTime;
    private String myTask, myTime;
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
    private ArrayList<String> listCreate_name = new ArrayList<String>();
    private String url;
    public ImageView dis;
    private TextView name;
    private Handler handler=null;
    private Handler handleri = null;
    private PopupWindow window = null;
    private boolean hasTask = true;
    private boolean doTask = false;
    ServiceConnection conn;
    private SlidingDrawer sd;
    private int mYear, mMonth, mDay;
    private boolean isChecked= false;

    public void updateList(){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                listCreate.clear();
                listCreate_name.clear();
                ArrayList<Topic> can = msgService.getTopics();
                for(int i = 0; i < can.size(); i++){
                    Topic q = can.get(i);
                    if(q.Enable){
                        listCreate_name.add(q.Tag);
                        String name = "#" + q.Tag + "," +  (100 * q.ViewedCount/q.Count) + "%" ;
                        listCreate.add(name);
                    }

                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (Exception ex) {
            Log.e("trace~", "updateList: " + ex);
        }
    }

    public void FirstLayout(){
        setContentView(R.layout.recommendlay);
        baseBitmap = null;

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


        clock.setOnTouchListener(new PicOnTouchListener());

        name = (TextView)findViewById(R.id.Name);
        name.setText("命运决定谁会进入我们的生活，内心决定我们与谁并肩。");

        Log.v("trace~",""+this.getWindowManager().getDefaultDisplay().getHeight());
        dis=(ImageView)(findViewById(R.id.display));


        dis.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //dis.setImageBitmap(bmp);
        dis.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(canUrl != 0) {


                    Uri uri = Uri.parse(url);
                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(it);
                    msgService.markAsViewed(url);

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
                    msgService.markAsViewed(recRec.get(1).DeepLink);
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
                    msgService.markAsViewed(recRec.get(2).DeepLink);
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
                    msgService.markAsViewed(recRec.get(3).DeepLink);
                }
            }
        });
        tmpRec = (ImageView)findViewById(R.id.rec4);
        tmpRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canUrl != 0) {
                    msgService.markAsViewed(recRec.get(4).DeepLink);
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
                    msgService.markAsViewed(recRec.get(5).DeepLink);
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
                    msgService.markAsViewed(recRec.get(6).DeepLink);
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
                    msgService.markAsViewed(recRec.get(7).DeepLink);
                    Uri uri = Uri.parse(recRec.get(7).DeepLink);
                    Intent it = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(it);
                }
            }
        });


        menu = (Button)(findViewById(R.id.Menu));
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewLayout();
            }
        });
        //spin = (Spinner)(findViewById(R.id.spinner));
        /*menu.setOnTouchListener(new View.OnTouchListener() {
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
*/
    }
    public void ViewLayout(){
        setContentView(R.layout.view);
        /*if(window.isShowing()) {
            window.dismiss();
        }*/

        updateList();

        ImageView left = (ImageView)findViewById(R.id.left);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirstLayout();
            }
        });
/*        menu = (Button)(findViewById(R.id.Menu));
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

        });*/
        adapter = new DeletableAdapter(this, listCreate);
        ListView listCreated = (ListView)findViewById(R.id.created);
        listCreated.setAdapter(adapter);
        ImageView create = (ImageView)findViewById(R.id.create);
        //listCreate.add("#马刺，每天");
        //adapter.notifyDataSetChanged();
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TopicLayout();
            }
        });




    }
    public void TopicLayout() {
        setContentView(R.layout.topic);
        /*if(window != null && window.isShowing())
            window.dismiss();
        if(window != null && window.isShowing())
            window.dismiss();
        if(window != null && window.isShowing())
            window.dismiss();
*/
        ImageView leftb = (ImageView)findViewById(R.id.leftM);
        leftb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewLayout();
            }
        });
        ImageView createT = (ImageView)findViewById(R.id.createT);


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

        final TextView time1 = (TextView)findViewById(R.id.time1);
        final TextView time2 = (TextView)findViewById(R.id.time2);
        final TextView time3 = (TextView)findViewById(R.id.time3);
        final TextView time4 = (TextView)findViewById(R.id.time4);
        final TextView time5 = (TextView)findViewById(R.id.time5);
        final CheckBox cb = (CheckBox)this.findViewById(R.id.checkBox);
        time1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                time1.setBackgroundColor(Color.parseColor("#bca590"));
                time5.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time2.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time3.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time4.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE,7);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                mMonth = c.get(Calendar.MONTH);
                mYear = c.get(Calendar.YEAR);
                setCTime(mYear,mMonth,mDay);
                last_time = c;
                myTime = ",0%";
                isChecked = false;
                cb.setChecked(isChecked);
            }
        });

        time2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                time2.setBackgroundColor(Color.parseColor("#bca590"));
                time1.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time5.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time3.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time4.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE,20);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                mMonth = c.get(Calendar.MONTH);
                mYear = c.get(Calendar.YEAR);
                setCTime(mYear,mMonth,mDay);
                last_time = c;
                myTime = ",0%";
                isChecked = false;
                cb.setChecked(isChecked);
            }
        });
        time3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                time3.setBackgroundColor(Color.parseColor("#bca590"));
                time1.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time2.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time5.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time4.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                Calendar c = Calendar.getInstance();
                c.add(Calendar.MONTH, 1);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                mMonth = c.get(Calendar.MONTH);
                mYear = c.get(Calendar.YEAR);
                setCTime(mYear,mMonth,mDay);

                last_time =  c;

                myTime = ",0%";
                isChecked = false;
                cb.setChecked(isChecked);
            }
        });
        time4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                time4.setBackgroundColor(Color.parseColor("#bca590"));
                time1.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time2.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time3.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time5.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                Calendar c = Calendar.getInstance();
                c.add(Calendar.MONTH, 3);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                mMonth = c.get(Calendar.MONTH);
                mYear = c.get(Calendar.YEAR);
                setCTime(mYear,mMonth,mDay);

                last_time = c;

                myTime = ",0%";
                isChecked = false;
                cb.setChecked(isChecked);
            }
        });



        time5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                time5.setBackgroundColor(Color.parseColor("#bca590"));
                time1.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time2.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time3.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time4.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                Calendar c = Calendar.getInstance();
                c.add(Calendar.YEAR, 1);
                mDay = c.get(Calendar.DAY_OF_MONTH);
                mMonth = c.get(Calendar.MONTH);
                mYear = c.get(Calendar.YEAR);
                setCTime(mYear,mMonth,mDay);
                myTime = ",0%";
                isChecked = false;

            }
        });
        cb.setChecked(isChecked);

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                    time1.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                    time2.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                    time3.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                    time4.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                    time5.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.YEAR,100);
                    mDay = c.get(Calendar.DAY_OF_MONTH);
                    mMonth = c.get(Calendar.MONTH);
                    mYear = c.get(Calendar.YEAR);
                    setCTime(mYear,mMonth,mDay);
                    myTime = ",每天";
                }
                else{
                    myTime = ",0%";
                }
            }
        });

        final TextView tag1 = (TextView)findViewById(R.id.tag1);
        final TextView tag2 = (TextView)findViewById(R.id.tag2);
        final TextView tag3 = (TextView)findViewById(R.id.tag3);
        final TextView tag4 = (TextView)findViewById(R.id.tag4);

        tag1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tag1.setBackgroundColor(Color.parseColor("#bca590"));
                myTask = "machine learning";
                tag2.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                tag3.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                tag4.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
            }
        });

        tag2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tag2.setBackgroundColor(Color.parseColor("#bca590"));
                myTask = "fitness";
                tag1.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                tag3.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                tag4.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
            }
        });

        tag3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tag3.setBackgroundColor(Color.parseColor("#bca590"));
                myTask = "ArtHistory";
                tag2.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                tag1.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                tag4.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
            }
        });

        tag4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tag4.setBackgroundColor(Color.parseColor("#bca590"));
                myTask = "IslandTravel";
                tag2.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                tag3.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                tag1.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
            }
        });
        createT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cont = "#" + myTask + myTime;

                listCreate_name.add(myTask);
                listCreate.add(cont);
                //adapter.notifyDataSetChanged();
                tag1.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                tag2.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                tag3.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                tag4.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time1.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time2.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time3.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time4.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));
                time5.setBackgroundDrawable(getResources().getDrawable(R.drawable.tagback));

                isChecked = false;

                try {
                    addtopic();
                } catch (InterruptedException e) {

                    Log.v("trace~", e.toString());
                }

                ViewLayout();
            }
        });
    }
    public void addtopic() throws InterruptedException {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                msgService.addTopicTag(myTask, last_time.getTime());
            }
        });
        t.start();
        t.join();
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
                    removetag(index);
                    Toast.makeText(context, textView.getText().toString() + " 已删除",
                            Toast.LENGTH_SHORT).show();
                }
            });
            return view;
        }
    }
    public void removetag(final int index){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                msgService.removeTopicTag(listCreate_name.get(index));
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void setCTime(int year, int monthOfYear, int dayOfMonth){

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

    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar c = Calendar.getInstance();
                    c.set(year, monthOfYear, dayOfMonth);
                    last_time =  c;
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

    Runnable   runnableUi=new  Runnable() {
        @Override
        public void run() {
            canUrl = 1;

            url = recRec.get(0).DeepLink;
            Log.v("trace~", url);
            canUrl = 1;
            ImageView logo = (ImageView) findViewById(R.id.logo);
            TextView txt = (TextView) findViewById(R.id.Rtime);
            Bitmap logo2;
            txt.setText("00:" + String.format("%02d", recRec.get(0).Period));
            TextView title = (TextView) findViewById(R.id.Name);
            String titleS = recRec.get(0).Title;
            if (titleS.length() > 20)
                titleS = titleS.substring(0, 19) + "...";
            title.setText(titleS);

            switch (recRec.get(0).Source) {

                case "jd":
                case "taobao":
                    //dis.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.shopping));
                    logo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.shopping_icon));
                break;

                case "le":
                case "wangyi":
                case "youku":
                    //dis.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.video));
                    logo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.video_icon));
                break;

                case "zhihu":
                case "toutiao":
                case "douban":
                case "wechat":
                case "xiecheng":
                    //dis.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.video));
                    logo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.news_icon));
                break;
                case "xmly":
                    logo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.music));
                    break;
                default:
                    //dis.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.video));
                    logo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.keep));
                break;
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

                ImageView tempDiss = (ImageView) findViewById(R.id.rec1);
                LayoutParams para = tempDiss.getLayoutParams();


                int height = para.height;

                int width = para.width;
                for (int i = 1; i <= 7; i++) {
                    Log.v("trace@", "into trace " + i + " " + recRec.get(i).Source);
                    Bitmap setLogo;
                    ImageView tempDis = (ImageView) findViewById(R.id.rec1);

                    switch (i) {
                        case 1:
                            tempDis = (ImageView) findViewById(R.id.rec1);
                            break;
                        case 2:
                            tempDis = (ImageView) findViewById(R.id.rec2);
                            break;
                        case 3:
                            tempDis = (ImageView) findViewById(R.id.rec3);
                            break;
                        case 4:
                            tempDis = (ImageView) findViewById(R.id.rec4);
                            break;
                        case 5:
                            tempDis = (ImageView) findViewById(R.id.rec5);
                            break;
                        case 6:
                            tempDis = (ImageView) findViewById(R.id.rec6);
                            break;
                        case 7:
                            tempDis = (ImageView) findViewById(R.id.rec7);
                            break;
                    }

                    Bitmap icon = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); //建立一个空的图画板
                    Canvas canvas = new Canvas(icon);//初始化画布绘制的图像到icon上

                    Paint photoPaint = new Paint(); //建立画笔
                    Paint photoPaint2 = new Paint();
                    photoPaint2.setTextSize(40);
                    //paint.setTypeface();
                    Rect dst = new Rect(30, 15, height - 30, height - 35);//创建一个指定的新矩形的坐标
                    Log.v("trace@", "into trace second " + i + " " + recRec.get(i).Source);

                    switch (recRec.get(i).Source) {
                        case "jd":
                            canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.shopping_icon), null, dst, photoPaint);
                            break;
                        case "youku":
                        case "le":
                        case "wangyi":
                            canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.video_icon), null, dst, photoPaint);
                            break;
                        case "xmly":
                            canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.music), null, dst, photoPaint);
                            break;
                        case "zhihu":
                        case "toutiao":
                        case "douban":
                        case "wechat":
                        case "xiecheng":
                            canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.news_icon), null, dst, photoPaint);
                            break;
                        default:
                            canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.keep), null, dst, photoPaint);
                            break;
                    }

                    //canvas.drawBitmap(recRec.get(i).getThumbnail(), null, dst, photoPaint);
                    Log.v("trace@", "into trace third " + i + " " + recRec.get(i).Source);
                    //将photo 缩放或则扩大到 dst使用的填充区photoPain
                    //canvas.drawText("new recommendation to do ahhhhhhhhhh", height, height/2, photoPaint2);//将photo 缩放或则扩大到 dst使用的填充区photoPaint
                    photoPaint.setTextSize(20.0F);
                    canvas.drawText("00:" + String.format("%02d", recRec.get(i).Period), 45, height - 15, photoPaint);

                    TextPaint textPaint = new TextPaint();
                    //textPaint.setARGB(0xFF, 0xFF, 0, 0);
                    textPaint.setTextSize(30.0F);
                    textPaint.setTypeface(SANS_SERIF);
                    String aboutTheGame = recRec.get(i).Title;
                    if (aboutTheGame.length() > 17)
                        aboutTheGame = aboutTheGame.substring(0, 16) + "...";
/** * aboutTheGame ：要 绘制 的 字符串 ,textPaint(TextPaint 类型)设置了字符串格式及属性 的画笔,240为设置 画多宽后 换行，后面的参数是对齐方式... */
                    StaticLayout layout = new StaticLayout(aboutTheGame, textPaint, width - height - 10, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
//从 (20,80)的位置开始绘制

                    canvas.translate(height, 30);
                    canvas.save(Canvas.ALL_SAVE_FLAG);
                    layout.draw(canvas);
                    tempDis.setImageBitmap(icon);



            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doTask = false;
        //if (getSupportActionBar() != null){
        //    getSupportActionBar().hide();
        //}
        verifyStoragePermissions(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        list.add("目标");
        list.add("随时");
        list.add("退出");

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
                            Log.e("trace~","have got recommendation");
                            recRec = msgService.getRec();
                            for(int i = 0; i < 1; i++) {
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
        Paint paint;
        @Override
        public boolean onTouch(View v, MotionEvent e) {
            switch (e.getAction()) {
                // 用户按下动作
                case MotionEvent.ACTION_DOWN:
                    paint = new Paint();
                    paint.setStrokeWidth(1);
                    paint.setColor(Color.RED);
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

            //handler.post(runnableUi);
            //handleri.post(runnableUii);



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



