package com.example.spgold;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
            //Log.v("Error66", "File: " + archivos[i]);
            Pattern pattern = Pattern.compile("loteria_sfile" + loteria + "_sfile", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(archivos[i]);
            boolean matchFound = matcher.find();
            if (matchFound) {
                Log.v("Error67", "Archivo encontrado. " + "\nArchivo:\n\n" + archivos[i]);
                try {
                    borrar_archivo(archivos[1]);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    borrar_archivo(archivos[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                crear_archivo(archivos[i]);
                archivo.delete();
                String alguito = imprimir_archivo(archivos[i]);
                guardar("BORRADA", archivos[i]);//Escribimos la palabra "BORRADA" en el archivo de la loteria que vamos a borrar. Esto para prevenir que la siguiente linea de codigo no opere correctamente.
                Log.v("Error68", "Loteria supuestamente borrada.\n\nContenido del archivo:\n\n" + alguito);
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
                //Toast.makeText(this, "Loteria " + loteria + " borrada.\n\nPresione atras para salir!...", Toast.LENGTH_LONG).show();
                //Toast.makeText(this, "Loteria " + loteria + " borrada.\n\nPresione atras para salir!...", Toast.LENGTH_LONG).show();
                //Toast.makeText(this, "Loteria " + loteria + " borrada.\n\nPresione atras para salir!...", Toast.LENGTH_LONG).show();
                //texto_v();
            }
        }
    }

    private void crear_archivo(String nombre_archivo) {
        try{
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre_archivo, Activity.MODE_PRIVATE));
            archivo.flush();
            archivo.close();
        }catch (IOException e) {
        }
    }

    private boolean ArchivoExiste (String[] archivos, String Tiquet){
        for (int i = 0; i < archivos.length; i++) {

            if (Tiquet.equals(archivos[i])) {
                return true;
            }
        }
        return false;
    }

    private String imprimir_archivo(String nombre_archivo){
        String archivos[] = fileList();
        String contenido = "";
        if (ArchivoExiste(archivos, nombre_archivo)) {//Archivo nombre_archivo es el archivo que vamos a imprimir
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(nombre_archivo));//Se abre archivo
                BufferedReader br = new BufferedReader(archivo);
                contenido = "";//Aqui se lee el contenido del archivo guardado.

                String linea = br.readLine();//Se lee archivo
                while (linea != null) {
                    contenido = contenido + linea + "\n";
                    linea = br.readLine();
                    //return;
                }
                //printIt(contenido);
                br.close();
                archivo.close();
            }catch (IOException e) {
            }
        }
        return contenido;
    }

    private void agregar_linea_archivo (String file_name, String new_line) {
        String archivos[] = fileList();
        String ArchivoCompleto = "";//Aqui se lee el contenido del archivo guardado.

        if (ArchivoExiste(archivos, file_name)) {
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(file_name));
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();

                while (linea != null) {
                    ArchivoCompleto = ArchivoCompleto + linea + "\n";
                    linea = br.readLine();
                }
                ArchivoCompleto = ArchivoCompleto + new_line + "\n";
                br.close();
                archivo.close();
            } catch (IOException e) {
            }
        } else {
            crear_archivo(file_name);
            agregar_linea_archivo(file_name, new_line);
            return;
        }
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(file_name, Activity.MODE_PRIVATE));
            archivo.write(ArchivoCompleto);
            archivo.flush();
        } catch (IOException e) {
        }
    }

    public void borrar_archivo(String file) throws IOException {
        File archivo = new File(file);
        String empty_string = "";
        guardar(empty_string, file);
        archivo.delete();
        archivo.getCanonicalFile().delete();
        crear_archivo(file);
        agregar_linea_archivo(file, "BORRADA");
        Log.v("ErrorBorrar_archivo", "File content:\n\n" + imprimir_archivo(file));
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