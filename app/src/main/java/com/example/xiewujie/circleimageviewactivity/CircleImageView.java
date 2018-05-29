package com.example.xiewujie.circleimageviewactivity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by xiewujie on 2018/3/20.
 */

public class CircleImageView extends View {
    private Bitmap bitmap;
    private Paint mPaint;
    private int resId;
    private boolean isSetBitmap;
    private String srcUrl;
    public CircleImageView(Context context) {
        super(context);
    }

    private PorterDuffXfermode duffXfermode;
    public CircleImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs,R.styleable.CircleImageView);
         resId = array.getResourceId(R.styleable.CircleImageView_src,R.drawable.ic_launcher_background);
        initPaint();
        array.recycle();
    }
    private void initPaint(){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
        mPaint.setFilterBitmap(true);
        duffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    }
    public int  getBitmapSize(){
        int size = 0;
        if (resId!=0){
            BitmapFactory.Options options = new BitmapFactory.Options();
            BitmapFactory.decodeResource(getResources(),resId,options);
            int height = options.outHeight;
            int width = options.outWidth;
            size = Math.min(height,width);
        }
        return size;
    }
    private Bitmap makeCircle(){
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int resultSize = getResultSize();
        Bitmap circleBitmap = Bitmap.createBitmap(resultSize,resultSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circleBitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLUE);
        canvas.drawCircle(resultSize/2+paddingLeft,resultSize/2+paddingTop,resultSize/2,paint);
        return circleBitmap;
    }

    private int getResultSize(){
        int size = getMeasuredHeight();
        return  Math.min(size-getPaddingLeft()-getPaddingRight(),size-getPaddingTop()-getPaddingBottom());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int size;
        if (widthMode==heightMode&&widthMode==MeasureSpec.AT_MOST){
            size = getBitmapSize();
        }else if (widthMode==MeasureSpec.AT_MOST){
            size = height;
        }else if (heightMode==MeasureSpec.AT_MOST){
            size = width;
        }else {
            size = Math.min(height,width);
        }
        setMeasuredDimension(size,size);
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        isSetBitmap = true;
        invalidate();
    }

    public void setBitmapFromUrl(String url){
        this.srcUrl = url;
        getInputStram(url, new GetInputListener() {
            @Override
            public void onInputStream(InputStream inputStream) {
                try {
                    int resultSize = getResultSize();
                    final Bitmap bitmap1 =decodeBitmapFromInputStream(inputStream,resultSize,resultSize);
                    post(new Runnable() {
                        @Override
                        public void run() {
                            bitmap= bitmap1;
                            invalidate();
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void setBitmapFromResource(int resId){
        this.resId =resId;
        invalidate();
    }

    private Bitmap getBitmap(){ int resultSize = getResultSize();
    if (!isSetBitmap&&srcUrl==null)
      bitmap = decodeSampledBitmapFromResource(getResources(),resId,resultSize,resultSize);
      return bitmap;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int resultSize = getResultSize();
        int sc = canvas.saveLayer(0,0,getWidth(),getHeight(),mPaint,Canvas.ALL_SAVE_FLAG);
        Bitmap de = makeCircle();
        canvas.drawBitmap(de,0,0,mPaint);
        bitmap = getBitmap();
        if (bitmap!=null) {
            mPaint.setXfermode(duffXfermode);
            bitmap = zooImag(bitmap, resultSize, resultSize);
            canvas.drawBitmap(bitmap, getPaddingLeft(), getPaddingTop(), mPaint);
            mPaint.setXfermode(null);
        }
       canvas.restoreToCount(sc);
    }
    /*
     *缩放图片
     */
    private Bitmap zooImag(Bitmap bm,int newWidth,int newHight){
        int width = bm.getWidth();
        int height = bm.getHeight();
        int size = Math.min(width,height);
        float scaleWidth = ((float)newWidth)/size;
        float scaleHeight = ((float)newHight)/size;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth,scaleHeight);
        Bitmap newBitmap = Bitmap.createBitmap(bm,0,0,width,height,matrix,true);
        return newBitmap;
    }

    private Bitmap decodeSampledBitmapFromResource(Resources res,int resId,int reqWidth,int reqHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res,resId,options);
        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res,resId,options);
    }

    private  Bitmap decodeBitmapFromInputStream(InputStream in,int reqWidth,int reqHeight){
        BufferedInputStream inputStream = new BufferedInputStream(in); //BufferedInputStream 支持来回读写操作
        inputStream.mark(Integer.MAX_VALUE);//标记流的初始位置
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream,null,options);
        options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds = false;
        try {
            inputStream.reset(); //重置读写时流的初始位置
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(inputStream,null,options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options,int reqWidth,int reqHeight){
        if (reqHeight==0||reqWidth==0){
            return 1;
        }
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height>reqHeight||width>reqWidth){
            final int halfHeight = height/2;
            final int halfWidth = width/2;
            while (halfHeight/inSampleSize>reqHeight&&halfWidth/inSampleSize>reqWidth){
                inSampleSize*=2;
            }
        }
        return inSampleSize;
    }

    //从网络获取输入流
    public static void getInputStram(final String address, final GetInputListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection=(HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(8000);
                    connection.setConnectTimeout(8000);
                    inputStream = connection.getInputStream();
                    if (listener!=null){
                        listener.onInputStream(inputStream);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if (connection!=null)
                        connection.disconnect();
                    if (inputStream!=null)
                        try {
                            inputStream.close();
                        }catch (IOException i){
                            i.printStackTrace();
                        }
                }
            }
        }).start();
    }
  //回掉接口
    interface GetInputListener{
        void onInputStream(InputStream inputStream);
    }
}
