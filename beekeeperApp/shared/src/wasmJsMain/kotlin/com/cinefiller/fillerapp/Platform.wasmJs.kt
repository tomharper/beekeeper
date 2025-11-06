// File: shared/src/wasmJsMain/kotlin/com/cinefiller/fillerapp/Platform.wasmJs.kt
package com.beekeeper.app

import kotlinx.browser.window

class WasmPlatform : Platform {
    override val name: String = "Web Assembly"
    
    val userAgent: String = window.navigator.userAgent
    val language: String = window.navigator.language
    val onLine: Boolean = window.navigator.onLine
    val screenWidth: Int = window.screen.width
    val screenHeight: Int = window.screen.height
}

actual fun getPlatform(): Platform = WasmPlatform()

// WASM-specific utilities
object WasmUtils {
    fun isMobile(): Boolean {
        val userAgent = window.navigator.userAgent.lowercase()
        return userAgent.contains("mobile") || 
               userAgent.contains("android") || 
               userAgent.contains("iphone") || 
               userAgent.contains("ipad")
    }
    
    fun getDeviceType(): String {
        return when {
            isMobile() -> "mobile"
            window.navigator.userAgent.lowercase().contains("tablet") -> "tablet"
            else -> "desktop"
        }
    }
    
    fun supportsWebGL(): Boolean {
        val canvas = window.document.createElement("canvas") as? org.w3c.dom.HTMLCanvasElement
        return canvas?.getContext("webgl") != null || canvas?.getContext("experimental-webgl") != null
    }
    
    fun supportsWebRTC(): Boolean {
        return js("'RTCPeerConnection' in window") as Boolean
    }
    
    fun supportsMediaRecorder(): Boolean {
        return js("'MediaRecorder' in window") as Boolean
    }
}
