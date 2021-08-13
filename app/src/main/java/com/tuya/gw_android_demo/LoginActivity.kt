package com.tuya.gw_android_demo

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.testlistq.SharedPreferencesUtils
import com.king.zxing.util.CodeUtils
import com.tuya.gw_android_demo.databinding.ActivityLoginBinding
import com.tuya.smart.android.user.api.IGetQRCodeTokenCallback
import com.tuya.smart.android.user.api.ILoginCallback
import com.tuya.smart.android.user.api.ILogoutCallback
import com.tuya.smart.android.user.bean.User
import com.tuya.smart.home.sdk.TuyaHomeSdk
import pub.devrel.easypermissions.EasyPermissions
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*

/**
 * 扫码登录界面
 * 用户扫码授权登录，若登录状态返回到此界面可点击按钮返回主界面
 *
 * TODO: 跳转后销毁本页面
 *
 */
class LoginActivity : AppCompatActivity() , View.OnClickListener{
    private lateinit var binding: ActivityLoginBinding
    private val TAG = "LoginActivity"
    private lateinit var mToken: String
    private var mHomeId: Long? = null
    lateinit var loginThread : Thread


//    private var loginSuccess: Boolean = false
//    companion object{
//        var mHomeId: Long = 0
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val rootView: View = binding.root
        setContentView(rootView)

        binding.imageView.setOnClickListener(this)

        val PERMISSION_STORAGE_MSG = "请授予权限，否则影响部分使用功能"
        val PERMISSION_STORAGE_CODE = 10001
        val params = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.INTERNET, Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN)

        if (EasyPermissions.hasPermissions(this, *params)) {
            binding.textViewLoginState.text = if(TuyaHomeSdk.getUserInstance().isLogin) "已登录" else "未登录"
            // 已经申请过权限，做想做的事
            loginTuyaSmart()
        } else {
            // 没有申请过权限，去申请
            EasyPermissions.requestPermissions(this, PERMISSION_STORAGE_MSG, PERMISSION_STORAGE_CODE, *params);
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.imageView -> {
                loginThread.interrupt()
                loginTuyaSmart()
                binding.textViewLoginState.text = if(TuyaHomeSdk.getUserInstance().isLogin) "已登录" else "未登录"
            }
        }
    }

    /**
     * 登录到涂鸦智能
     *
     */
    fun loginTuyaSmart() {
        if(TuyaHomeSdk.getUserInstance().isLogin){
            binding.textViewLoginState.text = "已登录"
            Toast.makeText(this, "已登录，正在进入主页", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("homeId", mHomeId)
            startActivity(intent)
            finish()
        } else {
            binding.textViewLoginState.text = "未登录"
            TuyaHomeSdk.getUserInstance().getQRCodeToken("86", object : IGetQRCodeTokenCallback {
                override fun onSuccess(token: String) {
                    Log.d(TAG, "onSuccess: token = $token")
                    mToken = token
                    binding.imageView.setImageBitmap(CodeUtils.createQRCode("tuyaSmart--qrLogin?token=$token", 200))
                    loginThread = Thread {
                        while(!TuyaHomeSdk.getUserInstance().isLogin) {
                            TuyaHomeSdk.getUserInstance().QRCodeLogin("86", mToken, object : ILoginCallback {
                                override fun onSuccess(user: User) {
                                    Log.d(TAG, "onSuccess: ${Date().time}")
                                    binding.textViewLoginState.text = "已登录"
                                    if (!TextUtils.isEmpty(user.sid)) {
                                        Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_LONG).show()
                                        // 登录成功 存储用户信息
                                        TuyaHomeSdk.getUserInstance().loginSuccess(user)
                                        // 登录成功后可以跳转到主页
                                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                                override fun onError(code: String, error: String) {
                                    if ("USER_QR_LOGIN_TOKEN_EXPIRE" == code) {
                                        // 如果二维码过期，重新获取一下 token，更新二维码
                                    }
                                }
                            })
                            try {
                                Thread.sleep(1000)
                            } catch (e: InterruptedException) {
                                break
                            }
                        }
                    }
                    loginThread.start()
                }
                override fun onError(code: String, error: String) {
                    Log.d(TAG, "getQRCodeToken onError: code = $code, error = $error")
                }
            })
        }
    }
}