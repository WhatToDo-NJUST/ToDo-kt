package com.example.todoapp.data

import androidx.room.TypeConverter
import com.example.todoapp.data.models.Priority

class Converter {

    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(priority: String): Priority {
        return Priority.valueOf(priority)
    }

//    您可以通过提供类型转换器来支持自定义类型，类型转换器是 Room 的方法，
//    用于告知 Room 如何将自定义类型与 Room 可以保留的已知类型相互转换。
//    您可以使用 @TypeConverter 注解来识别类型转换器。

}