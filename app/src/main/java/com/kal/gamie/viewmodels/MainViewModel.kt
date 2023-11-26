package com.kal.gamie.viewmodels

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainViewModel : ViewModel() {
    var auth = Firebase.auth
    var uid : MutableState<String> = mutableStateOf("")
    var loginProcess : MutableState<Boolean> = mutableStateOf(false)
    var loginProcess2 : MutableState<Int> = mutableIntStateOf(0)
    var getOtp:MutableState<Boolean> = mutableStateOf(false)
    var dpUri:MutableState<Uri?> = mutableStateOf(null)
    var dpV:MutableState<Long> = mutableLongStateOf(0L)
    var name:MutableState<String> = mutableStateOf("")
    var menu : MutableState<Boolean> = mutableStateOf(false)
    var dpUploading : MutableState<Boolean> = mutableStateOf(false)
    init {

    }

    fun getUser(){
        com.google.firebase.ktx.Firebase.database.getReference("users/${uid.value}/name").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                name.value=if(snapshot.value==null) "" else snapshot.value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        com.google.firebase.ktx.Firebase.database.getReference("users/${uid.value}/dpV").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dpV.value= if(snapshot.value!=null) snapshot.value as Long else 0L
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

}