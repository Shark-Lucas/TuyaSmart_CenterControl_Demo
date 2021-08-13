package com.tuya.gw_android_demo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.fastjson.JSON
import com.tuya.gw_android_demo.databinding.ActivityNoAbilityGwBinding
import com.tuya.libiot.TuyaIotSdk
import com.tuya.libiot.interfaces.IotCallbacks
import com.tuya.libiot.model.DataPoint
import com.tuya.libiot.model.IotConfig
import com.tuya.libiot.type.GatewayError.*
import com.tuya.libiot.type.GatewayStatus.*
import com.tuya.libiot.type.LogLevel.TY_LOG_LEVEL_DEBUG
import com.tuya.libiot.type.NetworkStatus.CLOUD_CONNECTED
import com.tuya.libiot.type.NetworkStatus.CLOUD_UNCONNECTED
import com.tuya.smart.android.network.Business
import com.tuya.smart.android.network.http.BusinessResponse
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.home.sdk.bean.HomeBean
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback
import com.tuya.smart.optimus.centralcontrol.api.ITuyaCentralControlSdk
import com.tuya.smart.optimus.sdk.TuyaOptimusSdk
import com.tuya.smart.optimus.verticalcategory.api.ITuyaVerticalSdk
import com.tuya.smart.optimus.verticalcategory.device.base.api.OnDeviceListener
import com.tuya.smart.optimus.verticalcategory.device.base.bean.DpUpdateBean
import com.tuya.smart.sdk.api.IResultCallback
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken
import java.io.File


class NoAbilityGW : AppCompatActivity() , View.OnClickListener{
    private lateinit var mIot: TuyaIotSdk
    private val TAG = "NoAbilityGW"
    private lateinit var binding: ActivityNoAbilityGwBinding
    private var mToken: String? = null
    private var mDevArr = ArrayList<DeviceBean>()
    private var mHomeId: Long = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoAbilityGwBinding.inflate(layoutInflater)
        val rootView: View = binding.root
        setContentView(rootView)

        binding.btnDevid.setOnClickListener(this)
        binding.btnSubDevTest.setOnClickListener(this)

        binding.btnDevid.isEnabled = false
        binding.btnSubDevTest.isEnabled = false


        mHomeId = intent.getLongExtra("homeId", 0L)
        Log.d(TAG, "onCreate: homeId = $mHomeId")

