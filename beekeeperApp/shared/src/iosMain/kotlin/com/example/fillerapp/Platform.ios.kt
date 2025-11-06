// File: shared/src/iosMain/kotlin/com/example/fillerapp/Platform.ios.kt
package com.example.fillerapp

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()
