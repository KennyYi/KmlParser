package com.kenny.kmlparser

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputStream = resources.assets.open("sample.xml")
        KmlParser.getInstance(this).parse(inputStream, Products::class.java)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {

                    main_text_view.setText(
                            "${it.bundleversion}\n" +
                                    "${it.version}\n" +
                                    "${it.commands?.msg?.command?.id}\n" +
                                    "${it.commands?.msg?.command?.name}\n" +
                                    "${it.commands?.msg?.command?.value}\n" +
                                    "${it.commands?.msg?.results?.size}\n"
                    )
//                    Log.d("Kenny", "-----> ${it.bundleversion}")
//                    Log.d("Kenny", "-----> ${it.version}")
//
//                    ////////////////////////////////////////
//                    Log.d("Kenny", "-----> ${it.commands?.msg?.command?.id}")
//                    Log.d("Kenny", "-----> ${it.commands?.msg?.command?.name}")
//                    Log.d("Kenny", "-----> ${it.commands?.msg?.command?.value}")
                }
    }
}
