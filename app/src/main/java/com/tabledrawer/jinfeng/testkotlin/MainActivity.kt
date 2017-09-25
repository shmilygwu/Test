package com.tabledrawer.jinfeng.testkotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.tabledrawer.jinfeng.testkotlin.model.Utilities
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val playButton = findViewById<View>(R.id.play_button)
//        playButton.setOnClickListener{
//
//        }
        play_button.setOnClickListener{
            loadGameData()
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        high_scores.setOnClickListener{
            high_scores.text = "High Score"
        }
    }

    fun loadGameData() {
        doAsync {
            val gameData = Utilities.loadGameData("presidents.cvs", this@MainActivity)
            if (gameData != null && gameData.questions.isNotEmpty()){
                toast(gameData.questions.count())
                Log.d("Test", gameData.questions.count().toString())
            }else{
                Log.d("Test", "Problem")
            }
        }
    }
}
