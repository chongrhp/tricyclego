package com.example.tricyclego.data

import com.google.firebase.Timestamp
var varUserId = ""
var driverId = ""
var passengerAddress = ""
var passContactNo = ""


data class Activities(
    val userId:String,
    val activityDate: Timestamp,
    val originLat:Double,
    val originLng: Double,
    val destinationLat: Double,
    val destinationLng: Double,
    val destinationPlace:String,
    val distanceInKm:Double,
    val paidFare:Double)