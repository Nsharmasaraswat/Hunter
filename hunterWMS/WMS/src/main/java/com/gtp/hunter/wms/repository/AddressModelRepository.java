package com.gtp.hunter.wms.repository;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverters;

import com.gtp.hunter.structure.converters.UUIDConverter;
import com.gtp.hunter.wms.model.AddressModel;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Dao
public interface AddressModelRepository {
    @Query("SELECT * FROM addressmodel")
    List<AddressModel> listAll();

    @Query("SELECT * FROM addressmodel WHERE id = :amId")
    AddressModel findById(@TypeConverters(UUIDConverter.class) UUID amId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Set<AddressModel> addrModelSet);
}
