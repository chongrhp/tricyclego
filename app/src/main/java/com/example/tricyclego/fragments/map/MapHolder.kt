package com.example.tricyclego.fragments.map

import android.text.Html
import androidx.recyclerview.widget.RecyclerView
import com.example.tricyclego.data.VTricycle
import com.example.tricyclego.databinding.AvailableRiderBinding
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

@Suppress("DEPRECATION")
class MapHolder(val binding:AvailableRiderBinding): RecyclerView.ViewHolder(binding.root) {
    fun bind(vacantTricycle: VTricycle){
        val orgLat = vacantTricycle.orgLat
        val orgLng = vacantTricycle.orgLng
        val passLat = vacantTricycle.passLat
        val passLng = vacantTricycle.passLng

        val meToPass = SphericalUtil.computeDistanceBetween(LatLng(orgLat,orgLng), LatLng(passLat,passLng))
        var showDistance = ""
        if((meToPass/1000) < 1) showDistance = "${String.format("%.2f",meToPass)}Meters"
        else showDistance = "${String.format("%.2f",meToPass/1000)}Kms"

        binding.txtTricycleNo.text = Html.fromHtml("Tricycle No. ${vacantTricycle.tricycle_no} / $showDistance")
        binding.txtDriverName.text = Html.fromHtml("Driver: ${vacantTricycle.driver_name}")
        binding.txtDriverContactNo.text = Html.fromHtml("Contact No. ${vacantTricycle.contact_no}")
    }


}