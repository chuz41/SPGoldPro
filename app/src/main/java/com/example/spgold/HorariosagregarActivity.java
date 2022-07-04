package com.example.spgold;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HorariosagregarActivity extends AppCompatActivity implements View.OnClickListener {

    private String loteria;
    private String tipo_loteria;
    private String nombre_archivo;
    private String nuevo_archivo;
    Button bjuega,blista,button_agreg_hor,button_confirmar_agH;
    EditText hora_juego,hora_lista;
    TextView mensaje,tv1,tv2,tv3,tv4;
    private int horaL,minutosL,horaJ,minutosJ;
    Spinner select_ident;
    private String[] identifiers;
    private Map<String, String> ident_dic = new HashMap<String, String>();
    private String hoRa_L,miNuto_L,hoRa_J,miNuto_J;
    private boolean m_added = false,d_added = false,t_added = false,n_added = false;



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horariosagregar);

        tipo_loteria = getIntent().getStringExtra("Tipo_loteria");
        nuevo_archivo = getIntent().getStringExtra("Archivo");
        loteria = getIntent().getStringExtra("Nombre_loteria");
        bjuega = (Button) findViewById(R.id.button_juega);
        blista = (Button) findViewById(R.id.button_lista);
        button_agreg_hor = (Button) findViewById(R.id.button_agreg_hor);
        button_confirmar_agH = (Button) findViewById(R.id.button_confirmar_agH);
        hora_juego = (EditText) findViewById(R.id.Edit_text_hora_juega);
        hora_lista = (EditText) findViewById(R.id.Edit_text_hora_lista);
        bjuega.setOnClickListener(this);
        blista.setOnClickListener(this);
        select_ident = (Spinner) findViewById(R.id.spinner_select_ident);
        mensaje = (TextView) findViewById(R.id.textView_agregar_horarios);
        tv1 = (TextView) findViewById(R.id.textView_1_hor);
        tv2 = (TextView) findViewById(R.id.textView_2_hor);
        tv3 = (TextView) findViewById(R.id.textView_3_hor);
        tv4 = (TextView) findViewById(R.id.textView_4_hor);

        hora_juego.setFocusableInTouchMode(false);
        hora_lista.setFocusableInTouchMode(false);

        mensaje.setText("Configurar horarios para " + loteria + "...");



        //##################### Acondicionamiento del nombre de la loteria #########################
        //Aqui se acondiciona el nombre de la loteria para que no hallan errores al guardar los archivos
        String loteria_nombre = loteria;
        String lot_actual = "";
        String[] split_nom_parts_vertical = loteria_nombre.split("\n");
        int size_nom_parts_vert = split_nom_parts_vertical.length;
        for (int i = 0; i < size_nom_parts_vert; i++){
            if (split_nom_parts_vertical[i] == " "){
                //do nothing.
            }else if (split_nom_parts_vertical[i] == "\n"){
                //do nothing.
            }else if(size_nom_parts_vert == (i + 1)) {
                lot_actual = lot_actual + split_nom_parts_vertical[i];
                //Toast.makeText(this, "Parte del nombre: " + lot_actual + "\nCiclo for 1. ", Toast.LENGTH_LONG).show();
            }else {
                lot_actual = lot_actual + split_nom_parts_vertical[i] + "_";
            }
        }

        String[] split_nom_parts = lot_actual.split(" ");
        int size_nom_parts = split_nom_parts.length;
        lot_actual = "";
        for (int i = 0; i < size_nom_parts; i++){
            if (split_nom_parts[i] == " "){
                //do nothing.
            }else if (split_nom_parts[i] == "\n"){
                //do nothing.
            }else if (size_nom_parts == (i + 1)){
                lot_actual = lot_actual + split_nom_parts[i];
                //Toast.makeText(this, "Parte del nombre: " + lot_actual + "\nCiclo for 1. ", Toast.LENGTH_LONG).show();
            }else {
                lot_actual = lot_actual + split_nom_parts[i] + "_";
            }
        }
        loteria_nombre = lot_actual;
        //##########################################################################################

        //Aqui se crea el nombre del archivo que va a contener a la loteria que se va a agregar.
        nombre_archivo = "loteria_sfile" + loteria_nombre + "_sfile.txt";

        ident_dic.put("Maniana", "true");
        ident_dic.put("Tarde", "true");
        ident_dic.put("Dia", "true");
        ident_dic.put("Noche", "true");

        //################# Llamado a la funcion llenar_spinner() ##################################
        //Variables que se deben preparar para llamar a llenar_spinner()
        boolean maniana_flag,tarde_flag,dia_flag,noche_flag;
        if (ident_dic.get("Maniana").equals("true")) {
            maniana_flag = true;
        } else {maniana_flag = false;}
        if (ident_dic.get("Tarde").equals("true")) {
            tarde_flag = true;
        } else {tarde_flag = false;}
        if (ident_dic.get("Dia").equals("true")) {
            dia_flag = true;
        } else {dia_flag = false;}
        if (ident_dic.get("Noche").equals("true")) {
            noche_flag = true;
        } else {noche_flag = false;}
        //Se llama a la funcion llenar_spinner()
        llenar_spinner(maniana_flag, tarde_flag, dia_flag, noche_flag);
        //##########################################################################################

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void llenar_spinner(boolean maniana_flag, boolean tarde_flag, boolean dia_flag, boolean noche_flag) {


        int cont = 0;
        //String
        if (maniana_flag) {
            cont++;
            //ident_dic.replace("Maniana", "true");
        } else {
            //ident_dic.replace("Maniana", "false");
        }

        if (tarde_flag) {
            cont++;
            //ident_dic.replace("Tarde", "true");
        } else {
            //ident_dic.replace("Tarde", "false");
        }

        if (dia_flag) {
            cont++;
            //ident_dic.replace("Dia", "true");
        } else {
            //ident_dic.replace("Dia", "false");
        }

        if (noche_flag) {
            cont++;
            //ident_dic.replace("Noche", "true");
        } else {
            //ident_dic.replace("Noche", "false");
        }

        String flagM,flagD,flagT,flagN;
        flagM = ident_dic.get("Maniana");
        flagD = ident_dic.get("Dia");
        flagT = ident_dic.get("Tarde");
        flagN = ident_dic.get("Noche");
        identifiers = new String[cont];
        for (int i = 0; i < cont; i++) {
            if (flagM.equals("true")) {
                flagM = "false";
                identifiers[i] = "Maniana";
            } else if (flagT.equals("true")) {
                flagT =  "false";
                identifiers[i] = "Tarde";
            } else if (flagD.equals("true")) {
                flagD = "false";
                identifiers[i] = "Dia";
            } else if (flagN.equals("true")) {
                flagN = "false";
                identifiers[i] = "Noche";
            } else {
                //Do nothing. Nunca debe llegar aqui.
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, identifiers);
        select_ident.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View view) {

        if (view == bjuega) {

            final Calendar c = Calendar.getInstance();
            horaJ = c.get(Calendar.HOUR_OF_DAY);
            minutosJ = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    String h,m;
                    if (i == 0) {
                        h = "00";
                    }else if (i == 1) {
                        h = "01";
                    }else if (i == 2) {
                        h = "02";
                    }else if (i == 3) {
                        h = "03";
                    }else if (i == 4) {
                        h = "04";
                    }else if (i == 5) {
                        h = "05";
                    }else if (i == 6) {
                        h = "06";
                    }else if (i == 7) {
                        h = "07";
                    }else if (i == 8) {
                        h = "08";
                    }else if (i == 9) {
                        h = "09";
                    }else {
                        h = String.valueOf(i);
                    }
                    if (i1 == 0) {
                        m = "00";
                    }else if (i1 == 1) {
                        m = "01";
                    }else if (i1 == 2) {
                        m = "02";
                    }else if (i1 == 3) {
                        m = "03";
                    }else if (i1 == 4) {
                        m = "04";
                    }else if (i1 == 5) {
                        m = "05";
                    }else if (i1 == 6) {
                        m = "06";
                    }else if (i1 == 7) {
                        m = "07";
                    }else if (i1 == 8) {
                        m = "08";
                    }else if (i1 == 9) {
                        m = "09";
                    }else {
                        m = String.valueOf(i1);
                    }
                    hora_juego.setText(h + ":" + m);
                    hoRa_J = h;
                    miNuto_J = m;

                }
            },horaJ,minutosJ,false);
            timePickerDialog.show();

        }

        if (view == blista) {

            final Calendar c = Calendar.getInstance();
            horaL = c.get(Calendar.HOUR_OF_DAY);
            minutosL = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int i, int i1) {
                    String h,m;
                    if (i == 0) {
                        h = "00";
                    }else if (i == 1) {
                        h = "01";
                    }else if (i == 2) {
                        h = "02";
                    }else if (i == 3) {
                        h = "03";
                    }else if (i == 4) {
                        h = "04";
                    }else if (i == 5) {
                        h = "05";
                    }else if (i == 6) {
                        h = "06";
                    }else if (i == 7) {
                        h = "07";
                    }else if (i == 8) {
                        h = "08";
                    }else if (i == 9) {
                        h = "09";
                    }else {
                        h = String.valueOf(i);
                    }
                    if (i1 == 0) {
                        m = "00";
                    }else if (i1 == 1) {
                        m = "01";
                    }else if (i1 == 2) {
                        m = "02";
                    }else if (i1 == 3) {
                        m = "03";
                    }else if (i1 == 4) {
                        m = "04";
                    }else if (i1 == 5) {
                        m = "05";
                    }else if (i1 == 6) {
                        m = "06";
                    }else if (i1 == 7) {
                        m = "07";
                    }else if (i1 == 8) {
                        m = "08";
                    }else if (i1 == 9) {
                        m = "09";
                    }else {
                        m = String.valueOf(i1);
                    }
                    hora_lista.setText(h + ":" + m);
                    hoRa_L = h;
                    miNuto_L = m;
                }
            },horaL,minutosL,false);
            timePickerDialog.show();

        }

        /*if (view == button_agreg_hor) {//button_agreg_hor
            agregar_horarios();
        }

        if (view == button_confirmar_agH) {
            confirm_horarios();
        }*/

    }

    public void confirm_horarios(View view) {
        //Se confirma la creacion de la loteria.

        if (!m_added) {//Por si no se han agregado horarios se hace esto:
            if (!d_added) {
                if (!t_added) {
                    if (!n_added) {
                        Toast.makeText(this, "Debe agregar al menos 1 horario!!!", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
        }

        //Algoritmo que llene el archivo con los horarios no agregados en false.
        if (!m_added) {
            String linea = "Maniana  " + "false\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
            nuevo_archivo = nuevo_archivo + linea;
            linea = "Hora_juego_M  " + "00:00\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
            nuevo_archivo = nuevo_archivo + linea;
            linea = "Hora_lista_M  " + "0000\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
            nuevo_archivo = nuevo_archivo + linea;
        }

        if (!t_added) {
            String linea = "Tarde  " + "false\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
            nuevo_archivo = nuevo_archivo + linea;
            linea = "Hora_juego_T  " + "00:00\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
            nuevo_archivo = nuevo_archivo + linea;
            linea = "Hora_lista_T  " + "0000\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
            nuevo_archivo = nuevo_archivo + linea;
        }

        if (!d_added) {
            String linea = "Dia  " + "false\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
            nuevo_archivo = nuevo_archivo + linea;
            linea = "Hora_juego_D  " + "00:00\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
            nuevo_archivo = nuevo_archivo + linea;
            linea = "Hora_lista_D  " + "0000\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
            nuevo_archivo = nuevo_archivo + linea;
        }

        if (!n_added) {
            String linea = "Noche  " + "false\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
            nuevo_archivo = nuevo_archivo + linea;
            linea = "Hora_juego_N  " + "00:00\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
            nuevo_archivo = nuevo_archivo + linea;
            linea = "Hora_lista_N  " + "0000\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
            nuevo_archivo = nuevo_archivo + linea;
        }

        agregar_ultima_linea();//Agrega el tipo de loteria que se esta creando

        crear_archivo(nombre_archivo);
        guardar("", nombre_archivo);
        guardar(nuevo_archivo, nombre_archivo);
        imprimir_archivo(nombre_archivo);
        animacion_guardar();

    }

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

    private boolean ArchivoExiste (String[] archivos, String Tiquet){
        for (int i = 0; i < archivos.length; i++) {

            if (Tiquet.equals(archivos[i])) {
                return true;
            }
        }
        return false;
    }

    private void animacion_guardar() {
        Intent Guardando = new Intent(this, GuardandoActivity.class);
        String mensaje = "Loteria " + loteria + " ha sido creada!\n\nPresione atras para salir...";
        Guardando.putExtra("mensaje", mensaje);
        startActivity(Guardando);
        finish();
        System.exit(0);
    }

    /*Personalizacion de la navegacion hacia atras!!
    #################################################################################################*/
    @Override
    public void onBackPressed(){
        boton_atras();
    }

    private void boton_atras() {
        Intent Main = new Intent(this, MainActivity.class);
        startActivity(Main);
        finish();
        System.exit(0);
    }
    //#################################################################################################

    private void agregar_ultima_linea() {

        //Se agrega el tipo de loteria que se esta generando.
        String linea = "Tipo_juego  " + tipo_loteria; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
        nuevo_archivo = nuevo_archivo + linea;
    }

    private void guardar(String Tcompleto, String nombre){
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre, Activity.MODE_PRIVATE));
            archivo.write(Tcompleto);
            archivo.flush();

        } catch (IOException e) {
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void agregar_horarios(View view){


        //##########################################################################################
        //################## Manejo de errores #####################################################
        //##########################################################################################
        if (hora_juego.getText().toString().isEmpty()) {
            Toast.makeText(this, "Debe seleccionar la hora del juego", Toast.LENGTH_LONG).show();
            return;
        }
        if (hora_lista.getText().toString().isEmpty()) {
            Toast.makeText(this, "Debe seleccionar la hora limite para vender", Toast.LENGTH_LONG).show();
            return;
        }
        if (m_added){
            if (d_added) {
                if (t_added) {
                    if (n_added) {
                        Toast.makeText(this, "No puede agregar mas horarios!", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
        }
        //##########################################################################################
        //##########################################################################################
        //##########################################################################################

        String variable = select_ident.getSelectedItem().toString();
        if (variable.equals("Maniana")) {
            ident_dic.replace("Maniana", "false");
        } else if (variable.equals("Tarde")) {
            ident_dic.replace("Tarde", "false");
        } else if (variable.equals("Dia")) {
            ident_dic.replace("Dia", "false");
        } else if (variable.equals("Noche")) {
            ident_dic.replace("Noche", "false");
        } else {
            //Do nothing. Nunca deberia llegar aqui.
        }

        if (!m_added) {
            if (ident_dic.get("Maniana").equals("true")) {
                //Do nothing
            } else {
                String linea = "Maniana  " + "true\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
                nuevo_archivo = nuevo_archivo + linea;
                linea = "Hora_juego_M  " + hoRa_J + ":" + miNuto_J + "\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
                nuevo_archivo = nuevo_archivo + linea;
                linea = "Hora_lista_M  " + hoRa_L + miNuto_L + "\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
                nuevo_archivo = nuevo_archivo + linea;
                tv1.setText("Maniana\n" + "  " + hoRa_J + ":" + miNuto_J);
                hora_juego.setText("");
                hora_lista.setText("");
                //################# Llamado a la funcion llenar_spinner() ##################################
                //Variables que se deben preparar para llamar a llenar_spinner()
                boolean tarde_flag, dia_flag, noche_flag;
                if (ident_dic.get("Tarde").equals("true")) {
                    tarde_flag = true;
                } else {
                    tarde_flag = false;
                }
                if (ident_dic.get("Dia").equals("true")) {
                    dia_flag = true;
                } else {
                    dia_flag = false;
                }
                if (ident_dic.get("Noche").equals("true")) {
                    noche_flag = true;
                } else {
                    noche_flag = false;
                }
                //Se llama a la funcion llenar_spinner()
                llenar_spinner(false, tarde_flag, dia_flag, noche_flag);
                //##########################################################################################
                m_added = true;
            }
        }

        if (!t_added) {
            if (ident_dic.get("Tarde").equals("true")) {
                //Do nothing.
            } else {
                String linea = "Tarde  " + "true\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
                nuevo_archivo = nuevo_archivo + linea;
                linea = "Hora_juego_T  " + hoRa_J + ":" + miNuto_J + "\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
                nuevo_archivo = nuevo_archivo + linea;
                linea = "Hora_lista_T  " + hoRa_L + miNuto_L + "\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
                nuevo_archivo = nuevo_archivo + linea;
                tv2.setText("Tarde\n" + hoRa_J + ":" + miNuto_J);
                hora_juego.setText("");
                hora_lista.setText("");
                //################# Llamado a la funcion llenar_spinner() ##################################
                //Variables que se deben preparar para llamar a llenar_spinner()
                boolean maniana_flag, dia_flag, noche_flag;
                if (ident_dic.get("Maniana").equals("true")) {
                    maniana_flag = true;
                } else {
                    maniana_flag = false;
                }
                if (ident_dic.get("Dia").equals("true")) {
                    dia_flag = true;
                } else {
                    dia_flag = false;
                }
                if (ident_dic.get("Noche").equals("true")) {
                    noche_flag = true;
                } else {
                    noche_flag = false;
                }
                //Se llama a la funcion llenar_spinner()
                llenar_spinner(maniana_flag, false, dia_flag, noche_flag);
                //##########################################################################################
                t_added = true;
            }
        }

        if (!d_added) {
            if (ident_dic.get("Dia").equals("true")) {
                //Do nothing.
            } else {
                String linea = "Dia  " + "true\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
                nuevo_archivo = nuevo_archivo + linea;
                linea = "Hora_juego_D  " + hoRa_J + ":" + miNuto_J + "\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
                nuevo_archivo = nuevo_archivo + linea;
                linea = "Hora_lista_D  " + hoRa_L + miNuto_L + "\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
                nuevo_archivo = nuevo_archivo + linea;
                tv3.setText("  Dia\n" + hoRa_J + ":" + miNuto_J);
                hora_juego.setText("");
                hora_lista.setText("");
                //################# Llamado a la funcion llenar_spinner() ##################################
                //Variables que se deben preparar para llamar a llenar_spinner()
                boolean maniana_flag, tarde_flag, noche_flag;
                if (ident_dic.get("Maniana").equals("true")) {
                    maniana_flag = true;
                } else {
                    maniana_flag = false;
                }
                if (ident_dic.get("Tarde").equals("true")) {
                    tarde_flag = true;
                } else {
                    tarde_flag = false;
                }
                if (ident_dic.get("Noche").equals("true")) {
                    noche_flag = true;
                } else {
                    noche_flag = false;
                }
                //Se llama a la funcion llenar_spinner()
                llenar_spinner(maniana_flag, tarde_flag, false, noche_flag);
                //##########################################################################################
                d_added = true;
            }
        }

        if (!n_added) {
            if (ident_dic.get("Noche").equals("true")) {
                //Do nothing
                //Toast.makeText(this, "Noche true", Toast.LENGTH_LONG).show();
            } else {
                String linea = "Noche  " + "true\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
                nuevo_archivo = nuevo_archivo + linea;
                linea = "Hora_juego_N  " + hoRa_J + ":" + miNuto_J + "\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
                nuevo_archivo = nuevo_archivo + linea;
                linea = "Hora_lista_N  " + hoRa_L + miNuto_L + "\n"; //Representa la ultima linea del archivo, por eso no se le pone el cambio de linea (/n).
                nuevo_archivo = nuevo_archivo + linea;
                tv4.setText("Noche\n" + hoRa_J + ":" + miNuto_J);
                hora_juego.setText("");
                hora_lista.setText("");
                //################# Llamado a la funcion llenar_spinner() ##################################
                //Variables que se deben preparar para llamar a llenar_spinner()
                boolean maniana_flag, tarde_flag, dia_flag;
                if (ident_dic.get("Maniana").equals("true")) {
                    maniana_flag = true;
                } else {
                    maniana_flag = false;
                }
                if (ident_dic.get("Tarde").equals("true")) {
                    tarde_flag = true;
                } else {
                    tarde_flag = false;
                }
                if (ident_dic.get("Dia").equals("true")) {
                    dia_flag = true;
                } else {
                    dia_flag = false;
                }
                //Se llama a la funcion llenar_spinner()
                llenar_spinner(maniana_flag, tarde_flag, dia_flag, false);
                //##########################################################################################
                n_added = true;
            }
        }
    }

}