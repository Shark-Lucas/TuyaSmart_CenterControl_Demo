package com.tuya.gw_android_demo

//import com.uuzuche.lib_zxing.activity.CodeUtils

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.tuya.gw_android_demo.databinding.ActivityMainBinding
import com.tuya.smart.android.user.api.ILogoutCallback
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.home.sdk.bean.HomeBean
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback


/**
 * 主界面，家庭管理，必要的初始化操作
 *
 */
class MainActivity : AppCompatActivity() , View.OnClickListener{

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"
    private var mHomeId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val rootView: View = binding.root
        setContentView(rootView)
        
        binding.noAbilityGW.setOnClickListener(this)
        binding.zbAbilityGW.setOnClickListener(this)
        binding.ownAbilityGW.setOnClickListener(this)
        binding.btnLogout.setOnClickListener(this)

        familyInit()
    }

    private fun familyInit(){
        val homeId = TuyaHomeSdk.getUserInstance().user!!.extras!!["homeId"].toString().toLong()
        Log.d(TAG, "familyInit: homeId = $homeId")
        mHomeId = homeId

        val homeInstance = TuyaHomeSdk.newHomeInstance(mHomeId)
        homeInstance.getHomeLocalCache(object : ITuyaHomeResultCallback {
            override fun onSuccess(bean: HomeBean) {
                for(i in bean.deviceList.indices){
                    Log.d(TAG, "getHomeLocalCache onSuccess: bean.deviceList = ${bean.deviceList[i].name}")
                }
            }
            override fun onError(errorCode: String, errorMsg: String) {
                //sdk cache error do not deal
                Log.d(TAG, "getHomeLocalCache onError: ")
            }
        })

        homeInstance.getHomeDetail(object : ITuyaHomeResultCallback{
            override fun onSuccess(bean: HomeBean?) {
                for(i in bean!!.deviceList.indices){
                    Log.d(TAG, "getHomeDetail onSuccess: bean.deviceList = ${bean.deviceList[i].name}")
                }
            }
            override fun onError(errorCode: String?, errorMsg: String?) {
                Log.d(TAG, "getHomeDetail onError: ")
            }
        })
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.noAbilityGW -> {
                val intent = Intent(this, NoAbilityGW::class.java)
                intent.putExtra("homeId", mHomeId)
                startActivity(intent)
            }
            R.id.zbAbilityGW -> {
                val intent = Intent(this, ZbAbilityGW::class.java)
                intent.putExtra("homeId", mHomeId)
                startActivity(intent)
            }
            R.id.ownAbilityGW -> {
                val intent = Intent(this, OwnAbilityGW::class.java)
                intent.putExtra("homeId", mHomeId)
                startActivity(intent)
            }
            R.id.btn_logout -> {
                TuyaHomeSdk.getUserInstance().logout(object : ILogoutCallback{
                    override fun onSuccess() {
                        Log.d(TAG, "logout onSuccess: ")
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    override fun onError(code: String?, error: String?) {
                        Log.d(TAG, "logout onError: code = $code, error = $error")
                    }
                })
            }
        }
    }

}