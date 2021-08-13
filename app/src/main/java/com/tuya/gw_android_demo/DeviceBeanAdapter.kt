package com.tuya.gw_android_demo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener
import com.tuya.gw_android_demo.databinding.ListDeviceBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


class DeviceBeanAdapter(val mContext: Context, val mDeviceList: ArrayList<DeviceBean>) :
    RecyclerView.Adapter<DeviceBeanAdapter.ViewHolder>() {
    private val TAG = "DeviceBeanAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d(TAG, "onCreateViewHolder: ")
        return ViewHolder(ListDeviceBinding.inflate(LayoutInflater.from(mContext)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: ")
        holder.deviceBean = mDeviceList[position]
        var loader = ImageLoader.getInstance()
        loader.displayImage(mDeviceList[position].iconUrl, holder.mView.ivDevImg)
        holder.mView.tvDevName.text = mDeviceList[position].name
        holder.mView.tvDevOnlineState.text =
            if(mDeviceList[position].cloudOnline === "true") "在线" else "离线"
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: ${mDeviceList.size}")
        return mDeviceList.size
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var mView : ListDeviceBinding
        lateinit var deviceBean : DeviceBean

        private val TAG = "ViewHolder"

        constructor(binding : ListDeviceBinding) : this(binding.root){
            mView = binding
            Log.d(TAG, "ViewHolder constructor: ")

            binding.root.setOnClickListener {
                Log.d(TAG, "onClickListener: ${deviceBean.toString()}")

            }
        }

    }

}