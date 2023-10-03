package com.example.tricyclego.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.res.Resources
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.tricyclego.data.varUserId
import com.example.tricyclego.databinding.FragmentReportBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import org.eazegraph.lib.charts.PieChart
import org.eazegraph.lib.models.PieModel
import java.text.SimpleDateFormat
import java.util.Date

@Suppress("DEPRECATION")
class ReportFragment : Fragment() {
    init {
        // Retain the fragment instance across configuration changes
        retainInstance = true
    }

    private lateinit var tvJan: TextView
    private lateinit var tvFeb: TextView
    private lateinit var tvMar: TextView
    private lateinit var tvApr: TextView
    private lateinit var tvMay: TextView
    private lateinit var tvJun: TextView
    private lateinit var tvJul: TextView
    private lateinit var tvAug: TextView
    private lateinit var tvSep: TextView
    private lateinit var tvOct: TextView
    private lateinit var tvNov: TextView
    private lateinit var tvDec: TextView
    private lateinit var pieChart: PieChart
    private lateinit var _binding : FragmentReportBinding
    val binding get() = _binding

    private var varJan = 0.0
    private var varFeb = 0.0
    private var varMar = 0.0
    private var varApr = 0.0
    private var varMay = 0.0
    private var varJun = 0.0
    private var varJul = 0.0
    private var varAug = 0.0
    private var varSep = 0.0
    private var varOct = 0.0
    private var varNov = 0.0
    private var varDec = 0.0

    @SuppressLint("DiscouragedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        _binding.lifecycleOwner = this

        tvJan = binding.tvJan
        tvFeb = binding.tvFeb
        tvMar = binding.tvMar
        tvApr = binding.tvApr
        tvMay = binding.tvMay
        tvJun = binding.tvJun
        tvJul = binding.tvJul
        tvAug = binding.tvAug
        tvSep = binding.tvSep
        tvOct = binding.tvOct
        tvNov = binding.tvNov
        tvDec = binding.tvDec

        binding.txtTotalExpenses.text = Html.fromHtml(
            "<font color='#0F7513'>Expenses:</font> " +
                    "<font color='#673AB7'><b>0.00</b></font>")

        pieChart = binding.piechart
        reports()

        val btnYear = binding.btnYear
        val dialog = datetimepicker()
        btnYear.setOnClickListener {
            dialog.show()

            val month = dialog.findViewById<View>(Resources.getSystem().getIdentifier("android:id/month",null,null))
            val day = dialog.findViewById<View>(Resources.getSystem().getIdentifier("android:id/day",null,null))
            if(month != null)month.visibility = View.GONE
            if(day != null)day.visibility = View.GONE

        }

        return binding.root
    }

    private fun datetimepicker():DatePickerDialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this.requireContext(),
            android.R.style.Theme_Holo_Light_Dialog_MinWidth,
            { _, i, _, _ ->

                binding.txtYear.text = i.toString()
                reports(i)
            }, year, month, day)
        return datePickerDialog
    }

    private fun setData(){
        // Set the percentage of language used
        tvJan.text = String.format("%,.2f",varJan)
        tvFeb.text = String.format("%,.2f",varFeb)
        tvMar.text = String.format("%,.2f",varMar)
        tvApr.text = String.format("%,.2f",varApr)
        tvMay.text = String.format("%,.2f",varMay)
        tvJun.text = String.format("%,.2f",varJun)
        tvJul.text = String.format("%,.2f",varJul)
        tvAug.text = String.format("%,.2f",varAug)
        tvSep.text = String.format("%,.2f",varSep)
        tvOct.text = String.format("%,.2f",varOct)
        tvNov.text = String.format("%,.2f",varNov)
        tvDec.text = String.format("%,.2f",varDec)

        // Set the data and color to the pie chart
        pieChart.addPieSlice(PieModel("Jan",varJan.toFloat(),Color.parseColor("#F44336")))
        pieChart.addPieSlice(PieModel("Feb",varFeb.toFloat(),Color.parseColor("#009688")))
        pieChart.addPieSlice(PieModel("Mar", varMar.toFloat(),Color.parseColor("#E91E63")))
        pieChart.addPieSlice(PieModel("Apr", varApr.toFloat(),Color.parseColor("#4CAF50")))
        pieChart.addPieSlice(PieModel("May", varMay.toFloat(),Color.parseColor("#9C27B0")))
        pieChart.addPieSlice(PieModel("Jun", varJun.toFloat(),Color.parseColor("#8BC34A")))
        pieChart.addPieSlice(PieModel("Jul", varJul.toFloat(),Color.parseColor("#673AB7")))
        pieChart.addPieSlice(PieModel("Aug", varAug.toFloat(),Color.parseColor("#CDDC39")))
        pieChart.addPieSlice(PieModel("Sep", varSep.toFloat(),Color.parseColor("#3F51B5")))
        pieChart.addPieSlice(PieModel("Oct", varOct.toFloat(),Color.parseColor("#FFEB3B")))
        pieChart.addPieSlice(PieModel("Nov", varNov.toFloat(),Color.parseColor("#2196F3")))
        pieChart.addPieSlice(PieModel("Dec", varDec.toFloat(),Color.parseColor("#FFC107")))
        pieChart.startAnimation()
    }

    private fun resetValues(){
        varJan = 0.0
        varFeb = 0.0
        varMar = 0.0
        varApr = 0.0
        varMay = 0.0
        varJun = 0.0
        varJul = 0.0
        varAug = 0.0
        varSep = 0.0
        varOct = 0.0
        varNov = 0.0
        varDec = 0.0

    }


    @SuppressLint("SimpleDateFormat")
    private fun reports(year:Int = 2023){

        val startDate = Timestamp(Date(year - 1900, 0,0, 0, 0))
        val endDate = Timestamp(Date(year - 1900,11, 31, 23, 59))
        resetValues()

        val db= FirebaseFirestore.getInstance()
        val collectionRef = db.collection("activities")

        var totalExpense = 0.0
        val query = collectionRef
            .whereEqualTo("userId", varUserId)
            .whereGreaterThanOrEqualTo("activityDate", startDate)
            .whereLessThanOrEqualTo("activityDate", endDate)


     query.get()
         .addOnSuccessListener {querySnapShot ->

              for (documentSnapshot in querySnapShot.documents){
                  val activities = documentSnapshot.data

                  val activitiesDate = activities?.get("activityDate") as Timestamp
                  val paidFare = activities["paidFare"].toString().toDouble()
                  val inputFormat = SimpleDateFormat("MM")



                  when (inputFormat.format(activitiesDate.toDate()).toInt()) {
                      1 -> varJan += paidFare
                      2 -> varFeb += paidFare
                      3 -> varMar += paidFare
                      4 -> varApr += paidFare
                      5 -> varMay += paidFare
                      6 -> varJun += paidFare
                      7 -> varJul += paidFare
                      8 -> varAug += paidFare
                      9 -> varSep += paidFare
                      10 -> varOct += paidFare
                      12 -> varNov += paidFare
                      13 -> varDec += paidFare
                  }

                  totalExpense += paidFare

              }

             setData()
             val txtExpenses = Html.fromHtml("<font color='#0F7513'>Expenses:</font> " +
                     "<font color='#673AB7'><b>${String.format("%,.2f", totalExpense)}</b></font>")
             binding.txtTotalExpenses.text = txtExpenses
             //.makeText(this.context,"Success to load", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this.context,"Failed to load", Toast.LENGTH_SHORT).show()
            }


    }


}