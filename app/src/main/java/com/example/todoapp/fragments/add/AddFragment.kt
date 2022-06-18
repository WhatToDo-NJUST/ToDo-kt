package com.example.todoapp.fragments.add

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

        setTimePicker()

        return binding.root
    }

    //    自定义函数
    private fun setTimePicker() {
//      时间栏获取焦点时不自动弹出键盘
        binding.timeEt.showSoftInputOnFocus = false
//        当时间栏获取焦点时弹出时间选择器
        binding.timeEt.setOnFocusChangeListener { v, hasFocus ->
            binding.timePicker.isVisible = hasFocus
        }
        binding.timePicker.setIs24HourView(true)

        // 绑定时间选择器
        binding.timePicker.setOnTimeChangedListener { view, hourOfDay, minute ->
            var hour=""
            var min=""
            if(hourOfDay<10) hour="0"+hourOfDay.toString()
            if(minute<10) min="0"+minute.toString()
            val sec=(0..60).random()
            binding.timeEt.setText("$hourOfDay:$minute:$sec")
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
        val date=DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now()).toString()
        val mTime = date+" "+ binding.timeEt.text.toString()
        Log.d("DATA",mTime.toString())
        val userId=
            context?.getSharedPreferences("data", Context.MODE_PRIVATE)?.getString("userId","")!!
        val isDone=false

        val validation = mSharedViewModel.verifyDataFromUser(mTitle, mDescription)

        if (validation) {
            // Insert Data to Database
            val newData = ToDoData(
                (0..100000000).random().toString(),
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