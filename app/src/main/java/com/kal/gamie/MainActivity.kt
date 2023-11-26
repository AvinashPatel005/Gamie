package com.kal.gamie

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.kal.gamie.presentation.LoginPage
import com.kal.gamie.presentation.LoginPage2
import com.kal.gamie.ui.theme.GamieTheme
import com.kal.gamie.viewmodels.MainViewModel
import java.util.Calendar
import java.util.concurrent.TimeUnit

const val TAG="MainActivity"
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>();
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition{viewModel.uid.value==""}
        viewModel.auth.addAuthStateListener {
            viewModel.uid.value=it.currentUser?.uid.toString()
            println(viewModel.auth.uid)
            viewModel.getUser()
        }
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                viewModel.dpUri.value=uri
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
        setContent {
            GamieTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if(viewModel.uid.value=="null"||viewModel.loginProcess2.value!=0||viewModel.loginProcess.value){
                        oneTapClient=Identity.getSignInClient(applicationContext)
                        signInRequest = BeginSignInRequest.builder()
                            .setGoogleIdTokenRequestOptions(
                                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                    .setSupported(true)
                                    .setServerClientId(getString(R.string.client))
                                    .setFilterByAuthorizedAccounts(false)
                                    .build()).build()
                        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                            if (result.resultCode == RESULT_OK) {
                                val signInCredential = oneTapClient.getSignInCredentialFromIntent(result.data)
                                val googleIdToken = signInCredential.googleIdToken
                                val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                                viewModel.auth.signInWithCredential(credential).addOnCompleteListener{task->
                                    if (task.isSuccessful){
                                        viewModel.loginProcess.value=false
                                        viewModel.loginProcess2.value=2
                                        val time = Calendar.getInstance().timeInMillis
                                        Firebase.database.getReference("users/${viewModel.auth.currentUser?.uid}/joined").addValueEventListener(object : ValueEventListener{
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if(snapshot.value==null){
                                                    Firebase.database.getReference("users/${viewModel.auth.currentUser?.uid}/joined").setValue(time)
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                            }
                                        })
                                        Firebase.database.getReference("users/${viewModel.auth.currentUser?.uid}/uid").setValue(viewModel.auth.currentUser?.uid)
                                        Firebase.database.getReference("users/${viewModel.auth.currentUser?.uid}/lastLogin").setValue(time)

                                    }
                                }
                            }
                        }

                        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                                viewModel.auth.signInWithCredential(credential).addOnCompleteListener{task->
                                    if (task.isSuccessful){
                                        viewModel.loginProcess.value=false
                                        viewModel.loginProcess2.value=1
                                        val time = Calendar.getInstance().timeInMillis
                                        Firebase.database.getReference("users/${viewModel.auth.currentUser?.uid}/joined").addValueEventListener(object : ValueEventListener{
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if(snapshot.value==null){
                                                    Firebase.database.getReference("users/${viewModel.auth.currentUser?.uid}/joined").setValue(time)
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                            }
                                        })
                                        Firebase.database.getReference("users/${viewModel.auth.currentUser?.uid}/uid").setValue(viewModel.auth.currentUser?.uid)
                                        Firebase.database.getReference("users/${viewModel.auth.currentUser?.uid}/lastLogin").setValue(time)

                                    }                                 }
                            }

                            override fun onVerificationFailed(e: FirebaseException) {
                                // This callback is invoked in an invalid request for verification is made,
                                // for instance if the the phone number format is not valid.
                                Log.w(TAG, "onVerificationFailed", e)

                                if (e is FirebaseAuthInvalidCredentialsException) {
                                    // Invalid request
                                } else if (e is FirebaseTooManyRequestsException) {
                                    // The SMS quota for the project has been exceeded
                                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                                    // reCAPTCHA verification attempted with null Activity
                                }
                                // Show a message and update the UI
                            }
                            override fun onCodeSent(
                                verificationId: String,
                                token: PhoneAuthProvider.ForceResendingToken,
                            ) {
                                storedVerificationId = verificationId
                                resendToken = token
                                viewModel.getOtp.value=true

                            }
                        }
                        if(viewModel.getOtp.value){
                            var otp by remember {
                                mutableStateOf("")
                            }
                            AlertDialog(modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),onDismissRequest = {
                                viewModel.getOtp.value=false
                                val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, otp)
                                viewModel.auth.signInWithCredential(credential).addOnCompleteListener{task->
                                    if (task.isSuccessful){
                                        viewModel.loginProcess.value=false
                                        val time = Calendar.getInstance().timeInMillis
                                        Firebase.database.getReference("users/${viewModel.auth.currentUser?.uid}/joined").addValueEventListener(object : ValueEventListener{
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if(snapshot.value==null){
                                                    Firebase.database.getReference("users/${viewModel.auth.currentUser?.uid}/joined").setValue(time)
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                            }
                                        })
                                        Firebase.database.getReference("users/${viewModel.auth.currentUser?.uid}/uid").setValue(viewModel.auth.currentUser?.uid)
                                        Firebase.database.getReference("users/${viewModel.auth.currentUser?.uid}/lastLogin").setValue(time)
                                    }
                                    else{
                                        Log.d(TAG, "onCodeSent: ${task.exception}")
                                    }
                                }
                            }) {
                                OutlinedTextField(value = otp, onValueChange ={
                                    otp=it
                                } )
                            }
                        }
                        if(viewModel.loginProcess2.value==0){
                            LoginPage(viewModel,modifier = Modifier){
                                viewModel.loginProcess.value=true
                                if(it=="google"){
                                    oneTapClient.beginSignIn(signInRequest).addOnCompleteListener{ task->
                                        if(task.isSuccessful){
                                            val intent = IntentSenderRequest.Builder(task.result.pendingIntent.intentSender).build()
                                            launcher.launch(intent)
                                        }
                                        else{
                                            viewModel.loginProcess.value=false
                                        }
                                    }
                                }
                                else{
                                    val options = PhoneAuthOptions.newBuilder(viewModel.auth)
                                        .setPhoneNumber(it) // Phone number to verify
                                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                        .setActivity(this) // Activity (for callback binding)
                                        .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                                        .build()
                                    PhoneAuthProvider.verifyPhoneNumber(options)
                                }
                            }

                        }
                        else if (viewModel.loginProcess2.value==1){
                            
                        }
                        else{

                            LoginPage2(viewModel, pickMedia)
                        }
                    }
                    else{
                        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.End){
                            Box(){
                                Row(
                                    Modifier
                                        .background(MaterialTheme.colorScheme.primary)
                                        .fillMaxWidth()
                                        .padding(
                                            top = 8.dp,
                                            bottom = 8.dp,
                                            start = 10.dp,
                                            end = 70.dp
                                        ), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
                                    Text(text = "Gamie", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, fontSize = 24.sp)
                                    Row ( verticalAlignment = Alignment.CenterVertically){
                                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onPrimary)
                                    }

                                }

                                GlideImage(model = "https://firebasestorage.googleapis.com/v0/b/gamie-35ab3.appspot.com/o/${viewModel.uid.value}%2Fdp.png?alt=media&toke=${viewModel.dpV}",contentDescription = null, modifier = Modifier
                                    .padding(end = 10.dp)
                                    .border(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(100)
                                    )
                                    .padding(2.dp)
                                    .size(50.dp)
                                    .align(Alignment.TopEnd)
                                    .clip(RoundedCornerShape(100))

                                    .background(Color.White.copy(alpha = 0.2f))
                                    .shadow(10.dp, RoundedCornerShape(100))
                                    .clickable {
                                        viewModel.menu.value = !viewModel.menu.value
                                    }
                                ){
                                    it.placeholder(R.drawable.user).centerCrop()
                                }
                            }
                            if(viewModel.menu.value){
                                Box(modifier = Modifier
                                    .padding(end = 4.dp, top = 4.dp)
                                    .clip(RoundedCornerShape(10))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .fillMaxWidth(0.4f)
                                    .height(100.dp).padding(10.dp)){
                                    Text(text ="SignOut", modifier = Modifier.clickable {
                                        viewModel.auth.signOut()
                                        viewModel.dpUri.value=null
                                        viewModel.menu.value=false
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
