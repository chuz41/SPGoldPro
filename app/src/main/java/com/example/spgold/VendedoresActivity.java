package com.example.spgold;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
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
import org.json.JSONException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class VendedoresActivity extends AppCompatActivity {

    private Spinner vendedores;
    private String listas_to_print = "LISTAS:";
    private Button listas;
    private Button cierre;
    private TextView rotulo;
    private Integer TOTAL_VENDEDORES = 0;
    private String[] VENDEDORES;
    private TextView letrero_spinner;
    private TextView downloading;
    private String bandera_continuar = "abajo";
    private int contador_de_premios = 0;
    private int contador_de_premios_local = 0;
    private int comision_banca = 0;
    private String mes;
    private String dia;
    private String anio;
    private String hora;
    private String minuto;
    private String segundo;
    private int total_premios = 0;
    private String fecha;
    private String dispositivo;
    private String vendedor = "";
    private String maquina = "";
    private String vendedor_a_presentar = "";
    private HashMap<String, String> SpreadSheets = new HashMap<String, String>();
    HashMap<String, Integer> hasMap_ordenado = new HashMap<String, Integer>();
    private HashMap<String, String> Premios = new HashMap<String, String>();
    private HashMap<String, String> premios_encontrados = new HashMap<String, String>();//Ej. key: tica   value: noche TODO: poner al final de premios. 
    private HashMap<String, Integer> Montos = new HashMap<String, Integer>();
    private HashMap<String, Integer> Premiados = new HashMap<String, Integer>();
    private HashMap<String, Integer> Comisiones = new HashMap<String, Integer>();
    private HashMap<Integer, String> saled_lots = new HashMap<Integer, String>();//Loterias vendidas
    private HashMap<String, String> SpreadSheets_vendedor = new HashMap<String, String>();
    private Map<String, Integer> meses = new HashMap<String, Integer>();
    private String readRowURL_global = "https://script.google.com/macros/s/AKfycbxJNCrEPYSw8CceTwPliCscUtggtQ2l_otieFmE/exec?spreadsheetId=1mQTx7D8-bKKcs7D9ZVdVw4lDbcW3u0N4gP8cKPpfC20&sheet=vendedores";
    private String readRowURL_premios = "https://script.google.com/macros/s/AKfycbxJNCrEPYSw8CceTwPliCscUtggtQ2l_otieFmE/exec?spreadsheetId=1iMXw4z0ljwvfhdR5BBmh586h1AOmNCWll7GYI1MJFbM&sheet=hoy";
    private String readRowURL_vendidas = "https://script.google.com/macros/s/AKfycbxJNCrEPYSw8CceTwPliCscUtggtQ2l_otieFmE/exec?spreadsheetId=";
    private String readRowURL = "https://script.google.com/macros/s/AKfycbxJNCrEPYSw8CceTwPliCscUtggtQ2l_otieFmE/exec?spreadsheetId=";
    private String sid_vendidas;
    private String s_vendidas;

    private int total_ventas = 0;
    private int comision_vendedor = 0;
    private int getComision_banquero = 0;
    public int conta_ventas = 0;
    private ProgressBar progressBar;
    private int var_maxima = 0;// Es la cantidad de loterias que tiene el vendedor. pero no debe cambiar, como en el caso del hashmap.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendedores);
        listas = (Button) findViewById(R.id.btlista);
        cierre = (Button) findViewById(R.id.btcierre);
        listas.setText("Ver listas");
        cierre.setText("Ver cierre");
        listas.setVisibility(View.GONE);
        cierre.setVisibility(View.GONE);
        rotulo = (TextView) findViewById(R.id.rotulo_mix);
        rotulo.setText("Cierre vendedor");
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setMax(100);
        vendedores = (Spinner) findViewById(R.id.spinner_vendedores);
        letrero_spinner = (TextView) findViewById(R.id.rot_vendedores);
        downloading = (TextView) findViewById(R.id.downloading);
        downloading.setText("Descargando...");
        downloading.setVisibility(View.GONE);
        clear_all();
        dispositivo = check_device();
        Log.v("onCreate", "Dispositivo: " + dispositivo);
        pre_config();
    }

    private void clear_all() {
        SpreadSheets.clear();
        saled_lots.clear();
        Premios.clear();
        Montos.clear();
        SpreadSheets_vendedor.clear();
        meses.clear();
        hasMap_ordenado.clear();
        premios_encontrados.clear();
        Premiados.clear();
        Comisiones.clear();
        contador_de_premios = 0;
        contador_de_premios_local = 0;
        bandera_continuar = "abajo";
        comision_banca = 0;
        total_premios = 0;
        vendedor = "";
        maquina = "";
        vendedor_a_presentar = "";
        total_ventas = 0;
        comision_vendedor = 0;
        getComision_banquero = 0;
        conta_ventas = 0;
    }

    private void pre_config() {
        vendedores.setVisibility(View.GONE);
        letrero_spinner.setVisibility(View.GONE);
        Integer[] params = new Integer[2];
        params[0] = 1000;
        params[1] = 0;
        new MyTask().execute(params);
        spinner_put();
        gene_fecha_hoy();
        Log.v("pre_config", ".\nDia: " + dia + "\nMes: " + mes + "\nAnio: " + anio + "\nHora: " + hora + "\nMinuto: " + minuto);
        listas_to_print = listas_to_print + "\nFecha: " + dia + "/" + mes + "/" + anio + "\nHora: " + hora + ":" + minuto + "\n";
        contar_premios();
        //vendedores.setFocusableInTouchMode(true);
    }

    private void leer_premios() {
        //Algoritmo para ver los resultados de hoy:
        if (verificar_internet()) {
            Ver_premios();
        } else {
            msg("Debe estar conectado a una red de Internet!!!");
            return;
        }
    }

    private void generar_cierre3() throws JSONException {//Parte de los premios...
        for (String key : Premiados.keySet()) {
            String[] split = key.split("ojo-rojo_ojo-rojo");
//                                              Tica                             dia                           90                                        500                                       25                               no                              no
//                                            split[1]                         split[2]                      split[3]                                  split[4]                                  split[5]                        split[6]                        split[7]
//String key_premiados = "ojo-rojo_ojo-rojo" + loteria + "ojo-rojo_ojo-rojo" + horario + "ojo-rojo_ojo-rojo" + paga + "ojo-rojo_ojo-rojo" + String.valueOf(monto_apuesta) + "ojo-rojo_ojo-rojo" + premio1 + "ojo-rojo_ojo-rojo" + premio2 + "ojo-rojo_ojo-rojo" + premio3 + "ojo-rojo_ojo-rojo";
//int value_premiados = monto_premio;
            int espaciado3 = 30;
            espaciado3 = espaciado3 - split[1].length() - 1 - split[2].length() - 2 - String.valueOf(Premiados.get(key)).length();
            String espaciado3_str = "";
            for (int o = 0; o < espaciado3; o++) {
                espaciado3_str = espaciado3_str + " ";
            }
            vendedor_a_presentar = vendedor_a_presentar + split[1] + " " + split[2] + ": " + espaciado3_str + Premiados.get(key) + "\n";
        }
        vendedor_a_presentar = vendedor_a_presentar + "\nSubidos a esta hora:\n" + hora + ":" + minuto + "\n\n";
        for (String key : premios_encontrados.keySet()) {
            vendedor_a_presentar = vendedor_a_presentar + key + " " + premios_encontrados.get(key) + "\n";
        }
        int espaciado3 = 30;
        espaciado3 = espaciado3 - 18 - String.valueOf(total_premios).length();
        String espaciado3_str = "";
        for (int o = 0; o < espaciado3; o++) {
            espaciado3_str = espaciado3_str + " ";
        }
        vendedor_a_presentar = vendedor_a_presentar + "\nTOTAL EN PREMIOS: " + espaciado3_str + String.valueOf(total_premios);
        vendedor_a_presentar = vendedor_a_presentar + "\n______________________________\n\n";
        Log.v("generar_cierre3", ".\n\nvendedor_a_presentar:\n\n" + vendedor_a_presentar);
        saldo_del_dia();
    }

    private void saldo_del_dia() {
        //printIt("Monto total: " + String.valueOf(calcular_monto_total()) + "\nComision vendedor: " + comision_vendedor +
        //"\nComision banca: " + comision_banca + "\nPremios: "+ total_premios);
        int saldo_del_dia = calcular_monto_total() - comision_vendedor - comision_banca - total_premios;
        vendedor_a_presentar = vendedor_a_presentar + "SALDOS:\n\n";
        int espaciado0 = 30;
        espaciado0 = espaciado0 - 15 - String.valueOf(saldo_del_dia).length();
        String espaciado0_str = "";
        for (int o = 0; o < espaciado0; o++) {
            espaciado0_str = espaciado0_str + " ";
        }
        vendedor_a_presentar = vendedor_a_presentar + "Saldo del dia: " + espaciado0_str + String.valueOf(saldo_del_dia) + "\n";
        //guardar_saldo();
        int saldo_anterior = leer_saldo_anterior();
        int espaciado1 = 30;
        espaciado1 = espaciado1 - 16 - String.valueOf(saldo_anterior).length();
        String espaciado1_str = "";
        for (int o = 0; o < espaciado1; o++) {
            espaciado1_str = espaciado1_str + " ";
        }
        vendedor_a_presentar = vendedor_a_presentar + "Saldo anterior: " + espaciado1_str + String.valueOf(saldo_anterior) + "\n";
        int saldo_total_a_la_fecha = saldo_del_dia + saldo_anterior;
        int espaciado2 = 30;
        espaciado2 = espaciado2 - 18 - String.valueOf(saldo_total_a_la_fecha).length();
        String espaciado2_str = "";
        for (int o = 0; o < espaciado2; o++) {
            espaciado2_str = espaciado2_str + " ";
        }
        vendedor_a_presentar = vendedor_a_presentar + "Saldo a la fecha: " + espaciado2_str + String.valueOf(saldo_total_a_la_fecha) + "\n";
        vendedor_a_presentar = vendedor_a_presentar + "\n\n------ ULTIMA --- LINEA ------\n\n\n\n";
        Log.v("saldo_del_dia", ".\n\nVendedor_a_presentar:\n\n" + vendedor_a_presentar + "\n\n.");
        elegir();
    }

    private void elegir() {
        //Hacer Visibles los 2 botones
        downloading.setText("Descargas completadas al 100%");
        //listas.setVisibility(View.VISIBLE);
        //cierre.setVisibility(View.VISIBLE);
        String string = vendedor_a_presentar + "\n\n" + listas_to_print;
        Log.v("elegir", ".\n\nVendedor_a_presentar:\n\n" + vendedor_a_presentar + "\n\n.");
        printIt(string);
    }

    public void ver_listas(View view) {

        //TODO: Presentar todas las listas!!!
        printIt(listas_to_print);

    }

    public void ver_cierre(View view) {
        printIt(vendedor_a_presentar);
    }

    private void guardar_saldo() {
    //TODO
    }

    private int leer_saldo_anterior() {//TODO: HACER ESTO EN UNA SPREADSHEET
        int i = 0;
        String archivos[] = fileList();
        String ArchivoCompleto = "";//Aqui se lee el contenido del archivo guardado.
        String file_name = vendedor + "_" + maquina;
        if (ArchivoExiste(archivos, file_name)) {
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(file_name));
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();
                String[] split = linea.split("ojo-rojo_ojo-rojo");
                //msg("Debug:\nSaldo anterior: " + String.valueOf(i));
                i = Integer.parseInt(split[1]);
                br.close();
                archivo.close();
            } catch (IOException e) {
            }
        } else {
            //Do nothing.
        }
        return i;
    }

    private void contar_premios() {

        if (verificar_internet()) {

            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);

            Premios.clear();
            RequestQueue requestQueue;

            // Instantiate the cache
            Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

            // Set up the network to use HttpURLConnection as the HTTP client.
            Network network = new BasicNetwork(new HurlStack());

            // Instantiate the RequestQueue with the cache and network.
            requestQueue = new RequestQueue(cache, network);

            // Start the queue
            requestQueue.start();

            String url = readRowURL_premios;

            // Formulate the request and handle the response.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onResponse(String response) {
                            // Do something with the response
                            //ML_ver.setText(response);

                            //HashMap<String, String> premios = new HashMap<String, String>();
                            String[] spliti = response.split("\"");
                            if (response != null & spliti.length > 3) {
                                //response.replace("loteria", "_sepa_");
                                //msg(response);
                                String[] split = response.split("loteria");//Se separa el objeto Json

                                //Se llena un HashMap con los premios, los cuales se bajan de la nube.
                                Premios.clear();
                                for (int i = 1; i < split.length; i++) {
                                    contador_de_premios++;
                                    //Debug
                                    //msg(String.valueOf(contador_de_premios));
                                    String[] split2 = split[i].split("\"");
                                    //                       Ej.                    Tica                              Noche
                                    String loteria_actual = "ojo-rojo_ojo-rojo" + split2[2] + "ojo-rojo_ojo-rojo" + split2[6] + "ojo-rojo_ojo-rojo";
                                    //                       Ej.                    03                                 no                                 no                                ID
                                    String premio_actual = "ojo-rojo_ojo-rojo" + split2[10] + "ojo-rojo_ojo-rojo" + split2[14] + "ojo-rojo_ojo-rojo" + split2[18] + "ojo-rojo_ojo-rojo" + split2[26] + "ojo-rojo_ojo-rojo";
                                    if (Premios.containsKey(loteria_actual)) {
                                        String premio_viejo = Premios.get(loteria_actual);
                                        String[] split3 = premio_viejo.split("ojo-rojo_ojo-rojo");


                                        //                  ID guardado         ID leido actualmente
                                        if (Integer.parseInt(split3[4]) >= Integer.parseInt(split2[26])) {
                                            //Premios.put()
                                            //Do nothing.
                                        } else {
                                            Premios.replace(loteria_actual, premio_viejo, premio_actual);
                                            String keye = split2[2] + " " + split2[6];
                                            String valueye = "";
                                            String sec = "";
                                            String tir = "";
                                            if (!split2[14].equals("no")) {//Significa que es Parley
                                                sec = " " + split2[14];
                                                tir = " " + split2[18];
                                            } else if (split2[18].equals("ROJA") | split2[18].equals("BLANCA") | split2[18].equals("VERDE")) {// Significa que es reventados.//TODO: Poner colores de bolitas online y todo con respecto a las loterias y sus cambios
                                                tir = " " + split2[18];
                                            } else {//Cualquier otra loteria
                                                //do nothing.
                                            }
                                            valueye = split2[10] + sec + tir;

                                            premios_encontrados.replace(keye, valueye);
                                        }
                                    } else {
                                        Premios.put(loteria_actual, premio_actual);
                                        String keye = split2[2] + " " + split2[6];
                                        String valueye = "";
                                        String sec = "";
                                        String tir = "";
                                        if (!split2[14].equals("no")) {//Significa que es Parley
                                            sec = " " + split2[14];
                                            tir = " " + split2[18];
                                        } else if (split2[18].equals("ROJA") | split2[18].equals("BLANCA") | split2[18].equals("VERDE")) {// Significa que es reventados.
                                            tir = " " + split2[18];
                                        } else {//Cualquier otra loteria
                                            //do nothing.
                                        }
                                        valueye = split2[10] + sec + tir;

                                        premios_encontrados.put(keye, valueye);

                                    }
                                }
                            }

                            Log.v("Contar_premios", ".\nResponse:\n\n" + response);
                            ver_vendedores();
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
        } else {
            Toast.makeText(this, "Debe estar conectado a una red de internet!!!", Toast.LENGTH_LONG).show();
            /*
            clear_all();
            dispositivo = check_device();
            pre_config();
            */
        }
    }

    private void Ver_premios() {

        vendedores.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (vendedores.getSelectedItem().toString().equals("Elija el vendedor...") | vendedores.getSelectedItem().toString().equals("Elija un vendedor...")) {
                            //msg("Escoja un vendedor...");
                        } else {
                            //clear_all();
                            //pre_config();
                            //cierre.setVisibility(View.GONE);
                            if (vendedores.getSelectedItem().toString().equals("Todos")) {
                                //TODO: Ver que hacer para manejar la opcion "Todos" puede ser generar un cierre general!
                            } else {
                                progressBar.setProgress(0);
                                progressBar.setVisibility(View.VISIBLE);
                                String[] split = vendedores.getSelectedItem().toString().split(" ");
                                vendedor = split[0];
                                maquina = split[1];
                                downloading.setVisibility(View.GONE);
                                //vendedores.setFocusableInTouchMode(false);
                                listas_to_print = listas_to_print + "Puesto " + vendedor + "\nMaquina: " + maquina + "\n******************************\n\n";

                                Log.v("Ver_premios", ".\n\nSe ha seleccionado el vendedor " + vendedor + " numero de maquina: " + maquina + "\n\n.");
                                presentar_vendedor(split[0], split[1]);
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
    }


    public String generar_cierre(HashMap<String, String> vendep, boolean vez) throws JSONException {//En este metodo se hacen algunas configuraciones y arreglos de variables.
        //cierre.setVisibility(view.GONE);
        vendedores.setVisibility(View.GONE);
        letrero_spinner.setVisibility(View.GONE);

        if (vez) {
            // msg("Hola, estoy en el generar_cierre true!");
            vendedor_a_presentar = vendedor_a_presentar + "______________________________\nVENTAS: \n\n";
            progressBar.setProgress(1);
        } else {
            int pro_cant = TOTAL_VENDEDORES - SpreadSheets_vendedor.size();//Se genera el numero que se va a representar en la progressbar
            progressBar.setProgress(pro_cant);
            //Do nothing.
        }
        //msg("Bandera continuar: " + bandera_continuar);
        Log.v("Bandera continuar: ", bandera_continuar);
        //msg("SpreadSheets_vendedor: " + String.valueOf(SpreadSheets_vendedor.size()));
        //msg("SpreadSheets_vendedor: " + String.valueOf(SpreadSheets_vendedor.size()));
        if (SpreadSheets_vendedor.size() == 0) {
            Log.v("Bandera continuar: ", bandera_continuar);
            //msg("SpreadSheets_vendedor: " + String.valueOf(SpreadSheets_vendedor.size()));
            if (bandera_continuar.equals("listo")) {
                //msg("Ir a generar_cierre2!!!");
                Log.v("generar_cierre", "Ir a generar_cierre2. Todo estubo bien!!!");
                generar_cierre2();
                return "Ultima entrada a generar_cierre. ya no tiene datos y debe abandonar este metodo";
            } else {
                return "Se acabaron las loterias";//TODO: Si esta Empty significa que ya termino. Llamar al siguiente metodo!!!
            }

        } else {
            //Continue;
        }

        Log.v("error3", "generar_ciere. Ya paso los 2 if");

        //esperar(5);

        for (String key : SpreadSheets_vendedor.keySet()) {
            //msg("key: " + key + "\nDato: " + SpreadSheets_vendedor.get(key));
            String[] split4 = SpreadSheets_vendedor.get(key).split("ojo-rojo_ojo-rojo");//Loteria a presentar
            String[] split5 = key.split("ojo-rojo_ojo-rojo");//SpreadSheet y sheet que se van a leer!

            /*                         split5[1]                        split5[2]                          split5[3]                          split5[4]                         split5[5]                          split5[6]
                    Ej.                  Tica                              dia                                hoy                            5 (comision)                         paga1                               paga2
String lot_act = "ojo-rojo_ojo-rojo" + split2[2] + "ojo-rojo_ojo-rojo" + split2[6] + "ojo-rojo_ojo-rojo" + split2[10] + "ojo-rojo_ojo-rojo" + split2[22] + "ojo-rojo_ojo-rojo" + split2[26] + "ojo-rojo_ojo-rojo" + split2[30] + "ojo-rojo_ojo-rojo";

                                          split4[1]                          split4[2]
                    Ej.                  SpreadSheet                           SHEET
String spread_act = "ojo-rojo_ojo-rojo" + split2[14] + "ojo-rojo_ojo-rojo" + split2[18] + "ojo-rojo_ojo-rojo";
             */

            if (verificar_internet()) {
                if (vendedores.getSelectedItem().toString().equals("Todos")) {
                    return "Se ha seleccionado \"Todos\"";
                    //TODO: Ver que hacer para manejar la opcion "Todos"
                } else {
                    //Ej.                        SpreadSheet                    SHEET                        Tica             dia
                    //msg("Debug:\nSpreadSheet: " + split4[1] + "\nSHEET: " + split4[2] + "\nLoteria: " + split5[1] + " " + split5[2]);
                    //       SpreadSheet    SHEET     Loteria    Horario   comision   hoy/maniana  paga1      paga2
                    SpreadSheets_vendedor.remove(key);
                    //msg("Loteria: " + split5[1] + " " + split5[2]);
                    leer_tabla(split4[1], split4[2], split5[1], split5[2], split5[4], split5[5], split5[6], SpreadSheets_vendedor);//aqui se leen las ventas de todas las loterias que halla vendido vendedor
                    Log.v("error4", "Dentro del loop for. Justo antes del... break!");
                    return "Final deseado";
                }
            } else {
                //Do nothing.
                return "No hay internet!";
            }
        }
        return "El for ha terminado!!!";
    }

    private void generar_cierre2() throws JSONException {
        //cierre2.setVisibility(View.GONE);
        Calcular_Total_Ventas();//Se hace aqui para darle tiempo a las demas acciones.
        //aqui se calculan las comisiones
        int espaciado = 30;
        int monto_total = calcular_monto_total();
        int comision_total_vendedor = calcular_comision_vendedor();
        comision_vendedor = comision_total_vendedor;
        //int comision_total_banca = (int) (monto_total * 0.04);
        //comision_banca = comision_total_banca;
        int comision_general = (int) (comision_total_vendedor);
        espaciado = espaciado - 19 - String.valueOf(comision_total_vendedor).length();
        String espaciado_str = "";
        //msg("Espaciado: >" + espaciado_str + "<");
        for (int o = 0; o < espaciado; o++) {
            espaciado_str = espaciado_str + " ";
        }

        int espaciado3 = 30;
        espaciado3 = espaciado3 - 16 - String.valueOf(comision_general).length();
        String espaciado3_str = "";
        //msg("Espaciado: >" + espaciado_str + "<");
        for (int o = 0; o < espaciado3; o++) {
            espaciado3_str = espaciado3_str + " ";
        }
        vendedor_a_presentar = vendedor_a_presentar + "COMISIONES: \n\nComision vendedor: " + espaciado_str + comision_total_vendedor +
                "\n\nCOMISION TOTAL: " + espaciado3_str + comision_general +
                "\n______________________________\n\nPREMIOS:\n\n";
        Log.v("generar_cierre2", "Comision: " + comision_general);
        generar_cierre3();
    }

    private void leer_tabla(String SiD, String SheeT, String loteria, String horario, String comision, String paga1, String paga2, HashMap<String, String> SpreadSheets_vendedor2) {
        RequestQueue requestQueue;
        //msg("Llegamos a leer_tabla");
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();

        String url = readRowURL + SiD + "&sheet=" + SheeT;
        Log.v("Error14", "SpreadSheet: " + SiD + "\nSheet: " + SheeT);
        //msg("SpreadSheet: " + SiD + "\nSheet: " + SheeT);

        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String response) {
                        //msg("onResponse de leer_tabla");
                        // Do something with the response
                        downloading.setVisibility(View.VISIBLE);
                        downloading.setText("Descargando ventas de " + loteria + " " + horario + "...");
                        String[] splitt = response.toString().split("\"");
                        //msg(response);
                        int length_splitt = splitt.length;
                        //msg("Response: " + response);
                        if (response != null & length_splitt > 3) {
                            conta_ventas++;
                            //msg("response: \n" + response);
                            //if (length_splitt > )
                            //msg(splitt[1]);
                            if (splitt[1].equals("numero1")) {//SheeT puede ser dia, tarde o noche!
                                //Respuesta es la correcta!
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////Aqui va el total de los algoritmos funcionales basados en una respuesta correcta!/////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                //msg(response);
                                String[] split = response.split("numero1");//Se separa el objeto Json
                                //Se llena un HashMap local con las ventas, las cuales se bajan de la nube.
                                HashMap<String, String> hashMap = new HashMap<String, String>();
                                for (int i = 1; i < split.length; i++) {//En este loop se filtran los valores repetidos en la tabla.

                                    String[] split2 = split[i].split("\"");
                                    String nu1 = split2[2];
                                    String nu2 = split2[6];
                                    String monto = split2[10];//10
                                    String extra_info = split2[14];//14
                                    //String factura = split2[18];
                                    String iD = split2[22];
                                    Log.v("Error106", "split[18]: " + split2[18] + " split[14]: " + split2[14]);
                                    String key_factura = "ojo-rojo_ojo-rojo" + nu1 + "ojo-rojo_ojo-rojo" + nu2 + "ojo-rojo_ojo-rojo" + extra_info + "ojo-rojo_ojo-rojo" + iD + "ojo-rojo_ojo-rojo";
                                    String valor_factura = "ojo-rojo_ojo-rojo" + monto + "ojo-rojo_ojo-rojo";
                                    Log.v("Error105", "Key: " + key_factura + "\nValue: " + valor_factura);

                                    //////////////////////////////////////////////////////////////////////////
                                    ///////Algoritmo que verifica si hay IDs iguales para omitirlos!//////////
                                    //////////////////////////////////////////////////////////////////////////
                                    if (hashMap.containsKey(key_factura)) {
                                        //Do nothing!
                                        //Si llega aqui significa que ha encontrado una linea repetida en la spreadsheet, por lo tanto la omite.
                                    } else {//   numeros jugados  monto jugado
                                        hashMap.put(key_factura, valor_factura);//Se agrega la venta al hasmap local. Ej. Tica.
                                    }
                                    //////////////////////////////////////////////////////////////////////////
                                }
                                /*for (String key : hashMap.keySet()) {
                                    msg("Factura: " + key + "\nValor: " + hashMap.get(key) + "\n");
                                }*/
                                //msg("antes del iterar_mapa, es ahi donde se ven los premiados");
                                Log.v("error12", "antes del iterar_mapa, es ahi donde se ven los premiados");
                                iterar_mapa(hashMap, loteria, horario, comision, paga1, paga2);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


                            } else {
                                //Respuesta incorrecta!!!
                            }
                        } else {
                            llamar_al_cierre();
                            //Respuesta incorrecta!!!
                        }
                        //TODO: llamar aqui a la funcion que llama a esta funcion!!!

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

    private void llamar_al_cierre() {
        Log.v("error6", "funcion que llama de nuevo a generar_cierre");
        try {
            String otra_variable = generar_cierre(SpreadSheets_vendedor, false);
            Log.v("error8", "Despues de llamar a generar_cierre: " + otra_variable);
            //msg("Despues de llamar a generar_cierre: " + otra_variable);
        } catch (Exception e) {
            Log.v("error7", "catch del metodo llamar_al_cierre");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void iterar_mapa(HashMap<String, String> hashMap, String loteria, String horario, String comision, String paga1, String paga2) {

        listas_to_print = listas_to_print + loteria + " " + horario + ":\n\n";


        Log.v("Error102", "Loteria: " + loteria + " " + horario + "\nComision: " + comision + "\nPaga1: " + paga1 + " Paga2: " + paga2);
        //msg("Loteria: " + loteria + " " + horario + "\nComision: " + comision + "\nPaga1: " + paga1 + " Paga2: " + paga2);
        HashMap<String, Integer> hashMap_local = crear_hasmap_local_ordenado();//Este hasmap tendra los numeros contenidos en hasMap pero sin repetir y sumados sus valores.
        Log.v("Error10", "iterar_mapa");

        for (String key : hashMap.keySet()) {
            String[] split_valor = hashMap.get(key).split("ojo-rojo_ojo-rojo");
            Log.v("Error104", "Key: " + key + "\nValue: " + hashMap.get(key));
            //Log.v("Error103", "Monto: " + split_valor[1]);
            //msg("Monto: " + split_valor[1]);
        }

        for (String key : hashMap.keySet()) {//Agregamos los valores de hashMap a hashMap_local pero ordenados y sumadas las apuestas de los numeros repetidos.
            /*
            Ejemplo:
            String key = "ojo-rojo_ojo-rojo" + nu1 + "ojo-rojo_ojo-rojo" + nu2 + "ojo-rojo_ojo-rojo" + extra_info + "ojo-rojo_ojo-rojo" + iD + "ojo-rojo_ojo-rojo";
            String valor = "ojo-rojo_ojo-rojo" + monto + "ojo-rojo_ojo-rojo";
             */
            //Ejemplo de como reemplazar valores del HashMap:  if (hashMap.containsKey(loteria_actual)) { hashMap.replace(loteria_actual, premio_viejo, premio_actual) };
            String[] split_valor = hashMap.get(key).split("ojo-rojo_ojo-rojo");
            int monto = Integer.parseInt(split_valor[1]);
           /* if (split_valor.length == 2) {//Es regular o reventados.
                //
            } else if (split_valor[1].equals("Orden") | split_valor[1].equals("Desorden")) {//Es monazos
                //
            } else if (Integer.parseInt(split_valor[0]) >= 0 & Integer.parseInt(split_valor[1]) >= 0 & split_valor.length == 3) {//Es parley
                //
            } else {
                //Do nothing.
            }*/

            //msg("Monto: " + String.valueOf(monto));
            String[] split_key = key.split("ojo-rojo_ojo-rojo");
            String new_key = "ojo-rojo_ojo-rojo" + split_key[1] + "ojo-rojo_ojo-rojo" + split_key[2] + "ojo-rojo_ojo-rojo" + split_key[3] + "ojo-rojo_ojo-rojo";//Se omite el ID ya que a este punto se ha pasado el filtro de IDs.
            //msg(new_key);
            if (hashMap_local.containsKey(new_key)) {
                //Significa que se deben sumar los valores
                int valor_viejo = hashMap_local.get(new_key);
                int valor_nuevo = valor_viejo + monto;
                hashMap_local.replace(new_key, valor_viejo, valor_nuevo);
            } else {
                //msg("Key: " + new_key + "\nvalue: " + monto);
                hashMap_local.put(new_key, monto);
            }
        }
        Log.v("Error11", "iterar_mapa");
        //ahora iteramos el hashmap local ordenado para imprimir o sumar los montos.
        int monto_total = 0;

        TreeMap<String, Integer> treeMap = new TreeMap<String, Integer>();
        treeMap.putAll(hashMap_local);
        int monto_contado = 0;
        for (String key : treeMap.keySet()) {
            String[] key_splited = key.split("ojo-rojo_ojo-rojo");
            String numero_jugado1 = key_splited[1];
            String numero_jugado2 = key_splited[2];//Numeros evaluados en esta iteracion
            String numero_jugado3 = key_splited[3];
            int monto_apuesta = hashMap_local.get(key);
            //TODO: fabricar el algoritmo que imprime las listas.
            String par1 = "";
            if (String.valueOf(numero_jugado1).length() == 1) {
                par1 = "0" + numero_jugado1;
            } else {
                par1 = numero_jugado1;
            }

            String par2 = "";
            String par3 = "";
            if (!numero_jugado2.equals("no")) {//Es parley
                if (String.valueOf(numero_jugado2).length() == 1) {
                    par2 = " 0" + numero_jugado2;
                } else {
                    par2 = " " + numero_jugado2;
                }

                //par3 = " " + numero_jugado3;
            } else if (numero_jugado3.equals("Orden") | numero_jugado3.equals("Desorden")) {// Es monazos
                if (String.valueOf(numero_jugado1).length() == 2) {
                    par1 = "0" + numero_jugado1;
                } else {
                    par1 = numero_jugado1;
                }
                if (numero_jugado3.equals("Orden")) {
                    par3 = " " + numero_jugado3 + "   ";
                } else {
                    par3 = " " + numero_jugado3;
                }
            } else {
                //Do nothing.
            }

            if (monto_apuesta <= 0) {
                //Do nothing. Significa que se borro una loteria.
            } else {
                monto_contado = monto_contado + monto_apuesta;
                listas_to_print = listas_to_print + par1 + par2 + par3 + "  -->  " + String.valueOf(monto_apuesta) + "\n";
            }
        }
        listas_to_print = listas_to_print + "\nTotal:  " + String.valueOf(monto_contado) + " colones.\n";
        listas_to_print = listas_to_print + "\n******************************\n\n";


        //Llenar HashMap premiados
        for (String key : hashMap_local.keySet()) {
            String[] key_splited = key.split("ojo-rojo_ojo-rojo");
            String numero_jugado1 = key_splited[1];
            String numero_jugado2 = key_splited[2];//Numeros evaluados en esta iteracion
            String numero_jugado3 = key_splited[3];
            int monto_apuesta = hashMap_local.get(key);

            //msg("key: " + key + "\n\nValue: " + hashMap_local.get(key));
            for (String key2 : Premios.keySet()) {
                String[] key2_splited = key2.split("ojo-rojo_ojo-rojo");
                String numero_premiado = Premios.get(key2);
                String[] premios_splited = numero_premiado.split("ojo-rojo_ojo-rojo");
                String premio1 = premios_splited[1];
                String premio2 = premios_splited[2];//Premios que se han bajado directamente de la nube.
                String premio3 = premios_splited[3];
                String Loteria_premios = key2_splited[1] + " " + key2_splited[2];
                String Loteria_actual = loteria + " " + horario;
                if (Loteria_actual.equals(Loteria_premios)) {
                    //msg("numero premiado: " + numero_premiado);
                    //msg("Loteria Premios: " + key2_splited[1] + " " + key2_splited[2] + "\n\nLoteria actual: " + loteria + " " + horario);
                    //int numero_winner = Integer.parseInt(numero_premiado);

                    String tipo_loteria = "regular";
                    if (numero_jugado2.equals("no")) {
                        //Do nothing!
                    } else {
                        tipo_loteria = "parley";
                    }
                    if (numero_jugado3.equals("Orden") | numero_jugado3.equals("Desorden")) {
                        tipo_loteria = "monazos";
                    } else {
                        //Do nothing.
                    }
                    if (premio3.equals("ROJA") | premio3.equals("VERDE") | premio3.equals("BLANCA")) {
                        tipo_loteria = "reventados";
                    }
                    //msg("Tipo loteria: " + tipo_loteria + "\nNumeros jugados: " + numero_jugado1 + " - " + numero_jugado2 + " - " + numero_jugado3 +
                    //              "\nNumeros premiados: " + premio1 + " - " + premio2 + " - " + premio3);

                    if (tipo_loteria.equals("regular")) {

                        //Aqui se revisa si hay premios!!!
                        if (numero_jugado1.equals(premio1)) {
                            int paga = Integer.parseInt(paga1);
                            int monto_premio = monto_apuesta * paga;
                            //                                            Tica                             dia                           90                                        500                                       25                               no                              no
                            //                                          split[1]                         split[2]                      split[3]                                  split[4]                                  split[5]                        split[6]                        split[7]
                            String key_premiados = "ojo-rojo_ojo-rojo" + loteria + "ojo-rojo_ojo-rojo" + horario + "ojo-rojo_ojo-rojo" + paga + "ojo-rojo_ojo-rojo" + String.valueOf(monto_apuesta) + "ojo-rojo_ojo-rojo" + premio1 + "ojo-rojo_ojo-rojo" + premio2 + "ojo-rojo_ojo-rojo" + premio3 + "ojo-rojo_ojo-rojo";
                            int value_premiados = monto_premio;
                            if (Premiados.containsKey(key_premiados)) {
                                int monto_viejo = Premiados.get(key_premiados);//Valor actual del hashmap
                                int monto_nuevo = Premiados.get(key_premiados) + monto_premio;
                                Premiados.replace(key_premiados, monto_viejo, monto_nuevo);
                                total_premios = total_premios + monto_premio;
                            } else {
                                Premiados.put(key_premiados, value_premiados);
                                total_premios = total_premios + monto_premio;
                            }
                        } else {
                            //Este numero no es el premiado. Se debe continuar buscando.
                        }

                    } else if (tipo_loteria.equals("reventados")) {

                        //Aqui se revisa si hay premios!!!
                        if (premio3.equals("BLANCA")) {

                        } else {
                            int paga = 0;
                            if (numero_jugado1.equals(premio1)) {//Entra si el numero jugado coninside con el numero premiado
                                if (premio3.equals("ROJA")) {
                                    paga = Integer.parseInt(paga1);
                                    int monto_premio = monto_apuesta * paga;
                                    //                                         reventados                          dia                           700                                        500                                       25                              no                             ROJA
                                    //                                          split[1]                         split[2]                      split[3]                                  split[4]                                  split[5]                         split[6]                        split[7]
                                    String key_premiados = "ojo-rojo_ojo-rojo" + loteria + "ojo-rojo_ojo-rojo" + horario + "ojo-rojo_ojo-rojo" + paga + "ojo-rojo_ojo-rojo" + String.valueOf(monto_apuesta) + "ojo-rojo_ojo-rojo" + premio1 + "ojo-rojo_ojo-rojo" + premio2 + "ojo-rojo_ojo-rojo" + premio3 + "ojo-rojo_ojo-rojo";
                                    int value_premiados = monto_premio;
                                    if (Premiados.containsKey(key_premiados)) {
                                        int monto_viejo = Premiados.get(key_premiados);//Valor actual del hashmap
                                        int monto_nuevo = Premiados.get(key_premiados) + monto_premio;
                                        Premiados.replace(key_premiados, monto_viejo, monto_nuevo);
                                        total_premios = total_premios + monto_premio;
                                    } else {
                                        Premiados.put(key_premiados, value_premiados);
                                        total_premios = total_premios + monto_premio;
                                    }
                                } else if (premio3.equals("VERDE")) {
                                    paga = Integer.parseInt(paga2);
                                    int monto_premio = monto_apuesta * paga;
                                    //                                         reventados                          dia                           700                                        500                                       25                              no                             ROJA
                                    //                                          split[1]                         split[2]                      split[3]                                  split[4]                                  split[5]                         split[6]                        split[7]
                                    String key_premiados = "ojo-rojo_ojo-rojo" + loteria + "ojo-rojo_ojo-rojo" + horario + "ojo-rojo_ojo-rojo" + paga + "ojo-rojo_ojo-rojo" + String.valueOf(monto_apuesta) + "ojo-rojo_ojo-rojo" + premio1 + "ojo-rojo_ojo-rojo" + premio2 + "ojo-rojo_ojo-rojo" + premio3 + "ojo-rojo_ojo-rojo";
                                    int value_premiados = monto_premio;
                                    if (Premiados.containsKey(key_premiados)) {
                                        int monto_viejo = Premiados.get(key_premiados);//Valor actual del hashmap
                                        int monto_nuevo = Premiados.get(key_premiados) + monto_premio;
                                        Premiados.replace(key_premiados, monto_viejo, monto_nuevo);
                                        total_premios = total_premios + monto_premio;
                                    } else {
                                        Premiados.put(key_premiados, value_premiados);
                                        total_premios = total_premios + monto_premio;
                                    }
                                } else if (premio3.equals("BLANCA")) {
                                    //paga = 0;
                                } else {
                                    //Do nothing.
                                }
                            } else {
                                //Este numero no es el premiado. Se debe continuar buscando.
                            }
                        }

                    } else if (tipo_loteria.equals("parley")) {

                        String paga = paga1;
                        int pagA = Integer.parseInt(paga);
                        String Winner_number[] = new String[3];
                        Winner_number[0] = premio1;
                        Winner_number[1] = premio2;
                        Winner_number[2] = premio3;
                        int Number_winner1 = Integer.parseInt(Winner_number[0]);
                        int Number_winner2 = Integer.parseInt(Winner_number[1]);
                        int Number_winner3 = Integer.parseInt(Winner_number[2]);
                        String flag1 = "false";
                        String flag2 = "false";
                        String flag3 = "false";
                        int num1 = -1;
                        int num2 = -1;
                        boolean flag11 = false;
                        boolean flag12 = false;
                        boolean flag13 = false;
                        //numero_jugado1.equals(premio1)
                        int Winner_num1 = Integer.parseInt(premio1);
                        int Winner_num2 = Integer.parseInt(premio2);
                        int Winner_num3 = Integer.parseInt(premio3);
                        String to_split = numero_jugado1 + "_" + numero_jugado2;
                        String[] split = to_split.split("_");
                        if (Integer.parseInt(split[0]) == Winner_num1) {
                            num1 = Integer.parseInt(split[0]);
                            flag1 = "true";
                            flag11 = true;
                        } else if (Integer.parseInt(split[0]) == Winner_num2) {
                            num1 = Integer.parseInt(split[0]);
                            flag1 = "true";
                            flag12 = true;
                        } else if (Integer.parseInt(split[0]) == Winner_num3) {
                            num1 = Integer.parseInt(split[0]);
                            flag1 = "true";
                            flag13 = true;
                        }

                        if (flag11) {
                            if (Integer.parseInt(split[1]) == Winner_num2) {
                                num2 = Integer.parseInt(split[1]);
                                flag2 = "true";
                            } else if (Integer.parseInt(split[1]) == Winner_num3) {
                                num2 = Integer.parseInt(split[1]);
                                flag2 = "true";
                            }
                        } else if (flag12) {
                            if (Integer.parseInt(split[1]) == Winner_num1) {
                                num2 = Integer.parseInt(split[1]);
                                flag2 = "true";
                            } else if (Integer.parseInt(split[1]) == Winner_num3) {
                                num2 = Integer.parseInt(split[1]);
                                flag2 = "true";
                            }
                        } else if (flag13) {
                            if (Integer.parseInt(split[1]) == Winner_num1) {
                                num2 = Integer.parseInt(split[1]);
                                flag2 = "true";
                            } else if (Integer.parseInt(split[1]) == Winner_num2) {
                                num2 = Integer.parseInt(split[1]);
                                flag2 = "true";
                            }
                        }


                        //Aqui se comprueba que las dos flags esten en "true":
                        if (flag1.equals("true")) {
                            if (flag2.equals("true")) {
                                flag3 = "true";
                            } else {
                                //Do nothing
                            }
                        } else {
                            //Do nothing
                        }

                        if (flag3.equals("true")) {

                            //Aqui va la carnita:
                            int pagar = Integer.parseInt(paga1);
                            int monto_premio = monto_apuesta * pagar;
                            //                                          monazos                            dia                           700                                       500                                       255                              no                            Orden
                            //                                          split[1]                         split[2]                      split[3]                                  split[4]                                  split[5]                        split[6]                        split[7]
                            String key_premiados = "ojo-rojo_ojo-rojo" + loteria + "ojo-rojo_ojo-rojo" + horario + "ojo-rojo_ojo-rojo" + pagar + "ojo-rojo_ojo-rojo" + String.valueOf(monto_apuesta) + "ojo-rojo_ojo-rojo" + premio1 + "ojo-rojo_ojo-rojo" + premio2 + "ojo-rojo_ojo-rojo" + premio3 + "ojo-rojo_ojo-rojo";
                            int value_premiados = monto_premio;
                            if (Premiados.containsKey(key_premiados)) {
                                int monto_viejo = Premiados.get(key_premiados);//Valor actual del hashmap
                                int monto_nuevo = Premiados.get(key_premiados) + monto_premio;
                                Premiados.replace(key_premiados, monto_viejo, monto_nuevo);
                                total_premios = total_premios + monto_premio;
                            } else {
                                Premiados.put(key_premiados, value_premiados);
                                total_premios = total_premios + monto_premio;
                            }
                        }

                    } else if (tipo_loteria.equals("monazos")) {

                        if (numero_jugado1.equals(premio1)) {//Entra si el numero jugado coninside con el numero premiado, osea, si gano en orden, pero si aposto en desorden, gana en desorden. 
                            int paga = 0;
                            if (numero_jugado3.equals("Orden")) {
                                paga = Integer.parseInt(paga1);
                            } else if (numero_jugado3.equals("Desorden")) {
                                paga = Integer.parseInt(paga2);
                            } else {
                                //Do nothing. Nunca debe llegar aqui.
                            }
                            int monto_premio = monto_apuesta * paga;
                            //                                          monazos                            dia                           700                                       500                                       255                              no                            Orden
                            //                                          split[1]                         split[2]                      split[3]                                  split[4]                                  split[5]                        split[6]                        split[7]
                            String key_premiados = "ojo-rojo_ojo-rojo" + loteria + "ojo-rojo_ojo-rojo" + horario + "ojo-rojo_ojo-rojo" + paga + "ojo-rojo_ojo-rojo" + String.valueOf(monto_apuesta) + "ojo-rojo_ojo-rojo" + premio1 + "ojo-rojo_ojo-rojo" + premio2 + "ojo-rojo_ojo-rojo" + premio3 + "ojo-rojo_ojo-rojo";
                            int value_premiados = monto_premio;
                            if (Premiados.containsKey(key_premiados)) {
                                int monto_viejo = Premiados.get(key_premiados);//Valor actual del hashmap
                                int monto_nuevo = Premiados.get(key_premiados) + monto_premio;
                                Premiados.replace(key_premiados, monto_viejo, monto_nuevo);
                                total_premios = total_premios + monto_premio;
                            } else {
                                Premiados.put(key_premiados, value_premiados);
                                total_premios = total_premios + monto_premio;
                            }
                        } else {
                            //Aqui se van a capturar todos los demas numeros que no calleron en el if del orden.
                            //Primero que nada se debe verificar si hay algun premio en desorden.
                            //Ahora vamos a separar el numero ganador en sus digitos.
                            boolean flag_1 = false, flag_2 = false, flag_3 = false, flag_11 = false, flag_12 = false, flag_13 = false, flag_21 = false, flag_22 = false, flag_23 = false;
                            int Wdig_1 = -1, Wdig_2 = -1, Wdig_3 = -1;
                            int Winner_num = Integer.parseInt(premio1);
                            if (Winner_num < 10) {
                                Wdig_1 = 0;
                                Wdig_2 = 0;
                                Wdig_3 = Winner_num;
                            } else if (Winner_num > 10) {
                                if (Winner_num < 100) {
                                    Wdig_1 = 0;
                                    Wdig_2 = Winner_num / 10;
                                    Wdig_3 = Winner_num % 10;
                                } else {
                                    Wdig_1 = Winner_num / 100;
                                    Wdig_2 = (Winner_num / 10) % 10;
                                    Wdig_3 = Winner_num % 10;
                                }
                            } else {
                                //Do nothing. Nunca llegara aqui!!!
                            }
                            //Ahora vamos a separar en sus digitos el numero que se esta analizando en esta iteracion.
                            int dig_1 = -1, dig_2 = -1, dig_3 = -1;
                            int iter_num = Integer.parseInt(numero_jugado1);
                            //Toast.makeText(this, "iter number: " + iter_num, Toast.LENGTH_LONG).show();
                            if (iter_num < 10) {
                                dig_1 = 0;
                                dig_2 = 0;
                                dig_3 = iter_num;
                            } else {
                                if (iter_num < 100) {
                                    dig_1 = 0;
                                    dig_2 = iter_num / 10;
                                    dig_3 = iter_num % 10;
                                } else {
                                    dig_1 = iter_num / 100;
                                    dig_2 = (iter_num / 10) % 10;
                                    dig_3 = iter_num % 10;
                                }
                            }
                            //Listo la separacion de los numeros en sus digitos. Ahora vamos a verificar si coinsiden en desorden.

                            //Toast.makeText(this, "Vamos a revisar los digitos de los numeros, tanto el ganador como el jugado", Toast.LENGTH_LONG).show();
                            //Toast.makeText(this, "\n" + "dig_1: " + dig_1 + "\ndig_2: " + dig_2 + "\ndig_3: " + dig_3, Toast.LENGTH_LONG).show();
                            //Toast.makeText(this, "\n" + "Wdig_1: " + Wdig_1 + "\nWdig_2: " + Wdig_2 + "\nWdig_3: " + Wdig_3, Toast.LENGTH_LONG).show();


                            if (dig_1 == Wdig_1) {
                                flag_11 = true;
                                flag_1 = true;
                            } else if (dig_1 == Wdig_2) {
                                flag_12 = true;
                                flag_1 = true;
                            } else if (dig_1 == Wdig_3) {
                                flag_13 = true;
                                flag_1 = true;
                            } else {
                                //Do nothing.
                            }

                            if (flag_11) {
                                if (dig_2 == Wdig_2) { //Significa que de haber otra coinsidencia, esta seria orden. X
                                    //flag_22 = true;
                                    //flag_2 = true;
                                } else if (dig_2 == Wdig_3) {
                                    flag_23 = true;
                                    flag_2 = true;
                                } else {
                                    //Do nothing.
                                }
                            } else if (flag_12) {
                                if (dig_2 == Wdig_1) {
                                    flag_21 = true;
                                    flag_2 = true;
                                } else if (dig_2 == Wdig_3) {
                                    flag_23 = true;
                                    flag_2 = true;
                                } else {
                                    //Do nothing.
                                }
                            } else if (flag_13) {
                                if (dig_2 == Wdig_1) {
                                    flag_21 = true;
                                    flag_2 = true;
                                } else if (dig_2 == Wdig_2) {
                                    flag_22 = true;
                                    flag_2 = true;
                                } else {
                                    //Do nothing
                                }
                            } else {
                                //Do nothing
                            }

                            if (flag_21) {
                                if (flag_12) {
                                    if (dig_3 == Wdig_3) {
                                        //flag_33 = true;
                                        flag_3 = true;
                                    } else {
                                        //Do nothing.
                                    }
                                } else if (flag_13) {
                                    if (dig_3 == Wdig_2) {
                                        //flag_32 = true;
                                        flag_3 = true;
                                    } else {
                                        //Do nothing.
                                    }
                                } else {
                                    //Do nothing.
                                }
                            } else if (flag_22) {
                                if (flag_11) {
                                    if (dig_3 == Wdig_3) {
                                        //flag_33 = true;
                                        //flag_3 = true; //Se comenta esta bandera porque representaria el orden, y eso se evaluo en la funcion anterior.
                                    } else {
                                        //Do nothing.
                                    }
                                } else if (flag_13) {
                                    if (dig_3 == Wdig_1) {
                                        //flag_31 = true;
                                        flag_3 = true;
                                    } else {
                                        //Do nothing.
                                    }
                                }
                            } else if (flag_23) {
                                if (flag_11) {
                                    if (dig_3 == Wdig_2) {
                                        //flag_32 = true;
                                        flag_3 = true;
                                    } else {
                                        //Do nothing.
                                    }
                                } else if (flag_12) {
                                    if (dig_3 == Wdig_1) {
                                        //flag_31 = true;
                                        flag_3 = true;
                                    } else {
                                        //Do nothing.
                                    }
                                }
                            } else {
                                //Do nothing.
                            }
                            //Aqui se agrega el numero ganador encontrado con el algoritmo de los if.
                            if (flag_1) {
                                if (flag_2) {
                                    if (flag_3) {
                                        //Coinsidencia encontrada. Se debe verificar si la compra se hizo en desorden.
                                        if (numero_jugado3.equals("Desorden")) {
                                            //Se ha capturado un premio en desorden
                                            int paga = Integer.parseInt(paga2);
                                            int monto_premio = monto_apuesta * paga;
                                            //                                          monazos                            dia                           115                                       500                                       751                              no                           Desorden
                                            //                                          split[1]                         split[2]                      split[3]                                  split[4]                                  split[5]                        split[6]                        split[7]
                                            String key_premiados = "ojo-rojo_ojo-rojo" + loteria + "ojo-rojo_ojo-rojo" + horario + "ojo-rojo_ojo-rojo" + paga + "ojo-rojo_ojo-rojo" + String.valueOf(monto_apuesta) + "ojo-rojo_ojo-rojo" + premio1 + "ojo-rojo_ojo-rojo" + premio2 + "ojo-rojo_ojo-rojo" + premio3 + "ojo-rojo_ojo-rojo";
                                            int value_premiados = monto_premio;
                                            if (Premiados.containsKey(key_premiados)) {
                                                int monto_viejo = Premiados.get(key_premiados);//Valor actual del hashmap
                                                int monto_nuevo = Premiados.get(key_premiados) + monto_premio;
                                                Premiados.replace(key_premiados, monto_viejo, monto_nuevo);
                                                total_premios = total_premios + monto_premio;
                                            } else {
                                                Premiados.put(key_premiados, value_premiados);
                                                total_premios = total_premios + monto_premio;
                                            }
                                        }
                                    }
                                }
                            }
                        }//fin del else desorden!!!
                    } else {
                        Log.v("Error12", "iterar_mapa: ningun tipo de loteria coinside!!");
                        //Do nothing. Nunca llega aqui.
                    }
                }
            }
        }

        for (String key : hashMap_local.keySet()) {
            monto_total = monto_total + hashMap_local.get(key);
            //msg("monto: " + monto_total);
        }

        if (monto_total == 0) {
            //Do nothing. Significa que no hay ventas para esta loteria.
        } else {

            //msg("monto: " + monto_total);
            /////////////////////////////   Arreglo para que la tabla se imprima bonita!   //////////////////////////////
            int espaciado = 30;
            espaciado = espaciado - loteria.length() - 1 - horario.length() - 2 - String.valueOf(monto_total).length();
            String espaciado_str = "";
            //msg("Espaciado: >" + espaciado_str + "<");
            for (int o = 0; o < espaciado; o++) {
                espaciado_str = espaciado_str + " ";
            }
            /////////////////////////////////////////////////////////////////////////////////////////////////////////////
            vendedor_a_presentar = vendedor_a_presentar + loteria + " " + horario + ": " + espaciado_str + monto_total + "\n";
            //msg(vendedor_a_presentar);
            String nombre_loteria = "ojo-rojo_ojo-rojo" + loteria + "ojo-rojo_ojo-rojo" + horario + "ojo-rojo_ojo-rojo" + comision + "ojo-rojo_ojo-rojo";
            if (Montos.containsKey(nombre_loteria)) {
                //Do nothing. Nunca deberia entrar aqui!!!
            } else {
                Montos.put(nombre_loteria, monto_total);
            }
        }
        llamar_al_cierre();
    }

    private HashMap<String, Integer> crear_hasmap_local_ordenado() {
        HashMap<String, Integer> local_ordenado = new HashMap<String, Integer>();
        return local_ordenado;
    }

    private void ver_vendedores() {
        if (verificar_internet()) {
            Ver_vendedores_online();
        } else {
            //Do nothing.
            msg("Debe estar conectado a una red de Internet!!!");
            return;
        }
    }

    private void Ver_vendedores_online() {

        RequestQueue requestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();

        String url = readRowURL_global;

        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String response) {
                        // Do something with the response

                        String SHEETT_info = "vendedor";
                        String[] splitt = response.split("\"");
                        int length_splitt = splitt.length;
                        if (response != null & length_splitt > 3) {
                            //msg(response);
                            if (splitt[1].equals(SHEETT_info)) {
                                //Respuesta es la correcta!
                                //msg(response);
                                //Aqui va el total de los algoritmos funcionales basados en una respuesta correcta!
                                String[] split = response.split("vendedor");//Se separa el objeto Json
                                String[] Vendedores = new String[split.length + 1];
                                VENDEDORES = new String[split.length + 1];
                                Vendedores[0] = "Elija el vendedor...";

                                //Se llena un HashMap y el spinner con los vendedores, los cuales se bajan de la nube.
                                for (int i = 1; i < split.length; i++) {
                                    String[] split2 = split[i].split("\"");
                                    //                       Ej.                  Chuz                              25
                                    String vendedor_act = "ojo-rojo_ojo-rojo" + split2[2] + "ojo-rojo_ojo-rojo" + split2[6] + "ojo-rojo_ojo-rojo";
                                    //                       Ej.                   SpreadSheet                    Sheet (loterias)                    SpreadSheet vendidas             Sheet (loterias_vendidas)
                                    String spreadsheet_act = "ojo-rojo_ojo-rojo" + split2[10] + "ojo-rojo_ojo-rojo" + split2[14] + "ojo-rojo_ojo-rojo" + split2[18] + "ojo-rojo_ojo-rojo" + split2[22] + "ojo-rojo_ojo-rojo";
                                    //vendedor =
                                    //crear_archivo_vendedor(split2[2], split2[6]);
                                    if (SpreadSheets.containsKey(spreadsheet_act)) {
                                        //Do nothing.
                                    } else {
                                        SpreadSheets.put(vendedor_act, spreadsheet_act);
                                        Vendedores[i] = split2[2] + " " + split2[6];
                                    }
                                }
                                Integer[] params = new Integer[2];
                                params[0] = 1000;
                                params[1] = 1;
                                Log.v("Error100", "llener spinner vendedores\n\nResponse:\n\n" + response);
                                //msg("Debug\nResponse: " + response);
                                new MyTask().execute(params);
                                Vendedores[split.length] = "Todos";
                                VENDEDORES = Vendedores;


                            } else {
                                Log.v("Error_ven_vend", "Respuesta incorrecta!!!" + "\nResponse: " + response);
                            }
                        } else {
                            Log.v("Error_ven_vend", "Respuesta incorrecta!!!" + "\nResponse: " + response);
                        }
                        Log.v("ver_vendedores_online", ".\nResponse:\n\n" + response + "\n\n.");
                        leer_premios();
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

    private void spinner_put() {
        String[] s = new String[1];
        s[0] = "Elija un vendedor...";
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, s);
        vendedores.setAdapter(adapter1);
    }

    private void crear_archivo_vendedor(String vendedor, String maquina) {
        String nombre_archivo_vendedor = vendedor + "_" + maquina;
        String archivos[] = fileList();
        if (ArchivoExiste(archivos, nombre_archivo_vendedor)) {
            //Do nothing.
        } else {//                                                         monto a la fecha de ayer. Inicia en cero.
            crear_archivo(nombre_archivo_vendedor);//                             split[1]
            agregar_linea_archivo(nombre_archivo_vendedor, "ojo-rojo_ojo-rojo0ojo-rojo_ojo-rojo");
        }
    }

    public void guardar(String Tcompleto, String nombre) {
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre, Activity.MODE_PRIVATE));
            archivo.write(Tcompleto);
            archivo.flush();
        } catch (IOException e) {
        }
    }

    public void borrar_archivo(String file) {
        File archivo = new File(file);
        String empty_string = "";
        guardar(empty_string, file);
        archivo.delete();
    }

    private void agregar_linea_archivo(String file_name, String new_line) {
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
        try {
            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput(nombre_archivo, Activity.MODE_PRIVATE));
            archivo.flush();
            archivo.close();
        } catch (IOException e) {
        }
    }

    private void presentar_vendedor(String vendedor, String maquina) {
        //iterar sobre el HashMap ...
        //vendedores.setVisibility(View.GONE);
        //letrero_spinner.setVisibility(View.GONE);
        if (vendedor.equals("Elija un vendedor...") | vendedor.equals("Elija el vendedor...")) {
            msg("Debe escoger un vendedor!");
            //vendedores.setVisibility(View.VISIBLE);
            //letrero_spinner.setVisibility(View.VISIBLE);
        } else if (vendedor.equals("Todos")) {
            //cuando se escogen todos! TODO: generar cierre!!!
            //vendedores.setVisibility(View.VISIBLE);
            //letrero_spinner.setVisibility(View.VISIBLE);
        } else {//Se ha elegido un vendedor!

            vendedor_a_presentar = "****** REPORTE CONTABLE ******\n\nTiempos " + vendedor + ", maquina " + maquina + "\nFecha: " + fecha + "/" + mes + "/" + anio + " Hora: " + hora + ":" + minuto + "\n\n";
            for (String key : SpreadSheets.keySet()) {
                //msg("key: " + key);
                String[] split4 = SpreadSheets.get(key).split("ojo-rojo_ojo-rojo");
                String[] split5 = key.split("ojo-rojo_ojo-rojo");
                if (vendedor.equals(split5[1]) && maquina.equals(split5[2])) {
                    if (verificar_internet()) {
                        Log.v("presentar_vendedor", ".\n\npresentar_vendedor.\nsplit4[1]: " + split4[1] + "Split4[2]" + split4[2] + "\n\n.");
                        ver_loterias_vendidas(split4[1], split4[2]);
                        //cuentas_vendedor(split4[1], split4[2]);
                    } else {
                        //Do nothing.
                    }
                }
            }
        }
    }

    private void ver_loterias_vendidas(String s1, String s2) {

        RequestQueue requestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();

        for (String key : SpreadSheets.keySet()) {
            //msg("key: " + key);
            String[] split4 = SpreadSheets.get(key).split("ojo-rojo_ojo-rojo");
            String[] split5 = key.split("ojo-rojo_ojo-rojo");
            if (vendedor.equals(split5[1]) && maquina.equals(split5[2])) {
                sid_vendidas = split4[3];
                s_vendidas = split4[4];
            }
        }

        String url = readRowURL_vendidas + sid_vendidas + "&sheet=" + s_vendidas;

        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String response) {
                        // Do something with the response
                        Log.v("ver_vendidas response", ".\nResponse:\n" + response);
                        String[] split_test = response.split("\"");
                        if (response != null & split_test.length > 3) {
                            String[] split = response.split("loteria");//Se separa el objeto Json
                            for (int i = 1; i < split.length; i++) {
                                String[] split2 = split[i].split("\"");

                                int key = i;
                                String horaritito = "";

                                if (split2[6].equals("Maniana")) {
                                    horaritito = "maniana";
                                } else if (split2[6].equals("Dia")) {
                                    horaritito = "dia";
                                } else if (split2[6].equals("Tarde")) {
                                    horaritito = "tarde";
                                } else if (split2[6].equals("Noche")) {
                                    horaritito = "noche";
                                } else {
                                    //Do nothing here.
                                }

                                String value = "ojo-rojo_ojo-rojo" + split2[2] + "ojo-rojo_ojo-rojo" + horaritito + "ojo-rojo_ojo-rojo";
                                saled_lots.put(key, value);
                                //Log.v("ver_loterias vendida ", ".\nLoteria: " + split2[2] + " " + split2[6]);
                                //Log.v("SpreadSheetId & Sheet ", ".\nSpreadSheetId: " + s1 + "\nSheet: " + s2);
                                //cuentas_vendedor(s1, s2);
                            }
                            Log.v("ver_loterias vendida ", ".\n\nResponse: \n\n" + response + "\n\n.");
                            Log.v("SpreadSheetId & Sheet ", ".\nSpreadSheetId: " + s1 + "\nSheet: " + s2);
                            cuentas_vendedor(s1, s2);
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


        //llamarlo en el onResponse
    }

    private void cuentas_vendedor(String sid, String s) {

        RequestQueue requestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();

        String url = readRowURL + sid + "&sheet=" + s;

        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String response) {
                        // Do something with the response
                        //ML_ver.setText(response);
                        Log.v("Error15", "Contar_vendedor\nResponse:\n" + response);
                        //msg(response);
                        String[] split_test = response.split("\"");
                        if (response != null & split_test.length > 3) {
                            //msg(response);
                            String[] split = response.split("loteria");//Se separa el objeto Json
                            //Se llena el String con los sids, los cuales se bajan de la nube.
                            for (int i = 1; i < split.length; i++) {
                                String[] split2 = split[i].split("\"");
                                if (split2[10].equals("maniana")) {
                                    //TODO: No se sabe que hacer aqui. Resolver metodos para ventas futuras
                                } else if (split2[10].equals("hoy")){
                                    //msg("Loterias: " + split2[2] + " " + split2[10]);

                                    //                  Ej.                  Tica                              dia                                hoy                            5 (comision)                         paga1                               paga2
                                    String lot_act = "ojo-rojo_ojo-rojo" + split2[2] + "ojo-rojo_ojo-rojo" + split2[6] + "ojo-rojo_ojo-rojo" + split2[10] + "ojo-rojo_ojo-rojo" + split2[22] + "ojo-rojo_ojo-rojo" + split2[26] + "ojo-rojo_ojo-rojo" + split2[30] + "ojo-rojo_ojo-rojo";
                                    String var_aux_comp = "ojo-rojo_ojo-rojo" + split2[2] + "ojo-rojo_ojo-rojo" + split2[6] + "ojo-rojo_ojo-rojo";//Variable auxiliar comparativa
                                    //                   Ej.                 SpreadSheet                     SHEET: loterias
                                    String spread_act = "ojo-rojo_ojo-rojo" + split2[14] + "ojo-rojo_ojo-rojo" + split2[18] + "ojo-rojo_ojo-rojo";


                                    for (int key : saled_lots.keySet()) {
                                        Log.v("Key and value", key + " and " + saled_lots.get(key) + "\n");
                                    }


                                    if (SpreadSheets_vendedor.containsKey(lot_act)) {
                                        Log.v("SpreadSheets_vendedor",  "contiene a " + lot_act);
                                        //Do nothing.
                                    } else {
                                        Log.v("SpreadSheets_vendedor",  "NO contiene a " + lot_act);
                                        if (saled_lots.containsValue(var_aux_comp)) {
                                            Log.v("saled_lots",  "contiene a " + var_aux_comp);
                                            SpreadSheets_vendedor.put(lot_act, spread_act);
                                        } else {
                                            Log.v("saled_lots",  "NO contiene a " + var_aux_comp);
                                            //No se guarda para que no se lean spreadsheets vacias.
                                        }
                                        //msg("Debug:\nSpreadSheet_vendedor: " + SpreadSheets_vendedor.get(lot_act));
                                    }
                                } else {
                                    //Do nothing here!
                                }
                            }
                            int cont = 0;
                            /*for (String key : SpreadSheets_vendedor.keySet()) {
                                cont++;
                                msg("Loteria: #" + cont + ":\n" + key + "\n\nHoja de Google: " + SpreadSheets_vendedor.get(key));
                            }*/
                            Log.v("error2", "Se va a llamar a la funcion generar_cierre por primera vez!!!");
                            String resul_cierre = null;

                            //Debug:
                            /*for (String key : SpreadSheets_vendedor.keySet()) {
                                msg("Llave: " + key + "\n\n" + "Valor: " + SpreadSheets_vendedor.get(key));
                            }*/


                            try {
                                bandera_continuar = "listo";//Esta bandera indica que si aparece vacio el hashmap
                                                            // SpreadSheets_vendedor, el programa debe detener la
                                                            // ejecucion de este metodo y continuar con un paso siguiente.
                                TOTAL_VENDEDORES = SpreadSheets_vendedor.size();
                                progressBar.setMax(TOTAL_VENDEDORES);
                                progressBar.setVisibility(View.VISIBLE);
                                progressBar.setProgress(0);
                                Log.v("cuentas_vendedor previo", "Antes de resul_cierre = generar_cierre(SpreadSheets_vendedor, true)");
                                resul_cierre = generar_cierre(SpreadSheets_vendedor, true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.v("cuentas_vendedorPositiv", "Resultado del llamado a generar_cierre: " + resul_cierre);
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
        //return retornar;
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

    private void llenar_spinner_vendedores(String[] Vendedores) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, Vendedores);
        vendedores.setAdapter(adapter);
    }

    private void msg (String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private boolean verificar_internet() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            //Toast.makeText(this, "Debe estar conectado a una red WiFi o datos mobiles.", Toast.LENGTH_LONG).show();
            return false;
        } else {
            //Si esta conectado a internet.
            //Toast.makeText(this, "Conectado a internet!", Toast.LENGTH_LONG).show();
            return true;
        }
    }

    private int calcular_monto_total() {//TODO: revisar que no haya errores
        int monto_total = 0;
        for (String key : Montos.keySet()) {
            monto_total = monto_total + Montos.get(key);
        }
        return monto_total;
    }

    private void Calcular_Total_Ventas() {
        int espaciado = 30;
        int monto_total = calcular_monto_total();
        espaciado = espaciado - 14 - String.valueOf(monto_total).length();
        String espaciado_str = "";
        //msg("Espaciado: >" + espaciado_str + "<");
        for (int o = 0; o < espaciado; o++) {
            espaciado_str = espaciado_str + " ";
        }
        vendedor_a_presentar = vendedor_a_presentar + "\nTOTAL VENTAS: " + espaciado_str + String.valueOf(monto_total) + "\n______________________________\n\n";

        //printIt(vendedor_a_presentar + "\n\n");
    }

    public int calcular_comision_vendedor() {
        int i = 0;//                         split[1]                        split[2]                        split[3]
        //String key = "ojo-rojo_ojo-rojo" + loteria + "ojo-rojo_ojo-rojo" + horario + "ojo-rojo_ojo-rojo" + comision + "ojo-rojo_ojo-rojo";

        for (String key : Montos.keySet()) {

            String[] split = key.split("ojo-rojo_ojo-rojo");
            //msg("Debug: \nComision: " + split[3] + "\nMonto venta: " + Montos.get(key));
            int comision = (Integer.parseInt(split[3])*Montos.get(key))/100;
            //msg("Debug: \nComision: " + String.valueOf(comision));
            comision_vendedor = comision_vendedor + comision;
            if (Comisiones.containsKey(key)) {
                //Do nothing. No deberia llegar aqui!!!
            } else {
                Comisiones.put(key, (int) comision);
            }
        }
        i = comision_vendedor;
        return i;
    }

    private boolean ArchivoExiste (String[] archivos, String Tiquet){
        for (int i = 0; i < archivos.length; i++) {

            if (Tiquet.equals(archivos[i])) {
                return true;
            }
        }
        return false;
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

    private String get_impresora() {
        String impresora = "00:11:22:33:44:55";
        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput("vent_active.txt"));
            //imprimir_archivo("facturas_online.txt");
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            Log.v("chequear no internet", ".\nLinea: " + linea + "\n\n");
            //Toast.makeText(this, "Debug:\nFuncion cambiar_bandera, linea:\n" + linea, Toast.LENGTH_LONG).show();
            String[] split = linea.split("_separador_");
            impresora = split[5];
            br.close();
            archivo.close();
        } catch (IOException e) {}
        return impresora;
    }

    public void printIt(String Mensaje) {

        if (dispositivo.equals("Celular")) {

            Intent Activity_ver = new Intent(this, VerActivity.class);
            Activity_ver.putExtra("mensaje", Mensaje);
            startActivity(Activity_ver);

        } else if (dispositivo.equals("Maquina")) {
            BluetoothSocket socket;
            socket = null;
            byte[] data = Mensaje.getBytes();

            //Get BluetoothAdapter
            BluetoothAdapter btAdapter = BluetoothUtil.getBTAdapter();
            if (btAdapter == null) {
                Toast.makeText(getBaseContext(), "Abriendo Bluetooth", Toast.LENGTH_SHORT).show();
                return;
            }
            // Get sunmi InnerPrinter BluetoothDevice
            String impresora = get_impresora();
            BluetoothDevice device = BluetoothUtil.getDevice(btAdapter, impresora);
            if (device == null) {
                Toast.makeText(getBaseContext(), "Asegurese de tener la impresora BlueTooth emparejada con el dispositivo!", Toast.LENGTH_LONG).show();
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

    private void llenar_spinner_vendedores2() {
        llenar_spinner_vendedores(VENDEDORES);
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

    class MyTask extends AsyncTask<Integer, Integer, String> {

        @Override
        protected void onPreExecute() {
            downloading.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            downloading.setText("Iniciando descarga...");
        }

        @Override
        protected String doInBackground(Integer... params) {
            //vendedores.setVisibility(View.GONE);
            //letrero_spinner.setVisibility(View.GONE);
            letrero_spinner.setVisibility(View.GONE);
            for (int count = 1; count <= params[0]; count = (count*2)) {
                try {
                    Thread.sleep(600/count);
                    publishProgress(count);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (params[1] == 0 | !verificar_internet()) {
                if (!verificar_internet()) {
                    return "Lectura de premios\nen progreso...\n\nDebe estar conectado a una red de Internet!!!";
                } else {
                    return "Lectura de premios\nen progreso...";
                }
            } else {
                return "Lectura de premios\nterminada!!!";
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //txt.setText("Running..."+ values[0]);
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            //progressBar.setVisibility(View.GONE);
            downloading.setText(result);
            letrero_spinner.setVisibility(View.GONE);
            //vendedores.setVisibility(View.VISIBLE);
            //letrero_spinner.setVisibility(View.VISIBLE);
            if (result.equals("Lectura de premios\nterminada!!!")) {
                llenar_spinner_vendedores2();
                letrero_spinner.setVisibility(View.VISIBLE);
                vendedores.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
            //TODO: Aqui se puede hacer algo interesante.
        }
    }
}




























