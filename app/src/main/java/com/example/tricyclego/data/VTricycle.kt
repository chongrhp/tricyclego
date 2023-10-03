package com.example.tricyclego.data

import com.google.firebase.Timestamp

var user_type = "fLUMCV4B7ERAPJ6pJcKY"
var btnCancelDialog = false
var availableId = ""
var phoneCall = ""

data class VTricycle(
    val activityId: String,
    val dtOnline: Timestamp,
    val driverId: String,
    val passId: String,
    val driver_name: String,
    val tricycle_no: String,
    val orgLat: Double,
    val orgLng: Double,
    val passLat: Double,
    val passLng: Double,
    val destLat: Double,
    val destLng: Double,
    val contact_no: String,
    val picked: Boolean,
    val accepted: Boolean,
    val rejected: Boolean,
    val passInKm: Double,
    val disInKm: Double,
    val paidFare: Double,
    val completed: Boolean,
)
