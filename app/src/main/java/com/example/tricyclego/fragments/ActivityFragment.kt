package com.example.tricyclego.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tricyclego.R
import com.example.tricyclego.data.Activities
import com.example.tricyclego.data.varUserId
import com.example.tricyclego.databinding.FragmentActivityBinding
import com.example.tricyclego.fragments.activities.ActivitiesAdaptor
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

@Suppress("DEPRECATION")
class ActivityFragment : Fragment() {

    private var varYear = 0
    private var varMonth = 0
    private var varDay = 0

    private lateinit var recyclerView: RecyclerView
    private lateinit var _binding : FragmentActivityBinding
    val binding get() = _binding
    @SuppressLint("SimpleDateFormat", "WeekBasedYear")
    val formatDate = SimpleDateFormat("MMMM dd, YYYY")

    @SuppressLint("WeekBasedYear")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentActivityBinding.inflate(inflater, container, false )
        _binding.lifecycleOwner = this

        recyclerView = binding.actRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val calendar = Calendar.getInstance()
        val formatter = DateTimeFormatter.ofPattern("MMMM dd, YYYY")
        val currentDate = LocalDateTime.now().format(formatter)

        binding.txtDateSelected.text = currentDate
        binding.btnDatePicker.isEnabled = false

        varYear = calendar.get(Calendar.YEAR)
        varMonth = calendar.get(Calendar.MONTH)
        varDay = calendar.get(Calendar.DAY_OF_MONTH)

        getAllActivities(false, varMonth, varDay, varYear)
        binding.chkDate.setOnClickListener {
            binding.btnDatePicker.isEnabled = binding.chkDate.isChecked
            if(binding.chkDate.isChecked) binding.btnDatePicker.setColorFilter(ContextCompat.getColor(this.requireContext(), R.color.border_text))
            else binding.btnDatePicker.setColorFilter(ContextCompat.getColor(this.requireContext(), R.color.background_color))
            if (!binding.chkDate.isChecked)getAllActivities(false,calendar.get(Calendar.MONTH),
               calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.YEAR))
        }

        binding.btnDatePicker.setOnClickListener {
            val getDate = Calendar.getInstance()
            val datePicker = DatePickerDialog(this.requireContext(), android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                { _, i, i2, i3 ->

                    varYear = i
                    varMonth = i2
                    varDay = i3

                    val selectDate = Calendar.getInstance()
                    selectDate.set(Calendar.YEAR, i)
                    selectDate.set(Calendar.MONTH, i2)
                    selectDate.set(Calendar.DAY_OF_MONTH, i3)
                    val date = formatDate.format(selectDate.time)
                    binding.txtDateSelected.text = date.toString()
                    getAllActivities(true,i2, i3, i)
                }, getDate.get(Calendar.YEAR), getDate.get(Calendar.MONTH),getDate.get(Calendar.DAY_OF_MONTH))
            datePicker.show()

        }


        return binding.root
    }


     @RequiresApi(Build.VERSION_CODES.O)
    private fun getAllActivities(isDate:Boolean, month:Int,  days:Int, years:Int){
        val db= FirebaseFirestore.getInstance()
        val collectionRef = db.collection("activities")

        val startDate = Timestamp(Date(years - 1900,month, if(isDate) days else 1, 0, 0))
        val endDate = Timestamp(Date(years - 1900,month, days, 23, 59))

        binding.progressBar2.isVisible = true
        var totalExpense = 0.0
        val query = collectionRef
            .whereEqualTo("userId", varUserId)
            .whereGreaterThanOrEqualTo("activityDate", startDate)
            .whereLessThanOrEqualTo("activityDate", endDate)

        query.get()
            .addOnSuccessListener {querySnapShot ->

                val activitiesList = mutableListOf<Activities>()
                for (documents in querySnapShot.documents){
                    val activities = documents.data
                    val userId = activities?.get("userId").toString()

                        val activitiesDate =
                            activities?.get("activityDate") as Timestamp
                        val originLat = activities["originLat"].toString().toDouble()
                        val originLng = activities["originLng"].toString().toDouble()
                        val destLat = activities["destinationLat"].toString().toDouble()
                        val destLng = activities["destinationLng"].toString().toDouble()
                        val destPlace = activities["destinationPlace"].toString()
                        val distInKm = activities["distanceInKm"].toString().toDouble()
                        val paidFare = activities["paidFare"].toString().toDouble()

                        val newActivities = Activities(
                            userId, activitiesDate,
                            originLat, originLng, destLat,
                            destLng, destPlace, distInKm, paidFare
                        )

                        activitiesList.add(newActivities)
                        totalExpense += paidFare
                }

                binding.txtTotalExpense.text = Html.fromHtml("Php ${String.format("%,.2f", totalExpense)}")
                recyclerView.adapter = ActivitiesAdaptor(activitiesList)
                binding.progressBar2.isVisible = false

            }
            .addOnFailureListener {
                Toast.makeText(this.context,"Failed to load: ${it.message}",Toast.LENGTH_LONG).show()
                binding.progressBar2.isVisible = false
            }

    }
}