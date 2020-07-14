/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.hellosceneform;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.PlaneRenderer;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.gson.Gson;
import com.quickbirdstudios.yuv2mat.Yuv;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class HelloSceneformActivity extends AppCompatActivity {
    private static final String TAG = HelloSceneformActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;
    double elapsedtime=0.0;
    long start_time,end_time;
    private ArFragment arFragment;
    private ModelRenderable andyRenderable;
    private boolean modelplaced=false;
    public static float[] translation,prev_translation;
    private double[] trans=new double[3];
    private double[] rotval=new double[4];

    public int startsaving=0;
    public static float[] rotation;
    public static double sum_x,sum_y;
    double prev_x=0.0,prev_y=0.0,prev_sum_x,prev_sum_y;
    int x_touch,y_touch,counter=0;
    List<Integer> hashcodelist=new ArrayList<>(20);
    public static List<Integer> x_map=new ArrayList<>(20);
    public static List<Mat> frames=new ArrayList<>(20);

    public static List<Integer> y_map=new ArrayList<>(20);
    public static int touched=0,tracking_start=0;
    public static int bitcounter=0,tracker=0,pixelid_tracking=0;
    Mat mRGBA,mRGBAT,mRGB,gray_thresh,res;
    public static final String uuid1="00001101-0000-1000-8000-00805f9b34fb";
    public static Mat frameroi;
    public BluetoothAdapter btAdapter;
    public static BluetoothSocket btSocket;
    public static List<Integer> touch_x=new ArrayList<>(20);
    public static List<Double> framesep_graph=new ArrayList<>(20);
    public static List<Double> timetracker=new ArrayList<>(20);
    public static List<Double> blue_timetracker=new ArrayList<>(20);
    public static int new_start=0,thresh;
    public static double sep1=0.0;
    //public static int contour_found=0;
    public static List<Integer> touch_y=new ArrayList<>(20);
    public static List<Integer> framesep=new ArrayList<>(20);
    public static int frameno=0,started=0,track_val=0,max_track_val,sum=0,pixel_touched=0,x_touch1,y_touch1,tracklocation=0;
    BluetoothDevice hc05;
    private TextView pixelid;
    private static int x_cont1=0,y_cont1=0;
    public static int captureid=0;
    private Button start;
    JavaCameraView javaCameraView;
    public static int bitsize=10,framecount=0; //not bits sent
    List<Integer> x_cont=new ArrayList<>(5);
    List<Integer> y_cont=new ArrayList<>(5);
    List<Integer> index=new ArrayList<>(5);
    public static int max=0,max_index=0,min=255,min_index=0;
    public static double center_index=0;
    public static int thresh_cont=255,contour_found=0;
    public static int displaystart=0;
    private ImageView imageView;
    private Button graphview;
    public static float[] projmtx = new float[16];

    public static double threshstart_index=0,threshend_index=0,threshstart_int=0,threshend_int=0;
    private Button mapbutton,calibbutton;
    static
    {
        if(OpenCVLoader.initDebug()){
            Log.e("opencvconfig","Opencv is configured");
        }
        else{
            Log.e("opencvconfig","opencvfailed");
        }
    }

    BaseLoaderCallback baseLoaderCallback=new BaseLoaderCallback(HelloSceneformActivity.this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS: {
                   // javaCameraView.enableView();
                    break;
                }
                default:
                {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };
      //  @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  // CompletableFuture requires api level 24
  // FutureReturnValueIgnored is not valid

    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!checkIsSupportedDeviceOrFinish(this)) {
      return;
    }

//    jsonformat1.name="antony";
//    jsonformat1.age="25";
//
//    jsonformat2.name="albert";
//    jsonformat2.age="26";



    erase(new File("/storage/emulated/0/scatter/calibmap.txt"));

    //sb.append(jsonformat1);
   // sb.append(jsonformat2);


      setContentView(R.layout.activity_ux);
    //bluetoothstart();

    mapbutton=(Button)findViewById(R.id.map);
    mapbutton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //displaystart=0;
            //imageView.setVisibility(View.INVISIBLE);
        }
    });

    start=(Button)findViewById(R.id.start);
    start.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            start.setText("IDFIND");
            pixelid_tracking=1;
            pixel_touched=0;
        }
    });

    calibbutton=(Button)findViewById(R.id.calibrate);
    calibbutton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Gson gson=new Gson();
            StringBuilder sb=new StringBuilder();
            jsonformat jsonformat1=new jsonformat();
            jsonformat jsonformat2=new jsonformat();
            String rotval=rotation[0]+","+rotation[1]+","+rotation[2]+","+rotation[3];
            String transval=translation[0]+","+translation[1]+","+translation[2];
            jsonformat1.quaternion=rotation;
            jsonformat1.translation=translation;
            String g=gson.toJson(jsonformat1);
            String g1=gson.toJson(jsonformat2);
            captureid++;
           // createjson(getApplicationContext(), new File("/storage/emulated/0/scatter/calibmap.txt"),g);
            //createjson(getApplicationContext(), new File("/storage/emulated/0/scatter/calibmap.txt"),g1);
              opencalibactivity();
           // calibration_recordstart();
        }
    });


    graphview=(Button)findViewById(R.id.graphview);
    graphview.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            int len=frames.size();
