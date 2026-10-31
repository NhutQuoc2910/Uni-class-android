package com.ppquang06.uniclassapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ppquang06.uniclassapp.data.local.entity.ClassEntity;

import java.util.List;

@Dao
public interface ClassDao {

    @Query("SELECT * FROM classes ORDER BY updatedAt DESC")
    List<ClassEntity> getAllClasses();

    @Query("SELECT * FROM classes WHERE id = :classId LIMIT 1")
    ClassEntity getClassById(String classId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertClasses(List<ClassEntity> classes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertClass(ClassEntity classEntity);

    @Query("DELETE FROM classes")
    void clearClasses();
}
