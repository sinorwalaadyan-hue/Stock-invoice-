package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import com.example.ui.theme.BrandCrimson
import com.example.ui.theme.BrandEmerald
import com.example.util.Formatters

@Composable
fun InvoiceDialog(
    sale: SaleEntity,
    items: List<SaleItemEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Top Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TAX INVOICE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Invoice Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "STOCKVISION ENTERPRISES",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "GSTIN: 27AABCS1429B1Z8 | FSSAI: 11520034000122",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "MG Road Commercial Plaza, Fort, Mumbai - 400001",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Invoice #: ${sale.invoiceNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = "Date: ${Formatters.formatDateTime(sale.date)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Customer: ${sale.customerName}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(text = "Mode: ${sale.paymentMethod}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Items Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Item", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(2f))
                    Text(text = "Qty", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                    Text(text = "Rate", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text(text = "Total", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                }

                // Items Rows
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item.productName, fontSize = 12.sp, modifier = Modifier.weight(2f))
                        Text(text = item.quantity.toString(), fontSize = 12.sp, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                        Text(text = Formatters.formatCurrency(item.unitSellingPrice), fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text(text = Formatters.formatCurrency(item.lineTotal), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Calculations Box
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(Formatters.formatCurrency(sale.totalAmount), fontSize = 12.sp)
                    }
                    if (sale.discountAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discount Applied:", fontSize = 12.sp, color = BrandEmerald)
                            Text("- " + Formatters.formatCurrency(sale.discountAmount), fontSize = 12.sp, color = BrandEmerald)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Taxes (GST Included):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(Formatters.formatCurrency(sale.taxAmount), fontSize = 12.sp)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Net Payable:", fontWeight = FontWeight.Black, fontSize = 15.sp)
                        Text(Formatters.formatCurrency(sale.netAmount), fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Amount Received:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(Formatters.formatCurrency(sale.amountPaid), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    val balanceDue = sale.netAmount - sale.amountPaid
                    if (balanceDue > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Outstanding Balance:", fontSize = 12.sp, color = BrandCrimson, fontWeight = FontWeight.Bold)
                            Text(Formatters.formatCurrency(balanceDue), fontSize = 12.sp, color = BrandCrimson, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Status Stamp
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (sale.paymentStatus == "PAID") BrandEmerald.copy(alpha = 0.15f) else BrandCrimson.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "STATUS: ${sale.paymentStatus}",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = if (sale.paymentStatus == "PAID") BrandEmerald else BrandCrimson,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Buttons (Share / Done)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val invoiceText = buildString {
                                appendLine("--- TAX INVOICE ---")
                                appendLine("StockVision Enterprises")
                                appendLine("Invoice: ${sale.invoiceNumber}")
                                appendLine("Customer: ${sale.customerName}")
                                appendLine("Date: ${Formatters.formatDateTime(sale.date)}")
                                appendLine("Amount: ${Formatters.formatCurrency(sale.netAmount)}")
                                appendLine("Status: ${sale.paymentStatus}")
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, invoiceText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Invoice")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share")
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}
