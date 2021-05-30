package com.gtp.hunter.wms.repository;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverters;

import com.gtp.hunter.structure.converters.UUIDConverter;
import com.gtp.hunter.wms.model.Product;

import java.util.List;
import java.util.UUID;

@Dao
public interface ProductRepository {
    @Query("SELECT * FROM product")
    List<Product> listAll();

    @Query("SELECT * FROM product WHERE parent_id = :parentId")
    List<Product> listByParent(@TypeConverters(value = UUIDConverter.class) UUID parentId);

    @Query("SELECT * FROM product WHERE id = :id")
    Product findById(@TypeConverters(value = UUIDConverter.class) UUID id);

    @Query("SELECT * FROM product WHERE sku = :sku")
    Product findBySku(String sku);

    @Query("SELECT * FROM product ORDER BY updatedAt DESC LIMIT 1")
    Product findLatest();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Product product);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Product> products);

    @Delete
    void delete(Product prd);

    @Query("DELETE FROM product")
    void clearTable();
}
