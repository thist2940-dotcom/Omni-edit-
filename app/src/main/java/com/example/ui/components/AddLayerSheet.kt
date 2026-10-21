package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClipType
import com.example.model.SampleMediaLibrary
import com.example.model.ShapeType
import com.example.ui.theme.*

@Composable
fun AddLayerSheet(
    onAddLayer: (ClipType, String, String, ShapeType) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ADD MULTI-TRACK LAYER",
                    color = NeonPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Insert Media, Titles or FX",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val layerOptions = listOf(
            LayerOption("Overlay Video", "PIP B-roll footage clip", Icons.Default.VideoLibrary, NeonPurple) {
                val stock = SampleMediaLibrary.STOCK_VIDEOS.random()
                onAddLayer(ClipType.VIDEO, stock.title, "", ShapeType.RECTANGLE)
            },
            LayerOption("Animated Text", "Kinetic subtitle or headline", Icons.Default.TextFields, NeonCyan) {
                onAddLayer(ClipType.TEXT, "Kinetic Text", "NEW TITLE", ShapeType.RECTANGLE)
            },
            LayerOption("Vector Shape", "Alight Motion star / polygon", Icons.Default.Star, NeonAmber) {
                onAddLayer(ClipType.SHAPE, "Star Shape", "", ShapeType.STAR)
            },
            LayerOption("Audio & SFX", "BGM soundtrack or impact whoosh", Icons.Default.Audiotrack, NeonGreen) {
                val sfx = SampleMediaLibrary.STOCK_AUDIO.random()
                onAddLayer(ClipType.AUDIO, sfx.title, "", ShapeType.RECTANGLE)
            },
            LayerOption("FX Adjustment Layer", "Stack After Effects color & shake", Icons.Default.AutoAwesome, NeonPink) {
                onAddLayer(ClipType.ADJUSTMENT, "FX Layer", "", ShapeType.RECTANGLE)
            }
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(layerOptions) { option ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = StudioDarkBg,
                    onClick = {
                        option.onClick()
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(option.color.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(option.icon, contentDescription = null, tint = option.color)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(option.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(option.subtitle, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        }

                        Icon(Icons.Default.Add, contentDescription = null, tint = option.color)
                    }
                }
            }
        }
    }
}

private data class LayerOption(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)
