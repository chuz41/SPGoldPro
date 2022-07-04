package com.example.spgold;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectconfigActivity extends AppCompatActivity {

    private String accion;
    private Spinner loteria;
    private String[] loterias;
    private String[] archivos_lot;
    private TextView mensaje_bienvenida;
    private String flag_lot = "";
    private Button accion_bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectconfig);

        accion = getIntent().getStringExtra("Accion");

        loteria = (Spinner) findViewById(R.id.spinner_select);
        mensaje_bienvenida = (TextView) findViewById(R.id.textView_select);
        accion_bt = (Button) findViewById(R.id.button_accion);

        accion_bt.setText(accion);

        if (accion.equals("Agregar")) {
            mensaje_bienvenida.setText("Seleccione el tipo de loteria que desea " + accion);
        } else {
            mensaje_bienvenida.setText("Seleccione la loteria que desea " + accion);
        }

        //Crear diccionarios de loterias configuradas:
        String archivos[] = fileList();
        int a = 0;
        for (int i = 0; i < archivos.length; i++) {
            Pattern pattern = Pattern.compile("loteria_sfile", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(archivos[i]);
            boolean matchFound = matcher.find();
            if (matchFound) {
                if (lotActiva(archivos[i])) {
                    a = a + 1;
                }
            }
        }
        loterias = new String[a+1];
        archivos_lot = new String[a];
        a = 0;
        loterias[a] = "Elija un sorteo...";
        for (int i = 0; i < archivos.length; i++) {
            Pattern pattern = Pattern.compile("loteria_sfile", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(archivos[i]);
            boolean matchFound = matcher.find();
            if (matchFound) {
                if (lotActiva(archivos[i])) {
                    //Toast.makeText(this, "Loteria encontrada: " + archivos[i], Toast.LENGTH_LONG).show();
                    a = a + 1;
                    String[] split = archivos[i].split("_sfile");
                    loterias[a] = split[1];
                    archivos_lot[a - 1] = archivos[i];
                }
            }
        }

        if (accion.equals("Agregar")) {
            String[] loteris = new String[4];
            loteris[0] = "Regular";
            loteris[1] = "Reventados";
            loteris[2] = "Parley";
            loteris[3] = "Monazos";
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, loteris);
            loteria.setAdapter(adapter);

        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, loterias);
            loteria.setAdapter(adapter);
        }


        loteria.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        //Crear diccionario con la informacion de la loteria seleccionada
                        String seleccion = loteria.getSelectedItem().toString();
                        if (seleccion.equals("Elija un sorteo...")) {
                            //Do nothing.
                            flag_lot = "no_lotery";
                        } else {
                            //Do nothing too.
                            flag_lot = seleccion;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

    }

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

    private boolean lotActiva(String archivo) {
        try {
            InputStreamReader Archivo = new InputStreamReader(openFileInput(archivo));//Se abre archivo
            BufferedReader br = new BufferedReader(Archivo);
            String linea = br.readLine();//Se lee la primera linea del archivo
            //Toast.makeText(this, "Linea: " + linea, Toast.LENGTH_LONG).show();
            Pattern pattern = Pattern.compile("BORRADA", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(linea);//Se verifica si la loteria se ha borrado
            boolean matchFound = matcher.find();
            if (matchFound) {
                br.close();
                Archivo.close();
                return false;
            }
            br.close();
            Archivo.close();
        }catch (IOException e) {
        }
        return true;
    }

    public void boton_accion (View view) {
        if (accion.equals("Agregar")) {
            agregar();
        } else if (accion.equals("Editar")) {
            editar();
        } else if (accion.equals("Borrar")) {
            borrar();
        }
    }

    private void agregar () {

        Intent Agregar = new Intent(this, AgregarActivity.class);
        Agregar.putExtra("Loteria", loteria.getSelectedItem().toString());
        startActivity(Agregar);
        finish();
        System.exit(0);

    }

    private void editar () {

        Intent Editar = new Intent(this, EditarActivity.class);
        Editar.putExtra("Loteria", loteria.getSelectedItem().toString());
        startActivity(Editar);
        finish();
        System.exit(0);

    }

    private void borrar () {

        Intent Borrar = new Intent(this, BorrarActivity.class);
        Borrar.putExtra("Loteria", loteria.getSelectedItem().toString());
        startActivity(Borrar);
        finish();
        System.exit(0);

    }

}