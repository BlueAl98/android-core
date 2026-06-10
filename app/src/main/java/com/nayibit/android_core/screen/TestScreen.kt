package com.nayibit.android_core.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nayibit.composables.presentation.components.dialogs.BaseDialog
import com.nayibit.composables.presentation.components.textFields.TextFieldBase
import androidx.compose.ui.text.input.ImeAction
@Composable
fun TestScreen(){
    var show by remember { mutableStateOf(false) }
    var textField by remember { mutableStateOf("") }
    var textRestriction by remember { mutableStateOf(false) }

    Column (Modifier.fillMaxSize()) {
        TextFieldBase(
            textRestriction = textRestriction,
            showCharCounter = true,
            value = textField,
            label = "Najib",
            unfocusedColor = Color.Gray,
            onValueChange = { textField = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        )
        TextFieldBase(
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            showCharCounter = true,
            label = "Segundo",
            unfocusedColor = Color.Gray,
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        )
    }

}

@Preview(showBackground = true)
@Composable
fun TestScreenPreview(){
    TestScreen()
}