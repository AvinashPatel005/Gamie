package com.kal.gamie.data

data class User(
    val name:String="",
    val uid:String="",
    val lastLogin: Long=0L,
    val joined: Long=0L,
    val dpV: Long=0L,
)
