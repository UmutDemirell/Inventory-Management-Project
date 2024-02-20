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
import com.google.android.gms.common.internal.Objects
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.ActionCodeEmailInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project.inventorymanagementproject.NavGraphDirections
import com.project.inventorymanagementproject.R
import com.project.inventorymanagementproject.databinding.FragmentCreateUserBinding
import com.project.inventorymanagementproject.databinding.FragmentNotificationBinding
import kotlin.random.Random

class CreateUserFragment : Fragment() {

    private lateinit var binding: FragmentCreateUserBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var password: String
    private val db = Firebase.firestore
    private val usersRef = db.collection("Users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCreateUserBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        initRegex()

        password = getRandPassword(8)
        binding.createdPasswordText.text = password

        binding.createUserButton.setOnClickListener(View.OnClickListener {
            createUser()
        })
    }

    private fun init(view: View){
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
    }

    private lateinit var regexEmail: Regex
    private lateinit var regexText: Regex
    private lateinit var regexTextOptionalSpace: Regex
    private fun initRegex(){
        regexEmail = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")
        regexText = Regex("^[a-zA-ZğüşöçıİĞÜŞÖÇ]+\$")
        regexTextOptionalSpace = Regex("^[a-zA-ZğüşöçıİĞÜŞÖÇ\\s*]+\$")
    }

    private lateinit var email: String
    private lateinit var firstName: String
    private lateinit var lastName: String
    private lateinit var department: String
    private fun createUser(){
        val isAdmin = binding.adminCheckBox.isChecked
        email = binding.emailEditText.text.toString()
        firstName = binding.firstNameEditText.text.toString()
        lastName = binding.lastNameEditText.text.toString()
        department = binding.departmentEditText.text.toString()

        if(email.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty() && department.isNotEmpty()){
            if(validateFields()){
                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(
                    OnCompleteListener {
                        if(!it.isSuccessful){
                            Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
                        }else{
                            val createdUid = it.result.user!!.uid

                            val firestoreUser = mapOf("FirstName" to firstName, "LastName" to lastName,
                                "Department" to department, "IsAdmin" to isAdmin)

                            usersRef.document(createdUid)
                                .set(firestoreUser)
                                .addOnSuccessListener(OnSuccessListener {
                                    Toast.makeText(context, "Kullanıcı Oluşturuldu", Toast.LENGTH_SHORT).show()
                                    val actionHome = NavGraphDirections.actionGlobalHomeFragmentMenu()
                                    navController.navigate(actionHome.actionId)
                                })
                                .addOnFailureListener(OnFailureListener {
                                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                                })
                        }
                    })
            }
        }else{
            Toast.makeText(context, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateFields(): Boolean{
        if(!regexText.matches(firstName)){
            Toast.makeText(context, "Lütfen geçerli bir isim giriniz", Toast.LENGTH_SHORT).show()
            return false
        }
        if(!regexText.matches(lastName)){
            Toast.makeText(context, "Lütfen geçerli bir soyisim giriniz", Toast.LENGTH_SHORT).show()
            return false
        }
        if(!regexEmail.matches(email)){
            Toast.makeText(context, "Lütfen geçerli bir e-posta adresi giriniz", Toast.LENGTH_SHORT).show()
            return false
        }
        if(!regexTextOptionalSpace.matches(department)){
            Toast.makeText(context, "Lütfen geçerli bir departman ismi giriniz", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    fun getRandPassword(n: Int): String
    {
        val characterSet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

        val random = Random(System.nanoTime())
        val password = StringBuilder()

        for (i in 0 until n)
        {
            val rIndex = random.nextInt(characterSet.length)
            password.append(characterSet[rIndex])
        }

        return password.toString()
    }
}