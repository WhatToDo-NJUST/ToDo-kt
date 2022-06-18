package com.example.todoapp.fragments.add

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.todoapp.R
import com.example.todoapp.data.models.ToDoData
import com.example.todoapp.data.viewmodel.ToDoViewModel
import com.example.todoapp.databinding.FragmentAddBinding
import com.example.todoapp.fragments.SharedViewModel
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AddFragment : Fragment() {

    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

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
//      时间栏获取焦点时不自动弹出键盘
        binding.timeEt.showSoftInputOnFocus = false
//        当时间栏获取焦点时弹出时间选择器
        binding.timeEt.setOnFocusChangeListener { v, hasFocus ->
            binding.dataPicker.isVisible = hasFocus
        }
        binding.timePicker.setIs24HourView(true)

        var data=""
        var time=""
        
        binding.dataPicker.setOnDateChangedListener { view, year, monthOfYear, dayOfMonth ->
            data=""
            var month=monthOfYear.toString()
            var day=dayOfMonth.toString()
            if(monthOfYear<10) month="0"+monthOfYear.toString()
            if(dayOfMonth<10) day="0"+dayOfMonth.toString()
            data="$year-$month-$day"

            binding.dataPicker.isVisible=false
            binding.timePicker.isVisible=true


        }

        // 绑定时间选择器
        binding.timePicker.setOnTimeChangedListener { view, hourOfDay, minute ->
            time=""
            var hour=hourOfDay.toString()
            var min= minute.toString()
            if(hourOfDay<10) hour="0"+hourOfDay.toString()
            if(minute<10) min="0"+minute.toString()
            val sec=(0..59).random()
            time="$hour:$min:$sec"

            binding.timeEt.setText("$data $time")
            binding.timePicker.isVisible=false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_add) {
            insertDataToDb()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun insertDataToDb() {
        val mTitle = binding.titleEt.text.toString()
        val mPriority = binding.prioritiesSpinner.selectedItem.toString()
        val mDescription = binding.descriptionEt.text.toString()
        val mTime = binding.timeEt.text.toString()
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