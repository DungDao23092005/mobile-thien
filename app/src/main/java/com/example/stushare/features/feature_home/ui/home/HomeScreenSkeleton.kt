package com.example.stushare.features.feature_home.ui.home

// File: features/feature_home/ui/home/HomeScreenSkeleton.kt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.stushare.features.feature_home.ui.components.DocumentCardSkeleton
import com.example.stushare.features.feature_home.ui.components.DocumentSectionHeaderSkeleton
import com.example.stushare.features.feature_home.ui.home.HomeHeaderSectionSkeleton

@Composable
fun HomeScreenSkeleton(
    columns: Int,
    modifier: Modifier = Modifier
) {
    if (columns == 1) {
        // --- BỐ CỤC SKELETON CHO MÀN HÌNH HẸP (LAZYCOLUMN) ---
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item { HomeHeaderSectionSkeleton() }

            // Section 1
            item { DocumentSectionHeaderSkeleton() }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(3) { DocumentCardSkeleton() }
                }
            }

            // Section 2
            item { DocumentSectionHeaderSkeleton() }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(3) { DocumentCardSkeleton() }
                }
            }
        }
    } else {
        // --- BỐ CỤC SKELETON CHO MÀN HÌNH RỘNG (LAZYVERTICALGRID) ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item(span = { GridItemSpan(columns) }) { HomeHeaderSectionSkeleton() }
            item(span = { GridItemSpan(columns) }) { Spacer(modifier = Modifier.height(16.dp)) }

            // Section 1
            item(span = { GridItemSpan(columns) }) {
                DocumentSectionHeaderSkeleton(modifier = Modifier.padding(horizontal = 0.dp))
            }
            items(columns) {
                DocumentCardSkeleton(modifier = Modifier.fillMaxWidth())
            }

            // Section 2
            item(span = { GridItemSpan(columns) }) {
                DocumentSectionHeaderSkeleton(modifier = Modifier.padding(horizontal = 0.dp))
            }
            items(columns) {
                DocumentCardSkeleton(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}