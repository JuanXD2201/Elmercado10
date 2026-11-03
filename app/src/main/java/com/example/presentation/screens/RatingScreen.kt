package com.example.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.InteractiveRatingBar
import com.example.presentation.components.PrimaryButton
import com.example.presentation.viewmodel.RatingViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.Slate500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingBottomSheet(
    ratingViewModel: RatingViewModel,
    productName: String,
    onDismiss: () -> Unit
) {
    val existingRating by ratingViewModel.existingRating.collectAsState()
    val isSubmitting by ratingViewModel.isSubmitting.collectAsState()

    var stars by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(existingRating) {
        existingRating?.let {
            stars = it.estrellas
            comment = it.comentario
        }
    }

    LaunchedEffect(ratingViewModel.eventFlow) {
        ratingViewModel.eventFlow.collect {
            onDismiss()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Calificar Producto",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = productName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            InteractiveRatingBar(
                rating = stars,
                onRatingChange = { stars = it }
            )

            Text(
                text = when (stars) {
                    5 -> "⭐⭐⭐⭐⭐ ¡Excelente producto!"
                    4 -> "⭐⭐⭐⭐ Muy bueno"
                    3 -> "⭐⭐⭐ Aceptable"
                    2 -> "⭐⭐ Regular"
                    else -> "⭐ Deficiente"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 10.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Chips rápidos de opinión
            val quickFeedbacks = listOf(
                "¡Excelente calidad!",
                "Envío súper rápido",
                "Vendedor muy atento",
                "100% recomendado",
                "Tal como en la foto"
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                quickFeedbacks.take(3).forEach { phrase ->
                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                comment = if (comment.isBlank()) phrase else "$comment. $phrase"
                            }
                    ) {
                        Text(
                            text = phrase,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Escribe una reseña o comentario (opcional)") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rating_comment_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = if (existingRating != null) "Actualizar Calificación" else "Publicar Calificación",
                isLoading = isSubmitting,
                onClick = {
                    ratingViewModel.submitRating(stars, comment)
                },
                testTag = "submit_rating_button"
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
