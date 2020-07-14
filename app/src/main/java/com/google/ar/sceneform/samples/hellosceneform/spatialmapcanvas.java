package com.google.ar.sceneform.samples.hellosceneform;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class spatialmapcanvas extends View {

    public static List<Integer> index;
    public static List<Integer> x;
    public static List<Integer> y;
    int touched[]=new int[100];
    private Paint paint=new Paint();
    Paint red,blue,green,green_fill,white_fill;
    int count=0;
    private int x_base=250;
    private int y_base=600;
    public static Mat inputimage;
    private int scale=0;
    private static final Logger logger = Logger.getLogger(canvasview.class.getName());
    public static Bitmap bmp,bmp_resized;
    public static String disp_recv_config=null;
    private static String hex;
    public static String target = "192.168.0.17:50051"; //need to mention the ipv4 address shown in the computer instead of using local host
    public static int counter=0;
    public spatialmapcanvas(Context context) {
        super(context);
        //Load the spatial map from the text file
        File file = new File("/storage/emulated/0/scatter/map.txt");
        List<List<Integer>> map_values=read(file);
        index=map_values.get(0);
        x=map_values.get(1);
        y=map_values.get(2);

        // Creating color object and setting their attributes
        blue=new Paint();
        blue.setColor(Color.BLUE);
        blue.setStyle(Paint.Style.FILL);
        red=new Paint();
        red.setColor(Color.RED);
        red.setStyle(Paint.Style.FILL);
        green=new Paint();
        green.setColor(Color.GREEN);
        green.setStyle(Paint.Style.STROKE);
        green.setStrokeWidth(2);
        green_fill=new Paint();
        green_fill.setColor(Color.GREEN);
        green_fill.setStyle(Paint.Style.FILL);
        white_fill=new Paint();
        white_fill.setColor(Color.WHITE);
        white_fill.setStyle(Paint.Style.FILL);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        String buffer = ""; // String containing the state of all the pixels forming the display
        List<Boolean> test = dist(spatialmap.x_touch, spatialmap.y_touch, 50);

        for (int i = 0; i < index.size(); i++) {
            int y1 = x.get(i); // needed when getting the data from the txt filed
            int x1 = 1080 - y.get(i); //needed when getting the data from the txt filed
            canvas.drawCircle(x1 / 1, y1 / 1, 25, blue);
        }

      // Display the pixel configuration obtained from the python server
//        if (canvasview.configfit[canvasview.dispcounter] != null) {
//            String temp=canvasview.configfit[canvasview.dispcounter];
//            char[] char_disp_recv = temp.toCharArray();
//            for (int i = 0; i < temp.length(); i++) {
//                int x1 = 1080 - y.get(i);
//                int y1 = x.get(i);
////                int x1 = x.get(i);
////                int y1 = y.get(i);
//                if (char_disp_recv[i] == '1') {
//                    canvas.drawCircle(x1 / scale, y1 / scale, 25, red);
//                    buffer += '1';
//                } else {
//                    canvas.drawCircle(x1 / scale, y1 / scale, 25, white_fill);
//                    buffer += '0';
//                }
//            }
//
//
//            //Padding with zeros to make the the string length divisible by 4
//            if ((index.size() % 4) != 0) {
//                int padding = index.size() / 4;
//                int paddingsep = index.size() % 4;
//                for (int i = (4 * padding + paddingsep); i < 4 * (padding + 1); i++) {
//                    buffer += '0';
//                }
//            }

            //Convert the binary string into hex values to be sent to all the scatter display pixels
//            Log.d("Messagesent_bin", String.valueOf(buffer)); //Binary value
//            BigInteger bigInteger = new BigInteger(buffer, 2);
//            hex = String.format("%025X", bigInteger);
//            Log.d("Messagesent_hex", String.valueOf(hex));
//            calibration3_record.bluetooth_send("a6" + hex + '\n');
        }


    //Get the bitmap image for a given bitmap image
    public static Bitmap getBitmap(String filename){
        inputimage = opencv_imgcodecs.imread(filename, opencv_imgcodecs.IMREAD_COLOR);
        OpenCVFrameConverter converter1 = new OpenCVFrameConverter.ToMat();
        AndroidFrameConverter convertertobitmap = new AndroidFrameConverter();
        Frame frame = converter1.convert(inputimage);
        bmp = convertertobitmap.convert(frame);
        return bmp;
    }

    //Hittest for only a single given point
    private List<Boolean> dist(int x1,int y1,int rad){
        List<Boolean> touchtrack=new ArrayList<>(20);
        Log.d("sending_blue","yolo");

        int len=index.size();
        for(int i=0;i<len ;i++){
            double dist=Math.pow(1080-y.get(i)-x1,2)+Math.pow(x.get(i)-y1,2);
            int dist1= (int)Math.sqrt(dist);
            if(dist1<rad){
                touchtrack.add(true);
            }
            else{
                touchtrack.add(false);
            }
            Log.d("touched",String.valueOf(dist1));
        }

        return touchtrack;
    }

    //Read the values from the spatial map text file
    public List<List<Integer>> read(File filename){
        String[] val_line=new String[15];
        BufferedReader br=null;
        int i=0;

        try{
            FileReader fr=new FileReader(filename);
            br=new BufferedReader(fr);
            String line;
            while((line=br.readLine())!=null) {
                Log.d("Fileread",line);
                val_line[i]=line;
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(br!=null)
            {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        List<Integer> id_int=new ArrayList<>();
        List<Integer> x_int=new ArrayList<>();
        List<Integer> y_int=new ArrayList<>();
        List<List<Integer>> result = new ArrayList<List<Integer>>();
        int counter_step=0;
        Log.d("Spatialmap_leni",String.valueOf(i));
        for(int j=0;j<i/4;j++) {
            List<String> id = Arrays.asList(val_line[counter_step + 1].split(","));
            List<String> x = Arrays.asList(val_line[counter_step + 2].split(","));
            List<String> y = Arrays.asList(val_line[counter_step + 3].split(","));
            for (String s : id) id_int.add(Integer.valueOf(s));
            for (String s : x) x_int.add(Integer.valueOf(s));
            for (String s : y) y_int.add(Integer.valueOf(s));
            result.add(id_int);
            result.add(x_int);
            result.add(y_int);
            counter_step+=4;
        }
        counter=counter+4;
        return result;
    }

}
