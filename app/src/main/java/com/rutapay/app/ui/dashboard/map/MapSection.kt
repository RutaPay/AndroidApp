package com.rutapay.app.ui.dashboard.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.MultiLineString
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.rememberMapState
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.DoubleListValue
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineCapValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineJoinValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineLayer
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.GeoJsonSourceState
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.rutapay.app.ui.theme.Lexend
import com.rutapay.app.ui.theme.WebCardBorder
import com.rutapay.app.ui.theme.WebDark
import com.rutapay.app.ui.theme.WebMutedText
import com.rutapay.app.ui.theme.WebPrimary
import com.rutapay.app.ui.theme.WebRouteIda
import com.rutapay.app.ui.theme.WebRouteRegreso
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

// Rutas de Celaya tomadas de RutaPayWeb (MapView.vue).
val CelayaRoutes = listOf(
    "R1 - SMO - CELAYA", "R2 - TAMAYO", "R3 - SAN LORENZO", "R7 - EL SAUZ",
    "R8 - SAN JUAN DE LA VEGA", "R8B - RAMAL SJV-5 DE MAYO (RAMAL)", "R9 - HOSPITAL",
    "R10 - PUENTE", "R11 - PRESA BLANCA", "R11 - ESQUIROZ", "R12 - SAN JOSÉ",
    "R13 - LAJA", "R14 - ESTRADA", "R15 - SILVA X IGNACIO GUTIERREZ",
    "R15 - SILVA X TENOCHTITLAN", "R16 - ALAMOS", "R17 - SAN ANTONIO",
    "R18 - SAN JOSE EL NUEVO-PLANCARTE", "R19 - ELGUERA", "R20 - GASCA",
    "R21 - MOLINO DE CRESPO", "R22 - EL PUESTO", "R23 - ROQUE", "R24 - TENERIA",
    "R25 - PLANCARTE", "R26 - PUERTA DEL MONTE CELAYA", "R27 - SAN ELIAS",
    "R28 - ARREGUIN", "R29 - SAN CAYETANO", "R31 - REVOLUCION", "R32 - MONTE BLANCO",
    "R36 - LAURELES", "R37 - VILLAS DEL BAJÍO", "R37B - LAGOS", "R39 - QUINTANA ROO",
    "R40 - DELICIAS", "R41 - HOSP.MAT- CENTRO", "R42 - HOSP.MAT - CENTRAL",
    "R43 - ROMERAL X FOVISSSTE", "R44 - VILLAS DEL BAJIO", "R45 - LOS OLIVOS",
    "R46 - CD.INDUST - CAMPUS - CENTRAL", "R47 - SAN MARTIN DE CAMARGO",
    "R49 - LAS FLORES X INDUSTRIAL", "R50 - LAS FLORES X FOVISSSTE",
    "R51 - LA HERRADURA X IRRIGACION", "R52 - LA HERRADURA X TENOCHTITLAN",
    "R53 - ROMERAL X INDUSTRIAL", "R54 - SAN LUIS REY X IRRIGACION",
    "R54 - SAN LUIS REY X 16 DE SEPTIEMBRE", "R55 - YUSTIS", "R56 - SAN ELIAS",
    "R58 - PROGRESO", "R60 - PINOS", "R61 - INDUSTRIAL", "R62 - SMO - CD INDUSTRIAL",
    "R63 - UNIVERSIDADES", "R67 - LITTLE SAINT JAMES - CAMINO A KIRK"
)

/**
 * Vista Mapa a pantalla completa (estilo app móvil, estética RutaPayWeb).
 *
 * Solo se muestran el mapa, la barra de búsqueda flotante y la navegación
 * inferior. Las rutas viven en un panel inferior colapsable.
 *
 * El mapa (Mapbox) carga la ruta seleccionada igual que la web: GeoJSON por
 * routeId desde GitHub, IDA sólida #449DD1 y REGRESO discontinuo #052B4A,
 * con la cámara encuadrada a la ruta.
 */
