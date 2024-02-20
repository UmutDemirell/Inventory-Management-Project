package com.project.inventorymanagementproject.classes

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.project.inventorymanagementproject.NavGraphDirections
import com.project.inventorymanagementproject.R
import kotlinx.coroutines.processNextEventInCurrentThread

private const val EXPLANATION_CHAR_LENGTH = 60
private const val TITLE_CHAR_LENGTH = 50

class ProductAdapter (
    private val productList: ArrayList<Product>
): RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val productImage: ImageView = itemView.findViewById(R.id.card_photo)
        var productTitle: TextView = itemView.findViewById(R.id.card_title)
        var productExplanation: TextView = itemView.findViewById(R.id.card_explanation)
        var productStockValue: TextView = itemView.findViewById(R.id.card_stock)
        var productRequestButton: Button = itemView.findViewById(R.id.card_button)
        var productCard: CardView = itemView.findViewById(R.id.product_card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_card, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        calculateCornerRadius(holder)

        if(product.getProductImage() != "null"){
            Glide.with(holder.itemView.context)
                .load(product.getProductImage())
                .transform(CenterCrop(), RoundedCorners(cornerRadius))
                .into(holder.productImage)
        }else{
            holder.productImage.setImageDrawable(null)
        }

        var productExplanation = product.getProductExplanation()
        var spaceIndex = 0

        if(productExplanation.length > EXPLANATION_CHAR_LENGTH){
            productExplanation = productExplanation.take(EXPLANATION_CHAR_LENGTH)
            spaceIndex = productExplanation.lastIndexOf(' ')
            holder.productExplanation.text = productExplanation.take(spaceIndex).plus("...")
        }else{
            holder.productExplanation.text = productExplanation
        }

        var productTitle= product.getProductTitle()

        if(productTitle.length > TITLE_CHAR_LENGTH){
            productTitle = productTitle.take(TITLE_CHAR_LENGTH)
            spaceIndex = productTitle.lastIndexOf(' ')
            holder.productTitle.text = productTitle.take(spaceIndex).plus("...")
        }else{
            holder.productTitle.text = productTitle
        }

        holder.productStockValue.text = "Stok: ".plus(product.getProductStockCount())

        holder.productCard.isVisible = true

        holder.itemView.setOnClickListener(View.OnClickListener {
            productDetails(product, it)
        })

        holder.productRequestButton.setOnClickListener(View.OnClickListener {
            if(product.getProductStockCount() > 0){
                productRequest(product, it)
            }else{
                Toast.makeText(holder.itemView.context, "Stokta ürün bulunmamakta", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private var cornerRadius: Int = 0
    private fun calculateCornerRadius(holder: ProductViewHolder){
        val scale = holder.itemView.context.resources.displayMetrics.density
        cornerRadius = (15.0f * scale + 0.5f).toInt()
    }

    private fun productDetails(product: Product, view: View){
        val productBundle = bundleOf("SelectedProduct" to product)
        val actionProductDetails = NavGraphDirections.actionGlobalProductDetailsFragment()
        view.findNavController().navigate(actionProductDetails.actionId, productBundle)
    }

    private fun productRequest(product: Product, view: View){
        val productBundle = bundleOf("SelectedProduct" to product)
        val actionProductRequest = NavGraphDirections.actionGlobalRequestFormFragment()
        view.findNavController().navigate(actionProductRequest.actionId, productBundle)
    }
}