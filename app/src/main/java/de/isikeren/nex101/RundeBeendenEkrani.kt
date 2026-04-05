package de.isikeren.nex101

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.isikeren.nex101.ui.theme.NEX101Theme

@Composable
fun RundeBeendenEkrani(
    uiState: RundeBeendenUiState,
    onGeriClick: () -> Unit,
    onSpeichernClick: (RundeBeendenUiState) -> Unit
) {
    var localState by remember(uiState) { mutableStateOf(rundeBeendenKurallariUygula(uiState)) }

    BackHandler(onBack = onGeriClick)

    fun localStateGuncelle(guncelleyici: (RundeBeendenUiState) -> RundeBeendenUiState) {
        localState = rundeBeendenKurallariUygula(guncelleyici(localState))
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        val kompakt = maxHeight < 760.dp
        val kartAbstand = if (kompakt) 6.dp else 12.dp
        val headerPadding = if (kompakt) 12.dp else 24.dp
        val buttonPadding = if (kompakt) 8.dp else 20.dp

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = headerPadding, bottom = headerPadding),
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
                    text = "Runde ${localState.turNo} Ende",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(kartAbstand)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpielerEndeKart(
                        modifier = Modifier.weight(1f),
                        state = localState.oyuncu1,
                        kompakt = kompakt,
                        rahmenFarbe = Color(0xFF81D4FA),
                        onStateChange = { localStateGuncelle { durum -> durum.copy(oyuncu1 = it) } }
                    )
                    SpielerEndeKart(
                        modifier = Modifier.weight(1f),
                        state = localState.oyuncu2,
                        kompakt = kompakt,
                        rahmenFarbe = Color(0xFFC62828),
                        onStateChange = { localStateGuncelle { durum -> durum.copy(oyuncu2 = it) } }
                    )
                }

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpielerEndeKart(
                        modifier = Modifier.weight(1f),
                        state = localState.oyuncu3,
                        kompakt = kompakt,
                        rahmenFarbe = Color(0xFF81D4FA),
                        onStateChange = { localStateGuncelle { durum -> durum.copy(oyuncu3 = it) } }
                    )
                    SpielerEndeKart(
                        modifier = Modifier.weight(1f),
                        state = localState.oyuncu4,
                        kompakt = kompakt,
                        rahmenFarbe = Color(0xFFC62828),
                        onStateChange = { localStateGuncelle { durum -> durum.copy(oyuncu4 = it) } }
                    )
                }
            }

            Spacer(modifier = Modifier.height(buttonPadding))

            Button(
                onClick = { onSpeichernClick(localState) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Speichern",
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SpielerEndeKart(
    modifier: Modifier = Modifier,
    state: SpielerRundenEndeUiState,
    kompakt: Boolean,
    rahmenFarbe: Color,
    onStateChange: (SpielerRundenEndeUiState) -> Unit
) {
    val headerSpacer = if (kompakt) 4.dp else 10.dp
    val checkboxGap = 0.dp
    val fieldVerticalPadding = if (kompakt) 6.dp else 14.dp

    Column(
        modifier = modifier
            .border(
                width = 2.dp,
                color = rahmenFarbe,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = state.spielerName.ifBlank { "-" },
            style = MaterialTheme.typography.titleLarge,
            color = rahmenFarbe,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(headerSpacer))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.eingabeText,
                onValueChange = {
                    onStateChange(state.copy(eingabeText = it))
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Wert") },
                enabled = !state.acamadi,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = fieldVerticalPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.multiplikatorText.ifBlank { "×1" },
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(headerSpacer))

        CheckboxSatiri(
            text = "cift",
            checked = state.cift,
            onCheckedChange = {
                if (!state.acamadi) {
                    onStateChange(state.copy(cift = it))
                }
            }
        )
        Spacer(modifier = Modifier.height(checkboxGap))
        CheckboxSatiri(
            text = "bitti",
            checked = state.bitti,
            onCheckedChange = {
                if (!state.acamadi) {
                    onStateChange(state.copy(bitti = it))
                }
            }
        )
        Spacer(modifier = Modifier.height(checkboxGap))
        CheckboxSatiri(
            text = "okeyle",
            checked = state.okeyle,
            onCheckedChange = {
                if (!state.acamadi) {
                    onStateChange(state.copy(okeyle = it))
                }
            }
        )
        Spacer(modifier = Modifier.height(checkboxGap))
        CheckboxSatiri(
            text = "acamadi",
            checked = state.acamadi,
            onCheckedChange = {
                if (it) {
                    onStateChange(
                        state.copy(
                            eingabeText = "202",
                            cift = false,
                            bitti = false,
                            okeyle = false,
                            acamadi = true,
                            eldenBitti = false
                        )
                    )
                } else {
                    onStateChange(
                        state.copy(
                            eingabeText = "",
                            acamadi = false
                        )
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(checkboxGap))
        CheckboxSatiri(
            text = "elden bitti",
            checked = state.eldenBitti,
            onCheckedChange = {
                if (!state.acamadi) {
                    onStateChange(state.copy(eldenBitti = it))
                }
            }
        )
    }
}

@Composable
private fun CheckboxSatiri(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.height(34.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(0.dp)
        )
        Text(text = text)
    }
}

private fun rundeBeendenKurallariUygula(uiState: RundeBeendenUiState): RundeBeendenUiState {
    var oyuncu1 = oyuncuNormalleştir(uiState.oyuncu1)
    var oyuncu2 = oyuncuNormalleştir(uiState.oyuncu2)
    var oyuncu3 = oyuncuNormalleştir(uiState.oyuncu3)
    var oyuncu4 = oyuncuNormalleştir(uiState.oyuncu4)

    if (uiState.mod == "ortak") {
        if (oyuncu1.eldenBitti) {
            oyuncu2 = oyuncuAcamadiYap(oyuncu2)
            oyuncu4 = oyuncuAcamadiYap(oyuncu4)
        }
        if (oyuncu2.eldenBitti) {
            oyuncu1 = oyuncuAcamadiYap(oyuncu1)
            oyuncu3 = oyuncuAcamadiYap(oyuncu3)
        }
        if (oyuncu3.eldenBitti) {
            oyuncu2 = oyuncuAcamadiYap(oyuncu2)
            oyuncu4 = oyuncuAcamadiYap(oyuncu4)
        }
        if (oyuncu4.eldenBitti) {
            oyuncu1 = oyuncuAcamadiYap(oyuncu1)
            oyuncu3 = oyuncuAcamadiYap(oyuncu3)
        }
    } else {
        if (oyuncu1.eldenBitti) {
            oyuncu2 = oyuncuAcamadiYap(oyuncu2)
            oyuncu3 = oyuncuAcamadiYap(oyuncu3)
            oyuncu4 = oyuncuAcamadiYap(oyuncu4)
        }
        if (oyuncu2.eldenBitti) {
            oyuncu1 = oyuncuAcamadiYap(oyuncu1)
            oyuncu3 = oyuncuAcamadiYap(oyuncu3)
            oyuncu4 = oyuncuAcamadiYap(oyuncu4)
        }
        if (oyuncu3.eldenBitti) {
            oyuncu1 = oyuncuAcamadiYap(oyuncu1)
            oyuncu2 = oyuncuAcamadiYap(oyuncu2)
            oyuncu4 = oyuncuAcamadiYap(oyuncu4)
        }
        if (oyuncu4.eldenBitti) {
            oyuncu1 = oyuncuAcamadiYap(oyuncu1)
            oyuncu2 = oyuncuAcamadiYap(oyuncu2)
            oyuncu3 = oyuncuAcamadiYap(oyuncu3)
        }
    }

    oyuncu1 = oyuncuNormalleştir(oyuncu1)
    oyuncu2 = oyuncuNormalleştir(oyuncu2)
    oyuncu3 = oyuncuNormalleştir(oyuncu3)
    oyuncu4 = oyuncuNormalleştir(oyuncu4)

    if (uiState.mod == "ortak") {
        if (oyuncu1.bitti) {
            oyuncu1 = oyuncu1.copy(eingabeText = "-101")
            if (!oyuncu3.bitti && !oyuncu3.acamadi) oyuncu3 = oyuncu3.copy(eingabeText = "0")
        } else if (!oyuncu3.bitti && !oyuncu3.acamadi && oyuncu3.eingabeText == "0") {
            oyuncu3 = oyuncu3.copy(eingabeText = "")
        }

        if (oyuncu3.bitti) {
            oyuncu3 = oyuncu3.copy(eingabeText = "-101")
            if (!oyuncu1.bitti && !oyuncu1.acamadi) oyuncu1 = oyuncu1.copy(eingabeText = "0")
        } else if (!oyuncu1.bitti && !oyuncu1.acamadi && oyuncu1.eingabeText == "0") {
            oyuncu1 = oyuncu1.copy(eingabeText = "")
        }

        if (oyuncu2.bitti) {
            oyuncu2 = oyuncu2.copy(eingabeText = "-101")
            if (!oyuncu4.bitti && !oyuncu4.acamadi) oyuncu4 = oyuncu4.copy(eingabeText = "0")
        } else if (!oyuncu4.bitti && !oyuncu4.acamadi && oyuncu4.eingabeText == "0") {
            oyuncu4 = oyuncu4.copy(eingabeText = "")
        }

        if (oyuncu4.bitti) {
            oyuncu4 = oyuncu4.copy(eingabeText = "-101")
            if (!oyuncu2.bitti && !oyuncu2.acamadi) oyuncu2 = oyuncu2.copy(eingabeText = "0")
        } else if (!oyuncu2.bitti && !oyuncu2.acamadi && oyuncu2.eingabeText == "0") {
            oyuncu2 = oyuncu2.copy(eingabeText = "")
        }
    } else {
        if (oyuncu1.bitti) oyuncu1 = oyuncu1.copy(eingabeText = "-101") else if (!oyuncu1.acamadi && oyuncu1.eingabeText == "-101") oyuncu1 = oyuncu1.copy(eingabeText = "")
        if (oyuncu2.bitti) oyuncu2 = oyuncu2.copy(eingabeText = "-101") else if (!oyuncu2.acamadi && oyuncu2.eingabeText == "-101") oyuncu2 = oyuncu2.copy(eingabeText = "")
        if (oyuncu3.bitti) oyuncu3 = oyuncu3.copy(eingabeText = "-101") else if (!oyuncu3.acamadi && oyuncu3.eingabeText == "-101") oyuncu3 = oyuncu3.copy(eingabeText = "")
        if (oyuncu4.bitti) oyuncu4 = oyuncu4.copy(eingabeText = "-101") else if (!oyuncu4.acamadi && oyuncu4.eingabeText == "-101") oyuncu4 = oyuncu4.copy(eingabeText = "")
    }

    val oyuncular = listOf(oyuncu1, oyuncu2, oyuncu3, oyuncu4)
    val herhangiBirOkeyle = oyuncular.any { it.okeyle }
    val herhangiBirEldenBitti = oyuncular.any { it.eldenBitti }

    oyuncu1 = oyuncu1.copy(multiplikatorText = "×${hesaplaMultiplikator(0, oyuncular, uiState.mod, herhangiBirOkeyle, herhangiBirEldenBitti)}")
    oyuncu2 = oyuncu2.copy(multiplikatorText = "×${hesaplaMultiplikator(1, oyuncular, uiState.mod, herhangiBirOkeyle, herhangiBirEldenBitti)}")
    oyuncu3 = oyuncu3.copy(multiplikatorText = "×${hesaplaMultiplikator(2, oyuncular, uiState.mod, herhangiBirOkeyle, herhangiBirEldenBitti)}")
    oyuncu4 = oyuncu4.copy(multiplikatorText = "×${hesaplaMultiplikator(3, oyuncular, uiState.mod, herhangiBirOkeyle, herhangiBirEldenBitti)}")

    return uiState.copy(
        oyuncu1 = oyuncu1,
        oyuncu2 = oyuncu2,
        oyuncu3 = oyuncu3,
        oyuncu4 = oyuncu4
    )
}

private fun oyuncuNormalleştir(oyuncu: SpielerRundenEndeUiState): SpielerRundenEndeUiState {
    return if (oyuncu.acamadi) {
        oyuncu.copy(
            eingabeText = "202",
            cift = false,
            bitti = false,
            okeyle = false,
            eldenBitti = false
        )
    } else {
        oyuncu.copy(bitti = oyuncu.bitti || oyuncu.okeyle || oyuncu.eldenBitti)
    }
}

private fun oyuncuAcamadiYap(oyuncu: SpielerRundenEndeUiState): SpielerRundenEndeUiState {
    return oyuncu.copy(
        eingabeText = "202",
        cift = false,
        bitti = false,
        okeyle = false,
        acamadi = true,
        eldenBitti = false
    )
}

private fun hesaplaMultiplikator(
    index: Int,
    oyuncular: List<SpielerRundenEndeUiState>,
    mod: String,
    herhangiBirOkeyle: Boolean,
    herhangiBirEldenBitti: Boolean
): Int {
    val oyuncu = oyuncular[index]
    var carpan = 1

    if (oyuncu.cift) carpan *= 2
    if (herhangiBirOkeyle) carpan *= 2
    if (herhangiBirEldenBitti) carpan *= 2
    if (rakipteCiftBittiVar(index, oyuncular, mod)) carpan *= 2

    return carpan
}

private fun rakipteCiftBittiVar(
    index: Int,
    oyuncular: List<SpielerRundenEndeUiState>,
    mod: String
): Boolean {
    val rakipIndexleri = if (mod == "ortak") {
        when (index) {
            0, 2 -> listOf(1, 3)
            else -> listOf(0, 2)
        }
    } else {
        oyuncular.indices.filter { it != index }
    }

    return rakipIndexleri.any { rakipIndex ->
        oyuncular[rakipIndex].cift && oyuncular[rakipIndex].bitti
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RundeBeendenEkraniPreview() {
    NEX101Theme {
        RundeBeendenEkrani(
            uiState = RundeBeendenUiState(
                turNo = 3,
                mod = "ortak",
                oyuncu1 = SpielerRundenEndeUiState(spielerName = "Eren", multiplikatorText = "×1"),
                oyuncu2 = SpielerRundenEndeUiState(spielerName = "Semir", multiplikatorText = "×1"),
                oyuncu3 = SpielerRundenEndeUiState(spielerName = "Erol", multiplikatorText = "×1"),
                oyuncu4 = SpielerRundenEndeUiState(spielerName = "Eray", multiplikatorText = "×1")
            ),
            onGeriClick = {},
            onSpeichernClick = {}
        )
    }
}