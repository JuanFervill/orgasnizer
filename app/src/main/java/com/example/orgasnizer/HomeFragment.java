package com.example.orgasnizer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import static androidx.core.content.ContextCompat.getSystemService;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.PermissionRequest;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Utilities;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class HomeFragment extends Fragment {

    private String tipo_informe, currentuser, busqueda, fecha, fechabusqueda, nombregas;
    String[] rutalmacena, rutabbdd;
    //Edit text de los mensajes a enviar
    Spinner tipoinfospinner;
    Spinner listobj;
    Button creainfo;
    FirebaseFirestore db;
    DocumentReference docRef;
    FirebaseAuth mAuth;
    TextView nameuser;
    EditText namegas;
    private static final int PERMISSION_REQ_CODE = 100;
    private static final String WRITE_EXTERNAL_STORAGE = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        nameuser = view.findViewById(R.id.nombreuser);
        namegas = view.findViewById(R.id.nombrestation);
        ArrayList<String> fechas = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getDisplayName() : "defaultUser";
        nameuser.setText(currentuser);
        db = FirebaseFirestore.getInstance();
        tipoinfospinner = view.findViewById(R.id.comboinfo);
        listobj = view.findViewById(R.id.comboobj);
        creainfo = view.findViewById(R.id.btngeninf);
        listobj.setEnabled(false);
        creainfo.setClickable(false);
        // creamos un canal de notificación para dispositivos con Android Oreo (API nivel 26) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("creainforme", "informenoti", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) requireActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), // o getActivity(), o requireContext()
                R.array.tiposinfor,
                R.layout.spinner_item_selected
        );
        adapter.setDropDownViewResource(R.layout.spinner_item);
        tipoinfospinner.setAdapter(adapter);

        tipoinfospinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipo_informe = parent.getItemAtPosition(position).toString();
                switch (tipo_informe) {
                    case "Diario":
                        busqueda = "informes_diarios";
                        break;
                    case "Semanal":
                        busqueda = "informes_semanales";
                        break;
                    case "Mensual":
                        busqueda = "informes_mensuales";
                        break;
                    default:
                        busqueda = "";
                        break;
                }

                if (!busqueda.isEmpty()) {
                    db.collection(busqueda)
                            .whereEqualTo("usuario", currentuser)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                fechas.clear(); // Limpiar la lista antes de agregar nuevas fechas
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    fecha = document.getString("fecha");
                                    if (fecha != null) {
                                        fechas.add(fecha);
                                    }
                                }
                                listobj.setEnabled(true);
                                ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getContext(), R.layout.spinner_item_selected, fechas);
                                adapter2.setDropDownViewResource(R.layout.spinner_item);
                                listobj.setAdapter(adapter2);
                            })
                            .addOnFailureListener(e -> Log.e("Firebase", "Error getting documents: ", e));
                } else {
                    fechas.clear();
                    listobj.setEnabled(false);
                    ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, fechas);
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    listobj.setAdapter(adapter2);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        listobj.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fechabusqueda = parent.getItemAtPosition(position).toString();
                if (!fechabusqueda.isEmpty()) {

                } else {
                    creainfo.setClickable(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        creainfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listobj.getSelectedItemPosition() == AdapterView.INVALID_POSITION || (listobj.getSelectedItemPosition() == -1)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Fecha vacia"); // Título del diálogo
                    builder.setMessage("Para generar un informe debe indicar la fecha de este.");
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    if (namegas.getText().toString().equals("")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Gasolinera sin nombre"); // Título del diálogo
                        builder.setMessage("Para generar un informe debe indicar el nombre de la gasolinera.");
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    } else {
                        switch (busqueda) {
                            case "informes_diarios":
                                tomadiario(db, currentuser, busqueda, fechabusqueda);
                                break;
                            case "informes_semanales":
                                tomasemanal(db, currentuser, busqueda, fechabusqueda);
                                break;
                            case "informes_mensuales":
                                tomamensual(db, currentuser, busqueda, fechabusqueda);
                                break;
                        }
                    }
                }
            }
        });

        return view;
    }

    private void tomadiario(FirebaseFirestore db, String currentuser, String busqueda, String fechabusqueda) {
        // Strings donde almacenaremos los mensajes
        String d1 = "", d2 = "", d3 = "", d4 = "", d5 = "", d6 = "", d7 = "", d8 = "", d9 = "", d10 = "", d11 = "", d12 = "";
        String[] mensajes = new String[]{d1, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12};

        // Strings donde almacenaremos el valor de los radiobutton
        String rb1 = "", rb2 = "", rb3 = "", rb4 = "", rb5 = "", rb6 = "", rb7 = "", rb8 = "", rb9 = "", rb10 = "", rb11 = "", rb12 = "";
        String[] valoresRadioButton = new String[]{rb1, rb2, rb3, rb4, rb5, rb6, rb7, rb8, rb9, rb10, rb11, rb12};

        String[] nombremensajes = new String[]{"txtsaedi", "txtpistil1", "txtpistil2", "txtsurti1", "txtsurti2", "txtarq", "txtextint", "txtzona", "txtconsola1", "txtconsola2", "txtconsola3", "txtconsola4"};
        String[] nombresrg = new String[]{"rgedi", "rgpistil1", "rgpistil2", "rgsurti1", "rgsurti2", "rgarq", "rgextint", "rgzona", "rgconsola1", "rgconsola2", "rgconsola3", "rgconsola4"};

        db.collection(busqueda)
                .whereEqualTo("usuario", currentuser)
                .whereEqualTo("fecha", fechabusqueda)
                .get()
                .addOnSuccessListener(ds -> {
                    DocumentReference docRef = null;
                    for (QueryDocumentSnapshot document : ds) {
                        // Almacenar la referencia del documento
                        docRef = document.getReference();

                        for (int x = 0; x < nombremensajes.length; x++) {
                            mensajes[x] = document.getString(nombremensajes[x]);
                            Log.d("VALORmen toma diario", mensajes[x]);
                        }
                        for (int y = 0; y < nombresrg.length; y++) {
                            valoresRadioButton[y] = document.getString(nombresrg[y]);
                            Log.d("VALORrb toma diario", valoresRadioButton[y]);
                        }
                    }
                    if (docRef != null) {

                        // Crear el PDF con los datos obtenidos
                        rutabbdd = CreaPDF(busqueda, mensajes, valoresRadioButton, currentuser, fechabusqueda);

                        // Actualizar el documento
                        Map<String, Object> informes_diarios = new HashMap<>();
                        informes_diarios.put("generado", true);
                        informes_diarios.put("ruta_archivo", rutabbdd[0]);
                        informes_diarios.put("nombredc", rutabbdd[1]);
                        docRef.update(informes_diarios)
                                .addOnSuccessListener(aVoid -> Log.d("FirestoreUpdate", "Document updated successfully"))
                                .addOnFailureListener(e -> Log.e("FirestoreUpdate", "Error updating document", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("FirestoreQuery", "Error al obtener los documentos", e));
    }


    private void tomasemanal(FirebaseFirestore db, String currentuser, String busqueda, String fechabusqueda) {
        //Strings donde almacenaremos los mensajes
        String d1 = "", d2 = "", d3 = "", d4 = "", d5 = "", d6 = "", d7 = "", d8 = "", d9 = "", d10 = "", d11 = "", d12 = "", d13 = "", d14 = "";
        String[] mensajes = new String[]{d1, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12, d13, d14};
        //Strings donde almacenaremos el valor de los radiobutton
        String rb1 = "", rb2 = "", rb3 = "", rb4 = "", rb5 = "", rb6 = "", rb7 = "", rb8 = "", rb9 = "", rb10 = "", rb11 = "", rb12 = "", rb13 = "", rb14 = "";
        String[] valoresRadioButton = new String[]{rb1, rb2, rb3, rb4, rb5, rb6, rb7, rb8, rb9, rb10, rb11, rb12, rb13, rb14};
        String nombresrg[] = new String[]{"rgpistil1", "rgpistil2", "rgsurtis", "rgarq1", "rgarq2", "rgarq3", "rgarq4", "rgpozo", "rgdescarga1", "rgdescarga2", "rgdescarga3", "rgredagua", "rgresi1", "rgresi2"};
        String nombremensajes[] = new String[]{"txtpistil1", "txtpistil2", "txtsurtis", "txtarq1", "txtarq2", "txtarq3", "txtarq4", "txtpozo", "txtdescarga1", "txtdescarga2", "txtdescarga3", "txtredagua", "txtresi1", "txtresi2"};
        db.collection(busqueda)
                .whereEqualTo("usuario", currentuser)
                .whereEqualTo("fecha", fechabusqueda)
                .get()
                .addOnSuccessListener(ds -> {
                    DocumentReference docRef = null;
                    for (QueryDocumentSnapshot document : ds) {
                        docRef = document.getReference();
                        for (int x = 0; x < nombremensajes.length; x++) {
                            mensajes[x] = document.getString(nombremensajes[x]);
                        }
                        for (int y = 0; y < nombresrg.length; y++) {
                            valoresRadioButton[y] = document.getString(nombresrg[y]);
                        }
                    }
                    if (docRef != null) {

                        // Crear el PDF con los datos obtenidos
                        rutabbdd = CreaPDF(busqueda, mensajes, valoresRadioButton, currentuser, fechabusqueda);

                        // Actualizar el documento
                        Map<String, Object> informes_semanales = new HashMap<>();
                        informes_semanales.put("generado", true);
                        informes_semanales.put("ruta_archivo", rutabbdd[0]);
                        informes_semanales.put("nombredc", rutabbdd[1]);
                        docRef.update(informes_semanales)
                                .addOnSuccessListener(aVoid -> Log.d("FirestoreUpdate", "Documento actualizado"))
                                .addOnFailureListener(e -> Log.e("FirestoreUpdate", "Error aztualizando el documento", e));
                    }
                });
    }

    private void tomamensual(FirebaseFirestore db, String currentuser, String busqueda, String fechabusqueda) {
        //Strings donde almacenaremos los mensajes
        String d1 = "", d2 = "", d3 = "", d4 = "", d5 = "", d6 = "", d7 = "", d8 = "", d9 = "", d10 = "", d11 = "", d12 = "", d13 = "", d14 = "";
        String[] mensajes = new String[]{d1, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12, d13, d14};
        //Strings donde almacenaremos el valor de los radiobutton
        String rb1 = "", rb2 = "", rb3 = "", rb4 = "", rb5 = "", rb6 = "", rb7 = "", rb8 = "", rb9 = "", rb10 = "", rb11 = "", rb12 = "", rb13 = "", rb14 = "";
        String[] valoresRadioButton = new String[]{rb1, rb2, rb3, rb4, rb5, rb6, rb7, rb8, rb9, rb10, rb11, rb12, rb13, rb14};
        String[] nombresrg = {"rgsurti1", "rgsurti2", "rgsurti3", "rgarq1", "rgarq2", "rgcompre1", "rgcompre2", "rgextint", "rgreda1", "rgreda2", "rgreda3", "rgresi", "rgtanq1", "rgtanq2"};
        String nombremensajes[] = new String[]{"txtsurti1", "txtsurti2", "txtsurti3", "txtarq1", "txtarq2", "txtcompre1", "txtcompre2", "txtextint", "txtreda1", "txtreda2", "txtreda3", "txtresi", "txttanq1", "txttanq2"};

        db.collection(busqueda)
                .whereEqualTo("usuario", currentuser)
                .whereEqualTo("fecha", fechabusqueda)
                .get()
                .addOnSuccessListener(ds -> {
                    DocumentReference docRef = null;
                    for (QueryDocumentSnapshot document : ds) {
                        docRef = document.getReference();
                        for (int x = 0; x < nombremensajes.length; x++) {
                            mensajes[x] = document.getString(nombremensajes[x]);
                        }
                        for (int y = 0; y < nombresrg.length; y++) {
                            valoresRadioButton[y] = document.getString(nombresrg[y]);
                        }
                    }
                    if (docRef != null) {

                        // Crear el PDF con los datos obtenidos
                        rutabbdd = CreaPDF(busqueda, mensajes, valoresRadioButton, currentuser, fechabusqueda);

                        // Actualizar el documento
                        Map<String, Object> informes_mensuales = new HashMap<>();
                        informes_mensuales.put("generado", true);
                        informes_mensuales.put("ruta_archivo", rutabbdd[0]);
                        informes_mensuales.put("nombredc", rutabbdd[1]);
                        docRef.update(informes_mensuales)
                                .addOnSuccessListener(aVoid -> Log.d("FirestoreUpdate", "Documento actualizado"))
                                .addOnFailureListener(e -> Log.e("FirestoreUpdate", "Error aztualizando el documento", e));
                    }
                });
    }

    private String[] CreaPDF(String busqueda, String[] mensajes, String[] valoresRadioButton, String currentuser, String fechabusqueda) {
        nombregas = namegas.getText().toString();
        switch (busqueda) {
            case "informes_diarios":
                rutalmacena = PDFdiario(busqueda, mensajes, valoresRadioButton, currentuser, fechabusqueda, nombregas);
                break;
            case "informes_semanales":
                rutalmacena = PDFsemanal(busqueda, mensajes, valoresRadioButton, currentuser, fechabusqueda, nombregas);
                break;
            case "informes_mensuales":
                rutalmacena = PDFmensual(busqueda, mensajes, valoresRadioButton, currentuser, fechabusqueda, nombregas);
                break;
        }
        return rutalmacena;
    }

    private void Notifica(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "creainforme");
        builder.setSmallIcon(R.drawable.gas_station);
        builder.setContentTitle("Informe creado");
        builder.setContentText("¡Se ha creado el informe con fecha " + fechabusqueda + " para " + nombregas + "!");

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        managerCompat.notify(1, builder.build());
    }

    private String[] PDFdiario(String busqueda, String[] mensajes, String[] valoresRadioButton, String currentuser, String fechabusqueda, String nombregas) {
        try {
            // Obtener la marca de tiempo actual para generar un nombre de archivo único
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            Font fontNegrita = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);

            String carpeta = "/informesOrgasnizer";
            String DEST = "/informesOrgasnizer/informediario_" + fechabusqueda + "_" + timestamp + ".pdf";
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + carpeta;

            File file = new File(getContext().getExternalFilesDir(null), "/informesOrgasnizer/");
            if (!file.exists()) {
                file.mkdirs();
            }


            File archivo = new File(file, "informediario_" + timestamp + ".pdf");
            String namedc = "informediario_" + timestamp + ".pdf";
            FileOutputStream fos = new FileOutputStream(archivo);
            FileInputStream fil = new FileInputStream(archivo);

            Document documento = new Document();
            PdfWriter.getInstance(documento, fos);

            documento.open();
            Paragraph titulo = new Paragraph(
                    "INFORME DIARIO \n\n\n",
                    FontFactory.getFont("arial", 22, Font.BOLD, BaseColor.BLUE)
            );
            documento.add(titulo);
            PdfPTable tabla = new PdfPTable(3);
            PdfPCell cell1 = new PdfPCell(new Phrase("Nombre E.S.: " + nombregas, fontNegrita));
            PdfPCell cell2 = new PdfPCell(new Phrase("Usuario: " + currentuser, fontNegrita));
            PdfPCell cell3 = new PdfPCell(new Phrase("Fecha: " + fechabusqueda, fontNegrita));
            tabla.addCell(cell1);
            tabla.addCell(cell2);
            tabla.addCell(cell3);
            int r = 0;
            tabla.addCell(getCell(1, "Estado"));
            tabla.addCell(getCell(2, "Acciones"));
            tabla.addCell(getCell(10, "Edificio"));
            tabla.addCell(getCell(10, "Sistema de detección y extracción de atmósferas explosivas: en correcto funcionamiento (semáforo en verde) solo si hay sótano."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Pista e isletas"));
            tabla.addCell(getCell(10, "Lamas de falso techo y otros elementos de imagen sin riesgo de caída (no se observa que estén sueltos o puedan desprenderse)."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Pavimento sin restos de hidrocarburo y/o derrames (zona de descarga y aparatos surtidores)."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Surtidores"));
            tabla.addCell(getCell(10, "Aparatos surtidores sin defectos importantes (impactos, tapas de carcasas abiertas, etc)."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Comprobar que en el primer suministro de la mañana no hay retraso en la salida de producto (bomba descebada)."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Arquetas de tanques"));
            tabla.addCell(getCell(10, "Tapas correctamente colocadas y cerradas."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Extintores"));
            tabla.addCell(getCell(10, "Número y tipo adecuado y correctamente posicionados (extintor de polvo ABC por aparato surtidor y descarga)."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Zona de descarga"));
            tabla.addCell(getCell(10, "Tapas correctamente colocadas y cerradas."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Consola de control - Sondas de Medición"));
            tabla.addCell(getCell(10, "La consola presenta todas las funciones normales y está en funcionamiento."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Ausencia de alarma de agua en tanques. En caso de alarma, realizar medición con varilla y pasta busca-aguas."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Ausencia de alarmas en doble pared (alarmas L1, L2, L3, etc)."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Ausencia de alarmas en sensor de PLLD (alarmas Q1, Q2, Q3, etc), solo para tuberías de impulsión y bombas sumergidas."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));


            documento.add(tabla);
            documento.close();
            Notifica();
            Intent intent = new Intent(getContext(), PdfVista.class);
            intent.putExtra("archivo", file.getAbsolutePath());
            intent.putExtra("namedc", namedc);
            startActivity(intent);
            String[] valoresreturn = new String[2];
            valoresreturn[0] = file.getAbsolutePath();
            valoresreturn[1] = namedc;
            return valoresreturn;

        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
            String[] vacio = new String[2];
            return vacio;
        }
    }


    private String[] PDFsemanal(String busqueda, String[] mensajes, String[] valoresRadioButton, String currentuser, String fechabusqueda, String nombregas) {
        try {
            // Obtener la marca de tiempo actual para generar un nombre de archivo único
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            Font fontNegrita = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);

            String carpeta = "/informesOrgasnizer";
            String DEST = "/informesOrgasnizer/informediario_" + fechabusqueda + "_" + timestamp + ".pdf";
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + carpeta;

            File file = new File(getContext().getExternalFilesDir(null), "/informesOrgasnizer/");
            if (!file.exists()) {
                file.mkdirs();
            }

            Log.d("VALOR", mensajes[3]);

            File archivo = new File(file, "informesemanal_" + timestamp + ".pdf");
            String namedc = "informesemanal_" + timestamp + ".pdf";
            FileOutputStream fos = new FileOutputStream(archivo);

            Document documento = new Document();
            PdfWriter.getInstance(documento, fos);

            documento.open();
            Paragraph titulo = new Paragraph(
                    "INFORME SEMANAL \n\n\n",
                    FontFactory.getFont("arial", 18, Font.BOLD, BaseColor.BLUE)
            );
            documento.add(titulo);
            PdfPTable tabla = new PdfPTable(3);
            PdfPCell cell1 = new PdfPCell(new Phrase("Nombre E.S.: " + nombregas, fontNegrita));
            PdfPCell cell2 = new PdfPCell(new Phrase("Usuario: " + currentuser, fontNegrita));
            PdfPCell cell3 = new PdfPCell(new Phrase("Fecha: " + fechabusqueda, fontNegrita));
            tabla.addCell(cell1);
            tabla.addCell(cell2);
            tabla.addCell(cell3);
            int r = 0;
            tabla.addCell(getCell(1, "Estado"));
            tabla.addCell(getCell(2, "Acciones"));
            tabla.addCell(getCell(10, "Pista e isletas"));
            tabla.addCell(getCell(10, "En caso de existir señalización de acceso y salida, ambas están en buen estado."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "La Pista no presenta baches o fisuras considerables."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Surtidores"));
            tabla.addCell(getCell(10, "Aparatos surtidores sin goteos en el interior, mangueras y boquerel sin fugas."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Arquetas de tanques"));
            tabla.addCell(getCell(10, "Arquetas libres de agua, en el caso de existir hay que extraerla con la bomba."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Arquetas sin presencia de producto que pueda provenir. de una fuga o sobrellenado."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Tubos de medición con tapón correctamente cerrado."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Canalizaciones de cableado de sonda con sellado visible (espuma de poliuretano)."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Pozos"));
            tabla.addCell(getCell(10, "Carteleria en punto de suministro indicando agua no potable."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Zona de descarga"));
            tabla.addCell(getCell(10, "Arquetas antiderrame libres de producto."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Válvula de drenaje se encuentra en buen estado: estanca cuando está cerrada y abre al tirar de la la cadena (para (para escurrir el producto que pueda quedar en la misma tras la descarga)."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Pinza de toma de tierra para el camión de descarga en. correcto estado, cableado en buen estado, vástago no obturado."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Red de aguas HCs y separador"));
            tabla.addCell(getCell(10, "Rejillas de desagüe de aguas hidrocarburadas no obturadas (pueden recoger un vertido en caso de producirse)."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Residuos"));
            tabla.addCell(getCell(10, "Recipiente para sepiolita en buen estado, correctamente situado."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Producto absorbente (sepiolita) limpio, sin elementos. extraños (nunca serrín) y con cantidad suficiente para recoger un vertido accidental."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            documento.add(tabla);
            documento.close();
            Notifica();
            Intent intent = new Intent(getContext(), PdfVista.class);
            intent.putExtra("archivo", file.getAbsolutePath());
            intent.putExtra("namedc", namedc);
            String[] valoresreturn = new String[2];
            valoresreturn[0] = file.getAbsolutePath();
            valoresreturn[1] = namedc;
            startActivity(intent);
            return valoresreturn;

        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
            String[] vacio = new String[2];
            return vacio;
        }
    }

    private String[] PDFmensual(String busqueda, String[] mensajes, String[] valoresRadioButton, String currentuser, String fechabusqueda, String nombregas) {
        try {
            // Obtener la marca de tiempo actual para generar un nombre de archivo único
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            Font fontNegrita = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);

            String carpeta = "/informesOrgasnizer";
            String DEST = "/informesOrgasnizer/informediario_" + fechabusqueda + "_" + timestamp + ".pdf";
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + carpeta;

            File file = new File(getContext().getExternalFilesDir(null), "/informesOrgasnizer/");
            if (!file.exists()) {
                file.mkdirs();
            }

            Log.d("VALOR", mensajes[3]);

            File archivo = new File(file, "informemensual_" + timestamp + ".pdf");
            String namedc = "informemensual_" + timestamp + ".pdf";
            FileOutputStream fos = new FileOutputStream(archivo);

            Document documento = new Document();
            PdfWriter.getInstance(documento, fos);

            documento.open();
            Paragraph titulo = new Paragraph(
                    "INFORME MENSUAL \n\n\n",
                    FontFactory.getFont("arial", 18, Font.BOLD, BaseColor.BLUE)
            );
            documento.add(titulo);
            PdfPTable tabla = new PdfPTable(3);
            PdfPCell cell1 = new PdfPCell(new Phrase("Nombre E.S.: " + nombregas, fontNegrita));
            PdfPCell cell2 = new PdfPCell(new Phrase("Usuario: " + currentuser, fontNegrita));
            PdfPCell cell3 = new PdfPCell(new Phrase("Fecha: " + fechabusqueda, fontNegrita));
            tabla.addCell(cell1);
            tabla.addCell(cell2);
            tabla.addCell(cell3);
            int r = 0;
            tabla.addCell(getCell(1, "Estado"));
            tabla.addCell(getCell(2, "Acciones"));
            tabla.addCell(getCell(10, "Surtidores"));
            tabla.addCell(getCell(10, "Aparatos verificados por Organismo de Control, con el adhesivo de verificación en lugar visible del AS."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Instrucciones de uso de los aparatos con las medidas de seguridad y cartel de instrucciones de emergencia para clientes expuestas en un lugar visible y buenas condiciones."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Afericiones de cada una de las mangueras, con el correspondiente registro y envio al responsable comercial."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Arquetas eléctricas"));
            tabla.addCell(getCell(10, "Canalizaciones eléctricas antes del cuadro eléctrico de la ES con sellado visible."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Comprobación del relleno de arena después de cada actuación (se comprueba el relleno de arena después de una obra o mantenimiento que lo hayan podido quitar)."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Compresor de aire"));
            tabla.addCell(getCell(10, "Revisión del nivel de aceite y regulación del presos."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Protegido y libre de obstáculos (accesible)."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Extintores"));
            tabla.addCell(getCell(10, "Extintores con fechas de inspección vigentes, presión correcta y sin pegatinas no autorizadas."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Red de aguas HCs y separador"));
            tabla.addCell(getCell(10, "Separador de hidrocarburos (comprobar que no están saturado o rebosa)."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Observar visualmente que el líquido en la arqueta de toma de muestras no presenta hidrocarburo en fase libre."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Comprobación visual del punto de vertido final (aparentemente el vertido es correcto, no apreciándose hidrocarburo en fase libre), especialmente en entornos sensibles."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Residuos"));
            tabla.addCell(getCell(10, "Armario de residuos peligrosos en buen estado y bidones etiquetados."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Tanques doble pared"));
            tabla.addCell(getCell(10, "Revisión de vacuómetro en arqueta de boca de hombre: comprobación de vacio (solo si existe y es visible)."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));
            r++;
            tabla.addCell(getCell(10, "Comprobación de funcionamiento (en la consola) del sensor de liquido de la doble pared o sistema SGB de Rafibra (si existe)."));
            tabla.addCell(getCell(1, valoresRadioButton[r]));
            tabla.addCell(getCell(2, mensajes[r]));

            documento.add(tabla);
            documento.close();
            Notifica();
            Intent intent = new Intent(getContext(), PdfVista.class);
            intent.putExtra("archivo", file.getAbsolutePath());
            intent.putExtra("namedc", namedc);
            startActivity(intent);
            String[] valoresreturn = new String[2];
            valoresreturn[0] = file.getAbsolutePath();
            valoresreturn[1] = namedc;
            return valoresreturn;

        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
            String[] vacio = new String[2];
            return vacio;
        }
    }

    private PdfPCell getCell(int cm, String texto) {
        PdfPCell cell = new PdfPCell();
        cell.setColspan(cm);
        cell.setUseAscender(true);
        cell.setUseDescender(true);
        if(cm == 10){
            Paragraph p = new Paragraph(
                    String.format(texto),
                    FontFactory.getFont("arial", 8, Font.BOLD, BaseColor.BLACK));
            p.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(p);
        } else if (texto.equals("Estado") || texto.equals("Acciones")) {
            Paragraph p = new Paragraph(
                    String.format(texto),
                    FontFactory.getFont("arial", 10, Font.BOLD, BaseColor.BLACK));
            p.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(p);
        } else {
            Paragraph p = new Paragraph(
                    String.format(texto),
                    FontFactory.getFont("arial", 8, Font.NORMAL, BaseColor.BLACK));
            p.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(p);
        }
        return cell;
    }

}

