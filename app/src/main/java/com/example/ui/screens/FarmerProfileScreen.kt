package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.CropType
import com.example.models.FarmerProfile
import com.example.models.Language
import com.example.models.UserRole
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.KrishiQueueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerProfileScreen(
    viewModel: KrishiQueueViewModel,
    modifier: Modifier = Modifier
) {
    val lang by viewModel.currentLanguage.collectAsState()
    val isHindi = lang == Language.HINDI
    val farmer by viewModel.farmerProfile.collectAsState()
    val centres by viewModel.centres.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val saveStatus by viewModel.profileSaveStatus.collectAsState()

    // Editable Form States
    var registrationId by remember(farmer) { mutableStateOf(farmer.registrationId) }
    var fullName by remember(farmer) { mutableStateOf(farmer.fullName) }
    var phone by remember(farmer) { mutableStateOf(farmer.phone) }
    var email by remember(farmer) { mutableStateOf(farmer.email) }
    var aadhaarLast4 by remember(farmer) { mutableStateOf(farmer.aadhaarLast4) }
    var selectedCentreId by remember(farmer) { mutableStateOf(farmer.preferredCentreId) }
    var village by remember(farmer) { mutableStateOf(farmer.village) }
    var block by remember(farmer) { mutableStateOf(farmer.block) }
    var district by remember(farmer) { mutableStateOf(farmer.district) }
    var state by remember(farmer) { mutableStateOf(farmer.state) }
    var landRecordNo by remember(farmer) { mutableStateOf(farmer.landRecordNo) }
    var landAreaAcresText by remember(farmer) { mutableStateOf(farmer.landAreaAcres.toString()) }
    var selectedCrop by remember(farmer) { mutableStateOf(farmer.primaryCrop) }
    var expectedQuantityText by remember(farmer) { mutableStateOf(farmer.expectedQuantityQuintals.toString()) }
    var bankName by remember(farmer) { mutableStateOf(farmer.bankName) }
    var bankAccountLast4 by remember(farmer) { mutableStateOf(farmer.bankAccountLast4) }
    var ifscCode by remember(farmer) { mutableStateOf(farmer.ifscCode) }

    var isEditing by remember { mutableStateOf(false) }
    var showCentreDropdown by remember { mutableStateOf(false) }
    var showCropDropdown by remember { mutableStateOf(false) }

    val selectedCentreName = centres.firstOrNull { it.id == selectedCentreId }?.name ?: farmer.preferredCentreName

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Room DB Persistence Status Banner
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MintLight),
                border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ForestGreenPrimary.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isHindi) "स्थानीय रूम डेटाबेस (Room DB)" else "Local Room DB Persistence",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "SQLITE",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreenPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = saveStatus ?: if (isHindi) "पंजीकरण व पसंदीदा केंद्र का डेटा सुरक्षित है" else "Farmer ID & Preferred Mandi cached on device",
                                fontSize = 10.5.sp,
                                color = TextSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    Button(
                        onClick = { isEditing = !isEditing },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEditing) HarvestGold else ForestGreenPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Visibility else Icons.Default.Edit,
                            contentDescription = null,
                            tint = if (isEditing) Color.Black else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isEditing) (if (isHindi) "देखें" else "View") else (if (isHindi) "संपादित करें" else "Edit"),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isEditing) Color.Black else Color.White
                        )
                    }
                }
            }
        }

        // Help Chatbot Quick Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, ForestGreenPrimary.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { viewModel.toggleHelpChat(true) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(ForestGreenPrimary, ForestGreenDark)))
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isHindi) "कृषि सहायक (Help Chat Bot)" else "Krishi Sahayak (Help Chat Bot)",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = HarvestGold
                            ) {
                                Text(
                                    text = "VOICE 🎙️",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isHindi) "एमएसपी, स्लॉट व कतार स्थिति बोलकर पूछें व सुनें" else "Ask questions in Hindi/English with voice readout",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = ForestGreenPrimary
                    )
                }
            }
        }

        // Section 1: Registration ID & Preferred Procurement Center
        item {
            Card(
                shape = AppCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "1. पंजीकरण संख्या व पसंदीदा मंडी" else "1. Registration ID & Preferred Mandi",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isEditing) {
                        // Registration ID Input
                        OutlinedTextField(
                            value = registrationId,
                            onValueChange = { registrationId = it },
                            label = { Text(if (isHindi) "किसान पंजीकरण संख्या (Reg ID)" else "Farmer Registration ID") },
                            placeholder = { Text("MFMB-2026-987410") },
                            leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = ForestGreenPrimary) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Preferred Procurement Center Selector
                        ExposedDropdownMenuBox(
                            expanded = showCentreDropdown,
                            onExpandedChange = { showCentreDropdown = !showCentreDropdown },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedCentreName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(if (isHindi) "पसंदीदा खरीद केंद्र (Preferred Mandi)" else "Preferred Procurement Center") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCentreDropdown) },
                                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = ForestGreenPrimary) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = showCentreDropdown,
                                onDismissRequest = { showCentreDropdown = false }
                            ) {
                                centres.forEach { centre ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = if (isHindi) centre.hindiName else centre.name,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.5.sp
                                                )
                                                Text(
                                                    text = "${centre.code} • ${centre.district}, ${centre.state}",
                                                    fontSize = 10.5.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedCentreId = centre.id
                                            showCentreDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        ProfileFieldRow(
                            label = if (isHindi) "किसान पंजीकरण संख्या (Registration ID)" else "Farmer Registration ID",
                            value = farmer.registrationId,
                            icon = Icons.Default.ConfirmationNumber
                        )
                        ProfileFieldRow(
                            label = if (isHindi) "पसंदीदा खरीद केंद्र (Preferred Mandi Center)" else "Preferred Procurement Center",
                            value = farmer.preferredCentreName,
                            icon = Icons.Default.Storefront
                        )
                    }
                }
            }
        }

        // Section 2: Contact Details
        item {
            Card(
                shape = AppCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContactPhone,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "2. संपर्क व व्यक्तिगत विवरण" else "2. Contact & Personal Details",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text(if (isHindi) "पूरा नाम" else "Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text(if (isHindi) "मोबाइल नंबर" else "Phone Number") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.3f),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = aadhaarLast4,
                                onValueChange = { if (it.length <= 4) aadhaarLast4 = it },
                                label = { Text("Aadhaar (4)") },
                                placeholder = { Text("8842") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(0.9f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text(if (isHindi) "ईमेल पता" else "Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        ProfileFieldRow(
                            label = if (isHindi) "किसान का पूरा नाम" else "Farmer Full Name",
                            value = farmer.fullName,
                            icon = Icons.Default.Person
                        )
                        ProfileFieldRow(
                            label = if (isHindi) "मोबाइल फोन नंबर" else "Mobile Phone",
                            value = farmer.phone,
                            icon = Icons.Default.Phone
                        )
                        ProfileFieldRow(
                            label = if (isHindi) "आधार संख्या (अंतिम 4 अंक)" else "Aadhaar Number (Last 4 Digits)",
                            value = "XXXX-XXXX-${farmer.aadhaarLast4}",
                            icon = Icons.Default.Fingerprint
                        )
                        ProfileFieldRow(
                            label = if (isHindi) "ईमेल आईडी" else "Email Address",
                            value = farmer.email,
                            icon = Icons.Default.Email
                        )
                    }
                }
            }
        }

        // Section 3: Farm & Land Details
        item {
            Card(
                shape = AppCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "3. कृषि भूमि व फसल का विवरण" else "3. Farm Land & Crop Records",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = landRecordNo,
                            onValueChange = { landRecordNo = it },
                            label = { Text(if (isHindi) "खसरा / जमाबंदी संख्या" else "Land Record / Khasra No.") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = landAreaAcresText,
                                onValueChange = { landAreaAcresText = it },
                                label = { Text(if (isHindi) "क्षेत्र (एकड़)" else "Area (Acres)") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = expectedQuantityText,
                                onValueChange = { expectedQuantityText = it },
                                label = { Text(if (isHindi) "मात्रा (क्विंटल)" else "Harvest (Qtl)") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Crop Dropdown
                        ExposedDropdownMenuBox(
                            expanded = showCropDropdown,
                            onExpandedChange = { showCropDropdown = !showCropDropdown },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = if (isHindi) selectedCrop.hindiName else selectedCrop.englishName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(if (isHindi) "मुख्य फसल (Primary Crop)" else "Primary Crop") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCropDropdown) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = showCropDropdown,
                                onDismissRequest = { showCropDropdown = false }
                            ) {
                                CropType.entries.forEach { crop ->
                                    DropdownMenuItem(
                                        text = {
                                            Text("${crop.icon} ${if (isHindi) crop.hindiName else crop.englishName} (MSP: ₹${crop.mspPerQuintal})")
                                        },
                                        onClick = {
                                            selectedCrop = crop
                                            showCropDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = village,
                                onValueChange = { village = it },
                                label = { Text(if (isHindi) "गाँव" else "Village") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = district,
                                onValueChange = { district = it },
                                label = { Text(if (isHindi) "जिला" else "District") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    } else {
                        ProfileFieldRow(
                            label = if (isHindi) "भू-अभिलेख संख्या (Khasra No.)" else "Land Record No. (Khasra)",
                            value = farmer.landRecordNo,
                            icon = Icons.Default.Landscape
                        )
                        ProfileFieldRow(
                            label = if (isHindi) "कुल कृषि क्षेत्र व स्थान" else "Total Land Area & Location",
                            value = "${farmer.landAreaAcres} Acres • ${farmer.village}, ${farmer.district}, ${farmer.state}",
                            icon = Icons.Default.LocationOn
                        )
                        ProfileFieldRow(
                            label = if (isHindi) "मुख्य पंजीकृत फसल" else "Primary MSP Crop",
                            value = "${farmer.primaryCrop.icon} ${if (isHindi) farmer.primaryCrop.hindiName else farmer.primaryCrop.englishName} (MSP: ₹${farmer.primaryCrop.mspPerQuintal}/Qtl)",
                            icon = Icons.Default.Grass
                        )
                        ProfileFieldRow(
                            label = if (isHindi) "अनुमानित उत्पादन" else "Estimated Harvest",
                            value = "${farmer.expectedQuantityQuintals} Quintals",
                            icon = Icons.Default.Scale
                        )
                    }
                }
            }
        }

        // Section 4: Direct Benefit Transfer (DBT) Bank Account
        item {
            Card(
                shape = AppCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHindi) "4. डीबीटी बैंक खाता (DBT Account)" else "4. DBT Direct Bank Account",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = bankName,
                            onValueChange = { bankName = it },
                            label = { Text(if (isHindi) "बैंक का नाम" else "Bank Name") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = bankAccountLast4,
                                onValueChange = { if (it.length <= 4) bankAccountLast4 = it },
                                label = { Text("A/C (Last 4)") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = ifscCode,
                                onValueChange = { ifscCode = it },
                                label = { Text("IFSC Code") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.2f),
                                singleLine = true
                            )
                        }
                    } else {
                        ProfileFieldRow(
                            label = if (isHindi) "पंजीकृत बैंक" else "Registered Bank",
                            value = "${farmer.bankName} (A/C ****${farmer.bankAccountLast4})",
                            icon = Icons.Default.AccountBalance
                        )
                        ProfileFieldRow(
                            label = "IFSC Code",
                            value = farmer.ifscCode,
                            icon = Icons.Default.Pin
                        )
                    }
                }
            }
        }

        // Save Button (Persistent into Room Database)
        if (isEditing) {
            item {
                Button(
                    onClick = {
                        val parsedArea = landAreaAcresText.toDoubleOrNull() ?: farmer.landAreaAcres
                        val parsedQuantity = expectedQuantityText.toDoubleOrNull() ?: farmer.expectedQuantityQuintals
                        val selectedMandi = centres.firstOrNull { it.id == selectedCentreId }

                        val updatedProfile = farmer.copy(
                            registrationId = registrationId.ifBlank { farmer.registrationId },
                            fullName = fullName.ifBlank { farmer.fullName },
                            phone = phone.ifBlank { farmer.phone },
                            mobile = phone.ifBlank { farmer.mobile },
                            email = email.ifBlank { farmer.email },
                            aadhaarLast4 = aadhaarLast4.ifBlank { farmer.aadhaarLast4 },
                            preferredCentreId = selectedCentreId,
                            preferredCentreName = selectedMandi?.name ?: farmer.preferredCentreName,
                            village = village.ifBlank { farmer.village },
                            block = block.ifBlank { farmer.block },
                            district = district.ifBlank { farmer.district },
                            state = state.ifBlank { farmer.state },
                            landRecordNo = landRecordNo.ifBlank { farmer.landRecordNo },
                            landAreaAcres = parsedArea,
                            primaryCrop = selectedCrop,
                            expectedQuantityQuintals = parsedQuantity,
                            bankName = bankName.ifBlank { farmer.bankName },
                            bankAccountLast4 = bankAccountLast4.ifBlank { farmer.bankAccountLast4 },
                            ifscCode = ifscCode.ifBlank { farmer.ifscCode }
                        )

                        viewModel.saveFarmerProfile(updatedProfile)
                        isEditing = false
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "💾 रूम डेटाबेस में सुरक्षित करें (Save Changes)" else "💾 Save Profile to Room Database",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Language & Role Switcher
        item {
            Card(
                shape = AppCardShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isHindi) "ऐप प्राथमिकताएं व भाषा" else "Preferences & Portal Role",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isHindi) "भाषा चुनें (Language)" else "Select Language",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        LanguageSwitcherPill(
                            currentLanguage = lang,
                            onLanguageChange = { viewModel.setLanguage(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isHindi) "सक्रिय पोर्टल रोल" else "Active Portal Role",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        RoleSelectorDropdown(
                            currentRole = currentRole,
                            onRoleSelected = { viewModel.setRole(it) },
                            isHindi = isHindi
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ProfileFieldRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        if (icon != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MintLight)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ForestGreenPrimary,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column {
            Text(
                text = label,
                fontSize = 10.5.sp,
                color = TextMuted
            )
            Text(
                text = value,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
