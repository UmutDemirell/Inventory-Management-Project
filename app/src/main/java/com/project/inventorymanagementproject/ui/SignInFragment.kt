package com.project.inventorymanagementproject.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.project.inventorymanagementproject.classes.User
import com.project.inventorymanagementproject.databinding.FragmentSignInBinding
import com.project.inventorymanagementproject.interfaces.NavigationListener
import com.project.inventorymanagementproject.objects.CurrentUser

class SignInFragment : Fragment() {

    private lateinit var currentUser: User
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var binding: FragmentSignInBinding
    private lateinit var navigationListener: NavigationListener
    private val db = Firebase.firestore
    private val usersRef = db.collection("Users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentSignInBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        initRegex()
        loginEvents()

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    //Cancel returning splash screen
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun init(view: View){
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
    }

    private lateinit var regexEmail: Regex
    private fun initRegex(){
        regexEmail = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")
    }

    private fun loginEvents(){
        binding.forgotPasswordText.setOnClickListener(View.OnClickListener {
            val actionForgotPassword = SignInFragmentDirections.actionSignInFragmentToForgotPasswordFragment()
            navController.navigate(actionForgotPassword)
        })

        binding.loginButton.setOnClickListener{
            val email = binding.editTextEmailAddress.text.toString().trim()
            val pass = binding.editTextPassword.text.toString().trim()

            if(email.isNotEmpty() && pass.isNotEmpty()){
                if(regexEmail.matches(email)){
                    auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(
                        OnCompleteListener {
                            if(it.isSuccessful){
                                Toast.makeText(context, "Giriş Başarılı", Toast.LENGTH_SHORT).show()
                                Firebase.messaging.subscribeToTopic("Authenticated")
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.i("FCM", "Subscribed to a topic successfully")
                                        }else{
                                            Log.i("FCM", "Error while subscribing to a topic")
                                        }
                                        getUser()
                                    }
                            }else{
                                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                }else{
                    Toast.makeText(context, "Lütfen geçerli bir e-posta adresi giriniz", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getUser(){

        usersRef.document(auth.uid.toString()).get()
            .addOnSuccessListener(OnSuccessListener { document->

                currentUser = User(
                    document["FirstName"].toString(),
                    document["LastName"].toString(),
                    auth.currentUser!!.email.toString(),
                    document["Department"].toString(),
                    document["IsAdmin"] as Boolean,
                    document["UserProfilePicture"].toString()
                )

                CurrentUser.currentUser = currentUser
                val actionHome = SignInFragmentDirections.actionSignInFragmentToHomeFragment()

                openBottomNav()
                navController.navigate(actionHome)

            }).addOnFailureListener(OnFailureListener { exception->
                Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
            })
    }

    private fun openBottomNav(){
        navigationListener = context as NavigationListener
        navigationListener.openBottomNav()
    }
}