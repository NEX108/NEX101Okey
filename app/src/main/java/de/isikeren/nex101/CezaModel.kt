package de.isikeren.nex101

enum class CezaTipi(val gorunenAd: String, val varsayilanPuan: Int?) {
    ISLEK_ATTI("islek atti", 101),
    OKEY_ATTI("okey atti", 101),
    ACAMADI_CEZA("acamadi", 101),
    OKEY_ELDE_PATLADI("okey elde", 101),
    OKEY_CALDIRDI("okey caldirdi", 101),
    TAS_CEKILDI("tas cekildi", null),
    DIGER("diger", null)
}

enum class CezaSecimRolu {
    YOK,
    KIRMIZI,
    YESIL
}

data class CezaOyuncuSecimi(
    val oyuncuId: Int? = null,
    val oyuncuAdi: String = "",
    val takimRengiArgb: Long = 0xFF81D4FAL,
    val secimRolu: CezaSecimRolu = CezaSecimRolu.YOK
)

data class CezaEkraniUiState(
    val turNo: Int,
    val mod: String,
    val duzenlenenCezaId: Int? = null,
    val seciliCezaTipi: CezaTipi? = null,
    val hedefOyuncular: List<CezaOyuncuSecimi> = emptyList(),
    val puanText: String = "",
    val tasDegeriText: String = "",
    val digerDegerText: String = ""
)

data class CezaKaydi(
    val id: Int,
    val turNo: Int,
    val cezaTipi: CezaTipi,
    val puan: Int,
    val kirmiziOyuncuId: Int? = null,
    val kirmiziOyuncuAdi: String,
    val kirmiziTakimRengiArgb: Long,
    val yesilOyuncuId: Int? = null,
    val yesilOyuncuAdi: String? = null,
    val yesilTakimRengiArgb: Long? = null
)