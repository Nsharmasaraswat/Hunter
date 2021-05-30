package com.gtp.hunter.wms.repository;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverters;

import com.gtp.hunter.structure.converters.UUIDConverter;
import com.gtp.hunter.wms.model.CheckingItem;

import java.util.List;
import java.util.UUID;

@Dao
public interface CheckingItemRepository {

    @Query("SELECT * FROM tmpchecking WHERE document_id = :document_id ORDER BY id DESC")
    List<CheckingItem> list(@TypeConverters(value = UUIDConverter.class) UUID document_id);

    @Query("SELECT * FROM tmpchecking")
    List<CheckingItem> listAll();

    @Query("DELETE FROM tmpchecking WHERE document_id = :document_id")
    void clear(@TypeConverters(value = UUIDConverter.class) UUID document_id);

    @Query("DELETE FROM tmpchecking")
    void clearTemp();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(CheckingItem temp);

    @Delete
    void delete(CheckingItem temp);
}
