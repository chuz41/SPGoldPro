package com.example.spgold;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ConfigActivity extends AppCompatActivity {

    private TextView etiqueta;
    private Spinner spinner_dev_used;
    private TextView tv_dev_used;
    //private String Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        etiqueta = (TextView)findViewById(R.id.tv_config);
        spinner_dev_used = (Spinner) findViewById(R.id.spinner_device);
        tv_dev_used = (TextView) findViewById(R.id.textView_device);

        llenar_spinner_dev();

        //Listener para el spinner de los dispositivos:
        spinner_dev_used.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        //Crear diccionario con la informacion de la loteria seleccionada
                        String seleccion = spinner_dev_used.getSelectedItem().toString();
                        if (seleccion.equals("Elija...")) {
                            //Do nothing.
                        } else {
                            seleccion = seleccion.replace("\n", "");
                            guardar(seleccion, "device.txt");
                            mensaje_device(seleccion);
                            //imprimir_archivo("device.txt");
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

    }


    /*
    private void imprimir_archivo(String nombre_archivo){
        String archivos[] = fileList();
        if (ArchivoExiste(archivos, nombre_archivo)) {//Archivo nombre_archivo es el archivo que vamos a imprimir
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(nombre_archivo));//Se abre archivo
                BufferedReader br = new BufferedReader(archivo);
                String contenido = "";//Aqui se lee el contenido del archivo guardado.

                String linea = br.readLine();//Se lee archivo
                while (linea != null) {
                    contenido = contenido + linea + "\n";
                    linea = br.readLine();
                    //return;
                }
                Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                br.close();
                archivo.close();
            }catch (IOException e) {
            }
        }
    }
     */


    private void mensaje_device(String seleccion) {
        Toast.makeText(this, "Se ha seleccionado el dispositivo " + seleccion, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed(){
        View view = null;
        boton_atras(view);
    }

    private boolean ArchivoExiste (String archivos [],String Tiquete){
        for (int i = 0; i < archivos.length; i++)
            if (Tiquete.equals(archivos[i]))
                return true;
        return false;
    }

    public void guardar(String Tcompleto, String nombre){
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre, Activity.MODE_PRIVATE));
            archivo.write(Tcompleto);
            archivo.flush();

        } catch (IOException e) {
        }
    }

    private void llenar_spinner_dev() {
        tv_dev_used.setText("Elija su dispositivo: ");
        String[] devices = new String[3];
        devices[0] = "Elija...";
        devices[1] = "Celular";
        devices[2] = "Maquina";
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, devices);
        spinner_dev_used.setAdapter(adapter);
    }


    public void boton_atras(View view) {
        Intent Main = new Intent(this, MainActivity.class);
        startActivity(Main);
        finish();
        System.exit(0);
    }

    public void agregar_loteria (View view) {
        Intent Agregar = new Intent(this, SelectconfigActivity.class);
        Agregar.putExtra("Accion", "Agregar");
        startActivity(Agregar);
        finish();
        System.exit(0);
    }

    public void editar_loteria (View view) {
        Intent Editar = new Intent(this, SelectconfigActivity.class);
        Editar.putExtra("Accion", "Editar");
        startActivity(Editar);
        finish();
        System.exit(0);
    }

    public void borrar_loteria (View view) {
        Intent Borrar = new Intent(this, SelectconfigActivity.class);
        Borrar.putExtra("Accion", "Borrar");
        startActivity(Borrar);
        finish();
        System.exit(0);
    }

}