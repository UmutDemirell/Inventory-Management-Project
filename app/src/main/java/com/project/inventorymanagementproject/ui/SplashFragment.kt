package com.project.inventorymanagementproject.ui

import android.net.Uri
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.project.inventorymanagementproject.R
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.project.inventorymanagementproject.classes.User
import com.project.inventorymanagementproject.interfaces.NavigationListener
import com.project.inventorymanagementproject.objects.CurrentUser

class SplashFragment : Fragment() {

    private lateinit var navigationListener: NavigationListener
    private lateinit var navController: NavController
    private lateinit var currentUser: User
    private lateinit var auth:FirebaseAuth
    private val db = Firebase.firestore
    private val usersRef = db.collection("Users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    //Cancel returning splash screen
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        Handler(Looper.myLooper()!!).postDelayed(Runnable {
            if(auth.currentUser != null){
                getUser()
            }else{
                val actionSignIn = SplashFragmentDirections.actionSplashFragmentToSignInFragment()
                navController.navigate(actionSignIn)
            }
        }, 2000)
    }

    private fun init(view: View){
        auth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(view)
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
            val actionHome = SplashFragmentDirections.actionSplashFragmentToHomeFragment()

            openBottomNav()
            navController.navigate(actionHome)

        }).addOnFailureListener(OnFailureListener {exception->
            Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun openBottomNav(){
        navigationListener = context as NavigationListener
        navigationListener.openBottomNav()
    }
}