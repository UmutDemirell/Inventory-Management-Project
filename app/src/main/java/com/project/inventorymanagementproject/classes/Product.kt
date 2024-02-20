package com.project.inventorymanagementproject.classes

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    private val productBarcode: String = "",
    private val productTitle:String = "",
    private val productExplanation: String = "",
    private val productFeatures: String = "",
    private val productStockCount: Int = 0,
    private val productRequestCount: Int = 0,
    private val productImage: String = "",
    private val productTags: ArrayList<String> = ArrayList()
) : Parcelable{

    public fun getProductBarcode():String{
        return this.productBarcode
    }

    public fun getProductTitle():String{
        return this.productTitle
    }

    public fun getProductExplanation():String{
        return this.productExplanation
    }

    public fun getProductFeatures():String{
        return this.productFeatures
    }

    public fun getProductStockCount(): Int{
        return this.productStockCount
    }

    public fun getProductRequestCount(): Int{
        return  this.productRequestCount
    }

    public fun getProductImage():String{
        return this.productImage
    }

    public fun getProductTags():ArrayList<String>{
        return this.productTags
    }
}
