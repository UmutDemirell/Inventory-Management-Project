package com.project.inventorymanagementproject.classes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.project.inventorymanagementproject.NavGraphDirections
import com.project.inventorymanagementproject.R
import java.text.SimpleDateFormat

private const val EXPLANATION_CHAR_LENGTH = 100
private const val TITLE_CHAR_LENGTH = 50

class RequestAdapterAdmin(
    private val requestList: ArrayList<Request>
):RecyclerView.Adapter<RequestAdapterAdmin.RequestViewHolder>() {

    class RequestViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var productTitle: TextView = itemView.findViewById(R.id.card_title)
        var requestId: TextView = itemView.findViewById(R.id.card_id)
        var requestExplanation: TextView = itemView.findViewById(R.id.card_explanation)
        var requestDate: TextView = itemView.findViewById(R.id.card_date)
        var requestOwner: TextView = itemView.findViewById(R.id.card_ownerName)
        var cardView: CardView = itemView.findViewById(R.id.card_admin)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.request_card_admin, parent, false)
        return RequestAdapterAdmin.RequestViewHolder(view)
    }

    override fun getItemCount(): Int {
        return requestList.size
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requestList[position]
        calculateCornerRadius(holder)
        init()

        usersRef.document(request.getUserId()).get().addOnSuccessListener(OnSuccessListener { snapshot ->
            holder.requestOwner.text = " ".plus(snapshot.get("FirstName").toString().plus(" ").plus(snapshot.get("LastName").toString()))
        }).addOnFailureListener(OnFailureListener {
            Toast.makeText(holder.itemView.context, it.message, Toast.LENGTH_SHORT).show()
        })

        var cardProduct: Product

        itemsRef.document(request.getProductId()).get().addOnSuccessListener(OnSuccessListener { snapshot ->
            cardProduct = Product(
                productTitle = snapshot.get("ProductTitle").toString()
            )

            var requestDetail = request.getRequestDetail()
            var spaceIndex = 0

            if(requestDetail.length > EXPLANATION_CHAR_LENGTH){
                requestDetail = requestDetail.take(EXPLANATION_CHAR_LENGTH)
                spaceIndex = requestDetail.lastIndexOf(' ')
                holder.requestExplanation.text = requestDetail.take(spaceIndex).plus("...")
            }else{
                holder.requestExplanation.text = requestDetail
            }

            val dateFormat = SimpleDateFormat("dd-MM-yyyy")
            val requestDate = dateFormat.format(request.getRequestDate().toDate())

            var productTitle= cardProduct.getProductTitle()

            if(productTitle.length > TITLE_CHAR_LENGTH){
                productTitle = productTitle.take(TITLE_CHAR_LENGTH)
                spaceIndex = productTitle.lastIndexOf(' ')
                holder.productTitle.text = productTitle.take(spaceIndex).plus("...")
            }else{
                holder.productTitle.text = productTitle
            }

            holder.requestDate.text = " ".plus(requestDate)
            holder.requestId.text = request.getRequestId()

            holder.cardView.isVisible = true

            holder.itemView.setOnClickListener(View.OnClickListener {
                requestDetails(request, it)
            })

        }).addOnFailureListener(OnFailureListener {
            Toast.makeText(holder.itemView.context, it.message, Toast.LENGTH_SHORT).show()
        })
    }

        private lateinit var db: FirebaseFirestore
        private lateinit var itemsRef: CollectionReference
        private lateinit var usersRef: CollectionReference
        private fun init(){
            db = FirebaseFirestore.getInstance()
            itemsRef = db.collection("Items")
            usersRef = db.collection("Users")
        }

    private var cornerRadius: Int = 0
    private fun calculateCornerRadius(holder: RequestAdapterAdmin.RequestViewHolder){
        val scale = holder.itemView.context.resources.displayMetrics.density
        cornerRadius = (15.0f * scale + 0.5f).toInt()
    }

    private fun requestDetails(request: Request, view: View){
        val requestBundle = bundleOf("SelectedRequest" to request)
        val actionRequestDetails = NavGraphDirections.actionGlobalRequestDetailsFragment()
        view.findNavController().navigate(actionRequestDetails.actionId, requestBundle)
    }
}