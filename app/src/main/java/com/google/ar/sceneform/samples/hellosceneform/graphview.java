package com.google.ar.sceneform.samples.hellosceneform;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class graphview extends AppCompatActivity {
    LineGraphSeries<DataPoint> series,series2,series3,series4,series5,series6,series7,series8,series9,series10,series11,series12,series13,series14,series15,series16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphview);
        double x,y;
        x=0;
        GraphView graph=(GraphView)findViewById(R.id.graph);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(60);
        graph.getViewport().setMaxY(255);
        graph.getViewport().setMinY(0);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
        series=new LineGraphSeries<DataPoint>();
        series2=new LineGraphSeries<DataPoint>();
        series3=new LineGraphSeries<DataPoint>();
        series4=new LineGraphSeries<DataPoint>();
        series5=new LineGraphSeries<DataPoint>();
        series6=new LineGraphSeries<DataPoint>();
        series7=new LineGraphSeries<DataPoint>();
        series8=new LineGraphSeries<DataPoint>();
        series9=new LineGraphSeries<DataPoint>();
        series10=new LineGraphSeries<DataPoint>();
        series11=new LineGraphSeries<DataPoint>();
        series12=new LineGraphSeries<DataPoint>();
        series13=new LineGraphSeries<DataPoint>();
        series14=new LineGraphSeries<DataPoint>();
        series15=new LineGraphSeries<DataPoint>();
        series16=new LineGraphSeries<DataPoint>();





        int length=HelloSceneformActivity.touch_y.size();
        int length1=HelloSceneformActivity.touch_x.size();

       // Log.d("intensity_length",String.valueOf(length));
        int sep=length/7;
        //Log.d("intensity_sep",String.valueOf(sep));

        int intensity=0;
        int ii=0;
        for(int k=0;k<7;k++) {
            for (int j = 0; j <=sep ; j++) {
                if (j != sep) {
                    if(ii<length) {
                        //   intensity += MainActivity.touch_x.get(ii);
                        if (j == sep / 2) {
                            intensity = HelloSceneformActivity.touch_x.get(ii);
                        }
                    }
                }
                if (j == sep) {
                    if(intensity>240) {
                        intensity=1;
                    }
                    else{
                        intensity=0;
                    }
                 //  Log.d("intensity", String.valueOf(intensity ));
                    intensity = 0;
                }
                ii++;
            }
        }
//        for(int i=0;i<length;i++){
//           // x=HelloSceneformActivity.timetracker.get(i);
//            y=HelloSceneformActivity.touch_y.get(i);
//            series.appendData(new DataPoint(x,y),true,length);
//            x=x+1;
//
//            //Log.d("intensity_tracking",String.valueOf(i));
//        }
        x=0;
        Log.d("touch_xsize",String.valueOf(HelloSceneformActivity.touch_x.size()));
        for(int i=0;i<length1;i++){
            // x=HelloSceneformActivity.timetracker.get(i);
            y=HelloSceneformActivity.touch_x.get(i);
            series13.appendData(new DataPoint(x,y),true,length1);
            x=x+1;

            //Log.d("intensity_tracking",String.valueOf(i));
        }
        int maxindex=HelloSceneformActivity.max_index;
       // double sep1=(double)((length1-HelloSceneformActivity.center_index)/11.0);
        double sep1=(double)((length1)/11.0);

//        for(int i=0;i<2;i++){
//            x=x+1.5;
//            y=HelloSceneformActivity.touch_x.get(i);
//            series.appendData(new DataPoint(x,y),true,length);
//            //Log.d("intensity_tracking",String.valueOf(i));
//        }

//        for(int i=0;i<10;i++) {
//            series2.appendData(new DataPoint(HelloSceneformActivity.framesep.get(i), 0), true, length);
//            series2.appendData(new DataPoint(HelloSceneformActivity.framesep.get(i), 255), true, length);
//        }
//        double sep1=(double)HelloSceneformActivity.framesep_graph.get(0)/10;
//        Log.d("sep",String.valueOf(sep1));
        double base=HelloSceneformActivity.center_index;
        base=2;
        length=2;

        series14.appendData(new DataPoint(base, 0), true, length);
        series14.appendData(new DataPoint(base, 255), true, length);

        series2.appendData(new DataPoint(sep1+base, 0), true, length);
        series2.appendData(new DataPoint(sep1+base, 255), true, length);

        series3.appendData(new DataPoint(sep1*2+base, 0), true, length);
        series3.appendData(new DataPoint(sep1*2+base, 255), true, length);

        series4.appendData(new DataPoint(sep1*3+base, 0), true, length);
        series4.appendData(new DataPoint(sep1*3+base, 255), true, length);

        series5.appendData(new DataPoint(sep1*4+base, 0), true, length);
        series5.appendData(new DataPoint(sep1*4+base, 255), true, length);

        series6.appendData(new DataPoint(sep1*5+base, 0), true, length);
        series6.appendData(new DataPoint(sep1*5+base, 255), true, length);

        series7.appendData(new DataPoint(sep1*6+base, 0), true, length);
        series7.appendData(new DataPoint(sep1*6+base, 255), true, length);

        series8.appendData(new DataPoint(sep1*7+base, 0), true, length);
        series8.appendData(new DataPoint(sep1*7+base, 255), true, length);

        series9.appendData(new DataPoint(sep1*8+base, 0), true, length);
        series9.appendData(new DataPoint(sep1*8+base, 255), true, length);

        series10.appendData(new DataPoint(sep1*9+base, 0), true, length);
        series10.appendData(new DataPoint(sep1*9+base, 255), true, length);

        series11.appendData(new DataPoint(sep1*10+base, 0), true, length);
        series11.appendData(new DataPoint(sep1*10+base, 255), true, length);

        series15.appendData(new DataPoint(sep1*11+base, 0), true, length);
        series15.appendData(new DataPoint(sep1*11+base, 255), true, length);

        series16.appendData(new DataPoint(sep1*12+base, 0), true, length);
        series16.appendData(new DataPoint(sep1*12+base, 255), true, length);


        series.appendData(new DataPoint(HelloSceneformActivity.threshstart_index,HelloSceneformActivity.threshstart_int), true, length);
        series.appendData(new DataPoint(HelloSceneformActivity.threshend_index,HelloSceneformActivity.threshend_int), true, length);
        //basse+sep1-1 //2


