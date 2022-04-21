package com.naim.androidinternetconnectivitycheckerexample.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InternetConnectivityCheckHelper constructor(private val context: Context) :
    LiveData<InternetConnectivityCheckHelper.NetworkStatus>() {
    private val validNetworkConnections = ArrayList<Network>()
    private val connectivityManager by lazy { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
        .build()
    private val getConnectivityManagerCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            val networkCapability = connectivityManager.getNetworkCapabilities(network)
            val hasNetworkConnection =
                networkCapability?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    ?: false
            if (hasNetworkConnection) {
                CoroutineScope(Dispatchers.IO).launch {
                    if(InternetAvailability.check()){
                        withContext(Dispatchers.Main){
                            validNetworkConnections.add(network)
                            announceStatus()
                        }
                    }
                }
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            validNetworkConnections.remove(network)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            val hasNetworkConnection =
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            if (hasNetworkConnection) {
                CoroutineScope(Dispatchers.IO).launch{
                    if(InternetAvailability.check()){
                        withContext(Dispatchers.Main){
                            validNetworkConnections.add(network)
                            announceStatus()
                        }
                    }
                }

            } else {
                validNetworkConnections.remove(network)
            }
        }
    }

    fun announceStatus() {
        if (validNetworkConnections.isEmpty()) {
            postValue(NetworkStatus.UnAvailable)
        } else {
            postValue(NetworkStatus.Available)
        }
    }


    override fun onActive() {
        super.onActive()
        connectivityManager.registerNetworkCallback(
            networkRequest,
            getConnectivityManagerCallback
        )
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(getConnectivityManagerCallback)
    }

    sealed class NetworkStatus {
        object Available : NetworkStatus()
        object UnAvailable : NetworkStatus()
    }
}