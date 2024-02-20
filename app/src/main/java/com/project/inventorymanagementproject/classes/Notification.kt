package com.project.inventorymanagementproject.classes

import com.google.firebase.Timestamp

data class Notification(
    private val notificationMessage: String = "",
    private val notificationDate: Timestamp =Timestamp.now()
){
    public fun getNotificationMessage():String{
        return this.notificationMessage
    }

    public fun getNotificationDate():Timestamp{
        return this.notificationDate
    }
}
