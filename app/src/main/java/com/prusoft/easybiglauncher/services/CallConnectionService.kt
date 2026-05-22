package com.prusoft.easybiglauncher.services

import android.os.Build
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
class CallConnectionService : ConnectionService() {

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        val connection = object : Connection() {
            init {
                setConnectionProperties(PROPERTY_SELF_MANAGED)
                setInitializing()
            }

            override fun onAnswer() {
                setActive()
            }

            override fun onReject() {
                setDisconnected(android.telecom.DisconnectCause(android.telecom.DisconnectCause.REJECTED))
                destroy()
            }

            override fun onDisconnect() {
                setDisconnected(android.telecom.DisconnectCause(android.telecom.DisconnectCause.LOCAL))
                destroy()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && request != null) {
            connection.setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
        }
        connection.setRinging()
        return connection
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        val connection = object : Connection() {
            init {
                setConnectionProperties(PROPERTY_SELF_MANAGED)
                setDialing()
            }

            override fun onAbort() {
                setDisconnected(android.telecom.DisconnectCause(android.telecom.DisconnectCause.CANCELED))
                destroy()
            }

            override fun onDisconnect() {
                setDisconnected(android.telecom.DisconnectCause(android.telecom.DisconnectCause.LOCAL))
                destroy()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && request != null) {
            connection.setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
        }
        return connection
    }
}
