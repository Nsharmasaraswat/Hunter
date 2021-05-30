package com.gtp.hunter.wms.repository;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.AddressField;
import com.gtp.hunter.wms.model.AddressModel;
import com.gtp.hunter.wms.model.CheckingItem;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.ProductField;
import com.gtp.hunter.wms.model.ProductModel;
import com.gtp.hunter.wms.model.SugarPrintTemporary;

@Database(entities = {Address.class, AddressModel.class, AddressField.class, Product.class, ProductModel.class, ProductField.class, CheckingItem.class, SugarPrintTemporary.class},
        version = 15, exportSchema = false)
public abstract class MobileDatabase extends RoomDatabase {

    public abstract AddressRepository addrDao();

    public abstract AddressModelRepository amDao();

    public abstract AddressFieldRepository afDao();

    public abstract ProductRepository prdDao();

    public abstract ProductModelRepository pmDao();

    public abstract ProductFieldRepository pfDao();

    public abstract SugarPrintTemporaryRepository sugarDao();

    public abstract CheckingItemRepository checkItemDao();
}
