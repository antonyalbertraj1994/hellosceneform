package com.google.ar.sceneform.samples.hellosceneform;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


public class calibration3_record extends AppCompatActivity {

    public static BluetoothSocket btSocket;
    public static final String uuid1="00001101-0000-1000-8000-00805f9b34fb";
    public BluetoothAdapter btAdapter;
    private Button optimal_fit;
    public static Button record,mapButton;
    private TextureView mTextureView;
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;
    private Size mPreviewSize;
    private Size mVideoSize;
    private MediaRecorder mMediaRecorder;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CameraConstrainedHighSpeedCaptureSession mPreviewSessionHighSpeed;
    private static final int REQUEST_CAMERA_PERMISSION_RESULT=0;
    private boolean mIsRecording=false;
    private String mVideoFilename;
    private int mTotalRotation;
    public static int bitcounter=0;
    private  static SparseIntArray ORIENTATIONS=new SparseIntArray();
    Surface recordSurface;


    static {
        ORIENTATIONS.append(Surface.ROTATION_0,0);
        ORIENTATIONS.append(Surface.ROTATION_90,90);
        ORIENTATIONS.append(Surface.ROTATION_180,180);
        ORIENTATIONS.append(Surface.ROTATION_270,270);
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener=new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Toast.makeText(getApplicationContext(),"Texture available",Toast.LENGTH_LONG).show();
            setupCamera(width,height);
            connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            //setupCamera(width,height);

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {


        }
    };

    private CameraDevice mCameraDevice;
    private CameraDevice.StateCallback mCameraDeviceStateCallback=new CameraDevice.StateCallback()
    {

        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice=camera;
            startPreview();
            //startpreview1();
            //framerecorder1();
            Toast.makeText(getApplicationContext(), "Camera connected", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice=null;
        }


        @Override
        public void onError( CameraDevice camera, int error) {
            camera.close();

            mCameraDevice=null;
        }
    };

    private static class CompareSizeByArea implements Comparator<Size>
    {
        @Override
        public int compare(Size lhs,Size rhs)
        {
            return Long.signum((long) lhs.getHeight()*lhs.getHeight()/(long)rhs.getWidth()*rhs.getHeight());
        }
    }

