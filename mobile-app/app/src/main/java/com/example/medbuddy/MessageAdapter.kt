package com.example.medbuddy

import SharedPrefUtil
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medbuddy.data.Message

class MessageAdapter(val context: Context, private val messageList: ArrayList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val itemReceive = 1
    private val itemSent = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType ==1){
            val view:View = LayoutInflater.from(context).inflate(R.layout.chat_receive,parent, false)
            ReceiveViewHolder(view)
        } else {
            val view:View = LayoutInflater.from(context).inflate(R.layout.chat_sent,parent, false)
            SentViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        if (holder.javaClass == SentViewHolder::class.java) {

            holder as SentViewHolder
            holder.sentMessage.text = currentMessage.message
        } else {
            holder as ReceiveViewHolder
            holder.receiveMessage.text = currentMessage.message
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        val userData = SharedPrefUtil.getUserData(context)
        return if (userData.id == currentMessage.receiverID) {
            itemReceive
        } else {
            itemSent
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage: TextView = itemView.findViewById(R.id.text_sent_message)
    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage: TextView = itemView.findViewById(R.id.text_receive_message)

    }
}