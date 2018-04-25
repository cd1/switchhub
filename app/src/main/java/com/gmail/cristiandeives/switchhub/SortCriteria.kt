package com.gmail.cristiandeives.switchhub

internal enum class SortCriteria(val sortBy: By, val direction: Direction) {
    FEATURED(By.FEATURED, Direction.ASCENDING),
    TITLE(By.TITLE, Direction.ASCENDING),
    RELEASE_DATE(By.RELEASE_DATE, Direction.DESCENDING),
    LOWEST_PRICE(By.PRICE, Direction.ASCENDING),
    HIGHEST_PRICE(By.PRICE, Direction.DESCENDING);

    enum class By {
        FEATURED,
        TITLE,
        PRICE,
        RELEASE_DATE;
    }

    enum class Direction {
        ASCENDING,
        DESCENDING;
    }
}