    private String mCameraId;
    private  void setupCamera(int width,int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);

                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)==CameraCharacteristics.LENS_FACING_FRONT)
                {

                    continue;
                }
                StreamConfigurationMap map=cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                Range<Integer> fps_val[]=cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
                int fps_val1 = cameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO;
                //Range<Integer> high_fps_val[]=map.getHighSpeedVideoFpsRanges();

                Range<Integer> v=cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);

                int g[]=cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
                int test[]=cameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);
                int deviceOrientation=getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation=sensorToDevice(cameraCharacteristics,deviceOrientation);
                boolean swapRotation= mTotalRotation==90 || mTotalRotation==270;
                int rotatedWidth=width;
                int rotatedHeight=height;
                if(swapRotation) {
                    rotatedHeight=width;
                    rotatedWidth=height;
                }
                mPreviewSize=chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),rotatedWidth,rotatedHeight);
                mVideoSize=chooseOptimalSize(map.getOutputSizes(MediaRecorder.class),rotatedHeight,rotatedWidth);

                mCameraId=cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void connectCamera()
    {
        CameraManager cameraManager=(CameraManager)getSystemService((Context.CAMERA_SERVICE));
        try {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
            {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraId,mCameraDeviceStateCallback,mBackgroundHandler);
                }
                else
                {
                    if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
                    {
                        Toast.makeText(getApplicationContext(),"access required to camera",Toast.LENGTH_LONG).show();
                    }
                    requestPermissions(new String[] {Manifest.permission.CAMERA},REQUEST_CAMERA_PERMISSION_RESULT);
                }
            }
            else{
                cameraManager.openCamera(mCameraId,mCameraDeviceStateCallback,mBackgroundHandler);

            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void startRecord()
    {
        try {
            setupMediaRecoder();
            SurfaceTexture surfaceTexture=mTextureView.getSurfaceTexture();
            //surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getWidth());
            surfaceTexture.setDefaultBufferSize(1920,1080);

            Surface previewSurface=new Surface(surfaceTexture);
            recordSurface=mMediaRecorder.getSurface();
            mCaptureRequestBuilder=mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            //mCaptureRequestBuilder=mCameraDevice

            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);
            Range<Integer> val= Range.create(120,120);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,val);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CameraMetadata.CONTROL_MODE_AUTO);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,CameraCharacteristics.CONTROL_AE_MODE_ON);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_LOCK,true);

            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,-4);

            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON);
            //mCaptureRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON);

            mCameraDevice.createConstrainedHighSpeedCaptureSession((Arrays.asList(previewSurface, recordSurface)), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        mPreviewSessionHighSpeed=(CameraConstrainedHighSpeedCaptureSession)session;
                        List<CaptureRequest> mPreviewBuilderBurst=mPreviewSessionHighSpeed.createHighSpeedRequestList(mCaptureRequestBuilder.build());
                        mPreviewSessionHighSpeed.setRepeatingBurst(mPreviewBuilderBurst,null,mBackgroundHandler);
                        // int a=(mPreviewBuilderBurst.get(2)).get(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE);
                        //int a=mCaptureRequestBuilder.get(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE);
                        // Log.d("video_stabili",String.valueOf(a));
                        // session.setRepeatingRequest(mCaptureRequestBuilder.build(),null,null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            },null );
        }catch (IOException | CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        SurfaceTexture surfaceTexture=mTextureView.getSurfaceTexture();

        // surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getWidth());
        surfaceTexture.setDefaultBufferSize(1920, 1080);

        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(mCameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);
            Range<Integer> val = Range.create(120, 120);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, val);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_OFF_KEEP_STATE);


            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {

                        session.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Toast.makeText(getApplicationContext(), "unable to setup preview", Toast.LENGTH_SHORT).show();
                }
            }, null);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    protected void onPause()
    {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void closeCamera()
    {
        if(mCameraDevice!=null)
        {
            mCameraDevice.close();
            mCameraDevice=null;
        }
    }

    public static ImageView imageView;
    private Button mapbutton1,pixelallon,pixelalloff,hidebutton,basestation,onebit;
    private List<Integer> intent_val;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration3_record);
        record=(Button)findViewById(R.id.button);
        DisplayMetrics displayMetrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mTextureView=(TextureView)findViewById(R.id.textureView);
        //imageView=(ImageView)findViewById(R.id.imageView);
        //imageView.setScaleType(ImageView.ScaleType.FIT_XY);

