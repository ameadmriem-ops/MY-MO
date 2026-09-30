package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.model.Anime
import com.example.model.Episode
import com.example.model.Season
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.AnimeCrimson
import com.example.ui.theme.AnimeCyan
import com.example.ui.theme.AnimeGold
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AdminViewModel

// -------------------------------------------------------------
// Admin Dashboard Screen
// -------------------------------------------------------------
@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel,
    onBackClick: () -> Unit,
    onAddAnimeClick: () -> Unit,
    onEditAnimeClick: (Anime) -> Unit,
    onManageEpisodesClick: (Anime) -> Unit
) {
    val context = LocalContext.current
    val uiState by adminViewModel.uiState.collectAsState()
    val allAnimeList by adminViewModel.allAnime.collectAsState()

    var animeToDelete by remember { mutableStateOf<Anime?>(null) }

    BackHandler { onBackClick() }

    LaunchedEffect(uiState.actionSuccessMessage) {
        uiState.actionSuccessMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            adminViewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stringResource(R.string.admin_dashboard),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "إدارة المحتوى المباشر والإحصائيات",
                        style = MaterialTheme.typography.labelSmall.copy(color = AnimeGold)
                    )
                }
            }

            Button(
                onClick = onAddAnimeClick,
                colors = ButtonDefaults.buttonColors(containerColor = AnimeCrimson),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("admin_add_anime_btn")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة أنمي", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AdminStatCard(
                title = "المستخدمين",
                value = "${uiState.stats.totalUsers}",
                icon = Icons.Filled.People,
                accentColor = AnimeCyan,
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "الأنميات",
                value = "${uiState.stats.totalAnime}",
                icon = Icons.Filled.Movie,
                accentColor = AnimeCrimson,
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "المشاهدات",
                value = "${uiState.stats.totalViews}",
                icon = Icons.Filled.Visibility,
                accentColor = AnimeGold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "قائمة الأنميات المضافة (${allAnimeList.size})",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (allAnimeList.isEmpty()) {
            EmptyStateView(
                message = "لا توجد أنميات مضافة بعد. اضغط على 'إضافة أنمي' لإضافة أول أنمي!",
                actionButtonText = "إضافة أنمي الآن",
                onActionClick = onAddAnimeClick,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(allAnimeList, key = { it.id }) { anime ->
                    AdminAnimeItemRow(
                        anime = anime,
                        onEdit = { onEditAnimeClick(anime) },
                        onManageEpisodes = { onManageEpisodesClick(anime) },
                        onDelete = { animeToDelete = anime }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (animeToDelete != null) {
        val target = animeToDelete!!
        AlertDialog(
            onDismissRequest = { animeToDelete = null },
            title = { Text(stringResource(R.string.admin_delete_confirm), color = TextPrimary) },
            text = { Text("هل تريد بالتأكيد حذف '${target.title}' وجميع مواسمه وحلقاته من قاعدة البيانات؟", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        adminViewModel.deleteAnime(target.id)
                        animeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("نعم، حذف", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { animeToDelete = null }) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun AdminStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(accentColor.copy(alpha = 0.5f), DarkCardBorder)))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun AdminAnimeItemRow(
    anime: Anime,
    onEdit: () -> Unit,
    onManageEpisodes: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkCardBorder, Color.Transparent)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 74.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = anime.posterUrl,
                    contentDescription = anime.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = anime.title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${anime.year} • ${anime.status} • ${anime.viewsCount} مشاهدة",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                    maxLines = 1
                )
                if (anime.featured) {
                    Text(
                        text = "⭐ مثبت في البانر المميز",
                        style = MaterialTheme.typography.labelSmall.copy(color = AnimeGold, fontSize = 10.sp)
                    )
                }
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Manage Episodes
                IconButton(
                    onClick = onManageEpisodes,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(AnimeCyan.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.VideoLibrary,
                        contentDescription = "Episodes",
                        tint = AnimeCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Edit
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ErrorRed.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = ErrorRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Admin Anime Form Screen (Add / Edit)
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminAnimeFormScreen(
    adminViewModel: AdminViewModel,
    animeToEdit: Anime?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by adminViewModel.uiState.collectAsState()

    BackHandler { onBackClick() }

    var title by remember { mutableStateOf(animeToEdit?.title ?: "") }
    var englishTitle by remember { mutableStateOf(animeToEdit?.englishTitle ?: "") }
    var description by remember { mutableStateOf(animeToEdit?.description ?: "") }
    var posterUrl by remember { mutableStateOf(animeToEdit?.posterUrl ?: "") }
    var bannerUrl by remember { mutableStateOf(animeToEdit?.bannerUrl ?: "") }
    var yearText by remember { mutableStateOf((animeToEdit?.year ?: 2024).toString()) }
    var status by remember { mutableStateOf(animeToEdit?.status ?: "مستمر") }
    var genresText by remember { mutableStateOf(animeToEdit?.genres?.joinToString(", ") ?: "أكشن, مغامرات") }
    var ageRating by remember { mutableStateOf(animeToEdit?.ageRating ?: "+13") }
    var ratingText by remember { mutableStateOf((animeToEdit?.rating ?: 8.5).toString()) }
    var isFeatured by remember { mutableStateOf(animeToEdit?.featured ?: false) }
    var isPopular by remember { mutableStateOf(animeToEdit?.popular ?: true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (animeToEdit == null) "إضافة أنمي جديد" else "تعديل أنمي: ${animeToEdit.title}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Form Fields
        AdminFormField(
            label = "اسم الأنمي بالعربية",
            value = title,
            onValueChange = { title = it },
            placeholder = "مثال: ون بيس"
        )

        AdminFormField(
            label = "اسم الأنمي بالإنجليزية",
            value = englishTitle,
            onValueChange = { englishTitle = it },
            placeholder = "مثال: One Piece"
        )

        AdminFormField(
            label = "قصة ووصف الأنمي",
            value = description,
            onValueChange = { description = it },
            placeholder = "وصف مختصر ومشوق للأنمي...",
            singleLine = false
        )

        AdminFormField(
            label = "رابط صورة البوستر (Poster URL)",
            value = posterUrl,
            onValueChange = { posterUrl = it },
            placeholder = "https://example.com/poster.jpg"
        )

        AdminFormField(
            label = "رابط صورة البانر العريض (Banner URL)",
            value = bannerUrl,
            onValueChange = { bannerUrl = it },
            placeholder = "https://example.com/banner.jpg"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AdminFormField(
                label = "سنة الإصدار",
                value = yearText,
                onValueChange = { yearText = it },
                modifier = Modifier.weight(1f)
            )
            AdminFormField(
                label = "التقييم (من 10)",
                value = ratingText,
                onValueChange = { ratingText = it },
                modifier = Modifier.weight(1f)
            )
        }

        AdminFormField(
            label = "التصنيفات (مفصولة بفواصل)",
            value = genresText,
            onValueChange = { genresText = it },
            placeholder = "أكشن, مغامرات, شياطين, فانتازيا"
        )

        // Status Selector (مستمر / مكتمل)
        Text("حالة الأنمي", style = MaterialTheme.typography.labelMedium.copy(color = TextSecondary))
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("مستمر", "مكتمل").forEach { s ->
                FilterChip(
                    selected = status == s,
                    onClick = { status = s },
                    label = { Text(s) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AnimeCyan,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Switches for Featured & Popular
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("عرض في البانر الرئيسي (Featured)", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                    Switch(
                        checked = isFeatured,
                        onCheckedChange = { isFeatured = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AnimeCrimson, checkedTrackColor = AnimeCrimson.copy(alpha = 0.5f))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("إضافة لقسم الأكثر مشاهدة (Popular)", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                    Switch(
                        checked = isPopular,
                        onCheckedChange = { isPopular = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AnimeCyan, checkedTrackColor = AnimeCyan.copy(alpha = 0.5f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = {
                val genresList = genresText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                val animeObj = Anime(
                    id = animeToEdit?.id ?: "",
                    title = title.trim(),
                    englishTitle = englishTitle.trim(),
                    description = description.trim(),
                    posterUrl = posterUrl.trim(),
                    bannerUrl = bannerUrl.trim(),
                    year = yearText.toIntOrNull() ?: 2024,
                    status = status,
                    genres = genresList,
                    ageRating = ageRating,
                    rating = ratingText.toDoubleOrNull() ?: 8.5,
                    featured = isFeatured,
                    popular = isPopular,
                    createdAt = animeToEdit?.createdAt ?: System.currentTimeMillis()
                )

                adminViewModel.saveAnime(animeObj) {
                    onBackClick()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("admin_save_anime_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = AnimeCrimson),
            shape = RoundedCornerShape(12.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
            } else {
                Icon(Icons.Filled.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.admin_save),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// -------------------------------------------------------------
// Admin Episodes & Seasons Screen
// -------------------------------------------------------------
@Composable
fun AdminEpisodesScreen(
    adminViewModel: AdminViewModel,
    anime: Anime,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val uiState by adminViewModel.uiState.collectAsState()

    BackHandler { onBackClick() }

    LaunchedEffect(anime.id) {
        adminViewModel.selectAnimeForEditing(anime)
    }

    var showAddSeasonDialog by remember { mutableStateOf(false) }
    var newSeasonNumber by remember { mutableIntStateOf(1) }
    var newSeasonTitle by remember { mutableStateOf("") }

    var episodeToEdit by remember { mutableStateOf<Episode?>(null) }
    var isEpisodeFormVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "إدارة حلقات: ${anime.title}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "تعديل روابط الفيديو والبث المباشر",
                        style = MaterialTheme.typography.labelSmall.copy(color = AnimeCyan)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seasons Row with Add Season Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("المواسم", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary))

            TextButton(onClick = {
                newSeasonNumber = uiState.seasons.size + 1
                newSeasonTitle = "الموسم $newSeasonNumber"
                showAddSeasonDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = AnimeCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة موسم", color = AnimeCyan, style = MaterialTheme.typography.labelSmall)
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(uiState.seasons, key = { it.id }) { season ->
                val isSelected = uiState.selectedSeason?.id == season.id
                FilterChip(
                    selected = isSelected,
                    onClick = { adminViewModel.selectSeason(season) },
                    label = { Text(season.title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AnimeCrimson,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Episodes Header with Add Episode Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "الحلقات (${uiState.episodes.size})",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Button(
                onClick = {
                    episodeToEdit = null
                    isEpisodeFormVisible = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = AnimeCrimson),
                shape = RoundedCornerShape(8.dp),
                enabled = uiState.selectedSeason != null
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة حلقة", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Episodes List
        if (uiState.episodes.isEmpty()) {
            EmptyStateView(
                message = "لا توجد حلقات في هذا الموسم بعد. أضف حلقة وضع رابط الفيديو الخاص بها.",
                actionButtonText = "إضافة حلقة الآن",
                onActionClick = {
                    episodeToEdit = null
                    isEpisodeFormVisible = true
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.episodes, key = { it.id }) { ep ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "الحلقة ${ep.episodeNumber}: ${ep.title}",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "رابط الفيديو: ${ep.videoUrl.take(35)}...",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                                    maxLines = 1
                                )
                                if (ep.alternativeVideoUrl.isNotBlank()) {
                                    Text(
                                        text = "الرابط البديل: متوفر ✓",
                                        style = MaterialTheme.typography.labelSmall.copy(color = AnimeCyan, fontSize = 10.sp)
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = {
                                    episodeToEdit = ep
                                    isEpisodeFormVisible = true
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = AnimeCyan, modifier = Modifier.size(20.dp))
                                }

                                IconButton(onClick = {
                                    val selSeason = uiState.selectedSeason ?: return@IconButton
                                    adminViewModel.deleteEpisode(anime.id, selSeason.id, ep.id)
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Season Dialog
    if (showAddSeasonDialog) {
        Dialog(onDismissRequest = { showAddSeasonDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface,
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(DarkCardBorder, DarkCardBorder)))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("إضافة موسم جديد", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                    Spacer(modifier = Modifier.height(12.dp))
                    AdminFormField(
                        label = "عنوان الموسم",
                        value = newSeasonTitle,
                        onValueChange = { newSeasonTitle = it },
                        placeholder = "الموسم 1"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddSeasonDialog = false }) {
                            Text("إلغاء", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                adminViewModel.addSeason(anime.id, newSeasonNumber, newSeasonTitle)
                                showAddSeasonDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AnimeCyan)
                        ) {
                            Text("إضافة", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Episode Add / Edit Dialog
    if (isEpisodeFormVisible) {
        val selectedSeason = uiState.selectedSeason
        if (selectedSeason != null) {
            EpisodeFormDialog(
                animeId = anime.id,
                seasonId = selectedSeason.id,
                existingEpisode = episodeToEdit,
                defaultEpNumber = uiState.episodes.size + 1,
                defaultThumbnail = anime.posterUrl,
                onDismiss = { isEpisodeFormVisible = false },
                onSave = { episodeObj ->
                    adminViewModel.saveEpisode(episodeObj) {
                        isEpisodeFormVisible = false
                    }
                }
            )
        }
    }
}

@Composable
fun EpisodeFormDialog(
    animeId: String,
    seasonId: String,
    existingEpisode: Episode?,
    defaultEpNumber: Int,
    defaultThumbnail: String,
    onDismiss: () -> Unit,
    onSave: (Episode) -> Unit
) {
    var episodeNumberText by remember { mutableStateOf((existingEpisode?.episodeNumber ?: defaultEpNumber).toString()) }
    var title by remember { mutableStateOf(existingEpisode?.title ?: "الحلقة $defaultEpNumber") }
    var description by remember { mutableStateOf(existingEpisode?.description ?: "") }
    var videoUrl by remember { mutableStateOf(existingEpisode?.videoUrl ?: "") }
    var alternativeVideoUrl by remember { mutableStateOf(existingEpisode?.alternativeVideoUrl ?: "") }
    var thumbnailUrl by remember { mutableStateOf(existingEpisode?.thumbnailUrl ?: defaultThumbnail) }
    var duration by remember { mutableStateOf(existingEpisode?.duration ?: "24:00") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(AnimeCrimson.copy(alpha = 0.5f), DarkCardBorder)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = if (existingEpisode == null) "إضافة حلقة جديدة" else "تعديل الحلقة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminFormField(
                        label = "رقم الحلقة",
                        value = episodeNumberText,
                        onValueChange = { episodeNumberText = it },
                        modifier = Modifier.width(90.dp)
                    )
                    AdminFormField(
                        label = "عنوان الحلقة",
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                AdminFormField(
                    label = "رابط الفيديو المباشر (MP4 / M3U8)",
                    value = videoUrl,
                    onValueChange = { videoUrl = it },
                    placeholder = "https://server.com/episode.mp4"
                )

                AdminFormField(
                    label = "رابط الفيديو البديل (سيرفر احتياطي)",
                    value = alternativeVideoUrl,
                    onValueChange = { alternativeVideoUrl = it },
                    placeholder = "https://backup.com/episode.m3u8"
                )

                AdminFormField(
                    label = "مدة الحلقة",
                    value = duration,
                    onValueChange = { duration = it },
                    placeholder = "24:30"
                )

                AdminFormField(
                    label = "صورة الحلقة المصغرة",
                    value = thumbnailUrl,
                    onValueChange = { thumbnailUrl = it },
                    placeholder = "https://example.com/thumb.jpg"
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val epObj = Episode(
                                id = existingEpisode?.id ?: "",
                                animeId = animeId,
                                seasonId = seasonId,
                                episodeNumber = episodeNumberText.toIntOrNull() ?: 1,
                                title = title.trim(),
                                description = description.trim(),
                                videoUrl = videoUrl.trim(),
                                alternativeVideoUrl = alternativeVideoUrl.trim(),
                                thumbnailUrl = thumbnailUrl.trim(),
                                duration = duration.trim(),
                                createdAt = existingEpisode?.createdAt ?: System.currentTimeMillis()
                            )
                            onSave(epObj)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AnimeCrimson)
                    ) {
                        Text("حفظ الحلقة", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true
) {
    Column(modifier = modifier.padding(vertical = 5.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AnimeCrimson,
                unfocusedBorderColor = DarkCardBorder,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = singleLine,
            maxLines = if (singleLine) 1 else 4
        )
    }
}
