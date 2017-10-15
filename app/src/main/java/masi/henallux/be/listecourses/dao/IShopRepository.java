package masi.henallux.be.listecourses.dao;

import java.util.Collection;

import masi.henallux.be.listecourses.model.Shop;

/**
 * Created by Arthur on 03-10-17.
 */

public interface IShopRepository {
    Collection<Shop> getAllShops();
    Collection<Shop> tryGetAllShopsWithPictures();
    Shop insertShop(Shop s);
    void deleteAllItemsOfShop(Shop s);
    void setMaximumToleranceFailure(int max);
    Shop getShopById(int id);
    void updateShop(Shop shop);
    void close();
}
