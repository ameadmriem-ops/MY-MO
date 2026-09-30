package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.config.AppConfig
import com.example.ui.theme.AnimeCrimson
import com.example.ui.theme.AnimeCyan
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onBackClick() }

    var autoPlayNext by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(true) }
    var selectedQuality by remember { mutableStateOf("1080p") }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

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
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
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
                text = "الإعدادات العامة",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Playback Preferences
        Text(
            text = "تفضيلات التشغيل",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = AnimeCyan
            ),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Auto Play Next Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PlayCircleOutline, contentDescription = null, tint = AnimeCyan)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("تشغيل الحلقة التالية تلقائياً", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                            Text("الانتقال للحلقة التالية عند نهاية المشاهدة", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                        }
                    }
                    Switch(
                        checked = autoPlayNext,
                        onCheckedChange = { autoPlayNext = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AnimeCrimson,
                            checkedTrackColor = AnimeCrimson.copy(alpha = 0.5f)
                        )
                    )
                }

                // Dark Mode Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.DarkMode, contentDescription = null, tint = AnimeCyan)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("الوضع الليلي (Dark Mode)", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                            Text("مفعل دائماً لتجربة بصرية سينمائية", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                        }
                    }
                    Switch(
                        checked = darkModeEnabled,
                        onCheckedChange = { darkModeEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AnimeCrimson,
                            checkedTrackColor = AnimeCrimson.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Data & Storage
        Text(
            text = "البيانات والتخزين",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = AnimeCyan
            ),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Toast.makeText(context, "تم مسح الذاكرة المؤقتة (Cache) بنجاح!", Toast.LENGTH_SHORT).show()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.CleaningServices, contentDescription = null, tint = AnimeCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("مسح الذاكرة المؤقتة (Clear Cache)", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                        Text("تحرير مساحة التخزين المؤقتة للصور", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Legal & About
        Text(
            text = "حول التطبيق والمعلومات",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = AnimeCyan
            ),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPrivacyDialog = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Policy, contentDescription = null, tint = AnimeCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("سياسة الخصوصية وشروط الاستخدام", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAboutDialog = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = AnimeCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("حول ${AppConfig.APP_NAME}", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                }
            }
        }

        // About Dialog
        if (showAboutDialog) {
            Dialog(onDismissRequest = { showAboutDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurface,
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(DarkCardBorder, DarkCardBorder)))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = AppConfig.APP_NAME,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "الإصدار ${AppConfig.APP_VERSION}",
                            style = MaterialTheme.typography.labelMedium.copy(color = AnimeCyan)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "تطبيق متكامل لمشاهدة ومتابعة أحدث مسلسلات وأفلام الأنمي بجودة عالية وسيرفرات مباشرة مع دعم الترجمة العربية وحفظ سجل المشاهدة.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        TextButton(onClick = { showAboutDialog = false }) {
                            Text("إغلاق", color = AnimeCrimson)
                        }
                    }
                }
            }
        }

        // Privacy Dialog
        if (showPrivacyDialog) {
            Dialog(onDismissRequest = { showPrivacyDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurface,
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(DarkCardBorder, DarkCardBorder)))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "سياسة الخصوصية",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "نحن نحترم خصوصيتك بالكامل. يتم تشفير بيانات الحساب واستخدامها فقط لتخصيص تجربة المشاهدة ومزامنة المفضلة وسجل المتابعة عبر السحابة بأمان تام.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showPrivacyDialog = false }) {
                                Text("موافق", color = AnimeCyan)
                            }
                        }
                    }
                }
            }
        }
    }
}
