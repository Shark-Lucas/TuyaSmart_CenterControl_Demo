package com.tuya.gw_android_demo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.tuya.libgateway.TuyaGatewaySdk
import com.tuya.libgateway.interfaces.GatewayCallbacks
import com.tuya.libgateway.model.GatewayConfig
import com.tuya.libiot.model.DataPoint
import com.tuya.libiot.model.IotConfig
import com.tuya.libiot.type.LogLevel
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken
import java.io.File


class ZbAbilityGW : AppCompatActivity() {
    private lateinit var mGateway: TuyaGatewaySdk

    private val TAG = "ZbAbilityGW"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zb_ability_gw)

        SDKInit()
//        activateDev()
    }

    private var gatewayCallbacks = object : GatewayCallbacks{
        override fun onStatusChanged(p0: Int) {
            Log.d(TAG, "onStatusChanged: $p0")
        }

        override fun onReset(p0: Int) {
            Log.d(TAG, "onReset: $p0")
        }

        override fun onReboot() {
            Log.d(TAG, "onReboot: ")
        }

        override fun onDataPointCommand(
            p0: Int,
            p1: Int,
            p2: String?,
            p3: String?,
            p4: Array<out DataPoint>?
        ) {
            Log.d(TAG, "onDataPointCommand: ")
        }

        override fun onRawDataPointCommand(
            p0: Int,
            p1: Int,
            p2: String?,
            p3: Int,
            p4: String?,
            p5: ByteArray?
        ) {
            Log.d(TAG, "onRawDataPointCommand: ")
        }

        override fun onDataPointQuery(p0: String?, p1: IntArray?) {
            Log.d(TAG, "onDataPointQuery: ")
        }

        override fun onNetworkStatus(p0: Int) {
            Log.d(TAG, "onNetworkStatus: $p0")
        }

        override fun onStartSuccess() {
            Log.d(TAG, "onStartSuccess: ")
        }

        override fun onStartFailure(p0: Int) {
            Log.d(TAG, "onStartFailure: $p0")
        }

        override fun onGetLogFile(): String {
            Log.d(TAG, "onGetLogFile: ")
            return ""
        }

        override fun onGetIP(): String {
            Log.d(TAG, "onGetIP: ")
            return ""
        }

        override fun onGetMacAddress(): String {
            Log.d(TAG, "onGetMacAddress: ")
            return ""
        }
    }

    fun SDKInit(){
        mGateway = TuyaGatewaySdk.getInstance()
        mGateway.setGatewayCallbacks(gatewayCallbacks)

        val filedirs: File = filesDir
        val storageDir: String = filedirs.path + File.separator + "storage" + File.separator
        val file = File(storageDir)
        if (!file.exists()) {
            file.mkdir()
        }

        val cfg = GatewayConfig()
        // 固件key或者pid。和mIsOEM配合使用: mIsOEM为true时，mFirmwareKey为固件key mIsOEM为false时，mFirmwareKey为pid
        cfg.mFirmwareKey = "keyuqugcq8rrtadw"
        // 是否是oem产品
        cfg.mIsOEM = true
        // 涂鸦IOT平台获取到的UUID（授权信息），和mAuthKey成对使用
        cfg.mUUID = "8be1356dcd3d1491"
        // 涂鸦IOT平台获取到的Authkey（授权信息），和mUUID成对使用
        cfg.mAuthKey = "88I3eX6xey59CG73oVRy6ont3AW7pPzQ"
        // App的包名，用于App/固件的OTA升级。
        cfg.mPackageName = packageName
        // App/设备固件的版本号，用于固件升级，格式为 “xx.xx.xx”。
        cfg.mVersion = "1.0.0"
        // 存储路径（要求可读写分区），该目录要在app中创建
        cfg.mPath = storageDir
        // SDK输出log信息的级别
        cfg.mLogLevel = LogLevel.TY_LOG_LEVEL_DEBUG
        // 设备的网卡名称，比如: wlan0, eth0。为null时SDK会使用系统默认的网卡。
        cfg.mNetworkInterface = "wlan0"

        // 启动网关中控
        mGateway.gatewayStart(this, cfg)
    }

//    fun activateDev(){
//        TuyaHomeSdk.getActivatorInstance()
//            .getActivatorToken(LoginActivity.mHomeId, object : ITuyaActivatorGetToken {
//                override fun onSuccess(token: String) {
//                    Log.d(TAG, "getActivatorToken onSuccess: token = $token")
//                    var mToken = token
//                    val bindResult = mGateway.tuyaIotBindToken(token)
//
//                    Log.d(TAG, "getActivatorToken onBind: $bindResult")
//                }
//                override fun onFailure(s: String, s1: String) {
//                    Log.d(TAG, "getActivatorToken onFailure: $s |||| $s1")
//                }
//            })
//    }

}