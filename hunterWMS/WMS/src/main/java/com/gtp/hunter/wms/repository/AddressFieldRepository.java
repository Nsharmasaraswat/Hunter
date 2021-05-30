package com.gtp.hunter.wms.repository;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverters;

import com.gtp.hunter.structure.converters.UUIDConverter;
import com.gtp.hunter.wms.model.AddressField;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Dao
public interface AddressFieldRepository {
    @Query("SELECT * FROM addressfield")
    List<AddressField> listAll();

    @Query("SELECT * FROM addressfield WHERE address_id = :addrId")
    List<AddressField> listByAddressId(@TypeConverters(UUIDConverter.class) UUID addrId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AddressField> fields);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Set<AddressField> fields);
}
