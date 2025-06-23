package com.example.orgasnizer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ListaInfo extends Fragment {
    Spinner tipoinforme;
    String tipo, busqueda, currentuser, fecha;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    RecyclerView recyclerView;
    TextView nodata;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lista_info, container, false);
        tipoinforme = view.findViewById(R.id.cbtipoinforme);
        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getDisplayName() : "defaultUser";
        db = FirebaseFirestore.getInstance();
        List<Item> items = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recyclerview); // Inicializar aquí
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Configurar LayoutManager una vez
        nodata = view.findViewById(R.id.sindatos);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), // o getActivity(), o requireContext()
                R.array.tiposinfor,
                R.layout.spinner_item_selected
        );
        adapter.setDropDownViewResource(R.layout.spinner_item);
        tipoinforme.setAdapter(adapter);
        tipoinforme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipo = parent.getItemAtPosition(position).toString();
                switch (tipo) {
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
                                items.clear(); // Limpiar la lista antes de agregar nuevos elementos
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    fecha = document.getString("fecha");
                                    if (fecha != null) {
                                        if(document.getBoolean("generado")){
                                        Item item = new Item(fecha, busqueda);
                                        items.add(item);
                                        }
                                    }
                                    if(items.isEmpty()){
                                        recyclerView.setVisibility(view.INVISIBLE);
                                        nodata.setVisibility(view.VISIBLE);
                                    }else{
                                        recyclerView.setVisibility(view.VISIBLE);
                                        nodata.setVisibility(view.INVISIBLE);
                                    }
                                }
                                recyclerView.setAdapter(new MyAdapter(getContext(), items)); // Configurar el adaptador aquí
                                Snackbar.make(view, "Se han cargado todos los datos", Snackbar.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                recyclerView.setVisibility(view.INVISIBLE);
                                nodata.setVisibility(view.VISIBLE);
                            });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }
}

