package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.UserProfile
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyaltyGold
import com.example.ui.theme.SlateDark800
import com.example.ui.theme.SlateDark900
import com.example.ui.theme.SlateDark950
import org.json.JSONArray
import org.json.JSONObject

enum class MapTileStyle(val label: String, val urlTemplate: String, val attribution: String) {
    DARK_MODE("Dark Mode", "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png", "&copy; CARTO"),
    STANDARD_STREET("Standard Street", "https://tile.openstreetmap.org/{z}/{x}/{y}.png", "&copy; OpenStreetMap"),
    CARTO_VOYAGER("CartoDB Voyager", "https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png", "&copy; CARTO"),
    ESRI_SATELLITE("Esri Satellite", "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}", "Esri World Imagery")
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InteractiveMapCanvas(
    users: List<UserProfile>,
    currentUser: UserProfile,
    onUserSelected: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var selectedTileStyle by remember { mutableStateOf(MapTileStyle.DARK_MODE) }
    var showTileMenu by remember { mutableStateOf(false) }

    val filteredUsers = remember(users, currentUser) {
        users.filter { user ->
            if (user.userId == currentUser.userId) return@filter true
            if (user.privacy.ghostMode) return@filter false
            if (!user.privacy.globalLocationSharing && !user.privacy.friendsOnlyLocation) return@filter false
            if (currentUser.privacy.blockedFriendIds.contains(user.userId)) return@filter false
            true
        }
    }

    class WebAppInterface {
        @JavascriptInterface
        fun onMarkerClick(userId: String) {
            val clicked = filteredUsers.find { it.userId == userId }
            if (clicked != null) {
                onUserSelected(clicked)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("interactive_map_canvas")) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            updateMapPins(this@apply, filteredUsers, currentUser)
                        }
                    }
                    addJavascriptInterface(WebAppInterface(), "AndroidBridge")
                    val leafletHtml = generateLeafletHtml(selectedTileStyle, currentUser)
                    loadDataWithBaseURL("https://leafletjs.com", leafletHtml, "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            update = { webView ->
                updateMapPins(webView, filteredUsers, currentUser)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Map Controls
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tile Switcher Dropdown
            Box {
                SmallFloatingActionButton(
                    onClick = { showTileMenu = !showTileMenu },
                    containerColor = SlateDark900,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("map_tile_switcher_btn")
                ) {
                    Icon(imageVector = Icons.Default.Layers, contentDescription = "Tile Switcher")
                }

                DropdownMenu(
                    expanded = showTileMenu,
                    onDismissRequest = { showTileMenu = false },
                    modifier = Modifier.background(SlateDark900)
                ) {
                    MapTileStyle.entries.forEach { style ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = style.label,
                                    color = if (style == selectedTileStyle) RoyaltyGold else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (style == selectedTileStyle) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                selectedTileStyle = style
                                showTileMenu = false
                                webViewRef?.let { wv ->
                                    val script = "setTileLayer('${style.urlTemplate}', '${style.attribution}');"
                                    wv.evaluateJavascript(script, null)
                                }
                            }
                        )
                    }
                }
            }

            // Fly to My Location Button
            SmallFloatingActionButton(
                onClick = {
                    webViewRef?.let { wv ->
                        val script = "flyToLocation(${currentUser.lat}, ${currentUser.lng}, 13);"
                        wv.evaluateJavascript(script, null)
                    }
                },
                containerColor = SlateDark900,
                contentColor = EmeraldGreen,
                modifier = Modifier.testTag("map_my_location_btn")
            ) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = "My Location")
            }

            // Fly to World View Button
            SmallFloatingActionButton(
                onClick = {
                    webViewRef?.let { wv ->
                        val script = "flyToLocation(20.0, 0.0, 2);"
                        wv.evaluateJavascript(script, null)
                    }
                },
                containerColor = SlateDark900,
                contentColor = RoyaltyGold,
                modifier = Modifier.testTag("map_world_view_btn")
            ) {
                Icon(imageVector = Icons.Default.Public, contentDescription = "World View")
            }
        }

        // Active Tile Style Pill Indicator
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = SlateDark900.copy(alpha = 0.9f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Map Layer: ${selectedTileStyle.label}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun updateMapPins(webView: WebView, users: List<UserProfile>, currentUser: UserProfile) {
    val jsonArray = JSONArray()
    users.forEach { u ->
        val obj = JSONObject().apply {
            put("userId", u.userId)
            put("fullName", u.fullName)
            put("stream", "${u.shsSchool} (${u.stream})")
            put("city", u.city)
            put("lat", u.lat)
            put("lng", u.lng)
            put("isSelf", u.userId == currentUser.userId)
            put("isOnline", u.isOnline)
        }
        jsonArray.put(obj)
    }
    val script = "updatePins(${jsonArray});"
    webView.evaluateJavascript(script, null)
}

private fun generateLeafletHtml(tileStyle: MapTileStyle, currentUser: UserProfile): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map { height: 100%; width: 100%; margin: 0; padding: 0; background: #020617; }
                .custom-pin {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    background: #0f172a;
                    border: 2px solid #6366f1;
                    border-radius: 50%;
                    color: white;
                    font-weight: bold;
                    font-family: sans-serif;
                    font-size: 11px;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.6);
                }
                .custom-pin.self-pin { border-color: #f59e0b; background: #1e293b; }
                .online-dot {
                    width: 10px; height: 10px; background: #10b981; border-radius: 50%;
                    position: absolute; bottom: 0; right: 0; border: 2px solid #0f172a;
                }
                .leaflet-popup-content-wrapper { background: #0f172a; color: #f8fafc; border-radius: 12px; }
                .leaflet-popup-tip { background: #0f172a; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', { zoomControl: false }).setView([${currentUser.lat}, ${currentUser.lng}], 12);
                var currentTileLayer = L.tileLayer('${tileStyle.urlTemplate}', {
                    maxZoom: 19,
                    attribution: '${tileStyle.attribution}'
                }).addTo(map);

                var markersGroup = L.layerGroup().addTo(map);

                function setTileLayer(url, attr) {
                    if (currentTileLayer) map.removeLayer(currentTileLayer);
                    currentTileLayer = L.tileLayer(url, { maxZoom: 19, attribution: attr }).addTo(map);
                }

                function flyToLocation(lat, lng, zoom) {
                    map.flyTo([lat, lng], zoom, { duration: 1.5 });
                }

                function updatePins(usersData) {
                    markersGroup.clearLayers();
                    usersData.forEach(function(u) {
                        var initials = u.fullName ? u.fullName.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase() : 'RC';
                        var pinClass = u.isSelf ? 'custom-pin self-pin' : 'custom-pin';
                        var html = '<div class="' + pinClass + '" style="width:36px; height:36px; position:relative;">' + initials +
                                   '<div class="online-dot"></div></div>';
                        var customIcon = L.divIcon({
                            html: html,
                            className: '',
                            iconSize: [36, 36],
                            iconAnchor: [18, 18]
                        });

                        var marker = L.marker([u.lat, u.lng], { icon: customIcon }).addTo(markersGroup);
                        var popupText = '<b>' + u.fullName + '</b><br/><small>' + u.stream + '</small><br/>📍 ' + u.city;
                        marker.bindPopup(popupText);
                        marker.on('click', function() {
                            if (window.AndroidBridge) {
                                window.AndroidBridge.onMarkerClick(u.userId);
                            }
                        });
                    });
                }
                setTimeout(function(){ map.invalidateSize(); }, 500);
            </script>
        </body>
        </html>
    """.trimIndent()
}
