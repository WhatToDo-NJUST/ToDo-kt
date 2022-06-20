package com.example.todoapp.fragments.list

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.todoapp.R
import com.example.todoapp.data.models.ToDoData
import com.example.todoapp.data.viewmodel.ToDoViewModel
import com.example.todoapp.databinding.FragmentListBinding
import com.example.todoapp.fragments.SharedViewModel
import com.example.todoapp.fragments.list.adapter.ListAdapter
import com.example.todoapp.utils.hideKeyboard
import com.example.todoapp.utils.makeRequest
import com.example.todoapp.utils.observeOnce
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.IOException
import org.json.JSONObject
import java.util.*
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody



class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val adapter: ListAdapter by lazy { ListAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view =  inflater.inflate(R.layout.fragment_list, container, false)

        _binding = FragmentListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.mSharedViewModel = mSharedViewModel

        setupRecyclerview()

        mToDoViewModel.getAllData.observe(viewLifecycleOwner) { data ->
            mSharedViewModel.checkIfDatabaseEmpty(data)
            adapter.setData(data)
            binding.recyclerView.scheduleLayoutAnimation()
        }

//        mToDoViewModel.getAllDataByDate(mSharedViewModel.getCurrentData().toString()).observe(viewLifecycleOwner) { data ->
//            mSharedViewModel.checkIfDatabaseEmpty(data)
//            adapter.setData(data)
//            binding.recyclerView.scheduleLayoutAnimation()
//        }

        setHasOptionsMenu(true)

        hideKeyboard(requireActivity())

        setDateTimePicker()

