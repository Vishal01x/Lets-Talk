package com.exa.android.letstalk.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.exa.android.letstalk.data.local.room.crypto.CryptoDao
import com.exa.android.letstalk.data.local.room.crypto.PreKeyEntity
import com.exa.android.letstalk.data.local.room.crypto.SessionRecordEntity
import com.exa.android.letstalk.data.local.room.crypto.SignedPreKeyEntity

@Database(
    entities = [
        ScheduledMessageEntity::class,
        SessionRecordEntity::class,
        PreKeyEntity::class,
        SignedPreKeyEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ScheduleMessageDatabase : RoomDatabase() {
    abstract fun scheduleMessageDao(): ScheduledMessageDao
    abstract fun cryptoDao(): CryptoDao
}



val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add the new columns
        database.execSQL("ALTER TABLE scheduled_messages ADD COLUMN recipientName TEXT")
        database.execSQL("ALTER TABLE scheduled_messages ADD COLUMN profileImageUri TEXT")

        // Set default values for existing records
        database.execSQL("UPDATE scheduled_messages SET recipientName = '' WHERE recipientName IS NULL")
        database.execSQL("UPDATE scheduled_messages SET profileImageUri = '' WHERE profileImageUri IS NULL")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create Signal Protocol crypto tables
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS signal_sessions (
                address TEXT PRIMARY KEY NOT NULL,
                record BLOB NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )
        
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS signal_prekeys (
                preKeyId INTEGER PRIMARY KEY NOT NULL,
                record BLOB NOT NULL
            )
            """.trimIndent()
        )
        
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS signal_signed_prekeys (
                signedPreKeyId INTEGER PRIMARY KEY NOT NULL,
                record BLOB NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

