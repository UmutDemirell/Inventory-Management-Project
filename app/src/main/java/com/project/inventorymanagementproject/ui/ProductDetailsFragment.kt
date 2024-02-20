package com.project.inventorymanagementproject.ui

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.project.inventorymanagementproject.NavGraphDirections
import com.project.inventorymanagementproject.classes.Product
import com.project.inventorymanagementproject.databinding.FragmentProductDetailsBinding


class ProductDetailsFragment : Fragment() {

    private lateinit var binding: FragmentProductDetailsBinding
    private lateinit var navController: NavController
    private lateinit var selectedProduct: Product

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProductDetailsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        getProduct()
    }

    private var cornerRadius: Int = 0
    private fun init(view: View){
        navController = Navigation.findNavController(view)

        val scale = resources.displayMetrics.density
        cornerRadius = (15.0f * scale + 0.5f).toInt()
    }

    private fun getProduct(){
        val bundle = this.arguments
        if (bundle != null) {
            selectedProduct = bundle.getParcelable<Product>("SelectedProduct")!!
            setFields()
            setPicture()
            setMargins()
            setNavigation()
        }
    }

    private fun setFields(){
        binding.productTitle.text = selectedProduct.getProductTitle()
        binding.productFeaturesValue.text = selectedProduct.getProductFeatures()
        binding.productExplanationValue.text = selectedProduct.getProductExplanation()
        binding.productStockValue.text = "Stok: ".plus(selectedProduct.getProductStockCount())
    }

    private fun setPicture(){
        Glide.with(this)
            .load(selectedProduct.getProductImage())
            .transform(CenterCrop(), RoundedCorners(cornerRadius))
            .into(binding.productImageView)
    }

    private fun setMargins(){
        requireView().viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val scale = resources.displayMetrics.density
                val cardHeight = (550.0f * scale + 0.5f).toInt()

                view!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if(binding.innerLayout.height < cardHeight){
                    val differenceStock = (20.0f * scale + 0.5f).toInt() + (cardHeight - binding.innerLayout.height)
                    val differenceButton = (25.0f * scale + 0.5f).toInt() + (cardHeight - binding.innerLayout.height)

                    val stockParam = binding.productStockValue.layoutParams as ViewGroup.MarginLayoutParams
                    stockParam.setMargins((15.0f * scale + 0.5f).toInt(),differenceStock,0,(5.0f * scale + 0.5f).toInt())
                    binding.productStockValue.layoutParams = stockParam

                    val buttonParam = binding.productDetailsButton.layoutParams as ViewGroup.MarginLayoutParams
                    buttonParam.setMargins(0,differenceButton,(15.0f * scale + 0.5f).toInt(),(10.0f * scale + 0.5f).toInt())
                    binding.productDetailsButton.layoutParams = buttonParam
                }
            }
        })
    }   

    private fun setNavigation(){
        binding.productDetailsButton.setOnClickListener(View.OnClickListener {
            if(selectedProduct.getProductStockCount() > 0){
                val productBundle = bundleOf("SelectedProduct" to selectedProduct)
                val actionRequest= NavGraphDirections.actionGlobalRequestFormFragment()
                navController.navigate(actionRequest.actionId, productBundle)
            }
            else{
                Toast.makeText(context, "Stokta ürün bulunmamakta", Toast.LENGTH_SHORT).show()
            }
        })
    }
}