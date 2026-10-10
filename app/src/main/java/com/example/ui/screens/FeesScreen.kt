package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeEntity
import com.example.data.model.StudentEntity
import com.example.data.model.UserRole
import com.example.ui.ErpViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(
    viewModel: ErpViewModel,
    modifier: Modifier = Modifier
) {
    val feesList by viewModel.filteredFees.collectAsState()
    val totalCollected by viewModel.totalFeesCollected.collectAsState()
    val totalPending by viewModel.totalFeesPending.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val currentFilter by viewModel.feeStatusFilter.collectAsState()
    val searchQuery by viewModel.feeSearchQuery.collectAsState()

    val showAddDialog by viewModel.showAddFeeDialog.collectAsState()
    val selectedReceipt by viewModel.selectedFeeForReceipt.collectAsState()
    val selectedForStatusUpdate by viewModel.selectedFeeForStatusUpdate.collectAsState()

    val isAdmin = userRole == UserRole.ADMIN
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Scaffold(
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { viewModel.openAddFeeDialog() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_fee_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.AddCard, contentDescription = "Add Fee")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Fee Record", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        modifier = modifier.testTag("fees_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fees Analytics & Filter Header
            Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Collected Fees",
                            value = currencyFormat.format(totalCollected),
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Outstanding / Pending",
                            value = currencyFormat.format(totalPending),
                            icon = Icons.Default.PendingActions,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.feeSearchQuery.value = it },
                        modifier = Modifier.fillMaxWidth().testTag("fee_search_input"),
                        placeholder = { Text("Search by student, roll number, or receipt...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.feeSearchQuery.value = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Paid", "Pending", "Overdue").forEach { status ->
                            FilterChip(
                                selected = currentFilter == status,
                                onClick = { viewModel.feeStatusFilter.value = status },
                                label = { Text(status) },
                                modifier = Modifier.testTag("filter_fee_$status")
                            )
                        }
                    }
                }
            }

            if (!isAdmin) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Access Control: Only Admins can create fee records and update payment statuses.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Fees List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(feesList, key = { it.id }) { fee ->
                    FeeRecordCard(
                        fee = fee,
                        isAdmin = isAdmin,
                        onViewReceipt = { viewModel.selectedFeeForReceipt.value = fee },
                        onUpdateStatus = { viewModel.selectedFeeForStatusUpdate.value = fee }
                    )
                }
            }
        }
    }

    // Add Fee Dialog
    if (showAddDialog) {
        AddFeeDialog(
            students = students,
            onDismiss = { viewModel.showAddFeeDialog.value = false },
            onSave = { student, feeType, amount, status, date, method ->
                viewModel.createFeeRecord(student, feeType, amount, status, date, method)
            }
        )
    }

    // Update Status Dialog
    if (selectedForStatusUpdate != null) {
        UpdatePaymentStatusDialog(
            fee = selectedForStatusUpdate!!,
            onDismiss = { viewModel.selectedFeeForStatusUpdate.value = null },
            onUpdate = { status, date, method ->
                viewModel.updateFeeStatus(selectedForStatusUpdate!!.id, status, date, method)
            }
        )
    }

    // Receipt Modal Dialog
    if (selectedReceipt != null) {
        FeeReceiptDialog(
            fee = selectedReceipt!!,
            onDismiss = { viewModel.selectedFeeForReceipt.value = null }
        )
    }
}

