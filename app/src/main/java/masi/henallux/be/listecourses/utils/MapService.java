package masi.henallux.be.listecourses.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import masi.henallux.be.listecourses.model.Shop;
import retrofit2.http.Url;

/**
 * Created by Arthur on 12-10-17.
 */

public class MapService {
    private Context ctx;
    private FileService fileService;

    public MapService(Context ctx) {

        this.ctx = ctx;
        fileService = new FileService(ctx);
    }

    /**
     * Returns an Uri pointing to a PNG file illustrating the
     * position of the shop on a map (or null if an exception has occured)
     *
     * @param shop
     * @return the Uri of the PNG file
     */
    public Uri getMapOfShop(Shop shop) {
        try {

            String encoded = Constants.URL_PREFIX_GOOGLE_MAPS +
                    Uri.encode(shop.getName()) +
                    "%20" +
                    Uri.encode(shop.getAddress().toString());

            URL url = new URL(encoded);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.connect();
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {

                //Download picture from URL
                InputStream in = httpURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                in.close();

                //Store picture in files as URI
                return fileService.saveBitmap(bitmap,"Pic_" + shop.getId());
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
