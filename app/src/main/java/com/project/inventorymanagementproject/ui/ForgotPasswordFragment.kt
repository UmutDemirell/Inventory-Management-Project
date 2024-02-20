package com.project.inventorymanagementproject.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.project.inventorymanagementproject.R
import com.project.inventorymanagementproject.classes.User
import com.project.inventorymanagementproject.databinding.FragmentForgotPasswordBinding
import com.project.inventorymanagementproject.databinding.FragmentHistoryBinding
import com.project.inventorymanagementproject.databinding.FragmentSignInBinding
import com.project.inventorymanagementproject.interfaces.NavigationListener

class ForgotPasswordFragment : Fragment() {

    private lateinit var binding: FragmentForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentForgotPasswordBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        initRegex()

        binding.resetPasswordButton.setOnClickListener(View.OnClickListener {
            resetPassword()
        })
    }

    private fun init(view: View){
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
    }

    private lateinit var regexEmail: Regex
    private fun initRegex(){
        regexEmail = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")
    }

    private fun resetPassword(){
        val email = binding.editTextEmailAddress.text.toString()
        if(email.isNotEmpty()){
            if(regexEmail.matches(email)){
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(OnSuccessListener {
                        Toast.makeText(context, "Sıfırlama maili gönderildi", Toast.LENGTH_SHORT).show()
                        returnSignIn()
                }).addOnFailureListener(OnFailureListener {
                        if(it.message.equals("There is no user record corresponding to this identifier. The user may have been deleted.")){
                            Toast.makeText(context, "Sıfırlama maili gönderildi", Toast.LENGTH_SHORT).show()
                            returnSignIn()
                        }else{
                            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        }
                    })
            }else{
                Toast.makeText(context, "Lütfen geçerli bir e-posta adresi giriniz", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
        }
    }

    private fun returnSignIn(){
        val actionSignIn = ForgotPasswordFragmentDirections.actionForgotPasswordFragmentToSignInFragment()
        navController.navigate(actionSignIn)
    }
}