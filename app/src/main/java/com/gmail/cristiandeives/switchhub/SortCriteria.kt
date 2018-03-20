package com.gmail.cristiandeives.switchhub

import com.gmail.cristiandeives.switchhub.http.NintendoEshopService

internal enum class SortCriteria(val sortBy: NintendoEshopService.SortBy, val sortDirection: NintendoEshopService.SortDirection) {
    FEATURED(NintendoEshopService.SortBy.Featured, NintendoEshopService.SortDirection.Descending),
    TITLE(NintendoEshopService.SortBy.Title, NintendoEshopService.SortDirection.Ascending),
    RELEASE_DATE(NintendoEshopService.SortBy.ReleaseDate, NintendoEshopService.SortDirection.Descending),
    LOWEST_PRICE(NintendoEshopService.SortBy.Price, NintendoEshopService.SortDirection.Ascending),
    HIGHEST_PRICE(NintendoEshopService.SortBy.Price, NintendoEshopService.SortDirection.Descending)
}