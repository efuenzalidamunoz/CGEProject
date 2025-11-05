package org.example.cgeproject.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.cgeproject.ui.AppScreen


@Composable
public fun TopNavBar(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo CGE
            Text(
                text = "CGE",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF001689),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )

            // Menú de navegación
            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppScreen.values().forEach { screen ->
                    NavBarItem(
                        text = screen.title,
                        isActive = currentScreen == screen,
                        onClick = { onScreenSelected(screen) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NavBarItem(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val color = if (isActive) Color(0xFF001689) else Color.Gray

    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            color = color,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}