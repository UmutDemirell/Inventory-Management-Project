package com.project.inventorymanagementproject.classes

import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.project.inventorymanagementproject.R
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class NotificationAdapter(
    private val notificationList: ArrayList<Notification>
): RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val notificationImage: ImageView = itemView.findViewById(R.id.card_photo)
        val notificationMessage:TextView = itemView.findViewById(R.id.card_notification_message)
        val notificationCard: CardView = itemView.findViewById(R.id.notification_card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_card, parent, false)
        return NotificationViewHolder(view)
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notificationList[position]

        val requestDate = notification.getNotificationDate().toDate()

        if((System.currentTimeMillis() - requestDate.time) < TimeUnit.DAYS.toMillis(1)){
            holder.notificationImage.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.darkRed), android.graphics.PorterDuff.Mode.SRC_IN)
        }
        holder.notificationMessage.text = notification.getNotificationMessage()

        holder.notificationCard.isVisible = true
    }
}