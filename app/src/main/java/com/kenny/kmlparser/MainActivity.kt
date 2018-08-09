package com.kenny.kmlparser

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var inputStream = resources.assets.open("sample.xml")
        KmlParser.getInstance(this).parse(inputStream, Products::class.java)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.d("Kenny", "-----> ${it.bundleversion}")
                    Log.d("Kenny", "-----> ${it.version}")
                }
    }
}
