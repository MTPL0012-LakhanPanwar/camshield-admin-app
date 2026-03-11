package com.security.cameralockfacility.ui

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.security.cameralockfacility.modal.ApiResult
import com.security.cameralockfacility.modal.FacilityData
import com.security.cameralockfacility.modal.QRData
import com.security.cameralockfacility.modal.QRPair
import com.security.cameralockfacility.viewmodel.FacilityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val QrBgDark = Color(0xFF0B101F)
private val QrCardBg = Color(0xFF161C2C)
private val QrAccentBlue = Color(0xFF2196F3)
private val QrTextGray = Color(0xFF8A92A6)
private val QrStatusRed = Color(0xFFEF5350)

enum class QRType { ENTRY, EXIT }

@Composable
fun QRCodeDialog(
    facility: FacilityData,
    viewModel: FacilityViewModel,
    focus: QRType? = null,
    onDismiss: () -> Unit,
    showSnackbar: (String) -> Unit
) {
    val qrState by viewModel.qrState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(facility.id) {
        viewModel.loadQRCodes(facility.id)
    }

    Dialog(
        onDismissRequest = {
            viewModel.resetQR()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = QrCardBg)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    "QR Codes",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    facility.name,
                    color = QrAccentBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(color = Color(0xFF2A3245))

                when (val state = qrState) {
                    is ApiResult.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = QrAccentBlue)
                        }
                    }

                    is ApiResult.Error -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Text(
                                state.message,
                                color = QrStatusRed,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = { viewModel.loadQRCodes(facility.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = QrAccentBlue)
                            ) { Text("Retry") }
                        }
                    }

                    is ApiResult.Success -> {
                        val qrPair = state.data
                        val requestedQrs = when (focus) {
                            QRType.ENTRY -> listOfNotNull(qrPair.entry)
                            QRType.EXIT -> listOfNotNull(qrPair.exit)
                            null -> listOfNotNull(qrPair.entry, qrPair.exit)
                        }

                        if (requestedQrs.isEmpty()) {
                            Text(
                                "QR code not available for this selection.",
                                color = QrStatusRed,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            val allBitmaps = remember { mutableStateMapOf<String, Bitmap>() }

                            // Generate only the requested QR bitmaps
                            LaunchedEffect(qrPair, focus) {
                                requestedQrs.forEach { qr ->
                                    val content = qr.value.ifBlank { qr.id }
                                    if (content.isNotBlank()) {
                                        val bmp = withContext(Dispatchers.Default) {
                                            generateQRBitmap(content)
                                        }
                                        if (bmp != null) allBitmaps[qr.id] = bmp
                                    }
                                }
                            }

                            fun downloadBitmap(bitmap: Bitmap, filename: String) {
                                scope.launch {
                                    val success = withContext(Dispatchers.IO) {
                                        saveQRToDownloads(context, bitmap, filename)
                                    }
                                    showSnackbar(
                                        if (success) "\"$filename\" saved to Downloads"
                                        else "Failed to save QR code"
                                    )
                                }
                            }

                            requestedQrs.forEachIndexed { index, qrData ->
                                val isEntry = qrData.id == qrPair.entry?.id
                                val label = if (isEntry) "Entry QR Code" else "Exit QR Code"
                                val filename = if (isEntry) "${facility.name}_entry_qr" else "${facility.name}_exit_qr"
                                QRCodeSection(
                                    label = label,
                                    qrData = qrData,
                                    bitmap = allBitmaps[qrData.id],
                                    onDownload = { bmp -> downloadBitmap(bmp, filename) }
                                )
                                if (index < requestedQrs.lastIndex && focus == null) {
                                    HorizontalDivider(color = Color(0xFF2A3245))
                                }
                            }

                            // Download Both button (only when showing both)
                            val entryBmp = qrPair.entry?.id?.let { allBitmaps[it] }
                            val exitBmp = qrPair.exit?.id?.let { allBitmaps[it] }
                            if (focus == null && entryBmp != null && exitBmp != null) {
                                Button(
                                    onClick = {
                                        downloadBitmap(entryBmp, "${facility.name}_entry_qr")
                                        downloadBitmap(exitBmp, "${facility.name}_exit_qr")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = QrAccentBlue),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Download,
                                        null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Download Both QR Codes", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    null -> {}
                }

                TextButton(
                    onClick = {
                        viewModel.resetQR()
                        onDismiss()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close", color = QrTextGray)
                }
            }
        }
    }
}

@Composable
private fun QRCodeSection(
    label: String,
    qrData: QRData,
    bitmap: Bitmap?,
    onDownload: (Bitmap) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            label,
            color = QrAccentBlue,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
        if (bitmap != null) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color(0xFF0D1426), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = QrAccentBlue,
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 2.dp
                )
            }
        }
        if (qrData.name.isNotBlank()) {
            Text(qrData.name, color = QrTextGray, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
        if (bitmap != null) {
            OutlinedButton(
                onClick = { onDownload(bitmap) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = QrAccentBlue),
                border = androidx.compose.foundation.BorderStroke(1.dp, QrAccentBlue)
            ) {
                Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Download", fontSize = 13.sp)
            }
        }
    }
}

private fun generateQRBitmap(content: String, size: Int = 512): Bitmap? {
    return try {
        val hints = hashMapOf<EncodeHintType, Any>(EncodeHintType.MARGIN to 1)
        val bitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also { bmp ->
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
                }
            }
        }
    } catch (e: Exception) {
        null
    }
}

private fun saveQRToDownloads(context: Context, bitmap: Bitmap, filename: String): Boolean {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$filename.png")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return false
            context.contentResolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            true
        } else {
            @Suppress("DEPRECATION")
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            dir.mkdirs()
            val file = java.io.File(dir, "$filename.png")
            java.io.FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            true
        }
    } catch (e: Exception) {
        false
    }
}
