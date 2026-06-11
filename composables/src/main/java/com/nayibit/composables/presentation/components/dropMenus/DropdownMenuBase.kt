package com.nayibit.composables.presentation.components.dropMenus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownMenuBase(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    labelSelector: (T) -> String,
    modifier: Modifier = Modifier,
    label: String = "Select Item",
    enabled: Boolean = true,
    searchable: Boolean = false,
    nextFocusRequester: FocusRequester? = null
) {
    val focusManager = LocalFocusManager.current
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    val displayedItems = remember(searchQuery.text, items) {
        if (searchable && searchQuery.text.isNotEmpty()) {
            items.filter { labelSelector(it).contains(searchQuery.text, ignoreCase = true) }
        } else {
            items
        }
    }

    val colors: TextFieldColors = TextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        errorTextColor = MaterialTheme.colorScheme.onSurface,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { next ->
            if (!enabled) return@ExposedDropdownMenuBox
            if (next && searchable) {
                val text = selectedItem?.let { labelSelector(it) } ?: ""
                searchQuery = TextFieldValue(text, selection = TextRange(text.length))
            }
            expanded = next
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = if (searchable && expanded) searchQuery
                    else TextFieldValue(selectedItem?.let { labelSelector(it) } ?: ""),
            onValueChange = { input ->
                if (searchable) searchQuery = input
            },
            readOnly = !searchable,
            label = { Text(label) },
            colors = colors,
            modifier = Modifier
                .menuAnchor(
                    type = if (searchable) MenuAnchorType.PrimaryEditable
                           else MenuAnchorType.PrimaryNotEditable,
                    enabled = enabled
                )
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused && searchable && !expanded) {
                        val text = selectedItem?.let { labelSelector(it) } ?: ""
                        searchQuery = TextFieldValue(text, selection = TextRange(text.length))
                        expanded = true
                    }
                }
                .then(
                    if (!searchable) Modifier.clickable(enabled = enabled) { expanded = !expanded }
                    else Modifier
                ),
            enabled = enabled
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                searchQuery = TextFieldValue("")
                focusManager.clearFocus()
            },
            modifier = Modifier.exposedDropdownSize()
        ) {
            displayedItems.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = labelSelector(item),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    },
                    onClick = {
                        onItemSelected(item)
                        searchQuery = TextFieldValue("")
                        expanded = false
                        if (nextFocusRequester != null) nextFocusRequester.requestFocus()
                        else focusManager.clearFocus()
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
