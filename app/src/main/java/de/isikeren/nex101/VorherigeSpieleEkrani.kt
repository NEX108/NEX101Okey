package de.isikeren.nex101

import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.isikeren.nex101.ui.theme.NEX101Theme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class VorherigesSpielListeItem(
    val oyunId: Int,
    val tarihText: String,
    val saatText: String,
    val oyuncuText: String
)

@Composable
fun VorherigeSpieleEkrani(
    onGeriClick: () -> Unit,
    onOyunClick: (Int) -> Unit,
    onOyunSilClick: (Int) -> Unit
) {
    val context = LocalContext.current
    val database = remember { DatabaseProvider.getDatabase(context) }
    val oyunDao = remember { database.oyunDao() }
    val oyunKatilimciDao = remember { database.oyunKatilimciDao() }
    val oyuncuDao = remember { database.oyuncuDao() }

    val bitenOyunlar by oyunDao.bitenOyunlariGetirFlow().collectAsState(initial = emptyList())
    val tumOyuncular by oyuncuDao.tumOyunculariGetir().collectAsState(initial = emptyList())

    val oncekiOyunlar by produceState<List<VorherigesSpielListeItem>>(
        initialValue = emptyList(),
        key1 = bitenOyunlar,
        key2 = tumOyuncular
    ) {
        val oyuncuMap = tumOyuncular.associateBy { it.id }

        value = bitenOyunlar.map { oyun ->
            val katilimcilar = oyunKatilimciDao.oyununKatilimcilariniGetir(oyun.id)
            val oyuncuAdlari = katilimcilar
                .sortedBy { it.pozisyon }
                .mapNotNull { katilimci -> oyuncuMap[katilimci.oyuncuId]?.ad }

            VorherigesSpielListeItem(
                oyunId = oyun.id,
                tarihText = oyun.bitisZamani?.let {
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
                } ?: oyun.baslangicZamani.let {
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(it))
                },
                saatText = oyun.bitisZamani?.let {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
                } ?: oyun.baslangicZamani.let {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it))
                },
                oyuncuText = oyuncuAdlari.joinToString(", ")
            )
        }
    }

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
                    text = "Vorherige Spiele",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (oncekiOyunlar.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Noch keine beendeten Spiele vorhanden.",
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(oncekiOyunlar) { oyun ->
                        VorherigesSpielSatiri(
                            item = oyun,
                            onClick = { onOyunClick(oyun.oyunId) },
                            onSilClick = { onOyunSilClick(oyun.oyunId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VorherigesSpielSatiri(
    item: VorherigesSpielListeItem,
    onClick: () -> Unit,
    onSilClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.tarihText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = item.saatText,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.oyuncuText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Löschen",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.clickable(onClick = onSilClick)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VorherigeSpieleEkraniPreview() {
    NEX101Theme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "←",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                Text(
                    text = "Vorherige Spiele",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            VorherigesSpielSatiri(
                item = VorherigesSpielListeItem(
                    oyunId = 1,
                    tarihText = "06.04.2026",
                    saatText = "19:45",
                    oyuncuText = "Eren, Semir, Erol, Eray"
                ),
                onClick = {},
                onSilClick = {}
            )

            VorherigesSpielSatiri(
                item = VorherigesSpielListeItem(
                    oyunId = 2,
                    tarihText = "05.04.2026",
                    saatText = "21:10",
                    oyuncuText = "Eren, Semir, Erol, Eray"
                ),
                onClick = {},
                onSilClick = {}
            )
        }
    }
}
