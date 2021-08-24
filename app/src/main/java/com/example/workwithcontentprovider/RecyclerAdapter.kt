package com.example.workwithcontentprovider

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.workwithcontentprovider.RecyclerAdapter.*

class RecyclerAdapter(private val onItemViewClickListener: OnItemViewClickListener) :
    RecyclerView.Adapter<ViewHolder>() {

    private lateinit var contacts: List<Contact>

    fun setData(data: List<Contact>) {
        contacts = data
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(contact: Contact) {
            with(itemView) {
                findViewById<TextView>(R.id.contactName).text = contact.name
                findViewById<TextView>(R.id.contactPhone).text =
                    if (contact.phones.isNotEmpty()) {
                        contact.phones[0]
                    } else (R.string.default_phone).toString()
                findViewById<Button>(R.id.call).setOnClickListener {
                    onItemViewClickListener.onCallButtonClick(
                        if (contact.phones.isNotEmpty()) contact.phones[0]
                        else "default")
                }
                findViewById<Button>(R.id.sendMessage).setOnClickListener {
                    onItemViewClickListener.onSendMessageButtonClick(contact.phones[0],
                        "Привет, ${contact.name}")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contact, parent, false) as View
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount() = contacts.size
}