package com.example.tricyclego.data

import com.google.firebase.Timestamp

var chatId = ""
var chatEnabled = false
var varDriverId = ""
var varPassId = ""
var varPassName = ""
var varDriverName = ""


data class Chats(
    val chatId: String,
    val passengerId: String,
    val driverId:String,
    val dateChat: Timestamp,
    val messages: String,
    val passengerMsg: Boolean)

data class ChatId(
    val chatIdDate: Timestamp,
    val passId: String,
    val driverId: String,
    val passName: String,
    val driverName: String,
    val chatOpen: Boolean,
)