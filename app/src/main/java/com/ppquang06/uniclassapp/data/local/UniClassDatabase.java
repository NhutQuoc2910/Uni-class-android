package com.ppquang06.uniclassapp.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.ppquang06.uniclassapp.data.local.dao.AssignmentDao;
import com.ppquang06.uniclassapp.data.local.dao.ClassDao;
import com.ppquang06.uniclassapp.data.local.entity.AssignmentEntity;
import com.ppquang06.uniclassapp.data.local.entity.ClassEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {ClassEntity.class, AssignmentEntity.class}, version = 1, exportSchema = false)
public abstract class UniClassDatabase extends RoomDatabase {

    private static volatile UniClassDatabase INSTANCE;

    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public abstract ClassDao classDao();

    public abstract AssignmentDao assignmentDao();

    public static UniClassDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (UniClassDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    UniClassDatabase.class,
                                    "uniclass_offline.db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
