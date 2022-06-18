package com.example.todoapp.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentProvider
import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import okhttp3.*
import okio.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun hideKeyboard(activity: Activity) {
    val inputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    val currentFocusedView = activity.currentFocus
    currentFocusedView?.let {
        inputMethodManager.hideSoftInputFromWindow(
            currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>){
    observe(lifecycleOwner, object : Observer<T>{
        override fun onChanged(t: T) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}



fun makeRequest(request: Request):String{
    val latch:CountDownLatch= CountDownLatch(1)
    var flag=false
    var res="res"
    val client = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .writeTimeout(2, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
        .build()

     client.newCall(request)
        .enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("DATA","error")
                res="error"
                flag=true
                latch.countDown()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("DATA","success")
                res= response.body.string()
                flag=true
                latch.countDown()
            }
        })
//    while(!flag){}
    latch.await()
    return res
}

