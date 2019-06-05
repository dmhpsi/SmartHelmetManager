package com.darkha.smarthelmetmanager;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface LogDao {
    @Query("SELECT * FROM applog")
    List<AppLog> getAll();

    @Query("SELECT * FROM applog WHERE type LIKE :type")
    List<AppLog> getByType(String type);

    @Query("DELETE FROM applog WHERE type LIKE :type")
    void deleteByType(String type);

    @Insert
    void addLog(AppLog appLogs);

    @Delete
    void deleteLog(AppLog appLog);
}
