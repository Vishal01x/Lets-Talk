package com.exa.android.letstalk.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ScheduledMessageEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ScheduleMessageDatabase: RoomDatabase() {

    abstract val dao: ScheduledMessageDao
}


val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add the new columns
        database.execSQL("ALTER TABLE scheduled_messages ADD COLUMN recipientName TEXT")
        database.execSQL("ALTER TABLE scheduled_messages ADD COLUMN profileImageUri TEXT")

        // Optionally, set default values (e.g., empty strings or null) for existing records
        // You can set `receiverName` and `profilePic` to empty string or null, depending on your logic
        database.execSQL("UPDATE scheduled_messages SET recipientName = '' WHERE recipientName IS NULL")
        database.execSQL("UPDATE scheduled_messages SET profileImageUri = '' WHERE profileImageUri IS NULL")
    }
}
