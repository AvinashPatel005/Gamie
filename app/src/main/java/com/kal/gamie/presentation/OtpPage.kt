package com.kal.gamie.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kal.gamie.R
import com.kal.gamie.viewmodels.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun OtpPage(viewModel: MainViewModel,onclick:(String)->Unit) {
    val focusRequester = remember {
        FocusRequester()
    }
    Column(modifier = Modifier.fillMaxSize()){
        Spacer(modifier = Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(),verticalAlignment = Alignment.CenterVertically){
            Spacer(modifier = Modifier.width(10.dp))
            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier
                .alpha(0.6f)
                .size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "OTP Verification", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        }

        Column (modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally){
            var otp by remember {
                mutableStateOf("")
            }
            Box(modifier = Modifier
                .padding(top = 55.dp, bottom = 30.dp)
                .border(
                    2.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                    RoundedCornerShape(100)
                )
            ){
                Image(
                    painter = painterResource(id = R.drawable.gamie),
                    contentDescription =null ,Modifier.fillMaxWidth(0.5f),
                    contentScale = ContentScale.FillWidth
                )
                if(viewModel.loginProcess.value) CircularProgressIndicator(modifier=Modifier.fillMaxWidth(0.5f))
            }

            Text(text = "Enter One Time Password", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(10.dp))
            val animate = animateFloatAsState(targetValue = if(viewModel.loginErrorMsg.value == null) 0f else 1f,
                infiniteRepeatable(tween(250, easing = LinearEasing),RepeatMode.Reverse), label = "shakeOTP"
            )

            BasicTextField(modifier = Modifier
                .focusRequester(focusRequester)
                .offset(x = if (viewModel.loginErrorMsg.value != null) (animate.value * 5).dp else 0.dp),
                value = otp,
                onValueChange = {
                    if (it.length <= 6) {
                        otp = it
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword),
                decorationBox = {
                    Row{
                        repeat( 6) { index->
                            val char = when {
                                index >= otp.length -> ""
                                else -> otp[index].toString()
                            }
                            val isFocused = otp.length==index
                            Text(
                                modifier = Modifier
                                    .width(40.dp)
                                    .border(
                                        if (isFocused) 2.dp
                                        else 1.dp,
                                        if (isFocused) Color.White
                                        else {
                                            if (viewModel.loginErrorMsg.value != null) Color.Red else Color.Gray
                                        },
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(2.dp) ,
                                text = char,
                                color = Color.White,
                                fontSize = 50.sp,
                                lineHeight = 60.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier. width (8. dp) )
                        }
                    }
                }
            )
            LaunchedEffect(key1 = true){
                focusRequester.requestFocus()
            }
            Spacer(modifier = Modifier.height(5.dp))
            Row (verticalAlignment = Alignment.CenterVertically){
                CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Auto-Verifying OTP  ", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            var timeLeft by remember { mutableIntStateOf(60) }
            LaunchedEffect(key1 = timeLeft) {
                while (timeLeft > 0) {
                    delay(1000L)
                    timeLeft--
                }
            }

            Button(onClick = {
                             timeLeft=60
                onclick("resend")
            },
                enabled = timeLeft==0) {
                Text(text = if(timeLeft!=0) "Resend OTP in ${timeLeft}s" else "Resend OTP")
            }
            Spacer(modifier = Modifier.height(5.dp))
            IconButton(onClick = {
                onclick(otp)
            }
                ,
                enabled = otp.length>=6,
                colors = IconButtonColors(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    MaterialTheme.colorScheme.onPrimaryContainer),
                modifier = Modifier.size(46.dp)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription =null,modifier = Modifier.size(30.dp))
            }
            LaunchedEffect(key1 = viewModel.loginErrorMsg.value){
                delay(1000)
                viewModel.loginErrorMsg.value=null
            }
        }
    }
}