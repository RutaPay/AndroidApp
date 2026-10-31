package com.rutapay.app.ui.dashboard.qr

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.rutapay.app.R

/**
 * Pantalla de escaneo QR con el diseño de RutaPay.
 *
 * Extiende [CaptureActivity] para reutilizar la cámara y el decodificador de
 * ZXing, pero reemplaza el layout por uno propio: vista previa a pantalla
 * completa, encabezado con botón de cierre, marco con esquinas celestes y una
 * hoja inferior con instrucciones y linterna. La orientación se fija en
 * vertical desde el AndroidManifest.
 */
class QrScannerActivity : CaptureActivity() {

    private lateinit var barcodeView: DecoratedBarcodeView
    private var torchOn = false

    override fun initializeContent(): DecoratedBarcodeView {
        setContentView(R.layout.activity_qr_scanner)
        barcodeView = findViewById(R.id.zxing_barcode_scanner)
        return barcodeView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<ImageView>(R.id.qr_close_button).setOnClickListener { finish() }

        val torchButton = findViewById<LinearLayout>(R.id.qr_torch_button)
        val torchIcon = findViewById<ImageView>(R.id.qr_torch_icon)
        val torchLabel = findViewById<TextView>(R.id.qr_torch_label)

        torchButton.setOnClickListener {
            torchOn = !torchOn
            runCatching {
                if (torchOn) barcodeView.setTorchOn() else barcodeView.setTorchOff()
            }
            applyTorchState(torchButton, torchIcon, torchLabel)
        }
    }

    private fun applyTorchState(
        button: LinearLayout,
        icon: ImageView,
        label: TextView
    ) {
        button.setBackgroundResource(
            if (torchOn) R.drawable.bg_qr_torch_on else R.drawable.bg_qr_torch_off
        )

        val textColor = ContextCompat.getColor(
            this,
            if (torchOn) R.color.white else R.color.rp_navy
        )
        icon.imageTintList = ColorStateList.valueOf(textColor)
        label.setTextColor(textColor)
        label.text = if (torchOn) "Linterna encendida" else "Encender linterna"
    }
}
