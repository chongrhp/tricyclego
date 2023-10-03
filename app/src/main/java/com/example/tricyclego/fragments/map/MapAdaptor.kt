package com.example.tricyclego.fragments.map

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tricyclego.R
import com.example.tricyclego.data.Activities
import com.example.tricyclego.data.VTricycle
import com.example.tricyclego.data.availableId
import com.example.tricyclego.data.btnCancelDialog
import com.example.tricyclego.data.driverId
import com.example.tricyclego.data.passContactNo
import com.example.tricyclego.data.passengerAddress
import com.example.tricyclego.data.phoneCall
import com.example.tricyclego.data.varDriverId
import com.example.tricyclego.data.varDriverName
import com.example.tricyclego.data.varUserId
import com.example.tricyclego.databinding.AvailableRiderBinding
import com.example.tricyclego.databinding.SelectectedRiderBinding
import com.example.tricyclego.databinding.ShowWaitForAcceptanceBinding
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.SphericalUtil

@Suppress("DEPRECATION")
class MapAdaptor(
    private val context:Context,
    private val myModel: MapViewModel,
    private val vacantTricycle:List<VTricycle>):RecyclerView.Adapter<MapHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MapHolder {
        val inflate = LayoutInflater.from(parent.context)
        val binding = AvailableRiderBinding.inflate(inflate, parent, false)
        return MapHolder(binding)
    }

    override fun getItemCount(): Int {
        return vacantTricycle.size
    }

    override fun onBindViewHolder(holder: MapHolder, position: Int) {
        holder.bind(vacantTricycle[position])
        holder.itemView.setOnClickListener {
            holder.binding.backLayout.setBackgroundResource(R.color.colorGray)
            showDialog(position)
        }
    }


    private fun showDialog(position: Int){
        val alertDialogBuilder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.selectected_rider, null)
        val dialogBinding = SelectectedRiderBinding.bind(dialogView)
        val btnOk = dialogBinding.btnRideOk
        val btnCancel = dialogBinding.btnRideCancel
        val driverId = vacantTricycle[position].driverId
        val orgLat = vacantTricycle[position].orgLat
        val orgLng = vacantTricycle[position].orgLng
        val passLat = vacantTricycle[position].passLat
        val passLng = vacantTricycle[position].passLng
        val destLat = vacantTricycle[position].destLat
        val destLng = vacantTricycle[position].destLng
        val activityId = vacantTricycle[position].activityId
        varDriverName =vacantTricycle[position].driver_name
        phoneCall = vacantTricycle[position].contact_no

        val meToPass = SphericalUtil.computeDistanceBetween(
            LatLng(orgLat,orgLng), LatLng(passLat,passLng))
        val passToDest = SphericalUtil.computeDistanceBetween(
            LatLng(passLat,passLng),LatLng(destLat,destLng))
        val meToPassInKm = meToPass/1000
        var pasToDestInKm = passToDest/1000
        var additionalFare = 0.0
        if((meToPassInKm+pasToDestInKm-1) >= 0.5) { additionalFare = (((meToPassInKm+pasToDestInKm) - 1)/0.5) * 5}
        val fare = 10+additionalFare
        var distanceRecord = ""
        if(pasToDestInKm < 1){
            distanceRecord = "${String.format("%,.2f",passToDest)}Meters"
        } else distanceRecord = "${String.format("%,.2f",pasToDestInKm)}Km"

        dialogBinding.txtDetailsTricycleNo.text = Html.fromHtml("<b>Tricycle No.</b> <font color='#ffffff'>${vacantTricycle[position].tricycle_no}</font>")
        dialogBinding.txtDetailDriverName.text = Html.fromHtml("<b>Driver name:</b> ${vacantTricycle[position].driver_name}")
        dialogBinding.txtDetailDistance.text = Html.fromHtml("<b>Distance:</b> $distanceRecord")
        dialogBinding.txtFareAmount.text = Html.fromHtml("<b>Fare:</b> Php <font color='red'>${String.format("%,.2f",fare)}</font>")

        alertDialogBuilder.setView(dialogView)
        val showDial = alertDialogBuilder.create()
        btnOk.setOnClickListener {
            //addActivity(passLat, passLng, destLat, destLng, passengerAddress,pasToDestInKm, fare)
            updateAndWaitForAcceptance(activityId, driverId, passLat, passLng,
                destLat,destLng, passengerAddress, meToPassInKm, passToDest, fare)

            showDial.dismiss()
        }

        btnCancel.setOnClickListener {
            btnCancelDialog = true
            showDial.dismiss()
        }

        alertDialogBuilder.setOnDismissListener(DialogInterface.OnDismissListener {
            return@OnDismissListener
        })

        showDial.setCanceledOnTouchOutside(false)
        showDial.show()
    }

    private fun addActivity(orgLat:Double, orgLng: Double, disLat:Double,
        distLng: Double, toDist: String, distInKm: Double, paidFare:Double){
        val newActivity = Activities(varUserId, Timestamp.now(), orgLat, orgLng,
        disLat, distLng, toDist, distInKm, paidFare)

        Firebase.firestore.collection("activities")
            .add(newActivity)
            .addOnSuccessListener {}
            .addOnFailureListener {Toast.makeText(this.context,"Failed to proceed...",Toast.LENGTH_LONG).show()}

    }

    private fun updateAndWaitForAcceptance(
        actId:String, driversId: String, passLat: Double,
        passLng: Double, destLat: Double, destLng: Double,
        passengerAddress:String, passInKm: Double,
        disInKm:Double, fare: Double){
        availableId = actId
        driverId = driversId
        varDriverId = driversId

        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("vacant_tricycle").document(actId)
        val updateVTricycle = hashMapOf<String, Any?>(
            "passId" to varUserId, "passLat" to passLat, "passLng" to passLng,
            "destLat" to destLat,"destLng" to destLng, "picked" to true, "dtpicked" to Timestamp.now(),
            "passInKm" to passInKm, "disInKm" to disInKm, "paidFare" to fare, "passContactNo" to passContactNo)
        collectionRef.update(updateVTricycle)
            .addOnSuccessListener {showWaitDialog(actId, passLat, passLng,
                destLat, destLng, passengerAddress, disInKm, fare) }
            .addOnFailureListener {}
    }


    private fun showWaitDialog(activityId: String, passLat: Double, passLng: Double,
    destLat:Double, destLng: Double, passengerAddress: String, pasToDestInKm: Double, fare: Double){
        val waitForResDialog = AlertDialog.Builder(context)
        val dialogWait = LayoutInflater.from(context).inflate(R.layout.show_wait_for_acceptance, null)
        val dialogBind = ShowWaitForAcceptanceBinding.bind(dialogWait)
        val btnWait = dialogBind.btnWaitForResponse
        val txtResponse = dialogBind.txtWaitForResponse
        val imgView = dialogBind.imageView5
        val txtTricycleResponse = dialogBind.txtTricycleResponse
        var boolAccepted = false
        var boolRejected = false

        Glide
            .with(this.context)
            .load(R.drawable.circlesmenu)
            .into(imgView)


        val db = FirebaseFirestore.getInstance()
        val collRef = db.collection("vacant_tricycle").document(activityId)
        collRef.addSnapshotListener { documentSnapshot, error ->
            if(error != null){return@addSnapshotListener}
            if(documentSnapshot != null && documentSnapshot.exists()) {
                boolAccepted = documentSnapshot.getBoolean("accepted") as Boolean
                boolRejected = documentSnapshot.getBoolean("rejected") as Boolean
                if(boolAccepted) {
                    addActivity(passLat, passLng, destLat, destLng, passengerAddress,pasToDestInKm, fare)
                    myModel.setService(boolAccepted)
                    completeService(activityId)
                    btnWait.performClick()
                }
                if(boolRejected) {
                    txtResponse.text = "Tricycle refuse to accept your request!"
                    imgView.isVisible = false
                    txtTricycleResponse.isVisible = true
                    btnWait.setText("Close")

                }
            }
        }


        waitForResDialog.setOnDismissListener(DialogInterface.OnDismissListener {
            return@OnDismissListener
        })

        waitForResDialog.setView(dialogWait)
        val alertDialog = waitForResDialog.create()
        alertDialog.setCanceledOnTouchOutside(false)
        btnWait.setOnClickListener {
            txtTricycleResponse.isVisible = false
            //chatEnabled = true
            alertDialog.dismiss()
        }


        alertDialog.show()
    }


    private fun completeService(docId: String){
        val db3 = FirebaseFirestore.getInstance()
        val collRef = db3.collection("vacant_tricycle").document(docId)
        collRef.addSnapshotListener { documentSnapshot, error ->
            if(error != null){return@addSnapshotListener}
            if(documentSnapshot != null && documentSnapshot.exists()) {
                val isCompleted = documentSnapshot.getBoolean("completed") as Boolean
                if(isCompleted) myModel.setService(isCompleted)
            }
        }
    }

}





