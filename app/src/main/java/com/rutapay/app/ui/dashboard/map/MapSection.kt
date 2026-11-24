package com.rutapay.app.ui.dashboard.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.mapbox.maps.extension.compose.style.layers.generated.CircleLayer
import com.mapbox.maps.extension.compose.style.layers.generated.LineCapValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineJoinValue
import com.mapbox.maps.extension.compose.style.layers.generated.LineLayer
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.GeoJsonSourceState
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.rutapay.app.ui.dashboard.navigation.BottomBarSpace
import com.rutapay.app.ui.theme.Lexend
import com.rutapay.app.ui.theme.WebCardBorder
import com.rutapay.app.ui.theme.WebDark
import com.rutapay.app.ui.theme.WebMutedText
import com.rutapay.app.ui.theme.WebPrimary
import com.rutapay.app.ui.theme.WebRouteIda
import com.rutapay.app.ui.theme.WebRouteRegreso
import com.rutapay.app.ui.theme.WebTextLight
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.hypot

// Los comentarios que representan dos barras "//" Son para funciones oficiales utiles.
// Los comentarios que representan "/*" son para funciones demo o de representacion unicamente.
// todo debajo de ello se puede borrar sin problema.


// ============================================================================
// ESTRUCTURA DE PARADAS Y CARGA DINÁMICA DESDE GEOJSON (ALINEADO CON RUTAPAYWEB)
// ============================================================================

private const val ROUTES_BASE_URL =
    "https://raw.githubusercontent.com/RutaPay/Routes/refs/heads/main"

/**
 * Modelo de datos que representa una Parada de Autobús (Demo).
 */
