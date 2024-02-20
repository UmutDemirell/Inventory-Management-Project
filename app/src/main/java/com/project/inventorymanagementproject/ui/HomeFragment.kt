package com.project.inventorymanagementproject.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.project.inventorymanagementproject.classes.Request
import com.project.inventorymanagementproject.classes.RequestAdapterHome
import com.project.inventorymanagementproject.classes.RequestAdapterUser
import com.project.inventorymanagementproject.classes.User
import com.project.inventorymanagementproject.databinding.FragmentHomeBinding
import com.project.inventorymanagementproject.objects.CurrentUser


class HomeFragment : Fragment() {

    private lateinit var currentUser: User
    private lateinit var binding: FragmentHomeBinding
    private lateinit var navController: NavController
    private lateinit var requestAdapterHome: RequestAdapterHome
    private lateinit var recyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var requestsRef: CollectionReference
    private lateinit var waitingRequestList: ArrayList<Request>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        getUser()
        prepareScreen()

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    //Cancel returning splash screen
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        binding.notifications.setOnClickListener(View.OnClickListener {
            val actionNotification = HomeFragmentDirections.actionHomeFragmentToNotificationFragment()
            navController.navigate(actionNotification.actionId)
        })

        binding.createUserButton.setOnClickListener(View.OnClickListener {
            val actionCreateUser = HomeFragmentDirections.actionHomeFragmentMenuToCreateUserFragment()
            navController.navigate(actionCreateUser.actionId)
        })

        binding.addItemButton.setOnClickListener(View.OnClickListener {
            val actionAddItem = HomeFragmentDirections.actionHomeFragmentMenuToAddItemFragment()
            navController.navigate(actionAddItem.actionId)
        })

        binding.removeItemButton.setOnClickListener(View.OnClickListener {
            val actionRemoveItem = HomeFragmentDirections.actionHomeFragmentMenuToRemoveItemFragment()
            navController.navigate(actionRemoveItem.actionId)
        })
    }

    private fun init(view: View){
        navController = Navigation.findNavController(view)
        db = FirebaseFirestore.getInstance()
        requestsRef = db.collection("Requests")
        auth = FirebaseAuth.getInstance()
        waitingRequestList = ArrayList()

        recyclerView = binding.requestHomeRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun prepareScreen(){
        val welcomeText = binding.welcomeText.text.toString() + " " + currentUser.getFirstName()
        binding.welcomeText.text = welcomeText

        if(currentUser.getIsAdmin()){
            binding.createUserButton.isVisible = true
            binding.addItemButton.isVisible = true
            binding.removeItemButton.isVisible = true
        }else{
            binding.homeRequestText.isVisible = true
            binding.homeRequestCard.isVisible = true
            getRequestsHome()
        }
    }

    private fun getUser(){
        currentUser = CurrentUser.currentUser!!
    }

    private fun getRequestsHome(){
        requestsRef.whereEqualTo("UserId", auth.uid).whereEqualTo("RequestState", 0).get()
            .addOnSuccessListener(OnSuccessListener { querySnapshot ->
                for(snapshot in querySnapshot!!.documents) {

                    val request: Request = Request(
                        requestId = snapshot.id,
                        productId = snapshot.get("ProductId").toString(),
                        userId = snapshot.get("UserId").toString(),
                        requestDate = snapshot.get("RequestDate") as Timestamp
                    )

                    waitingRequestList.add(request)
                }

                waitingRequestList.sortByDescending { it.getRequestDate() }
                setAdapterHome()
            }).addOnFailureListener(OnFailureListener { exception->
                Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
            })
    }

    private fun setAdapterHome(){
        requestAdapterHome = RequestAdapterHome(waitingRequestList)
        recyclerView.adapter = requestAdapterHome
    }
}