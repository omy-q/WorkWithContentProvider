package com.example.workwithcontentprovider

import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.Manifest
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workwithcontentprovider.databinding.MainFragmentBinding

class MainFragment : Fragment() {

    private val READ_CONTACTS = 1
    private val CALL = 2
    private val SEND = 3
    private lateinit var number : String

    private val contacts: MutableList<Contact> = mutableListOf()
    private val recyclerAdapter = RecyclerAdapter(object : OnItemViewClickListener {
        override fun onCallButtonClick(phone: String) {
            number = phone
            context?.let {
                when (PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(it,
                        Manifest.permission.CALL_PHONE) -> phoneCall(number)
                    else -> requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), CALL)
                }
            }
        }

        override fun onSendMessageButtonClick(phone: String, message: String) {
            number = phone
            context?.let {
                when (PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(it,
                        Manifest.permission.SEND_SMS) -> sendMessage(number)
                    else -> requestPermissions(arrayOf(Manifest.permission.SEND_SMS), SEND)
                }
            }
        }
    })

    private fun phoneCall(number: String) {
        if (number != "default") {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$number")
            startActivity(callIntent)
        } else {
            Toast.makeText(context, "Number is not defined", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendMessage(number: String){
        if (number != "default") {
            val uri = Uri.parse("tel:$number")
            val sendIntent = Intent(Intent.ACTION_SENDTO, uri)
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Hello")
            startActivity(sendIntent)}
        else{
            Toast.makeText(context, "Number is not defined", Toast.LENGTH_LONG).show()
        }
    }

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
    }

    private fun checkPermission() {
        context?.let {
            when (PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(it,
                    Manifest.permission.READ_CONTACTS) -> getContacts()
                else -> requestPermission()
            }
        }
    }

    private fun showDialog(it: Context) {
        AlertDialog.Builder(it)
            .setTitle("Доступ к контактам")
            .setMessage("Объяснение")
            .setNegativeButton("Не надо") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Предоставить доступ") { _, _ -> requestPermission() }
            .create()
            .show()
    }

    private fun requestPermission() {
        requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), READ_CONTACTS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    getContacts()
                } else {
                    context?.let { showDialog(it) }
                }
            }
            CALL -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    phoneCall(number)
                } else{
                    Toast.makeText(context, "You don't assign permission.", Toast.LENGTH_SHORT).show()
                }
            }
            SEND -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    sendMessage(number)
                } else{
                    Toast.makeText(context, "You don't assign permission.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getContacts() {
        context?.let {
            val contentResolver = it.contentResolver
            val cursorWithContacts = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            )

            cursorWithContacts?.let { cursor: Cursor ->
                for (i in 0..cursor.count) {
                    if (cursor.moveToPosition(i)) {
                        lateinit var phones: List<String>
                        val id = cursor.getString(
                            cursor
                                .getColumnIndex(ContactsContract.Contacts._ID)
                        )
                        val name = cursor.getString(
                            cursor
                                .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        )
                        if (Integer.parseInt(
                                cursor.getString(
                                    cursor
                                        .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                                )
                            ) > 0
                        ) {
                            phones = getContactPhones(it, id)
                        }
                        contacts.add(Contact(name, phones))
                    }
                }
            }
            cursorWithContacts?.close()
            setView()
        }
    }

    private fun setView() {
        with(binding) {
            recyclerView.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL, false
            )
            recyclerView.adapter = recyclerAdapter
            recyclerAdapter.setData(contacts)
        }
    }

    private fun getContactPhones(it: Context, id: String): List<String> {
        val phones: MutableList<String> = mutableListOf()
        val cursorWithPhones = it.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
            null,
            null
        )
        cursorWithPhones?.let { cursor: Cursor ->
            for (i in 0..cursor.count) {
                if (cursor.moveToPosition(i)) {
                    val phone = cursorWithPhones.getString(
                        cursorWithPhones
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    )
                    phones.add(phone)
                }
            }
        }
        cursorWithPhones?.close()
        return phones.toList()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}