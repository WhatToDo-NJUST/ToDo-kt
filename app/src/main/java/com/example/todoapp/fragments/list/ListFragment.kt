package com.example.todoapp.fragments.list

import android.R.id.message
import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
import com.example.todoapp.utils.observeOnce
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okio.IOException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


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

        // Data binding
        _binding = FragmentListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.mSharedViewModel = mSharedViewModel

        // Setup RecyclerView
        setupRecyclerview()

        // Observe LiveData
        mToDoViewModel.getAllData.observe(viewLifecycleOwner) { data ->
            mSharedViewModel.checkIfDatabaseEmpty(data)
            adapter.setData(data)
            binding.recyclerView.scheduleLayoutAnimation()
        }

        // Set Menu
        setHasOptionsMenu(true)

        // Hide soft keyboard
        hideKeyboard(requireActivity())

        login()

        return binding.root
    }


    private fun setupRecyclerview() {
        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)

        // Swipe to Delete
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
            R.id.menu_download_plan->download()
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

    private fun countDown() {

    }

    // Show AlertDialog to Confirm Removal of All Items from Database Table
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

    private fun makeRequest(request: Request):String{
        var flag=false
        var res="res"
        OkHttpClient().newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    flag=true
                }

                override fun onResponse(call: Call, response:Response) {
                    res= response.body.string()
                    flag=true
                }
            })


        while(!flag){}
        return res
    }

    private fun login(){
        val url = "http://10.0.2.2:10001//user/login"

        val requestBody = FormBody.Builder()
            .add("phone", "13750794329")
            .add("password", "123456")
            .build()

        //创建request请求对象
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val res=makeRequest(request)
        val jsonObject=JSONObject(res)
        val data=jsonObject.getJSONObject("data")
        val sharedPreferences:SharedPreferences= (context?.getSharedPreferences("data",MODE_PRIVATE) ?:null) as SharedPreferences
        val editor:SharedPreferences.Editor=sharedPreferences.edit()
        editor.putString("token",data.getString("token"))
        editor.apply()

    }

    private fun download(){
        val builder=makeAlertDialog("Successfully download data!","Download Data",
            "Are you sure you want to download data?"
        ) { downloadPlan() }
        builder.create().show()
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


        val jsonObject=JSONObject(res)
        val data=jsonObject.getJSONArray("data")

        val lists=stringToArray(data.toString(), Array<ToDoData>::class.java)
        lists?.let { adapter.setData(it) }

    }


    fun <T> stringToArray(s: String?, clazz: Class<Array<T>>?): List<T>? {
        val arr = Gson().fromJson(s, clazz)
        return listOf(*arr)//or return Arrays.asList(new Gson().fromJson(s, clazz)); for a one-liner
    }

    fun uploadPlan(){
        val url = "http://10.0.2.2:10001//todo/upload"
        var shp = context?.getSharedPreferences("data", MODE_PRIVATE)
        var token: String? = shp?.getString("token","")

//        Log.d("DATA",token.toString())

        val requestBody = FormBody.Builder()
            .add("token", token.toString())
            .build()

        //创建request请求对象
        val request = okhttp3.Request.Builder()
            .url(url)
            .addHeader("token",token.toString())
            .post(requestBody)
            .build()

        //创建call并调用enqueue()方法实现网络请求

        var flag=false
        var res="res"
        OkHttpClient().newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                }

                override fun onResponse(call: Call, response: okhttp3.Response) {
                    res= response.body.string()
//                    Log.d("DATA", result.toString())

                    flag=true
                }
            })

        while(!flag){}
        val jsonObject=JSONObject(res)
        val data=jsonObject.getJSONArray("data")
        Log.d("DATA",data.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}