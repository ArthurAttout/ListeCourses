package masi.henallux.be.listecourses.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collection;

import masi.henallux.be.listecourses.dao.sqlModels.AddressEntity;
import masi.henallux.be.listecourses.dao.sqlModels.ItemEntity;
import masi.henallux.be.listecourses.dao.sqlModels.ShopEntity;
import masi.henallux.be.listecourses.model.Address;
import masi.henallux.be.listecourses.model.Item;
import masi.henallux.be.listecourses.model.Shop;
import masi.henallux.be.listecourses.utils.MapService;

/**
 * Created by Le Roi Arthur on 09-10-17.
 */

public class SQLShopRepository implements IShopRepository {

    private SQLiteDatabase database;
    private Context context;
    private MapService mapService;

    //region helpers
    private String[] allColumnsShop = { ShopEntity.COLUMN_ID,
            ShopEntity.COLUMN_NAME,
            ShopEntity.COLUMN_DOMAIN_NAME,
            ShopEntity.COLUMN_MAP_DRAWABLE,
            ShopEntity.COLUMN_LOGO_DRAWABLE,
            ShopEntity.COLUMN_FK_ADDRESS };

    private String allColumnsShopNoId =  ShopEntity.COLUMN_NAME
            + "," + ShopEntity.COLUMN_DOMAIN_NAME
            + "," + ShopEntity.COLUMN_FK_ADDRESS;
    private String allColumnsAddressNoId = AddressEntity.COLUMN_NUMBER
            + "," + AddressEntity.COLUMN_STREETNAME
            + "," + AddressEntity.COLUMN_ZIPCODE
            + "," + AddressEntity.COLUMN_CITY
            + "," + AddressEntity.COLUMN_COUNTRY;

    private String allColumnsItem = ItemEntity.COLUMN_ID
            + "," + ItemEntity.COLUMN_NAME
            + "," + ItemEntity.COLUMN_QUANTITY
            + "," + ItemEntity.COLUMN_UNIT_TYPE;

    //endregion

    private static SQLShopRepository instance;

    public static SQLShopRepository getInstance(Context context){
        if(instance == null){
            instance = new SQLShopRepository(context);
        }
        return instance;
    }

    private SQLShopRepository(Context context) {
        this.context = context;
        database = SQLiteHelper.getDatabaseInstance(context);
        mapService = new MapService(context);
    }

