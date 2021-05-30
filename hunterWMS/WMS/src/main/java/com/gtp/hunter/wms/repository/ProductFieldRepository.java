package com.gtp.hunter.wms.repository;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverters;

import com.gtp.hunter.structure.converters.UUIDConverter;
import com.gtp.hunter.wms.model.ProductField;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Dao
public interface ProductFieldRepository {
    @Query("SELECT * FROM productfield")
    List<ProductField> listAll();

    @Query("SELECT * FROM productfield WHERE product_id = :prdId")
    List<ProductField> listByProductId(@TypeConverters(UUIDConverter.class) UUID prdId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProductField> products);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Set<ProductField> products);
}
