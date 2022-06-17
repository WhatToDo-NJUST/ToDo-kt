package com.example.todoapp.data.models

import java.time.LocalDateTime

class Test (
    var id: String,
    var userId:String,
    var title: String,
    var priority: String,
    var description: String,
    var isDone:Boolean,
    var registerTime:String


) {
    override fun toString(): String {
        return "Test(id='$id', userId='$userId', title='$title', priority='$priority', description='$description', isDone=$isDone, registerTime='$registerTime')"
    }
}