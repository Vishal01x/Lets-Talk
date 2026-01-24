package com.exa.android.letstalk.data.local.room

import androidx.room.TypeConverter
import com.exa.android.letstalk.domain.Message
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    // Convert Message? to JSON
    @TypeConverter
    fun fromMessage(message: Message?): String? {
        return gson.toJson(message)
    }

    // Convert JSON back to Message?
    @TypeConverter
    fun toMessage(json: String?): Message? {
        return json?.let {
            gson.fromJson(it, object : TypeToken<Message>() {}.type)
        }
    }

    // Convert List<String?> to JSON
    @TypeConverter
    fun fromStringList(list: List<String?>?): String? {
        return gson.toJson(list)
    }

    // Convert JSON back to List<String?>
    @TypeConverter
    fun toStringList(json: String?): List<String?> {
        return json?.let {
            gson.fromJson(it, object : TypeToken<List<String?>>() {}.type)
        } ?: emptyList()
    }
}