        SDKInit()
        activateDev()
        homeInit()
    }

    var iotCallback = object : IotCallbacks{
        override fun onStartSuccess() {
            Log.d(TAG, "onStartSuccess")
        }

        override fun onStartFailure(p0: Int) {
            when(p0){
                ERROR_COM_ERROR -> Log.d(TAG, "onStartFailure : ERROR_COM_ERROR")
                ERROR_INVALID_PARM -> Log.d(TAG, "onStartFailure : ERROR_INVALID_PARM")
                ERROR_INVALID_STATUS -> Log.d(TAG, "onStartFailure : ERROR_INVALID_STATUS")
            }
        }

        override fun onStatusChanged(p0: Int) {
            when(p0){
                STATUS_RESET -> Log.d(TAG, "onStatusChanged : STATUS_RESET")
                STATUS_ACTIVATED -> Log.d(TAG, "onStatusChanged : STATUS_ACTIVATED")
                STATUS_FIRST_START -> Log.d(TAG, "onStatusChanged : STATUS_FIRST_START")
                STATUS_NORMAL -> Log.d(TAG, "onStatusChanged : STATUS_NORMAL")
            }
        }

        override fun onReset(p0: Int) {
            Log.d(TAG, "onReset : $p0")
        }

        override fun onReboot() {
            Log.d(TAG, "onReboot")
        }

        override fun onDataPointCommand(
            p0: Int,
            p1: Int,
            p2: String?,
            p3: String?,
            p4: Array<out DataPoint>?
        ) {
            Log.d(TAG, "onDataPointCommand")
            Log.d(TAG, "onDataPointCommand: p0 = $p0")
            Log.d(TAG, "onDataPointCommand: p1 = $p1")
            Log.d(TAG, "onDataPointCommand: p2 = ${p2 ?: "null"}")
            Log.d(TAG, "onDataPointCommand: p3 = ${p3 ?: "null"}")
            if(p4 != null) {
                Log.d(TAG, "onDataPointCommand: p4 = ${p4.size}")
                for(index in p4.indices){
                    dumpDataPoint(p4[index])
                }
            }
        }

        override fun onRawDataPointCommand(
            p0: Int,
            p1: Int,
            p2: String?,
            p3: Int,
            p4: String?,
            p5: ByteArray?
        ) {
            Log.d(TAG, "onRawDataPointCommand")
            Log.d(TAG, "onDataPointCommand: p0 = $p0")
            Log.d(TAG, "onDataPointCommand: p1 = $p1")
            Log.d(TAG, "onDataPointCommand: p2 = ${p2 ?: "null"}")
            Log.d(TAG, "onDataPointCommand: p3 = $p3")
            Log.d(TAG, "onDataPointCommand: p4 = ${p4 ?: "null"}")
            Log.d(TAG, "onDataPointCommand: p5 = ${(p5 ?: ByteArray(0)).size}")
        }

        override fun onDataPointQuery(p0: String?, p1: IntArray?) {
            Log.d(TAG, "onDataPointQuery: p0 = " + (p0 ?: "null") + " p1 = " + (p1 ?: "null"))
        }

        override fun onNetworkStatus(p0: Int) {
            when(p0){
                CLOUD_CONNECTED -> Log.d(TAG, "onNetworkStatus: CLOUD_CONNECTED")
                CLOUD_UNCONNECTED -> Log.d(TAG, "onNetworkStatus: CLOUD_UNCONNECTED")
            }
        }

        override fun onGetLogFile(): String {
            Log.d(TAG, "onGetLogFile")
            return ""
        }
    }

    private fun SDKInit() {
        // 获取中控实例
        mIot = TuyaIotSdk.getInstance()
        // 设置回调，SDK初始化结果异步返回，由回调函数 onStartSuccess 和 onStartFailure 通知
        mIot.setIotCallbacks(iotCallback)

        val filedirs: File = filesDir
        val storageDir: String = filedirs.path + File.separator + "storage" + File.separator
        val file = File(storageDir)
        if (!file.exists()) {
            file.mkdir()
        }

        val cfg = IotConfig()
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
        cfg.mLogLevel = TY_LOG_LEVEL_DEBUG
        // 设备的网卡名称，比如: wlan0, eth0。为null时SDK会使用系统默认的网卡。
        cfg.mNetworkInterface = null

        // 启动网关中控
        mIot.tuyaIotStart(this, cfg)
    }

    private fun activateDev(){
        TuyaHomeSdk.getActivatorInstance()
            .getActivatorToken(mHomeId, object : ITuyaActivatorGetToken {
                override fun onSuccess(token: String) {
                    Log.d(TAG, "getActivatorToken onSuccess: token = $token")
                    mToken = token
                    val bindResult = mIot.tuyaIotBindToken(token)
                    Log.d(TAG, "getActivatorToken onBind: $bindResult")
                }
                override fun onFailure(s: String, s1: String) {
                    Log.d(TAG, "getActivatorToken onFailure: $s |||| $s1")
                }
            })
    }

    private fun homeInit(){
        TuyaHomeSdk.newHomeInstance(mHomeId).getHomeDetail(object : ITuyaHomeResultCallback {
            override fun onSuccess(bean: HomeBean) {
                Log.d(TAG, "getHomeDetail onSuccess: ")
                binding.btnDevid.isEnabled = true
            }

            override fun onError(errorCode: String, errorMsg: String) {
                Log.d(TAG, "getHomeDetail onError: ")

            }
        })

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btn_devid -> {
                val manager = TuyaOptimusSdk.getManager(ITuyaCentralControlSdk::class.java)
                val dataManager = manager.dataManager
                dataManager.getAllDevice(mIot.id, object :Business.ResultListener<ArrayList<String>>{
                    override fun onFailure(p0: BusinessResponse?, p1: ArrayList<String>?, p2: String?) {
                        Log.d(TAG, "getAllDevice onFailure: ")
                    }
                    override fun onSuccess(p0: BusinessResponse?, p1: ArrayList<String>?, p2: String?) {
                        Log.d(TAG, "getAllDevice onSuccess: $p1")
                        mDevArr.clear()
                        if (p1 != null) {
                            for(index in p1.indices){
                                var dev = JSON.parseObject(p1[index], DeviceBean::class.java)
                                val centralControlSdk = TuyaOptimusSdk.getManager(ITuyaVerticalSdk::class.java)
                                val deviceManager = centralControlSdk.deviceManager
                                //依据mDevId 找到对应设备的 category
                                val category = deviceManager.getDeviceCategory(dev.devId) as String
                                dev.categroy = category
                                mDevArr.add(dev)
                            }
                        }
                        val adapter = DeviceBeanAdapter(this@NoAbilityGW, mDevArr)
                        val layoutManager = LinearLayoutManager(this@NoAbilityGW)
                        layoutManager.orientation = RecyclerView.VERTICAL //线性布局

                        binding.rv.layoutManager = layoutManager
                        binding.rv.adapter = adapter
                        binding.btnSubDevTest.isEnabled = true
                    }
                })
            }
            R.id.btn_subDevTest -> {
                Log.d(TAG, "onClick: ${mDevArr.size}")
                for(index in mDevArr.indices){
                    val centralControlSdk = TuyaOptimusSdk.getManager(ITuyaVerticalSdk::class.java)
                    val deviceManager = centralControlSdk.deviceManager
                    //依据mDevId 找到对应设备的 category
                    val catgory = deviceManager.getDeviceCategory(mDevArr[index].devId) as String
                    Log.d(TAG, "onClick: catgory = $catgory")
                    when(catgory){
                        "cz" -> {
                            val czDev = deviceManager.getSinglePlugDevice(mDevArr[index].devId)
                            var funList = czDev.supportFunctionList
                            var staList = czDev.supportStatusList
                            var isStand = czDev.isStandDevice
                            Log.d(TAG, "onClick: funList.size = ${funList.size}")
                            Log.d(TAG, "onClick: staList.size = ${staList.size}")
                            Log.d(TAG, "onClick: isStand = $isStand")
                        }
                        "wsdcg" -> {
                            val wsdDev = deviceManager.getSensorWSDCGDevice(mDevArr[index].devId)
                            var funList = wsdDev.supportFunctionList
                            var staList = wsdDev.supportStatusList
                            var isStand = wsdDev.isStandDevice
                            Log.d(TAG, "onClick: funList.size = ${funList.size}")
                            Log.d(TAG, "onClick: staList.size = ${staList.size}")
                            Log.d(TAG, "onClick: isStand = $isStand")
                        }
                        "kt" -> {
                            val ktDev = deviceManager.getAirConditionDevice(mDevArr[index].devId)
                            var funList = ktDev.supportFunctionList
                            var staList = ktDev.supportStatusList
                            var isStand = ktDev.isStandDevice
                            Log.d(TAG, "onClick: funList.size = ${funList.size}")
                            Log.d(TAG, "onClick: staList.size = ${staList.size}")
                            Log.d(TAG, "onClick: isStand = $isStand")
                            ktDev.operate().powerSwitch(!ktDev.powerSwitch).publish(object : IResultCallback{
                                override fun onError(code: String?, error: String?) {
                                    Log.d(TAG, "onError: code = $code , String = $String")
                                }

                                override fun onSuccess() {
                                    Log.d(TAG, "onSuccess: ")
                                }
                            })
                            ktDev.registerDeviceListener(object : OnDeviceListener{
                                override fun onDpUpdate(p0: String?, p1: DpUpdateBean?) {
                                    Log.d(TAG, "onDpUpdate: p0 = $p0 , p1 = ${p1?.convertMap}")
                                }

                                override fun onRemoved() {
                                    Log.d(TAG, "onRemoved: ")
                                }

                                override fun onStatusChanged(p0: Boolean) {
                                    Log.d(TAG, "onStatusChanged: p0 = $p0")
                                }

                                override fun onNetworkStatusChanged(p0: Boolean) {
                                    Log.d(TAG, "onNetworkStatusChanged: p0 = $p0")
                                }

                                override fun onDevInfoUpdate(p0: String?) {
                                    Log.d(TAG, "onDevInfoUpdate: p0 = $p0")
                                }
                            })
                        }
                    }

                }
            }
        }
    }

    private fun dumpDataPoint(dataPoint: DataPoint) {
        Log.d(TAG, "id         :" + dataPoint.mId);
        Log.d(TAG, "type       :" + dataPoint.mType);
        Log.d(TAG, "timestamp  :" + dataPoint.mTimeStamp);
        Log.d(TAG, "data       :" + dataPoint.mData);
        when (dataPoint.mType) {
            DataPoint.TYPE_BOOL -> {
                Log.d(TAG, "bool       :" + dataPoint.mData );
            }
            DataPoint.TYPE_STRING -> {
                Log.d(TAG, "string     :" + dataPoint.mData);
            }
            else -> {
                Log.d(TAG, "value      :" + dataPoint.mData + "(" + dataPoint.mData + ")");
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mIot.tuyaIotRelease()
    }

}
