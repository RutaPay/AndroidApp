package com.rutapay.app.ui.dashboard.qr

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import com.journeyapps.barcodescanner.ViewfinderView

/**
 * Marco de escaneo con el estilo de RutaPay: el enmascarado oscuro y la línea
 * láser vienen de [ViewfinderView]; encima dibujamos las cuatro esquinas
 * redondeadas en el celeste de la marca para que el área de lectura se vea
 * claramente delimitada.
 */
class RutaQrViewfinderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewfinderView(context, attrs) {

    private val density = context.resources.displayMetrics.density

    private val cornerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#78C0E0")
        style = Paint.Style.STROKE
        strokeWidth = 5f * density
        strokeCap = Paint.Cap.ROUND
    }

    private val cornerLength = 30f * density

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val frame = framingRect ?: return
        val left = frame.left.toFloat()
        val top = frame.top.toFloat()
        val right = frame.right.toFloat()
        val bottom = frame.bottom.toFloat()
        val length = cornerLength

        // Superior izquierda
        canvas.drawLine(left, top + length, left, top, cornerPaint)
        canvas.drawLine(left, top, left + length, top, cornerPaint)

        // Superior derecha
        canvas.drawLine(right - length, top, right, top, cornerPaint)
        canvas.drawLine(right, top, right, top + length, cornerPaint)

        // Inferior izquierda
        canvas.drawLine(left, bottom - length, left, bottom, cornerPaint)
        canvas.drawLine(left, bottom, left + length, bottom, cornerPaint)

        // Inferior derecha
        canvas.drawLine(right - length, bottom, right, bottom, cornerPaint)
        canvas.drawLine(right, bottom, right, bottom - length, cornerPaint)
    }
}
