package com.example.xiewujie.circleimageviewactivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private CircleImageView circleImageView;
    Handler handler = new Handler();
    private String address = "http://ov80qs5d9.bkt.clouddn.com/1_201108022210121gfzQ.jpg";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        circleImageView = (CircleImageView)findViewById(R.id.circle_view);
       // setBitmap();
        //circleImageView.setBitmapFromUrl(address);
       // circleImageView.setBitmapFromResource(R.drawable.p_3);
    }
    private void setBitmap(){
        HttpGetUtil.getInputStram(address, new GetInputListener() {
            @Override
            public void onInputStream(InputStream inputStream) {
                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        circleImageView.setBitmap(bitmap);
                    }
                });
            }
        });
    }
}
