package com.gtp.hunter.wms.repository;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverters;

import com.gtp.hunter.structure.converters.UUIDConverter;
import com.gtp.hunter.wms.model.ProductModel;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Dao
public interface ProductModelRepository {
    @Query("SELECT * FROM productmodel")
    List<ProductModel> listAll();

    @Query("SELECT * FROM productmodel WHERE id = :pmId")
    ProductModel findById(@TypeConverters(UUIDConverter.class) UUID pmId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Set<ProductModel> prdModelSet);
}
