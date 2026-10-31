package com.ppquang06.uniclassapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ppquang06.uniclassapp.data.local.entity.AssignmentEntity;

import java.util.List;

@Dao
public interface AssignmentDao {

    @Query("SELECT * FROM assignments ORDER BY dueDate ASC")
    List<AssignmentEntity> getAllAssignments();

    @Query("SELECT * FROM assignments WHERE classId = :classId ORDER BY dueDate ASC")
    List<AssignmentEntity> getAssignmentsByClass(String classId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAssignments(List<AssignmentEntity> assignments);

    @Query("DELETE FROM assignments WHERE classId = :classId")
    void clearByClass(String classId);

    @Query("DELETE FROM assignments")
    void clearAll();
}