        return binding.root
    }


    private fun setupRecyclerview() {
        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)

        swipeToDelete(recyclerView)
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem = adapter.dataList[viewHolder.adapterPosition]
                // Delete Item
                mToDoViewModel.deleteItem(deletedItem)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                // Restore Deleted Item
                restoreDeletedData(viewHolder.itemView, deletedItem)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeletedData(view: View, deletedItem: ToDoData) {
        val snackBar = Snackbar.make(
            view, "Deleted '${deletedItem.title}'",
            Snackbar.LENGTH_LONG
        )
        snackBar.setAction("Undo") {
            mToDoViewModel.insertData(deletedItem)
        }
        snackBar.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_delete_all -> confirmRemoval()
            R.id.menu_priority_high -> mToDoViewModel.sortByHighPriority.observe(viewLifecycleOwner, { adapter.setData(it) })
            R.id.menu_priority_low -> mToDoViewModel.sortByLowPriority.observe(viewLifecycleOwner, { adapter.setData(it) })
            R.id.menu_sortByTime ->mToDoViewModel.sortByTime.observe(viewLifecycleOwner,{adapter.setData(it)})
            R.id.menu_download_plan-> download()
            R.id.menu_upload_plan->upload()
            R.id.menu_login->findNavController().navigate(R.id.action_listFragment_to_loginFragment)
            R.id.menu_exit->exit()
            R.id.menu_grid->findNavController().navigate(R.id.action_listFragment_to_recordFragment)
        }
        return super.onOptionsItemSelected(item)
    }



    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    private fun searchThroughDatabase(query: String) {
        val searchQuery = "%$query%"

        mToDoViewModel.searchDatabase(searchQuery).observeOnce(viewLifecycleOwner, { list ->
            list?.let {
                Log.d("ListFragment", "searchThroughDatabase")
                adapter.setData(it)
            }
        })
    }


    private fun confirmRemoval() {
        val builder=makeAlertDialog("Successfully Removed Everything!","Delete everything?",
            "Are you sure you want to remove everything?"
        ) { mToDoViewModel.deleteAll() }
        builder.create().show()
    }

    private fun makeAlertDialog(yesMessage:String,title:String,message:String,
    function:()-> Unit):AlertDialog.Builder{
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            function()
            Toast.makeText(
                requireContext(),
                yesMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle(title)
        builder.setMessage(message)
        return builder
    }


    private fun download(){
        var shp= context?.getSharedPreferences("data", MODE_PRIVATE)!!
        if(shp.getString("token",null)==null){
            Toast.makeText(context,"请先登录",Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_listFragment_to_loginFragment)
        }
        else{
            val builder=makeAlertDialog("download data!","Download Data",
                "Are you sure you want to download data?"
            ) { downloadPlan() }
            builder.create().show()
        }

    }

    private fun downloadPlan(){
        val url = "http://10.0.2.2:10001//todo/download"
        var shp = context?.getSharedPreferences("data", MODE_PRIVATE)
        var token: String? = shp?.getString("token","")
        //创建request请求对象
        val request = Request.Builder()
            .url(url)
            .addHeader("token",token.toString())
            .build()

        val res=makeRequest(request)
        if (res == "error") {
            Toast.makeText(context,"网络故障，请调试网络后重新登录",
                Toast.LENGTH_SHORT).show()
            return
        }


        val jsonObject=JSONObject(res)
        val data=jsonObject.getJSONArray("data")

        val lists=stringToArray(data.toString(), Array<ToDoData>::class.java)
        lists?.let { adapter.setData(it) }
        mSharedViewModel.emptyDatabase.value=false
    }


    fun upload(){
        var shp= context?.getSharedPreferences("data", MODE_PRIVATE)!!
        if(shp.getString("token",null)==null){
            Toast.makeText(context,"请先登录",Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_listFragment_to_loginFragment)
        }else{
            val builder=makeAlertDialog("upload data!","Upload Data",
                "Are you sure you want to upload data?"
            ) { uploadPlan() }
            builder.create().show()
        }
    }

    fun uploadPlan(){
        val url = "http://10.0.2.2:10001//todo/upload"
        var shp = context?.getSharedPreferences("data", MODE_PRIVATE)
        var token: String? = shp?.getString("token","")

//        Log.d("DATA",token.toString())

        val jsonArray=JsonArray()

        val lists=mToDoViewModel.getAllData.value


        for(i in lists?.indices!!){
            val jo=JsonObject()
            val shp= context?.getSharedPreferences("data", MODE_PRIVATE)!!
            val userId=shp.getInt("userId",lists[i].userId)
            jo.addProperty("id",lists[i].id)
            jo.addProperty("userId",userId)
            jo.addProperty("title",lists[i].title)
            jo.addProperty("priority",lists[i].priority.toString())
            jo.addProperty("description",lists[i].description)
            jo.addProperty("isDone",lists[i].isDone.toString())
            jo.addProperty("registerTime",lists[i].registerTime)
//            jo.addProperty("registerTime","2022-12-12 12:12:12")
            jsonArray.add(jo)
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

        val requestBody = jsonArray.toString().toRequestBody(mediaType)



        //创建request请求对象
        val request = Request.Builder()
            .url(url)
            .addHeader("token",token.toString())
            .post(requestBody)
            .build()


        val res=makeRequest(request)
        if (res == "error") {
            Toast.makeText(context,"网络故障，请调试网络后重新登录",
                Toast.LENGTH_SHORT).show()
            return
        }
    }

    fun exit(){
        var shp= context?.getSharedPreferences("data", MODE_PRIVATE)!!
        var editor:SharedPreferences.Editor=shp.edit()
        editor.clear()
        editor.apply()
        Toast.makeText(context,"退出登录,若需要请重新登录",Toast.LENGTH_LONG).show()
    }

    fun <T> stringToArray(s: String?, clazz: Class<Array<T>>?): List<T>? {
        val arr = Gson().fromJson(s, clazz)
        return listOf(*arr)//or return Arrays.asList(new Gson().fromJson(s, clazz)); for a one-liner
    }

    @SuppressLint("SetTextI18n")
    private fun setDateTimePicker() {
        binding.currentDate.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            context?.let { it1 ->
                DatePickerDialog(it1, { _, year, month, day ->
                    run {
                        val format = "${setDateFormat(year, month, day)}"
                        binding.currentDate.setText(format)
                    }
                }, year, month, day).show()
            }
        }


    }


    private fun setDateFormat(year: Int, month: Int, day: Int): String {
        var month1=(month+1).toString()
        var day1=day.toString()
        if(month+1<10) month1="0"+month1
        if(day<10) day1="0"+day1
        return "$year-$month1-$day1"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}