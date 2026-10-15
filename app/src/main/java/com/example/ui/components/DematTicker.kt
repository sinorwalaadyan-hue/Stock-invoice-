package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ProductEntity
import com.example.ui.theme.BrandCrimson
import com.example.ui.theme.BrandEmerald
import com.example.util.Formatters

@Composable
fun DematTicker(
    products: List<ProductEntity>,
    modifier: Modifier = Modifier,
    onProductClick: (ProductEntity) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .horizontalScroll(scrollState)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Live Market Indicator Badge
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(BrandEmerald, RoundedCornerShape(3.dp))
                )
                Text(
                    text = "TICKER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    letterSpacing = 1.sp
                )
            }
        }

        products.take(10).forEach { product ->
            val isPositive = product.priceChangeCurrency >= 0
            val deltaColor = if (isPositive) BrandEmerald else BrandCrimson
            val sampleSparkline = if (isPositive) {
                listOf(product.previousSellingPrice.toFloat(), (product.previousSellingPrice * 1.01).toFloat(), product.sellingPrice.toFloat())
            } else {
                listOf(product.previousSellingPrice.toFloat(), (product.previousSellingPrice * 0.99).toFloat(), product.sellingPrice.toFloat())
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .clickable { onProductClick(product) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column {
                        Text(
                            text = product.name.take(16) + if (product.name.length > 16) ".." else "",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = product.sku,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = Formatters.formatCurrency(product.sellingPrice),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = if (isPositive) "Up" else "Down",
                                tint = deltaColor,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${if (isPositive) "+" else ""}${String.format("%.1f", product.priceChangePercent)}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = deltaColor
                            )
                        }
                    }

                    MiniSparkline(
                        data = sampleSparkline,
                        isPositive = isPositive,
                        modifier = Modifier.size(width = 28.dp, height = 16.dp)
                    )
                }
            }
        }
    }
}
