package de.isikeren.nex101

data class SpielerRundenEndeUiState(
    val spielerId: Int? = null,
    val spielerName: String = "",
    val eingabeText: String = "",
    val cift: Boolean = false,
    val bitti: Boolean = false,
    val okeyle: Boolean = false,
    val acamadi: Boolean = false,
    val eldenBitti: Boolean = false,
    val multiplikatorText: String = "",
    val berechneterText: String = ""
)

data class RundeBeendenUiState(
    val turNo: Int,
    val mod: String,
    val oyuncu1: SpielerRundenEndeUiState,
    val oyuncu2: SpielerRundenEndeUiState,
    val oyuncu3: SpielerRundenEndeUiState,
    val oyuncu4: SpielerRundenEndeUiState
)

fun oyuncuSonPuaniHesapla(oyuncu: SpielerRundenEndeUiState): Int {
    val temelDeger = oyuncu.eingabeText.toIntOrNull() ?: 0
    val carpan = oyuncu.multiplikatorText
        .removePrefix("×")
        .toIntOrNull() ?: 1
    return temelDeger * carpan
}

fun ortakTakimToplamlariniHesapla(uiState: RundeBeendenUiState): Pair<Int, Int> {
    val takim1Toplami =
        oyuncuSonPuaniHesapla(uiState.oyuncu1) + oyuncuSonPuaniHesapla(uiState.oyuncu3)

    val takim2Toplami =
        oyuncuSonPuaniHesapla(uiState.oyuncu2) + oyuncuSonPuaniHesapla(uiState.oyuncu4)

    return takim1Toplami to takim2Toplami
}