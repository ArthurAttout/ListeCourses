package masi.henallux.be.listecourses.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;

import masi.henallux.be.listecourses.R;
import masi.henallux.be.listecourses.dao.sqlModels.AddressEntity;
import masi.henallux.be.listecourses.dao.sqlModels.ItemEntity;
import masi.henallux.be.listecourses.dao.sqlModels.ShopEntity;


/**
 * Created by Arthur on 09-10-17.
 */

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shops.db";
    private static final int DATABASE_VERSION = 1;
    private Context context;
    private static SQLiteDatabase databaseInstance;

    public static final String CREATE_TABLE_ADDRESS =
    "create table "
            + AddressEntity.TABLE + "("
            + AddressEntity.COLUMN_ID + " integer primary key autoincrement, "
            + AddressEntity.COLUMN_NUMBER + " varchar(10) not null,"
            + AddressEntity.COLUMN_STREETNAME + " varchar(10) not null,"
            + AddressEntity.COLUMN_ZIPCODE + " varchar(10) not null,"
            + AddressEntity.COLUMN_CITY + " varchar(10) not null,"
            + AddressEntity.COLUMN_COUNTRY + " varchar(10) not null);";

    public static final String CREATE_TABLE_ITEMS =
    "create table "
            + ItemEntity.TABLE + "("
            + ItemEntity.COLUMN_ID + " integer primary key autoincrement, "
            + ItemEntity.COLUMN_NAME + " varchar(10) not null,"
            + ItemEntity.COLUMN_QUANTITY + " integer not null,"
            + ItemEntity.COLUMN_UNIT_TYPE + " varchar(10) not null,"
            + ItemEntity.COLUMN_FK_STORE + " integer not null, foreign key (" + ItemEntity.COLUMN_FK_STORE + ") references " + ShopEntity.TABLE + "(" + ShopEntity.COLUMN_ID + ")) ";

    public static final String CREATE_TABLE_SHOPS =
    "create table "
            + ShopEntity.TABLE + "("
            + ShopEntity.COLUMN_ID + " integer primary key autoincrement, "
            + ShopEntity.COLUMN_NAME + " varchar(10) not null,"
            + ShopEntity.COLUMN_DOMAIN_NAME + " varchar(10), "
            + ShopEntity.COLUMN_MAP_DRAWABLE + " varchar(10),"
            + ShopEntity.COLUMN_LOGO_DRAWABLE + " varchar(10),"
            + ShopEntity.COLUMN_FK_ADDRESS + " varchar(10) not null, foreign key (" + ShopEntity.COLUMN_FK_ADDRESS + ") references " + AddressEntity.TABLE + "(" + AddressEntity.COLUMN_ID + ")) ";



    private SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public static SQLiteDatabase getDatabaseInstance(Context ctx){
        if(databaseInstance == null){
            SQLiteHelper helper = new SQLiteHelper(ctx);
            databaseInstance = helper.getWritableDatabase();
        }
        return databaseInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("drop table if exists " + AddressEntity.TABLE);
        sqLiteDatabase.execSQL("drop table if exists " + ItemEntity.TABLE);
        sqLiteDatabase.execSQL("drop table if exists " + ShopEntity.TABLE);

        sqLiteDatabase.execSQL(CREATE_TABLE_ADDRESS);
        sqLiteDatabase.execSQL(CREATE_TABLE_ITEMS);
        sqLiteDatabase.execSQL(CREATE_TABLE_SHOPS);

        //region Insert init
        ContentValues values = new ContentValues();
        values.put(AddressEntity.COLUMN_NUMBER, "10");
        values.put(AddressEntity.COLUMN_STREETNAME, "Avenue de batta");
        values.put(AddressEntity.COLUMN_CITY, "Huy");
        values.put(AddressEntity.COLUMN_ZIPCODE, "4500");
        values.put(AddressEntity.COLUMN_COUNTRY, "Belgique");
        sqLiteDatabase.insert(AddressEntity.TABLE, null, values);

        values = new ContentValues();
        values.put(AddressEntity.COLUMN_NUMBER, "20");
        values.put(AddressEntity.COLUMN_STREETNAME, "Rue neuve");
        values.put(AddressEntity.COLUMN_CITY, "Huy");
        values.put(AddressEntity.COLUMN_ZIPCODE, "4520");
        values.put(AddressEntity.COLUMN_COUNTRY, "Belgique");
        sqLiteDatabase.insert(AddressEntity.TABLE, null, values);

        values = new ContentValues();
        values.put(AddressEntity.COLUMN_NUMBER, "1");
        values.put(AddressEntity.COLUMN_STREETNAME, "Avenue des Ardennes");
        values.put(AddressEntity.COLUMN_CITY, "Huy");
        values.put(AddressEntity.COLUMN_ZIPCODE, "4520");
        values.put(AddressEntity.COLUMN_COUNTRY, "Belgique");
        sqLiteDatabase.insert(AddressEntity.TABLE, null, values);

        values = new ContentValues();
        values.put(AddressEntity.COLUMN_NUMBER, "20");
        values.put(AddressEntity.COLUMN_STREETNAME, "Vieille route de Liège");
        values.put(AddressEntity.COLUMN_CITY, "Marche en famenne");
        values.put(AddressEntity.COLUMN_ZIPCODE, "6900");
        values.put(AddressEntity.COLUMN_COUNTRY, "Belgique");
        sqLiteDatabase.insert(AddressEntity.TABLE, null, values);

        values = new ContentValues();
        values.put(ShopEntity.COLUMN_NAME, "Match");
        values.put(ShopEntity.COLUMN_DOMAIN_NAME, "Carrefour.be");
        values.put(ShopEntity.COLUMN_LOGO_DRAWABLE, fromDrawable(context, R.drawable.carrefour));
        values.put(ShopEntity.COLUMN_FK_ADDRESS, 1);
        sqLiteDatabase.insert(ShopEntity.TABLE, null, values);

        values = new ContentValues();
        values.put(ShopEntity.COLUMN_NAME, "Ici Paris XL");
        values.put(ShopEntity.COLUMN_DOMAIN_NAME, "paris-xl.be");
        values.put(ShopEntity.COLUMN_LOGO_DRAWABLE, fromDrawable(context, R.drawable.parisxl));
        values.put(ShopEntity.COLUMN_FK_ADDRESS, 2);
        sqLiteDatabase.insert(ShopEntity.TABLE, null, values);

        values = new ContentValues();
        values.put(ShopEntity.COLUMN_NAME, "C&A");
        values.put(ShopEntity.COLUMN_DOMAIN_NAME, "ca.be");
        values.put(ShopEntity.COLUMN_LOGO_DRAWABLE, fromDrawable(context, R.drawable.ca));
        values.put(ShopEntity.COLUMN_FK_ADDRESS, 3);
        sqLiteDatabase.insert(ShopEntity.TABLE, null, values);

        values = new ContentValues();
        values.put(ShopEntity.COLUMN_NAME, "Aldi");
        values.put(ShopEntity.COLUMN_DOMAIN_NAME, "aldi.be");
        values.put(ShopEntity.COLUMN_LOGO_DRAWABLE, fromDrawable(context, R.drawable.aldi));
        values.put(ShopEntity.COLUMN_FK_ADDRESS, 4);
        sqLiteDatabase.insert(ShopEntity.TABLE, null, values);
        //endregion
    }

    public static final String fromDrawable(@NonNull Context context,
                                             @AnyRes int drawableId) {
        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + context.getResources().getResourcePackageName(drawableId)
                + '/' + context.getResources().getResourceTypeName(drawableId)
                + '/' + context.getResources().getResourceEntryName(drawableId) );
        return imageUri.toString();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
