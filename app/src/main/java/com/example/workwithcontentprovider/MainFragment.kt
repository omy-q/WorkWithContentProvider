package com.example.workwithcontentprovider

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.workwithcontentprovider.databinding.MainFragmentBinding

class MainFragment : Fragment() {

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
                        val name = cursor.getString(
                            cursor
                                .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        )
                        addView(it, name)
                    }
                }
            }
            cursorWithContacts?.close()
        }
    }

    private fun addView(context: Context, textToShow: String) {
        binding.containerForContacts.addView(TextView(context).apply {
            text = textToShow
            textSize = resources.getDimension(R.dimen.text_size)
        })
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}