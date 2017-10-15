package masi.henallux.be.listecourses.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Arthur on 03-10-17.
 */

public class Address implements Parcelable {
    private long id;
    private String streetName;
    private String streetNumber;
    private String postalCode;
    private String country;
    private String city;

    public Address(long id, String streetNumber, String streetName, String postalCode, String city, String country) {
        this.id = id;
        this.streetName = streetName;
        this.streetNumber = streetNumber;
        this.postalCode = postalCode;
        this.country = country;
        this.city = city;
    }

    public Address() {

    }

    @Override
    public String toString() {
        return streetNumber + " " + streetName + " " + postalCode + " " + city + " " + country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        if (id != address.id) return false;
        if (streetName != null ? !streetName.equals(address.streetName) : address.streetName != null)
            return false;
        if (streetNumber != null ? !streetNumber.equals(address.streetNumber) : address.streetNumber != null)
            return false;
        if (postalCode != null ? !postalCode.equals(address.postalCode) : address.postalCode != null)
            return false;
        if (country != null ? !country.equals(address.country) : address.country != null)
            return false;
        return city != null ? city.equals(address.city) : address.city == null;
    }

    //region Getter & setter

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    //endregion

    //region Parcelable

    protected Address(Parcel in) {
        id = in.readLong();
        streetName = in.readString();
        streetNumber = in.readString();
        postalCode = in.readString();
        country = in.readString();
        city = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(streetName);
        dest.writeString(streetNumber);
        dest.writeString(postalCode);
        dest.writeString(country);
        dest.writeString(city);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Address> CREATOR = new Parcelable.Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };

    //endregion
}