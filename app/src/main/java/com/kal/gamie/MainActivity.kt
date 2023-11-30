package com.kal.gamie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kal.gamie.data.User
import com.kal.gamie.presentation.HomeScreen
import com.kal.gamie.presentation.LoginPage
import com.kal.gamie.presentation.LoginPage2
import com.kal.gamie.presentation.OtpPage
import com.kal.gamie.ui.theme.GamieTheme
import com.kal.gamie.viewmodels.LoginViewModel
import com.kal.gamie.viewmodels.MainViewModel

const val TAG="MainActivity"
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private val lvm by viewModels<LoginViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            setKeepOnScreenCondition{(viewModel.uid.value=="" || viewModel.user.value==User()) && viewModel.uid.value!="null"}
        }
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                viewModel.dpUri.value=uri
            }
        }
        lvm.googleSignInCallback(viewModel,this)
        setContent {
            GamieTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if(viewModel.uid.value=="null"||viewModel.loginProcess2.value!=0||viewModel.loginProcess.value){
                        LaunchedEffect(key1 = true){
                            lvm.initializeGoogleAgents(applicationContext)
                            lvm.phoneSignInCallBack(viewModel)
                        }

                        when (viewModel.loginProcess2.value) {
                            0 -> {
                                LoginPage(viewModel,modifier = Modifier){login->
                                    viewModel.loginProcess.value=true
                                    lvm.loginMethod=login
                                    if(login=="google"){
                                        lvm.launchOneTap(viewModel)
                                    } else{
                                        lvm.startPhoneAuth(viewModel,login,this)
                                    }
                                }
                            }
                            1 -> {
                                OtpPage(viewModel){click->
                                    if(click=="resend"){
                                        lvm.resendVerificationCode(viewModel,lvm.loginMethod,this)
                                    }
                                    else{
                                        lvm.verifyOtp(viewModel,click)
                                    }
                                }
                            }
                            else -> {
                                LoginPage2(viewModel,pickMedia)
                            }
                        }
                    }
                    else{
                        HomeScreen(viewModel)
                    }
                }
            }
        }
    }
}
