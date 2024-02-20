package com.project.inventorymanagementproject.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.HorizontalScrollView
import android.widget.MultiAutoCompleteTextView
import android.widget.NumberPicker
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
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
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.project.inventorymanagementproject.NavGraphDirections
import com.project.inventorymanagementproject.R
import com.project.inventorymanagementproject.classes.User
import com.project.inventorymanagementproject.databinding.FragmentAddItemBinding
import com.project.inventorymanagementproject.databinding.FragmentCreateUserBinding
import com.project.inventorymanagementproject.databinding.FragmentProfileBinding
import com.project.inventorymanagementproject.interfaces.NavigationListener
import com.project.inventorymanagementproject.objects.CurrentUser

private const val CAMERA_REQUEST_CODE = 101

class AddItemFragment : Fragment() {

    private lateinit var binding: FragmentAddItemBinding
    private lateinit var productImageUri: Uri
    private lateinit var codeScanner: CodeScanner
    private lateinit var barcodeVal:String
    private lateinit var navController: NavController
    private lateinit var storageRef: StorageReference
    private lateinit var db: FirebaseFirestore
    private lateinit var itemsRef: CollectionReference
    private lateinit var notificationsRef: CollectionReference
    private lateinit var itemMap: HashMap<String,Any>
    private lateinit var productTags: ArrayList<String>
    private lateinit var notificationMap: HashMap<String,Any>
    private lateinit var checkBoxContainer: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddItemBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        setUpPermissions()
        codeScanner()

