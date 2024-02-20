package com.project.inventorymanagementproject.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project.inventorymanagementproject.NavGraphDirections
import com.project.inventorymanagementproject.R
import com.project.inventorymanagementproject.classes.Product
import com.project.inventorymanagementproject.classes.User
import com.project.inventorymanagementproject.databinding.FragmentRemoveItemBinding
import com.project.inventorymanagementproject.databinding.FragmentRequestDetailsBinding
import com.project.inventorymanagementproject.databinding.FragmentRequestFormBinding
import com.project.inventorymanagementproject.objects.CurrentUser
import it.sephiroth.android.library.numberpicker.doOnProgressChanged

class RequestFormFragment : Fragment() {

    private lateinit var binding: FragmentRequestFormBinding
    private lateinit var navController: NavController
    private lateinit var currentUser: User
    private lateinit var selectedProduct: Product
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var itemsRef: CollectionReference
    private lateinit var requestsRef: CollectionReference
    private lateinit var itemMap: HashMap<String,Any>
    private lateinit var requestMap: HashMap<String,Any>


    //todo 0: Product State için -> 0: Beklemede 1: Kabul 2: Red

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRequestFormBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        getUser()
        getProduct()

        binding.makeRequestButton.setOnClickListener(View.OnClickListener {
            getFieldValues()
            createRequest()
        })
    }

    private fun init(view: View){
        navController = Navigation.findNavController(view)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        itemsRef = db.collection("Items")
        requestsRef = db.collection("Requests")
        itemMap = HashMap<String,Any>()
        requestMap = HashMap<String,Any>()
    }

    private fun getUser(){
        currentUser = CurrentUser.currentUser!!
        fillUserFields()
    }

    private fun fillUserFields(){
        binding.nameSurnameValueText.text = currentUser.getFirstName().plus(" ").plus(currentUser.getLastName())
    }

    private fun getProduct(){
        val bundle = this.arguments
        if (bundle != null) {
            selectedProduct = bundle.getParcelable<Product>("SelectedProduct")!!
            setProductFields()
        }
    }

    private fun setProductFields(){
        val requestCountNumberPicker = binding.requestCountNumberPicker
        val productStockCount = selectedProduct.getProductStockCount()

        requestCountNumberPicker.doOnProgressChanged { numberPicker, progress, formUser ->
            if(progress > productStockCount){
                numberPicker.progress = productStockCount
            }
        }
    }

    private fun createRequest(){
        requestsRef.add(requestMap).addOnSuccessListener(OnSuccessListener {
            itemMap["RequestCount"] = selectedProduct.getProductRequestCount() + binding.requestCountNumberPicker.progress

            itemsRef.document(selectedProduct.getProductBarcode()).set(itemMap, SetOptions.merge()).addOnSuccessListener(
                OnSuccessListener {
                    Toast.makeText(context, "Talep başarıyla gönderildi", Toast.LENGTH_SHORT).show()
                    returnSearch()
                }).addOnFailureListener(OnFailureListener {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            })
        }).addOnFailureListener(OnFailureListener {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun getFieldValues(){
        requestMap["RequestUrgency"] = binding.requestUrgencyNumberPicker.progress
        requestMap["RequestCount"] = binding.requestCountNumberPicker.progress
        requestMap["RequestDate"] = Timestamp.now()
        requestMap["RequestState"] = 0
        requestMap["UserId"] = auth.uid.toString()
        requestMap["ProductId"] = selectedProduct.getProductBarcode()
        requestMap["RequestDetail"] = binding.requestDetailsMultiText.text.toString()
    }

    private fun returnSearch(){
        val actionSearch = NavGraphDirections.actionGlobalSearchFragmentMenu()
        navController.navigate(actionSearch.actionId)
    }
}