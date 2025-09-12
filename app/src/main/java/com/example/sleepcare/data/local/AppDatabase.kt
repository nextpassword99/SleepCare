package com.example.sleepcare.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sleepcare.data.local.dao.AnalysisReportDao
import com.example.sleepcare.data.local.dao.SensorDataDao
import com.example.sleepcare.data.local.dao.SleepSessionDao
import com.example.sleepcare.data.local.entity.AnalysisReportEntity
import com.example.sleepcare.data.local.entity.SensorDataEntity
import com.example.sleepcare.data.local.entity.SleepSessionEntity
import com.example.sleepcare.util.Converters

@Database(
    entities = [
        SleepSessionEntity::class,
        SensorDataEntity::class,
        AnalysisReportEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sleepSessionDao(): SleepSessionDao
    abstract fun sensorDataDao(): SensorDataDao
    abstract fun analysisReportDao(): AnalysisReportDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sleepcare_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
