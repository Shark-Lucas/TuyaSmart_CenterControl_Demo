package com.tuya.gw_android_demo

import android.app.Application
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.tuya.smart.home.sdk.TuyaHomeSdk


class TuyaSmartApp : Application() {

    companion object{
        lateinit var app : TuyaSmartApp

        fun getInstance() : TuyaSmartApp{
            return app
        }
    }

    override fun onCreate() {
        super.onCreate()

        app = this

        TuyaHomeSdk.init(this)
        TuyaHomeSdk.setDebugMode(false)

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(applicationContext))
    }
}