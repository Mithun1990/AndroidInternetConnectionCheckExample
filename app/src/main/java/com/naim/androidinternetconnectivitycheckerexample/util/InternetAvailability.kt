package com.naim.androidinternetconnectivitycheckerexample.util

import java.net.InetSocketAddress
import java.net.Socket

object InternetAvailability {
    fun check(): Boolean {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress("8.8.8.8", 53))
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }
}