        binding.itemPhotoImageView.setOnClickListener(View.OnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        })

        binding.addItemButton.setOnClickListener(View.OnClickListener {
            if(binding.itemBarcodeValueText.text != "000000000000" && binding.itemTitleEditText.text.isNotEmpty() &&
               binding.itemFeaturesMultiText.text.isNotEmpty() && binding.itemExplanationMultiText.text.isNotEmpty()){
                takeValues()
                getCheckBoxes()
                setProductPhoto()
            }
        })
    }

    private var itemProductImageHasChanged: Boolean = false
    private var productRequestCount: Int = 0
    private var productStockCount: Int = 0
    private var cornerRadius: Int = 0

    private fun init(view: View){
        navController = Navigation.findNavController(view)
        checkBoxContainer = binding.checkBoxContainer
        storageRef = FirebaseStorage.getInstance().reference.child("Items")
        db = FirebaseFirestore.getInstance()
        itemsRef = db.collection("Items")
        notificationsRef = db.collection("Notifications")
        itemMap = HashMap<String,Any>()
        productTags = ArrayList<String>()
        notificationMap = HashMap<String,Any>()


        val scale = resources.displayMetrics.density
        cornerRadius = (15.0f * scale + 0.5f).toInt()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            productImageUri = data?.data!!

            Glide.with(this)
                .load(productImageUri)
                .transform(CenterCrop(), RoundedCorners(cornerRadius))
                .into(binding.itemPhotoImageView)

            itemProductImageHasChanged = true

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
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
                    Log.d("Item", "Found copy of item ${barcodeVal}")

                    itemProductImageHasChanged = false
                    clearForm()

                    productTags = document["ProductTags"] as ArrayList<String>
                    productStockCount = document["StockCount"].toString().toInt()
                    productRequestCount = document["RequestCount"].toString().toInt()

                    Glide.with(this)
                        .load(document["ProductImage"].toString())
                        .transform(CenterCrop(), RoundedCorners(cornerRadius))
                        .into(binding.itemPhotoImageView)

                    binding.itemTitleEditText.getText().insert( binding.itemTitleEditText.getSelectionStart(),
                        document["ProductTitle"].toString()
                    );
                    binding.itemExplanationMultiText.getText().insert( binding.itemExplanationMultiText.getSelectionStart(),
                        document["ProductExplanation"].toString()
                    );
                    binding.itemFeaturesMultiText.getText().insert( binding.itemFeaturesMultiText.getSelectionStart(),
                        document["ProductFeatures"].toString()
                    );

                    fillCheckBoxes()
                }else{
                    Log.w("Item", "No copy of item ${barcodeVal}")
                    clearForm()
                }
            }).addOnFailureListener(OnFailureListener { exception->
                Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
            })
    }

    private fun clearForm(){
        binding.itemTitleEditText.getText().clear()
        binding.itemFeaturesMultiText.getText().clear()
        binding.itemExplanationMultiText.getText().clear()

        for (i in 0 until checkBoxContainer.childCount){
            val checkBox = checkBoxContainer.getChildAt(i) as CheckBox
            checkBox.isChecked = false
        }
    }

    private fun takeValues(){
        itemMap["RequestCount"] = productRequestCount
        itemMap["ProductTitle"] = binding.itemTitleEditText.text.toString().trim()
        itemMap["ProductFeatures"] = binding.itemFeaturesMultiText.text.toString()
        itemMap["ProductExplanation"] = binding.itemExplanationMultiText.text.toString()
        itemMap["StockCount"] = productStockCount + binding.itemCountNumberPicker.progress
    }

    private fun setProductPhoto(){
        if(itemProductImageHasChanged){
            updatePhoto()
        }else{
            setProduct()
        }
    }

    private fun updatePhoto(){
        storageRef = storageRef.child(barcodeVal)
        storageRef.putFile(productImageUri).addOnCompleteListener(OnCompleteListener {
            if(it.isSuccessful){
                storageRef.downloadUrl.addOnSuccessListener(OnSuccessListener { uri->
                    itemMap["ProductImage"] = uri.toString()
                    setProduct()
                })
            }else{
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setProduct(){
        itemsRef.document(barcodeVal).set(itemMap, SetOptions.merge()).addOnSuccessListener(OnSuccessListener {
            Toast.makeText(context, "Ürün başarıyla kaydedildi", Toast.LENGTH_SHORT).show()
            generateNotification()
            saveNotification()
        }).addOnFailureListener(OnFailureListener {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun generateNotification(){
        notificationMap["NotificationMessage"] = binding.itemCountNumberPicker.progress.toString()
            .plus(" adet ").plus(binding.itemTitleEditText.text.toString()).plus(" stoklara eklendi.")
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

    private fun fillCheckBoxes(){
        if(productTags.contains("Office Supply")){
            binding.officeCheckBox.isChecked = true
        }
        if(productTags.contains("Computer Component")){
            binding.computerComponentsCheckBox.isChecked = true
        }
        if(productTags.contains("Clothing Supply")){
            binding.clothingCheckBox.isChecked = true
        }
        if(productTags.contains("License")){
            binding.licenseCheckBox.isChecked = true
        }
        if(productTags.contains("Electronic")){
            binding.electronicCheckBox.isChecked = true
        }
        if(productTags.contains("Computer")){
            binding.computerCheckBox.isChecked = true
        }
        if(productTags.contains("Furniture")){
            binding.furnitureCheckBox.isChecked = true
        }
        if(productTags.contains("Kitchen Tool")){
            binding.kitchenToolsCheckBox.isChecked = true
        }
        if(productTags.contains("Decor")){
            binding.decorCheckBox.isChecked = true
        }
    }

    private fun getCheckBoxes(){
        productTags = ArrayList<String>()

        if(binding.officeCheckBox.isChecked){
            productTags.add("Office Supply")
        }
        if(binding.computerComponentsCheckBox.isChecked){
            productTags.add("Computer Component")
        }
        if(binding.clothingCheckBox.isChecked){
            productTags.add("Clothing Supply")
        }
        if(binding.licenseCheckBox.isChecked){
            productTags.add("License")
        }
        if(binding.electronicCheckBox.isChecked){
            productTags.add("Electronic")
        }
        if(binding.computerCheckBox.isChecked){
            productTags.add("Computer")
        }
        if(binding.furnitureCheckBox.isChecked){
            productTags.add("Furniture")
        }
        if(binding.kitchenToolsCheckBox.isChecked){
            productTags.add("Kitchen Tool")
        }
        if(binding.decorCheckBox.isChecked){
            productTags.add("Decor")
        }

        itemMap["ProductTags"] = productTags
    }
}