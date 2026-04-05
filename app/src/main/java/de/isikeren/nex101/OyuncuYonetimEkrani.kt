package de.isikeren.nex101

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.isikeren.nex101.ui.theme.NEX101Theme
import kotlinx.coroutines.launch

@Composable
fun OyuncuYonetimEkrani(onGeriClick: () -> Unit) {
    val context = LocalContext.current
    val dao = remember { DatabaseProvider.getDatabase(context).oyuncuDao() }
    val oyuncular by dao.tumOyunculariGetir().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var yeniOyuncuAdi by remember { mutableStateOf("") }
    var oyuncuEkleDialogAcik by remember { mutableStateOf(false) }
    var duzenlenenOyuncuId by remember { mutableStateOf<Int?>(null) }
    var duzenlenenOyuncuAdi by remember { mutableStateOf("") }
    var silinecekOyuncu by remember { mutableStateOf<OyuncuEntity?>(null) }

    BackHandler(onBack = onGeriClick)

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "←",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .clickable(onClick = onGeriClick)
                        .padding(end = 16.dp)
                )

                Text(
                    text = "Spieler verwalten",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            AnaMenuButonu(
                text = "Hinzufügen",
                onClick = {
                    yeniOyuncuAdi = ""
                    oyuncuEkleDialogAcik = true
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(oyuncular, key = { it.id }) { oyuncu ->
                    OyuncuSatiri(
                        oyuncu = oyuncu,
                        onDuzenleClick = {
                            duzenlenenOyuncuId = oyuncu.id
                            duzenlenenOyuncuAdi = oyuncu.ad
                        },
                        onSilClick = {
                            silinecekOyuncu = oyuncu
                        }
                    )
                }
            }
        }
    }

    if (oyuncuEkleDialogAcik) {
        AlertDialog(
            onDismissRequest = { oyuncuEkleDialogAcik = false },
            title = { Text("Spieler hinzufügen") },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = yeniOyuncuAdi,
                    onValueChange = { yeniOyuncuAdi = it },
                    label = { Text("Spielername") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val temizAd = yeniOyuncuAdi.trim()
                        if (temizAd.isNotEmpty()) {
                            scope.launch {
                                dao.oyuncuEkle(OyuncuEntity(ad = temizAd))
                                yeniOyuncuAdi = ""
                                oyuncuEkleDialogAcik = false
                            }
                        }
                    }
                ) {
                    Text("Hinzufügen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        oyuncuEkleDialogAcik = false
                        yeniOyuncuAdi = ""
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }

    if (duzenlenenOyuncuId != null) {
        AlertDialog(
            onDismissRequest = { duzenlenenOyuncuId = null },
            title = { Text("Spieler bearbeiten") },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = duzenlenenOyuncuAdi,
                    onValueChange = { duzenlenenOyuncuAdi = it },
                    label = { Text("Spielername") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val temizAd = duzenlenenOyuncuAdi.trim()
                        val oyuncuId = duzenlenenOyuncuId
                        if (temizAd.isNotEmpty() && oyuncuId != null) {
                            scope.launch {
                                dao.oyuncuGuncelle(
                                    OyuncuEntity(
                                        id = oyuncuId,
                                        ad = temizAd
                                    )
                                )
                                duzenlenenOyuncuId = null
                                duzenlenenOyuncuAdi = ""
                            }
                        }
                    }
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { duzenlenenOyuncuId = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    silinecekOyuncu?.let { oyuncu ->
        AlertDialog(
            onDismissRequest = { silinecekOyuncu = null },
            title = { Text("Spieler löschen") },
            text = { Text("Willst du den Spieler \"${oyuncu.ad}\" wirklich löschen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            dao.oyuncuSil(oyuncu)
                            silinecekOyuncu = null
                        }
                    }
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { silinecekOyuncu = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
private fun OyuncuSatiri(
    oyuncu: OyuncuEntity,
    onDuzenleClick: () -> Unit,
    onSilClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#${oyuncu.id}",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(56.dp)
        )

        Text(
            text = oyuncu.ad,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium
        )

        SembolButon(text = "⚙", onClick = onDuzenleClick)
        Spacer(modifier = Modifier.width(8.dp))
        SembolButon(text = "🗑", onClick = onSilClick)
    }
}

@Composable
private fun SembolButon(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OyuncuYonetimEkraniPreview() {
    NEX101Theme {
        OyuncuYonetimEkrani(onGeriClick = {})
    }
}
