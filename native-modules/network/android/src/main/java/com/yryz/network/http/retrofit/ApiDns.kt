package com.yryz.network.http.retrofit

import okhttp3.Dns
import java.net.Inet4Address
import java.net.InetAddress
import java.net.UnknownHostException

class ApiDns : Dns {
    override fun lookup(hostname: String?): MutableList<InetAddress> {

        return hostname?.run {
            try {
                var mInetAddressesList = mutableListOf<InetAddress>()
                val mInetAddresses = InetAddress.getAllByName(hostname)
                for (address in mInetAddresses) {
                    if (address is Inet4Address) {
                        mInetAddressesList.add(0, address)
                    } else {
                        mInetAddressesList.add(address)
                    }
                }
                return mInetAddressesList
            } catch (var4: NullPointerException) {
                val unknownHostException = UnknownHostException("Broken system behaviour")
                unknownHostException.initCause(var4)
                throw unknownHostException
            }
        } ?: throw  UnknownHostException("hostname == null")
    }
}