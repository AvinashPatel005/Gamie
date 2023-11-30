package com.kal.gamie.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kal.gamie.viewmodels.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginPage(
    viewModel: MainViewModel,
    modifier: Modifier,
    onClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = modifier.fillMaxWidth()
        ){
            var number by remember {
                mutableStateOf("")
            }
            var code by remember {
                mutableStateOf("+91")
            }
            val focus = remember{
                FocusRequester()
            }
            val focusManager = LocalFocusManager.current
            Box(modifier = Modifier
                .padding(top = 90.dp, bottom = 30.dp)
                .border(
                    2.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                    RoundedCornerShape(100)
                )
            ){
                Image(
                    painter = painterResource(id = com.kal.gamie.R.drawable.gamie),
                    contentDescription =null ,Modifier.fillMaxWidth(0.5f),
                    contentScale = ContentScale.FillWidth
                )
                if(viewModel.loginProcess.value) CircularProgressIndicator(modifier=Modifier.fillMaxWidth(0.5f))
            }

            Text(text = "Welcome To Gamie",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(shadow = Shadow(Color.Black, Offset(-2f,-2f),0.4f)),
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Row(
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                OutlinedTextField(value = number,modifier=Modifier.focusRequester(focus),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text(text = "Phone Number", modifier = modifier.alpha(0.4f))},
                    placeholder = { Text(text = "00000 00000")},
                    onValueChange = {
                        number=it
                    },
                    singleLine = true,
                    leadingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(4.dp))
                            BasicTextField(value = code, onValueChange = {
                                if(it.length in 1..5) code=it
                            },Modifier.width(40.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), textStyle = TextStyle.Default.copy(MaterialTheme.colorScheme.onPrimaryContainer))
                            Box(
                                modifier = Modifier
                                    .height(30.dp)
                                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                                    .width(1.dp)
                            )
                        }
                    })
                Spacer(modifier = Modifier.height(4.dp))
            }
            IconButton(
                onClick = {
                    focusManager.clearFocus()
                    onClick(code+number)
                },
                enabled = number.length>=10,
                colors = IconButtonColors(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    MaterialTheme.colorScheme.onPrimaryContainer),
                modifier = Modifier.size(46.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription =null,modifier = Modifier.size(30.dp))
            }
            Text(text = "OR", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))
            IconButton (onClick = {
                onClick("google")
            }) {
                Image(painter = painterResource(id = com.kal.gamie.R.drawable.google), contentDescription =null)
            }
        }
        Column (Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
            ){
            Text(text = "@GamieTD",
                Modifier
                    .alpha(0.3f)
                    .padding(vertical = 10.dp))
            AnimatedVisibility(visible =viewModel.loginErrorMsg.value!=null ) {
                Snackbar {
                    (if(viewModel.loginErrorMsg.value!=null)viewModel.loginErrorMsg.value else "")?.let { Text(text = it) }
                }
            }
            LaunchedEffect(key1 = viewModel.loginErrorMsg.value){
                delay(3000)
                viewModel.loginErrorMsg.value=null
            }

        }
    }
}