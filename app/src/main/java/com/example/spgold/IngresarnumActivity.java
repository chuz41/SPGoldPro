package com.example.spgold;

/*
https://script.google.com/macros/s/AKfycbxJNCrEPYSw8CceTwPliCscUtggtQ2l_otieFmE/exec?spreadsheetId=1iMXw4z0ljwvfhdR5BBmh586h1AOmNCWll7GYI1MJFbM&sheet=hoy
 */

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.spgold.Util.TranslateUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IngresarnumActivity extends AppCompatActivity {

    private Spinner loteria;
    private Spinner horario;
    private Spinner num_premio1;
    private Spinner num_premio2;
    private Spinner num_premio3;
    private String[] loterias;//Informacion que aparecera en el spinner de loterias
    private String[] horarios;//Informacion que aparecera en el spinner de horarios
    private String[] archivos_lot;
    private Button boton_subir;
    private Button boton_ver;
    private int cont_reventados = 0;
    private String SHEET = "hoy";
    private String SPREADSHEET_ID = "1iMXw4z0ljwvfhdR5BBmh586h1AOmNCWll7GYI1MJFbM";
    private String addRowURL = "https://script.google.com/macros/s/AKfycbweyYb-DHVgyEdCWpKoTmvOxDGXleawjAN8Uw9AeJYbZ24t9arB/exec";
    private String readRowURL = "https://script.google.com/macros/s/AKfycbxJNCrEPYSw8CceTwPliCscUtggtQ2l_otieFmE/exec?spreadsheetId=1iMXw4z0ljwvfhdR5BBmh586h1AOmNCWll7GYI1MJFbM&sheet=hoy";
    private String mes;
    private String dia;
    private String anio;
    private String hora;
    private String minuto;
    private String segundo;
    private String fecha;
    private Map<String, String> loter = new HashMap<String, String>();
    private Map<String, Integer> meses = new HashMap<String, Integer>();
    private TextView tv_listo;
    private EditText ML_ver;
    private TextView lot_tv;
    private TextView hor_tv;
    private TextView tv_admin;
    private int contador_ver = 0;
    private int contador_toast = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingresarnum);

        loteria = (Spinner) findViewById(R.id.spinner_lot_admin);
        horario = (Spinner) findViewById(R.id.spinner_hor_admin);
        num_premio1 = (Spinner) findViewById(R.id.et_numwin1);
        num_premio2 = (Spinner) findViewById(R.id.et_numwin2);
        num_premio3 = (Spinner) findViewById(R.id.et_numwin3);
        boton_subir = (Button) findViewById(R.id.button_agregar_numero);
        boton_ver = (Button) findViewById(R.id.button_ver_resultados);
        tv_listo = (TextView) findViewById(R.id.textView_listo);
        ML_ver = (EditText) findViewById(R.id.editTextTextMultiLine_ver);
        lot_tv = (TextView) findViewById(R.id.tv_loteria_admin);
        hor_tv = (TextView) findViewById(R.id.tv_horario_admin);
        tv_admin = (TextView) findViewById(R.id.tv_admin);
        tv_listo.setVisibility(View.GONE);
        ML_ver.setVisibility(View.GONE);
        boton_subir.setVisibility(View.GONE);
        aparecer_spinners_limpios();
        limpiar_spinner_horario();
        loter.put("Tipo_juego", "ninguno");

        gene_fecha_hoy();

        //crear diccionarios de loterias configuradas
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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, loterias);
        loteria.setAdapter(adapter);

        //generar_ID();

        horario.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (horario.getSelectedItem().toString().equals("Escoja el horario...")) {
                            //msg("Escoja el horario...");
                        } else {
                            //Do the algorithm
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

        loteria.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        aparecer_spinners_limpios();
                        contador_ver++;
                        if (contador_ver < 2) {
                            //Do nothing!
                        } else {
                            boton_ver.setVisibility(View.GONE);
                        }

                        //Crear diccionario con la informacion de la loteria seleccionada
                        String seleccion = loteria.getSelectedItem().toString();

                        if (seleccion.equals("Elija una loteria...")) {
                            //Do nothing!
                        }else {
                            crearDiccionario();//Meter aqui la loteria seleccionada en el spinner

                            //Condicionales
                            if (loter.get("Tipo_juego").equals("Monazos")) {

                                msg("Escoja el horario...");

                            } else if (loter.get("Tipo_juego").equals("Parley")) {

                                msg("Escoja el horario...");

                            } else if (loter.get("Tipo_juego").equals("Reventados")) {

                                msg("Escoja el horario...");

                            } else if (loter.get("Tipo_juego").equals("Regular")) {

                                msg("Escoja el horario...");

                            } else {
                                //error_msm();
                            }
                            //Fin condicionales

                            //Agregar horarios al otro Spinner
                            llenar_spinner_hor();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

        horario.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        aparecer_spinners_limpios();
                        //Crear diccionario con la informacion de la loteria seleccionada
                        String seleccion = loteria.getSelectedItem().toString();
                        contador_toast++;

                        if (seleccion.equals("Elija una loteria...")) {
                            //Do nothing!
                        }else {

                            //Do the algorithm!
                            if (loter.get("Tipo_juego").equals("Regular")) {

                                if (contador_toast > 2) {
                                    msg("Escoja el numero ganador...");
                                }

                                llenar_spinner_winer("regular");
                                desaparecer_ambos_spinners();

                            } else if(loter.get("Tipo_juego").equals("Monazos")) {

                                if (contador_toast > 2) {
                                    msg("Escoja el numero ganador...");
                                }
                                llenar_spinner_winer("monazos");
                                desaparecer_ambos_spinners();

                            } else if (loter.get("Tipo_juego").equals("Reventados")) {

                                if (contador_toast > 2) {
                                    msg("Escoja el numero ganador y el color de la bolita...");
                                }
                                llenar_spinner_winer("reventados");
                                desaparecer_segundo_spinner();
                                llenar_spinner_bolita();

                            } else if (loter.get("Tipo_juego").equals("Parley")) {

                                if (contador_toast > 2) {
                                    msg("Ingrese los 3 numeros ganadores de Tica " + horario.getSelectedItem().toString());
                                }

                                llenar_spinners_parley();

                            } else {

                                //Do nothing. Funciona en caso de un nuevo juego.

                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });


        num_premio1.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        String seleccion = loteria.getSelectedItem().toString();
                        String seleccion_num1 = num_premio1.getSelectedItem().toString();
                        String seleccion_hor = horario.getSelectedItem().toString();

                        if (seleccion.equals("Elija una loteria...") | seleccion_hor.equals("Escoja el horario...")) {
                            //Do nothing!
                        }else {

                            //Do the algorithm!
                            if (loter.get("Tipo_juego").equals("Regular")) {

                                //Aparecer el boton subir
                                if (seleccion_num1.equals("Num")){
                                    //Do nothing!
                                } else {
                                    aparecer_boton_subir();
                                }

                            } else if(loter.get("Tipo_juego").equals("Monazos")) {

                                //Aparecer el boton subir
                                if (seleccion_num1.equals("Num")){
                                    //Do nothing!
                                } else {
                                    aparecer_boton_subir();
                                }

                            } else if (loter.get("Tipo_juego").equals("Reventados")) {

                                //Aparecer el boton subir
                                if (seleccion_num1.equals("Num")){
                                    //Do nothing!
                                } else {
                                    String bolita = num_premio3.getSelectedItem().toString();
                                    if (bolita.equals("BLANCA")) {
                                        //Do nothing!
                                    } else {
                                        aparecer_boton_subir();
                                    }
                                }

                            } else if (loter.get("Tipo_juego").equals("Parley")) {

                                String seleccion_num2 = num_premio2.getSelectedItem().toString();
                                String seleccion_num3 = num_premio3.getSelectedItem().toString();
                                //Aparecer el boton subir
                                if (seleccion_num2.equals("2-") | seleccion_num3.equals("3-") | seleccion_num1.equals("1-")){
                                    //Do nothing!
                                } else {
                                    aparecer_boton_subir();
                                }

                            } else {

                                //Do nothing. Funciona en caso de un nuevo juego.

                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

        num_premio2.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        String seleccion = loteria.getSelectedItem().toString();
                        String seleccion_num2 = num_premio2.getSelectedItem().toString();
                        String seleccion_hor = horario.getSelectedItem().toString();

                        if (seleccion.equals("Elija una loteria...") | seleccion_hor.equals("Escoja el horario...")) {
                            //Do nothing!
                        }else {

                            //Do the algorithm!
                            if (loter.get("Tipo_juego").equals("Parley")) {

                                String seleccion_num1 = num_premio1.getSelectedItem().toString();
                                String seleccion_num3 = num_premio3.getSelectedItem().toString();
                                //Aparecer el boton subir
                                if (seleccion_num2.equals("2-") | seleccion_num3.equals("3-") | seleccion_num1.equals("1-")){
                                    //Do nothing!
                                } else {
                                    aparecer_boton_subir();
                                }

                            } else {

                                //Do nothing. Funciona en caso de un nuevo juego.

                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

        num_premio3.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        //cont_reventados = 0;
                        String seleccion = loteria.getSelectedItem().toString();
                        String seleccion_num3 = num_premio3.getSelectedItem().toString();
                        String seleccion_hor = horario.getSelectedItem().toString();
                        String seleccion_num1 = num_premio1.getSelectedItem().toString();

                        if (seleccion.equals("Elija una loteria...") | seleccion_hor.equals("Escoja el horario...")) {
                            //Do nothing!
                        }else {

                            //Do the algorithm!
                            if (loter.get("Tipo_juego").equals("Parley")) {

                                String seleccion_num2 = num_premio2.getSelectedItem().toString();
                                //Aparecer el boton subir
                                if (seleccion_num2.equals("2-") | seleccion_num3.equals("3-") | seleccion_num1.equals("1-")){
                                    //Do nothing!
                                } else {
                                    aparecer_boton_subir();
                                }

                            } else if (loter.get("Tipo_juego").equals("Reventados")) {
                                cont_reventados++;
                                if (seleccion_num1.equals("Num")) {
                                    //Do nothing!
                                } else {
                                    if (cont_reventados > 1) {
                                        aparecer_boton_subir();
                                    } else {
                                        //Do nothing!
                                    }
                                }

                            } else {

                                //Do nothing. Funciona en caso de un nuevo juego.

                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

    }

    private int generar_ID() {
        llenar_mapa_meses();
        Date now = Calendar.getInstance().getTime();
        //msg("Time: " + now);
        String ahora = now.toString();
        //se separan los campos de la fecha y hora para verificar que si se pueda realizar la venta.
        separar_fechaYhora(ahora);
        String id = mes + fecha + hora + minuto + segundo;
        int ID = Integer.parseInt(id);
        return ID;
    }

    private void gene_fecha_hoy() {
        llenar_mapa_meses();
        Date now = Calendar.getInstance().getTime();
        //msg("Time: " + now);
        String ahora = now.toString();
        //se separan los campos de la fecha y hora para verificar que si se pueda realizar la venta.
        separar_fechaYhora(ahora);
    }

    private void separar_fechaYhora(String ahora) {
        String[] split = ahora.split(" ");

        mes = String.valueOf(meses.get(split[1]));
        anio = split[5];
        fecha = split[2];
        dia = split[2];
        hora = split[3];
        split = hora.split(":");
        minuto = split[1];
        hora = split[0];
        segundo = split[2];
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

    public void ver_resultados(View view) throws JSONException {

        boton_subir.setVisibility(View.GONE);
        loteria.setVisibility(View.GONE);
        horario.setVisibility(View.GONE);
        num_premio1.setVisibility(View.GONE);
        num_premio2.setVisibility(View.GONE);
        num_premio3.setVisibility(View.GONE);
        tv_listo.setVisibility(View.GONE);
        boton_ver.setVisibility(View.GONE);
        lot_tv.setVisibility(View.GONE);
        hor_tv.setVisibility(View.GONE);
        tv_admin.setVisibility(View.GONE);
        ML_ver.setVisibility(View.VISIBLE);
        ML_ver.setFocusableInTouchMode(false);

        //Algoritmo para ver los resultados de hoy:

        if (verificar_internet()) {
            JsonObjectRequest();
        } else {
            msg("Debe estar conectado a una red de Internet!!!");
            return;
        }
    }

    private void JsonObjectRequest() {

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
                                //                       Ej.    Tica              Noche
                                String loteria_actual = "_" + split2[2] + "_" + split2[6] + "_";
                                //                       Ej.   03                 no                 no                 ID
                                String premio_actual = "_" + split2[10] + "_" + split2[14] + "_" + split2[18] + "_" + split2[26] + "_";
                                if (premios.containsKey(loteria_actual)) {
                                    String premio_viejo = premios.get(loteria_actual);
                                    String[] split3 = premio_viejo.split("_");
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
                            String s = "Premios de hoy  " + fecha + "/" + mes + "/" + anio + "\n\n\n";
                            for (String key : premios.keySet()) {

                                String[] split4 = premios.get(key).split("_");
                                String[] split5 = key.split("_");
                                String loteria_a_presentar = split5[1] + " " + split5[2];

                                //Primer numero
                                String nume1 = split4[1];
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
                                s = s + loteria_a_presentar + ": " + nume1;

                                //Segundo numero
                                if (split4[2].equals("no")) {
                                    //Do nothing.
                                } else {
                                    String nume2 = split4[2];
                                    if (Integer.parseInt(nume2) < 10) {
                                        nume2 = "0" + String.valueOf(Integer.parseInt(nume2));
                                    } else {
                                        //Do nothing.
                                    }
                                    s = s + " " + nume2;
                                }
                                s = s + " ";

                                //Tercer numero
                                if (split4[3].equals("no")) {
                                    //Do nothing.
                                } else if (split4[3].equals("ROJA") | split4[3].equals("GRIS") | split4[3].equals("BLANCA")){
                                    s = s + split4[3];
                                } else {
                                    String nume3 = split4[3];
                                    if (Integer.parseInt(nume3) < 10) {
                                        nume3 = "0" + String.valueOf(Integer.parseInt(nume3));
                                    } else {
                                        //Do nothing.
                                    }
                                    s = s + nume3;
                                }
                                s = s + "\n\n";
                            }
                            ML_ver.setText(s);
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

    public void subir_numero(View view) throws JSONException {

        //Condicionales
        if (loter.get("Tipo_juego").equals("Monazos") | loter.get("Tipo_juego").equals("Reventados") | loter.get("Tipo_juego").equals("Regular")) {

            if (loteria.getSelectedItem().toString().equals("Elija una loteria...") | horario.getSelectedItem().toString().equals("Escoja el horario...") | num_premio1.getSelectedItem().toString().equals("Num")) {
                msg("Debe completar la informacion solicitada!!!");
                return;
            } else {
                go_online();
            }

        } else if (loter.get("Tipo_juego").equals("Parley")) {

            if (loteria.getSelectedItem().toString().equals("Elija una loteria...") | horario.getSelectedItem().toString().equals("Escoja el horario...") | num_premio1.getSelectedItem().toString().equals("1-") | num_premio2.getSelectedItem().toString().equals("2-") | num_premio3.getSelectedItem().toString().equals("3-")) {
                msg("Debe completar la informacion solicitada!!!");
                return;
            } else {
                go_online();
            }

        } else {
            //Sirve para nuevos juegos!!
        }
        //Fin condicionales

    }

    private void go_online() throws JSONException {
        //Aqui va la magia online!!!

        if (verificar_internet()) {

            int ID = generar_ID();
            String id = String.valueOf(ID);
            String numero2 = "";
            String numero3 = "";
            if (loter.get("Tipo_juego").equals("Parley")) {
                numero2 = num_premio2.getSelectedItem().toString();
                numero3 = num_premio3.getSelectedItem().toString();
            } else if (loter.get("Tipo_juego").equals("Reventados")) {
                numero2 = "no";
                numero3 = num_premio3.getSelectedItem().toString();
            } else {
                numero2 = "no";
                numero3 = "no";
            }

            String horario_lowercase = "";
            if (horario.getSelectedItem().toString().equals("Dia")) {
                horario_lowercase = "dia";
            } else if (horario.getSelectedItem().toString().equals("Tarde")) {
                horario_lowercase = "tarde";
            } else if (horario.getSelectedItem().toString().equals("Noche")) {
                horario_lowercase = "noche";
            } else if (horario.getSelectedItem().toString().equals("Maniana")) {
                horario_lowercase = "maniana";
            }  else {
                //Do nothing. Nunca deberia llegar aqui.
            }
            JSONObject jsonObject = TranslateUtil.premiados_to_Json_subir(num_premio1.getSelectedItem().toString(), numero2, numero3, numero3, SPREADSHEET_ID, SHEET, id, loteria.getSelectedItem().toString(), horario_lowercase);
            subir_ganador(jsonObject);

        } else {
            return;
        }

    }

    /*
    private String uncapitalize(String s) {
        if (s!=null && s.length() > 0) {
            return s.substring(0, 1).toLowerCase() + s.substring(1);
        } else {
            return s;
        }
    }
     */

    private void subir_ganador(JSONObject jsonObject) throws JSONException {

        RequestQueue queue;
        queue = Volley.newRequestQueue(this);

        //Llamada POST usando Volley:
        RequestQueue requestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();

        String url = addRowURL;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String[] split = response.toString().split("\"");
                        int length_split = split.length;
                        if (length_split > 3) {
                            if (split[3].equals(SHEET)) {
                                msg("Resultado se ha subido correctamente!");
                                boton_subir.setVisibility(View.GONE);
                                loteria.setVisibility(View.GONE);
                                horario.setVisibility(View.GONE);
                                num_premio1.setVisibility(View.GONE);
                                num_premio2.setVisibility(View.GONE);
                                num_premio3.setVisibility(View.GONE);
                                tv_listo.setVisibility(View.VISIBLE);
                                boton_ver.setVisibility(View.VISIBLE);
                                //cambiar_bandera (Consecutivo);
                            } else {
                                //Do nothing!
                            }
                        } else {
                            //No se subio correctamente!
                            msg("ERROR!!! Resultado no se ha subido!");
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        msg("ERROR al tratar de subir el resultado!!!");

                    }
                });

        // Add the request to the RequestQueue.
        requestQueue.add(jsonObjectRequest);
    }

    private boolean verificar_internet() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            Toast.makeText(this, "Debe estar conectado a una red WiFi o datos mobiles.", Toast.LENGTH_LONG).show();
            return false;
        } else {
            //Si esta conectado a internet.
            Toast.makeText(this, "Conectado a internet!", Toast.LENGTH_LONG).show();
            return true;
        }
    }

    private void aparecer_boton_subir() {
        boton_subir.setVisibility(View.VISIBLE);
    }

    private void llenar_spinners_parley() {

        String[] numeros1 = new String[101];
        numeros1[0] = "1-";
        String[] numeros2 = new String[101];
        numeros2[0] = "2-";
        String[] numeros3 = new String[101];
        numeros3[0] = "3-";
        for (int i = 0; i < 100; i++) {
            String i_s = String.valueOf(i);
            if(i_s.length() == 1) {
                i_s = "0" + i_s;
            }
            numeros1[i+1] = i_s;
            numeros2[i+1] = i_s;
            numeros3[i+1] = i_s;
        }
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, numeros1);
        num_premio1.setAdapter(adapter1);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, numeros2);
        num_premio2.setAdapter(adapter2);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, numeros3);
        num_premio3.setAdapter(adapter3);

    }

    private void llenar_spinner_winer(String airetol) {

        int ref = 0;
        if (airetol.equals("regular") | airetol.equals("reventados")) {
            ref = 100;
        } else if (airetol.equals("monazos")) {
            ref = 1000;
        } else {
            //No debe llegar aqui.
        }
        String[] numeros = new String[ref + 1];
        numeros[0] = "Num";
        for (int i = 0; i < ref; i++) {
            String i_s = String.valueOf(i);
            if (airetol.equals("monazos")) {
                if (i_s.length() == 1) {
                    i_s = "00" + i_s;
                } else if(i_s.length() == 2) {
                    i_s = "0" + i_s;
                }
            } else {
                if(i_s.length() == 1) {
                    i_s = "0" + i_s;
                }
            }
            numeros[i+1] = i_s;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, numeros);
        num_premio1.setAdapter(adapter);

    }

    private void desaparecer_segundo_spinner() {

        num_premio2.setVisibility(View.GONE);
        num_premio1.setVisibility(View.VISIBLE);
        num_premio3.setVisibility(View.VISIBLE);

    }

    private void aparecer_spinners_limpios() {

        String[] s = new String[1];
        s[0] = " ";
        num_premio2.setVisibility(View.VISIBLE);
        num_premio1.setVisibility(View.VISIBLE);
        num_premio3.setVisibility(View.VISIBLE);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, s);
        num_premio1.setAdapter(adapter1);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, s);
        num_premio2.setAdapter(adapter2);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, s);
        num_premio3.setAdapter(adapter3);

    }

    private void limpiar_spinner_horario() {

        String[] s = new String[1];
        s[0] = " ";
        ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, s);
        loteria.setAdapter(adapter4);
        ArrayAdapter<String> adapter5 = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, s);
        horario.setAdapter(adapter5);

    }

    private void llenar_spinner_bolita() {

        String[] bolitas = new String[3];
        bolitas[0] = "BLANCA";
        bolitas[1] = "ROJA";
        bolitas[2] = "GRIS";
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, bolitas);
        num_premio3.setAdapter(adapter);

    }

    private void desaparecer_ambos_spinners() {

        num_premio3.setVisibility(View.GONE);
        num_premio2.setVisibility(View.GONE);
        num_premio1.setVisibility(View.VISIBLE);

    }

    //////////////////Personalizacion de la navegacion hacia atras!//////////////////
    @Override
    public void onBackPressed(){
        boton_atras();
    }

    private void boton_atras() {

        Intent Admin = new Intent(this, AdministrarActivity.class);
        startActivity(Admin);
        finish();
        System.exit(0);
    }
    /////////////////////////////////////////////////////////////////////////////////

    private void mensaje_debug(String m) {
        Toast.makeText(this, m, Toast.LENGTH_LONG).show();
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

    private void crearDiccionario() {

        String archivos[] = fileList();
        //String ArchivoCompleto = "";//Aqui se lee el contenido del archivo guardado.
        String selec_lot = loteria.getSelectedItem().toString();
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

    private boolean ArchivoExiste (String archivos [],String Tiquete){
        for (int i = 0; i < archivos.length; i++)
            if (Tiquete.equals(archivos[i]))
                return true;
        return false;
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

    private void llenar_spinner_hor() {
        crear_array_horarios();
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, horarios);
        horario.setAdapter(adapter2);
    }

    private void select_lot_msg () {
        Toast.makeText(this, "Debe seleccionar una loteria", Toast.LENGTH_SHORT).show();
    }

    private void msg (String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void ocultar_teclado(){
        View view = this.getCurrentFocus();
        InputMethodManager imn = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imn.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}