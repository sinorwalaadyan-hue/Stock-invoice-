package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.Formatters

data class CandlestickData(
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Float,
    val label: String
)

@Composable
fun CandlestickChart(
    data: List<CandlestickData>,
    modifier: Modifier = Modifier,
    title: String = "Price Movement & Volume",
    timeFilter: String = "1M",
    onTimeFilterSelected: (String) -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time Valuation & Demand Track",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Time Filter Tabs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    listOf("1D", "1W", "1M", "3M", "1Y").forEach { period ->
                        val isSelected = period == timeFilter
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            onClick = { onTimeFilterSelected(period) },
                            modifier = Modifier.size(width = 32.dp, height = 26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = period,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No price history recorded yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val minPrice = data.minOf { it.low } * 0.98f
                val maxPrice = data.maxOf { it.high } * 1.02f
                val priceRange = (maxPrice - minPrice).coerceAtLeast(1f)
                val maxVolume = data.maxOf { it.volume }.coerceAtLeast(1f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val volumeAreaHeight = canvasHeight * 0.25f
                    val candleAreaHeight = canvasHeight * 0.70f

                    // Draw subtle grid lines
                    val gridColor = Color(0x1A808080)
                    for (i in 0..4) {
                        val y = candleAreaHeight * (i / 4f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1f
                        )
                    }

                    val stepX = canvasWidth / data.size
                    val candleWidth = (stepX * 0.55f).coerceAtLeast(4f)

                    data.forEachIndexed { index, candle ->
                        val centerX = (index * stepX) + (stepX / 2f)
                        val isBullish = candle.close >= candle.open
                        val candleColor = if (isBullish) BrandEmerald else BrandCrimson

                        // Candlestick Y coordinates
                        val openY = candleAreaHeight - ((candle.open - minPrice) / priceRange * candleAreaHeight)
                        val closeY = candleAreaHeight - ((candle.close - minPrice) / priceRange * candleAreaHeight)
                        val highY = candleAreaHeight - ((candle.high - minPrice) / priceRange * candleAreaHeight)
                        val lowY = candleAreaHeight - ((candle.low - minPrice) / priceRange * candleAreaHeight)

                        // Draw Wick (thin vertical line)
                        drawLine(
                            color = candleColor,
                            start = Offset(centerX, highY),
                            end = Offset(centerX, lowY),
                            strokeWidth = 2f
                        )

                        // Draw Candle Body (rectangle)
                        val topY = minOf(openY, closeY)
                        val bodyHeight = kotlin.math.abs(closeY - openY).coerceAtLeast(2f)

                        drawRect(
                            color = candleColor,
                            topLeft = Offset(centerX - (candleWidth / 2f), topY),
                            size = Size(candleWidth, bodyHeight)
                        )

                        // Draw Volume Bar at Bottom
                        val volHeight = (candle.volume / maxVolume) * volumeAreaHeight
                        val volTopY = canvasHeight - volHeight
                        drawRect(
                            color = candleColor.copy(alpha = 0.35f),
                            topLeft = Offset(centerX - (candleWidth / 2f), volTopY),
                            size = Size(candleWidth, volHeight)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Low: ${Formatters.formatCurrency(minPrice.toDouble())}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "High: ${Formatters.formatCurrency(maxPrice.toDouble())}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SalesAreaChart(
    salesData: List<Pair<String, Double>>,
    costData: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    title: String = "Revenue vs COGS (Cost of Goods)"
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Comparing gross sales against inventory replacement costs",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (salesData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions in selected period", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val maxVal = maxOf(
                    salesData.maxOf { it.second },
                    costData.maxOfOrNull { it.second } ?: 0.0,
                    1000.0
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Grid
                    for (i in 0..3) {
                        val y = h * (i / 3f)
                        drawLine(
                            color = Color(0x15808080),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    val stepX = w / (salesData.size - 1).coerceAtLeast(1)

                    // Sales Gradient Area
                    val salesPath = Path()
                    val salesStroke = Path()

                    salesData.forEachIndexed { i, pt ->
                        val x = i * stepX
                        val y = h - ((pt.second / maxVal) * (h * 0.85f)).toFloat()
                        if (i == 0) {
                            salesPath.moveTo(x, y)
                            salesStroke.moveTo(x, y)
                        } else {
                            salesPath.lineTo(x, y)
                            salesStroke.lineTo(x, y)
                        }
                    }

                    // Fill Sales Area
                    val fillPath = Path().apply {
                        addPath(salesPath)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(BrandEmerald.copy(alpha = 0.35f), Color.Transparent),
                            startY = 0f,
                            endY = h
                        )
                    )
                    drawPath(
                        path = salesStroke,
                        color = BrandEmerald,
                        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                    )

                    // COGS Line
                    if (costData.size == salesData.size) {
                        val costStroke = Path()
                        costData.forEachIndexed { i, pt ->
                            val x = i * stepX
                            val y = h - ((pt.second / maxVal) * (h * 0.85f)).toFloat()
                            if (i == 0) costStroke.moveTo(x, y) else costStroke.lineTo(x, y)
                        }
                        drawPath(
                            path = costStroke,
                            color = BrandCyan,
                            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chart Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(10.dp).background(BrandEmerald, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gross Sales", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)

                    Spacer(modifier = Modifier.width(20.dp))

                    Box(modifier = Modifier.size(10.dp).background(BrandCyan, RoundedCornerShape(2.dp)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("COGS (Cost)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun MiniSparkline(
    data: List<Float>,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    if (data.size < 2) return

    val min = data.minOrNull() ?: 0f
    val max = data.maxOrNull() ?: 1f
    val range = (max - min).coerceAtLeast(0.1f)
    val color = if (isPositive) BrandEmerald else BrandCrimson

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stepX = w / (data.size - 1)

        val path = Path()
        data.forEachIndexed { i, v ->
            val x = i * stepX
            val y = h - ((v - min) / range * (h * 0.75f) + (h * 0.125f))
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2f, cap = StrokeCap.Round)
        )
    }
}
