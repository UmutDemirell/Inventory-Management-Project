package com.project.inventorymanagementproject.classes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.project.inventorymanagementproject.R
import java.text.SimpleDateFormat

private const val TITLE_CHAR_LENGTH = 50

class RequestAdapterHome(
    private val requestList: ArrayList<Request>
):RecyclerView.Adapter<RequestAdapterHome.RequestViewHolder>() {

    class RequestViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val productImage: ImageView = itemView.findViewById(R.id.card_photo)
        var productTitle: TextView = itemView.findViewById(R.id.card_title)
        var requestDate: TextView = itemView.findViewById(R.id.card_date)
        var cardView: CardView = itemView.findViewById(R.id.card_home)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.request_card_home, parent, false)
        return RequestAdapterHome.RequestViewHolder(view)
    }

    override fun getItemCount(): Int {
        return requestList.size
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requestList[position]
        calculateCornerRadius(holder)
        init()

        var cardProduct: Product

        itemsRef.document(request.getProductId()).get().addOnSuccessListener(OnSuccessListener {snapshot->
            cardProduct = Product(
                productTitle = snapshot.get("ProductTitle").toString(),
                productImage = snapshot.get("ProductImage").toString()
            )

            if(cardProduct.getProductImage() != "null"){
                Glide.with(holder.itemView.context)
                    .load(cardProduct.getProductImage())
                    .transform(CenterCrop(), RoundedCorners(cornerRadius))
                    .into(holder.productImage)
            }else{
                holder.productImage.setImageDrawable(null)
            }

            val dateFormat = SimpleDateFormat("dd-MM-yyyy")
            val requestDate = dateFormat.format(request.getRequestDate().toDate())

            var productTitle= cardProduct.getProductTitle()
            var spaceIndex = 0

            if(productTitle.length > TITLE_CHAR_LENGTH){
                productTitle = productTitle.take(TITLE_CHAR_LENGTH)
                spaceIndex = productTitle.lastIndexOf(' ')
                holder.productTitle.text = productTitle.take(spaceIndex).plus("...")
            }else{
                holder.productTitle.text = productTitle
            }

            holder.requestDate.text = " ".plus(requestDate)

            holder.cardView.isVisible = true
        }).addOnFailureListener(OnFailureListener {
            Toast.makeText(holder.itemView.context, it.message, Toast.LENGTH_SHORT).show()
        })
    }

    private lateinit var db: FirebaseFirestore
    private lateinit var itemsRef: CollectionReference
    private fun init(){
        db = FirebaseFirestore.getInstance()
        itemsRef = db.collection("Items")
    }

    private var cornerRadius: Int = 0
    private fun calculateCornerRadius(holder: RequestAdapterHome.RequestViewHolder){
        val scale = holder.itemView.context.resources.displayMetrics.density
        cornerRadius = (15.0f * scale + 0.5f).toInt()
    }
}