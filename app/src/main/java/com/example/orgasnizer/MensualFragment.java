package com.example.orgasnizer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MensualFragment extends Fragment {

    private RadioGroup a, b, c, d, e ,f, g, h, i ,j, k, l, m, n;
    //Edit text de los mensajes a enviar
    EditText et1, et2, et3, et4, et5, et6, et7, et8, et9, et10, et11, et12, et13, et14;
    //Strings donde almacenaremos los mensajes
    String d1, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12, d13, d14;
    RadioGroup[] botonesm;
    Button enviames;
    Button auto;
    EditText selfecm;
    String[] mesesDelAno = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
    FirebaseAuth mAuth;
    String currentuser;
    String fechainfor;
    FirebaseFirestore db;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mensual, container, false);
        selfecm = view.findViewById(R.id.etfecham);
        auto = view.findViewById(R.id.autodiam);
        enviames = view.findViewById(R.id.enviarmes);
        //RadioButtons
        a = view.findViewById(R.id.rgsurti1);
        b = view.findViewById(R.id.rgsurti2);
        c = view.findViewById(R.id.rgsurti3);
        d = view.findViewById(R.id.rgarq1);
        e = view.findViewById(R.id.rgarq2);
        f = view.findViewById(R.id.rgcompre1);
        g = view.findViewById(R.id.rgcompre2);
        h = view.findViewById(R.id.rgextint);
        i = view.findViewById(R.id.rgreda1);
        j = view.findViewById(R.id.rgreda2);
        k = view.findViewById(R.id.rgreda3);
        l = view.findViewById(R.id.rgresi);
        m = view.findViewById(R.id.rgtanq1);
        n = view.findViewById(R.id.rgtanq2);
        //EditTexts
        et1 = view.findViewById(R.id.desasurti1);
        et2 = view.findViewById(R.id.desasurti2);
        et3 = view.findViewById(R.id.desasurti3);
        et4 = view.findViewById(R.id.desaarq1);
        et5 = view.findViewById(R.id.desaarq2);
        et6 = view.findViewById(R.id.desacompre1);
        et7 = view.findViewById(R.id.desacompre2);
        et8 = view.findViewById(R.id.desaextint);
        et9 = view.findViewById(R.id.desareda1);
        et10 = view.findViewById(R.id.desareda2);
        et11 = view.findViewById(R.id.desareda3);
        et12 = view.findViewById(R.id.desaresi);
        et13 = view.findViewById(R.id.desatanq1);
        et14 = view.findViewById(R.id.desatanq2);
        botonesm = new RadioGroup[]{a, b, c, d, e, f, g, h, i, j, k, l, m, n};
        EditText[] editTextArray = {et1, et2, et3, et4, et5, et6, et7, et8, et9, et10, et11, et12, et13, et14};
        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser().getDisplayName();
        db = FirebaseFirestore.getInstance();
        String[] nombresrg = {"rgsurti1", "rgsurti2", "rgsurti3", "rgarq1", "rgarq2", "rgcompre1", "rgcompre2", "rgextint", "rgreda1", "rgreda2", "rgreda3", "rgresi", "rgtanq1", "rgtanq2"};
        String nombremensajes[] = new String[]{"txtsurti1", "txtsurti2", "txtsurti3", "txtarq1", "txtarq2", "txtcompre1", "txtcompre2", "txtextint", "txtreda1", "txtreda2", "txtreda3", "txtresi", "txttanq1", "txttanq2"};
        auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i=0; i<botonesm.length; i++){
                    RadioGroup aaceptargrupo = botonesm[i];
                    RadioButton aaceptarboton = (RadioButton)  aaceptargrupo.getChildAt(1);
                    aaceptarboton.setChecked(true);
                }
            }
        });
        enviames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fechainfor = selfecm.getText().toString();
                if(fechainfor.isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("Fecha vacia"); // Título del diálogo
                    builder.setMessage("Para guardar un informe debe indicar la fecha de este.");
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    CollectionReference informesmensuales = db.collection("informes_mensuales");
                    informesmensuales.whereEqualTo("fecha", fechainfor).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Si ya existe un informe para esta fecha
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Informe existente"); // Título del diálogo
                            builder.setMessage("Ya existe un informe para esta fecha, ¿desea borrarlo y crear uno nuevo?"); // Mensaje del diálogo

                            builder.setPositiveButton("Crear nuevo", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                                        document.getReference().delete().addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Informe borrado", Toast.LENGTH_LONG).show();
                                            creainformev(editTextArray, nombremensajes, nombresrg);
                                        }).addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Error al borrar el informe", Toast.LENGTH_LONG).show();
                                        });
                                    }
                                }
                            });

                            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getContext(), "Informe cancelado", Toast.LENGTH_LONG).show();
                                }
                            });

                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        } else {
                            // Si no existe un informe para esta fecha
                            creainformev(editTextArray, nombremensajes, nombresrg);
                        }
                    });
                }
            }
        });
        //Metodo para seleccionar una fecha
        selfecm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                switch (v.getId()){
                    case R.id.etfecham:
                        showDatePickerDialog();
                        break;
                }
            }
        });
        return view;
    }

    private void creainformev(EditText[] editTextArray, String nombremensajes[], String nombresrg[]) {
        ArrayList<String> valoresrb = new ArrayList<>();
        //Obtenemos los mensajes de los edittext
        d1 = et1.getText().toString();
        d2 = et2.getText().toString();
        d3 = et3.getText().toString();
        d4 = et4.getText().toString();
        d5 = et5.getText().toString();
        d6 = et6.getText().toString();
        d7 = et7.getText().toString();
        d8 = et8.getText().toString();
        d9 = et9.getText().toString();
        d10 = et10.getText().toString();
        d11 = et11.getText().toString();
        d12 = et12.getText().toString();
        d13 = et13.getText().toString();
        d14 = et14.getText().toString();
        String mensajesarray[] = new String[]{d1, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12, d13, d14};
        int todobien = 0;
        for (int i = 0; i < botonesm.length; i++) {
            RadioGroup compruebagrupo = botonesm[i];
            RadioButton b, m, n;
            b = (RadioButton) compruebagrupo.getChildAt(1);
            m = (RadioButton) compruebagrupo.getChildAt(3);
            n = (RadioButton) compruebagrupo.getChildAt(5);

            String valor;
            if (b.isChecked()) {
                valor = "Bien";
            } else if (m.isChecked()) {
                valor = "Mal";
            } else if (n.isChecked()) {
                valor = "NA";
            } else {
                valor = "";
            }
            valoresrb.add(valor);

            if (!b.isChecked() && !m.isChecked() && !n.isChecked()) {
                todobien++;
            }
        }
        if (todobien != 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Campos vacíos"); // Título del diálogo
            builder.setMessage("Para guardar un informe debe rellenar todos los campos.");
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else{
            Map<String, Object> informes_mensuales = new HashMap<>();
            informes_mensuales.put("usuario", currentuser);
            informes_mensuales.put("fecha", fechainfor);
            informes_mensuales.put("generado", false);
            for(int x=0; x<14; x++){
                informes_mensuales.put(nombresrg[x], valoresrb.get(x));
                informes_mensuales.put(nombremensajes[x], mensajesarray[x]);
            }
            db.collection("informes_mensuales").add(informes_mensuales).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(getContext(), "Informe guardado con éxito", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Registra el error en el logcat
                            Log.e("ERROR BBDD", "Error al guardar el informe", e);
                            // Muestra un mensaje al usuario indicando que ha ocurrido un error
                            Toast.makeText(getContext(), "No se ha podido guardar el informe", Toast.LENGTH_LONG).show();
                        }
                    });
            for (int l=0; l< 14; l++){
                RadioGroup compruebagrupo = botonesm[l];
                compruebagrupo.clearCheck();
                editTextArray[l].setText("");
                if(l==14){
                    break;
                }
            }
        }
    }

    private void showDatePickerDialog() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because January is zero
                String mes = "";
                for (int i = 0; i<mesesDelAno.length; i++){
                    if (i == month){
                        mes = mesesDelAno[i];
                    }
                }
                final String selectedDateDia = mes + " " + year;
                selfecm.setText(selectedDateDia);
            }
        });

        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }
}