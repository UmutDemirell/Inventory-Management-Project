package com.project.inventorymanagementproject.ui

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
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
import com.google.firebase.storage.StorageReference
import com.project.inventorymanagementproject.NavGraphDirections
import com.project.inventorymanagementproject.R
import com.project.inventorymanagementproject.databinding.FragmentAddItemBinding
import com.project.inventorymanagementproject.databinding.FragmentRemoveItemBinding
import it.sephiroth.android.library.numberpicker.NumberPicker
import it.sephiroth.android.library.numberpicker.doOnProgressChanged

private const val CAMERA_REQUEST_CODE = 101

class RemoveItemFragment : Fragment() {

    private lateinit var binding: FragmentRemoveItemBinding
    private lateinit var codeScanner: CodeScanner
    private lateinit var numberPicker: NumberPicker
    private lateinit var barcodeVal:String
    private lateinit var navController: NavController
    private lateinit var storageRef: StorageReference
    private lateinit var db: FirebaseFirestore
    private lateinit var itemsRef: CollectionReference
    private lateinit var notificationsRef: CollectionReference
    private lateinit var itemMap: HashMap<String,Any>
    private lateinit var notificationMap: HashMap<String, Any>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRemoveItemBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        setUpPermissions()
        codeScanner()

        binding.removeItemButton.setOnClickListener(View.OnClickListener {
            if(binding.itemBarcodeValueText.text != "000000000000" && isItemFound){
                removeItem()
            }
        })

        numberPicker.doOnProgressChanged { numberPicker, progress, formUser ->
            if(isItemFound){
                if(progress > productStockCount){
                    numberPicker.progress = productStockCount
                }
            }
        }
    }

    private var isItemFound: Boolean = false
    private var productStockCount: Int = 0
    private var cornerRadius: Int = 0

    private fun init(view: View){
        navController = Navigation.findNavController(view)
        storageRef = FirebaseStorage.getInstance().reference.child("Items")
        db = FirebaseFirestore.getInstance()
        itemsRef = db.collection("Items")
        notificationsRef = db.collection("Notifications")
        numberPicker = binding.itemCountNumberPicker
        itemMap = HashMap<String,Any>()
        notificationMap = HashMap<String,Any>()

        val scale = resources.displayMetrics.density
        cornerRadius = (15.0f * scale + 0.5f).toInt()
    }

    private fun codeScanner(){
        codeScanner = CodeScanner(this.requireActivity(), binding.barcodeScannerView)

        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ONE_DIMENSIONAL_FORMATS

            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                activity?.runOnUiThread {
                    barcodeVal = it.text
                    binding.itemBarcodeValueText.text = barcodeVal
                    getItem()
                }
            }

            errorCallback = ErrorCallback {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Kamera başlatılamadı", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.barcodeScannerView.setOnClickListener(View.OnClickListener {
            codeScanner.startPreview()
        })
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun setUpPermissions(){
        val permission = ContextCompat.checkSelfPermission(this.requireContext(), android.Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED){
            makeRequest()
        }
    }

    private fun makeRequest(){
        ActivityCompat.requestPermissions(this.requireActivity(), arrayOf(android.Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            CAMERA_REQUEST_CODE->{
                if(grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(context, "Kamera izni gerekli", Toast.LENGTH_SHORT).show()
                }else{
                    //successful
                }
            }
        }
    }

    private fun getItem(){

        itemsRef.document(barcodeVal.toString()).get()
            .addOnSuccessListener(OnSuccessListener { document->
                if(document.exists()){

                    Glide.with(this)
                        .load(document["ProductImage"].toString())
                        .transform(CenterCrop(), RoundedCorners(cornerRadius))
                        .into(binding.itemPhotoImageView)

                    binding.itemTitleValueText.text = document["ProductTitle"].toString()
                    productStockCount = document["StockCount"].toString().toInt()

                    if(productStockCount > 0){
                        isItemFound = true
                    }else{
                        Toast.makeText(context, "Ürün stokta bulunmamakta", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(context, "Bu barkoda sahip ürün bulunamadı", Toast.LENGTH_SHORT).show()
                    isItemFound = false
                }
            }).addOnFailureListener(OnFailureListener { exception->
                Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
            })
    }

    private fun removeItem(){
        val currentNumber = productStockCount - numberPicker.progress
        itemMap["StockCount"] = currentNumber

        itemsRef.document(barcodeVal.toString()).set(itemMap, SetOptions.merge()).addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(context, "Ürün başarıyla kaldırıldı", Toast.LENGTH_SHORT).show()

                if(currentNumber == 0){
                    generateNotification()
                    saveNotification()
                }else{
                    returnHome()
                }
            }else{
                Toast.makeText(context, it.exception!!.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateNotification(){
        notificationMap["NotificationMessage"] = "Tüm ".plus(binding.itemTitleValueText.text).plus(" ürünleri stoktan kaldırıldı")
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
                returnHome()
            })
    }

    private fun returnHome(){
        val actionHome = NavGraphDirections.actionGlobalHomeFragmentMenu()
        navController.navigate(actionHome.actionId)
    }
}