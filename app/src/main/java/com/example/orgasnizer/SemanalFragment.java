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

public class SemanalFragment extends Fragment {

    private RadioGroup a, b, c, d, e ,f, g, h, i ,j, k, l, m, n;
    //Edit text de los mensajes a enviar
    EditText et1, et2, et3, et4, et5, et6, et7, et8, et9, et10, et11, et12, et13, et14;
    //Strings donde almacenaremos los mensajes
    String d1, d2, d3, d4, d5, d6, d7, d8, d9, d10, d11, d12, d13, d14;
    //Strings donde almacenaremos el valor de los radiobutton
    String rb1, rb2, rb3, rb4, rb5, rb6, rb7, rb8, rb9, rb10, rb11, rb12, rb13, rb14;
    String[] rbfor;
    String[] nombresrg;
    RadioGroup[] botoness;
    Button auto;
    Button enviasem;
    EditText selfecs;
    FirebaseAuth mAuth;
    String currentuser;
    String fechainfor;
    FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_semanal, container, false);
        selfecs = view.findViewById(R.id.etfechass);
        auto = view.findViewById(R.id.autodias);
        enviasem = view.findViewById(R.id.enviarsem);
        a = view.findViewById(R.id.rgpistsem1);
        b = view.findViewById(R.id.rgpistsem2);
        c = view.findViewById(R.id.rgsurtis);
        d = view.findViewById(R.id.rgarqs1);
        e = view.findViewById(R.id.rgarqs2);
        f = view.findViewById(R.id.rgarqs3);
        g = view.findViewById(R.id.rgarqs4);
        h = view.findViewById(R.id.rgpozoss);
        i = view.findViewById(R.id.rgdescass1);
        j = view.findViewById(R.id.rgdescass2);
        k = view.findViewById(R.id.rgdescass3);
        l = view.findViewById(R.id.rgreds);
        m = view.findViewById(R.id.rgresis1);
        n = view.findViewById(R.id.rgresis2);
        //EditTexts
        et1 = view.findViewById(R.id.desapistsem1);
        et2 = view.findViewById(R.id.desapistsem2);
        et3 = view.findViewById(R.id.desasurtis);
        et4 = view.findViewById(R.id.desaarqs1);
        et5 = view.findViewById(R.id.desaarqs2);
        et6 = view.findViewById(R.id.desaarqs3);
        et7 = view.findViewById(R.id.desaarqs4);
        et8 = view.findViewById(R.id.desapozoss);
        et9 = view.findViewById(R.id.desadescass1);
        et10 = view.findViewById(R.id.desadescass2);
        et11 = view.findViewById(R.id.desadescass3);
        et12 = view.findViewById(R.id.desareds);
        et13 = view.findViewById(R.id.desaresis1);
        et14 = view.findViewById(R.id.desaresis2);
        EditText[] editTextArray = {et1, et2, et3, et4, et5, et6, et7, et8, et9, et10, et11, et12, et13, et14};
        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser().getDisplayName();
        db = FirebaseFirestore.getInstance();
        botoness = new RadioGroup[]{a, b, c, d, e, f, g, h, i, j, k, l, m, n};
        nombresrg = new String[]{"rgpistil1", "rgpistil2", "rgsurtis", "rgarq1", "rgarq2", "rgarq3", "rgarq4", "rgpozo", "rgdescarga1", "rgdescarga2", "rgdescarga3", "rgredagua", "rgresi1", "rgresi2"};
        String nombremensajes[] = new String[]{"txtpistil1", "txtpistil2", "txtsurtis", "txtarq1", "txtarq2", "txtarq3", "txtarq4", "txtpozo", "txtdescarga1", "txtdescarga2", "txtdescarga3", "txtredagua", "txtresi1", "txtresi2"};
        auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i=0; i<botoness.length; i++){
                    RadioGroup aaceptargrupo = botoness[i];
                    RadioButton aaceptarboton = (RadioButton)  aaceptargrupo.getChildAt(1);
                    aaceptarboton.setChecked(true);
                }
            }
        });
        enviasem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fechainfor = selfecs.getText().toString();
                if(fechainfor.isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("Fecha vacia"); // Título del diálogo
                    builder.setMessage("Para guardar un informe debe indicar la fecha de este.");
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    CollectionReference informessemanales = db.collection("informes_semanales");
                    informessemanales.whereEqualTo("fecha", fechainfor).get().addOnSuccessListener(queryDocumentSnapshots -> {
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
                                            creainformev(editTextArray, nombremensajes);
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
                            creainformev(editTextArray, nombremensajes);
                        }
                    });
                }
            }
        });
        //Metodo para seleccionar una fecha
        selfecs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                switch (v.getId()){
                    case R.id.etfechass:
                        showDatePickerDialog();
                        break;
                }
            }
        });
        return view;
    }

    private void creainformev(EditText[] editTextArray, String nombremensajes[]) {
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
        for (int i = 0; i < botoness.length; i++) {
            RadioGroup compruebagrupo = botoness[i];
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
            Map<String, Object> informes_semanales = new HashMap<>();
            informes_semanales.put("usuario", currentuser);
            informes_semanales.put("fecha", fechainfor);
            informes_semanales.put("generado", false);
            for(int x=0; x<14; x++){
                informes_semanales.put(nombresrg[x], valoresrb.get(x));
                informes_semanales.put(nombremensajes[x], mensajesarray[x]);
            }
            db.collection("informes_semanales").add(informes_semanales).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
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
                RadioGroup compruebagrupo = botoness[l];
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
                final String selectedDateDia = day + " / " + (month+1) + " / " + year + "  |  " + (day+7) + " / " + (month+1) + " / " + year;
                selfecs.setText(selectedDateDia);
            }
        });

        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }
}