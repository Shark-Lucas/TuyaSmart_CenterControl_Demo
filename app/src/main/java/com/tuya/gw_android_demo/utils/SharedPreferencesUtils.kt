package com.example.testlistq

import android.content.Context

object SharedPreferencesUtils {
    private const val FILE_NAME = "share_date"

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context
     * @param key  保存对应的KEY
     * @param object  保存对应的值
     */
    fun setParam(context: Context, key: String?, `object`: Any) {
        val type = `object`.javaClass.simpleName
        val sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        if ("String" == type) {
            editor.putString(key, `object` as String)
        } else if ("Integer" == type) {
            editor.putInt(key, (`object` as Int))
        } else if ("Boolean" == type) {
            editor.putBoolean(key, (`object` as Boolean))
        } else if ("Float" == type) {
            editor.putFloat(key, (`object` as Float))
        } else if ("Long" == type) {
            editor.putLong(key, (`object` as Long))
        }
        editor.commit()
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param context
     * @param key
     * @param defaultObject
     * @return
     */
    fun getParam(context: Context, key: String?, defaultObject: Any): Any? {
        val type = defaultObject.javaClass.simpleName
        val sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        if ("String" == type) {
            return sp.getString(key, defaultObject as String)
        } else if ("Integer" == type) {
            return sp.getInt(key, (defaultObject as Int))
        } else if ("Boolean" == type) {
            return sp.getBoolean(key, (defaultObject as Boolean))
        } else if ("Float" == type) {
            return sp.getFloat(key, (defaultObject as Float))
        } else if ("Long" == type) {
            return sp.getLong(key, (defaultObject as Long))
        }
        return null
    }
}