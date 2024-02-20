package com.project.inventorymanagementproject.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Resources.getSystem
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.project.inventorymanagementproject.NavGraphDirections
import com.project.inventorymanagementproject.R
import com.project.inventorymanagementproject.classes.User
import com.project.inventorymanagementproject.databinding.FragmentProfileBinding
import com.project.inventorymanagementproject.interfaces.NavigationListener
import com.project.inventorymanagementproject.objects.CurrentUser

class ProfileFragment : Fragment() {

    private lateinit var currentUser: User
    private lateinit var auth: FirebaseAuth
    private lateinit var userImageUri: Uri
    private lateinit var navController: NavController
    private lateinit var binding: FragmentProfileBinding
    private lateinit var navigationListener: NavigationListener
    private lateinit var storageRef: StorageReference
    private lateinit var db: FirebaseFirestore
    private lateinit var usersRef: CollectionReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        getUser()
        fillUserInformation()

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    //Cancel returning splash screen
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.logoutButton.setOnClickListener(View.OnClickListener {
            logoutEvents()
        })

        binding.profilePicture.setOnClickListener(View.OnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        })
    }

    private var cornerRadius: Int = 0
    private fun init(view: View){
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference.child("Users")
        db = FirebaseFirestore.getInstance()
        usersRef = db.collection("Users")

        val scale = resources.displayMetrics.density
        cornerRadius = (15.0f * scale + 0.5f).toInt()
    }

    private fun logoutEvents(){

        Firebase.messaging.unsubscribeFromTopic("Authenticated")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("FCM", "Unsubscribed to a topic successfully")
                    getUser()
                }else{
                    Log.i("FCM", "Error while unsubscribing to a topic")
                }

                auth.signOut()
                Toast.makeText(context, "Çıkış Başarılı", Toast.LENGTH_SHORT).show()

                closeBottomNav()

                val actionLogin = NavGraphDirections.actionGlobalSplashFragment()
                navController.navigate(actionLogin.actionId)
            }
    }

    private fun closeBottomNav(){
        navigationListener = context as NavigationListener
        navigationListener.closeBottomNav()
    }

    private fun getUser(){
        currentUser = CurrentUser.currentUser!!
    }

    @SuppressLint("SetTextI18n")
    private fun fillUserInformation(){
        binding.userFullName.text = currentUser.getFirstName() + " " + currentUser.getLastName()
        binding.userEmailValue.text = currentUser.getEmail()
        binding.userDepartmentValue.text = currentUser.getDepartment()

        if (currentUser.getUserProfilePicture() != "null"){
            Glide.with(this)
                .load(currentUser.getUserProfilePicture())
                .transform(CenterCrop(), RoundedCorners(cornerRadius))
                .into(binding.profilePicture)
        }
        else{
            val defaultProfilePicture: Drawable? =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_person_24, null)

            Glide.with(this)
                .load(defaultProfilePicture)
                .transform(CenterCrop(), RoundedCorners(cornerRadius))
                .into(binding.profilePicture)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            userImageUri = data?.data!!
            uploadImage()

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }

    }

    private fun uploadImage(){
        storageRef = storageRef.child(auth.uid.toString())
        storageRef.putFile(userImageUri).addOnCompleteListener(OnCompleteListener {
            if(it.isSuccessful){
                storageRef.downloadUrl.addOnSuccessListener(OnSuccessListener { uri->
                    val uriMap = HashMap<String,Any>()
                    uriMap["UserProfilePicture"] = uri.toString()

                    usersRef.document(auth.uid.toString()).update(uriMap).addOnCompleteListener(
                        OnCompleteListener {firestoreTask ->
                            currentUser.setUserProfilePicture(uri.toString())
                            if(firestoreTask.isSuccessful){
                                Glide.with(this)
                                    .load(currentUser.getUserProfilePicture())
                                    .transform(CenterCrop(), RoundedCorners(cornerRadius))
                                    .into(binding.profilePicture)
                                Toast.makeText(context, "Fotoğraf Başarıyla Yüklendi", Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
                            }
                        })
                })
            }else{
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}