data class BusStopDemo(
    val id: String,
    val name: String,
    val locationDescription: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

/**
 * Modelo de datos para las paradas oficiales cargadas desde `${routeId}_P.geojson`.
 * Alineado con las propiedades usadas en RutaPayWeb (`nombre`, `colonia`, `sentido`).
 */
data class RouteStop(
    val id: String,
    val name: String,
    val colonia: String,
    val sentido: String,
    val point: Point
)

/**
 * Formatea nombres crudos tipo "PO_001_Cruz Roja_I" -> "Cruz Roja", igual que en RutaPayWeb.
 */
internal fun formatStopName(raw: String): String =
    raw.replace(Regex("^[A-Za-z]{2,4}_"), "")
        .replace(Regex("^\\d+_"), "")
        .replace(Regex("_[IR]$", RegexOption.IGNORE_CASE), "")
        .replace("_", " ")
        .trim()
        .ifBlank { "Parada" }

/**
 * Formatea el campo `sentido` igual que en RutaPayWeb ("Ida y regreso", "Ida", "Regreso").
 */
internal fun formatSentido(sentido: String?): String {
    if (sentido.isNullOrBlank()) return ""
    val values = sentido.split(",").map { it.trim().uppercase() }
    val hasIda = "IDA" in values
    val hasRegreso = "REGRESO" in values
    return when {
        hasIda && hasRegreso -> "Ida y regreso"
        hasIda -> "Ida"
        hasRegreso -> "Regreso"
        else -> sentido.trim()
    }
}

/**
 * Caché en memoria por ruta para las paradas (`null` = la ruta aún no tiene archivo `_P.geojson` publicado).
 */
private object RouteStopsRepository {
    private val stopsCache = mutableMapOf<String, List<RouteStop>?>()

    suspend fun fetchStops(routeId: String): List<RouteStop>? {
        if (stopsCache.containsKey(routeId)) {
            return stopsCache[routeId]
        }

        return withContext(Dispatchers.IO) {
            try {
                val connection = (URL("$ROUTES_BASE_URL/${routeId}_P.geojson").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                }
                val code = connection.responseCode
                if (code !in 200..299) {
                    connection.disconnect()
                    stopsCache[routeId] = null
                    return@withContext null
                }

                val json = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()

                val features = FeatureCollection.fromJson(json).features() ?: emptyList()
                val stops = features.mapIndexedNotNull { index, feature ->
                    val point = feature.geometry() as? Point ?: return@mapIndexedNotNull null
                    val rawName = when {
                        feature.hasProperty("nombre") && !feature.getProperty("nombre").isJsonNull ->
                            feature.getStringProperty("nombre")
                        feature.hasProperty("name") && !feature.getProperty("name").isJsonNull ->
                            feature.getStringProperty("name")
                        else -> "Parada"
                    }
                    val colonia = if (feature.hasProperty("colonia") && !feature.getProperty("colonia").isJsonNull) {
                        feature.getStringProperty("colonia").trim()
                    } else {
                        ""
                    }
                    val rawSentido = if (feature.hasProperty("sentido") && !feature.getProperty("sentido").isJsonNull) {
                        feature.getStringProperty("sentido")
                    } else {
                        null
                    }

                    RouteStop(
                        id = "${routeId}_stop_$index",
                        name = formatStopName(rawName),
                        colonia = colonia,
                        sentido = formatSentido(rawSentido),
                        point = point
                    )
                }

                stopsCache[routeId] = stops
                stops
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
        }
    }
}

/**
 * Repositorio DEMO con paradas representativas de Celaya y sus rutas directas.
 */
object DemoStopsRepository {
    val CelayaStops = listOf(
        BusStopDemo("stop_central", "Parada Central de Autobuses", "Av. Antonio Plaza"),
        BusStopDemo("stop_tec", "TecNM Campus 1 (Tecnológico)", "Av. Tecnológico"),
        BusStopDemo("stop_hospital", "Hospital General Celaya", "Diego Rivera"),
        BusStopDemo("stop_villas", "Villas del Bajío", "Av. Las Torres"),
        BusStopDemo("stop_fovissste", "Fovissste / Av. Tecnológico", "Fovissste"),
        BusStopDemo("stop_centro", "Centro Histórico", "Jardín Principal Celaya"),
        BusStopDemo("stop_canitos", "Mercado Cañitos", "Barrio de San Miguel"),
        BusStopDemo("stop_parque", "Plaza Parque Celaya", "Eje Norponiente")
    )

    val RouteStopsMapping = mapOf(
        "R8 - SAN JUAN DE LA VEGA" to listOf("stop_central", "stop_fovissste", "stop_tec"),
        "R1 - SMO - CELAYA" to listOf("stop_centro", "stop_fovissste", "stop_tec"),
        "R9 - HOSPITAL" to listOf("stop_central", "stop_hospital", "stop_centro"),
        "R15 - SILVA X TENOCHTITLAN" to listOf("stop_fovissste", "stop_tec", "stop_villas"),
        "R37 - VILLAS DEL BAJÍO" to listOf("stop_villas", "stop_parque", "stop_central"),
        "R43 - ROMERAL X FOVISSSTE" to listOf("stop_fovissste", "stop_hospital", "stop_centro"),
        "R60 - PINOS" to listOf("stop_parque", "stop_fovissste", "stop_hospital"),
        "R63 - UNIVERSIDADES" to listOf("stop_tec", "stop_hospital", "stop_parque")
    )

    fun findMatchingRoutes(originStopId: String, destinationStopId: String): List<String> {
        return RouteStopsMapping.filter { (_, stops) ->
            val originIndex = stops.indexOf(originStopId)
            val destIndex = stops.indexOf(destinationStopId)
            originIndex != -1 && destIndex != -1 && originIndex < destIndex
        }.keys.toList()
    }
}

// Lista general de nombres de rutas de Celaya
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
 * Vista Mapa a pantalla completa con selector de rutas superior, tarjeta demo y paradas oficiales interactivas.
 */
@Composable
fun MapSection(
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color
) {
    val coroutineScope = rememberCoroutineScope()
    var search by remember { mutableStateOf("") }
    var selectedRoute by remember { mutableStateOf<String?>(null) }
    var loadingRoute by remember { mutableStateOf(false) }
    var routeError by remember { mutableStateOf<String?>(null) }
    var reloadTick by remember { mutableIntStateOf(0) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Estados para el sub-menú de Origen y Destino
    var selectedOriginStop by remember { mutableStateOf<BusStopDemo?>(null) }
    var selectedDestinationStop by remember { mutableStateOf<BusStopDemo?>(null) }
    var showOriginPicker by remember { mutableStateOf(false) }
    var showDestinationPicker by remember { mutableStateOf(false) }

    // Estados para las paradas oficiales de la ruta (alineado con RutaPayWeb)
    var stopsVisible by remember { mutableStateOf(false) }
    var stopsLoading by remember { mutableStateOf(false) }
    var stopsList by remember { mutableStateOf<List<RouteStop>>(emptyList()) }
    var focusedStop by remember { mutableStateOf<RouteStop?>(null) }
    var stopsInfoMessage by remember { mutableStateOf<String?>(null) }
    var stopsLoadToken by remember { mutableIntStateOf(0) }

    val currentRouteId = remember(selectedRoute) {
        selectedRoute?.substringBefore(" - ")?.trim().orEmpty()
    }

    LaunchedEffect(stopsInfoMessage) {
        if (stopsInfoMessage != null) {
            delay(3200)
            stopsInfoMessage = null
        }
    }

    fun loadStopsForCurrentRoute(notifyIfMissing: Boolean) {
        val routeId = currentRouteId
        if (routeId.isBlank()) {
            if (notifyIfMissing) {
                stopsInfoMessage = "Selecciona una ruta para ver sus paradas"
            }
            return
        }
        val token = ++stopsLoadToken
        stopsLoading = true
        coroutineScope.launch {
            try {
                val data = RouteStopsRepository.fetchStops(routeId)
                if (token != stopsLoadToken) return@launch
                if (data.isNullOrEmpty()) {
                    stopsList = emptyList()
                    stopsVisible = false
                    focusedStop = null
                    if (notifyIfMissing) {
                        stopsInfoMessage = "La ruta $routeId todavía no tiene paradas publicadas"
                    }
                } else {
                    stopsList = data
                }
            } finally {
                if (token == stopsLoadToken) {
                    stopsLoading = false
                }
            }
        }
    }

    fun toggleStops() {
        if (stopsLoading) return
        if (stopsVisible) {
            stopsVisible = false
            focusedStop = null
            return
        }
        if (currentRouteId.isBlank()) {
            stopsInfoMessage = "Selecciona una ruta para ver sus paradas"
            return
        }
        stopsVisible = true
        loadStopsForCurrentRoute(notifyIfMissing = true)
    }

    // Al cambiar de ruta, limpia la parada enfocada y recarga paradas si estaban visibles
    LaunchedEffect(selectedRoute) {
        focusedStop = null
        stopsList = emptyList()
        if (selectedRoute == null) {
            stopsVisible = false
            stopsLoading = false
        } else if (stopsVisible) {
            loadStopsForCurrentRoute(notifyIfMissing = false)
        }
    }

    // Cálculo de rutas recomendadas en base a las paradas seleccionadas
    val matchingRoutes = remember(selectedOriginStop, selectedDestinationStop) {
        if (selectedOriginStop != null && selectedDestinationStop != null) {
            DemoStopsRepository.findMatchingRoutes(selectedOriginStop!!.id, selectedDestinationStop!!.id)
        } else {
            emptyList()
        }
    }

    val filtered = remember(search) {
        if (search.isBlank()) CelayaRoutes
        else CelayaRoutes.filter { it.contains(search, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ---- Mapa a pantalla completa ----
        FullScreenMap(
            selectedRoute = selectedRoute,
            stops = if (stopsVisible) stopsList else emptyList(),
            focusedStop = if (stopsVisible) focusedStop else null,
            reloadTick = reloadTick,
            onStopClick = { stop ->
                focusedStop = stop
            },
            onLoadingChange = { loadingRoute = it },
            onError = { routeError = it },
            modifier = Modifier.fillMaxSize()
        )

        // ---- Contenedor Superior (Barra de búsqueda, menú desplegable y Tarjeta Origen/Destino) ----
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp)
        ) {
            // ---- 1. Buscador y menú desplegable superior ----
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = WebMutedText,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = search,
                            onValueChange = {
                                search = it
                                if (it.isNotBlank()) {
                                    dropdownExpanded = true
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp),
                            textStyle = TextStyle(
                                fontFamily = Lexend,
                                fontSize = 14.sp,
                                color = WebDark
                            ),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (search.isEmpty()) {
                                        Text(
                                            text = "Busca una ruta...",
                                            style = TextStyle(
                                                fontFamily = Lexend,
                                                color = Color(0xFFC3C3C3),
                                                fontSize = 14.sp
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        if (search.isNotBlank()) {
                            IconButton(
                                onClick = { search = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar",
                                    tint = WebMutedText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        // Icono del camión + Número total de rutas + Semi-flecha desplegable
                        Row(
                            modifier = Modifier
                                .clickable { dropdownExpanded = !dropdownExpanded }
                                .padding(start = 4.dp, end = 2.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsBus,
                                contentDescription = null,
                                tint = WebPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = WebPrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "${filtered.size}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = TextStyle(
                                        fontFamily = Lexend,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WebPrimary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (dropdownExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (dropdownExpanded) "Cerrar menú" else "Abrir menú",
                                tint = WebMutedText,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Lista desplegable de rutas (de arriba a abajo)
                    AnimatedVisibility(
                        visible = dropdownExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(WebCardBorder.copy(alpha = 0.5f))
                            )
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 260.dp)
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                items(filtered) { route ->
                                    val isSelected = route == selectedRoute
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedRoute = route
                                                dropdownExpanded = false
                                            }
                                            .background(
                                                if (isSelected) WebPrimary.copy(alpha = 0.08f) else Color.Transparent,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                                    fontFamily = Lexend,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
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

            Spacer(modifier = Modifier.height(10.dp))

            /* ---- 2. Tarjeta Demo de Selección de Origen y Destino ---- */
            OriginDestinationSelectorCard(
                originStop = selectedOriginStop,
                destinationStop = selectedDestinationStop,
                matchingRoutes = matchingRoutes,
                onSelectOriginClick = { showOriginPicker = true },
                onSelectDestinationClick = { showDestinationPicker = true },
                onRouteSelect = { routeName ->
                    selectedRoute = routeName
                },
                onClear = {
                    selectedOriginStop = null
                    selectedDestinationStop = null
                }
            )
        }

        /* ---- Panel Flotante de Ruta Seleccionada, Botón de Paradas y Popup de Parada ---- */
        AnimatedVisibility(
            visible = selectedRoute != null && !dropdownExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = BottomBarSpace + 16.dp, start = 12.dp, end = 12.dp)
        ) {
            selectedRoute?.let { route ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Mensaje informativo de paradas (cuando no hay paradas publicadas)
                    AnimatedVisibility(visible = stopsInfoMessage != null) {
                        stopsInfoMessage?.let { infoMsg ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = WebDark),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBus,
                                        contentDescription = null,
                                        tint = WebPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = infoMsg,
                                        style = TextStyle(
                                            fontFamily = Lexend,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { stopsInfoMessage = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Cerrar",
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Popup de detalle de parada seleccionada (alineado con buildStopPopupHtml de RutaPayWeb)
                    AnimatedVisibility(visible = stopsVisible && focusedStop != null) {
                        focusedStop?.let { stop ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WebCardBorder),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 3.dp)
                                            .size(12.dp)
                                            .background(WebPrimary, CircleShape)
                                            .border(2.dp, Color.White, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stop.name,
                                            style = TextStyle(
                                                fontFamily = Lexend,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = WebDark,
                                                lineHeight = 17.sp
                                            )
                                        )
                                        if (stop.colonia.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = stop.colonia,
                                                style = TextStyle(
                                                    fontFamily = Lexend,
                                                    fontSize = 11.sp,
                                                    color = WebTextLight
                                                )
                                            )
                                        }
                                        if (stop.sentido.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Sentido: ${stop.sentido}",
                                                style = TextStyle(
                                                    fontFamily = Lexend,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = WebPrimary
                                                )
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { focusedStop = null },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Cerrar parada",
                                            tint = WebMutedText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Tarjeta principal de ruta seleccionada
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = WebPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = route,
                                        style = TextStyle(
                                            fontFamily = Lexend,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WebDark
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RouteLegendDot(color = WebRouteIda, label = "IDA")
                                        Spacer(modifier = Modifier.width(10.dp))
                                        RouteLegendDot(color = WebRouteRegreso, label = "REGRESO")
                                        if (stopsVisible && stopsList.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "· ${stopsList.size} paradas",
                                                style = TextStyle(
                                                    fontFamily = Lexend,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = WebTextLight
                                                )
                                            )
                                        }
                                        if (loadingRoute) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                strokeWidth = 2.dp,
                                                color = WebPrimary
                                            )
                                        }
                                    }
                                }
                                IconButton(onClick = {
                                    selectedRoute = null
                                    focusedStop = null
                                    stopsVisible = false
                                    stopsList = emptyList()
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar ruta", tint = WebMutedText)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Botón Toggle de Paradas (alineado con RutaPayWeb: Mostrar/Ocultar paradas + conteo)
                            Surface(
                                onClick = { toggleStops() },
                                enabled = !stopsLoading,
                                shape = RoundedCornerShape(50),
                                color = if (stopsVisible) WebPrimary else Color(0xFFF3F4F6),
                                border = if (stopsVisible) null else androidx.compose.foundation.BorderStroke(1.dp, WebCardBorder),
                                shadowElevation = if (stopsVisible) 4.dp else 0.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (stopsLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = if (stopsVisible) Color.White else WebPrimary
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Place,
                                            contentDescription = null,
                                            tint = if (stopsVisible) Color.White else WebDark,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when {
                                            stopsLoading -> "Cargando paradas…"
                                            stopsVisible -> "Ocultar paradas"
                                            else -> "Mostrar paradas"
                                        },
                                        style = TextStyle(
                                            fontFamily = Lexend,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (stopsVisible) Color.White else WebDark
                                        )
                                    )
                                    if (stopsVisible && stopsList.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = Color.White.copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = "${stopsList.size}",
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                                style = TextStyle(
                                                    fontFamily = Lexend,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            /* Lista Horizontal interactiva de paradas reales de la ruta */
                            if (stopsVisible && stopsList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(WebCardBorder.copy(alpha = 0.5f))
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(stopsList, key = { it.id }) { stop ->
                                        val isFocused = focusedStop?.id == stop.id
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isFocused) WebPrimary else Color(0xFFF1F5F9),
                                            modifier = Modifier
                                                .clickable { focusedStop = stop }
                                                .border(
                                                    1.dp,
                                                    if (isFocused) WebPrimary else WebCardBorder,
                                                    RoundedCornerShape(12.dp)
                                                )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .background(
                                                            if (isFocused) Color.White else WebPrimary,
                                                            CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DirectionsBus,
                                                        contentDescription = null,
                                                        tint = if (isFocused) WebPrimary else Color.White,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = stop.name,
                                                        style = TextStyle(
                                                            fontFamily = Lexend,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = if (isFocused) Color.White else WebDark
                                                        )
                                                    )
                                                    val subtitle = listOfNotNull(
                                                        stop.colonia.takeIf { it.isNotBlank() },
                                                        stop.sentido.takeIf { it.isNotBlank() }?.let { "Sentido: $it" }
                                                    ).joinToString(" · ")
                                                    if (subtitle.isNotBlank()) {
                                                        Text(
                                                            text = subtitle,
                                                            style = TextStyle(
                                                                fontFamily = Lexend,
                                                                fontSize = 9.sp,
                                                                color = if (isFocused) Color.White.copy(alpha = 0.85f) else WebTextLight
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ---- Error de carga ----
        AnimatedVisibility(
            visible = routeError != null,
            enter = expandVertically(),
            exit = shrinkVertically(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    bottom = BottomBarSpace + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                )
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
    }

    /* ---- Diálogos de Selección de Paradas (Demo) ---- */
    if (showOriginPicker) {
        StopSelectionDialog(
            title = "Selecciona Punto de Partida",
            stops = DemoStopsRepository.CelayaStops,
            onStopSelected = { stop ->
                selectedOriginStop = stop
                showOriginPicker = false
            },
            onDismiss = { showOriginPicker = false }
        )
    }

    if (showDestinationPicker) {
        StopSelectionDialog(
            title = "Selecciona Destino",
            stops = DemoStopsRepository.CelayaStops,
            onStopSelected = { stop ->
                selectedDestinationStop = stop
                showDestinationPicker = false
            },
            onDismiss = { showDestinationPicker = false }
        )
    }
}

/**
 * Tarjeta para la selección de "Punto de partida" y "Destino" (Demo).
 */
@Composable
private fun OriginDestinationSelectorCard(
    originStop: BusStopDemo?,
    destinationStop: BusStopDemo?,
    matchingRoutes: List<String>,
    onSelectOriginClick: () -> Unit,
    onSelectDestinationClick: () -> Unit,
    onRouteSelect: (String) -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF3F8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Indicador Gráfico Izquierdo (Punto -> Línea -> Cuadrado)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 6.dp, end = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(WebDark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color.White, CircleShape)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(28.dp)
                            .background(WebDark.copy(alpha = 0.8f))
                    )

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(WebDark, RoundedCornerShape(3.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color.White, RoundedCornerShape(1.dp))
                        )
                    }
                }

                /* Campos de Origen y Destino */
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectOriginClick() }
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = originStop?.name ?: "Punto de partida",
                            style = TextStyle(
                                fontFamily = Lexend,
                                fontSize = 15.sp,
                                fontWeight = if (originStop != null) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (originStop != null) WebDark else Color(0xFF0F263D)
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFCBD5E1))
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectDestinationClick() }
                            .padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = destinationStop?.name ?: "Destino",
                            style = TextStyle(
                                fontFamily = Lexend,
                                fontSize = 15.sp,
                                fontWeight = if (destinationStop != null) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (destinationStop != null) WebDark else Color(0xFF0F263D)
                            )
                        )
                    }
                }

                if (originStop != null || destinationStop != null) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpiar selección",
                            tint = WebMutedText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (originStop != null && destinationStop != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFCBD5E1))
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (matchingRoutes.isNotEmpty()) {
                    Text(
                        text = "Rutas sugeridas que conectan ambas paradas:",
                        style = TextStyle(
                            fontFamily = Lexend,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WebPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        matchingRoutes.forEach { routeName ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, WebPrimary.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onRouteSelect(routeName) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBus,
                                        contentDescription = null,
                                        tint = WebPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = routeName,
                                        style = TextStyle(
                                            fontFamily = Lexend,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WebDark
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "Ver en mapa",
                                        style = TextStyle(
                                            fontFamily = Lexend,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WebPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No se encontraron rutas directas entre estas paradas en la demo.",
                        style = TextStyle(
                            fontFamily = Lexend,
                            fontSize = 11.sp,
                            color = Color(0xFFD93025)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Diálogo interactivo DEMO para la selección de paradas.
 */
@Composable
private fun StopSelectionDialog(
    title: String,
    stops: List<BusStopDemo>,
    onStopSelected: (BusStopDemo) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = Lexend,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = WebDark
                )
            )
        },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                items(stops) { stop ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStopSelected(stop) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = WebPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stop.name,
                                style = TextStyle(
                                    fontFamily = Lexend,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WebDark
                                )
                            )
                            if (stop.locationDescription.isNotBlank()) {
                                Text(
                                    text = stop.locationDescription,
                                    style = TextStyle(
                                        fontFamily = Lexend,
                                        fontSize = 11.sp,
                                        color = WebMutedText
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    style = TextStyle(fontFamily = Lexend, fontSize = 13.sp, color = WebMutedText)
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun FullScreenMap(
    selectedRoute: String?,
    stops: List<RouteStop>,
    focusedStop: RouteStop?,
    reloadTick: Int,
    onStopClick: (RouteStop) -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current.density

    var idaFeatures by remember { mutableStateOf<List<Feature>>(emptyList()) }
    var regresoFeatures by remember { mutableStateOf<List<Feature>>(emptyList()) }
    var mapViewRef by remember { mutableStateOf<com.mapbox.maps.MapView?>(null) }

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
                URL("$ROUTES_BASE_URL/$routeId.geojson").readText()
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
    val stopsSourceState = remember { GeoJsonSourceState("route-stops") }
    val focusedStopSourceState = remember { GeoJsonSourceState("route-stops-focused") }

    LaunchedEffect(idaFeatures) {
        idaSourceState.data = GeoJSONData(idaFeatures)
    }
    LaunchedEffect(regresoFeatures) {
        regresoSourceState.data = GeoJSONData(regresoFeatures)
    }
    LaunchedEffect(stops) {
        val stopFeatures = stops.map { stop ->
            Feature.fromGeometry(stop.point).apply {
                addStringProperty("id", stop.id)
                addStringProperty("name", stop.name)
            }
        }
        stopsSourceState.data = GeoJSONData(stopFeatures)
    }
    LaunchedEffect(focusedStop) {
        val focusedFeatures = focusedStop?.let { listOf(Feature.fromGeometry(it.point)) } ?: emptyList()
        focusedStopSourceState.data = GeoJSONData(focusedFeatures)
    }

    val mapState = rememberMapState {
        coroutineScope.launch {
            mapLoadingErrorEvents.onEach {
                // Error occurred when loading the map
            }
        }
        coroutineScope.launch {
            styleLoadedEvents.first().let {
                // Style loaded
            }
        }
    }

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(12.0)
            center(Point.fromLngLat(-100.8140458, 20.521788))
            pitch(0.0)
            bearing(0.0)
        }
    }

    MapboxMap(
        modifier.fillMaxSize(),
        mapState = mapState,
        mapViewportState = mapViewportState,
        onMapClickListener = { clickPoint ->
            val mv = mapViewRef
            if (mv != null && stops.isNotEmpty()) {
                val clickPixel = mv.mapboxMap.pixelForCoordinate(clickPoint)
                val maxTapPx = 28.0 * density
                val hit = stops.minByOrNull { stop ->
                    val stopPixel = mv.mapboxMap.pixelForCoordinate(stop.point)
                    hypot(clickPixel.x - stopPixel.x, clickPixel.y - stopPixel.y)
                }
                if (hit != null) {
                    val stopPixel = mv.mapboxMap.pixelForCoordinate(hit.point)
                    val dist = hypot(clickPixel.x - stopPixel.x, clickPixel.y - stopPixel.y)
                    if (dist <= maxTapPx) {
                        onStopClick(hit)
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            } else {
                false
            }
        }
    ) {
        MapEffect(Unit) { mapView ->
            mapViewRef = mapView
        }

        if (idaFeatures.isNotEmpty()) {
            LineLayer(sourceState = idaSourceState, layerId = "route-ida") {
                lineCap = LineCapValue.ROUND
                lineJoin = LineJoinValue.ROUND
                lineColor = ColorValue(WebRouteIda)
                lineWidth = DoubleValue(4.0)
            }
        }
        if (regresoFeatures.isNotEmpty()) {
            LineLayer(sourceState = regresoSourceState, layerId = "route-regreso") {
                lineCap = LineCapValue.ROUND
                lineJoin = LineJoinValue.ROUND
                lineColor = ColorValue(WebRouteRegreso)
                lineWidth = DoubleValue(4.0)
                lineDasharray = DoubleListValue(listOf(2.0, 2.0))
            }
        }

        /* Capas para dibujar los puntos de las Paradas en el mapa (#449dd1 con borde blanco, igual que RutaPayWeb) */
        if (stops.isNotEmpty()) {
            CircleLayer(sourceState = stopsSourceState, layerId = "route-stops-layer") {
                circleColor = ColorValue(WebPrimary)
                circleRadius = DoubleValue(6.5)
                circleStrokeColor = ColorValue(Color.White)
                circleStrokeWidth = DoubleValue(2.0)
                circleOpacity = DoubleValue(0.95)
            }
        }

        if (focusedStop != null) {
            CircleLayer(sourceState = focusedStopSourceState, layerId = "route-stops-focused-layer") {
                circleColor = ColorValue(WebDark)
                circleRadius = DoubleValue(9.5)
                circleStrokeColor = ColorValue(Color.White)
                circleStrokeWidth = DoubleValue(2.5)
            }
        }

        /* Efecto de Soft-Zoom animado de cámara al presionar una parada */
        MapEffect(focusedStop) { mapView ->
            focusedStop?.let { stop ->
                mapView.camera.easeTo(
                    com.mapbox.maps.CameraOptions.Builder()
                        .center(stop.point)
                        .zoom(15.8)
                        .build(),
                    MapAnimationOptions.mapAnimationOptions { duration(900L) }
                )
            }
        }

        /* Ajuste automático de cámara para encuadrar la ruta */
        MapEffect(idaFeatures, regresoFeatures) { mapView ->
            if (focusedStop != null) return@MapEffect

            val routePoints = (idaFeatures + regresoFeatures).flatMap { feature ->
                when (val geometry = feature.geometry()) {
                    is LineString -> geometry.coordinates()
                    is MultiLineString -> geometry.coordinates().flatten()
                    else -> emptyList()
                }
            }

            if (routePoints.size >= 2) {
                val camera = mapView.mapboxMap.cameraForCoordinates(
                    routePoints,
                    EdgeInsets(
                        (140 * density).toDouble(),
                        (20 * density).toDouble(),
                        ((BottomBarSpace.value + 180) * density).toDouble(),
                        (20 * density).toDouble()
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
