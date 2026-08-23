package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IsometricScene3D(
    modifier: Modifier = Modifier,
    currentlyServing: String = "CP-098",
    activeTokens: List<String> = listOf("CP-098", "CP-099", "CP-100", "CP-104"),
    onNodeClicked: (String) -> Unit = {}
) {
    // Animation clocks
    val infiniteTransition = rememberInfiniteTransition(label = "iso3d")
    val waveAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Interactive pointer drag / mouse offset
    var dragOffsetX by remember { mutableStateOf(0f) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    var hoveredNode by remember { mutableStateOf<String?>(null) }

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9),
                        Color(0xFFD7ECD9),
                        Color(0xFFC8E6C9)
                    )
                )
            )
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    dragOffsetX = (dragOffsetX + dragAmount.x * 0.3f).coerceIn(-60f, 60f)
                    dragOffsetY = (dragOffsetY + dragAmount.y * 0.3f).coerceIn(-30f, 30f)
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val w = size.width
            val h = size.height
            val centerX = w * 0.5f + dragOffsetX
            val centerY = h * 0.52f + dragOffsetY

            // 1. Draw 3D Ground Isometric Grid Plates (Field + Yard)
            drawIsometricField(centerX, centerY, waveAnim)

            // 2. Draw Procurement Centre Building & Silos
            drawProcurementBuilding(centerX, centerY)

            // 3. Draw Weighbridge & Vehicle
            drawWeighbridgeAndTruck(centerX, centerY, waveAnim)

            // 4. Draw Digital Queue Path & Glowing Token Nodes
            drawDigitalQueueNodes(
                centerX = centerX,
                centerY = centerY,
                pulse = pulseAnim,
                wave = waveAnim,
                currentlyServing = currentlyServing,
                activeTokens = activeTokens,
                textMeasurer = textMeasurer
            )
        }

        // Overlay status badge on 3D view
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ForestGreenPrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "3D Live Yard: Serving $currentlyServing",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreenPrimary
            )
        }

        // Interactive hint
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Drag to tilt 3D perspective",
                fontSize = 10.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun DrawScope.drawIsometricField(cx: Float, cy: Float, wave: Float) {
    // Left side: Golden Wheat crop field isometric tile
    val fieldPath = Path().apply {
        moveTo(cx - 220f, cy + 20f)
        lineTo(cx - 40f, cy - 70f)
        lineTo(cx - 20f, cy - 30f)
        lineTo(cx - 180f, cy + 60f)
        close()
    }
    drawPath(
        path = fieldPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFE9C46A), Color(0xFFF4A261)),
            start = Offset(cx - 200f, cy),
            end = Offset(cx, cy)
        )
    )

    // Animated crop wave lines
    for (i in 0..5) {
        val yOffset = cy - 40f + i * 15f
        val sway = sin((wave + i * 40) * Math.PI.toFloat() / 180f) * 4f
        drawLine(
            color = Color(0xFFD4A373).copy(alpha = 0.7f),
            start = Offset(cx - 190f + i * 20f + sway, yOffset + 30f),
            end = Offset(cx - 90f + i * 10f + sway, yOffset - 20f),
            strokeWidth = 2f
        )
    }

    // Right side: Main Concrete Procurement Yard Isometric Base
    val yardPath = Path().apply {
        moveTo(cx - 60f, cy - 60f)
        lineTo(cx + 180f, cy - 120f)
        lineTo(cx + 240f, cy + 10f)
        lineTo(cx + 20f, cy + 70f)
        close()
    }
    drawPath(
        path = yardPath,
        color = Color(0xFFE0E5E0)
    )
    drawPath(
        path = yardPath,
        color = Color(0xFFB0BEB4),
        style = Stroke(width = 1.5f)
    )
}

private fun DrawScope.drawProcurementBuilding(cx: Float, cy: Float) {
    val bx = cx + 80f
    val by = cy - 70f

    // Building Isometric Front & Side Walls
    // Roof (Green AgriTech theme)
    val roof = Path().apply {
        moveTo(bx, by - 40f)
        lineTo(bx + 70f, by - 65f)
        lineTo(bx + 110f, by - 35f)
        lineTo(bx + 40f, by - 10f)
        close()
    }
    drawPath(roof, Color(0xFF2D6A4F))

    // Front wall
    val frontWall = Path().apply {
        moveTo(bx + 40f, by - 10f)
        lineTo(bx + 110f, by - 35f)
        lineTo(bx + 110f, by + 10f)
        lineTo(bx + 40f, by + 35f)
        close()
    }
    drawPath(frontWall, Color(0xFF52B788))

    // Left wall
    val leftWall = Path().apply {
        moveTo(bx, by - 40f)
        lineTo(bx + 40f, by - 10f)
        lineTo(bx + 40f, by + 35f)
        lineTo(bx, by + 5f)
        close()
    }
    drawPath(leftWall, Color(0xFF74C69D))

    // Silo (Cylindrical Grain Silo)
    drawRoundRect(
        color = Color(0xFFD8F3DC),
        topLeft = Offset(bx - 35f, by - 55f),
        size = Size(26f, 65f),
        cornerRadius = CornerRadius(12f, 12f)
    )
    drawRoundRect(
        color = Color(0xFF40916C),
        topLeft = Offset(bx - 35f, by - 62f),
        size = Size(26f, 12f),
        cornerRadius = CornerRadius(12f, 12f)
    )
}

