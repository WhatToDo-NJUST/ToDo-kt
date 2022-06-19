package com.example.todoapp.fragments.add

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.todoapp.R
import com.example.todoapp.data.models.ToDoData
import com.example.todoapp.data.viewmodel.ToDoViewModel
import com.example.todoapp.databinding.FragmentAddBinding
import com.example.todoapp.fragments.SharedViewModel
import java.util.*
import com.example.todoapp.MainActivity
import com.example.todoapp.utils.AlarmReceiver

class AddFragment : Fragment() {

    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    val calendar: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddBinding.inflate(layoutInflater, container, false)

        // Set Menu
        setHasOptionsMenu(true)

        // Spinner Item Selected Listener
        binding.prioritiesSpinner.onItemSelectedListener = mSharedViewModel.listener

        setDataTimePicker()

        return binding.root
    }

    //    自定义函数
    @SuppressLint("SetTextI18n")
    private fun setDataTimePicker() {
        binding.dateEt.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            context?.let { it1 ->
                DatePickerDialog(it1, { _, year, month, day ->
                    run {
                        val format = "${setDateFormat(year, month, day)}"
                        binding.dateEt.setText(format)
                    }
                }, year, month, day).show()
            }
        }

        binding.timeEt.setOnClickListener {
            val ca = Calendar.getInstance()
            var mHour = ca[Calendar.HOUR_OF_DAY]
            var mMinute = ca[Calendar.MINUTE]

            val timePickerDialog = TimePickerDialog(
                context,
                TimePickerDialog.OnTimeSetListener{_, hourOfDay, minute ->
                    mHour   = hourOfDay
                    mMinute = minute
                    val mTime = "${setTimeFormat(hourOfDay,minute)}"
                    binding.timeEt.setText(mTime)
                },
                mHour, mMinute, true
            )
            timePickerDialog.show()
        }

    }


private fun setTimeFormat(hour: Int,min:Int):String{
    var hour1=hour.toString()
    var min1=min.toString()
    if(hour<10) hour1="0"+hour1
    if(min<10) min1="0"+min1
    val sec=(10..59).random()
    return "$hour1:$min1:$sec"
}

private fun setDateFormat(year: Int, month: Int, day: Int): String {
    var month1=(month+1).toString()
    var day1=day.toString()
    if(month+1<10) month1="0"+month1
    if(day<10) day1="0"+day1
    return "$year-$month1-$day1"
}

override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_fragment_menu, menu)
    }
    private fun startAlarm(calendar: Calendar) {
        val mContext = activity?.applicationContext
        val alarmManager = mContext?.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(activity, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(activity, 0, intent, 0)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_add) {
            insertDataToDb()
            startAlarm(calendar = calendar)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun insertDataToDb() {
        val mTitle = binding.titleEt.text.toString()
        val mPriority = binding.prioritiesSpinner.selectedItem.toString()
        val mDescription = binding.descriptionEt.text.toString()
        val mTime = binding.dateEt.text.toString()+" "+binding.timeEt.text.toString()
        val userId=
            context?.getSharedPreferences("data", Context.MODE_PRIVATE)?.getInt("userId",0)!!
        val isDone=false

        val validation = mSharedViewModel.verifyDataFromUser(mTitle, mDescription)

        if (validation) {
            // Insert Data to Database
            val newData = ToDoData(
                0,
                userId,
                mTitle,
                mSharedViewModel.parsePriority(mPriority),
//                mPriority,
                mDescription,
                isDone,
                mTime,
            )
            mToDoViewModel.insertData(newData)



            Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_SHORT).show()
            // Navigate Back
            findNavController().navigate(R.id.action_addFragment_to_listFragment)
        } else {
            Toast.makeText(requireContext(), "Please fill out all fields.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}