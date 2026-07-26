package com.example.kwachawise.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.*

@Composable
fun GenerateDocumentDialog(
    onDismiss: () -> Unit,
    onGenerate: (startDate: Long, endDate: Long, title: String) -> Unit
) {
    var selectedOption by remember { mutableStateOf(0) } // 0: Last 30 days, 1: Last 90 days
    var title by remember { mutableStateOf("Bank Report - ${System.currentTimeMillis()}") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Generate Bank Document",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(text = "Select Period", style = MaterialTheme.typography.labelLarge)

                Column {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = selectedOption == 0, onClick = { selectedOption = 0 })
                        Text("Last 30 Days")
                    }
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = selectedOption == 1, onClick = { selectedOption = 1 })
                        Text("Last 90 Days")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(onClick = {
                        val calendar = Calendar.getInstance()
                        val endDate = calendar.timeInMillis
                        val days = if (selectedOption == 0) 30 else 90
                        calendar.add(Calendar.DAY_OF_YEAR, -days)
                        val startDate = calendar.timeInMillis
                        onGenerate(startDate, endDate, title)
                    }) {
                        Text("Generate")
                    }
                }
            }
        }
    }
}
