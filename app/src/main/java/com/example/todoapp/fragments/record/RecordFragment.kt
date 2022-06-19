package com.example.todoapp.fragments.record

import android.os.Bundle
import android.util.Log
import android.view.*

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.todoapp.R
import com.example.todoapp.data.models.Record
import com.example.todoapp.data.models.ToDoData
import com.example.todoapp.data.viewmodel.ToDoViewModel
import com.example.todoapp.databinding.FragmentRecordBinding
import com.example.todoapp.fragments.SharedViewModel
import com.example.todoapp.fragments.record.adapter.GridAdapter
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.concurrent.CountDownLatch

class RecordFragment : Fragment() {

    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()

    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private val gridadapter: GridAdapter by lazy { GridAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_record, container, false)

        // Data binding
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.mSharedViewModel = mSharedViewModel

        // Observe LiveData
        mToDoViewModel.getAllData.observe(viewLifecycleOwner) { data ->
            mSharedViewModel.checkIfDatabaseEmpty(data)
        }

        // Set recyclerView
        setRecyclerView()

        // Set grid
        setGrid()

        return binding.root
    }

    private fun setRecyclerView() {
        val gridRecyclerView = binding.gridRecyclerView
        gridRecyclerView.adapter = gridadapter// to grid adapter
        gridRecyclerView.layoutManager = StaggeredGridLayoutManager(7, StaggeredGridLayoutManager.VERTICAL)

    }

     private fun setGrid() {
         val toDoDataList = mToDoViewModel.toDodataList
         Log.d("DATA", toDoDataList.toString())
         var recordList = mutableListOf<Record>()

         // Init data
         val dayTotal = arrayOf(-1, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

         // Get month
         val current = LocalDateTime.now()
         val nowYear = current.get(ChronoField.YEAR)
         val nowMonth = current.get(ChronoField.MONTH_OF_YEAR)
         val firstDay = if(nowMonth < 10) {
             "$nowYear-0$nowMonth-01"
         } else {
             "$nowYear-$nowMonth-01"
         }
//         Log.d("DATA", firstDay)
         val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
         val startDay = format.parse(firstDay).get(ChronoField.DAY_OF_WEEK)

         // set Text
         binding.textView.setText(if(nowMonth < 10) {
             "$nowYear-0$nowMonth"
         } else {
             "$nowYear-$nowMonth"
         })

         // set Grid
         for(i in 2..startDay) {
             var rec = Record(-1)
             recordList.add(rec)
         }
         for(i in 1..dayTotal[nowMonth]) {
            var rec = Record(0)
//            for(j in toDoDataList?.indices!!){
//                val format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//                val day = format.parse(toDoDataList[j].registerTime).get(ChronoField.DAY_OF_MONTH)
//                if(i == day) {
//                    rec.count += 1
//                }
//            }
             rec.count = setGridData(i)
             recordList.add(rec)
         }

         recordList.let{ gridadapter.setData(it) }
     }



//    private fun setGrid() {

//        val toDoDataList = mToDoViewModel.getAllData.value
//        var recordList = mutableListOf<Record>()
////        Log.d("DATA",toDoDataList.toString())
//
//        // Init data
//        val dayTotal = arrayOf(-1, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
//
//        // Get month
//        val current = LocalDateTime.now()
//        val nowMonth = current.get(ChronoField.MONTH_OF_YEAR)
//        for(i in 1..dayTotal[nowMonth]) {
//            var rec = Record(0)
//            for(j in toDoDataList?.indices!!){
////                val format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
////                val day = format.parse(toDoDataList[j].registerTime).get(ChronoField.DAY_OF_MONTH)
////                if(i == day) {
////                    rec.count += 1
////                }
//            }
//            Log.d("DATA", rec.count.toString())
//            recordList.add(rec)
//        }
//
//        recordList.let{ gridadapter.setData(it) }
//    }


     private fun setGridData(i: Int): Int {
         return if(i in 11..17) {
             1
         } else if(i == 18) {
                 10
         } else if(i == 19) {
             3
         } else {
             0
         }
     }

}