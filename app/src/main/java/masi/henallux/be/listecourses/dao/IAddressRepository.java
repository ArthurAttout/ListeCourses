package masi.henallux.be.listecourses.dao;

import masi.henallux.be.listecourses.model.Address;

/**
 * Created by Le Roi Arthur on 12-10-17.
 */

public interface IAddressRepository {
    Address getAddressById(long id);
    Address updateAddress(Address address);
}
