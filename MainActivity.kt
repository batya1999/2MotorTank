package com.brandonhxrr.esp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONObject
import java.net.URI
import java.net.URISyntaxException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {

    lateinit var tvRPM: TextView
    lateinit var tvTemp: TextView
    lateinit var tvCapacitance: TextView
    lateinit var tvVoltage: TextView
    lateinit var txtIP: TextInputEditText
    lateinit var btnConnect: Button
    lateinit var btnUp: Button
    lateinit var btnDown: Button
    lateinit var btnLeft: Button
    lateinit var btnRight: Button

    private var webSocketClient: WebSocketClient? = null
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var sendCommandRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtIP = findViewById(R.id.txt_ip)
        btnConnect = findViewById(R.id.btn_connect)
        btnUp = findViewById(R.id.btn_up)
        btnDown = findViewById(R.id.btn_down)
        btnLeft = findViewById(R.id.btn_left)
        btnRight = findViewById(R.id.btn_right)

        btnConnect.setOnClickListener {
            val ipAddress = txtIP.text.toString()
            if (isIPAddress(ipAddress)) {
                connectWebSocket(ipAddress)
                val timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        fetchDataFromESP32(ipAddress)
                    }
                }, 0, 1500)
            } else {
                Toast.makeText(
                    applicationContext,
                    "No es una dirección IP válida",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        setButtonTouchListener(btnUp, "MoveCar,1")
        setButtonTouchListener(btnDown, "MoveCar,2")
        setButtonTouchListener(btnLeft, "MoveCar,3")
        setButtonTouchListener(btnRight, "MoveCar,4")
    }

    private fun setButtonTouchListener(button: Button, command: String) {
        button.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startSendingCommand(command)
                MotionEvent.ACTION_UP -> stopSendingCommand()
            }
            true
        }
    }

    private fun startSendingCommand(command: String) {
        sendCommandRunnable = object : Runnable {
            override fun run() {
                sendCommandToESP32(command)
                handler.postDelayed(this, 100) // Send command every 100 ms
            }
        }
        handler.post(sendCommandRunnable!!)
    }

    private fun stopSendingCommand() {
        sendCommandRunnable?.let {
            handler.removeCallbacks(it)
        }
    }

    private fun connectWebSocket(ip: String) {
        val uri: URI
        try {
            uri = URI("ws://$ip/CarInput")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return
        }

        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake) {
                Log.d("WebSocket", "Connected")
            }

            override fun onMessage(message: String) {
                Log.d("WebSocket", "Message received: $message")
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                Log.d("WebSocket", "Disconnected")
            }

            override fun onError(ex: Exception) {
                ex.printStackTrace()
                Log.e("WebSocket", "Error: ${ex.message}")
            }
        }
        webSocketClient?.connect()
    }

    private fun sendCommandToESP32(command: String) {
        if (webSocketClient != null && webSocketClient!!.isOpen) {
            webSocketClient!!.send(command)
        } else {
            Toast.makeText(applicationContext, "WebSocket is not connected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchDataFromESP32(ip: String) {
        try {
            val url = URL("http://$ip/")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val responseStringBuilder = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                responseStringBuilder.append(line)
            }

            val response = responseStringBuilder.toString()

            if (response.isNotEmpty()) {
                val json = JSONObject(response)
                val rpm = json.getDouble("rpm")
                val capacitance = json.getDouble("capacitancia")
                val temperature = json.getDouble("temperatura")
                val voltage = json.getDouble("voltaje")

                val isMagneticField = if (capacitance == 100.0) "No" else "Si"

                Handler(Looper.getMainLooper()).post {
                    tvRPM.text = String.format("%.0f rpm", rpm)
                    tvTemp.text = String.format("%.2f °C", temperature)
                    tvCapacitance.text = isMagneticField
                    tvVoltage.text = String.format("%.2f V", voltage)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MainActivity", "Error: ${e.message}")
        }
    }

    private fun isIPAddress(ip: String): Boolean {
        val regex =
            Regex(pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")
        return ip.matches(regex)
    }
}
