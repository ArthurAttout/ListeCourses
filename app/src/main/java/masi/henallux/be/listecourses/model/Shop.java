package masi.henallux.be.listecourses.model;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Arthur on 03-10-17.
 */

public class Shop implements Parcelable {

    private long id;
    private String name;
    private Uri shopLogoUriDrawable;
    private Uri shopMapUriDrawable;
    private Address address;
    private String domainName; //Used to retrieve the logo (if the logo should be retrieved through an API)
    private ArrayList<Item> chosenItems = new ArrayList<>();

    public Shop(long id,String name, Address address, String domainName, Uri shopFacadeUriDrawable, Uri shopLogoUriDrawable, ArrayList<Item> chosenItems) {
        this.id = id;
        this.domainName = domainName;
        this.name = name;
        this.shopLogoUriDrawable = shopLogoUriDrawable;
        this.shopMapUriDrawable = shopFacadeUriDrawable;
        this.chosenItems = chosenItems;
        this.address = address;
    }

    public Shop() {

    }

    //region Getters & setters


    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public ArrayList<Item> getChosenItems() {
        return chosenItems;
    }

    public void addNewItem(Item item){
        chosenItems.add(item);
    }

    public void addItemRange(ArrayList<Item> items){
        chosenItems.addAll(items);
    }

    public void clearChosenItems(){
        chosenItems.clear();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Uri getShopLogoUriDrawable() {
        return shopLogoUriDrawable;
    }

    public void setShopLogoUriDrawable(Uri shopLogoUriDrawable) {
        this.shopLogoUriDrawable = shopLogoUriDrawable;
    }

    public Uri getShopMapUriDrawable() {
        return shopMapUriDrawable;
    }

    public void setShopMapUriDrawable(Uri shopMapUriDrawable) {
        this.shopMapUriDrawable = shopMapUriDrawable;
    }

    //endregion

    //region Parcelable
    protected Shop(Parcel in) {
        id = in.readLong();
        name = in.readString();
        domainName = in.readString();
        shopLogoUriDrawable = (Uri) in.readValue(Uri.class.getClassLoader());
        shopMapUriDrawable = (Uri) in.readValue(Uri.class.getClassLoader());
        address = (Address) in.readValue(Address.class.getClassLoader());
        if (in.readByte() == 0x01) {
            chosenItems = new ArrayList<Item>();
            in.readList(chosenItems, Item.class.getClassLoader());
        } else {
            chosenItems = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(domainName);
        dest.writeValue(shopLogoUriDrawable);
        dest.writeValue(shopMapUriDrawable);
        dest.writeValue(address);
        if (chosenItems == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(chosenItems);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Shop> CREATOR = new Parcelable.Creator<Shop>() {
        @Override
        public Shop createFromParcel(Parcel in) {
            return new Shop(in);
        }

        @Override
        public Shop[] newArray(int size) {
            return new Shop[size];
        }
    };
    //endregion
}