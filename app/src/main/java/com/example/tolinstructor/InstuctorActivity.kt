package com.example.tolinstructor

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import kotlinx.android.synthetic.main.activity_instuctor.*
import org.json.JSONObject
import java.io.InputStream

class InstuctorActivity : AppCompatActivity() {

    lateinit var mSocket: Socket;
    val PREFS_FILENAME = "com.example.tugoflogic.prefs"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instuctor)

        editTextMainClaim.visibility = View.GONE
        btSaveMainClaim.visibility = View.GONE
        btStartGame.visibility = View.GONE

        var string: String? = ""
        try {
            val inputStream: InputStream = assets.open("source.txt")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)

            inputStream.read(buffer)
            string = String(buffer)

            mSocket = IO.socket(string)
            mSocket.connect()

            Toast.makeText(this,"Connected to " + string + mSocket.connected(), Toast.LENGTH_LONG).show()


        } catch (e: Exception) {
            Log.d("error", e.message.toString())
        }

        // =================================
        // GENERAL MESSAGE RETURN CALLBACK
        // =================================

        mSocket.on("message_return_instructor", onMessageReturn)


        // ===============
        // CREATE GAME
        // ===============

        btCreateGame.setOnClickListener {
            btCreateGame.visibility = View.GONE

            try {
                val sharedPreference =  getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
                val saved_roomId = sharedPreference.getString("room_id","")

                val jsonstring : String  = "{'room_id': ${saved_roomId}}"
                val jobj = JSONObject(jsonstring)

                mSocket.emit("create_game", jobj)
            } catch (e: Exception ) {
                Log.d("FlaskError", e.message.toString())
            }

            editTextMainClaim.visibility = View.VISIBLE
            btSaveMainClaim.visibility = View.VISIBLE
            btStartGame.visibility = View.VISIBLE

        }

        mSocket.on("create_game_return", onCreateGame)


        // ==================
        // SAVE MAIN CLAIM
        // ==================

        btSaveMainClaim.setOnClickListener {

            val sharedPreference =  getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
            val saved_gameId = sharedPreference.getString("game_id","")

            val mc_text = editTextMainClaim.text
            val jsonstring : String  = "{'game_id': ${saved_gameId}, 'main_claim_text': '${mc_text}'}"
            val jobj = JSONObject(jsonstring)

            mSocket.emit("save_main_claim", jobj)
        }



        // ==================
        // START GAME
        // ==================

        btStartGame.setOnClickListener {

            val sharedPreference =  getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
            val saved_gameId = sharedPreference.getString("game_id","")

            val jsonstring : String  = "{'game_id': ${saved_gameId}}"
            val jobj = JSONObject(jsonstring)

            mSocket.emit("start_game", jobj)
        }



    }

    var onMessageReturn = Emitter.Listener {
        val message = it[0] as String
        txtMsgReturn.setText(message)
    }

    var onCreateGame = Emitter.Listener {

        val data = it[0] as JSONObject

        val msg = data.getString("message")
        val game_id = data.getString("game_id")

        val sharedPreference =  getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        editor.putString("game_id",game_id)
        editor.commit()

        txtMsgReturn.setText(msg + game_id)

    }



}