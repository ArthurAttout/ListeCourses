package masi.henallux.be.listecourses.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import static android.os.Environment.getExternalStorageDirectory;

/**
 * Created by Le Roi Arthur on 15-10-17.
 */

public class FileService {

    private Context ctx;

    public FileService(Context ctx) {
        this.ctx = ctx;
    }

    public Uri saveBitmap(Bitmap bitmap, String name){
        FileOutputStream out = null;
        File f = new File(ctx.getFilesDir(), name);
        f.delete(); //Ensure to delete an eventual old file
        f = new File(ctx.getFilesDir(), name);
        try {
            out = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        return Uri.fromFile(f);
    }

    /**
     * Creates a new file in the internal storage from an Uri and returns the new URI pointing to the new file
     * @param uri the URI pointing to the file to duplicate
     * @param name the name of the file
     * @return the new URI
     */
    public Uri saveUri(Uri uri, String name) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try
        {
            ContentResolver content = ctx.getContentResolver();
            inputStream = content.openInputStream(uri);
            // create a directory
            File saveDirectory = new File(ctx.getFilesDir(),name);
            outputStream = new FileOutputStream(saveDirectory);

            byte[] buffer = new byte[1000];
            while (inputStream.read( buffer, 0, buffer.length) >= 0 )
            {
                outputStream.write(buffer, 0, buffer.length);
            }
            return Uri.fromFile(saveDirectory);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
