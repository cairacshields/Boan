package com.example.cairashields.boan.stripe

import android.util.Log
import com.squareup.okhttp.*
import io.reactivex.Observable
import org.json.JSONException
import org.json.JSONObject

object FetchConnectedUserId {

    fun getId(code: String, userId: String): Observable<Response>? {

        val JSON = MediaType.parse("application/json; charset=utf-8")
        val client = OkHttpClient()

        try {
            val json = JSONObject()

            json.put("code", code)
            json.put("userId", userId)

            //Add body to the request
            val body = RequestBody.create(JSON, json.toString())


            //Send request to server, posting the body
            val request = Request.Builder()
                    //.header("Authorization", SERVER_KEY)
                    .url("https://boanservice-api.boanservices.com/connectExpress")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()

            //Try to complete the request
            return io.reactivex.Observable.create { emitter ->
                try {
                    val response = client.newCall(request).execute()
                    Log.v("RESPONSE", response.message())
                    emitter.onNext(response)
                    emitter.onComplete()
                } catch (e: Exception) {
                    e.printStackTrace()
                    emitter.onError(e) // In case there are network errors
                }
            }

        } catch (e: JSONException) {
            Log.d("Connected User Class ", "ERROR")
            e.printStackTrace()
            return null
        }
    }
}