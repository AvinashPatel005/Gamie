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
import com.kal.gamie.data.User

class MainViewModel : ViewModel() {
    var auth = Firebase.auth
    var uid : MutableState<String> = mutableStateOf("")
    var user:MutableState<User> = mutableStateOf(User())

    var loginProcess : MutableState<Boolean> = mutableStateOf(false)
    var loginProcess2 : MutableState<Int> = mutableIntStateOf(0)
    var loginErrorMsg : MutableState<String?> = mutableStateOf(null)

    var dpUri:MutableState<Uri?> = mutableStateOf(null)
    var menu : MutableState<Boolean> = mutableStateOf(false)
    var dpUploading : MutableState<Boolean> = mutableStateOf(false)
    init {
        auth.addAuthStateListener {
            uid.value=it.currentUser?.uid.toString()
            getAccount()
        }
    }
    private fun getAccount(){
        if(uid.value!="null" && uid.value!=""){
            val dataRef = Firebase.database.getReference("users/${uid.value}")
            val callback = object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    user.value= snapshot.getValue(User::class.java)!!
                }
                override fun onCancelled(error: DatabaseError) {
                }
            }
            dataRef.addValueEventListener(callback)
        }
        else user.value=User()
    }
    fun signOut(){
        Firebase.auth.signOut()
        menu.value=false
    }

}