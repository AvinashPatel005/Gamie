package com.kal.gamie.viewmodels

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.getString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.kal.gamie.MainActivity
import com.kal.gamie.R
import com.kal.gamie.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

class LoginViewModel:ViewModel() {
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    var storedVerificationId: String? = ""
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var launcher: ActivityResultLauncher<IntentSenderRequest>

    var loginMethod:String="";
    fun initializeGoogleAgents(applicationContext: Context) {
        oneTapClient= Identity.getSignInClient(applicationContext)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(applicationContext,R.string.client))
                    .setFilterByAuthorizedAccounts(false)
                    .build()).build()
    }
    fun googleSignInCallback(viewModel: MainViewModel, mainActivity: MainActivity) {
        launcher = mainActivity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){ result->
            if (result.resultCode == ComponentActivity.RESULT_OK) {
                val signInCredential =oneTapClient.getSignInCredentialFromIntent(result.data)
                val googleIdToken = signInCredential.googleIdToken
                val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                viewModel.auth.signInWithCredential(credential).addOnCompleteListener{task->
                    if (task.isSuccessful){
                        viewModel.loginProcess2.value=2
                        viewModel.loginProcess.value=false
                        val time = Calendar.getInstance().timeInMillis
                        Firebase.database.getReference("users/${viewModel.auth.currentUser?.uid}/joined").addValueEventListener(object :
                            ValueEventListener {
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
                    else {
                        viewModel.loginProcess.value=false
                        viewModel.loginErrorMsg.value= task.exception?.message
                    }
                }
            }
            else{
                viewModel.loginProcess.value=false
                viewModel.loginErrorMsg.value="Something went wrong!"
            }
        }
    }

    fun phoneSignInCallBack(viewModel: MainViewModel) {
        callbacks = object:PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModel.auth.signInWithCredential(credential).addOnCompleteListener{task->
                    if (task.isSuccessful){
                        viewModel.loginProcess2.value=2
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
                }
            }
            override fun onVerificationFailed(e: FirebaseException) {
                viewModel.loginProcess.value=false
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
                viewModel.loginErrorMsg.value=e.message
                println("kullu"+e.message)
            }
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                viewModel.loginProcess.value=false
                storedVerificationId = verificationId
                resendToken = token
                viewModel.loginProcess2.value=1
            }
        }
    }
    fun launchOneTap(viewModel: MainViewModel) {
        viewModelScope.launch(Dispatchers.Default){
            oneTapClient.beginSignIn(signInRequest).addOnCompleteListener{ task->
                if(task.isSuccessful){
                    val intent = IntentSenderRequest.Builder(task.result.pendingIntent.intentSender).build()
                    launcher.launch(intent)
                } else{
                    viewModel.loginErrorMsg.value=task.exception?.message
                    viewModel.loginProcess.value=false
                }
            }
        }
    }
    fun startPhoneAuth(viewModel: MainViewModel, phoneNumber: String, mainActivity: MainActivity) {
        val options = PhoneAuthOptions.newBuilder(viewModel.auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(mainActivity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun resendVerificationCode(viewModel: MainViewModel, phoneNumber: String, mainActivity: MainActivity ) {
        val options = PhoneAuthOptions.newBuilder(viewModel.auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(mainActivity)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(viewModel: MainViewModel, otp: String) {
        viewModel.loginProcess.value=true
        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, otp)
        viewModelScope.launch(Dispatchers.Default){
            viewModel.auth.signInWithCredential(credential).addOnCompleteListener{task->
                if (task.isSuccessful){
                    viewModel.loginProcess.value=false
                    viewModel.loginProcess2.value=2
                    val time = Calendar.getInstance().timeInMillis
                    Firebase.database.getReference("users/${viewModel.auth.currentUser?.uid}/joined").addValueEventListener(object :
                        ValueEventListener {
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
                    viewModel.loginProcess.value=false
                    viewModel.loginErrorMsg.value=task.exception?.localizedMessage
                }
            }
        }
    }
}