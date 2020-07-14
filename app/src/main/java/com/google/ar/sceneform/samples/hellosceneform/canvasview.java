package com.google.ar.sceneform.samples.hellosceneform;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class canvasview extends View {
    public List<Integer> index;
    public List<Integer> x;
    public List<Integer> y;
    public List<Integer> x_new1=new ArrayList<>(20);;
    public List<Integer> y_new1=new ArrayList<>(20);;


    int touched[]=new int[100];
    private Paint paint=new Paint();
    Paint red;
    Paint blue,black,white;

    public canvasview(Context context) {
        super(context);
        File file = new File("/storage/emulated/0/scatter/calibmap.txt");

        List<List<Integer>> map_values=read(file);
        index=map_values.get(0);
        x=map_values.get(1);
        y=map_values.get(2);

        blue=new Paint();
        blue.setColor(Color.BLUE);
        blue.setStyle(Paint.Style.FILL);

        red=new Paint();
        red.setColor(Color.RED);
        red.setStyle(Paint.Style.FILL);


        black=new Paint();
        black.setColor(Color.BLACK);
        black.setStyle(Paint.Style.FILL);

        white=new Paint();
        white.setColor(Color.WHITE);
        white.setStyle(Paint.Style.FILL);
        int max_x= Collections.max(x);
        int max_y= Collections.max(y);
        int min_x=Collections.min(x);
        int min_y=Collections.min(y);
        Log.d("maxes",String.valueOf(max_x));

//        Log.d("size_canvas",String.valueOf(h));
//        Log.d("size_canvas",String.valueOf(w));
        int padding_x=250;
        int padding_y=250;
        for(int i=0;i<x.size();i++) {
            int x1 = x.get(i); // needed when getting the data from the txt filed
            int y1 = y.get(i); //needed when getting the data from the txt filed
            if (max_x != 0 && max_y != 0) {
                int x_new = (int) map(x1, min_x, max_x, padding_x, 2100-padding_x);
                int y_new = (int) map(y1, min_y, max_y, padding_y, 1080-padding_y);

                Log.d("size", String.valueOf(x1));
                Log.d("size1", String.valueOf(y_new));

                y1=2100-x_new;
                y_new1.add(y1);
                x_new1.add(y_new);
            }
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        WindowManager wm=(WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display=wm.getDefaultDisplay();
        super.onDraw(canvas);

        String buffer1="",hex="";
        canvas.drawPaint(black);
        //canvas.drawCircle(0,0,150,red);
        //canvas.drawCircle(400,0,150,blue);

        List<Boolean> test = dist(mapactivity.x_touch, mapactivity.y_touch, 50);
        for(int i=1;i<=100;i++){
            if(index.contains(i)) {
                Log.d("canvasview",String.valueOf(i));
                int index1=index.indexOf(i);
                int x1 = x_new1.get(index1);
                int y1 = y_new1.get(index1);

                if (touched[index1] == 0) {
                    if (test.get(index1)) {
                        canvas.drawCircle(x1, y1, 25, red);
                        Log.d("touched", "red");
                        touched[index1] = 1;
                    } else {
                        canvas.drawCircle(x1, y1, 25, white);
                    }
                } else {
                    if (test.get(index1)) {
                        canvas.drawCircle(x1, y1, 25, white);
                        Log.d("touched", "red");
                        touched[index1] = 0;

                    } else {
                        canvas.drawCircle(x1, y1, 25, red);
                    }
                }
                buffer1 += String.valueOf(touched[index1]);
            }
            else{
                buffer1 += "0";
            }
        }

        Log.d("Messagelength",String.valueOf(buffer1.length()));
        Log.d("Messagesent_bin", String.valueOf(buffer1)); //Binary value
        BigInteger decimal1=new BigInteger(buffer1,2);
        hex = String.format("%25X", decimal1);
        Log.d("Messagesent", String.valueOf(hex));
        HelloSceneformActivity.bluetooth_send("a6" + hex + '\n');

    }


    public List<List<Integer>> read(File filename)
    {
        String[] val_line=new String[3];

        BufferedReader br=null;
        try{
            FileReader fr=new FileReader(filename);
            br=new BufferedReader(fr);
            String line;
            int i=0;
            while((line=br.readLine())!=null)
            {
                Log.d("fileread",line);
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

        List<String> id=Arrays.asList(val_line[0].split(","));
        List<String> x=Arrays.asList(val_line[1].split(","));
        List<String> y=Arrays.asList(val_line[2].split(","));
        for(String s:id) id_int.add(Integer.valueOf(s));
        for(String s:x) x_int.add(Integer.valueOf(s));
        for(String s:y) y_int.add(Integer.valueOf(s));
        List<List<Integer>> result=new ArrayList<List<Integer>>();
        result.add(id_int);
        result.add(x_int);
        result.add(y_int);
        Log.d("fileread","success");
        return result;
    }


    private double map(int x,int x1_low,int x2_low,int x1_high,int x2_high){
        double R=(x2_high-x1_high)/(x2_low-x1_low);
        double y=x1_high+ R*(x-x1_low);
        return y;

    }

    private List<Boolean> dist(int x1,int y1,int rad){
        List<Boolean> touchtrack=new ArrayList<>(20);
        Log.d("sending_blue","yolo");

        int len=index.size();
        for(int i=0;i<len ;i++){
            double dist=Math.pow(x_new1.get(i)-x1,2)+Math.pow(y_new1.get(i)-y1,2);
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

//    public List<List<Integer>> read(File filename)
//    {
//        String[] val_line=new String[3];
//
//        BufferedReader br=null;
//        try{
//            FileReader fr=new FileReader(filename);
//            br=new BufferedReader(fr);
//            String line;
//            int i=0;
//            while((line=br.readLine())!=null)
//            {
//                Log.d("fileread",line);
//                val_line[i]=line;
//                i++;
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            if(br!=null)
//            {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        List<Integer> id_int=new ArrayList<>();
//        List<Integer> x_int=new ArrayList<>();
//        List<Integer> y_int=new ArrayList<>();
//
//        List<String> id= Arrays.asList(val_line[0].split(","));
//        List<String> x=Arrays.asList(val_line[1].split(","));
//        List<String> y=Arrays.asList(val_line[2].split(","));
//        for(String s:id) id_int.add(Integer.valueOf(s));
//        for(String s:x) x_int.add(Integer.valueOf(s));
//        for(String s:y) y_int.add(Integer.valueOf(s));
//        List<List<Integer>> result=new ArrayList<List<Integer>>();
//        result.add(id_int);
//        result.add(x_int);
//        result.add(y_int);
//        return result;
//    }


}
