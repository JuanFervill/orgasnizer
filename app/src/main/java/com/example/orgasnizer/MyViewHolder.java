package com.example.orgasnizer;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MyViewHolder extends RecyclerView.ViewHolder {
    TextView fecha;
    ImageView borrar;
    LinearLayout raya;
    FirebaseFirestore db;
    String busqueda, fechabusqueda, currentuser, rutapdf, nombrepdf;
    FirebaseAuth mAuth;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getDisplayName() : "defaultUser";
        borrar = itemView.findViewById(R.id.borrainforme);
        raya = itemView.findViewById(R.id.rayaitem);
        fecha = itemView.findViewById(R.id.fechavi);
        fecha.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SuspiciousIndentation")
            @Override
            public void onClick(View v) {
              fechabusqueda = fecha.getText().toString();
              abrepdf(fechabusqueda, v, currentuser);
            }
        });
        borrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fechabusqueda = fecha.getText().toString();
                borrapdf(fechabusqueda, v, currentuser, fecha, borrar, raya);
            }
        });
    }

    private void abrepdf(String fechabusqueda, View v, String currentuser){
        db.collection(busqueda)
                .whereEqualTo("usuario", currentuser)
                .whereEqualTo("fecha", fechabusqueda)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        rutapdf = document.getString("ruta_archivo");
                        nombrepdf = document.getString("nombredc");
                    }
                        Intent intent = new Intent(v.getContext(), PdfVista.class);
                        intent.putExtra("archivo", rutapdf);
                        intent.putExtra("namedc", nombrepdf);
                        v.getContext().startActivity(intent);
                })
                .addOnFailureListener(e -> {

                });
    }

    private void borrapdf(String fechabusqueda, View v, String currentuser, TextView fecha, ImageView borrar, LinearLayout raya) {
        db.collection(busqueda)
                .whereEqualTo("usuario", currentuser)
                .whereEqualTo("fecha", fechabusqueda)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Borrar informe"); // Título del diálogo
                        builder.setMessage("¿Desea borrar este informe?"); // Mensaje del diálogo

                        builder.setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("yass", "Pulsaul q si ");
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    Log.d("yass", "hemos entrau ");
                                    document.getReference().delete()
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(v.getContext(), "Informe borrado", Toast.LENGTH_LONG).show();
                                                fecha.setVisibility(View.INVISIBLE);
                                                borrar.setVisibility(View.INVISIBLE);
                                                raya.setVisibility(View.INVISIBLE);
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("Firestore", "Error al borrar el informe", e);
                                                Toast.makeText(v.getContext(), "Error al borrar el informe", Toast.LENGTH_LONG).show();
                                            });
                                }
                            }
                        });

                        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d("yass", "Pulsaul q no ");
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    } else {
                        Log.d("Firestore", "No se encontraron documentos para borrar");
                        Toast.makeText(v.getContext(), "No se encontraron documentos para borrar", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al obtener documentos", e);
                    Toast.makeText(v.getContext(), "Error al obtener documentos", Toast.LENGTH_LONG).show();
                });
    }


}
