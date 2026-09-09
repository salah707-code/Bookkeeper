package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.entity.UserProfileEntity
import com.example.ui.components.MizanTopBar
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

data class CurrencyOption(val code: String, val symbolAr: String, val symbolEn: String, val nameAr: String, val nameEn: String)

val supportedCurrencies = listOf(
    CurrencyOption("SAR", "ر.س", "SAR", "ريال سعودي", "Saudi Riyal"),
    CurrencyOption("AED", "د.إ", "AED", "درهم إماراتي", "UAE Dirham"),
    CurrencyOption("KWD", "د.ك", "KWD", "دينار كويتي", "Kuwaiti Dinar"),
    CurrencyOption("QAR", "ر.ق", "QAR", "ريال قطري", "Qatari Riyal"),
    CurrencyOption("BHD", "د.ب", "BHD", "دينار بحريني", "Bahraini Dinar"),
    CurrencyOption("OMR", "ر.ع", "OMR", "ريال عماني", "Omani Rial"),
    CurrencyOption("EGP", "ج.م", "EGP", "جنيه مصري", "Egyptian Pound"),
    CurrencyOption("JOD", "د.أ", "JOD", "دينار أردني", "Jordanian Dinar"),
    CurrencyOption("USD", "$", "$", "دولار أمريكي", "US Dollar"),
    CurrencyOption("EUR", "€", "€", "يورو", "Euro"),
    CurrencyOption("GBP", "£", "£", "جنيه إسترليني", "British Pound")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfileEntity,
    isArabic: Boolean,
    onBack: () -> Unit,
    onSaveProfile: (name: String, phone: String, email: String, currencyCode: String, currencySymbol: String, photoUri: String?, bio: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var name by remember(userProfile) { mutableStateOf(userProfile.name) }
    var phone by remember(userProfile) { mutableStateOf(userProfile.phone) }
    var email by remember(userProfile) { mutableStateOf(userProfile.email) }
    var bio by remember(userProfile) { mutableStateOf(userProfile.bio) }
    var photoUriString by remember(userProfile) { mutableStateOf(userProfile.photoUri) }

    var selectedCurrency by remember(userProfile) {
        mutableStateOf(
            supportedCurrencies.firstOrNull { it.code == userProfile.currencyCode }
                ?: supportedCurrencies[0]
        )
    }
    var currencyDropdownExpanded by remember { mutableStateOf(false) }

    // Photo picker launcher (Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            try {
                // Copy photo to internal app storage
                val inputStream = context.contentResolver.openInputStream(sourceUri)
                val file = File(context.filesDir, "profile_avatar_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                photoUriString = file.absolutePath
            } catch (e: Exception) {
                photoUriString = sourceUri.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            MizanTopBar(
                title = if (isArabic) "بياناتي الشخصية" else "My Profile",
                onBack = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Profile Photo Center
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("profile_photo_box"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!photoUriString.isNullOrEmpty()) {
                            AsyncImage(
                                model = photoUriString,
                                contentDescription = "الصورة الشخصية",
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(60.dp)
                            )
                        }

                        // Camera overlay badge
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "تغيير الصورة",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isArabic) "انقر لتغيير الصورة الشخصية" else "Tap to change profile picture",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Input Fields Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(if (isArabic) "الاسم الكامل" else "Full Name") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_name_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Phone
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(if (isArabic) "رقم الهاتف" else "Phone Number") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_phone_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(if (isArabic) "البريد الإلكتروني" else "Email") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Email, contentDescription = null)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_email_input")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Currency Selector Dropdown
                        ExposedDropdownMenuBox(
                            expanded = currencyDropdownExpanded,
                            onExpandedChange = { currencyDropdownExpanded = !currencyDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = if (isArabic) "${selectedCurrency.nameAr} (${selectedCurrency.symbolAr})"
                                else "${selectedCurrency.nameEn} (${selectedCurrency.symbolEn})",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(if (isArabic) "العملة الأساسية" else "Base Currency") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null)
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyDropdownExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("currency_selector")
                            )

                            ExposedDropdownMenu(
                                expanded = currencyDropdownExpanded,
                                onDismissRequest = { currencyDropdownExpanded = false }
                            ) {
                                for (curr in supportedCurrencies) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (isArabic) "${curr.nameAr} (${curr.symbolAr} - ${curr.code})"
                                                else "${curr.nameEn} (${curr.symbolEn} - ${curr.code})"
                                            )
                                        },
                                        onClick = {
                                            selectedCurrency = curr
                                            currencyDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bio / Financial Motto
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text(if (isArabic) "عبارتك المالية أو شعارك" else "Personal Motto") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.FormatQuote, contentDescription = null)
                            },
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_bio_input")
                        )
                    }
                }
            }

            // Save Button
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        val symbol = if (isArabic) selectedCurrency.symbolAr else selectedCurrency.symbolEn
                        onSaveProfile(
                            name.trim(),
                            phone.trim(),
                            email.trim(),
                            selectedCurrency.code,
                            symbol,
                            photoUriString,
                            bio.trim()
                        )
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = if (isArabic) "تم حفظ البيانات بنجاح" else "Profile saved successfully"
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_profile_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "حفظ التغييرات" else "Save Changes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
