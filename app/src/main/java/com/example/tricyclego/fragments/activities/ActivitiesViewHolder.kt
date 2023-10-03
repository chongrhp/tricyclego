package com.example.tricyclego.fragments.activities

import android.annotation.SuppressLint
import android.text.Html
import androidx.recyclerview.widget.RecyclerView
import com.example.tricyclego.data.Activities
import com.example.tricyclego.databinding.ActivitiesItemLayoutBinding
import java.text.SimpleDateFormat

@Suppress("DEPRECATION")
class ActivitiesViewHolder(val binding:ActivitiesItemLayoutBinding):RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SimpleDateFormat")
    fun bind(activities: Activities){
        val inputFormat = SimpleDateFormat("MMMM dd, yyyy hh:mm:ss a")
        val actDate = inputFormat.format(activities.activityDate.toDate())
        binding.txtAddress.text = Html.fromHtml("<b><font color='#1E8323'>DEST:</font></b> ${activities.destinationPlace}")
        binding.txtDetailed.text = Html.fromHtml("<small><b><font color='#1E8323'>DATE:</font></b> " +
        "<i>${actDate}</i>, <b><font color='#1E8323'>Dist:</font></b> ${String.format("%.2f",activities.distanceInKm)}Km," +
        " Paid: ${String.format("%.2f",activities.paidFare)}<br/><b><font color='#1E8323'>Orig:</font></b> <i>${activities.originLat}</i>, " +
        "<i>${activities.originLng}</i>; <b><font color='#1E8323'>Dest:</font></b> <i>${activities.destinationLat}</i>, <i>${activities.destinationLng}</i></small>")
    }
}