package com.exa.android.letstalk.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ScheduledMessageEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class ScheduleMessageDatabase: RoomDatabase() {

    abstract val dao: ScheduledMessageDao
}