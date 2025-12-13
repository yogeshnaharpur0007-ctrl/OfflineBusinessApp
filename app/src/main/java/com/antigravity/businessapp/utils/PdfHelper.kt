package com.antigravity.businessapp.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.antigravity.businessapp.data.Transaction
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfHelper {

    fun generateLedgerPdf(context: Context, partyName: String, transactions: List<Transaction>) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Title
        paint.color = Color.BLACK
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Ledger: $partyName", 50f, 50f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        canvas.drawText("Generated: ${dateFormat.format(Date())}", 50f, 70f, paint)

        // Table Header
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        var yPos = 100f
        
        // Headers
        canvas.drawRect(40f, yPos, 555f, yPos + 20, paint)
        paint.style = Paint.Style.FILL
        canvas.drawText("Date", 50f, yPos + 15, paint)
        canvas.drawText("Type", 180f, yPos + 15, paint)
        canvas.drawText("Total", 300f, yPos + 15, paint)
        canvas.drawText("Paid", 400f, yPos + 15, paint)
        
        yPos += 20f

        // Rows
        for (tx in transactions) {
            paint.style = Paint.Style.STROKE
            canvas.drawRect(40f, yPos, 555f, yPos + 20, paint)
            
            paint.style = Paint.Style.FILL
            val dateStr = dateFormat.format(Date(tx.timestamp)).substring(0, 10)
            canvas.drawText(dateStr, 50f, yPos + 15, paint)
            canvas.drawText(tx.type, 180f, yPos + 15, paint)
            canvas.drawText(tx.totalAmount.toString(), 300f, yPos + 15, paint)
            canvas.drawText(tx.paidAmount.toString(), 400f, yPos + 15, paint)
            
            yPos += 20f
            
            if (yPos > 800) {
                // Determine pagination later, for now truncate/single page or just stop
                break 
            }
        }

        pdfDocument.finishPage(page)

        // Save
        val fileName = "Ledger_${partyName.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val file = File(path, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "PDF Saved to Documents/$fileName", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        pdfDocument.close()
    }

    fun generateSalesReport(context: Context, transactions: List<Transaction>) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        paint.color = Color.BLACK
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Sales Report", 50f, 50f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        canvas.drawText("Generated: ${dateFormat.format(Date())}", 50f, 70f, paint)

        var yPos = 100f
        var totalSales = 0.0

        paint.style = Paint.Style.STROKE
        canvas.drawRect(40f, yPos, 555f, yPos + 20, paint)
        paint.style = Paint.Style.FILL
        canvas.drawText("Date", 50f, yPos + 15, paint)
        canvas.drawText("Type", 200f, yPos + 15, paint)
        canvas.drawText("Amount", 400f, yPos + 15, paint)
        yPos += 20f

        for (tx in transactions) {
            if (tx.type == "SALE") {
                paint.style = Paint.Style.STROKE
                canvas.drawRect(40f, yPos, 555f, yPos + 20, paint)
                paint.style = Paint.Style.FILL
                
                val dateStr = dateFormat.format(Date(tx.timestamp)).substring(0, 10)
                canvas.drawText(dateStr, 50f, yPos + 15, paint)
                canvas.drawText(tx.type, 200f, yPos + 15, paint)
                canvas.drawText(tx.totalAmount.toString(), 400f, yPos + 15, paint)
                
                totalSales += tx.totalAmount
                yPos += 20f
            }
            if (yPos > 800) break
        }
        
        yPos += 30f
        paint.isFakeBoldText = true
        canvas.drawText("Total Sales: ₹ $totalSales", 50f, yPos, paint)

        pdfDocument.finishPage(page)
        savePdf(context, pdfDocument, "Sales_Report")
    }

    fun generateStockReport(context: Context, items: List<com.antigravity.businessapp.data.Item>) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        paint.color = Color.BLACK
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("Stock Summary Report", 50f, 50f, paint)
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        canvas.drawText("Generated: ${dateFormat.format(Date())}", 50f, 70f, paint)

        var yPos = 100f

        paint.style = Paint.Style.STROKE
        canvas.drawRect(40f, yPos, 555f, yPos + 20, paint)
        paint.style = Paint.Style.FILL
        canvas.drawText("Item Name", 50f, yPos + 15, paint)
        canvas.drawText("Current Stock", 300f, yPos + 15, paint)
        canvas.drawText("Price", 450f, yPos + 15, paint)
        yPos += 20f

        for (item in items) {
             paint.style = Paint.Style.STROKE
             canvas.drawRect(40f, yPos, 555f, yPos + 20, paint)
             paint.style = Paint.Style.FILL
             
             canvas.drawText(item.name, 50f, yPos + 15, paint)
             canvas.drawText(item.stockQuantity.toString() + " " + item.unit, 300f, yPos + 15, paint)
             canvas.drawText(item.sellingRate.toString(), 450f, yPos + 15, paint)
             
             yPos += 20f
             if (yPos > 800) break
        }

        pdfDocument.finishPage(page)
        savePdf(context, pdfDocument, "Stock_Report")
    }

    private fun savePdf(context: Context, document: PdfDocument, namePrefix: String) {
        val fileName = "${namePrefix}_${System.currentTimeMillis()}.pdf"
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val file = File(path, fileName)

        try {
            document.writeTo(FileOutputStream(file))
            Toast.makeText(context, "Saved: Documents/$fileName", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            document.close()
        }
    }
