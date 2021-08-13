package com.tuya.gw_android_demo

import com.alibaba.fastjson.annotation.JSONField

class DeviceBean {
    lateinit var devId: String

    lateinit var iconUrl: String

    lateinit var name: String

    lateinit var cloudOnline: String

    @JSONField(deserialize = true)
    var categroy: String? = null

    override fun toString(): String {
        return "devId = $devId, name = $name, cloudOnline = $cloudOnline, categroy = $categroy"
    }
}