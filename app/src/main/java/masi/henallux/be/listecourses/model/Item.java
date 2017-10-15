package masi.henallux.be.listecourses.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Arthur on 03-10-17.
 */
public class Item implements Parcelable {
    private long id;
    private String name;
    private int quantity;
    private UnitType unit;

    public Item(long id, String name, int quantity, UnitType unit) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public Item() {

    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Item){
            Item item = (Item)obj;
            return(item.getId() == id &&
                    item.getName().equals(name) &&
                    item.getQuantity() == quantity &&
                    item.getUnit() == unit);
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UnitType getUnit() {
        return unit;
    }

    public void setUnit(UnitType unit) {
        this.unit = unit;
    }

    public enum UnitType{
        none,
        kilogram,
        gram,
        liter
    }


    //region Parcelable

    //endregion

    protected Item(Parcel in) {
        id = in.readLong();
        name = in.readString();
        quantity = in.readInt();
        unit = (UnitType) in.readValue(UnitType.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeInt(quantity);
        dest.writeValue(unit);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
}