//        intent_val=getIntent().getIntegerArrayListExtra("list");
//        Log.d("Intent_arrayval",String.valueOf(intent_val.size()));
//        Log.d("intentval_values",String.valueOf(intent_val.get(0))+","+String.valueOf(intent_val.get(1)));

        mapButton=(Button)findViewById(R.id.mapbutton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openmapactivity();
            }
        });

        mapbutton1=(Button)findViewById(R.id.mapviewbutton);
        mapbutton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetooth_send("a60000000000000000000000000\n");
                openmapactivity1();


            }
        });

        File file = new File("/storage/emulated/0/scatter/map.txt");


        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsRecording==false)
                {
                    //bluetooth_send("a60000000000000000000000000\n");
                    createfile();
                    mMediaRecorder=new MediaRecorder();
                    record.setEnabled(false);
                    record.setText("RECORDING");
                    mIsRecording=true;
                    checkWriteStoragePermission();


                }
                else
                {

                    //record.setText("RECORD");
                   // mIsRecording=false;
                    //mMediaRecorder.stop();
                    //mMediaRecorder.release();
                    startPreview();
                }
            }
        });
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d("connecteddevice", String.valueOf(btAdapter.getBondedDevices()));

        BluetoothDevice hc05 = btAdapter.getRemoteDevice("98:D3:31:20:9B:EB");
        Log.d("connecteddevice",hc05.getName());
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


    public void openmapactivity() {
        ReadInput r1=new ReadInput();
        r1.stop();
        Intent intent=new Intent(this,processactivity.class);
        startActivity(intent);
    }
    private String createfile()
    {
        String root= Environment.getExternalStorageDirectory().getAbsolutePath();
        mVideoFilename =root+"/scatter/"+"antony.mp4";
        return mVideoFilename;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CAMERA_PERMISSION_RESULT)
        {
            if(grantResults[0]!=PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(getApplicationContext(),"Cannot without access to camera",Toast.LENGTH_LONG).show();
            }
        }
    }
    public void openmapactivity1()
    {
        Intent intent=new Intent(this,canvasview.class);
        startActivity(intent);
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        startBackgroundThread();
        if(mTextureView.isAvailable())
        {
            setupCamera(mTextureView.getWidth(),mTextureView.getHeight());
            connectCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
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
    private void startBackgroundThread(){
        mBackgroundHandlerThread=new HandlerThread("camera2");
        mBackgroundHandlerThread.start();
        mBackgroundHandler=new Handler(mBackgroundHandlerThread.getLooper());

    }

    private void stopBackgroundThread()
    {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread=null;
            mBackgroundHandler=null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    private static int sensorToDevice(CameraCharacteristics cameraCharacteristics,int deviceOrientation){
        int sensorOrientation=cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation=ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation+deviceOrientation+360)%360;

    }
    private Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough=new ArrayList<Size>();
        for(Size option:choices)
        {
            int h=option.getHeight();
            int w=option.getWidth();
            double aspect_ratio=h/w;
            if(h==1080 && w==1920)
            //if(h==w*(height/width) && h>=height && w>=width)
            {
                bigEnough.add(option);
                Toast.makeText(getApplicationContext(),"foundsize",Toast.LENGTH_SHORT).show();
            }
        }
        if(bigEnough.size()>0)
        {
            return Collections.min(bigEnough,new CompareSizeByArea());
        }
        else
        {
            return choices[18];
        }
    }
    private  void checkWriteStoragePermission()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)


                createfile();
            startRecord();
            Toast.makeText(getApplicationContext(), "Video permissiongranted", Toast.LENGTH_SHORT).show();

            mMediaRecorder.start();
            bluetooth_send("1\n");

        }

    }
    private  void setupMediaRecoder() throws IOException
    {
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoFilename);
        mMediaRecorder.setVideoEncodingBitRate(30000000);
        mMediaRecorder.setVideoFrameRate(120);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(),mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.prepare();

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
                    byte[] buffer=new byte[256];
                    if(inputStream.available()>0){
                        int bytes=inputStream.read(buffer);
                        final String message=new String(buffer,0,bytes);
                        //int len=message.length();
                        //Log.d("recieved",String.valueOf(len));
                        // Log.d("recieved",message);
                        bitcounter++;
                        if(bitcounter==1){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(!message.isEmpty()) {

                                        // Log.d("recieved", "videostopped");
                                        //
                                        //  mMediaRecorder.start();
                                    }
                                }
                            });
                        }else if(bitcounter==2) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!message.isEmpty()) {

                                        // Log.d("recieved", "videostopped");
                                        mIsRecording = false;
                                        mMediaRecorder.stop();
                                        mMediaRecorder.release();
                                        record.setEnabled(true);
                                        record.setText("Calibrate");
                                        bitcounter=0;

                                        startPreview();
                                    }
                                }
                            });
                        }
                        else{
                            bitcounter=0;
                        }



                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void stop(){
            bStop=true;
            try {
                btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                thisSocket.connect();;
                Log.d("connectedde","connected to hc-05");
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
}
