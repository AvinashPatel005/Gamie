package com.kal.gamie.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.kal.gamie.R
import com.kal.gamie.viewmodels.MainViewModel

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.End){
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "Gamie",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp
                )
                Row (
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    GlideImage(
                        model = "https://firebasestorage.googleapis.com/v0/b/gamie-c8f3c.appspot.com/o/${viewModel.uid.value}%2Fdp.png?alt=media&toke=${viewModel.user.value}",
                        contentDescription = null,
                        modifier = Modifier
                            .border(1.dp,MaterialTheme.colorScheme.primary,RoundedCornerShape(100))
                            .padding(1.dp)
                            .size(40.dp)
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
            }
        HorizontalDivider(thickness =0.5.dp)
        if(viewModel.menu.value){
            Box(modifier = Modifier
                .padding(end = 4.dp, top = 4.dp)
                .clip(RoundedCornerShape(10))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .fillMaxWidth(0.4f)
                .height(100.dp)
                .padding(10.dp)){
                Text(text ="SignOut", modifier = Modifier.clickable {
                    viewModel.signOut()
                })
            }
        }
    }
}