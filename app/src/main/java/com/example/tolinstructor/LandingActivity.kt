package com.example.tolinstructor

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_landing.*
import org.json.JSONObject
import java.io.InputStream

class LandingActivity : AppCompatActivity() {

    lateinit var mSocket: Socket;
    val PREFS_FILENAME = "com.example.tugoflogic.prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        var string: String? = ""
        try {
            val inputStream: InputStream = assets.open("source.txt")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)

            inputStream.read(buffer)
            string = String(buffer)
            hostIPTxt.setText(string)

            mSocket = IO.socket(hostIPTxt.text.toString())
            mSocket.connect()
            Toast.makeText(this,"Connected to " + hostIPTxt.text.toString(), Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Log.d("error", e.message.toString())
        }


        btInstructorLogin.setOnClickListener {
            mSocket.emit("instructor_login")
            val intent = Intent(this, InstuctorActivity::class.java)
            startActivity(intent)
        }

        mSocket.on("instructor_login_return", onInstructorLogin)

    }


    var onInstructorLogin = Emitter.Listener {

        val data = it[0] as JSONObject

        val msg = data.getString("message")
        val room_id = data.getString("room_id")

        val sharedPreference =  getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        editor.putString("room_id",room_id)
        editor.commit()

        startActivity(Intent(this, InstuctorActivity::class.java))
    }


}