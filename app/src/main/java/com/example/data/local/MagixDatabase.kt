package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AuditLogDao
import com.example.data.local.dao.AutomationDao
import com.example.data.local.dao.MemoryDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.AutomationEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.ReminderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MemoryEntity::class,
        AutomationEntity::class,
        ReminderEntity::class,
        AuditLogEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class MagixDatabase : RoomDatabase() {

    abstract fun memoryDao(): MemoryDao
    abstract fun automationDao(): AutomationDao
    abstract fun reminderDao(): ReminderDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: MagixDatabase? = null

        fun getInstance(context: Context): MagixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MagixDatabase::class.java,
                    "magix_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            prepopulateInitialData(database)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulateInitialData(db: MagixDatabase) {
            // Seed initial sample memories
            db.memoryDao().insertMemory(
                MemoryEntity(
                    key = "Preferred voice",
                    value = "MAGIX Male (Puck)",
                    category = "Preferences",
                    timestamp = System.currentTimeMillis() - 72000000L
                )
            )
            db.memoryDao().insertMemory(
                MemoryEntity(
                    key = "Primary workspace",
                    value = "Google AI Studio",
                    category = "Work",
                    timestamp = System.currentTimeMillis() - 18000000L
                )
            )

            // Automations are kept completely empty and clean so the user can define and save their own custom subroutines.

            // Seed initial sample reminder
            db.reminderDao().insertReminder(
                ReminderEntity(
                    title = "Review MAGIX system architecture",
                    dueTimeMillis = System.currentTimeMillis() + 3600000L * 3,
                    formattedTime = "Today, 6:00 PM",
                    isCompleted = false
                )
            )

            // Seed initial audit log
            db.auditLogDao().insertLog(
                AuditLogEntity(
                    actionName = "SYSTEM_INITIALIZE",
                    target = "MAGIX Core",
                    status = "SUCCESS",
                    details = "Initialized clean database, memory store, and autonomous engine.",
                    evidence = "Core engines ready"
                )
            )
        }
    }
}
