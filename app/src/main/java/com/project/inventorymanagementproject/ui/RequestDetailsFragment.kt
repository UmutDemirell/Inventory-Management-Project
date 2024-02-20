package com.project.inventorymanagementproject.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.project.inventorymanagementproject.NavGraphDirections
import com.project.inventorymanagementproject.R
import com.project.inventorymanagementproject.classes.Product
import com.project.inventorymanagementproject.classes.Request
import com.project.inventorymanagementproject.classes.User
import com.project.inventorymanagementproject.databinding.FragmentProfileBinding
import com.project.inventorymanagementproject.databinding.FragmentRequestDetailsBinding
import com.project.inventorymanagementproject.objects.CurrentUser

class RequestDetailsFragment : Fragment() {

    private lateinit var binding: FragmentRequestDetailsBinding
    private lateinit var navController: NavController
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUser: User
    private lateinit var requestsRef: CollectionReference
    private lateinit var itemsRef: CollectionReference
    private lateinit var notificationsRef: CollectionReference
    private lateinit var selectedRequest: Request
    private lateinit var requestMap: HashMap<String,Any>
    private lateinit var productMap: HashMap<String,Any>
    private lateinit var notificationMap: HashMap<String, Any>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRequestDetailsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        getUser()
        getRequest()
    }

    private var productName: String = ""
    private var cornerRadius: Int = 0
    private var requestRequestCount: Int = 0
    private var productRequestCount: Int = 0
    private var stockCount: Int = 0
    private fun init(view: View){
        navController = Navigation.findNavController(view)
        db = FirebaseFirestore.getInstance()
        requestsRef = db.collection("Requests")
        itemsRef = db.collection("Items")
        notificationsRef = db.collection("Notifications")
        requestMap = HashMap<String,Any>()
        productMap = HashMap<String,Any>()
        notificationMap = HashMap<String,Any>()

        val scale = resources.displayMetrics.density
        cornerRadius = (15.0f * scale + 0.5f).toInt()
    }

    private fun getRequest(){
        val bundle = this.arguments
        if (bundle != null) {
            selectedRequest = bundle.getParcelable<Request>("SelectedRequest")!!

            setFields()
        }
    }

    private fun setFields(){

        itemsRef.document(selectedRequest.getProductId()).get().addOnSuccessListener(
            OnSuccessListener { document->
                Glide.with(this)
                    .load(document["ProductImage"])
                    .transform(CenterCrop(), RoundedCorners(cornerRadius))
                    .into(binding.productImageView)

                productName = document["ProductTitle"].toString()
                binding.productTitle.text = productName

                stockCount = document["StockCount"].toString().toInt()
                requestRequestCount = selectedRequest.getRequestCount()
                productRequestCount= document["RequestCount"].toString().toInt()
                binding.productStockCountValueText.text = document["StockCount"].toString()

                binding.requestCountValueText.text = selectedRequest.getRequestCount().toString()
                binding.requestUrgencyValueText.text = selectedRequest.getRequestUrgency().toString()
                binding.requestExplanation.text = selectedRequest.getRequestDetail()
                binding.requestOwner.text = currentUser.getFirstName().plus(" ").plus(currentUser.getLastName())

                binding.acceptRequestButton.setOnClickListener(View.OnClickListener {
                    if(stockCount >= requestRequestCount){
                        acceptRequest()
                    }else{
                        Toast.makeText(context, "Stokta yeterli ürün bulunmamakta", Toast.LENGTH_SHORT).show()
                    }
                })

                binding.rejectRequestButton.setOnClickListener(View.OnClickListener {
                    rejectRequest()
                })

            }).addOnFailureListener(OnFailureListener {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun acceptRequest(){
        requestMap["RequestState"] = 1

        requestsRef.document(selectedRequest.getRequestId()).set(requestMap, SetOptions.merge()).addOnSuccessListener(
            OnSuccessListener {
                stockCount -= requestRequestCount
                productMap["StockCount"] = stockCount
                productRequestCount -= requestRequestCount
                productMap["RequestCount"] = productRequestCount

                itemsRef.document(selectedRequest.getProductId()).set(productMap, SetOptions.merge()).addOnSuccessListener(
                    OnSuccessListener {
                        Toast.makeText(context, "Talep başarıyla kabul edildi", Toast.LENGTH_SHORT).show()

                        if(stockCount == 0){
                            generateNotification()
                            saveNotification()
                        }else{
                            returnHistory()
                        }
                    }).addOnFailureListener(OnFailureListener {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                })
            }).addOnFailureListener(OnFailureListener {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun rejectRequest(){
        requestMap["RequestState"] = 2

        requestsRef.document(selectedRequest.getRequestId()).set(requestMap, SetOptions.merge()).addOnSuccessListener(
            OnSuccessListener {
                productRequestCount -= requestRequestCount
                productMap["RequestCount"] = productRequestCount

                itemsRef.document(selectedRequest.getProductId()).set(productMap, SetOptions.merge()).addOnSuccessListener(
                    OnSuccessListener {
                        Toast.makeText(context, "Talep başarıyla reddedildi", Toast.LENGTH_SHORT).show()
                        returnHistory()
                    }).addOnFailureListener(OnFailureListener {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                })

            }).addOnFailureListener(OnFailureListener {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun generateNotification(){
        notificationMap["NotificationMessage"] = productName.plus("stokları tükendi")
        notificationMap["NotificationDate"] = Timestamp.now()
    }

    @SuppressLint("LogNotTimber")
    private fun saveNotification(){
        notificationsRef.add(notificationMap)
            .addOnCompleteListener(OnCompleteListener {
                if(it.isSuccessful){
                    Log.i("Notification", "Notification Saved Successfully")
                }else{
                    Log.i("Notification", "${it.exception!!.message}")
                }
                returnHistory()
            })
    }

    private fun getUser(){
        currentUser = CurrentUser.currentUser!!
    }

    private fun returnHistory(){
        val actionHistory = NavGraphDirections.actionGlobalHistoryFragmentMenu()
        navController.navigate(actionHistory.actionId)
    }
}