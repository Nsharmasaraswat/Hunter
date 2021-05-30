package com.gtp.hunter.wms.repository;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.gtp.hunter.wms.model.SugarPrintTemporary;

import java.util.List;

@Dao
public interface SugarPrintTemporaryRepository {

    @Query("SELECT * FROM printedsugar")
    List<SugarPrintTemporary> listAll();

    @Query("DELETE FROM printedsugar")
    void clearSugar();

    @Insert
    void save(SugarPrintTemporary print);
}
