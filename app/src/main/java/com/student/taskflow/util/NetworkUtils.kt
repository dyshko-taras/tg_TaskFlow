package com.student.taskflow.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

object NetworkUtils {

    fun validateEmail(email: String): Boolean {
        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()
        return email.isNotEmpty() && email.matches(emailRegex)
    }

    fun getActiveConnectionType(context: Context): String {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "No Connection"
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return "No Connection"
        Log.d("NetworkUtils", networkCapabilities.toString())

        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                Log.d("NetworkUtils", "Connected to Wi-Fi")
                "Wi-Fi"
            }

            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                Log.d("NetworkUtils", "Connected to Mobile Data")
                "Mobile Data"
            }

            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                Log.d("NetworkUtils", "Connected to Ethernet")
                "Ethernet"
            }

            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                Log.d("NetworkUtils", "Connected to VPN")
                "VPN"
            }

            else -> {
                Log.d("NetworkUtils", "No Internet Connection")
                "No Connection"
            }
        }
    }

//    fun isInternetAvailable(): Boolean {
//        return try {
//            val socket = Socket()
//            val socketAddress = InetSocketAddress("8.8.8.8", 53) // Google DNS
//            socket.connect(socketAddress, 1500) // timeout
//            socket.close()
//            Log.d("NetworkUtils", "Internet accessible")
//            true
//        } catch (e: Exception) {
//            Log.d("NetworkUtils", "Internet not accessible: ${e.localizedMessage}")
//            false
//        }
//    }

    fun isInternetAvailable(context: Context): Boolean {
        getActiveConnectionType(context)
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}