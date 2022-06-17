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
    @PrimaryKey
    var id: String,
    var userId:String,
    var title: String,
    var priority: Priority,
    var description: String,
    var isDone:Boolean,
    var registerTime:String

): Parcelable

// Parcelable序列化工具
