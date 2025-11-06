// File: shared/src/wasmJsMain/kotlin/com/cinefiller/fillerapp/data/WasmIndexedDB.kt
package com.beekeeper.app.data

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.js.Promise

/**
 * Simple IndexedDB wrapper for WASM without external dependencies
 */
class WasmIndexedDB(private val dbName: String, private val version: Int = 1) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    private var db: IDBDatabase? = null
    
    suspend fun open(stores: List<String>) {
        val request = window.indexedDB.open(dbName, version)
        
        request.onupgradeneeded = { event ->
            val db = (event.target as IDBOpenDBRequest).result
            stores.forEach { storeName ->
                if (!db.objectStoreNames.contains(storeName)) {
                    db.createObjectStore(storeName, js("{ keyPath: 'id' }"))
                }
            }
        }
        
        db = Promise<IDBDatabase> { resolve, reject ->
            request.onsuccess = { event ->
                resolve((event.target as IDBOpenDBRequest).result)
            }
            request.onerror = { event ->
                reject(Exception("Failed to open database"))
            }
        }.await()
    }
    
    suspend inline fun <reified T : Any> put(storeName: String, id: String, data: T) {
        val database = db ?: throw Exception("Database not opened")
        val transaction = database.transaction(arrayOf(storeName), "readwrite")
        val store = transaction.objectStore(storeName)
        
        val jsObject = js("{}")
        jsObject.id = id
        jsObject.data = json.encodeToString(data)
        
        val request = store.put(jsObject)
        
        Promise<Unit> { resolve, reject ->
            request.onsuccess = { resolve(Unit) }
            request.onerror = { reject(Exception("Failed to put data")) }
        }.await()
    }
    
    suspend inline fun <reified T : Any> get(storeName: String, id: String): T? {
        val database = db ?: throw Exception("Database not opened")
        val transaction = database.transaction(arrayOf(storeName), "readonly")
        val store = transaction.objectStore(storeName)
        val request = store.get(id)
        
        return Promise<T?> { resolve, reject ->
            request.onsuccess = { event ->
                val result = (event.target as IDBRequest).result
                if (result != null && result.data != undefined) {
                    try {
                        val data = json.decodeFromString<T>(result.data as String)
                        resolve(data)
                    } catch (e: Exception) {
                        resolve(null)
                    }
                } else {
                    resolve(null)
                }
            }
            request.onerror = { reject(Exception("Failed to get data")) }
        }.await()
    }
    
    suspend fun delete(storeName: String, id: String) {
        val database = db ?: throw Exception("Database not opened")
        val transaction = database.transaction(arrayOf(storeName), "readwrite")
        val store = transaction.objectStore(storeName)
        val request = store.delete(id)
        
        Promise<Unit> { resolve, reject ->
            request.onsuccess = { resolve(Unit) }
            request.onerror = { reject(Exception("Failed to delete data")) }
        }.await()
    }
    
    suspend inline fun <reified T : Any> getAll(storeName: String): List<T> {
        val database = db ?: throw Exception("Database not opened")
        val transaction = database.transaction(arrayOf(storeName), "readonly")
        val store = transaction.objectStore(storeName)
        val request = store.getAll()
        
        return Promise<List<T>> { resolve, reject ->
            request.onsuccess = { event ->
                val results = (event.target as IDBRequest).result as Array<dynamic>
                val items = results.mapNotNull { item ->
                    try {
                        if (item.data != undefined) {
                            json.decodeFromString<T>(item.data as String)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                resolve(items)
            }
            request.onerror = { reject(Exception("Failed to get all data")) }
        }.await()
    }
    
    fun close() {
        db?.close()
        db = null
    }
}

// External declarations for IndexedDB
external interface IDBDatabase {
    val objectStoreNames: DOMStringList
    fun transaction(storeNames: Array<String>, mode: String): IDBTransaction
    fun createObjectStore(name: String, options: dynamic): IDBObjectStore
    fun close()
}

external interface IDBTransaction {
    fun objectStore(name: String): IDBObjectStore
}

external interface IDBObjectStore {
    fun put(value: dynamic): IDBRequest
    fun get(key: dynamic): IDBRequest
    fun delete(key: dynamic): IDBRequest
    fun getAll(): IDBRequest
}

external interface IDBRequest {
    val result: dynamic
    var onsuccess: ((dynamic) -> Unit)?
    var onerror: ((dynamic) -> Unit)?
}

external interface IDBOpenDBRequest : IDBRequest {
    var onupgradeneeded: ((dynamic) -> Unit)?
}

external interface DOMStringList {
    fun contains(string: String): Boolean
}

external interface Window {
    val indexedDB: IDBFactory
}

external interface IDBFactory {
    fun open(name: String, version: Int): IDBOpenDBRequest
}
