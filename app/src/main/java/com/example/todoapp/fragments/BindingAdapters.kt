package com.example.todoapp.fragments

import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.example.todoapp.R
import com.example.todoapp.data.models.Priority
import com.example.todoapp.data.models.ToDoData
import com.example.todoapp.fragments.list.ListFragmentDirections
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BindingAdapters {

    companion object{

        @BindingAdapter("android:navigateToAddFragment")
        @JvmStatic
        fun navigateToAddFragment(view: FloatingActionButton, navigate: Boolean){
            view.setOnClickListener {
                if(navigate){
                    view.findNavController().navigate(R.id.action_listFragment_to_addFragment)
                }
            }
        }
        @BindingAdapter("android:navigateToCountDownFragment")
        @JvmStatic
        fun navigateToCountDownFragment(view: FloatingActionButton, navigate: Boolean){
            view.setOnClickListener {
                if(navigate){
                    view.findNavController().navigate(R.id.action_listFragment_to_countDownFragment)
                }
            }
        }
        @BindingAdapter("android:emptyDatabase")
        @JvmStatic
        fun emptyDatabase(view: View, emptyDatabase: MutableLiveData<Boolean>){
            when(emptyDatabase.value){
                true -> view.visibility = View.VISIBLE
                false -> view.visibility = View.INVISIBLE
                else -> {}
            }
        }
        @BindingAdapter("android:parsePriorityToInt")
        @JvmStatic
        fun parsePriorityToInt(view: Spinner, priority: Priority){
            when(priority){
                Priority.HIGH -> { view.setSelection(0) }
                Priority.MEDIUM -> { view.setSelection(1) }
                Priority.LOW -> { view.setSelection(2) }
            }
        }

        @BindingAdapter("android:parseDate")
        @JvmStatic
        fun parseDate(view: EditText, dateTime: String){
            val date=dateTime.split(" ")
            view.setText(date[0])
        }

        @BindingAdapter("android:parseTime")
        @JvmStatic
        fun parseTime(view: EditText, dateTime: String){
            val date=dateTime.split(" ")
            view.setText(date[1])
        }

        @BindingAdapter("android:parseViewTime")
        @JvmStatic
        fun parseViewTime(view: TextView, dateTime: String){
            val date=dateTime.split(" ")
            view.text=date[1]
        }

        @BindingAdapter("android:parsePriorityColor")
        @JvmStatic
        fun parsePriorityColor(cardView: CardView, priority: Priority){
            when(priority){
                Priority.HIGH -> { cardView.setCardBackgroundColor(cardView.context.getColor(R.color.red)) }
                Priority.MEDIUM -> { cardView.setCardBackgroundColor(cardView.context.getColor(R.color.yellow)) }
                Priority.LOW -> { cardView.setCardBackgroundColor(cardView.context.getColor(R.color.green)) }
            }
        }


        @BindingAdapter("android:sendDataToUpdateFragment")
        @JvmStatic
        fun sendDataToUpdateFragment(view: ConstraintLayout, currentItem: ToDoData){
            view.setOnClickListener {
                val action = ListFragmentDirections.actionListFragmentToUpdateFragment(currentItem)
                view.findNavController().navigate(action)
            }
        }

        @BindingAdapter("android:parseFrequencyColor")
        @JvmStatic
        fun parseFrequencyColor(cardView: CardView, frequency: Int){
            if(frequency == -1) {
                cardView.setCardBackgroundColor(cardView.context.getColor(R.color.white))
            } else if(frequency == 0) {
                cardView.setCardBackgroundColor(cardView.context.getColor(R.color.f0))
            } else if(frequency <= 1) {
                cardView.setCardBackgroundColor(cardView.context.getColor(R.color.f1))
            } else if(frequency <= 3) {
                cardView.setCardBackgroundColor(cardView.context.getColor(R.color.f2))
            } else if(frequency <= 5) {
                cardView.setCardBackgroundColor(cardView.context.getColor(R.color.f3))
            } else {
                cardView.setCardBackgroundColor(cardView.context.getColor(R.color.f4))
            }
        }


    }

}