//        int new_start=HelloSceneformActivity.new_start;
//        int length2=HelloSceneformActivity.touch_y.size()/9;
//        double sep1=(double)HelloSceneformActivity.touch_y.size()/9.0;
//        double base=0.0;
//        series2.appendData(new DataPoint(sep1/2+0*sep1, 0), true, length);
//        series2.appendData(new DataPoint(sep1/2+0*sep1, 255), true, length);
//
//
//        series3.appendData(new DataPoint(sep1/2+1*sep1, 0), true, length);
//        series3.appendData(new DataPoint(sep1/2+1*sep1, 255), true, length);
//
//        series4.appendData(new DataPoint(sep1/2+2*sep1, 0), true, length);
//        series4.appendData(new DataPoint(sep1/2+2*sep1, 255), true, length);
//
//        series5.appendData(new DataPoint(sep1/2+3*sep1, 0), true, length);
//        series5.appendData(new DataPoint(sep1/2+3*sep1, 255), true, length);
//
//        series6.appendData(new DataPoint(sep1/2+4*sep1, 0), true, length);
//        series6.appendData(new DataPoint(sep1/2+4*sep1, 255), true, length);
//
//        series7.appendData(new DataPoint(sep1/2+5*sep1, 0), true, length);
//        series7.appendData(new DataPoint(sep1/2+5*sep1, 255), true, length);
//
//        series8.appendData(new DataPoint(sep1/2+6*sep1, 0), true, length);
//        series8.appendData(new DataPoint(sep1/2+6*sep1,255), true, length);
//
//        series9.appendData(new DataPoint(sep1/2+7*sep1, 0), true, length);
//        series9.appendData(new DataPoint(sep1/2+7*sep1, 255), true, length);
//
//        series10.appendData(new DataPoint(sep1/2+8*sep1, 0), true, length);
//        series10.appendData(new DataPoint(sep1/2+8*sep1, 255), true, length);
//
//        series11.appendData(new DataPoint(sep1/2+9*sep1, 0), true, length);
//        series11.appendData(new DataPoint(sep1/2+9*sep1, 255), true, length);
//
//               series12.appendData(new DataPoint(0,HelloSceneformActivity.thresh), true, length);
//               series12.appendData(new DataPoint(40,HelloSceneformActivity.thresh), true, length);
//               series12.setColor(Color.RED);
//        series13.setColor(Color.GREEN);


//        series2.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(0),0), true, length);
//        series2.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(0), 255), true, length);
//
//        series3.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(1), 0), true, length);
//        series3.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(1), 255), true, length);
//
//        series4.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(2), 0), true, length);
//        series4.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(2), 255), true, length);
//
//        series5.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(3), 0), true, length);
//        series5.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(3), 255), true, length);
//
//        series6.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(4), 0), true, length);
//        series6.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(4), 255), true, length);
//
//        series7.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(5), 0), true, length);
//        series7.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(5), 255), true, length);
//
//        series8.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(6), 0), true, length);
//        series8.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(6), 255), true, length);
//
//        series9.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(7), 0), true, length);
//        series9.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(7), 255), true, length);
//
//        series10.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(8), 0), true, length);
//        series10.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(8), 255), true, length);
//
//        series11.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(9), 0), true, length);
//        series11.appendData(new DataPoint(HelloSceneformActivity.framesep_graph.get(9), 255), true, length);

//        series12.appendData(new DataPoint(HelloSceneformActivity.framesep.get(10), 0), true, length);
//        series12.appendData(new DataPoint(HelloSceneformActivity.framesep.get(10), 255), true, length);


        series.setColor(Color.RED);
        graph.addSeries(series);
        graph.addSeries(series14);
        graph.addSeries(series2);
        graph.addSeries(series3);
        graph.addSeries(series4);
        graph.addSeries(series5);
        graph.addSeries(series6);
        graph.addSeries(series7);
        graph.addSeries(series8);
        graph.addSeries(series9);
        graph.addSeries(series10);
        graph.addSeries(series11);
       // graph.addSeries(series12);
        graph.addSeries(series13);

        HelloSceneformActivity.touch_x.clear();
        HelloSceneformActivity.max=0;
        HelloSceneformActivity.max_index=0;

        HelloSceneformActivity.min=255;

     //   HelloSceneformActivity.touch_x.clear();
     //   HelloSceneformActivity.t.clear();

    }
    }

