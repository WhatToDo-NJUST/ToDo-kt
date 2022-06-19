package com.example.todoapp.fragments.record.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.data.models.Record
import com.example.todoapp.databinding.GridLayoutBinding

class GridAdapter : RecyclerView.Adapter<GridAdapter.MyViewHolder>() {

    var dataList = emptyList<Record>()

    class MyViewHolder(private val binding: GridLayoutBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(record: Record){
            binding.record = record
            binding.executePendingBindings()
        }
        companion object{
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = GridLayoutBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(
                    binding
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(
            parent
        )
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = dataList[position]
        holder.bind(currentItem)
    }

    fun setData(record: List<Record>){
        this.dataList = record
        notifyDataSetChanged()
    }

}