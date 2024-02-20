package com.project.inventorymanagementproject.ui

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.project.inventorymanagementproject.NavGraphDirections
import com.project.inventorymanagementproject.classes.Product
import com.project.inventorymanagementproject.classes.ProductAdapter
import com.project.inventorymanagementproject.classes.Request
import com.project.inventorymanagementproject.classes.RequestAdapterAdmin
import com.project.inventorymanagementproject.classes.RequestAdapterUser
import com.project.inventorymanagementproject.classes.User
import com.project.inventorymanagementproject.databinding.FragmentHistoryBinding
import com.project.inventorymanagementproject.databinding.FragmentSearchBinding
import com.project.inventorymanagementproject.objects.CurrentUser

class HistoryFragment : Fragment() {

    private lateinit var currentUser: User
    private lateinit var binding: FragmentHistoryBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var requestAdapterUser: RequestAdapterUser
    private lateinit var requestAdapterAdmin: RequestAdapterAdmin
    private lateinit var navController: NavController
    private lateinit var db: FirebaseFirestore
    private lateinit var requestsRef: CollectionReference
    private lateinit var requestList: ArrayList<Request>
    private lateinit var waitingRequestList: ArrayList<Request>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHistoryBinding.inflate(inflater,container,false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        getUser()

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    //Cancel returning splash screen
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        if(currentUser.getIsAdmin()){
            binding.historyTitle.text = "Gelen Talepler"
            getRequestsAdmin()
        }else{
            binding.historyTitle.text = "Talep Geçmişi"
            getRequestsUser()
        }
    }

    private fun init(view: View){
        navController = Navigation.findNavController(view)
        db = FirebaseFirestore.getInstance()
        requestsRef = db.collection("Requests")
        auth = FirebaseAuth.getInstance()
        requestList = ArrayList()
        waitingRequestList = ArrayList()

        recyclerView = binding.requestRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun getUser(){
        currentUser = CurrentUser.currentUser!!
    }

    private fun getRequestsUser(){
        requestsRef.whereEqualTo("UserId", auth.uid).get()
            .addOnSuccessListener(OnSuccessListener {querySnapshot ->
                for(snapshot in querySnapshot!!.documents) {

                    val request: Request = Request(
                        requestId = snapshot.id,
                        productId = snapshot.get("ProductId").toString(),
                        userId = snapshot.get("UserId").toString(),
                        requestDetail = snapshot.get("RequestDetail").toString(),
                        requestState = snapshot.get("RequestState").toString().toInt(),
                        requestDate = snapshot.get("RequestDate") as Timestamp
                    )

                    if(request.getRequestState() == 0){
                        waitingRequestList.add(request)
                    }else{
                        requestList.add(request)
                    }
                }

                waitingRequestList.sortByDescending { it.getRequestDate() }
                requestList.sortByDescending { it.getRequestDate() }
                waitingRequestList.addAll(requestList)

                setAdapterUser()
        }).addOnFailureListener(OnFailureListener {exception->
                Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
        })
    }

    private fun setAdapterUser(){
        requestAdapterUser = RequestAdapterUser(waitingRequestList)
        recyclerView.adapter = requestAdapterUser
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getRequestsAdmin(){
        requestsRef.whereEqualTo("RequestState", 0).get()
            .addOnSuccessListener(OnSuccessListener {querySnapshot ->
                for(snapshot in querySnapshot!!.documents) {

                    val request: Request = Request(
                        snapshot.id,
                        snapshot.get("ProductId").toString(),
                        snapshot.get("UserId").toString(),
                        snapshot.get("RequestDetail").toString(),
                        snapshot.get("RequestUrgency").toString().toInt(),
                        snapshot.get("RequestCount").toString().toInt(),
                        snapshot.get("RequestState").toString().toInt(),
                        snapshot.get("RequestDate") as Timestamp
                    )

                    request.calculateRequestImportance()
                    requestList.add(request)
                }

                requestList.sortByDescending { it.getRequestImportance() }
                setAdapterAdmin()
            }).addOnFailureListener(OnFailureListener {exception->
                Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
            })
    }

    private fun setAdapterAdmin(){
        requestAdapterAdmin = RequestAdapterAdmin(requestList)
        recyclerView.adapter = requestAdapterAdmin
    }
}