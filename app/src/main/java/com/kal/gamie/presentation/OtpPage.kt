package com.kal.gamie.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun OtpPage() {
    Row {
        OutlinedTextField(value = "", onValueChange = {})
        OutlinedTextField(value = "", onValueChange = {})
        OutlinedTextField(value = "", onValueChange = {})
        OutlinedTextField(value = "", onValueChange = {})
    }
}