package com.kenny.kmlparser

import android.content.Context
import android.support.annotation.NonNull
import android.util.Log
import io.reactivex.Observable
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

class NewFiXmlParser private constructor(context: Context) {

    private val TAG: String = NewFiXmlParser::class.java.simpleName
    private var appContext = context.applicationContext
    private var pullParser = XmlPullParserFactory.newInstance().newPullParser()

    init {
        // Initializing
    }

    companion object: SingletonHolder<NewFiXmlParser, Context>(:: NewFiXmlParser)

    @Throws(XmlPullParserException::class, IOException::class)
    fun <T : Any>parse(@NonNull file: File, @NonNull clazz: Class<T>): Observable<T> {

        return parse(FileInputStream(file), clazz)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun <T : Any>parse(@NonNull inputStream: InputStream, @NonNull clazz: Class<T>): Observable<T> {

        inputStream.use {
            pullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            pullParser.setInput(inputStream, null)
            pullParser.nextTag()

            if (pullParser.name.equals(clazz.simpleName, true)) {
                val instance = clazz.newInstance()

                Log.d("Kenny", "instance: ${instance}")

                val attributeMap = HashMap<String, Any>()

                for (index in 0..(pullParser.attributeCount - 1)) {
                    Log.d("Kenny", "${pullParser.getAttributeName(index)} ${pullParser.getAttributeNamespace(index)} ${pullParser.getAttributeValue(index)}")
                    attributeMap[pullParser.getAttributeName(index)] = pullParser.getAttributeValue(index)
                }

                for (field in clazz.declaredFields) {

                    val name = field.name
                    System.out.println("field: ${field.name}")
                    Log.d("Kenny", "find from map: $name -> ${attributeMap[name]}")
                    val property = instance::class.java.getDeclaredField(field.name)
                    property.set(instance, attributeMap[name])
                }

                return Observable.just(instance)
            }

            return Observable.empty()
        }
    }
}

