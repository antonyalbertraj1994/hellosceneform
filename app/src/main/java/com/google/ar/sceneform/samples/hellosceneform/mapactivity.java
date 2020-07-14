package com.google.ar.sceneform.samples.hellosceneform;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class mapactivity extends AppCompatActivity {
    public static int touch_x;
    public static int touch_y;
    public static int x_touch,y_touch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapactivity);
        FrameLayout layout=(FrameLayout)findViewById(R.id.framelayout);

        final View view=new canvasview(this);
        layout.addView(view);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Log.d("touched","touched");
                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    //touch_x.add((int)event.getX());
                    //touch_y.add((int)event.getY());
                    touch_x=(int)event.getX();
                    touch_y=(int)event.getY();
                    x_touch=(int)event.getX();
                    y_touch=(int)event.getY();
                    //touchlist=dist(x_touch,y_touch,50);
                    view.invalidate();
                    return true;

                }
                return false;
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        View decorView=getWindow().getDecorView();
        if(hasFocus){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    }