@Composable
fun MapSection(
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color
) {
    var search by remember { mutableStateOf("") }
    var selectedRoute by remember { mutableStateOf<String?>(null) }
    var loadingRoute by remember { mutableStateOf(false) }
    var routeError by remember { mutableStateOf<String?>(null) }
    var reloadTick by remember { mutableIntStateOf(0) }
    var panelExpanded by remember { mutableStateOf(false) }

    val filtered = remember(search) {
        if (search.isBlank()) CelayaRoutes
        else CelayaRoutes.filter { it.contains(search, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ---- Mapa a pantalla completa (carga la ruta como la web) ----
        FullScreenMap(
            selectedRoute = selectedRoute,
            reloadTick = reloadTick,
            onLoadingChange = { loadingRoute = it },
            onError = { routeError = it },
            modifier = Modifier.fillMaxSize()
        )

        // ---- Buscador flotante ----
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Busca una ruta...", color = Color(0xFFC3C3C3), fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WebMutedText) },
                trailingIcon = {
                    if (search.isNotBlank()) {
                        IconButton(onClick = { search = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = WebMutedText)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WebPrimary,
                    unfocusedBorderColor = WebCardBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }

        // ---- Ruta seleccionada (flotante bajo el buscador) ----
        AnimatedVisibility(
            visible = selectedRoute != null,
            enter = expandVertically(),
            exit = shrinkVertically(),
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(top = 96.dp, start = 12.dp, end = 12.dp)
        ) {
            selectedRoute?.let { route ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = WebPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = route,
                                style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WebDark)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RouteLegendDot(color = WebRouteIda, label = "IDA")
                                Spacer(modifier = Modifier.width(10.dp))
                                RouteLegendDot(color = WebRouteRegreso, label = "REGRESO")
                                if (loadingRoute) {
                                    Spacer(modifier = Modifier.width(10.dp))
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = WebPrimary)
                                }
                            }
                        }
                        IconButton(onClick = { selectedRoute = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Quitar ruta", tint = WebMutedText)
                        }
                    }
                }
            }
        }

        // ---- Error de carga (se auto-oculta, no interfiere con lista ni indicador) ----
        AnimatedVisibility(
            visible = routeError != null,
            enter = expandVertically(),
            exit = shrinkVertically(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 120.dp, start = 16.dp, end = 16.dp)
        ) {
            routeError?.let { message ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD93025)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message,
                            style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, color = Color(0xFFD93025)),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reintentar",
                            style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WebPrimary),
                            modifier = Modifier.clickable { reloadTick++ }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(onClick = { routeError = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = WebMutedText, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // ---- Panel inferior de rutas ----
        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { panelExpanded = !panelExpanded }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp, 4.dp)
                            .background(WebCardBorder, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = WebPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rutas",
                            style = TextStyle(fontFamily = Lexend, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WebDark),
                            modifier = Modifier.weight(1f)
                        )
                        Surface(shape = RoundedCornerShape(12.dp), color = WebPrimary.copy(alpha = 0.1f)) {
                            Text(
                                text = "${filtered.size}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                style = TextStyle(fontFamily = Lexend, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WebPrimary)
                            )
                        }
                        Icon(
                            imageVector = if (panelExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                            contentDescription = if (panelExpanded) "Contraer" else "Expandir",
                            tint = WebMutedText
                        )
                    }
                }

                // Lista expandible
                AnimatedVisibility(
                    visible = panelExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 12.dp)
                    ) {
                        items(filtered) { route ->
                            val isSelected = route == selectedRoute
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedRoute = route }
                                    .background(
                                        if (isSelected) WebPrimary.copy(alpha = 0.08f) else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) WebPrimary else Color(0xFFF3F4F6)
                                ) {
                                    Text(
                                        text = route.substringBefore(" - "),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = TextStyle(
                                            fontFamily = Lexend, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else WebDark
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = route.substringAfter(" - ", route),
                                    style = TextStyle(
                                        fontFamily = Lexend,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) WebPrimary else WebDark
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FullScreenMap(
    selectedRoute: String?,
    reloadTick: Int,
    onLoadingChange: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current.density

    // Descarga del GeoJSON igual que la web (MapView.vue: loadRoute).
    var idaFeatures by remember { mutableStateOf<List<Feature>>(emptyList()) }
    var regresoFeatures by remember { mutableStateOf<List<Feature>>(emptyList()) }

    LaunchedEffect(selectedRoute, reloadTick) {
        if (selectedRoute == null) {
            idaFeatures = emptyList()
            regresoFeatures = emptyList()
            onLoadingChange(false)
            onError(null)
            return@LaunchedEffect
        }
        onLoadingChange(true)
        onError(null)
        try {
            val routeId = selectedRoute.substringBefore(" - ")
            val json = withContext(Dispatchers.IO) {
                URL(
                    "https://raw.githubusercontent.com/RutaPay/Routes/refs/heads/main/$routeId.geojson"
                ).readText()
            }
            val features = FeatureCollection.fromJson(json).features() ?: emptyList()
            idaFeatures = features.filter {
                it.getStringProperty("name")?.contains("IDA") == true
            }
            regresoFeatures = features.filter {
                it.getStringProperty("name")?.contains("REGRESO") == true
            }
            onLoadingChange(false)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            idaFeatures = emptyList()
            regresoFeatures = emptyList()
            onError("No se pudo cargar la ruta. Revisa tu conexión e inténtalo de nuevo.")
            onLoadingChange(false)
        }
    }

    val idaSourceState = remember { GeoJsonSourceState("route-ida") }
    val regresoSourceState = remember { GeoJsonSourceState("route-regreso") }
    LaunchedEffect(idaFeatures) {
        idaSourceState.data = GeoJSONData(idaFeatures)
    }
    LaunchedEffect(regresoFeatures) {
        regresoSourceState.data = GeoJSONData(regresoFeatures)
    }

    val mapState = rememberMapState {
        coroutineScope.launch {
            mapLoadingErrorEvents.onEach {
                // Error occurred when loading the map, try to handle it gracefully here
            }
        }
        coroutineScope.launch {
            styleLoadedEvents.first().let {
                // Map is setup and style has loaded, Now you can add data or make other map adjustments.
            }
        }
    }
    MapboxMap(
        Modifier.fillMaxSize(),
        mapState = mapState,
        mapViewportState = rememberMapViewportState {
            setCameraOptions {
                zoom(12.0)
                center(Point.fromLngLat(-100.8140458, 20.521788))
                pitch(0.0)
                bearing(0.0)
            }
        },
        logo = {
            Logo(Modifier.padding(bottom = 40.dp))
        },
        attribution = {
            Attribution(Modifier.padding(bottom = 40.dp))
        }
    ) {
        if (idaFeatures.isNotEmpty()) {
            LineLayer(sourceState = idaSourceState, layerId = "route-ida") {
                lineCap = LineCapValue.ROUND
                lineJoin = LineJoinValue.ROUND
                lineColor = ColorValue(Color(0xFF449DD1))
                lineWidth = DoubleValue(4.0)
            }
        }
        if (regresoFeatures.isNotEmpty()) {
            LineLayer(sourceState = regresoSourceState, layerId = "route-regreso") {
                lineCap = LineCapValue.ROUND
                lineJoin = LineJoinValue.ROUND
                lineColor = ColorValue(Color(0xFF052B4A))
                lineWidth = DoubleValue(4.0)
                lineDasharray = DoubleListValue(listOf(2.0, 2.0))
            }
        }

        MapEffect(idaFeatures, regresoFeatures) { mapView ->
            val points = (idaFeatures + regresoFeatures).flatMap { feature ->
                when (val geometry = feature.geometry()) {
                    is LineString -> geometry.coordinates()
                    is MultiLineString -> geometry.coordinates().flatten()
                    else -> emptyList()
                }
            }
            if (points.size >= 2) {
                val camera = mapView.mapboxMap.cameraForCoordinates(
                    points,
                    EdgeInsets(
                        (120 * density).toDouble(),
                        (16 * density).toDouble(),
                        (190 * density).toDouble(),
                        (16 * density).toDouble()
                    ),
                    null,
                    null
                )
                mapView.camera.easeTo(
                    camera,
                    MapAnimationOptions.mapAnimationOptions { duration(1000L) }
                )
            }
        }
    }
}

@Composable
private fun RouteLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(20.dp, 5.dp).background(color, RoundedCornerShape(3.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = TextStyle(fontFamily = Lexend, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = WebDark)
        )
    }
}
