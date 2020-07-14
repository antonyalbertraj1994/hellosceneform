package com.google.ar.sceneform.samples.hellosceneform;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Point2f;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.opencv.core.CvType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;


import static org.bytedeco.opencv.global.opencv_core.CV_32SC1;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC1;
import static org.bytedeco.opencv.global.opencv_core.absdiff;
import static org.bytedeco.opencv.global.opencv_core.bitwise_and;
import static org.bytedeco.opencv.global.opencv_core.bitwise_not;
import static org.bytedeco.opencv.global.opencv_core.countNonZero;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_AA;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_BGR2HSV;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_CHAIN_APPROX_NONE;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_DIST_L2;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_GRAY2BGR;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_RETR_EXTERNAL;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_THRESH_BINARY;
import static org.bytedeco.opencv.global.opencv_imgproc.INTER_CUBIC;
import static org.bytedeco.opencv.global.opencv_imgproc.LINE_AA;
import static org.bytedeco.opencv.global.opencv_imgproc.MORPH_ELLIPSE;
import static org.bytedeco.opencv.global.opencv_imgproc.MORPH_OPEN;
import static org.bytedeco.opencv.global.opencv_imgproc.circle;
import static org.bytedeco.opencv.global.opencv_imgproc.contourArea;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.dilate;
import static org.bytedeco.opencv.global.opencv_imgproc.distanceTransform;
import static org.bytedeco.opencv.global.opencv_imgproc.findContours;
import static org.bytedeco.opencv.global.opencv_imgproc.getStructuringElement;
import static org.bytedeco.opencv.global.opencv_imgproc.minEnclosingCircle;
import static org.bytedeco.opencv.global.opencv_imgproc.morphologyEx;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;
import static org.bytedeco.opencv.global.opencv_imgproc.threshold;
import static org.bytedeco.opencv.global.opencv_imgproc.watershed;

public class processactivity extends AppCompatActivity {
    ImageView imageView1;
    String filename="/storage/emulated/0/scatter/antony.mp4";
    String filename_output="/storage/emulated/0/scatter/stab_antony.avi";

    int height=1080,width=1920;
    Mat avg_frame_off=new Mat();
    Mat avg_frame_on=new Mat();
    private boolean printflag=true;
    Mat avg_frame1=new Mat();
    Mat avg_diff=new Mat();
    private SeekBar seekBar;
    private Button nextButton;
    private TextView frameno_disp;
    private static int maxindex=0;

    public static int bl_stress,gl_stress,rl_stress,bh_stress,gh_stress,rh_stress,gray_stress,graydiff_stress;
    int mode=3;
    int start=1;
    List<Integer> x_cont=new ArrayList<>(5);
    List<Integer> y_cont=new ArrayList<>(5);
    List<Integer> index=new ArrayList<>(5);
    private static int avg_threshold;
    int x_touch,y_touch,seek_value=0;
    int xl_touch,yl_touch,xh_touch,yh_touch;
    int disp_height;
    int disp_width,touchcount=0;
    int seek_value_mode2,seek_value_mode1;
    Point t1;
    Point b1;
    List<Integer> x_mask=new ArrayList<Integer>(20);
    List<Integer> y_mask=new ArrayList<Integer>(20);
    ArrayList<Mat> video_frame=new ArrayList<Mat>(50);
    public static int x_touch_pixel=0,y_touch_pixel=0;
    private static org.bytedeco.opencv.opencv_core.Mat grayimagemax_test=new Mat(1080,1920, CvType.CV_8UC1,new Scalar(0,0,0,0));
    private static org.bytedeco.opencv.opencv_core.Mat grabbedimagemax_test=new Mat(1080,1920, CvType.CV_8UC3,new Scalar(0,0,0,0));

    private static org.bytedeco.opencv.opencv_core.Mat grayimagemax_test_off=new Mat(1080,1920, CvType.CV_8UC1,new Scalar(0,0,0,0));
    private static org.bytedeco.opencv.opencv_core.Mat grabbedimagemax_test_off=new Mat(1080,1920, CvType.CV_8UC3,new Scalar(0,0,0,0));

    private static List<Mat> grayimagemax_test1=new ArrayList<>();
    private static List<Mat> grabbedimagemax_test1=new ArrayList<>();

