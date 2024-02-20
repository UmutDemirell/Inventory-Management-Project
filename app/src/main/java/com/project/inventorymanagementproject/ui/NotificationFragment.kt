package com.project.inventorymanagementproject.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.project.inventorymanagementproject.NavGraphDirections
import com.project.inventorymanagementproject.R
import com.project.inventorymanagementproject.classes.Notification
import com.project.inventorymanagementproject.classes.NotificationAdapter
import com.project.inventorymanagementproject.classes.Product
import com.project.inventorymanagementproject.classes.RequestAdapterUser
import com.project.inventorymanagementproject.classes.User
import com.project.inventorymanagementproject.databinding.FragmentHistoryBinding
import com.project.inventorymanagementproject.databinding.FragmentNotificationBinding
import com.project.inventorymanagementproject.databinding.FragmentProfileBinding
import com.project.inventorymanagementproject.objects.CurrentUser

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var navController: NavController
    private lateinit var db: FirebaseFirestore
    private lateinit var notificationsRef: CollectionReference
    private lateinit var notificationList: ArrayList<Notification>
    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNotificationBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        getNotifications()
        binding.notifications.setOnClickListener(View.OnClickListener {
            val actionHome = NotificationFragmentDirections.actionNotificationFragmentToHomeFragmentMenu()
            navController.navigate(actionHome.actionId)
        })
    }

    private fun init(view: View){
        navController = Navigation.findNavController(view)
        db = FirebaseFirestore.getInstance()
        notificationsRef = db.collection("Notifications")
        notificationList = ArrayList()

        recyclerView = binding.notificationRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun getNotifications(){
        notificationsRef.get()
            .addOnSuccessListener(OnSuccessListener { querySnapshot ->
                for(snapshot in querySnapshot!!.documents) {

                    val notification: Notification = Notification(
                        snapshot["NotificationMessage"].toString(),
                        snapshot["NotificationDate"] as Timestamp
                    )

                    notificationList.add(notification)
                }

                notificationList.sortByDescending { it.getNotificationDate() }
                setNotificationAdapter()
            }).addOnFailureListener(OnFailureListener { exception->
                Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
            })
    }

    private fun setNotificationAdapter(){
        notificationAdapter = NotificationAdapter(notificationList)
        recyclerView.adapter = notificationAdapter
    }
}