package com.example.spgold;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.example.spgold.Util.BluetoothUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReventadosActivity extends AppCompatActivity {

    private String mes;
    private String anio;
    private String dia;
    private String hora;
    private String minuto;
    private String nume1 = "not_asigned";
    private String nume2 = "not_asigned";
    private String nume3 = "not_asigned";
    private String fecha;
    private String contenido = "";
    private TextView lot;
    private TextView hor;
    private TextView loteria;
    private Spinner horario;
    private Spinner paga_rev;
    private Spinner bolita;
    private String[] loterias;//Informacion que aparecera en el spinner de loterias
    private String[] horarios;//Informacion que aparecera en el spinner de horarios
    private String[] archivos_lot;
    private String[] paga_str_array;
    private TextView titulo;
    private TextView paga_x_veces;
    private EditText num_premio;
    private int total = 0;
    private boolean flag_revent = false;
    private String Loteria;
    Map<String, String> loter = new HashMap<String, String>();
    private Map<String, Integer> meses = new HashMap<String, Integer>();
    private String dispositivo;
    private String bolaina = "BLANCA";
    private String readRowURL = "https://script.google.com/macros/s/AKfycbxJNCrEPYSw8CceTwPliCscUtggtQ2l_otieFmE/exec?spreadsheetId=1iMXw4z0ljwvfhdR5BBmh586h1AOmNCWll7GYI1MJFbM&sheet=hoy";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reventados);

        Loteria = getIntent().getStringExtra("Loteria");//Nombre con el que se guardo este reventados. Se recibe de la activity anterior.
        titulo = (TextView) findViewById(R.id.tv_winreventados);
        lot = (TextView) findViewById(R.id.tv_ingrese_lot_rev);
        hor = (TextView) findViewById(R.id.tv_ingrese_hor_rev);
        loteria = (TextView) findViewById(R.id.textView_lot_rev);// TextView que muesta lo que era el spinner de las loterias. ahora es otro textview.
        horario = (Spinner) findViewById(R.id.spinner_hor_reven);
        paga_rev = (Spinner) findViewById(R.id.spinner_paga_rev);
        num_premio = (EditText) findViewById(R.id.et_numwinn_rev);
        paga_x_veces = (TextView) findViewById(R.id.textView_paga_rev);
        bolita = (Spinner) findViewById(R.id.spinner_bolita);

        llenar_mapa_meses();

        dispositivo = check_device();



        //titulo.setText("Reporte de ventas");

        Date now = Calendar.getInstance().getTime();
        String ahora = now.toString();
        //se separan los campos de la fecha y hora para verificar que si se pueda realizar la venta.
        separar_fechaYhora(ahora);

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
        loterias[a] = "Elija una loteria...";
        for (int i = 0; i < archivos.length; i++) {
            Pattern pattern = Pattern.compile("loteria_sfile", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(archivos[i]);
            boolean matchFound = matcher.find();
            if (matchFound) {
                if (lotActiva(archivos[i])) {
                    a = a + 1;
                    String[] split = archivos[i].split("_sfile");
                    loterias[a] = split[1];
                    archivos_lot[a - 1] = archivos[i];
                }
            }
        }

        //String[] bolita_str_array = {"BLANCA", "ROJA", "GRIS"};
        String[] bolita_str_array = new String[3];
        bolita_str_array[0] = "BLANCA";
        bolita_str_array[1] = "ROJA";
        bolita_str_array[2] = "GRIS";
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, bolita_str_array);
        bolita.setAdapter(adapter3);

        loteria.setText(Loteria);
        crearDiccionario();//Meter aqui la loteria seleccionada en el spinner
        crear_array_horarios();
        llenar_spinner_hor();
        //llenar_spinner_paga();


        //Implementacion de un text listener que oculta el teclado cuando se han introducido 2 cifras
        num_premio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) {
                    ocultar_teclado();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        bolita.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        //Crear diccionario con la informacion de la loteria seleccionada
                        //String seleccion = loteria.getSelectedItem().toString();
                        String color_bolita = bolita.getSelectedItem().toString();
                        int pagar = 0;
                        //String num_str = num_premio.getText().toString();
                        if (color_bolita.equals("BLANCA")) {
                            //Colocar valor cero en el spinner paga
                            pagar = 0;
                        } else if (color_bolita.equals("ROJA")) {
                            //colocar el valor paga1 en el spinner paga
                            pagar = Integer.parseInt(loter.get("Paga1"));
                        } else if (color_bolita.equals("GRIS")) {
                            //Colocar el valor paga2 en el spinner paga
                            pagar = Integer.parseInt(loter.get("Paga2"));
                        } else {
                            //Do nothing. Nunca se va a llegar aqui.
                            error_msm();
                        }
                        llenar_spinner_paga(pagar);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

        /*paga.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        //Crear diccionario con la informacion de la loteria seleccionada
                        //String seleccion = loteria.getSelectedItem().toString();
                        String valor_paga = paga.getSelectedItem().toString();
                        //String num_str = num_premio.getText().toString();
                        if (Integer.parseInt(valor_paga) > 0) {
                            msn(valor_paga, 0);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });  */
    }

    private void llenar_mapa_meses() {

        meses.put("Jan",1);
        meses.put("Feb",2);
        meses.put("Mar",3);
        meses.put("Apr",4);
        meses.put("May",5);
        meses.put("Jun",6);
        meses.put("Jul",7);
        meses.put("Aug",8);
        meses.put("Sep",9);
        meses.put("Oct",10);
        meses.put("Nov",11);
        meses.put("Dic",12);
        meses.put("1",1);
        meses.put("2",2);
        meses.put("3",3);
        meses.put("4",4);
        meses.put("5",5);
        meses.put("6",6);
        meses.put("7",7);
        meses.put("8",8);
        meses.put("9",9);
        meses.put("10",10);
        meses.put("11",11);
        meses.put("12",12);

    }

    private void error_msm() {
        Toast.makeText(this, "Este mensaje nunca debe ser observado.\nSi lo ve llame a soporte tecnico.\nTelefono: 85258108", Toast.LENGTH_LONG).show();
    }

    private void ocultar_teclado(){
        View view = this.getCurrentFocus();
        InputMethodManager imn = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imn.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void msn1(){
        Toast.makeText(this, "Debe indicar la loteria que desea revisar!!!", Toast.LENGTH_LONG).show();
    }

    private void msn2(){
        Toast.makeText(this, "Debe indicar el horario que desea revisar!!!", Toast.LENGTH_LONG).show();
    }

    private void msn(String paga, int winnn_int) {
        //Toast.makeText(this, "Hola bebe!!!\n\nPaga: " + paga, Toast.LENGTH_LONG).show();
        //Toast.makeText(this, "Numero ganador: " + String.valueOf(winnn_int), Toast.LENGTH_LONG).show();
    }

    //Navegacion hacia atras personalizada
    @Override
    public void onBackPressed(){
        boton_atras();
    }


    private void boton_atras() {
        Intent Win = new Intent(this, MainActivity.class);
        startActivity(Win);
        finish();
        System.exit(0);
    }

    private int solicitar_num_premio(){
        //Se le solicita al vendedor introducir el numero ganador correspondiente a la loteria en cuestion

        String win_number = num_premio.getText().toString();
        if (win_number.isEmpty()){
            Toast.makeText(this, "Ingrese el numero ganador!!!", Toast.LENGTH_LONG).show();
            return 100;
        }else if(Integer.parseInt(win_number) < 0 || Integer.parseInt(win_number) > 99){
            Toast.makeText(this, "Debe ingresar un numero valido!!!", Toast.LENGTH_LONG).show();
            return 200;
        }else {
            int numbre_win = Integer.parseInt(win_number);
            return numbre_win;
        }
    }

    private void separar_fechaYhora(String ahora) {
        String[] split = ahora.split(" ");
        dia = split[2];
        mes = String.valueOf(meses.get(split[1]));
        anio = split[5];
        hora = split[3];
        fecha = split[2];
        split = hora.split(":");
        minuto = split[1];
        hora = split[0];
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

    private void generar_tiquete(String file, int Winner_num, String cliente, int pagA) {
        //Aqui se revisa si el cada tiquete tiene premio.
        //contenido = "";
        //String Loteria = loteria.getSelectedItem().toString();
        String Horario = horario.getSelectedItem().toString();

        //Revisar spinner que dice cual bolita salio

        try {
            InputStreamReader archivo24 = new InputStreamReader(openFileInput(file));//Se abre el archivo
            BufferedReader br24 = new BufferedReader(archivo24);
            String linea = br24.readLine();//Se lee archivo
            Pattern pattern = Pattern.compile("BORRADA", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(linea);//Se verifica si la factura se ha borrado
            String seleccion_premio = String.valueOf(pagA);
            //Toast.makeText(this, "Linea: " + linea + "\nSeleccion premio: " + seleccion_premio, Toast.LENGTH_LONG).show();
            boolean matchFound = matcher.find();
            if (matchFound){
                //Toast.makeText(this, "Se ha borrado esta factura!!!", Toast.LENGTH_LONG).show();
                return;
            }
            while (linea != null){
                //Toast.makeText(this, "La linea aun no es nula!!!", Toast.LENGTH_LONG).show();
                String[] split = linea.split("      ");//Se separa el monto del numero guardado.
                if (split[0].isEmpty()){
                    break;
                }else if (Integer.parseInt(split[0]) == Winner_num){
                    String cliente_print = cliente.replace("x_x"," ");
                    contenido = contenido + "\n\n################################\nPremio encontrado!!!\nCliente: " + cliente_print + "\nNumero ganador: " + String.valueOf(Winner_num) + "\n\n" + split[1] + " X " + seleccion_premio + " = " + String.valueOf(pagA * Integer.parseInt(split[1])) + " colones. \n################################\n\n";
                    total = total + (Integer.parseInt(seleccion_premio) * Integer.parseInt(split[1]));
                }
                linea = br24.readLine();
            }
            br24.close();
            archivo24.close();
            //printIt(contenido);
        }catch (IOException e) {
        }
    }

    private void msg(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
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
                //Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                //Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                //Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                br.close();
                archivo.close();
            }catch (IOException e) {
            }
        }
    }

    public void buscar_numero_ganador(String lot) {//Premios premios
        //Algoritmo que revisa la nube a ver si se subieron los numeros ganadores.

        RequestQueue requestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();

        String url = readRowURL;

        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String response) {
                        // Do something with the response
                        //ML_ver.setText(response);
                        //msg(response);

                        HashMap<String, String> premios = new HashMap<String, String>();
                        if (response != null) {
                            //response.replace("loteria", "_sepa_");
                            //msg(response);
                            String[] split = response.split("loteria");//Se separa el objeto Json

                            //Se llena un HashMap con los premios, los cuales se bajan de la nube.
                            for (int i = 1; i < split.length; i++) {

                                String[] split2 = split[i].split("\"");
                                String horarito = "";
                                if (split2[6].equals("noche")) {
                                    horarito = "Noche";
                                } else if (split2[6].equals("dia")) {
                                    horarito = "Dia";
                                } else if (split2[6].equals("tarde")) {
                                    horarito = "Tarde";
                                } else if (split2[6].equals("maniana")) {
                                    horarito = "Maniana";
                                } else {

                                }
                                //msg("Horario: " + split2[6]);
                                //                       Ej.                    Tica                             Noche
                                String loteria_actual = "ojo-rojo_ojo-rojo" + split2[2] + "ojo-rojo_ojo-rojo" + horarito + "ojo-rojo_ojo-rojo";
                                //                       Ej.                    03                                  no                                no                                 ID
                                String premio_actual = "ojo-rojo_ojo-rojo" + split2[10] + "ojo-rojo_ojo-rojo" + split2[14] + "ojo-rojo_ojo-rojo" + split2[18] + "ojo-rojo_ojo-rojo" + split2[26] + "ojo-rojo_ojo-rojo";
                                if (premios.containsKey(loteria_actual)) {
                                    String premio_viejo = premios.get(loteria_actual);
                                    String[] split3 = premio_viejo.split("ojo-rojo_ojo-rojo");
                                    if (Integer.parseInt(split3[4]) >= Integer.parseInt(split2[26])) {
                                        //Do nothing.
                                    } else {
                                        premios.replace(loteria_actual, premio_viejo, premio_actual);
                                    }
                                } else {
                                    premios.put(loteria_actual, premio_actual);
                                }
                            }

                            //iterar sobre el HashMap...
                            //String s = "Premios de hoy  " + fecha + "/" + mes + "/" + anio + "\n\n\n";
                            for (String key : premios.keySet()) {
                                //msg("key: " + key + "\n\npremios.key: " + premios.get(key));
                                //msg("key: " + key + "\n\nlot: " + lot);
                                if (key.equals(lot)) {

                                    msg("key: " + key + "\n\nlot: " + lot);
                                    String[] split4 = premios.get(key).split("ojo-rojo_ojo-rojo");
                                    String[] split5 = key.split("ojo-rojo_ojo-rojo");
                                    //String loteria_a_presentar = split5[1] + " " + split5[2];

                                    //Primer numero
                                    nume1 = split4[1];
                                    //Caso Monazos:
                                    if (split5[1].equals("Monazos")) {
                                        if (Integer.parseInt(nume1) < 10) {
                                            nume1 = "00" + String.valueOf(Integer.parseInt(nume1));
                                        } else if (Integer.parseInt(nume1) < 100) {
                                            nume1 = "0" + String.valueOf(Integer.parseInt(nume1));
                                        } else {
                                            //do nothing.
                                        }
                                    } else {//Caso otras loterias:
                                        if (Integer.parseInt(nume1) < 10) {
                                            nume1 = "0" + String.valueOf(Integer.parseInt(nume1));
                                        } else {
                                            //do nothing.
                                        }
                                    }

                                    //Segundo numero
                                    if (split4[2].equals("no")) {
                                        //Do nothing.
                                    } else {
                                        nume2 = split4[2];
                                        if (Integer.parseInt(nume2) < 10) {
                                            nume2 = "0" + String.valueOf(Integer.parseInt(nume2));
                                        } else {
                                            //Do nothing.
                                        }
                                    }

                                    //Tercer numero
                                    if (split4[3].equals("no")) {
                                        //Do nothing.
                                    } else if (split4[3].equals("ROJA") | split4[3].equals("GRIS") | split4[3].equals("BLANCA")) {
                                        flag_revent = true;
                                        bolaina = split4[3];
                                        nume3 = split4[3];
                                        msg("nume3: " + nume3);
                                        num_premio.setText(nume1);
                                        llenar_spinner_bola(nume3);
                                    } else {
                                        //nume3 = split4[3];
                                        if (Integer.parseInt(nume3) < 10) {
                                            nume3 = "0" + String.valueOf(Integer.parseInt(nume3));

                                        } else {
                                            //Do nothing.
                                        }
                                    }

                                }
                            }
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                    }
                });

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);
    }

    private void llenar_spinner_bola(String nu) {
        String[] paga_str = new String[1];
        String color = nu;
        paga_str[0] = color;
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, paga_str);
        bolita.setAdapter(adapter3);
    }

    public void tirar_reporte(View view){//Se deben de leer todos los tiquetes que se generaron dunrante el dia para cada loteria y cada horario elegidos
        String numero_gana = "not_yet";
        //String bolaina = "not_yet";

        if (horario.getSelectedItem().toString().equals("Escoja el horario...")) {
            Toast.makeText(this, "Debe seleccionar el horario que desea revisar", Toast.LENGTH_LONG).show();
            return;
        }else{
            //Do nothing. Continue con el ciclo!
        }

        if (num_premio.getText().toString().isEmpty()){
            String lot = "ojo-rojo_ojo-rojoReventadosojo-rojo_ojo-rojo" + horario.getSelectedItem().toString() + "ojo-rojo_ojo-rojo";
            if (nume1.equals("not_asigned") | nume3.equals("not_asigned")) {
                Toast.makeText(this, "Buscando numero ganador...", Toast.LENGTH_LONG).show();
                //return;
                buscar_numero_ganador(lot);
            } else {
                //numero_gana = nume1;
                //bolaina = nume3;
                //Do nothing more!
                //Continue con el ciclo.
            }

            /*if (nume1.equals("not_asigned") | nume3.equals("not_asigned")) {
                 Toast.makeText(this, "Debe indicar el numero ganador premiado para este sorteo", Toast.LENGTH_LONG).show();
                 return;
            } else {
                numero_gana = nume1;
                bolaina = nume3;
                //Do nothing more!
                //Continue con el ciclo. 
            }*/
        } else {

           String numero_premiado = "";
           String bola = "";
           int premio = 0;
           if (nume1.equals("not_asigned") | nume3.equals("not_asigned")){
                //Do nothing.
               msg("Debug: Nunca deberia imprimir esto!!! \n                       ERROR!!!");
               numero_premiado = nume1;
               bola = bolaina;
           } else {
               numero_premiado = nume1;
               bola = bolaina;
               if (bola.equals("ROJA")) {
                   premio = Integer.parseInt(loter.get("Paga1"));
               } else if (bola.equals("GRIS")) {
                   premio = Integer.parseInt(loter.get("Paga2"));
               } else if (bola.equals("BLANCA")) {
                   premio = 0;
               } else {
                   //Do nothing. Nunca debe llegar aqui!
               }
           }
            //borrar_archivo("premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt");
            //File file = new File("premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt");
            //agregar_linea_archivo("premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt", "");
            //file.delete();
            anular("premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt");
            borrar_archivo("premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt");
            agregar_linea_archivo("premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt", numero_premiado + "  " + String.valueOf(premio));
            //String Loteria = loteria.getSelectedItem().toString();
            String Horario = horario.getSelectedItem().toString();
            String Paga = String.valueOf(premio);
            int pagA = premio;
            String Winner_number = numero_premiado;
            int Number_winner = Integer.parseInt(Winner_number);
            String archivos[] = fileList();
            contenido = "";
            contenido = contenido + "\n    Reporte de ganadores\n\n    ----> " + Loteria + " " + Horario + " <----\n\nFecha: " + fecha + "/" + mes + "/" + anio + "\n\n";
            for (int i = 0; i < archivos.length; i++) {
                Pattern pattern = Pattern.compile(Loteria + "_separador_" + Horario + "_separador_" + fecha + "_separador_", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(archivos[i]);
                boolean matchFound = matcher.find();
                String[] split_nombre = archivos[i].split("_separador_");
                String jugador_actual = "";
                if (matchFound) {
                    //Se encuentra un archivo!!!
                    String[] split_nom_parts = split_nombre[0].split("x_x");
                    int size_nom_parts = split_nom_parts.length;
                    for (int ii = 0; ii < size_nom_parts; ii++) {
                        jugador_actual = jugador_actual + split_nom_parts[ii] + " ";
                    }
                    String archivo = archivos[i];
                    generar_tiquete(archivo, Number_winner, jugador_actual, pagA);
                }
            }
            contenido = contenido + "\n\n################################\n Total de premios en \n" + Loteria + " " + Horario + ": " + String.valueOf(total) + " colones.\n################################\n\n\n\n\n";
            printIt(contenido);
            total = 0;
        }
    }

    public void guardar (String Tcompleto, String nombre){
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre, Activity.MODE_PRIVATE));
            archivo.write(Tcompleto);
            archivo.flush();

        } catch (IOException e) {
        }
    }

    public void anular (String nombre){
        try {
            //Testing the function before.
            //imprimir_archivo(nombre);
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre, Activity.MODE_PRIVATE));
            archivo.write("");
            archivo.flush();
            //testing the function after.
            //imprimir_archivo(nombre);
        } catch (IOException e) {
        }
    }

    private void guardar_pdf(){
        //Respaldo digital del reporte contable.
    }

    private void borrar_archivo(String s){
        //Se borra el archivo

        //Testing the function before.
        //imprimir_archivo(s);
        File file = new File(s);
        file.delete();
        //testing the function after.
        //imprimir_archivo(s);
    }


    private String check_device() {
        String archivos[] = fileList();
        dispositivo = null;
        if (ArchivoExiste(archivos, "device.txt")) {
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput("device.txt"));
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();

                if (linea != null) {
                    //Se lee el dispositivo guardado
                    dispositivo = linea;
                    dispositivo = dispositivo.replace("\n", "");
                } else {
                    //Error!!!
                }
                br.close();
                archivo.close();
            } catch (IOException e) {
            }
        }
        return dispositivo;
    }


    public void printIt(String Mensaje) {

        if (dispositivo.equals("Celular")) {

            Intent Activity_ver = new Intent(this, VerActivity.class);
            Activity_ver.putExtra("mensaje", Mensaje);
            //startActivity(Activity_ver);
            Intent rev_act = new Intent(this, ReventadosActivity.class);
            rev_act.putExtra("Loteria", Loteria);
            startActivity(rev_act);
            finish();
            startActivity(Activity_ver);
            System.exit(0);

        } else if (dispositivo.equals("Maquina")) {
            BluetoothSocket socket;
            socket = null;
            byte[] data = Mensaje.getBytes();

            //Get BluetoothAdapter
            BluetoothAdapter btAdapter = BluetoothUtil.getBTAdapter();
            if (btAdapter == null) {
                Toast.makeText(getBaseContext(), "Open Bluetooth", Toast.LENGTH_SHORT).show();
                return;
            }
            // Get sunmi InnerPrinter BluetoothDevice
            BluetoothDevice device = BluetoothUtil.getDevice(btAdapter);
            if (device == null) {
                Toast.makeText(getBaseContext(), "Make Sure Bluetooth have InnterPrinter", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                socket = BluetoothUtil.getSocket(device);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                assert socket != null;
                BluetoothUtil.sendData(data, socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Error en tipo de dispositivo", Toast.LENGTH_LONG).show();
        }
    }

    private void llenar_spinner_hor() {
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, horarios);
        horario.setAdapter(adapter2);
    }

    private void llenar_spinner_paga(int pagar) {

        paga_str_array = new String[1];
        String color = String.valueOf(pagar);
        paga_str_array[0] = color;
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, paga_str_array);
        paga_rev.setAdapter(adapter3);
    }

    private void crear_array_horarios(){
        //String archivos[] = fileList();
        int a = 0;
        for (String i : loter.keySet()) {
            if (loter.get(i).equals("true")){
                a++;
            }
        }
        horarios = new String[a+1];
        a = 0;
        horarios[0] = "Escoja el horario...";
        for (String i : loter.keySet()) {

            if (loter.get(i).equals("true")){
                horarios[a+1] = i;
                a++;
            }
        }
    }

    private void crearDiccionario() {

        String archivos[] = fileList();
        //String ArchivoCompleto = "";//Aqui se lee el contenido del archivo guardado.
        String selec_lot = Loteria;
        String archivo_leido = "";
        String matcheo = selec_lot + "_sfile";
        for (int i = 0; i < archivos.length; i++) {
            Pattern pattern = Pattern.compile(matcheo, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(archivos[i]);
            boolean matchFound = matcher.find();
            if (matchFound) {
                archivo_leido = archivos[i];
            }
        }

        if (ArchivoExiste(archivos, archivo_leido)) {
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(archivo_leido));
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();

                while (linea != null) {
                    String[] split = linea.split("  ");
                    loter.put(split[0], split[1]);
                    //ArchivoCompleto = ArchivoCompleto + linea + "\n";
                    linea = br.readLine();
                }
                //ArchivoCompleto = ArchivoCompleto + new_line + "\n";
                br.close();
                archivo.close();
            } catch (IOException e) {
            }
        }
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

    private void crear_archivo(String nombre_archivo) {
        try{
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre_archivo, Activity.MODE_PRIVATE));
            archivo.flush();
            archivo.close();
        }catch (IOException e) {
        }
    }

    private boolean ArchivoExiste (String archivos [],String Tiquete){
        for (int i = 0; i < archivos.length; i++)
            if (Tiquete.equals(archivos[i]))
                return true;
        return false;
    }
}
