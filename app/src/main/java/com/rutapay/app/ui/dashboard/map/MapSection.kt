package com.rutapay.app.ui.dashboard.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

/**
 * Modelo de datos para las paradas oficiales de una ruta (con marcadores naranja en el mapa).
 */
data class OfficialRouteStop(
    val id: String,
    val name: String,
    val sequenceNumber: Int,
    val point: Point,
    val description: String = ""
)

/**
 * Coordenadas de las paradas con puntito naranja para la Ruta 60 (R60 - PINOS).
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
    "R63 - UNIVERSIDADES"
)

/**
 * Vista Mapa a pantalla completa con selector de rutas superior unificado.
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

    // Verifica si la ruta seleccionada es la Ruta 60 (R60 - PINOS) para la demo de paradas oficiales
    val isR60Selected = remember(selectedRoute) {
        selectedRoute?.contains("R60") == true
    }
    val officialStops = remember(isR60Selected) {
        if (isR60Selected) R60OfficialStops else emptyList()
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
            reloadTick = reloadTick,
            onLoadingChange = { loadingRoute = it },
            onError = { routeError = it },
            modifier = Modifier.fillMaxSize()
        )

        // ---- Barra de búsqueda y menú desplegable superior unificado ----
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp),
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
                                .heightIn(max = 380.dp)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            items(filtered) { route ->
                                val isSelected = route == selectedRoute
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedRoute = route
                                            dropdownExpanded = false // Cierra la lista en cuanto se selecciona la ruta
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

        // ---- Ruta seleccionada (flotante bajo el buscador cuando el desplegable está cerrado) ----
        AnimatedVisibility(
            visible = selectedRoute != null && !dropdownExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 80.dp, start = 12.dp, end = 12.dp)
        ) {
            selectedRoute?.let { route ->
                Card(
                    shape = RoundedCornerShape(14.dp),
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
}

@Composable
private fun FullScreenMap(
    selectedRoute: String?,
    officialStops: List<OfficialRouteStop>,
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

        // Capas para dibujar los puntos de las Paradas Oficiales en el mapa (Marcadores Naranjas con borde Blanco)
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

        // Ajuste automático de cámara para encuadrar la ruta y todas las paradas oficiales
        MapEffect(idaFeatures, regresoFeatures, officialStops) { mapView ->
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
                        ((BottomBarSpace.value + 40) * density).toDouble(),
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
