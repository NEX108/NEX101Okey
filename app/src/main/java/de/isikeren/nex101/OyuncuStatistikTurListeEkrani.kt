package de.isikeren.nex101

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.isikeren.nex101.ui.theme.NEX101Theme

enum class OyuncuTurFiltreTipi(val baslik: String) {
    ACDI("acdi"),
    BITTI("bitti"),
    OKEYLE("okeyle"),
    ELDEN("elden"),
    CIFT("çift"),
    ACAMADI("açamadı")
}

data class OyuncuTurListeItem(
    val oyunId: Int,
    val turNo: Int,
    val tarihText: String,
    val aciklamaText: String = ""
)

data class OyuncuStatistikTurListeUiState(
    val oyuncuAdi: String,
    val filtreTipi: OyuncuTurFiltreTipi,
    val toplamAdet: Int,
    val turlar: List<OyuncuTurListeItem>
)

@Composable
fun OyuncuStatistikTurListeEkrani(
    uiState: OyuncuStatistikTurListeUiState,
    onGeriClick: () -> Unit,
    onTurClick: (OyuncuTurListeItem) -> Unit = {}
) {
    BackHandler(onBack = onGeriClick)

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "←",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable(onClick = onGeriClick)
                )

                Text(
                    text = uiState.filtreTipi.baslik,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.background,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = uiState.oyuncuAdi,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Anzahl: ${uiState.toplamAdet}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.turlar.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Keine passenden Runden vorhanden.",
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.turlar) { item ->
                        TurListeSatiri(
                            item = item,
                            onClick = { onTurClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TurListeSatiri(
    item: OyuncuTurListeItem,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.tarihText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                if (item.aciklamaText.isNotBlank()) {
                    Text(
                        text = item.aciklamaText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Text(
                text = "Runde ${item.turNo}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OyuncuStatistikTurListeEkraniPreview() {
    NEX101Theme {
        OyuncuStatistikTurListeEkrani(
            uiState = OyuncuStatistikTurListeUiState(
                oyuncuAdi = "Eren",
                filtreTipi = OyuncuTurFiltreTipi.ELDEN,
                toplamAdet = 2,
                turlar = listOf(
                    OyuncuTurListeItem(
                        oyunId = 11,
                        turNo = 2,
                        tarihText = "11.04.2026",
                        aciklamaText = "Mühendis vs Gay"
                    ),
                    OyuncuTurListeItem(
                        oyunId = 14,
                        turNo = 5,
                        tarihText = "15.04.2026",
                        aciklamaText = "Anadolu vs Avrupa"
                    )
                )
            ),
            onGeriClick = {}
        )
    }
}