@Composable
fun FeeRecordCard(
    fee: FeeEntity,
    isAdmin: Boolean,
    onViewReceipt: () -> Unit,
    onUpdateStatus: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val statusColor = when (fee.status.lowercase()) {
        "paid" -> Color(0xFF10B981)
        "overdue" -> Color(0xFFEF4444)
        else -> Color(0xFFF59E0B)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("fee_card_${fee.receiptNumber}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = fee.studentName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(text = "${fee.rollNumber} • ${fee.feeType}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = fee.status.uppercase(),
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Amount Due / Paid", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = currencyFormat.format(fee.amount), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Receipt #", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = fee.receiptNumber, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (fee.status == "Paid") "Paid on: ${fee.paymentDate}" else "Status: Awaiting Payment",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onViewReceipt,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("btn_receipt_${fee.receiptNumber}")
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Receipt", fontSize = 12.sp)
                    }

                    if (isAdmin && fee.status != "Paid") {
                        Button(
                            onClick = onUpdateStatus,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_update_status_${fee.receiptNumber}")
                        ) {
                            Text("Mark Paid", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeeReceiptDialog(
    fee: FeeEntity,
    onDismiss: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Campus ERP Official Receipt", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Receipt Number:", style = MaterialTheme.typography.labelSmall)
                            Text(fee.receiptNumber, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payment Date:", style = MaterialTheme.typography.labelSmall)
                            Text(if (fee.paymentDate.isNotEmpty()) fee.paymentDate else "Pending Verification", fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                DetailItem("Student Name", fee.studentName)
                DetailItem("Roll Number", fee.rollNumber)
                DetailItem("Fee Classification", fee.feeType)
                DetailItem("Payment Mode", fee.paymentMethod)
                DetailItem("Total Amount", currencyFormat.format(fee.amount))

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (fee.status == "Paid") Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (fee.status == "Paid") "✓ PAYMENT CLEARED & RECORDED" else "⚠ PENDING PAYMENT CLEARANCE",
                            color = if (fee.status == "Paid") Color(0xFF10B981) else Color(0xFFF59E0B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close Receipt") }
        }
    )
}

@Composable
fun AddFeeDialog(
    students: List<StudentEntity>,
    onDismiss: () -> Unit,
    onSave: (student: StudentEntity, feeType: String, amount: Double, status: String, paymentDate: String, method: String) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
    var feeType by remember { mutableStateOf("Tuition Fee - Fall 2026") }
    var amountText by remember { mutableStateOf("4500.00") }
    var status by remember { mutableStateOf("Pending") }
    var method by remember { mutableStateOf("Online Portal") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val feeTypes = listOf("Tuition Fee - Fall 2026", "Computer Lab & Tech Fee", "Hostel & Amenities Fee", "Examination Fee", "Library Caution Fee")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Fee Record", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMsg != null) {
                    Text(errorMsg!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Text("Select Student:", style = MaterialTheme.typography.labelSmall)
                students.take(4).forEach { st ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(selected = selectedStudent?.id == st.id, onClick = { selectedStudent = st })
                        Text("${st.name} (${st.rollNumber})", fontSize = 13.sp)
                    }
                }

                OutlinedTextField(
                    value = feeType,
                    onValueChange = { feeType = it },
                    label = { Text("Fee Description / Type *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; errorMsg = null },
                    label = { Text("Amount ($) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Payment Status:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Pending", "Paid", "Overdue").forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (selectedStudent == null || amt == null || amt <= 0) {
                        errorMsg = "Please enter a valid student and positive amount"
                        return@Button
                    }
                    onSave(selectedStudent!!, feeType, amt, status, LocalDate.now().toString(), method)
                }
            ) {
                Text("Create Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun UpdatePaymentStatusDialog(
    fee: FeeEntity,
    onDismiss: () -> Unit,
    onUpdate: (status: String, date: String, method: String) -> Unit
) {
    var newStatus by remember { mutableStateOf("Paid") }
    var paymentDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var paymentMethod by remember { mutableStateOf("Online Portal") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Payment Status", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Student: ${fee.studentName} (${fee.rollNumber})")
                Text("Fee Type: ${fee.feeType}")

                Text("New Status:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Paid", "Pending", "Overdue").forEach { st ->
                        FilterChip(
                            selected = newStatus == st,
                            onClick = { newStatus = st },
                            label = { Text(st) }
                        )
                    }
                }

                OutlinedTextField(
                    value = paymentDate,
                    onValueChange = { paymentDate = it },
                    label = { Text("Payment Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("Payment Mode") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onUpdate(newStatus, paymentDate, paymentMethod) }) {
                Text("Confirm Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
