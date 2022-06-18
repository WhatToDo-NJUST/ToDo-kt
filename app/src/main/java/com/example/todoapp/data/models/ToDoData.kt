package com.example.todoapp.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.sql.Time
import java.time.LocalDateTime

@Entity(tableName = "todo_table")
@Parcelize
data class ToDoData(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var userId:Int,
    var title: String,
    var priority: Priority,
    var description: String,
    var isDone:Boolean,
    var registerTime:String

): Parcelable

// Parcelable序列化工具
