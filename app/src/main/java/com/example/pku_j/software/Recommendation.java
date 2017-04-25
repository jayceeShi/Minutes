package com.example.pku_j.software;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Recommendation {
    public String DeepLink;
    public String Source;
    public String Title;
    public int Period;
    public java.util.Date DateTime;
    public String Abstract;
    public String ThumbnailURL;
    public String DeepLinkSchema;

    private Bitmap thumbnail;


    public double getPriority() {
        switch (this.Source) {
        case "keep":
            return 2.0;
        case "jd":
            return 1.8;
        case "youku":
            return 1.6;
        case "zhihu":
            return 1.0;
        case "toutiao":
            return 0.8;
        default:
            return 0;
        }
    }


    private static byte[] readInputStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while( (len=inStream.read(buffer)) != -1 ){
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
    }

    private static byte[] getImageFromNetByUrl(String strUrl){
        try {
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5 * 1000);
            InputStream inStream = conn.getInputStream();//通过输入流获取图片数据
            byte[] btImg = readInputStream(inStream);//得到图片的二进制数据
            return btImg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap getThumbnail()
    {
        if (this.thumbnail == null) {
            byte[] imageBytes = getImageFromNetByUrl(this.ThumbnailURL);
            if (imageBytes != null) {
                this.thumbnail = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                Log.d("trace~", String.format("Downloaded thumbnail: [%s] %s", this.Source, this.Title));
            }
            else {
                Log.d("trace~", String.format("Downloaded error: [%s] %s", this.Source, this.Title));
            }
        }
        return this.thumbnail;
    }

}
