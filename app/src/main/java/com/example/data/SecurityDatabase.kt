package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val score: Int,
    val totalChecks: Int,
    val issuesFound: Int,
    val statusSummary: String
)

@Entity(tableName = "ignored_warnings")
data class IgnoredWarningEntity(
    @PrimaryKey val checkKey: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "security_settings")
data class SecuritySettingsEntity(
    @PrimaryKey val id: Int = 1,
    val autoScanEnabled: Boolean = true,
    val notifyOnWarning: Boolean = true,
    val soundAlerts: Boolean = true,
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val scanFrequencyDays: Int = 1
)

@Dao
interface SecurityDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC LIMIT 20")
    fun getScanHistory(): Flow<List<ScanHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanHistory(history: ScanHistoryEntity)

    @Query("SELECT * FROM ignored_warnings")
    fun getIgnoredWarnings(): Flow<List<IgnoredWarningEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addIgnoredWarning(warning: IgnoredWarningEntity)

    @Query("DELETE FROM ignored_warnings WHERE checkKey = :key")
    suspend fun removeIgnoredWarning(key: String)

    @Query("SELECT * FROM security_settings WHERE id = 1")
    fun getSettings(): Flow<SecuritySettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SecuritySettingsEntity)
}

@Database(
    entities = [ScanHistoryEntity::class, IgnoredWarningEntity::class, SecuritySettingsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SecurityDatabase : RoomDatabase() {
    abstract fun securityDao(): SecurityDao

    companion object {
        @Volatile
        private var INSTANCE: SecurityDatabase? = null

        fun getDatabase(context: Context): SecurityDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SecurityDatabase::class.java,
                    "security_service_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
