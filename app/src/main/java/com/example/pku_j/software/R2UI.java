package com.example.pku_j.software;

/**
 * Created by pku_j on 2017/4/7.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SlidingDrawer;
public class R2UI extends Activity {
    private GridView gv;
    private SlidingDrawer sd;
    private ImageView iv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recommendlay);

        sd = (SlidingDrawer) findViewById(R.id.sliding);
        ImageView up = (ImageView) findViewById(R.id.handle);
    }
}