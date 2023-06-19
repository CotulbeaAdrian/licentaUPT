package com.example.medbuddy.data

class Message {
    var message: String? = null
    var senderID: String? = null
    var receiverID: String? = null
    var roomID: String? = null

    constructor(message: String?, senderID: String?, receiverID: String?, roomID: String?) {
        this.message = message
        this.senderID = senderID
        this.receiverID = receiverID
        this.roomID = roomID
    }
}