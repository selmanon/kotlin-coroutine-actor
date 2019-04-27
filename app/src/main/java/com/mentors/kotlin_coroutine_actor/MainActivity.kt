package com.mentors.kotlin_coroutine_actor

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach


// message types that the actor will handle
sealed class Action

data class Spin(val id: Int) : Action()
data class Done(val ack: CompletableDeferred<Boolean>) : Action()


class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    companion object {
        private const val TAG = "MainActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

    @ObsoleteCoroutinesApi
    @SuppressLint("LogNotTimber")
    override fun onResume() {
        super.onResume()


        //firstImplementationOfActor()
        secondImplementationOfActor()
    }

    @SuppressLint("LogNotTimber")
    private fun firstImplementationOfActor() {
        val start = System.currentTimeMillis() // compute the start time

        (1..100).map { Spin(it) }
            .forEach {

                    spin: Spin ->
                CoroutineScope(Dispatchers.Main).launch {
                    actor.send(spin)

                    val duration =
                        System.currentTimeMillis() - start // compute the duration of handling the message sent to actor

                    Log.d(
                        TAG, "[thread=${Thread.currentThread().name}] [${spin.id}]"
                                + " time = $duration"
                    )
                }
            }

        Log.d(
            TAG,
            "time = ${System.currentTimeMillis() - start}"
        )
    }


    @SuppressLint("LogNotTimber")
    private fun secondImplementationOfActor() {
        val start = System.currentTimeMillis() // compute the start time
        val completable = CompletableDeferred<Boolean>()

        (1..101).map { if(it == 101) Done(completable) else Spin(it) }
            .forEach {
                    spin ->
                CoroutineScope(Dispatchers.Main).launch {
                    actor.send(spin)

                    val duration =
                        System.currentTimeMillis() - start // compute the duration of handling the message sent to actor

                    Log.d(
                        TAG, "[thread=${Thread.currentThread().name}]"+ "$spin]  time = $duration")
                }
            }

        CoroutineScope(Dispatchers.Main).launch {
            completable.await()
            Log.d(
                TAG,
                "time = ${System.currentTimeMillis() - start}"
            )
        }


    }

    @ObsoleteCoroutinesApi
    val actor = actor<Action>(Dispatchers.Default, 0) {
        consumeEach { action ->
            when (action) {
                is Spin -> spin(action.id)
            }
        }
    }

    @SuppressLint("LogNotTimber")
    private fun spin(value: Int) {
        val startInMillSeconds = System.currentTimeMillis()
        while (System.currentTimeMillis() - startInMillSeconds < 10) { }
        Log.d(TAG, "[thread=${Thread.currentThread().name}] [$value] processed")
    }
}

