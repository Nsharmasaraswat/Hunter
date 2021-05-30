package com.gtp.hunter.wms.repository;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverters;

import com.gtp.hunter.structure.converters.UUIDConverter;
import com.gtp.hunter.wms.model.Address;

import java.util.List;
import java.util.UUID;

@Dao
public interface AddressRepository {
    @Query("SELECT * FROM address")
    List<Address> listAll();

    @Query("SELECT * FROM address WHERE parent_id = :parentId")
    List<Address> listByParent(@TypeConverters(value = UUIDConverter.class) UUID parentId);

    @Query("SELECT * FROM address WHERE id = :id")
    Address findById(@TypeConverters(value = UUIDConverter.class) UUID id);

    @Query("SELECT * FROM address ORDER BY updatedAt DESC LIMIT 1")
    Address findLatest();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Address address);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Address> addresses);

    @Delete
    void delete(Address addr);
}