//            if(framecount<len) {
//                show_framebyframe(framecount, 240, 240, 10);
//                framecount++;
//            }
//            else {
//                framecount=0;
//            }


            //graphview();
            if(pixelid_tracking==0){
                openmapactivity();
            }
            else {
                File file = new File("/storage/emulated/0/scatter/calibmap.txt");
                save(file);
                openmapactivity();
            }
        }
    });

    imageView=(ImageView)findViewById(R.id.imageView);
    imageView.setVisibility(View.INVISIBLE);
    pixelid=(TextView)findViewById(R.id.pixelid);
    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
    arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdate);



      // When you build a Renderable, Sceneform loads its resources in the background while returning
    // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
    ModelRenderable.builder()
        .setSource(this, R.raw.andy)
        .build()
        .thenAccept(renderable -> andyRenderable = renderable)
        .exceptionally(
            throwable -> {
              Toast toast =
                  Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
              toast.setGravity(Gravity.CENTER, 0, 0);
              toast.show();
              return null;
            });
      int[] viewcords=new int[2];
      imageView.getLocationOnScreen(viewcords);
      Log.d("view_x",String.valueOf(viewcords[0]));
      Log.d("view_y",String.valueOf(viewcords[1]));


      arFragment.getArSceneView().setOnTouchListener((view,motionEvent)-> {
        Log.d("ArFragment", "Touched");
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            x_touch = (int) motionEvent.getX();
            y_touch = (int) motionEvent.getY();
            tracking_start=1;
            pixel_touched=1;
            touch_x.clear();
            framesep.clear();
            framesep_graph.clear();
            timetracker.clear();
            blue_timetracker.clear();
            frameno=0;
            max_track_val=0;
            started=1;
            tracklocation=1;
            touch_y.clear();
            return true;
        }
        return false;

    });
      File file1 = new File("/storage/emulated/0/scatter/map.txt");
      erase(file1);

    }
    private void createjson(Context context,File fileName,String jsonString){
      String FILENAME="spatialmap.json";
       String seperator= System.getProperty("line.separator");
      try{
          FileOutputStream fos=new FileOutputStream(fileName,true);
          if(jsonString!=null){
              fos.write(jsonString.getBytes());
              fos.write(seperator.getBytes());
          }
          fos.close();
      }catch (FileNotFoundException fileNotFound){
      }catch (IOException ioException){
      }
    }

    private void erase(File file){
        file.delete();
        Log.d("filedelete","Success");
    }


    public void calibration_recordstart()
    {
        List<Integer> sample=new ArrayList<>();
        sample.add((int)(sum_x*100));
        sample.add((int)(sum_y*100));

        Intent intent=new Intent(this,calibration3_record.class);
        List<Integer> new_selectedimages=sample.stream().distinct().collect(Collectors.toList());
        sample.clear();
        intent.putIntegerArrayListExtra("list",(ArrayList<Integer>)new_selectedimages);
        startActivity(intent);
    }
    private void opencalibactivity(){
        Intent intent=new Intent(this,calibration3_record.class);
        startActivity(intent);
    }

    private void bluetoothstart(){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d("connecteddevice", String.valueOf(btAdapter.getBondedDevices()));

        hc05 = btAdapter.getRemoteDevice("98:D3:31:20:9B:EB");

        if(!btAdapter.isEnabled()){
            Intent enableIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,3);
            Log.d("connnected","btadapter is not enabled");
        }else{
            Log.d("connected","startingthread");

            ConnectThread connectThread=new ConnectThread(hc05);
            connectThread.start();
        }
    }
    private void openmapactivity(){
        Intent intent=new Intent(this,mapactivity.class);
        startActivity(intent);
    }

    private void graphview(){
        Intent intent=new Intent(this,graphview.class);
        startActivity(intent);
    }

    private void onUpdate(FrameTime frameTime) {
        int x1=1132,y1=251,x2=1143,y2=516,x3=1528,y3=738;
        Frame frame = arFragment.getArSceneView().getArFrame();
        List<HitResult> hit1=frame.hitTest(1080-y1,x1);
        List<HitResult> hit2=frame.hitTest(1080-y2,x2);
        List<HitResult> hit3=frame.hitTest(1080-y3,x3);

        if(hit1.size()>0) {
            HitResult closest1=findclosesthit(hit1);
            Pose hit_pose=closest1.getHitPose();
            float[] g = hit_pose.getTranslation();
            String send = g[0] + "," + g[1] + "," + g[2];
            Log.d("hitresult1", send);
        }

        if(hit2.size()>0) {
            HitResult closest1=findclosesthit(hit2);
            Pose hit_pose=closest1.getHitPose();
            float[] g = hit_pose.getTranslation();
            String send = g[0] + "," + g[1] + "," + g[2];
            Log.d("hitresult2", send);
        }

        if(hit3.size()>0) {
            HitResult closest1=findclosesthit(hit3);
            Pose hit_pose=closest1.getHitPose();
            float[] g = hit_pose.getTranslation();
            String send = g[0] + "," + g[1] + "," + g[2];
            Log.d("hitresult3", send);
        }

        Camera camera=frame.getCamera();
        Image image=null;
        float[] focal=camera.getImageIntrinsics().getFocalLength();
        float[] princ=camera.getImageIntrinsics().getPrincipalPoint();
        float[] projectionmatrix=new float[16];
        camera.getProjectionMatrix(projmtx,0,0.01f,100.0f);

        Mat rgb = new Mat(640, 480, CvType.CV_8UC3);
        Mat hsv = new Mat(640, 480, CvType.CV_8UC3);
        Mat hsv_1080 = new Mat(2240, 1080, CvType.CV_8UC3);

        int w=480,h=640;

        try {

            //Get the image from the ARcore output into RGB
            image=frame.acquireCameraImage();
            ByteBuffer buffer=image.getPlanes()[0].getBuffer();
            byte[] bytes=new byte[buffer.remaining()];
            buffer.get(bytes);
            rgb.put(0,0,bytes);
            rgb=Yuv.rgb(image);
            rgb.copyTo(hsv);
            Core.rotate(hsv,hsv,0);

            //Scale the values from the touch screen to the output image from ARcore
            double x_new=(double) ((x_touch*480.0)/1080.0);
            double y_new=(double) ((y_touch*640.0)/2159.0);
            x_touch1=(int)x_new;
            y_touch1=(int)y_new;

            if(started==1) {
                Log.d("PixelData","Sending");
                bluetooth_send("2\n");
                frames.clear();
                started=0;
                start_time=System.currentTimeMillis();
            }

            if(pixel_touched==1 && startsaving==1) {
                frames.add(hsv);
            }


        } catch (NotYetAvailableException e) {
            e.printStackTrace();
        }

        if(image!=null) {

            image.close();
        }
        if(camera.getTrackingState()==TrackingState.TRACKING && pixelid_tracking==1){
            Log.d("LocationTracking","Started");
            Pose pose=frame.getAndroidSensorPose();
            Pose a=pose.extractTranslation();
            Pose b=pose.extractRotation();

            rotation=b.getRotationQuaternion();
            translation=a.getTranslation();

//            double trans_x=Math.abs(translation[2]);
//            double trans_y=Math.abs(translation[0]);

            double trans_x=translation[2];
            double trans_y=translation[0];

            double delta_x=trans_x-prev_x;
            double delta_y=trans_y-prev_y;
            sum_x = sum_x + delta_x;
            sum_y = sum_y + delta_y;

            prev_x=trans_x;
            prev_y=trans_y;
            prev_sum_x = sum_x;
            prev_sum_y = sum_y;
            Log.d("translation_x",String.valueOf(trans_x));
            Log.d("translation_y",String.valueOf(trans_y));
          //  Log.d("translation_z",String.valueOf(trans_z));


            if(tracklocation==1){
                x_map.add((int)Math.abs(sum_x*100));
                y_map.add((int)Math.abs(sum_y*100));
                x_cont.add((int)Math.abs(sum_x*100));
                y_cont.add((int)Math.abs(sum_y*100));
                tracklocation=0;
            }

            Log.d("Sum_x_y",String.valueOf(sum_x*100)+","+String.valueOf(sum_y*100));
            Log.d("Trans_x_y",String.valueOf(trans_x)+","+String.valueOf(trans_y));
            String a_text=String.valueOf((int)(sum_x*100))+","+String.valueOf((int)(sum_y*100));
            pixelid.setText(a_text);
        }
    }

    private HitResult findclosesthit(List<HitResult> hitResults){
      int i=0;
      for(HitResult hitResult:hitResults){
          i++;
          if(hitResult.getTrackable() instanceof Plane){
              Log.d("Planedetect",String.valueOf(i)+","+"plane");
              return hitResult;
          }
      }
      return hitResults.get(0);
    }


    private int starframe(int boxsize) {
        int framelength = frames.size(); // length of the saved frames list
        int x1=(int)(x_touch1-boxsize/2); // X position for extracting the ROI
        int y1=(int)(y_touch1-boxsize/2); // Y position for extractuing the ROI
        int gradient = 0;
        double prev_tot_b = 0;
        int startfameno=0;

        //Opencv mat variables
        Mat diff_image = new Mat(boxsize, boxsize, CvType.CV_8UC1, new Scalar(0));
        Mat prev_image = new Mat(boxsize, boxsize, CvType.CV_8UC1, new Scalar(0));
        Mat gray = new Mat();

        for (int i = 0; i < framelength; i++) {
            Mat rgb = frames.get(i);
            Imgproc.cvtColor(rgb, gray, Imgproc.COLOR_BGR2GRAY);
            Mat gray_roi = new Mat(gray, new Rect(x1, y1, boxsize, boxsize));            // Extract the roi corresponding to the touch location
            Core.absdiff(prev_image, gray_roi, diff_image); // Frame diff to find the peak values for detecting the start frame
            gray_roi.copyTo(prev_image);

            //Finding the peak values of the sum of frame difference
            Scalar tot = Core.sumElems(diff_image);
            double tot_b = tot.val[0];
            if (tot_b > prev_tot_b && gradient != -1 && i > 0) {
                gradient = 1;
            }
            if (tot_b < prev_tot_b && gradient == 1) {
                startfameno=i;
                Log.d("Tracking_startframe", String.valueOf(startfameno));
                break;
            }
            if (i > 0) {
                prev_tot_b = tot_b;
            }
        }
        return startfameno;
    }

    // Find the ROI for extracting the gray scale values
    private List<Integer> find_roi(int boxsize,int startframeno,int threshold){
        startsaving=0;
        int x1=(int)(x_touch1-boxsize/2); // X position for extracting the ROI
        int y1=(int)(y_touch1-boxsize/2); // Y position for extractuing the ROI
        double maxarea=0;
        int x_cont=0,y_cont=0,rad_cont=0;// Variable to store the roi values of the contour

        // Threshold the image and then extract the only area bounded by the user touch for the next step of contour detection
        Mat gray=new Mat();
        Mat startframe=frames.get(startframeno);
        Imgproc.cvtColor(startframe, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(gray,gray,threshold,255,Imgproc.THRESH_BINARY);
        Mat startframe_roi=new Mat(gray,new Rect(x1,y1,boxsize,boxsize));

        //Contour detection to find the ROI for the scatterpixel
        ArrayList<MatOfPoint> contours=new ArrayList<>();
        Imgproc.findContours(startframe_roi,contours,new Mat(),Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
        Iterator<MatOfPoint> iterator=contours.iterator();
        Log.d("find_roi_contlen",String.valueOf(contours.size()));

        while(iterator.hasNext()){
            MatOfPoint contour=iterator.next();
            double area= Imgproc.contourArea(contour);
            if(area>maxarea) {
                maxarea=area;

                //Find the enclosing circle to fit the contour
                Point point_cent=new Point();
                float[] radius=new float[2];
                Imgproc.minEnclosingCircle(new MatOfPoint2f(contour.toArray()),point_cent,radius);
                x_cont=(int)point_cent.x;
                y_cont=(int)point_cent.y;
                rad_cont=(int)radius[0];
            }
        }

        // Add the base values to make it reference it to the normal image(640x480)
        x_cont=x1+x_cont;
        y_cont=y1+y_cont;
        Imgproc.rectangle(startframe,new Point(x1,y1),new Point(x1+boxsize,y1+boxsize),new Scalar(255,0,0),5);
        Imgproc.circle(startframe, new Point(x_cont,y_cont),(int)rad_cont , new Scalar(255, 0, 255), 4);
        //displayimage(startframe);

        List<Integer> roivalue= new ArrayList<>();
        roivalue.add(x_cont);
        roivalue.add(y_cont);
        roivalue.add(rad_cont);
        return roivalue;

    }


    private List<Integer> grayvalues(List<Integer> roivalue,int startframeno){
        int x_cont=roivalue.get(0);
        int y_cont=roivalue.get(1);
        int rad_cont=roivalue.get(2);
        int[] index_array=new int[]{0,3,6,9,12,15,18,21,24,27,30};
        List<Integer> index1=new ArrayList<>();
        for(int i=0;i<index_array.length;i++){
            index1.add(index_array[i]);
        }
        List<Integer> gray_intensity= new ArrayList<>(); // Store the values of the average roi values

        // Mask for extracting the gray values
        Mat roi_mask = new Mat(640, 480, CvType.CV_8UC1, new Scalar(0));//
        Imgproc.circle(roi_mask, new Point(x_cont,y_cont),(int)rad_cont , new Scalar(255, 255, 255), -1);
        int countnonzero=Core.countNonZero(roi_mask);
        Mat gray=new Mat();
        Mat gray_masked=new Mat();

        for(int i=0;i<frames.size();i++) {
          if(index1.contains(i-startframeno)) {
              Imgproc.cvtColor(frames.get(i), gray, Imgproc.COLOR_BGR2GRAY);
              Core.bitwise_and(gray, roi_mask,gray_masked);
              Scalar sum=Core.sumElems(gray_masked);
              int gray_average=(int)(sum.val[0]/countnonzero);
              gray_intensity.add(gray_average);
          }
        }

        Log.d("grayvalue_listsize",String.valueOf(gray_intensity.size()));
        return gray_intensity;
    }

    private double thresh_interp(double x1,double y1,double x2,double y2,double x_new){
        double slope=(double)((y2-y1)/(x2-x1));
        double y_new=slope*(x_new-x1)+y1;
        return y_new;
    }

    private void pixelid_decode(List<Integer> grayvalues){
        startsaving=0;
        int[] index1=new int[]{0,3,6,9,12,15,18,21,24,27,30};

        // Printing the intensity values for the 11 bits
        String intensity_test="";
        for(int i=0;i<grayvalues.size();i++){
            intensity_test+=String.valueOf(grayvalues.get(i));
        }
        Log.d("pixelid_intensity",intensity_test);


        //Find the threshold values of on/off at the start and end of the flashing sequence
        int thresh_start_on=(grayvalues.get(0));
        int thresh_start_off=(grayvalues.get(1));
        int thresh_end_off=(grayvalues.get(9));
        int thresh_end_on=(grayvalues.get(10));
        Log.d("Tracking_thresh",String.valueOf(thresh_start_on)+","+String.valueOf(thresh_start_off)+","+String.valueOf(thresh_end_off)+","+ String.valueOf(thresh_end_on));

        int[] a=new int[7];
        for(int i=0;i<7;i++){
            int interp_on=(int)(thresh_interp(index1[0],thresh_start_on,index1[10],thresh_end_on,index1[i+2]));
            int interp_off=(int)(thresh_interp(index1[1],thresh_start_off,index1[9],thresh_end_off,index1[i+2]));
            int thresh_tot=(int)(interp_off+interp_on)/2;
            Log.d("Tracking_thresh_tot",String.valueOf(thresh_tot));
            if(grayvalues.get(i+2)>thresh_tot){
                a[i]=1;
                Log.d("Tracking_state","1");
            }
            else{
                Log.d("Tracking_state","0");
                a[i]=0;
            }
        }

        //Find the decimal equivalent of the decoded binary flashing sequence
        sum=0;
        for(int gg=0;gg<7;gg++){
            sum+=(int)(a[6-gg]*Math.pow(2,gg));
            Log.d("pixel_id1",String.valueOf(Math.pow(2,gg)));
        }
        Log.d("pixel_id_sum",String.valueOf(sum));
       // imageView.setVisibility(View.VISIBLE);


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(sum>100){
                    pixelid.setText("Oops");
                }
                else {
                    index.add(sum);
                    pixelid.setText(String.valueOf(sum));

                }
            }
        });

    }


    private class ReadInput implements Runnable{
        private boolean bStop=false;
        private Thread t;
        public ReadInput(){
            t=new Thread(this,"InputThread");
            t.start();
        }
        public void run(){
            InputStream inputStream;
            try {
                inputStream=btSocket.getInputStream();
                while (!bStop){
                    byte[] buffer=new byte[300];
                    if(inputStream.available()>0){
                        int bytes=inputStream.read(buffer);
                        final String message=new String(buffer,0,bytes);
                        int len=message.length();
                        Log.d("recieved",String.valueOf(len));
                        Log.d("recieved",message);
                        if(!message.isEmpty()) {
                            bitcounter++;
                            if(bitcounter==1){
                                startsaving=1;
                            }
                            framesep.add(frameno);
                            double elapse1=System.currentTimeMillis()-start_time;
                            framesep_graph.add((double)frameno);

                            blue_timetracker.add(elapse1/1000);
                            Log.d("frameno", String.valueOf(frameno));
                            Log.d("length", String.valueOf(touch_x.size()));

                            if (bitcounter == 2) {
                                tracker = 0;
                                Log.d("recieved", "videostopped");
                                pixel_touched = 0;
                                bitcounter=0;
                                framecount=0;

                                int startframeno=starframe(100); // Find the start frame
                                List<Integer> temp_roi=find_roi(100,startframeno,210); //Use the start frame to find roi
                                List<Integer> grayvalues=grayvalues(temp_roi,startframeno); // Track the gray values within the roi
                                pixelid_decode(grayvalues);
                                //frames.clear();
                                //graphview();

                            }

                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void stop(){
            bStop=true;
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket thisSocket;
        private final BluetoothDevice thisdevice;
        public ConnectThread(BluetoothDevice device){
            BluetoothSocket tmp=null;
            thisdevice=device;
            try{
                tmp=thisdevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString(uuid1));
            }catch (IOException e){
                Log.d("connecteddevice","cannot connected toservice");
            }
            thisSocket=tmp;

        }
        public void run(){
            btAdapter.cancelDiscovery();
            try{

                thisSocket.connect();
                Log.d("connected","antonytest");

                Log.d("connected","connected to hc-05");
            }catch (IOException e){
                try {
                    thisSocket.close();
                }catch (IOException e1){
                    e1.printStackTrace();
                }
                return;
            }
            btSocket=thisSocket;
            ReadInput r1=new ReadInput();
            r1.run();
        }

        public void cancel(){
            try{
                thisSocket.close();
            }catch (IOException e){
                Log.d("connected","cannot close socket");
            }
        }

    }

    public void save(File filename) {
        //   int[] a = {1, 2, 3, 4, 5,6,7,8,9};
        // int[] x = {10, 100, 200, 300, 400,500,600,700,800,900};
        // int[] y = {10, 100, 200, 300, 400,500,600,700,800,900};
        FileOutputStream fos = null;
        BufferedWriter bw = null;
        try {
            FileWriter fw = new FileWriter(filename);
            bw = new BufferedWriter(fw);
            //bw.write("albert");
            for (int i = 0; i < index.size(); i++) {
                bw.write(String.valueOf(index.get(i)));
                if (i < (index.size() - 1)) {
                    bw.write(",");
                }
            }
            bw.newLine();
            for (int i = 0; i < x_cont.size(); i++) {
                bw.write(String.valueOf(x_cont.get(i)));
                if (i < (x_cont.size() - 1)) {
                    bw.write(",");
                }
            }
            bw.newLine();

            for (int i = 0; i < y_cont.size(); i++) {
                bw.write(String.valueOf(y_cont.get(i)));
                if (i < (y_cont.size() - 1)) {
                    bw.write(",");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public void displayimage(Mat image){
        Log.d("displayimage","started");
        Mat test=new Mat();

        if(image.channels()==1) {
            Imgproc.cvtColor(image,test,Imgproc.COLOR_GRAY2BGR);
        }
        else{
            test=image.clone();
        }

        Bitmap bmp=Bitmap.createBitmap(480,640,Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(test,bmp);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bmp);
            }
        });
        test.release();
    }



    private void show_framebyframe(int frameno,int x1,int y1,int w1){
        Mat contour_gray=new Mat();
        Mat gray=new Mat();
        Mat hsv=new Mat();

        Mat masked=new Mat();
        Mat rgb=frames.get(frameno);
        Imgproc.cvtColor(rgb,gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(rgb,hsv, Imgproc.COLOR_BGR2HSV);


        Imgproc.threshold(gray,contour_gray,254,255,Imgproc.THRESH_BINARY);
        Log.d("showframebyframe",String.valueOf(frameno));
        pixelid.setText(String.valueOf(frameno));
        Core.inRange(hsv,new Scalar(0,0,250),new Scalar(5,5,255),masked);
        displayimage(rgb);
        double a1[]=new double[20];

        a1 = hsv.get((int)y1, (int)x1);
        if(a1!=null) {
            Log.d("tracking_new_H", String.valueOf(a1[0]));
            Log.d("tracking_new_S", String.valueOf(a1[1]));
            Log.d("tracking_new_V", String.valueOf(a1[2]));
        }
    }





//    // Find the grayscale values in the roi
//    private void trackingnew(int boxsize,int startframeno){
//        int framelength = frames.size(); // length of the saved frames list
//        int x1=(int)(x_touch1-boxsize/2); // X position for extracting the ROI
//        int y1=(int)(y_touch1-boxsize/2); // Y position for extractuing the ROI
//
//        Mat gray=new Mat();
//
//        for (int i = 0; i < framelength; i++) {
//            Mat rgb=frames.get(i);
//            Imgproc.cvtColor(rgb, gray, Imgproc.COLOR_BGR2GRAY);
//            Mat frameroi = new Mat(gray, new Rect(x1, y1, boxsize, boxsize));
//
//
//            if(i>=2){
//                Core.bitwise_and(frameroi,mask_disp,masked_disp);
//                Scalar sum=Core.sumElems(masked_disp);
//                double sum1=sum.val[0];
//                int countnonzero=Core.countNonZero(mask_disp);
//                int avg_int=(int)(sum1/countnonzero);
//                touch_x.add(avg_int);
//                Log.d("Tracking_int1",String.valueOf(avg_int));
//            }
//
//            Imgproc.circle(rgb, new Point(x1 + boxsize / 2, y1 + boxsize / 2), 15, new Scalar(255, 255, 255), 1);
//
//            Bitmap bmp=Bitmap.createBitmap(50,50,Bitmap.Config.ARGB_8888);
//
//            Mat test=new Mat();
//            Imgproc.cvtColor(gray,test,Imgproc.COLOR_GRAY2BGR);
//
//            //displayimage(masked_disp);
//        }
//        pixelcal1();
//
//    }
//
//    private void tracking_new(List<Mat> frames1,int x1, int y1, int boxsize,int frameno) {
//        displaystart=1;
//        Mat diff_image = new Mat(640, 480, CvType.CV_8UC1,new Scalar(0));
//        Mat prev_image= new Mat(640, 480, CvType.CV_8UC1,new Scalar(0));
//
//        int length = frames1.size();
//
//
//        Mat gray = new Mat(); //1920, 1080, CvType.CV_8UC1);
//        int gradient=0;
//        double prev_tot_b=0;
//
//        Mat mask_disp = new Mat(boxsize, boxsize, CvType.CV_8UC1, new Scalar(0, 0, 0));
//        Mat masked_disp = new Mat(boxsize, boxsize, CvType.CV_8UC1, new Scalar(0, 0, 0));
//
//        for (int i = 0; i < length; i++) {
//
//            Mat rgb=frames1.get(i);
//            double a1[] = new double[20];
//            Imgproc.cvtColor(rgb, gray, Imgproc.COLOR_BGR2GRAY);
//            Mat frameroi = new Mat(gray, new Rect(x1, y1, boxsize, boxsize));
//            Core.absdiff(prev_image, gray, diff_image);
//
//            gray.copyTo(prev_image);
//
//            Scalar val2 = Core.sumElems(frameroi);
//            double b1 = val2.val[0];
//            //touch_x.add((int) b1 / (boxsize * boxsize));
//            Scalar tot = Core.sumElems(diff_image);
//            double tot_b = tot.val[0];
//            if(tot_b>prev_tot_b && gradient!=-1 && i>0){
//              gradient=1;
//            }
//            if(tot_b<prev_tot_b && gradient==1){
//                gradient=-1;
//                Log.d("Tracking_startframe",String.valueOf(i));
//            }
//            if(i>0) {
//                prev_tot_b = tot_b;
//            }
//            Log.d("Tracking_diffsum",String.valueOf(tot_b));
//
//            if(i==2){
//                Mat contour_res=new Mat();
//                Imgproc.threshold(frameroi,contour_res,220,255,Imgproc.THRESH_BINARY);
//                ArrayList<MatOfPoint> contours=new ArrayList<>();
//                Imgproc.findContours(contour_res,contours,new Mat(),Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
//                Iterator<MatOfPoint> iterator=contours.iterator();
//                Log.d("Tracking_Cont",String.valueOf(contours.size()));
//                int x_cont=0,y_cont=0;
//                int contour_found=0;
//
//                MatOfPoint2f approxcurve=new MatOfPoint2f();
//                //Imgproc.drawContours(rgb,contours,0,new Scalar(255,0,255),5);
//                //Rect rect=Imgproc.boundingRect(contours.get(0));
//
//                while(iterator.hasNext()){
//                    MatOfPoint contour=iterator.next();
//                    double area= Imgproc.contourArea(contour);
//                    Point point_cent=new Point();
//                    float[] radius=new float[2];
//                    Imgproc.minEnclosingCircle(new MatOfPoint2f(contour.toArray()),point_cent,radius);
//
//                    if(area>100) {
//
//                       // Imgproc.circle(rgb,new Point(x_touch,y_touch),50,new Scalar(255,255,255),4);
//                        Imgproc.rectangle(rgb,new Point(x1,y1),new Point(x1+boxsize,y1+boxsize),new Scalar(255,0,0),5);
//                        Imgproc.circle(rgb, point_cent,(int)radius[0] , new Scalar(255, 0, 255), 10);
//                        Imgproc.circle(mask_disp,point_cent,(int)radius[0],new Scalar(255,255,255),-1);
//                        Bitmap bmp1=Bitmap.createBitmap(480,640,Bitmap.Config.ARGB_8888);
//
//                        Utils.matToBitmap(rgb,bmp1);
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                imageView.setImageBitmap(bmp1);
//                            }
//                        });
//                    }
//                }
//            }
//            if(i>=2){
//                Core.bitwise_and(frameroi,mask_disp,masked_disp);
//                Scalar sum=Core.sumElems(masked_disp);
//                double sum1=sum.val[0];
//                int countnonzero=Core.countNonZero(mask_disp);
//                int avg_int=(int)(sum1/countnonzero);
//                touch_x.add(avg_int);
//                Log.d("Tracking_int1",String.valueOf(avg_int));
//            }
//
//            Imgproc.circle(rgb, new Point(x1 + boxsize / 2, y1 + boxsize / 2), 15, new Scalar(255, 255, 255), 1);
//
//            Bitmap bmp=Bitmap.createBitmap(50,50,Bitmap.Config.ARGB_8888);
//
//            Mat test=new Mat();
//            Imgproc.cvtColor(gray,test,Imgproc.COLOR_GRAY2BGR);
//
//        //displayimage(masked_disp);
//    }
//    pixelcal1();
//
//    }


    //    public void playframes(List<Mat>frames1){
//        int length=frames1.size();
//        Log.d("frameleng",String.valueOf(length));
//        int boxsize=100;
//        int x1=(int)(x_touch1-boxsize/2);
//        int y1=(int)(y_touch1-boxsize/2);
//        Mat gray = new Mat();//2240, 1080, CvType.CV_8UC1);
//        tracking_new(frames1,x1,y1,boxsize,0);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                // imageView.setVisibility(View.INVISIBLE);
//            }
//        });
//    }


    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        {
            if(OpenCVLoader.initDebug()){
                Log.e("opencvconfig","Opencv is configured");
                baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
            }
            else{
                Log.e("opencvconfig","opencvfailed");
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,baseLoaderCallback);
            }
        }
    }

    public static void bluetooth_send(String msg){
        Log.d("sending_blue","yes");
        if(btSocket!=null){
            try{
                OutputStream out= btSocket.getOutputStream();
                out.write((msg).getBytes());
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }




    public void resize(int x1,int y1,int boxsize,Mat rgb){

        Mat frameroi = new Mat(rgb,new Rect(x1,y1,boxsize,boxsize));
        Mat rgb_zoomed = new Mat(640,480,CvType.CV_8UC3,new Scalar(0,0,0));
        Imgproc.resize(frameroi,rgb_zoomed,new Size(640,480),0,0, Imgproc.INTER_CUBIC);
        // displayimage(rgb);

//        Mat boundingarea_rgb=new Mat(frame_on,new Rect(x_base,y_base,width_roi,height_roi)); // Just extract the part of the image based on stage 2 bounding box
//        Mat boundingarea_gray=new Mat(grayimage_on,new Rect(x_base,y_base,width_roi,height_roi)); // Just extract the part of the image based on stage 2 bounding box
//        Mat resize_boundingarea_rgb=new Mat();
//        Mat resize_boundingarea_gray=new Mat();
//        Mat resized_ranged_image = new Mat(height_roi, width_roi, CvType.CV_8UC1,new Scalar(0));
//
//        Size sz=new Size(width,height);
//        resize(boundingarea_gray,resize_boundingarea_gray,sz,0,0,INTER_CUBIC);
//        resize(boundingarea_rgb,resize_boundingarea_rgb,sz,0,0, INTER_CUBIC);
    }






}


//    public void pixelcal(){
//        int stress=20;
//        double base=0;
//        int a[]=new int[20];
//        sum=0;
//        Log.d("touchsize",String.valueOf(touch_x.size()));
//        int[] threshold= new int[2];
//        thresh=0;
//
//        int max=0,increase=0;
//        for(int i=0;i<touch_x.size();i++){
//            if(touch_x.get(i)>max && increase==0){
//                max=touch_x.get(i);
//            }else{
//                Log.d("highvaluepixel",String.valueOf(i));
//                break;
//            }
//
//        }
//        for(int g=0;g<framesep.get(1);g++) {
////            int sep2 = framesep.get(g);
//            //          int index = (base + sep2) / 2;
//            //        base = sep2;
//
//            int intensity1 = touch_x.get(g);
//            //threshold[g]=intensity1;
//            thresh+=intensity1;
//        }
//        //thresh=(threshold[0]+threshold[1])/2;
//        thresh=thresh/framesep.get(1);
//        new_start=0;
//
//        for(int g=0;g<framesep.size();g++) {
//            if (touch_x.get(g) > thresh) {
//                new_start=g;
//                break;
//            }
//        }
//        int pixel_bit1=0;
//        if(new_start%2==0) {
//            pixel_bit1=touch_x.get(new_start/2);
//        }
//        else {
//            double index=(double)new_start/2.0;
//            int index1=(int)index;
//            int index2=index1+1;
//
//            pixel_bit1=(touch_x.get(index1)+touch_x.get(index2)/2);
//        }
//
//        for(int k=new_start;k<touch_x.size();k++)
//        {
//            touch_y.add(touch_x.get(k));
//        }
//        Log.d("framestartnew",String.valueOf(new_start));
//
//        Log.d("intensity_threshold",String.valueOf(thresh));
//        base=0;
//        sep1=(double)(touch_y.size()/9.0);
//        Log.d("intensity_sep1",String.valueOf(sep1));
//        Log.d("intensity_size",String.valueOf(touch_y.size()));
//
//
//
//        for(int g=0;g<bitsize-1;g++){
//            int sep2=framesep.get(g);
//            double index=(double)(sep1/2+g*sep1);
//            int index1=(int)index;
//
//            int x1=(int)(index1);
//            int x2=x1+1;
//            double y1=0;
//            double y2=0;
//            if(x2<touch_y.size()) {
//                y1 = (double) touch_y.get(x1);
//                y2 = (double) touch_y.get(x2);
//            }
//            double interp;
//            if(index!=index1) {
//                interp = (y1+y2)/2;
//            }
//            else
//            {
//                interp=y1;
//            }
//
//            base=sep1-1;
//            Log.d("intensity_interp",String.valueOf(interp));
//            Log.d("intensity_x1",String.valueOf(index));
//            // Log.d("intensity_indexer",String.valueOf(indexer));
//
//            Log.d("intensity_y1",String.valueOf(y1));
//            Log.d("intensity_y2",String.valueOf(y2));
//
//
//            if(interp>(thresh)){ //240 values works for inside the room ,change based on room lighting conditions
//                Log.d("pixel_id","1");
//                a[g]=1;
//
//            }
//            else{
//                Log.d("pixel_id","0");
//                a[g]=0;
//            }
//        }
//        for(int gg=0;gg<7;gg++){
//            sum+=(int)(a[7-gg]*Math.pow(2,gg));
//            Log.d("pixel_id1",String.valueOf(Math.pow(2,gg)));
//        }
//        Log.d("pixel_id_tot",String.valueOf(sum));
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                imageView.setVisibility(View.VISIBLE);
//
//            }
//        });
//        if(pixel_bit1>thresh)
//        {
//            pixel_bit1=1;
//        }else {
//            pixel_bit1=0;
//        }
//        final int p=pixel_bit1;
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if(a[0]!=1 || p!=0 || sum>10){
//                    pixelid.setText("Oops");
//                }
//                else {
//                    index.add(sum);
//                    pixelid.setText(String.valueOf(sum));
//
//                }
//            }
//        });
//        touch_x.clear();
//        //   contour_found=0;
//    }
//
//    public int pixelintensity_on(){
//
//        return 0;
//    }
//
//
//    private double map(int x,int x1_low,int x2_low,int x1_high,int x2_high){
//        double R=(x2_high-x1_high)/(x2_low-x1_low);
//        double y=x1_high+ R*(x-x1_low);
//        return y;
//
//    }

//    public void tracking(int x1,int y1,int x2,int y2,Mat rgb)
//    {
//
//        Mat gray = new Mat();//2240, 1080, CvType.CV_8UC1);
//        int c;
//        Imgproc.cvtColor(rgb,gray, Imgproc.COLOR_BGR2GRAY);
//        if(gray!=null) {
//            Log.d("gray", "notnull");
//
//            Mat frameroi = new Mat(640, 480, CvType.CV_8UC1,new Scalar(0));
//            Mat frameroi1 = new Mat(640, 480, CvType.CV_8UC3);
//            Mat mask = new Mat(640, 480, CvType.CV_8UC1,new Scalar(0));
//
//
//            // Imgproc.rectangle(frameroi1, new Point(x1, y1), new Point(x2, y2), new Scalar(255, 255, 255), -1);
//
//            Imgproc.rectangle(frameroi, new Point(x1, y1), new Point(x2, y2), new Scalar(255), -1);
//            Mat res=new Mat();
//            Mat res1=new Mat();
//
//            Core.bitwise_and(gray, frameroi, res);
//            //Core.bitwise_and(rgb, frameroi1, res1);
//            Mat contour_res=new Mat();
//            Imgproc.threshold(res,contour_res,200,255,Imgproc.THRESH_BINARY);
//            Core.MinMaxLocResult g = Core.minMaxLoc(res);
//            List<MatOfPoint> contours=new ArrayList<>();
//            Imgproc.findContours(contour_res,contours,new Mat(),Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
//            Iterator<MatOfPoint> iterator=contours.iterator();
//            int x_cont=0,y_cont=0;
//            int contour_found=0;
//            while(iterator.hasNext()){
//                MatOfPoint contour=iterator.next();
//                Rect rect=Imgproc.boundingRect(contour);
//                if(Imgproc.contourArea(contour)>thresh_cont) {
//                    int x = rect.x;
//                    int y = rect.y;
//                    int w = rect.width;
//                    int h = rect.height;
//                    x_cont = (int) (x + w / 2);
//                    y_cont= (int) (y + h / 2);
//                    c=min(w,h);
//                    Imgproc.circle(mask, new Point(x_cont, y_cont),c , new Scalar(255, 255, 255), -1);
//                    contour_found=1;
//                }
//                //  int w=rect.width;
//                // int
//                //Imgproc.rectangle(res1, new Point(x1, y1), new Point(x2, y2), new Scalar(255, 255, 255), -1);
//
//            }
//            int max_x = (int) g.maxLoc.x;
//            int max_y = (int) g.maxLoc.y;
//            double[] a1=new double[20];
//            max_x=(int)x_touch1;
//            max_y=(int)y_touch1;
//            if(contour_found==0) {
//                a1= gray.get(max_y, max_x);
//                Imgproc.circle(rgb, new Point(max_x, max_y), 15, new Scalar(255, 255, 255), -1);
//                thresh_cont=(int)a1[0];
//
//            }
//            else{
//                Mat gray1=new Mat();
//                Core.bitwise_and(mask,gray,gray1);
//                double sum=Core.countNonZero(mask);
//                Scalar val2=Core.sumElems(gray1);
//                double b1=val2.val[0];
//
//                double temp1=b1/sum;
//                //a1= gray1.get(y_cont, x_cont);
//                a1[0]=(int)temp1;
//                // Imgproc.circle(rgb, new Point(x_cont, y_cont), 15, new Scalar(255, 255, 255), -1);
//            }
//
//
//            Imgproc.circle(rgb, new Point(max_x, max_y), 15, new Scalar(255, 255, 255), -1);
//
////            if(contour_found==1)
////            {
////                max_x=(int)x_cont1;
////                max_y=(int)y_cont1;
////            }
////            Imgproc.circle(rgb, new Point(max_x, max_y), 25, new Scalar(255, 255, 255), 5);
////            a1= gray.get(max_y, max_x);
//
//            if (a1 != null) {
//                Log.d("gray", String.valueOf(a1[0]));
//                touch_x.add((int) a1[0]);
//                elapsedtime=System.currentTimeMillis()-start_time;
//                timetracker.add(elapsedtime/1000);
//                Log.d("pixelvalues",String.valueOf(a1[0]));
//                frameno++;
//                track_val = (int) g.maxVal;
//                if (track_val > max_track_val) {
//                    max_track_val = track_val;
//                }
//            }
//            Bitmap bmp=Bitmap.createBitmap(480,640,Bitmap.Config.ARGB_8888);
//            Mat test=new Mat();
//            Imgproc.cvtColor(res,test,Imgproc.COLOR_GRAY2BGR);
//            Utils.matToBitmap(rgb,bmp);
//            imageView.setVisibility(View.VISIBLE);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    imageView.setImageBitmap(bmp);
//                }
//            });
//            frameroi.release();;
//            res.release();;
//
//            tracker++;
//
//        }
//        gray.release();
//        rgb.release();
//        //Imgproc.circle(gray_thresh, new Point(max_x,max_y), 10, new Scalar(0, 0, 255), -1);
//        //            Mat roi=new Mat(gray,new Rect(x_touch-x1,y_touch-y1,x_touch+x1,y_touch+y1));
//        // Imgproc.rectangle(gray,new Point(x_touch-x1,y_touch-y1),new Point(x_touch+x1,y_touch+y1),new Scalar(255,255,255),5);
//        //Imgproc.rectangle(gray_thresh,new Point(x_touch-x1,y_touch-y1),new Point(x_touch+x1,y_touch+y1),new Scalar(255,255,255),5);
//    }
//    int a[]=new int[20];
//
//        min=255;
//                max=0;
//                max_index=0;
//                min_index=0;
//                for(int i=0;i<14;i++){
//        Log.d("value_touchx",String.valueOf(touch_x.get(i)));
//        if(touch_x.get(i)>max ){
//        max=touch_x.get(i);
//        max_index=i;
//        }
//        }
//
//        for(int i=max_index;i<14;i++){
//        if(touch_x.get(i)<min ){
//        min=touch_x.get(i);
//        min_index=i;
//        }
//        }
//        double center=(max+min)/2.0;
//
//        for(int i=max_index;i<14;i++){
//        if(touch_x.get(i)<center){
//        center_index=(2*i-1)/2.0;
//        break;
//        }else if(touch_x.get(i)==center){
//        center_index=i;
//        break;
//        }
//        }
//        threshstart_index=center_index;
//        threshstart_int=center;
//        Log.d("valuehigh",String.valueOf(max));
//        Log.d("valuehigh_index",String.valueOf(max_index));
//        Log.d("valuelow",String.valueOf(min));
//        Log.d("valuecenter",String.valueOf(center_index));
//        Log.d("valuethresh",String.valueOf(center));
//
//        double sep1=(double)((touch_x.size()-HelloSceneformActivity.center_index)/11.0);
//        Log.d("valuesep",String.valueOf(sep1));
//
//        double thresh=(HelloSceneformActivity.max+HelloSceneformActivity.min)/2.0;
////                                for(int i=;i<)
//        Log.d("value_thresh",String.valueOf(thresh));
//    int framelen=touch_x.size();
//    double base=center_index;
//    double center_index_end_h=0;
//    double center_int_end_h=0;
//
//    double center_index_end_l=0;
//    double center_int_end_l=0;
//
//        for(int i=1;i<11;i++) {
//        double bit1 = (base*2 + sep1)/2.0;
//        base = base+sep1;
//        Log.d("value",String.valueOf(bit1));
//        int lower=(int)bit1;
//        int higher=(int)(Math.ceil(bit1));
//        double lower_val=touch_x.get(lower);
//        double higherval=touch_x.get(higher);
//        double avg=(lower_val+higherval)/2;
//        Log.d("value_int",String.valueOf(avg));
//
//        if(i==9){
//        center_index_end_h=bit1;
//        center_int_end_h=avg;
//        }
//        if(i==10){
//        center_index_end_l=bit1;
//        center_int_end_l=avg;
//        }
//        }
//
//        base=center_index;
//
//
//        threshend_index=(center_index_end_h+center_index_end_l)/2.0;
//        threshend_int=(center_int_end_h+center_int_end_l)/2.0;;
//
//
//
//        for(int i=0;i<11;i++) {
//        double bit1 = (base*2 + sep1)/2.0;
//        base = base+sep1;
//        Log.d("value",String.valueOf(bit1));
//        int lower=(int)bit1;
//        int higher=(int)(Math.ceil(bit1));
//        double lower_val=touch_x.get(lower);
//        double higherval=touch_x.get(higher);
//
//        if ((thresh_interp(lower, lower_val, higher, higherval, bit1))>(thresh_interp(threshstart_index,threshstart_int,threshend_index,threshend_int,bit1))) { //240 values works for inside the room ,change based on room lighting conditions
//        Log.d("pixel_id", "1");
//        a[i] = 1;
//
//        } else {
//        Log.d("pixel_id", "0");
//        a[i ] = 0;
//        }
//        }
//    private void makecube(Anchor anchor){
//        MaterialFactory.makeOpaqueWithColor(this,new Color(android.graphics.Color.RED)).thenAccept(material -> { ModelRenderable cuberenderable= ShapeFactory.makeCube(new Vector3(0.3f,0.3f,0.3f),new Vector3(0f,0,3f,0f),material);
//        });
//        AnchorNode anchorNode=new AnchorNode();
//        anchorNode.setRenderable(cube);
//    }
/**
 * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
 * on this device.
 *
 * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
 *
 * <p>Finishes the activity if Sceneform can not run
 */



//        List<MatOfPoint> contours=new ArrayList<>();
//        Imgproc.findContours(contour_gray,contours,new Mat(),Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
//        Iterator<MatOfPoint> iterator=contours.iterator();
//        double maxarea=0;
//        int x_max=0,y_max=0;
//        while(iterator.hasNext()){
//            MatOfPoint contour=iterator.next();
//            Rect rect=Imgproc.boundingRect(contour);
//            double area=Imgproc.contourArea(contour);
//            int x = rect.x;
//            int y = rect.y;
//            int w = rect.width;
//            int h = rect.height;
//            if(area>300) {
//                //Imgproc.circle(mask, new Point(x, y1+y_max),25 , new Scalar(255, 255, 255), -1);
//                if(x>x1 && (x+w)<(x1+w1) & y>y1 && (y+h)<(y1+w1)) {
//                  //  Imgproc.rectangle(rgb, new Point(x, y), new Point(x + w, y + h), new Scalar(0, 0, 255), 2);
//
//                    x_cont1 = (int) (x + w / 2);
//                    y_cont1= (int) (y + h / 2);
//                    contour_found=1;
//
//
//                }
//
//
//
//                //Imgproc.circle(gray, new Point(x_cont+x1, y_cont+y1),25 , new Scalar(255, 255, 255), 2);
//                //Imgproc.circle(rgb, new Point(x_cont+x1,y_cont+y1),5 , new Scalar(255,0, 255), 2);
////                if(area>maxarea){
////                    x_max=(int) (x + w / 2);
////                    y_max=(int) (y + h / 2);
////                    Log.d("contour_frameno",String.valueOf(frameno));
////                    maxarea=area;
////                    Imgproc.rectangle(mask_disp, new Point(0, 0), new Point(480, 640), new Scalar(0, 0, 0), -1);
////                    //Imgproc.rectangle(mask_disp, new Point(x1+x, y1+y), new Point(x1+x+w, y1+y+h), new Scalar(255, 255, 255), -1);
////                    Imgproc.circle(mask_disp, new Point(x1+x_max, y1+y_max),25 , new Scalar(255, 255, 255), -1);
////
////                    Imgproc.rectangle(mask, new Point(0, 0), new Point(480, 640), new Scalar(0, 0, 0), -1);
////
////                    //Imgproc.rectangle(mask, new Point(x1+x, y1+y), new Point(x1+x+w, y1+y+h), new Scalar(255, 255, 255), -1);
////                    //Imgproc.circle(mask, new Point(0, 480),25 , new Scalar(255, 255, 255), -1);
////
////                    Imgproc.circle(mask, new Point(x1+x_max, y1+y_max),25 , new Scalar(255, 255, 255), -1);
////
////                }
//                //  Imgproc.circle(gray, new Point(10, 480),25 , new Scalar(255, 255, 255), -1);
//              //  Imgproc.rectangle(rgb,new Point(x1,y1),new Point(x1+x+w,y1+y+h),new Scalar(255,0,255),2,0,0);
//
//            }
//            //  int w=rect.width;
//            // int
//
//
//        }

//        Imgproc.threshold(frameroi,contour_res,thresh_cont,255,Imgproc.THRESH_BINARY);
//        Imgproc.threshold(gray,contour_gray,thresh_cont,255,Imgproc.THRESH_BINARY);

// Imgproc.circle(mask, new Point(x_cont+x1, y_cont+y1),5 , new Scalar(255, 255, 255), -1);
//Imgproc.circle(mask_disp, new Point(x_max+x1,y_max+y1),5 , new Scalar(255,255,255), -1);
//  Imgproc.rectangle(mask_disp,new Point(x1,y1),new Point(x1+w1,y1+w1),new Scalar(255,255,255),3,0,0);



//Imgproc.cvtColor(contour_gray,thresh_disp,Imgproc.COLOR_GRAY2BGR);


//
//        Imgproc.circle(mask, new Point(x1+w1/2, y1+w1/2),10 , new Scalar(255, 255, 255), -1);
//        //Imgproc.circle(mask, new Point(x1+w1/2, y1+w1/2),10 , new Scalar(255, 255, 255), -1);
//
//        Core.bitwise_and(mask,gray,mask_gray);


//        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(mask_gray);
//        double x_maxloc=minMaxLocResult.maxLoc.x;
//        double y_maxloc=minMaxLocResult.maxLoc.y;
//        double x_maxval=minMaxLocResult.maxVal;
//        double y_minval=minMaxLocResult.minVal;
//
//        a1 = hsv.get((int)y1+w1/2, (int)x1+w1/2);
//        Log.d("tracking_new_H",String.valueOf(a1[0]));
//        Log.d("tracking_new_S",String.valueOf(a1[1]));
//        Log.d("tracking_new_V",String.valueOf(a1[2]));



//        if(contour_found==0) {
//         //   a1 = gray.get(y_touch1, x_touch1);
//            a1 = gray.get((int)y_maxloc, (int)x_maxloc);
//
//            touch_x.add((int) a1[0]);
//            thresh_cont=(int)a1[0];
//        }
//        else {
//           // a1 = gray.get(y_cont+y1, x_cont+x1);
//            Mat gray1=new Mat();
//            Core.bitwise_and(mask,gray,gray1);
//            double sum=Core.countNonZero(mask);
//            Scalar val2=Core.sumElems(gray1);
//            double b1=val2.val[0];
//
//            double temp1=b1/sum;
//            //a1= gray1.get(y_cont, x_cont);
//            //a1[0]=(int)temp1;
//            a1 = gray.get((int)y_maxloc, (int)x_maxloc);
//
//            touch_x.add((int)a1[0]);
//          //  Imgproc.circle(rgb, new Point(x_max+x1,y_max+y1),10 , new Scalar(0,0,255), 3);
//
//        }