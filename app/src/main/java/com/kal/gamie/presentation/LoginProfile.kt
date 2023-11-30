package com.kal.gamie.presentation

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import com.kal.gamie.R
import com.kal.gamie.viewmodels.MainViewModel

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LoginPage2(viewModel: MainViewModel, pickMedia: ActivityResultLauncher<PickVisualMediaRequest>) {
    var name by remember {
        mutableStateOf("")
    }
    LaunchedEffect(key1 = viewModel.user.value.name){
        name=viewModel.user.value.name
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
        ){
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier
                        .alpha(0.6f)
                        .size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Profile Setup",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 55.dp, bottom = 25.dp))
            {
                GlideImage(
                    model = if (viewModel.dpUri.value == null) "https://firebasestorage.googleapis.com/v0/b/gamie-c8f3c.appspot.com/o/${viewModel.uid.value}%2Fdp.png?alt=media&token=${viewModel.user.value.dpV}" else viewModel.dpUri.value,
                    contentDescription = null,
                    modifier = Modifier
                        .border(1.dp,color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), RoundedCornerShape(100))
                        .fillMaxWidth(0.5f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(100))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                ){
                    it.placeholder(R.drawable.user).centerCrop()
                }
                if(viewModel.dpUploading.value) CircularProgressIndicator(modifier=Modifier.fillMaxWidth(0.5f))
            }
            OutlinedTextField(
                value =name,
                onValueChange = { name=it },
                placeholder = {
                    Text(
                        text = "Name",
                        modifier = Modifier.alpha(0.3f)
                    )
                },
                singleLine = true,
                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null)}
            )
            Spacer(modifier = Modifier.height(20.dp))
            IconButton(
                enabled = !viewModel.dpUploading.value&&name!="",
                onClick = {
                    val storageRef = FirebaseStorage.getInstance().reference
                    Firebase.database.getReference("users/${viewModel.uid.value}/name").setValue(name)

                    val dpRef = viewModel.dpUri.value?.let {
                        storageRef.child("${viewModel.uid.value}/dp.png").putFile(
                            it
                        )
                    }
                    if(dpRef==null) viewModel.loginProcess2.value=0
                    else {
                        viewModel.dpUploading.value=true
                        Firebase.database.getReference("users/${viewModel.uid.value}/dpV").setValue(viewModel.user.value.dpV+1L)
                    }
                    dpRef?.addOnSuccessListener {
                        viewModel.loginProcess2.value=0
                        viewModel.dpUploading.value=false
                        viewModel.dpUri.value=null
                    }
                },
                colors = IconButtonColors(
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Done",
                    modifier = Modifier.size(25.dp))
            }
        }

        Image(
            painter = painterResource(id = R.drawable.gamie),
            contentDescription = null ,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .alpha(0.05f)
                .fillMaxWidth(0.3f)
        )
        Text(
            text = "@GamieTD",
            modifier = Modifier
                .alpha(0.3f)
                .padding(vertical = 10.dp))
    }

}