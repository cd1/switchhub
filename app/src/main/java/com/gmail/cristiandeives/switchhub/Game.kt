package com.gmail.cristiandeives.switchhub

import java.math.BigDecimal
import java.util.Date

internal data class Game(
    val nsuid: String? = null,
    val title: String,
    val releaseDate: Date?,
    val price: BigDecimal? = null,
    val frontBoxArtUrl: String
)
