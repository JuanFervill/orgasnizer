package com.example.orgasnizer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.itextpdf.text.Document;
import com.pdfview.PDFView;

import java.io.File;
import java.io.FileNotFoundException;

public class PdfVista extends AppCompatActivity {

    PDFView vistaPDF;
    String archivo;
    String namedc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_vista);
        namedc = getIntent().getStringExtra("namedc");
        archivo = getIntent().getStringExtra("archivo");
        vistaPDF = findViewById(R.id.viewpdfid);
            File file = new File(archivo, namedc);
            if (!file.exists()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Informe no encontrado"); // Título del diálogo
                builder.setMessage("No se ha podido encontrar el informe, por favor vuelva a generarlo"); // Mensaje del diálogo
                builder.show();
            }else {
                vistaPDF.fromFile(file);
                vistaPDF.setZoomEnabled(true);
                vistaPDF.show();
            }
    }
}