    private ProgressBar completepercentange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processactivity);
        completepercentange=(ProgressBar)findViewById(R.id.progressBar);
        completepercentange.setScaleY(8.0f);
        completepercentange.setProgress(0);

        completepercentange.setVisibility(View.INVISIBLE);
        imageView1 = (ImageView) findViewById(R.id.imageView_calib3);

        startframe.start();

        System.setProperty("org.bytedeco.javacpp.maxphysicalbytes", "0"); //set this property to avoid the physical memory limitation.
        System.setProperty("org.bytedeco.javacpp.maxbytes", "0"); //set this property to avoid the physical memory limitation.

    }

    Thread startframe=new Thread() {
        @Override
        public void run() {
            super.run();
            debugtagprint("StartProcessing","Success",printflag);
            loadframes();
            int x=startframe();
            List<List<Integer>> rois=findrois();
            stage3_diffdetector(rois);
        }
    };

    private  int startframe(){
        Log.d("Startframe","Success");

        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        FFmpegFrameGrabber grabber_test = new FFmpegFrameGrabber(filename);
        Mat grabbedimage_test=new Mat();
        org.bytedeco.opencv.opencv_core.Mat masked_location = new Mat(height, width, CvType.CV_8UC1);
        org.bytedeco.opencv.opencv_core.Mat grayimage_test = new Mat(height, width, CvType.CV_8UC1);
        org.bytedeco.opencv.opencv_core.Mat masker_test=new Mat(height,width, CvType.CV_8UC1,new Scalar(0,0,0,0));
        org.bytedeco.opencv.opencv_core.Mat diff_image=new Mat(height,width, CvType.CV_8UC1,new Scalar(0,0,0,0));
        org.bytedeco.opencv.opencv_core.Mat diff_image_thresh=new Mat(height,width, CvType.CV_8UC1,new Scalar(0,0,0,0));
        org.bytedeco.opencv.opencv_core.Mat prev_gray=new Mat(height,width, CvType.CV_8UC1,new Scalar(0,0,0,0));
        org.bytedeco.opencv.opencv_core.Mat diff_image_prev=new Mat(height,width, CvType.CV_8UC1,new Scalar(0,0,0,0));
        Mat kernel = getStructuringElement(MORPH_ELLIPSE, new Size(5, 5));

        int found=0;
        long maxvalue=0;
        int frameno_test=0;
        int contour_count=0;
        int gradient=0,maxframediff=0,maxframediff_index=0;
        double prev_sum=0;
        MatVector contours=new MatVector();
        int i=0;
        int[] intensity=new int[30];
        while(i<30){
            frameno_test= i;//grabber_test.getFrameNumber();
            if(frameno_test<30) {

                cvtColor(video_frame.get(i), grayimage_test, CV_BGR2GRAY);

//                if(frameno_test>5) {
//                    absdiff(grayimage_test, prev_gray, diff_image);
//                }
                grayimage_test.copyTo(prev_gray);
                Scalar sum = opencv_core.sumElems(grayimage_test);
                double sum1 = sum.get(0);
                if(frameno_test>5) {
                    intensity[i]=(int)sum1;

                    if (sum1 > maxframediff && frameno_test < 30 && gradient!=-1) {
                        maxframediff = (int) sum1;
                        maxframediff_index = frameno_test;
                        maxindex=maxframediff_index+6;
                        gradient=1;
                    }
                    if(gradient==1 && sum1<maxframediff){
                        maxindex=frameno_test;
                        gradient=-1;
                    }
                    Log.d("Framemax_all", String.valueOf(sum1) + "," + String.valueOf(frameno_test) + "," + String.valueOf(gradient));
                }
                else{
                    intensity[i]=0;
                }
            }
            i++;
        }



        int[] sortedindices= IntStream.range(0,intensity.length).boxed().sorted((i1,j)-> intensity[i1]-(intensity[j]) ).mapToInt(ele -> ele).toArray();
        String sortedans="";

        List<Integer> sorted_main=new ArrayList<>();
        for(int g=0;g<11;g++) {
            sortedans+=String.valueOf(sortedindices[29-g]);
            sortedans+=',';
            sorted_main.add(sortedindices[29-g]);
        }
        int min= Collections.min(sorted_main);
        int max=Collections.max(sorted_main);

        Log.d("sorted_min",String.valueOf(min));
        Log.d("sorted_max",String.valueOf(max));
        maxindex=(int)((min+max)/2);
        Mat gray_on=new Mat();
        Mat gray_off=new Mat();
        //maxindex=21;
        cvtColor(video_frame.get(maxindex),gray_on,CV_BGR2GRAY);
        cvtColor(video_frame.get(maxindex+12),gray_off,CV_BGR2GRAY);
        absdiff(gray_on, gray_off, diff_image);
        Mat diff_image_thresh1=new Mat();
        //threshold(diff_image,diff_image_thresh1,60,255,CV_THRESH_BINARY);
        //displayimage(diff_image_thresh1);
        video_frame.get(maxindex).copyTo(grabbedimagemax_test);
        gray_on.copyTo(grayimagemax_test);
        Log.d("Framemax",String.valueOf(maxindex));
        Log.d("Framemaxindex",String.valueOf(maxframediff_index));
        displayimage(grabbedimagemax_test);
        return maxindex;
    }


    //Find the rois based a smart thresholding algorithm
    private List<List<Integer>> findrois(){
        Log.d("Findingroi","Started");
        double[] minval=new double[20];
        double[] maxval=new double[20];
        Point maxloc=new Point();
        Point minlox=new Point();
        int areathresh=200;

        MatVector contours_tot=new MatVector();
        org.bytedeco.opencv.opencv_core.Mat contour_mask=new Mat(height,width, CvType.CV_8UC1,new Scalar(0,0,0,0));

        int contourcount=0,prev_count=0;
        int stablecount=0;


        int[] stablecounter=new int[200];
        for(int i=50;i<225;i++) {
            threshold(grayimagemax_test, contour_mask, i, 255, CV_THRESH_BINARY);
            Mat kernel = getStructuringElement(MORPH_ELLIPSE, new Size(5, 5));
            opencv_imgproc.erode(contour_mask, contour_mask, kernel);
            opencv_imgproc.erode(contour_mask, contour_mask, kernel);
            opencv_imgproc.erode(contour_mask, contour_mask, kernel);
            opencv_imgproc.erode(contour_mask, contour_mask, kernel);

            int counter_var=0;
            int[] variance=new int[100];
            findContours(contour_mask, contours_tot, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);
            contourcount=(int)contours_tot.size();
            int count=0;
            for(int j=0;j<contourcount;j++){
                Mat contour=contours_tot.get(j);
                double area=opencv_imgproc.contourArea(contour);
                if(area>areathresh){
//                    Point2f center=new Point2f();
//                    float[] radius=new float[20];
//                    minEnclosingCircle(contour,center,radius);
//                    variance[counter_var]=(int)radius[0];
                    count++;
                    counter_var++;

                }
            }

            stablecounter[i-50]=count;
            String tot=String.valueOf(count)+","+String.valueOf(stablecount)+","+String.valueOf(i);
            // Log.d("count,stable,thres",tot);

        }


        List<Integer> stable=new ArrayList<>();
        int counter=0,prev_val=0,start=0,counter_max=0,counter_int=0,counter_intmax=0;
        for(int i=0;i<175;i++) {
            int a = stablecounter[i];
            // Log.d("Stabledetect",String.valueOf(a));
            if (a != prev_val && start==1) {
                if(counter>counter_max) {
                    counter_max=counter;
                    counter_intmax=counter_int;
                }
                i--;
                prev_val=0;
                start=0;
                //Log.d("Stabledetect1",String.valueOf(counter)+","+String.valueOf(i+50));
                counter=0;

            }
            else{
                if(start==0){
                    counter_int=50+i;
                }
                counter++;
                prev_val=a;
                start=1;
            }

        }

        Log.d("stabledetect2",String.valueOf(counter_max)+","+String.valueOf(counter_intmax));

        // Use the computed threshold to find the rois
        Mat kernel = getStructuringElement(MORPH_ELLIPSE, new Size(5, 5));
        List<Integer> gray_x=new ArrayList<>();
        List<Integer> gray_y=new ArrayList<>();
        List<Integer> gray_r=new ArrayList<>();

        avg_threshold=counter_intmax;

        Mat temp=new Mat();
        threshold(grayimagemax_test, contour_mask, avg_threshold, 255, CV_THRESH_BINARY);
        grabbedimagemax_test.copyTo(temp);
        opencv_imgproc.erode(contour_mask, contour_mask, kernel);
        opencv_imgproc.erode(contour_mask, contour_mask, kernel);
        opencv_imgproc.erode(contour_mask, contour_mask, kernel);
        opencv_imgproc.erode(contour_mask, contour_mask, kernel);

        findContours(contour_mask, contours_tot, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);
        long n1 = contours_tot.size();
        for (long j = 0; j < n1; j++) {
            Mat contour1 = contours_tot.get(j);
            double area = contourArea(contour1);
            if (area>areathresh) {
                Point2f center=new Point2f();
                float[] radius=new float[20];
                minEnclosingCircle(contour1,center,radius);
                int center_x=(int)center.x();
                int center_y=(int)center.y();
                opencv_imgproc.circle(temp, new Point(center_x,center_y), (int)radius[0], Scalar.BLUE, 3, CV_AA, 0);
                gray_x.add(center_x);
                gray_y.add(center_y);
                gray_r.add((int)radius[0]);

            }
        }
        //displayimage(contour_mask);
        displayimage(temp);

        Log.d("Findroi_avgthresh",String.valueOf(avg_threshold));
        List<List<Integer>> rois=new ArrayList<>();
        rois.add(gray_x);
        rois.add(gray_y);
        rois.add(gray_r);
        return rois;
    }


    //Interpolate the threshold values for intermediate frame based on the values from the start and end frame
    private double thresh_interp(double x1,double y1,double x2,double y2,double x_new){
        double slope=(double)((y2-y1)/(x2-x1));
        double y_new=slope*(x_new-x1)+y1;
        return y_new;
    }

    // Track the location and ID's of the pixels
    private void stage3_diffdetector(List<List<Integer>> rois) {
        List<Integer> gray_x=rois.get(0);
        List<Integer> gray_y=rois.get(1);
        List<Integer> gray_r=rois.get(2);

        Log.d("Stage3_avg_thresh",String.valueOf(avg_threshold));
        Log.d("Stage3_maxindex",String.valueOf(maxindex));

        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(filename);
        org.bytedeco.opencv.opencv_core.Mat grayimage = new Mat(height, width, CvType.CV_8UC1);
        org.bytedeco.opencv.opencv_core.Mat masker=new Mat(height,width, CvType.CV_8UC3,new Scalar(0,0,0,0));
        org.bytedeco.opencv.opencv_core.Mat circle_roi= new Mat(height, width, CvType.CV_8UC1);
        Mat grabbedimage=new Mat();
        Mat grabbedimage1=new Mat();
        int pixels_count=100; // enter the number of pixels being tracked
        int frameno=0;

        //List to store the gray scale values of the rois
        List<List<Integer>> gray_int=new ArrayList<>();

        // List to hold the local threshold values for different rois
        List<Integer> threshold_off=new ArrayList<>();
        List<Integer> threshold_on=new ArrayList<>();
        List<Integer> threshold_off_end=new ArrayList<>();
        ArrayList<Integer> threshold_on_end=new ArrayList<>();

        //Indices at which to extract the grayscale values for decoding the pixel id
        int[] index_new=new int []{0,11,23,35,47,59,71,83,95,107,119}; // 100 ms
        List<Integer> index1=new ArrayList<>(12);
        for(int i=0;i<index_new.length;i++){
            index1.add(index_new[i]);
        }

        int detection_start=0;
        try{
            MatVector contours=new MatVector();
            grabber.start();
            int counter_frames=0;
            int lengthinframes=grabber.getLengthInFrames();
            completepercentange.setMax(lengthinframes);
            //Main loop to iterate through the all the frames
            while ((grabbedimage = converter.convert(grabber.grabImage())) != null) { //grabImage instead of grab
                frameno=grabber.getFrameNumber();
                double completepercentage=(((frameno+1)/(double)(lengthinframes+1))*100);

                cvtColor(grabbedimage, grayimage, CV_BGR2GRAY);
                completepercentange.setProgress(frameno);
                //Draw the rois
//                for(int i=0;i<gray_x.size();i++){
//                    opencv_imgproc.circle(grabbedimage,new Point(gray_x.get(i),gray_y.get(i)),gray_r.get(i), Scalar.BLUE,5,CV_AA,0);
//                }

                //Tracking the grayvalues of the different rois
                if(detection_start==1) {
                    List<Integer> gray_int_cont =new ArrayList<>();
                    for(int i=0;i<gray_x.size();i++) {

                        //Create the mask for the pixel boundary
                        Mat circle=new Mat(height,width,CV_8UC1,new Scalar(0,0,0,0));
                        opencv_imgproc.circle(circle,new Point(gray_x.get(i),gray_y.get(i)),gray_r.get(i), Scalar.WHITE,-1,CV_AA,0);
                        bitwise_and(circle,grayimage,circle_roi);

                        //Find the average intensity within the roi
                        Scalar sum=opencv_core.sumElems(circle_roi);
                        int nonzero=countNonZero(circle);
                        double avg_gray = sum.get(0)/nonzero;

                        if(counter_frames==index1.get(0)){
                            threshold_on.add((int)avg_gray);
                        }

                        if(counter_frames==index1.get(1)){
                            threshold_off.add((int)avg_gray);
                        }

                        if(counter_frames==index1.get(9)){
                            threshold_off_end.add((int)avg_gray);
                        }

                        if(counter_frames==index1.get(10)){
                            threshold_on_end.add((int)avg_gray);
                        }

                        if(index1.contains(counter_frames)){
                            gray_int_cont.add((int)avg_gray);
                        }
                        circle.release();
                    }

                    if(index1.contains(counter_frames)) {
                        gray_int.add(gray_int_cont);
                    }
                    counter_frames++;
                }

                //When to start the detection process
                if(frameno==maxindex){
                    detection_start=1;
                }
                //displayimage(grabbedimage);
            }
        }catch(Exception e){
            debugtagprint("Stage3_Exeception","Exception",printflag);
            e.printStackTrace();
        }
        // Thresholds on/off in the start and end frames
        String thresh_on="",thresh_off="",thresh_on_end="",thresh_off_end="";
        for(int i=0;i<threshold_on.size();i++){
            thresh_on+=String.valueOf(threshold_on.get(i));
            thresh_off+=String.valueOf(threshold_off.get(i));
            thresh_on_end+=String.valueOf(threshold_on_end.get(i));
            thresh_off_end+=String.valueOf(threshold_off_end.get(i));
            thresh_off+=",";
            thresh_on+=",";
            thresh_off_end+=",";
            thresh_on_end+=",";

        }
        Log.d("thresh_on",String.valueOf(thresh_on));
        Log.d("thresh_off",String.valueOf(thresh_off));
        Log.d("thresh_on_end",String.valueOf(thresh_on_end));
        Log.d("thresh_off_end",String.valueOf(thresh_off_end));
        ArrayList<Integer> address=new ArrayList<Integer>(40);

        //Decode the pixel values by comparing with the thresholds
        for(int i=0;i<gray_x.size();i++) {
            String bit1 = "";
            Log.d("address","test");
            for (int j = 0; j < 7; j++) {
                List<Integer> temp = gray_int.get(j+2);
                // Log.d("address_int",String.valueOf(temp.get(i)));
                double interp_threshon=thresh_interp((double)index_new[0],(double)threshold_on.get(i),(double)index_new[10],(double)threshold_on_end.get(i),(double)index_new[j+2]);
                double interp_threshoff=thresh_interp((double)index_new[1],(double)threshold_off.get(i),(double)index_new[9],(double)threshold_off_end.get(i),(double)index_new[j+2]);
                int thresh=(int)((interp_threshoff+interp_threshon)/2);
                Log.d("thresh_tot",String.valueOf(thresh));
                if (temp.get(i) >thresh){
                    bit1 += "1";
                }
                else{
                    bit1 += "0";
                }
            }
            int decimalval=Integer.parseInt(bit1,2);
            address.add(decimalval);
            Log.d("address_val",String.valueOf(decimalval));
        }

        // Store the spatial map in the phone
        int pixcount=gray_x.size();
        int x[]=new int[pixcount];
        int y[]=new int[pixcount];
        int index_array[]=new int[pixcount];

        int k=0;
        for(int i=1;i<=pixels_count;i++){

            if(address.contains(i)){
                int index_pixel=address.indexOf(i);
                int x_pos=(int)(gray_x.get(index_pixel)+gray_r.get(index_pixel));
                int y_pos=(int)(gray_y.get(index_pixel)+gray_r.get(index_pixel));
                x_cont.add(x_pos);
                y_cont.add(y_pos);
                Log.d("pixelfound","yoloy");
                index.add(i);
                x[k]=x_pos;
                y[k]=y_pos;
                index_array[k]=i;
                k++;

            }

        }

        try {
            grabber.stop();
            debugtagprint("Stage3_Stopped","Done",printflag);
            File file = new File("/storage/emulated/0/scatter/map.txt");
            save(file);

            Gson gson=new Gson();
            jsonformat jsonformat1=new jsonformat();
            //String rotval=rotation[0]+","+rotation[1]+","+rotation[2]+","+rotation[3];
            //String transval=translation[0]+","+translation[1]+","+translation[2];
            jsonformat1.quaternion=HelloSceneformActivity.rotation;
            jsonformat1.translation=HelloSceneformActivity.translation;
            jsonformat1.x=x;
            jsonformat1.y=y;
            jsonformat1.index=index_array;
            jsonformat1.height=1920;
            jsonformat1.width=1080;
            jsonformat1.projectionmatrix=HelloSceneformActivity.projmtx;
            jsonformat1.captureid=HelloSceneformActivity.captureid;
            String g=gson.toJson(jsonformat1);

            createjson(getApplicationContext(), new File("/storage/emulated/0/scatter/calibmap.txt"),g);

            debugtagprint("Stage3_Mapfile","Saved",printflag);
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        Toastdisplayer("Processing Done");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                viewmap();
            }
        });
    }

    private void createjson(Context context, File fileName, String jsonString){
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

    public void viewmap()
    {
        Intent intent=new Intent(this,spatialmap.class);
        //Intent intent=new Intent(this,canvasview.class);
        startActivity(intent);
    }


    //Display the image on UI
    private void displayimage(Mat image){
        AndroidFrameConverter convertertobitmap= new AndroidFrameConverter();
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        org.bytedeco.opencv.opencv_core.Mat image1 = new Mat(1080, 1920, CvType.CV_8UC3);
        int channel_no=image.channels();
        Frame frame=null;
        Bitmap bmp;

        //Checking the if the image is RGB or Grayscale image
        if(channel_no==1) {
            cvtColor(image, image1, CV_GRAY2BGR);
            frame = converter.convert(image1);
        }else {
            frame = converter.convert(image);
        }
        bmp = convertertobitmap.convert(frame);

        Matrix matrix=new Matrix();
        matrix.postRotate(90);
        Bitmap newbitmap=Bitmap.createBitmap(bmp,0,0,bmp.getWidth(),bmp.getHeight(),matrix,true);
        final Bitmap finalBmp = newbitmap;
        int bmp_x=finalBmp.getHeight();
        int bmp_y=finalBmp.getWidth();
        Log.d("bmp_x1",String.valueOf(bmp_x));
        Log.d("bmp_y1",String.valueOf(bmp_y));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView1.setImageBitmap(finalBmp);
            }
        });


    }

    public void Toastdisplayer(final String text){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void debugtagprint(String TAG,String text,boolean printflag) {
        if (printflag) {
            Log.d(TAG, text);
        }
    }


    private void loadframes(){
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(filename);
        Frame grab=null;
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        org.bytedeco.opencv.opencv_core.Mat grabbed_image = new Mat(1080, 1920, CvType.CV_8UC3);
        org.bytedeco.opencv.opencv_core.Mat grayimage = new Mat(1080, 1920, CvType.CV_8UC3);
        AndroidFrameConverter convertertobitmap= new AndroidFrameConverter();

        Bitmap bmp;
        try {
            grabber.start();
            Log.d("frames_video",String.valueOf(grabber.getLengthInVideoFrames()));
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        int i=0;
        try {
            while ((grabbed_image = converter.convert(grabber.grabImage())) != null) {
                cvtColor(grabbed_image,grayimage,CV_BGR2GRAY);
                Log.d("framessave",String.valueOf(i));
                if(i<40) {
                    video_frame.add(grabbed_image.clone());
                    i++;
                }
                else{
                    break;
                }

            }
        }catch (FrameGrabber.Exception e) {
            Log.d("framessave","errror");

            e.printStackTrace();
        } finally {
            try {
                grabber.stop();
                Log.d("frames", "loadframes");
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        View decorView=getWindow().getDecorView();
        if(hasFocus){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);


            int temp2=imageView1.getWidth(); //2159
            int temp5=imageView1.getHeight(); //1080
            Log.d("size_img_w",String.valueOf(imageView1.getWidth()));
            Log.d("size_img_h",String.valueOf( imageView1.getHeight()));
        }
    }


    public void save(File filename) {
        //   int[] a = {1, 2, 3, 4, 5,6,7,8,9};
        // int[] x = {10, 100, 200, 300, 400,500,600,700,800,900};
        // int[] y = {10, 100, 200, 300, 400,500,600,700,800,900};
        FileOutputStream fos = null;
        BufferedWriter bw = null;
        try {
            FileWriter fw = new FileWriter(filename,true);
            bw = new BufferedWriter(fw);
            //bw.write("albert");
            String location=String.valueOf((int)(HelloSceneformActivity.sum_x*100))+","+String.valueOf((int)(HelloSceneformActivity.sum_y*100));
            bw.write(location);
            bw.newLine();

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

            bw.newLine();

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




    @Override
    protected void onResume() {
        super.onResume();
        View decorView=getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }



}
