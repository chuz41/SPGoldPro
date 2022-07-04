package com.example.spgold;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BorrarActivity extends AppCompatActivity {

    private String loteria;
    //private TextView lot_borrada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrar);

        loteria = getIntent().getStringExtra("Loteria");


        borrar();
        //lot_borrada = (TextView) findViewById(R.id.textView_lot_borrada);

    }

    /*
    ################################################################################################
    Naegacion hacia atras personalizada!!!
    ################################################################################################
     */
    @Override
    public void onBackPressed(){
        View view = null;
        boton_atras(view);
    }

    public void boton_atras(View view) {
        Intent Main = new Intent(this, MainActivity.class);
        startActivity(Main);
        finish();
        System.exit(0);
    }

    /*
    ################################################################################################
     */

    private void borrar () {

        String archivos[] = fileList();

        for (int i = 0; i < archivos.length; i++) {
            File archivo = new File(archivos[i]);
            Pattern pattern = Pattern.compile("loteria_sfile" + loteria + "_sfile", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(archivos[i]);
            boolean matchFound = matcher.find();
            if (matchFound) {

                guardar("BORRADA", archivos[i]);//Escribimos la palabra "BORRADA" en el archivo de la loteria que vamos a borrar. Esto para prevenir que la siguiente linea de codigo no opere correctamente.
                archivo.delete();

                View view1 = getLayoutInflater().inflate(R.layout.custom_toast, (ViewGroup) this.findViewById(R.id.layoutContainer));
                Toast toast = new Toast(this);
                toast.setGravity(Gravity.BOTTOM,0,150);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(view1);
                toast.show();
                toast.show();
                toast.show();
                Toast.makeText(this, "Loteria " + loteria + " borrada.\n\nPresione atras para salir!...", Toast.LENGTH_LONG).show();
                Toast.makeText(this, "Loteria " + loteria + " borrada.\n\nPresione atras para salir!...", Toast.LENGTH_LONG).show();
                Toast.makeText(this, "Loteria " + loteria + " borrada.\n\nPresione atras para salir!...", Toast.LENGTH_LONG).show();
                Toast.makeText(this, "Loteria " + loteria + " borrada.\n\nPresione atras para salir!...", Toast.LENGTH_LONG).show();
                //texto_v();
            }
        }
    }

    private void texto_v() {
        //String texto = "Se ha borrado la loteria " + loteria + "\n\nPresione Atras para salir...";
        //lot_borrada.setText(texto);
    }


    private void guardar (String Tcompleto, String nombre){
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre, Activity.MODE_PRIVATE));
            archivo.write(Tcompleto);
            archivo.flush();

        } catch (IOException e) {
        }
    }

}