private fun DrawScope.drawWeighbridgeAndTruck(cx: Float, cy: Float, wave: Float) {
    val tx = cx - 20f
    val ty = cy + 15f

    // Weighbridge platform
    drawRoundRect(
        color = Color(0xFF6C757D),
        topLeft = Offset(tx - 30f, ty - 10f),
        size = Size(65f, 25f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = Color(0xFFFFD166),
        topLeft = Offset(tx - 28f, ty - 8f),
        size = Size(61f, 21f),
        cornerRadius = CornerRadius(3f, 3f),
        style = Stroke(width = 2f)
    )

    // Small stylized tractor / grain delivery truck
    drawRoundRect(
        color = Color(0xFFE76F51),
        topLeft = Offset(tx - 15f, ty - 24f),
        size = Size(24f, 18f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    drawCircle(
        color = Color(0xFF264653),
        radius = 5f,
        center = Offset(tx - 8f, ty - 4f)
    )
    drawCircle(
        color = Color(0xFF264653),
        radius = 5f,
        center = Offset(tx + 6f, ty - 4f)
    )
}

private fun DrawScope.drawDigitalQueueNodes(
    centerX: Float,
    centerY: Float,
    pulse: Float,
    wave: Float,
    currentlyServing: String,
    activeTokens: List<String>,
    textMeasurer: TextMeasurer
) {
    // Nodes representing tokens waiting in physical & digital yard
    val nodePositions = listOf(
        Offset(centerX - 90f, centerY + 30f),  // Entry Gate (Token CP-104 YOU)
        Offset(centerX - 40f, centerY + 50f),  // Waiting Line (CP-102)
        Offset(centerX + 20f, centerY + 35f),  // Pre-Weighment (CP-100)
        Offset(centerX + 65f, centerY + 5f)    // Counter 1 / Active (CP-098)
    )

    // Draw connecting laser/digital queue line
    for (i in 0 until nodePositions.size - 1) {
        drawLine(
            color = ForestGreenPrimary.copy(alpha = 0.5f),
            start = nodePositions[i],
            end = nodePositions[i + 1],
            strokeWidth = 3f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), wave)
        )
    }

    // Draw Token nodes
    val tokenLabels = listOf("CP-104 (YOU)", "CP-102", "CP-100", currentlyServing)

    nodePositions.forEachIndexed { index, pos ->
        val isServing = index == 3
        val isFarmer = index == 0

        val nodeColor = when {
            isServing -> Color(0xFF15803D)
            isFarmer -> SaffronOrange
            else -> ForestGreenPrimary
        }

        // Glowing pulse halo
        drawCircle(
            color = nodeColor.copy(alpha = if (isServing || isFarmer) 0.25f * pulse else 0.15f),
            radius = if (isServing || isFarmer) 16f * pulse else 12f,
            center = pos
        )

        // Center Pin
        drawCircle(
            color = nodeColor,
            radius = if (isServing || isFarmer) 8f else 6f,
            center = pos
        )
        drawCircle(
            color = Color.White,
            radius = 3f,
            center = pos
        )

        // Floating Pill Label
        val label = tokenLabels.getOrElse(index) { "CP-00" }
        val labelBgColor = if (isFarmer) SaffronOrange else if (isServing) ForestGreenPrimary else Color(0xFF334155)

        val pillWidth = if (isFarmer) 68f else 48f
        val pillHeight = 16f

        drawRoundRect(
            color = labelBgColor,
            topLeft = Offset(pos.x - pillWidth / 2f, pos.y - 28f),
            size = Size(pillWidth, pillHeight),
            cornerRadius = CornerRadius(4f, 4f)
        )

        val textLayout = textMeasurer.measure(
            text = label,
            style = TextStyle(
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        )

        drawText(
            textLayoutResult = textLayout,
            topLeft = Offset(
                pos.x - textLayout.size.width / 2f,
                pos.y - 27f
            )
        )
    }
}
