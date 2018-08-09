package com.kenny.kmlparser

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var inputStream = resources.assets.open("sample.xml")
        KmlParser.getInstance(this).parse(inputStream, Products::class.java)
                .subscribe {
                    Log.d("Kenny", "----->")
                }
    }
}
