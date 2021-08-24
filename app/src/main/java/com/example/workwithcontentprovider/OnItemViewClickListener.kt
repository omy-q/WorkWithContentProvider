package com.example.workwithcontentprovider

interface OnItemViewClickListener {
    fun onCallButtonClick(phone : String)
    fun onSendMessageButtonClick(phone : String, msg : String)
}