package com.project.inventorymanagementproject.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.project.inventorymanagementproject.NavGraphDirections
import com.project.inventorymanagementproject.classes.Product
import com.project.inventorymanagementproject.classes.ProductAdapter
import com.project.inventorymanagementproject.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var productList: ArrayList<Product>
    private lateinit var productAdapter: ProductAdapter
    private lateinit var navController: NavController
    private lateinit var db: FirebaseFirestore
    private lateinit var itemsRef: CollectionReference
    private lateinit var searchView: androidx.appcompat.widget.SearchView
    private lateinit var productTags: ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        getItems()
        setCheckBoxes()

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    //Cancel returning splash screen
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private var adapterInitialized: Boolean = false
    private fun init(view: View){
        navController = Navigation.findNavController(view)
        db = FirebaseFirestore.getInstance()
        itemsRef = db.collection("Items")
        productList = ArrayList()
        productTags = ArrayList()
        adapterInitialized = false

        recyclerView = binding.productRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        searchView = binding.itemSearchView
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    searchItemWithSearchBar(query)
                    return true
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun getItems(){
        itemsRef.get()
            .addOnSuccessListener(OnSuccessListener {querySnapshot ->
                for(snapshot in querySnapshot!!.documents) {

                    val product: Product = Product(
                        snapshot.id,
                        snapshot.get("ProductTitle").toString(),
                        snapshot.get("ProductExplanation").toString(),
                        snapshot.get("ProductFeatures").toString(),
                        snapshot.get("StockCount").toString().toInt(),
                        snapshot.get("RequestCount").toString().toInt(),
                        snapshot.get("ProductImage").toString(),
                        snapshot.get("ProductTags") as ArrayList<String>
                    )

                    productList.add(product)
                }

                if(!adapterInitialized){
                    setAdapter()
                    adapterInitialized = true
                }else{
                    updateAdapter()
                }
            }).addOnFailureListener(OnFailureListener { exception->
                Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
            })
    }

    private fun setAdapter(){
        productAdapter = ProductAdapter(productList)
        recyclerView.adapter = productAdapter
    }

    private fun setCheckBoxes(){
        binding.officeCheckBox.setOnClickListener(View.OnClickListener {
            if(binding.officeCheckBox.isChecked){
                productList.clear()
                productTags.add("Office Supply")
            }
            else{
                productList.clear()
                productTags.remove("Office Supply")
            }
            searchItemWithCheckBox()
        })
        binding.computerComponentsCheckBox.setOnClickListener(View.OnClickListener {
            if(binding.computerComponentsCheckBox.isChecked){
                productList.clear()
                productTags.add("Computer Component")
            }
            else{
                productList.clear()
                productTags.remove("Computer Component")
            }
            searchItemWithCheckBox()
        })
        binding.clothingCheckBox.setOnClickListener(View.OnClickListener {
            if(binding.clothingCheckBox.isChecked){
                productList.clear()
                productTags.add("Clothing Supply")
            }
            else{
                productList.clear()
                productTags.remove("Clothing Supply")
            }
            searchItemWithCheckBox()
        })
        binding.licenseCheckBox.setOnClickListener(View.OnClickListener {
            if(binding.licenseCheckBox.isChecked){
                productList.clear()
                productTags.add("License")
            }
            else{
                productList.clear()
                productTags.remove("License")
            }
            searchItemWithCheckBox()
        })
        binding.electronicCheckBox.setOnClickListener(View.OnClickListener {
            if(binding.electronicCheckBox.isChecked){
                productList.clear()
                productTags.add("Electronic")
            }
            else{
                productList.clear()
                productTags.remove("Electronic")
            }
            searchItemWithCheckBox()
        })
        binding.computerCheckBox.setOnClickListener(View.OnClickListener {
            if(binding.computerCheckBox.isChecked){
                productList.clear()
                productTags.add("Computer")
            }
            else{
                productList.clear()
                productTags.remove("Computer")
            }
            searchItemWithCheckBox()
        })
        binding.furnitureCheckBox.setOnClickListener(View.OnClickListener {
            if(binding.furnitureCheckBox.isChecked){
                productList.clear()
                productTags.add("Furniture")
            }
            else{
                productList.clear()
                productTags.remove("Furniture")
            }
            searchItemWithCheckBox()
        })
        binding.kitchenToolsCheckBox.setOnClickListener(View.OnClickListener {
            if(binding.kitchenToolsCheckBox.isChecked){
                productList.clear()
                productTags.add("Kitchen Tool")
            }
            else{
                productList.clear()
                productTags.remove("Kitchen Tool")
            }
            searchItemWithCheckBox()
        })
        binding.decorCheckBox.setOnClickListener(View.OnClickListener {
            if(binding.decorCheckBox.isChecked){
                productList.clear()
                productTags.add("Decor")
            }
            else{
                productList.clear()
                productTags.remove("Decor")
            }
            searchItemWithCheckBox()
        })
    }

    private fun searchItemWithCheckBox(){
        if(productTags.isEmpty()){
            getItems()
        }else{
            for(tag in productTags){
                itemsRef.whereArrayContains("ProductTags", tag).get()
                    .addOnSuccessListener(OnSuccessListener {querySnapshot ->
                        for(snapshot in querySnapshot!!.documents) {

                            val product: Product = Product(
                                snapshot.id,
                                snapshot.get("ProductTitle").toString(),
                                snapshot.get("ProductExplanation").toString(),
                                snapshot.get("ProductFeatures").toString(),
                                snapshot.get("StockCount").toString().toInt(),
                                snapshot.get("RequestCount").toString().toInt(),
                                snapshot.get("ProductImage").toString(),
                                snapshot.get("ProductTags") as ArrayList<String>
                            )

                            if(!productList.contains(product)){
                                productList.add(product)
                            }
                        }

                        updateAdapter()
                    }).addOnFailureListener(OnFailureListener { exception->
                        Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                    })
            }
        }
    }

    private fun searchItemWithSearchBar(searchText: String){
        Toast.makeText(context, "Geçici olarak çevrimdışı", Toast.LENGTH_SHORT).show()
    }

    private fun updateAdapter(){
        recyclerView.adapter?.notifyDataSetChanged()
    }
}