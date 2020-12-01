package com.example.tolinstructor

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    lateinit var mSocket: Socket;
    lateinit var userName: String;
    lateinit var roomName: String;
    val PREFS_FILENAME = "com.example.socketiodemo.prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var string: String? = ""
        try {
            val inputStream: InputStream = assets.open("source.txt")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)

            inputStream.read(buffer)
            string = String(buffer)
            hostTxt.setText(string)
        } catch (e: Exception) {
            Log.d("error", e.message.toString())
        }


        try {
            mSocket = IO.socket(hostTxt.text.toString())
            mSocket.connect()
            result.text = "connected to " + hostTxt.text.toString() + " " + mSocket.connected()
        } catch (e: Exception) {
            result.text = " Failed to connect. " + e.message
        }

        mSocket.on(Socket.EVENT_CONNECT, Emitter.Listener {
            mSocket.emit("instructor_login")
        });


        mSocket.on("create_game_return", onNewGame) // To know if the new user entered the room.

        mSocket.on("message", onNewMessage) // To know if the new user entered the room.


        // ===============
        // CREATE GAME
        // ===============
        submitName.setOnClickListener {
            mSocket.emit("create_game")
        }

        // ===============
        // CREATE MAIN CLAIM
        // ===============
        sendBtn.setOnClickListener {
            val mc_text = msgTxt.text
            val game_id = "recuperar"
            val jsonstring : String  = "{'game_id': ${game_id}, 'main_claim_text': '${mc_text}'}"
            val jobj = JSONObject(jsonstring)
            mSocket.emit("save_main_claim", jobj)
        }


    }

    var onNewMessage = Emitter.Listener {
        val message = it[0] as String
        result.text = message
    }

    var onNewGame = Emitter.Listener {

        // val data = args.get(0) as JSONObject
        val data = it[0] as JSONObject

        val msg = data.getString("message")
        val game_id = data.getString("game_id")

        //val gameController = GameController()
        //gameController.storeCurrentGameId(game_id)


        val sharedPreference =  getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        editor.putString("game_id",game_id)
        editor.commit()

        val saved_gameId = sharedPreference.getString("game_id","")

        result.text = msg + saved_gameId
    }


}