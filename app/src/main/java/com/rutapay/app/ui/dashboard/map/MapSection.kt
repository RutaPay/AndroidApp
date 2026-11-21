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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

// ============================================================================
// ESTRUCTURA DE PARADAS Y MODELOS DE DATOS PARA LA RUTA 60
// ============================================================================

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
 * Modelo de datos para las paradas oficiales registradas en el sistema.
 */
data class OfficialRouteStop(
    val id: String,
    val name: String,
    val sequenceNumber: Int,
    val point: Point,
    val description: String = ""
)

/**
 * Coordenadas oficiales exactas proporcionadas para la **Ruta 60 (PINOS)**.
 */
val R60OfficialStops = listOf(
    OfficialRouteStop("r60_base", "Parada Ruta 60 (Base / Principal)", 1, Point.fromLngLat(-100.7804651, 20.5148246), "Base Principal Ruta 60"),
    OfficialRouteStop("r60_1", "Parada Ruta 60 - Punto 1", 2, Point.fromLngLat(-100.8127641, 20.5195271), "Zona Centro / Sur"),
    OfficialRouteStop("r60_2", "Parada Ruta 60 - Punto 2", 3, Point.fromLngLat(-100.8275872, 20.5195474), "Tramo Sur-Poniente"),
    OfficialRouteStop("r60_3", "Parada Ruta 60 - Punto 3", 4, Point.fromLngLat(-100.8400645, 20.5194977), "Zona Poniente"),
    OfficialRouteStop("r60_4", "Parada Ruta 60 - Punto 4", 5, Point.fromLngLat(-100.8489929, 20.5203608), "Extremo Poniente"),
    OfficialRouteStop("r60_5", "Parada Ruta 60 - Punto 5", 6, Point.fromLngLat(-100.8404791, 20.5483256), "Subiendo hacia el Norte"),
    OfficialRouteStop("r60_6", "Parada Ruta 60 - Punto 6", 7, Point.fromLngLat(-100.8345247, 20.5535950), "Zona Norte / Circuito"),
    OfficialRouteStop("r60_7", "Parada Ruta 60 - Punto 7", 8, Point.fromLngLat(-100.8260151, 20.5527643), "Eje Norte"),
    OfficialRouteStop("r60_8", "Parada Ruta 60 - Punto 8", 9, Point.fromLngLat(-100.8121592, 20.5513734), "Eje Nororiente"),
    OfficialRouteStop("r60_9", "Parada Ruta 60 - Punto 9", 10, Point.fromLngLat(-100.8080580, 20.5509745), "Tramo Oriente"),
    OfficialRouteStop("r60_10", "Parada Ruta 60 - Punto 10", 11, Point.fromLngLat(-100.7810981, 20.5473756), "Bajando hacia el Oriente"),
    OfficialRouteStop("r60_11", "Parada Ruta 60 - Punto 11", 12, Point.fromLngLat(-100.7766205, 20.5308457), "Cierre de circuito")
)

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
    var focusedOfficialStop by remember { mutableStateOf<OfficialRouteStop?>(null) }

    // Verifica si la ruta seleccionada es la Ruta 60 (R60 - PINOS)
    val isR60Selected = remember(selectedRoute) {
        selectedRoute?.contains("R60") == true
    }
    val officialStops = remember(isR60Selected) {
        if (isR60Selected) R60OfficialStops else emptyList()
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
            officialStops = officialStops,
            focusedStop = focusedOfficialStop,
            reloadTick = reloadTick,
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

            // ---- 2. Tarjeta Demo de Selección de Origen y Destino ----
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

        // ---- Panel Flotante de Ruta Seleccionada y Sub-menú de Paradas Oficiales ----
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
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = WebPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = route,
                                    style = TextStyle(fontFamily = Lexend, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WebDark)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
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
                            IconButton(onClick = {
                                selectedRoute = null
                                focusedOfficialStop = null
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Quitar ruta", tint = WebMutedText)
                            }
                        }

                        // SUB-MENÚ INTERACTIVO DE PARADAS OFICIALES (Específico para Ruta 60)
                        if (isR60Selected) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(WebCardBorder.copy(alpha = 0.5f))
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBus,
                                        contentDescription = null,
                                        tint = WebPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Paradas Oficiales (Ruta 60)",
                                        style = TextStyle(
                                            fontFamily = Lexend,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WebDark
                                        )
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = WebPrimary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "${officialStops.size} Paradas",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = TextStyle(
                                            fontFamily = Lexend,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WebPrimary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Lista Horizontal interactiva con Soft-Zoom al presionar
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(officialStops) { stop ->
                                    val isFocused = focusedOfficialStop?.id == stop.id
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isFocused) WebPrimary else Color(0xFFF1F5F9),
                                        modifier = Modifier
                                            .clickable { focusedOfficialStop = stop }
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
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isFocused) Color.White else WebDark
                                                    )
                                                )
                                                Text(
                                                    text = stop.description,
                                                    style = TextStyle(
                                                        fontFamily = Lexend,
                                                        fontSize = 9.sp,
                                                        color = if (isFocused) Color.White.copy(alpha = 0.8f) else WebMutedText
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

    // ---- Diálogos de Selección de Paradas (Demo) ----
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

                // Campos de Origen y Destino
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
    officialStops: List<OfficialRouteStop>,
    focusedStop: OfficialRouteStop?,
    reloadTick: Int,
    onLoadingChange: (Boolean) -> Unit,
    onError: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current.density

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
    val officialStopsSourceState = remember { GeoJsonSourceState("official-stops-source") }

    LaunchedEffect(idaFeatures) {
        idaSourceState.data = GeoJSONData(idaFeatures)
    }
    LaunchedEffect(regresoFeatures) {
        regresoSourceState.data = GeoJSONData(regresoFeatures)
    }
    LaunchedEffect(officialStops) {
        val stopFeatures = officialStops.map { stop ->
            Feature.fromGeometry(stop.point).apply {
                addStringProperty("name", stop.name)
                addNumberProperty("seq", stop.sequenceNumber)
            }
        }
        officialStopsSourceState.data = GeoJSONData(stopFeatures)
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
        mapViewportState = mapViewportState
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

        // Capas para dibujar los puntos de las Paradas Oficiales en el mapa (Naranja con borde Blanco)
        if (officialStops.isNotEmpty()) {
            CircleLayer(sourceState = officialStopsSourceState, layerId = "stops-outer-ring") {
                circleColor = ColorValue(Color.White)
                circleRadius = DoubleValue(9.0)
                circleStrokeColor = ColorValue(Color(0xFFFF6B00))
                circleStrokeWidth = DoubleValue(2.0)
            }
            CircleLayer(sourceState = officialStopsSourceState, layerId = "stops-inner-dot") {
                circleColor = ColorValue(Color(0xFFFF6B00))
                circleRadius = DoubleValue(5.5)
            }
        }

        // Efecto de Soft-Zoom animado de cámara al presionar una parada en el sub-menú
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

        // Ajuste automático de cámara para encuadrar la ruta y todas las paradas oficiales
        MapEffect(idaFeatures, regresoFeatures, officialStops) { mapView ->
            if (focusedStop != null) return@MapEffect

            val routePoints = (idaFeatures + regresoFeatures).flatMap { feature ->
                when (val geometry = feature.geometry()) {
                    is LineString -> geometry.coordinates()
                    is MultiLineString -> geometry.coordinates().flatten()
                    else -> emptyList()
                }
            }
            val stopPoints = officialStops.map { it.point }
            val allPoints = routePoints + stopPoints

            if (allPoints.size >= 2) {
                val camera = mapView.mapboxMap.cameraForCoordinates(
                    allPoints,
                    EdgeInsets(
                        (140 * density).toDouble(),
                        (20 * density).toDouble(),
                        ((BottomBarSpace.value + 160) * density).toDouble(),
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
