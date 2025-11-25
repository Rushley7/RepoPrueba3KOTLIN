package com.example.bettersneaker.utils

import android.content.Intent
import java.io.Serializable

fun <T : Serializable> Intent.getSerializable(key: String, mClass: Class<T>): T? {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        this.getSerializableExtra(key, mClass)
    } else {
        @Suppress("DEPRECATION")
        this.getSerializableExtra(key) as? T
    }
}