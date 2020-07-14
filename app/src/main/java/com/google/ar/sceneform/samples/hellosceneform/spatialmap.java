package com.google.ar.sceneform.samples.hellosceneform;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

import org.bytedeco.opencv.opencv_core.Mat;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class spatialmap extends AppCompatActivity {
    public static int touch_x;
    public static int touch_y;

    public static int x_base = 250, y_base = 600;
    List<Integer> x_file = new ArrayList<>(20);
    List<Integer> y_file = new ArrayList<>(20);
    public static int x_touch, y_touch;
    private Button eraser, count,optimallayout;
    public static int image_width = 0, image_height = 0;
    public static long timeleft = 60000;
    public static int erasermode = 0, counter = 0;
    private CountDownTimer countDownTimer;
    private Mat inputimage;
    private Resources mResources;
    public static Bitmap bmp1;
    public static List<Integer> image_x = new ArrayList<>(20);
    public static List<Integer> image_y = new ArrayList<>(20);
    private ImageView imageView;
    public static List<Boolean> touchlist = new ArrayList<>(20);
    private int drag_x_start = 0, drag_y_start = 0, drag_x_end = 0, drag_y_end = 0;
    private float mScalefactor = 1.f;
    private View view;
    private SeekBar zoommap;
    public static int zoomscale = 1;
    private static Bitmap bmp;
    public static int length_index_recv = 0;
    public static String[] configfit;

    private Button forward_disp,back_disp;
    private ScaleGestureDetector mScaleDetector;
    private static final Logger logger = Logger.getLogger(canvasview.class.getName());
    private static String disp_recv_config;

    private Context context;
    public static int imageid;
    public static int dispcounter=0;
    private List<Integer> intent_val;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spatialmap);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        //Add the canvasmap.java canvas to the framelayout
        FrameLayout layout = (FrameLayout) findViewById(R.id.framelayout);
        //mycanvas = new Canvas();
        view = new spatialmapcanvas(this);
        layout.addView(view);



        //Implement the dragging of the image on the canvas
        imageView = (ImageView) findViewById(R.id.image_disp);
        imageView.setX(x_base);
        imageView.setY(y_base);

        imageView.setVisibility(ImageView.INVISIBLE);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                    // v.startDragAndDrop(null, shadowBuilder, v, 0);
                    // v.startDrag(null, shadowBuilder, v, 0);
                    drag_x_start = (int) event.getX();
                    drag_y_start = (int) event.getY();
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    drag_x_end = (int) event.getX();
                    drag_y_end = (int) event.getY();
                    Log.d("Drag", "TouchMove");
                    int x_mov = drag_x_end - drag_x_start;
                    int y_mov = drag_y_end - drag_y_start;
                    imageView.setX(x_base + x_mov);
                    imageView.setY(y_base + y_mov);
                    x_base = x_base + x_mov;
                    y_base = y_base + y_mov;

                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    return true;
                }
                return false;
            }
        });
    }
}
