package com.security.cameralockfacility.ui

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.security.cameralockfacility.R
import com.security.cameralockfacility.activity.CreateUpdateFacility
import com.security.cameralockfacility.modal.Facility

@Composable
fun FacilityContent() {
    // For now, we use a dummy object to see the design
    val dummyFacility = Facility("1", "Automobile warehouse", "Active", 20)
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
// 1. Action Header Component
        FacilityActionHeader(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            onCreateClick = {
                context.startActivity(
                    Intent(
                        context,
                        CreateUpdateFacility::class.java
                    )
                )
            }
        )
        FacilityCard(facility = dummyFacility, onUpdate = {
            context.startActivity(
                Intent(
                    context,
                    CreateUpdateFacility::class.java
                )
            )
        }, onDelete = {
            // 2. LOGIC: Just flip the switch here
            showDeleteDialog = true
        })
    }
    if (showDeleteDialog) {
        OpenDeleteConfirmationDialog(
            onDismissRequest = { showDeleteDialog = false }, // Hide when canceled
            onConfirmDelete = {
                // Perform delete logic here
                showDeleteDialog = false
            }
        )
    }
}
@Composable
fun OpenDeleteConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = Color(0xFF161C2C), // Dark Navy matching your card theme
        title = {
            Text(
                text = "Delete Facility",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete this facility?",
                color = Color.Gray,
                fontSize = 16.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)), // Danger Red
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Delete", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
@Composable
fun FacilityActionHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCreateClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search Facility...", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF161C2C),
                unfocusedContainerColor = Color(0xFF161C2C),
                focusedBorderColor = Color(0xFF2196F3),
                unfocusedBorderColor = Color.Transparent
            )
        )

        // Create Button
        Button(
            onClick = onCreateClick,
            modifier = Modifier.height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Create", fontSize = 14.sp)
        }
    }
}

@Composable
fun FacilityCard(
    facility: Facility,
    onUpdate: () -> Unit,
    onDelete: () -> Unit
) {
    var selectedQrLabel by remember { mutableStateOf<String?>(null) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161C2C)) // Slightly lighter than bg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Info
            Text(
                text = facility.name,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp)
            )

            Row(modifier = Modifier.padding(top = 10.dp)) {
                Text(text = "Status: ", color = Color.Gray, fontSize = 14.sp)
                Text(
                    text = facility.status,
                    color = Color(0xFF4CAF50),
                    fontSize = 14.sp
                ) // Green for Active
            }

            Row(modifier = Modifier.padding(top = 8.dp)) {
                Text(text = "Device Active Count: ", color = Color.Gray, fontSize = 14.sp)
                Text(text = "${facility.deviceCount}", color = Color.White, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // QR Sections
            // QR and Download Sections
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp), // Fixed height helps see the centering
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically // THIS centers all columns in the row
            ) {
                QRItem(
                    label = "Entry QR",
                    iconRes = R.drawable.icon_dummy_qr,
                    iconSize = 60.dp,
                    onClick = { selectedQrLabel = "Entry QR" }
                )

                QRItem(
                    label = "Exit QR",
                    iconRes = R.drawable.icon_dummy_qr,
                    iconSize = 60.dp,
                    onClick = { selectedQrLabel = "Exit QR" }
                )

                 QRItem(
                    label = "Download",
                    iconRes = R.drawable.icon_download,
                    iconSize = 25.dp, // Smaller icon, but now centered vertically
                    onClick = { /* Do nothing for now */ }
                )
            }
            if (selectedQrLabel != null) {
                ShowQrPreviewDialog(
                    label = selectedQrLabel!!,
                    onDismiss = { selectedQrLabel = null }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = {
                        // Put logic here (e.g., calling the delete function)
                        onDelete()
                    }
                ) {
                    // Put UI components here
                    Text("Delete", color = Color(0xFFEF5350))
                }
                Button(
                    onClick = onUpdate,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Update", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun QRItem(
    label: String,
    iconRes: Int,
    iconSize: Dp = 40.dp,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, // Centers icon and text
        verticalArrangement = Arrangement.Center,           // Centers content vertically
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(iconSize),
            tint = Color.White
        )
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun ShowQrPreviewDialog(label: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161C2C),
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF2196F3))
            }
        },
        title = {
            Text(text = label, color = Color.White, fontSize = 18.sp)
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Display the actual dummy QR image here
                Icon(
                    painter = painterResource(id = R.drawable.dummy_qr),
                    contentDescription = "QR Preview",
                    modifier = Modifier.size(200.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Scan this code at the facility $label", color = Color.Gray, fontSize = 14.sp)
            }
        }
    )
}

@Preview(showBackground = true, device = "spec:width=411dp,height=1000dp")
@Composable
fun FacilityCardPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B101F))
    ) {
        FacilityContent()
    }
}
@Preview
@Composable
fun DeleteDialogPreview() {
    MaterialTheme {
        // We use a Box with a dark background to simulate the app state
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0B101F))) {
            OpenDeleteConfirmationDialog(
                onDismissRequest = {},
                onConfirmDelete = {}
            )
        }
    }
}