package com.project.inventorymanagementproject.classes

import android.os.Build
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import java.time.ZoneId

@Parcelize
data class Request(
    private val requestId: String = "",
    private val productId: String = "",
    private val userId:String = "",
    private val requestDetail: String = "",
    private val requestUrgency: Int = 0,
    private val requestCount: Int = 0,
    private val requestState: Int = 0,
    private val requestDate: Timestamp = Timestamp.now(),
    private var requestImportance: Float = 0f
) : Parcelable {

    public fun getRequestId():String{
        return this.requestId
    }

    public fun getProductId():String{
        return this.productId
    }

    public fun getUserId():String{
        return this.userId
    }

    public fun getRequestDetail():String{
        return this.requestDetail
    }

    public fun getRequestUrgency():Int{
        return this.requestUrgency
    }

    public fun getRequestCount():Int{
        return this.requestCount
    }

    public fun getRequestState():Int{
        return this.requestState
    }

    public fun getRequestDate():Timestamp{
        return this.requestDate
    }

    public fun getRequestImportance():Float{
        return this.requestImportance
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public fun calculateRequestImportance(){
        val date = this.requestDate.toDate()
        val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val datePoint = localDate.monthValue.toString().plus(localDate.dayOfMonth.toString()).toInt()
        val requestImportance = this.requestUrgency / (datePoint/1000f)
        this.requestImportance = requestImportance
    }
}
