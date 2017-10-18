package masi.henallux.be.listecourses.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import masi.henallux.be.listecourses.dao.sqlModels.AddressEntity;
import masi.henallux.be.listecourses.model.Address;

/**
 * Created by Arthur on 12-10-17.
 */

public class SQLAddressRepository implements IAddressRepository{

    private Context context;
    private static SQLAddressRepository instance;

    private String allColumnsAddress = AddressEntity.COLUMN_ID
            + "," + AddressEntity.COLUMN_NUMBER
            + "," + AddressEntity.COLUMN_STREETNAME
            + "," + AddressEntity.COLUMN_CITY
            + "," + AddressEntity.COLUMN_ZIPCODE
            + "," + AddressEntity.COLUMN_COUNTRY;


    private SQLAddressRepository(Context context) {
        this.context = context;
    }

    public static SQLAddressRepository getInstance(Context ctx){
        if(instance == null){
            instance = new SQLAddressRepository(ctx);
        }
        return instance;
    }

    @Override
    public Address getAddressById(long addressId) {

        SQLiteDatabase database = SQLiteHelper.getDatabaseInstance(context);
        Cursor cursorAddress = database.rawQuery("Select " + allColumnsAddress +" from " + AddressEntity.TABLE + " where " + AddressEntity.COLUMN_ID + " = ?",
                new String[]{String.valueOf(addressId)});

        Address address = new Address();
        cursorAddress.moveToFirst();
        address.setId(cursorAddress.getLong(0));
        address.setStreetNumber(cursorAddress.getString(1));
        address.setStreetName(cursorAddress.getString(2));
        address.setCity(cursorAddress.getString(3));
        address.setPostalCode(cursorAddress.getString(4));
        address.setCountry(cursorAddress.getString(5));
        cursorAddress.close();
        return address;
    }

    @Override
    public Address updateAddress(Address address) {
        ContentValues values = new ContentValues();
        values.put(AddressEntity.COLUMN_STREETNAME,address.getStreetName());
        values.put(AddressEntity.COLUMN_NUMBER,address.getStreetNumber());
        values.put(AddressEntity.COLUMN_CITY,address.getCity());
        values.put(AddressEntity.COLUMN_ZIPCODE,address.getPostalCode());
        values.put(AddressEntity.COLUMN_COUNTRY,address.getCountry());
        SQLiteHelper.getDatabaseInstance(context)
                .update(AddressEntity.TABLE,values,AddressEntity.COLUMN_ID + "=?",
                new String[]{String.valueOf(address.getId())});
        return address;

    }
}
