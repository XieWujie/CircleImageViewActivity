package com.example.xiewujie.circleimageviewactivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpGetUtil {
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
}
