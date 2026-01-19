package com.ai.roboteacher

sealed class PdfBlock {
    data class Text(val text: String) : PdfBlock()
    data class Equation(val latex: String) : PdfBlock()
    data class Table(val rows: List<List<String>>) : PdfBlock()
}