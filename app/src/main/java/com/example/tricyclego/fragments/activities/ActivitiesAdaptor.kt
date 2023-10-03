package com.example.tricyclego.fragments.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tricyclego.data.Activities
import com.example.tricyclego.databinding.ActivitiesItemLayoutBinding

class ActivitiesAdaptor(private val activities:List<Activities>):RecyclerView.Adapter<ActivitiesViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivitiesViewHolder {
        val inflate = LayoutInflater.from(parent.context)
        val binding = ActivitiesItemLayoutBinding.inflate(inflate, parent, false)
        return ActivitiesViewHolder(binding)
    }

    override fun getItemCount(): Int {return activities.size}

    override fun onBindViewHolder(holder: ActivitiesViewHolder, position: Int) {
        holder.bind(activities[position])
        holder.itemView.setOnClickListener {
            //Toast.makeText(this@ActivitiesAdaptor,"Sample",Toast.LENGTH_SHORT).show()
            //val mainAct = MainActivity::class.java
        }
    }


}