    @Override
    public Collection<Shop> getAllShops() {
        ArrayList<Shop> shops = new ArrayList<>();
        Cursor cursor = database.query(ShopEntity.TABLE, allColumnsShop, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            shops.add(convertCursorToShop(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return shops;
    }

    private Shop convertCursorToShop(Cursor cursor) {
        Shop shop = new Shop();

        shop.setId(cursor.getLong(0));
        shop.setName(cursor.getString(1));

        shop.setDomainName(cursor.getString(2));
        shop.setShopMapUriDrawable(!TextUtils.isEmpty(cursor.getString(3)) ? Uri.parse(cursor.getString(3)) : null);
        shop.setShopLogoUriDrawable(!TextUtils.isEmpty(cursor.getString(4)) ? Uri.parse(cursor.getString(4)) : null);
        long addressId = cursor.getLong(5);
        shop.setAddress(SQLAddressRepository.getInstance(context).getAddressById(addressId));

        long storeId = shop.getId();

        Cursor cursorItems = database.rawQuery("Select " + allColumnsItem +" from " + ItemEntity.TABLE + " where " + ItemEntity.COLUMN_FK_STORE + " = ?",
                new String[]{String.valueOf(storeId)});

        cursorItems.moveToFirst();
        while (!cursorItems.isAfterLast()) {
            Item item = new Item();
            item.setId(cursorItems.getLong(0));
            item.setName(cursorItems.getString(1));
            item.setQuantity(cursorItems.getInt(2));
            item.setUnit(Item.UnitType.valueOf(cursorItems.getString(3)));
            shop.addNewItem(item);
            cursorItems.moveToNext();
        }
        cursorItems.close();

        return shop;
    }



    @Override
    public Collection<Shop> tryGetAllShopsWithPictures() {
        int failureCount = 0;
        ArrayList<Shop> defaultShops = new ArrayList<>(getAllShops());

        for (Shop shop : defaultShops) {
            //If the picture has already been retrieved in the past, go to next
            if (shop.getShopMapUriDrawable() != null) continue;

            Uri uri = mapService.getMapOfShop(shop);
            if (uri != null) {
                shop.setShopMapUriDrawable(uri);
                updateShop(shop); //Update to save the URI in database
            } else {
                failureCount++;
                if (failureCount > maximumToleranceFailure) { //Failure tolerance overlapped. Give up, and return normal shop list
                    return defaultShops;
                }
            }
        }
        return defaultShops; //The list of shops with drawable attribute that has been set
    }

    @Override
    public Shop insertShop(Shop s) {
        ContentValues values = new ContentValues();
        values.put(AddressEntity.COLUMN_NUMBER,s.getAddress().getStreetNumber());
        values.put(AddressEntity.COLUMN_STREETNAME,s.getAddress().getStreetName());
        values.put(AddressEntity.COLUMN_CITY,s.getAddress().getCity());
        values.put(AddressEntity.COLUMN_ZIPCODE,s.getAddress().getPostalCode());
        values.put(AddressEntity.COLUMN_COUNTRY,s.getAddress().getCountry());
        long addressId = database.insert(AddressEntity.TABLE,null,values);
        s.getAddress().setId(addressId);

        values = new ContentValues();
        values.put(ShopEntity.COLUMN_NAME,s.getName());
        if(s.getShopLogoUriDrawable() != null){
            values.put(ShopEntity.COLUMN_LOGO_DRAWABLE,s.getShopLogoUriDrawable().toString());
        }
        values.put(ShopEntity.COLUMN_FK_ADDRESS,addressId);
        long shopId = database.insert(ShopEntity.TABLE,null,values);
        s.setId(shopId);
        return s;
    }

    @Override
    public void deleteAllItemsOfShop(Shop s) {
        database.delete(ItemEntity.TABLE,ItemEntity.COLUMN_FK_STORE + "=?",
                new String[]{String.valueOf(s.getId())});

        s.getChosenItems().clear(); //Clear items list in GUI
    }

    private int maximumToleranceFailure = 3;
    @Override
    public void setMaximumToleranceFailure(int max) {
        maximumToleranceFailure = max;
    }

    @Override
    public Shop getShopById(int id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateShop(Shop shop) {

        updateShopAttributes(shop);
        SQLAddressRepository.getInstance(context).updateAddress(shop.getAddress());
        ArrayList<Item> persistedItemsOfShop = getPersistedItems(shop);

        ArrayList<Item> itemsToAdd = new ArrayList<>();
        ArrayList<Item> itemsToDelete = new ArrayList<>();
        ArrayList<Item> itemsToUpdate = new ArrayList<>();

        //Search for items to delete
        for(Item persisted : persistedItemsOfShop){
            if(!shop.getChosenItems().contains(persisted))
                itemsToDelete.add(persisted);
        }

        //Search for items to add
        for(Item persisted : shop.getChosenItems()){
            if(!persistedItemsOfShop.contains(persisted))
                itemsToAdd.add(persisted);
        }

        //Modify each updated item
        for(Item persisted : persistedItemsOfShop){
            for(Item newItem : shop.getChosenItems()){
                if(newItem.equals(persisted)) continue; //No changes
                if(newItem.getId() == persisted.getId()){
                    persisted.setQuantity(newItem.getQuantity());
                    persisted.setName(newItem.getName());
                    persisted.setUnit(newItem.getUnit());
                    itemsToUpdate.add(persisted);
                }
            }
        }

        ContentValues values;

        for(Item i : itemsToAdd){
            values = new ContentValues();
            values.put(ItemEntity.COLUMN_NAME,i.getName());
            values.put(ItemEntity.COLUMN_QUANTITY,i.getQuantity());
            values.put(ItemEntity.COLUMN_UNIT_TYPE,i.getUnit().toString());
            values.put(ItemEntity.COLUMN_FK_STORE,shop.getId());
            database.insert(ItemEntity.TABLE,null,values);
        }

        for(Item i : itemsToDelete){
            database.delete(ItemEntity.TABLE,ItemEntity.COLUMN_ID + "=?",
                    new String[]{String.valueOf(i.getId())});
        }

        for(Item i : itemsToUpdate){
            values = new ContentValues();
            values.put(ItemEntity.COLUMN_QUANTITY,i.getQuantity());
            values.put(ItemEntity.COLUMN_NAME,i.getName());
            database.update(ItemEntity.TABLE,values,ItemEntity.COLUMN_ID + "=?",
                    new String[]{String.valueOf(i.getId())});
        }
    }

    @NonNull
    private ArrayList<Item> getPersistedItems(Shop shop) {
        Cursor cursorItems = database.rawQuery("Select " + allColumnsItem +" from " + ItemEntity.TABLE + " where " + ItemEntity.COLUMN_FK_STORE + " = ?",
                new String[]{String.valueOf(shop.getId())});
        ArrayList<Item> persistedItemsOfShop = new ArrayList<>();
        cursorItems.moveToFirst();
        while (!cursorItems.isAfterLast()) {
            Item item = new Item();
            item.setId(cursorItems.getLong(0));
            item.setName(cursorItems.getString(1));
            item.setQuantity(cursorItems.getInt(2));
            persistedItemsOfShop.add(item);
            cursorItems.moveToNext();
        }
        cursorItems.close();
        return persistedItemsOfShop;
    }



    private void updateShopAttributes(Shop shop) {
        ContentValues values = new ContentValues();
        values.put(ShopEntity.COLUMN_NAME,shop.getName());
        values.put(ShopEntity.COLUMN_DOMAIN_NAME,shop.getDomainName());

        if(shop.getShopLogoUriDrawable() != null){
            values.put(ShopEntity.COLUMN_LOGO_DRAWABLE,shop.getShopLogoUriDrawable().toString());
        }
        else
        {
            values.put(ShopEntity.COLUMN_LOGO_DRAWABLE,"");
        }

        if(shop.getShopMapUriDrawable() != null){
            values.put(ShopEntity.COLUMN_MAP_DRAWABLE,shop.getShopMapUriDrawable().toString());
        }
        else
        {
            values.put(ShopEntity.COLUMN_MAP_DRAWABLE,"");
        }

        values.put(ShopEntity.COLUMN_FK_ADDRESS,shop.getAddress().getId());
        long a = database.update(ShopEntity.TABLE,values,ShopEntity.COLUMN_ID + "=?",
                new String[]{String.valueOf(shop.getId())});
    }

    @Override
    public void close() {

    }
}
