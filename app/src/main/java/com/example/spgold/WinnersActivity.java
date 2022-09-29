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
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.spgold.Util.BluetoothUtil;
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WinnersActivity extends AppCompatActivity {

    private String mes;
    private String anio;
    private String dia;
    private String hora;
    private String minuto;
    private String fecha;
    private String nume1 = "not_asigned";
    private String nume2 = "not_asigned";
    private String nume3 = "not_asigned";
    private String contenido = "";
    private String numero_gana;
    private TextView lot;
    private TextView hor;
    private TextView veces;
    private Spinner loteria;
    private Spinner horario;
    private Spinner paga;
    private String[] loterias;//Informacion que aparecera en el spinner de loterias
    private String[] horarios;//Informacion que aparecera en el spinner de horarios
    private String[] archivos_lot;
    private String[] paga_str_array;
    private TextView titulo;
    private TextView paga_x_veces;
    private EditText num_premio;
    private int total = 0;
    Map<String, String> loter = new HashMap<String, String>();
    private Map<String, Integer> meses = new HashMap<String, Integer>();
    private String dispositivo;
    private HashMap<String, String> abajos2 = new HashMap<String, String>();
    private String addRowURL = "https://script.google.com/macros/s/AKfycbweyYb-DHVgyEdCWpKoTmvOxDGXleawjAN8Uw9AeJYbZ24t9arB/exec";
    private String readRowURL = "https://script.google.com/macros/s/AKfycbxJNCrEPYSw8CceTwPliCscUtggtQ2l_otieFmE/exec?spreadsheetId=1iMXw4z0ljwvfhdR5BBmh586h1AOmNCWll7GYI1MJFbM&sheet=hoy";
    private String facturas_diarias = "facturas_diarias.txt";
    private String historial_facturas = "historial_facturas.txt";
    private String contabilidad = "contabilidad.txt";
    private String nombre_dia;

    @Override
    protected void onPause() {
        super.onPause();

        try {
            subir_facturas_resagadas();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            subir_facturas_resagadas();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winners);

        titulo = (TextView) findViewById(R.id.tv_repventas);
        lot = (TextView) findViewById(R.id.tv_ingrese_lot);
        hor = (TextView) findViewById(R.id.tv_ingrese_hor);
        loteria = (Spinner) findViewById(R.id.spinner_lot);
        horario = (Spinner) findViewById(R.id.spinner_hor);
        paga = (Spinner) findViewById(R.id.spinner_paga);
        num_premio = (EditText) findViewById(R.id.et_numwinn);
        paga_x_veces = (TextView) findViewById(R.id.textView_paga);
        veces = (TextView) findViewById(R.id.textView_3);

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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, loterias);
        loteria.setAdapter(adapter);

        //num_premio.setFocusableInTouchMode(true);

        num_premio.setFocusableInTouchMode(false);
        horario.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (horario.getSelectedItem().toString().equals("Escoja el horario...")) {
                            //msn2();
                        } else {
                            num_premio.setFocusableInTouchMode(true);
                            //TODO: Verificar la siguiente linea de codigo. Es un experimento de requestFocus
                            num_premio.requestFocus();
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
                        //Crear diccionario con la informacion de la loteria seleccionada
                        String seleccion = loteria.getSelectedItem().toString();
                        //num_premio.setText("");
                        //String valor_paga = paga.getSelectedItem().toString();
                        String num_str = num_premio.getText().toString();
                        if (seleccion.equals("Elija una loteria...")) {
                            //Do nothing!
                        }else {
                            crearDiccionario();//Meter aqui la loteria seleccionada en el spinner

                            //Condicionales

                            if (loter.get("Tipo_juego").equals("Monazos")) {
                                //Do nothing. Se hace en el listener del numero ganador!
                            } else if (loter.get("Tipo_juego").equals("Parley")) {
                                //Abrir nueva activity con 3 espacios para colocar numeros premiados de 2 digitos X 2
                                //No matar activity WinnersActivity para que cuando volvamos de la activity Parley to-do este como lo dejamos.
                                parleyActivity();

                            } else if (loter.get("Tipo_juego").equals("Reventados")) {
                                //Abrir activity con un spinner adicional para elegir color de la bolita.
                                //No matar activity WinnersActivity para que cuando volvamos de la activity Reventados to-do este como lo dejamos.
                                reventados_act();

                            } else if (loter.get("Tipo_juego").equals("Regular")) {
                                //Do nothing. Se hizo en el listener del numero ganador!
                            } else {
                                error_msm();
                            }

                            //Fin condicionales

                            //Agregar horarios al otro Spinner
                            crear_array_horarios();
                            llenar_spinner_hor();
                            llenar_spinner_paga();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

        //Implementacion de un text listener
        num_premio.setFocusableInTouchMode(false);
        num_premio.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //num_premio.setFocusableInTouchMode(false);
                if (loteria.getSelectedItem().toString().equals("Elija una loteria...")) {
                    select_lot_msg();
                    //num_premio.setFocusableInTouchMode(false);
                    //num_premio.setText("");
                    ocultar_teclado();
                } else if (horario.getSelectedItem().toString().equals("Escoja el horario")) {
                    select_hor_msg();
                    //num_premio.setFocusableInTouchMode(false);
                    //num_premio.setText("");
                    ocultar_teclado();
                } else {
                    if (loter.get("Tipo_juego").equals("Monazos")) {
                        //num_premio.setFocusableInTouchMode(true);
                        if (s.length() == 3) {
                            ocultar_teclado();

                            //if (ArchivoExiste(archivos, "premios" + "x_y" + loteria.getSelectedItem().toString() + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt")) ;

                            /*
                            try {
                                OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput("premios" + "x_y" + loteria.getSelectedItem().toString() + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt", Activity.MODE_PRIVATE));
                                String ArchivoCompleto = "";
                                archivo.write(ArchivoCompleto);
                                archivo.flush();
                            } catch (IOException e) {
                            }
                            File file = new File("premios" + "x_y" + loteria.getSelectedItem().toString() + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt");
                            file.delete();
                            agregar_linea_archivo("premios" + "x_y" + loteria.getSelectedItem().toString() + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt", num_premio.getText().toString());
                            //imprimir_archivo("premios" + "x_y" + loteria.getSelectedItem().toString() + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + ".txt");
                             */
                            numero_gana = num_premio.getText().toString();

                        }
                    } else if (loter.get("Tipo_juego").equals("Parley")) {
                        //Abrir nueva activity con 3 espacios para colocar numeros de 2 digitos. (Ya se hizo en otra parte). Donde? En el listener de loteria. jejeje
                        //No matar activity WinnersActivity para que cuando volvamos de la activity Parley to-do este como lo dejamos.
                    } else if (loter.get("Tipo_juego").equals("Reventados")) {
                        //Abrir activity con un spinner adicional para elegir color de la bolita.
                        //No matar activity WinnersActivity para que cuando volvamos de la activity Reventados to-do este como lo dejamos.
                    } else if (loter.get("Tipo_juego").equals("Regular")) {
                        //num_premio.setFocusableInTouchMode(true);
                        if (s.length() == 2) {
                            ocultar_teclado();
                            /*if (ArchivoExiste(archivos, "premios" + "x_y" + loteria.getSelectedItem().toString() + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt")) ;

                            try {
                                OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput("premios" + "x_y" + loteria.getSelectedItem().toString() + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt", Activity.MODE_PRIVATE));
                                String ArchivoCompleto = "";
                                archivo.write(ArchivoCompleto);
                                archivo.flush();
                            } catch (IOException e) {
                            }
                            File file = new File("premios" + "x_y" + loteria.getSelectedItem().toString() + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt");
                            file.delete();
                            agregar_linea_archivo("premios" + "x_y" + loteria.getSelectedItem().toString() + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt", num_premio.getText().toString());
                            //imprimir_archivo("premios" + "x_y" + loteria.getSelectedItem().toString() + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt");
                             */
                            numero_gana = num_premio.getText().toString();
                        }
                    } else {
                        //error_msm();
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        paga.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        //Crear diccionario con la informacion de la loteria seleccionada
                        //String seleccion = loteria.getSelectedItem().toString();
                        String valor_paga = paga.getSelectedItem().toString();
                        //String num_str = num_premio.getText().toString();
                        if (Integer.parseInt(valor_paga) > 0) {
                            msn(valor_paga, Integer.parseInt(valor_paga));
                            /*if (loter.get("Loteria").equals("Monazos")) {
                                if (valor_paga.equals(loter.get("Paga1"))) {
                                    paga_x_veces.setText("Orden");
                                } else if (valor_paga.equals(loter.get("Paga2"))) {
                                    paga_x_veces.setText("Desorden");
                                } else {
                                    //Do nothing.
                                }
                            }*/
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

        try {
            subir_facturas_resagadas();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void cambiar_bandera (String Consecutivo, String tag) {
        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput("facturas_online.txt"));
            //imprimir_archivo("facturas_online.txt");
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();

            String contenido = "";
            while (linea != null) {
                //String[] split_perreo = linea.split(" ");
                Log.v("cambiar_bandera_fact", "  Linea: " + linea + "\nTag: " + tag + "\n\n");
                //Toast.makeText(this, "Debug:\nFuncion cambiar_bandera, linea:\n" + linea, Toast.LENGTH_LONG).show();
                String[] split = linea.split(" ");
                String[] split_perreo = split[1].split("_separador_");
                if (split_perreo[14].equals("equi.txt")) {
                    Log.v("Cambiar_band_equi", " Se ha intentado cambiar bandera a archivo \"equi.txt\"\nLinea: " + linea + "\n\n               Tag: " + tag + "\n\n");
                    //Do nothing for now!! maybe later do a TODO.
                    if (tag.equals("equi")) {
                        if (split[0].equals("BORRADA")) {
                            //TODO: Pensar que hacer!!!
                        } else if (split[0].equals("abajo")) {
                            String[] split_name = split[1].split("_separador_");
                            String factura = split_name[6];// split_name[6] contiene el numero de la factura que se desea subir.
                            if (factura.equals(Consecutivo)) {
                                linea = linea.replace("abajo", "arriba");
                                contenido = contenido + linea + "\n";
                            } else {
                                contenido = contenido + linea + "\n";
                            }
                        } else if (split[0].equals("arriba")) {
                            //No hacer nada garantiza que se borra la linea que ya esta arriba
                        } else {
                            //Do nothing. No deberia llegar aqui.
                        }
                    } else {
                        contenido = contenido + linea + "\n";
                    }

                } else {
                    Log.v("Cambiar_band_null.txt", " Se intentara cambiar bandera a archivo \"null.txt\"\nLinea: " + linea);
                    if (tag.equals("BORRADA")) {
                        if (split[0].equals("BORRADA")) {
                            //Do nothing guaranties that the line will be erased
                        } else if (split[0].equals("abajo")) {
                            String[] split_name = split[1].split("_separador_");
                            String factura = split_name[6];// split_name[6] contiene el numero de la factura que se desea subir.
                            if (factura.equals(Consecutivo)) {
                                linea = linea.replace("abajo", tag);
                                contenido = contenido + linea + "\n";
                            } else {
                                contenido = contenido + linea + "\n";
                            }
                        } else if (split[0].equals("arriba")) {
                            /*String[] split_name = split[1].split("_separador_");
                            String factura = split_name[6];// split_name[6] contiene el numero de la factura que se desea subir.
                            if (factura.equals(Consecutivo)) {
                                linea = linea.replace("arriba", tag);
                                contenido = contenido + linea + "\n";
                            } else {
                                contenido = contenido + linea + "\n";
                            }*/
                            //No hacer nada garantiza el borrado de la linea que contiene "arriba"
                        } else {
                            //Do nothing. No deberia llegar aqui.
                        }
                    } else {
                        if (split[0].equals("BORRADA")) {
                            //No hacer nada para eliminar la linea!
                        } else if (split[0].equals("abajo")) {
                            String[] split_name = split[1].split("_separador_");
                            String factura = split_name[6];// split_name[6] contiene el numero de la factura que se desea subir.
                            if (factura.equals(Consecutivo)) {
                                linea = linea.replace("abajo", "arriba");
                                contenido = contenido + linea + "\n";
                            } else {
                                contenido = contenido + linea + "\n";
                            }
                        } else if (split[0].equals("arriba")) {
                            //No hacer nada para eliminar la linea!
                        } else {
                            //Do nothing. No deberia llegar aqui.
                        }
                    }
                }
                linea = br.readLine();
            }

            br.close();
            archivo.close();
            borrar_archivo("facturas_online.txt");
            //imprimir_archivo("facturas_online.txt");
            guardar(contenido, "facturas_online.txt");//Aqui se eliminan las lineas que corresponden a archivos que ya se han subido.
            Log.v("cambiar_band_result", "\n\nArchivo \"facturas_online.txt\":\n\n" + imprimir_archivo("facturas_online.txt"));
            //imprimir_archivo("facturas_online.txt");

        } catch (IOException e) {
        }
    }

    private void agregar_fact_online(String file, String spid, String sheet, String tip_lot) {
        String linea_agrgar = "abajo " + file + " " + spid + " " + sheet + " " + tip_lot;//agregar_linea_archivo("facturas_online.txt", "abajo " + file + " " + SPREADSHEET_ID + " " + SHEET + " " + tipo_lot);
        Log.v("Error800", "Agregar a facturas_online.txt :\n\n" + imprimir_archivo(file));
        agregar_linea_archivo("facturas_online.txt", linea_agrgar);
        Log.v("Error111", "SpreadSheet ID: " + spid + "\nSheet: " + sheet + "\nTipo lot: " + tip_lot + "\nFile name: " + file);
        //msg("Error111 SpreadSheet ID: " + spid + "\nSheet: " + sheet + "\nTipo lot: " + tip_lot + "\nFile name: " + file);
        //debug
        //imprimir_archivo(file);
        Log.v("Error110", "facturas_online.txt:\n\n" + imprimir_archivo("facturas_online.txt"));
        //msg("Error110\n" + imprimir_archivo(imprimir_archivo(file)));
    }

    private void subir_facturas_resagadas() throws JSONException {
        boolean flag_internet = verificar_internet();
        //JSONObject objeto_Json_a_subir = null;
        if (flag_internet) {
            //ocultar_todo();
            obtener_Json_otras_facturas();
        } else {
            //Toast.makeText(this, "Verifique su coneccion a Internet!!!", Toast.LENGTH_LONG).show();
        }
    }

    private void obtener_Json_otras_facturas() throws JSONException {

        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput("facturas_online.txt"));
            //imprimir_archivo("facturas_online.txt");
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            //String contenido = "";
            abajos2.clear();
            Integer countercito = 0;
            boolean flag = true;
            while (linea != null) {
                countercito++;
                String count = String.valueOf(countercito);
                String[] split = linea.split(" ");
                if (split[0].equals("abajo")) {
                    Log.v("OJOF_abajo: ", "\n\nLinea: " + linea + " Fin de linea!!!");
                    abajos2.put(count, split[1]);
                    flag = false;
                } else if (split[0].equals("BORRADA")) {
                    Log.v("OJOF_BORRADA: ", "\n\nLinea: " + linea + " Fin de linea!!!");
                    //TODO: Pensar que hacer!!!
                } else if (split[0].equals("arriba")) {
                    Log.v("OJOF_arriba: ", "\n\nLinea: " + linea + " Fin de linea!!!");
                    //TODO: Pensar que hacer!!!
                } else {
                    Log.v("OJOF_(error): ", "\n\n(No deberia llegar aqui!!!\n\nLinea: " + linea + " Fin de linea!!!");
                    //Do nothing.
                }
                linea = br.readLine();
            }

            if (flag) {
                //mostrar_todo();
                return;
            } else {
                //Do nothing. Continue with the work
            }

            archivo.close();
            br.close();

        } catch (IOException e) {
        }

        abajiar();
        //return objeto_json;
    }

    private void abajiar() throws JSONException {

        //ocultar_todo();

        for (String key : abajos2.keySet()) {
            JSONObject objeto_json = new JSONObject();
            String SSHHEETT = "";
            String SSPPRREEAADDSSHHEETT = "";
            String tipo_lot = "";
            String factura = "";
            String file = abajos2.get(key);
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(facturas_diarias));
                //imprimir_archivo("facturas_online.txt");
                BufferedReader br = new BufferedReader(archivo);
                String linea = br.readLine();

                while (linea != null) {
                    String[] split = linea.split("      ");
                    if (file.equals(split[3])) {
                        String[] split_name = file.split("_separador_");
                        Log.v("ERROR9001_facturas", "Linea leida: " + linea);
                        if (linea.equals("BORRADA")) {
                            Log.v("abajiar_linea_borrada", "\n\nLinea: " + linea);
                            //Do nothing.
                        } else if (linea.isEmpty()) {
                            Log.v("abajiar_linea_empty", "\n\nLinea: " + linea);
                            //Do nothing.
                        } else if (split_name[14].equals("equi.txt")) {
                            Log.v("abajiar_nombre_equi.txt", "\n\nLinea: " + linea);
                            tipo_lot = split_name[9];
                            SSPPRREEAADDSSHHEETT = split[1];
                            SSHHEETT = split[2];
                            factura = split_name[6];
                            //file = abajos2.get(key);
                            Log.v("Errequilibrar_facturas", "\n\nFinal del nombre del archivo: " + split_name[14] + "\n\nTipo loteria: " + tipo_lot + "\nSpreadSheet: " + SSPPRREEAADDSSHHEETT + "\nSheet: " + SSHHEETT + "\nFactura numero: " + factura + "file: " + file);
                            br.close();
                            archivo.close();
                            equilibrar(SSPPRREEAADDSSHHEETT, SSHHEETT, file, factura, tipo_lot, key);
                            break;
                        } else if (split_name[14].equals("null.txt")) {
                            Log.v("abajiar_nombre_null.txt", "\n\nLinea: " + linea);
                            tipo_lot = split_name[9];
                            SSPPRREEAADDSSHHEETT = split[1];
                            SSHHEETT = split[2];
                            factura = split_name[6];
                            //file = abajos2.get(key);\n
                            Log.v("Error9003_pre", "\n\nTipo_lot: " + tipo_lot + "\nSpreadSheet: " + SSPPRREEAADDSSHHEETT + "\nSheet: " + SSHHEETT + "\nFactura numero: " + factura + "\n\n.");
                            objeto_json = generar_Json_resagadas(file, factura, SSHHEETT, SSPPRREEAADDSSHHEETT, tipo_lot);
                            Log.v("Error9003_facturas", "\n\nTipo loteria: " + tipo_lot + "\nSpreadSheet: " + SSPPRREEAADDSSHHEETT + "\nSheet: " + SSHHEETT + "\nFactura numero: " + factura + "file: " + file);
                            abajos2.remove(key);
                            br.close();
                            archivo.close();
                            subir_factura_resagadas(objeto_json, "nothing");
                            break;
                        } else {
                            Log.v("abajiar_ninguna_opcion", " Error!!! Nunca deberia llegar aqui!!!\n\nLinea: " + linea);
                            //Do nothing.
                        }
                    }
                    linea = br.readLine();
                }

                br.close();
                archivo.close();
            } catch (IOException e) {

            }
        }
        //mostrar_todo();
    }

    private void subir_factura_resagadas(JSONObject jsonObject, String tag) throws JSONException {
        //flag_file_arriba = false;

        //msg("Json: 123 " + jsonObject);

        RequestQueue queue;
        queue = Volley.newRequestQueue(this);

        //Debug:
        //mensaje_confirma_subida(jsonObject.toString());

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

        //Toast.makeText(this, "Debug:\nConsecutivo: " + Consecutivo + "\nconsecutivo: " + consecutivo + "\nDeben ser iguales.", Toast.LENGTH_LONG).show();

        String url = addRowURL;

        //ocultar_todo();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String[] split = response.toString().split("\"");
                        //msg("Json: 123 " + response.toString());
                        int length_split = split.length;
                        Log.v("info_sub_fact_resag: ", "\n\n" + response + "\n\n");
                        if (length_split > 3) {
                            if (split[2].equals(":")) {
                                //mensaje_confirma_subida(response.toString());
                                String factura_num = split[15];

                                //mensaje_confirma_subida("factura #" + factura_num + " se ha subido correctamente!");
                                cambiar_bandera (factura_num, tag);
                                //mostrar_todo();
                                try {
                                    abajiar();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                String factura_num = split[15];
                                //mensaje_confirma_subida("Factura #" + factura_num + " no se ha subido!");
                            }
                        } else {
                            //No se subio correctamente!
                            String factura_num = split[15];
                            //mensaje_confirma_subida("Factura #" + factura_num + " no se ha subido!");
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        //mensaje_error_en_subida();

                    }
                });

        // Add the request to the RequestQueue.
        requestQueue.add(jsonObjectRequest);
        //Toast.makeText(this, "Debug:\nBandera flag_file_arriba (antes del return): " + String.valueOf(flag_file_arriba), Toast.LENGTH_LONG).show();

        //Toast.makeText(this, "Debug:\nEste mensaje debe aparecer despues del mensaje de funcion cambiar_bandera.\nSi aparece antes es que no es sincronico!!!", Toast.LENGTH_LONG).show();

    }

    private JSONObject generar_Json_resagadas(String file, String factura, String SSHHEETT, String SPREEADSHEET_ID, String tipo_lote) {
        //boolean flag_subir = false;
        JSONObject jsonObject = new JSONObject();

        //Debug:
        //imprimir_archivo(file);
        boolean flagsita = true;

        try {
            InputStreamReader archivo = new InputStreamReader(openFileInput(facturas_diarias));
            BufferedReader br = new BufferedReader(archivo);
            String linea = br.readLine();
            String json_string = "";
            Log.v("facturas_diarias_Json", ".\n\nLinea leida:\n\n" + linea + "\n\n.");
            while (linea != null) {
                String[] split = linea.split("      ");
                if (split[3].equals(file)) {
                    String information = split[0];
                    if (split[0].equals("BORRADA")) {
                        Log.v("ErrorBorrada", "Factura esta borrada, no hacer nada!!!");
                        flagsita = false;
                    } else if (tipo_lote.equals("Regular") | tipo_lote.equals("Reventados")) {
                        Log.v("Regular_reventados", ".\n\nLinea:\n\n" + linea + "\n\n.");
                        String[] split2 = information.split("__");
                        for (int i = 0; i < split2.length; i++) {
                            String[] split3 = split2[i].split("_");
                            //                            #1                 #2              monto          ext. info         factura
                            json_string = json_string + split3[0] + "_n_" + "no" + "_n_" + split3[1] + "_n_" + "no" + "_n_" + factura + "_l_";
                        }
                    } else if (tipo_lote.equals("Monazos")) {
                        String[] split2 = information.split("__");
                        for (int i = 0; i < split2.length; i++) {
                            String[] split3 = split2[i].split("_");
                            //                            #1                 #2             monto              ext. info           factura
                            json_string = json_string + split3[0] + "_n_" + "no" + "_n_" + split3[2] + "_n_" + split3[1] + "_n_" + factura + "_l_";
                        }
                    } else if (tipo_lote.equals("Parley")) {
                        String[] split2 = information.split("__");
                        for (int i = 0; i < split2.length; i++) {
                            String[] split3 = split2[i].split("_");
                            //                            #1                  #2                 monto           ext. info         factura
                            json_string = json_string + split3[0] + "_n_" + split3[1] + "_n_" + split3[2] + "_n_" + "no" + "_n_" + factura + "_l_";
                        }
                    } else {
                        //Do nothing. Nunca llega aqui!
                    }
                } else {
                    //Do nothing. Continue.
                }
                linea = br.readLine();
            }
            br.close();
            archivo.close();
            if (flagsita) {
                jsonObject = TranslateUtil.string_to_Json(json_string, SPREEADSHEET_ID, SSHHEETT, factura);
            }
            else {
                Log.v("Error21", "Factura ha sido borrada!!!");
            }
        } catch (IOException | JSONException e) {
        }
        return jsonObject;
    }

    private void equilibrar(String SpreadSheet, String Sheet, String file, String factura, String tipo_lot, String key) {//Este metodo revisa si se ha subido parte del tiquete a la nube.
        RequestQueue requestQueue;//Se llama a la SpreadSheet que contiene la loteria actual para verificar que no hay errores en la subida de datos. Usar: Method.get
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        requestQueue = new RequestQueue(cache, network);

        // Start the queue
        requestQueue.start();

        String readRowURL = "https://script.google.com/macros/s/AKfycbxJNCrEPYSw8CceTwPliCscUtggtQ2l_otieFmE/exec?spreadsheetId=" + SpreadSheet +"&sheet=" + Sheet;

        String url = readRowURL;

        //ocultar_todo();

        // Formulate the request and handle the response.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String response) {
                        // Do something with the response
                        //ML_ver.setText(response);
                        //msg(response);

                        //HashMap<String, String> premios = new HashMap<String, String>();
                        //msg("Response: " + response);
                        if (response != null) {
                            //response.replace("loteria", "_sepa_");

                            //String[] split = response.split("loteria");//Se separa el objeto Json
                            //TODO: Algoritmo que revisa la spreadsheet para ver si se ha subido de manera parcial un tiquete cualquiera, pero que no se ha terminado de subir, por lo tanto aparece como "abajo"
                            Log.v("Equilibrar onResponse", " Response: \n\n" + response);
                            //debug:
                            //msg("Response: (Debe ser toda la SpreadSheet a la cual se ha subido el tiquete anterior, mostrado en \"Atencion1\"\n\n" + response);
                            //TODO: Se debe modificar el archivo "file" con la informacion de numeros negativizados encontrados en la spreadsheet con el mismo numero de factura.

                            boolean flagsitilla = false;

                            String[] split = response.split("numero1");//Se separa el objeto Json "response"
                            //Se llena un HashMap local con las ventas, las cuales se bajan de la nube.
                            HashMap<String, String> hashMap = new HashMap<String, String>();
                            int new_monto = 0;
                            for (int i = 1; i < split.length; i++) {//En este loop se filtran los valores repetidos en la tabla.

                                String[] split2 = split[i].split("\"");
                                String nu1 = split2[2];
                                String nu2 = split2[6];
                                String monto = split2[10];//10
                                String extra_info = split2[14];//14
                                String factura_leida = split2[18];//Numero de factura
                                Log.v("Equilibrar fact_leida", "\n\nFactura leida: " + factura_leida);
                                String iD = split2[22];
                                Log.v("E6", "split[18]: " + split2[18] + " split[14]: " + split2[14]);
                                String key_factura = "ojo-rojo_ojo-rojo" + nu1 + "ojo-rojo_ojo-rojo" + nu2 + "ojo-rojo_ojo-rojo" + extra_info + "ojo-rojo_ojo-rojo" + iD + "ojo-rojo_ojo-rojo" + monto + "ojo-rojo_ojo-rojo";
                                String valor_factura = factura_leida;//numero de factura
                                //Debug:
                                //msg("Key: " + key_factura + "\nValue: " + valor_factura);

                                //////////////////////////////////////////////////////////////////////////
                                ///////Algoritmo que verifica si hay IDs iguales para omitirlos!//////////
                                //////////////////////////////////////////////////////////////////////////
                                if (hashMap.containsKey(key_factura)) {
                                    Log.v("equilibrar ID repetido", "\n\nID: " + iD + ". (No se hace nada!)\n\n");
                                    //Do nothing!
                                    //Si llega aqui significa que ha encontrado una linea repetida en la spreadsheet, por lo tanto la omite.
                                } else {//   numeros jugados  monto jugado

                                    int un_fact = Integer.parseInt(factura);
                                    int dos_fact = Integer.parseInt(factura_leida);
                                    Log.v("equilibrar en el for", "\n\nFactura: " + un_fact + " Factura leida: " + dos_fact);
                                    if (un_fact == dos_fact) {
                                        new_monto = new_monto + Integer.parseInt(monto);
                                        hashMap.put(key_factura, valor_factura);//Lo que hay en este hashMap es la informacion que se pudo haber subido de manera parcial y haber dejado el mensaje: "abajo"
                                    } else {
                                        //Do nothing.
                                    }
                                }
                                //////////////////////////////////////////////////////////////////////////
                            }

                            //borrar_archivo(file);

                            String[] splityto = file.split("_separador_");
                            String factoura = String.valueOf((Integer.parseInt(splityto[6])) * -1);
                            String montitito = String.valueOf(new_monto * -1);
                            String new_name = splityto[0] + "_separador_" + splityto[1] + "_separador_" + splityto[2] + "_separador_" + splityto[3] + "_separador_" + splityto[4] + "_separador_" + splityto[5] + "_separador_" + factoura + "_separador_" + splityto[7] + "_separador_" + splityto[8] + "_separador_" + splityto[9] + "_separador_" + splityto[10] + "_separador_" + splityto[11] + "_separador_" + montitito + "_separador_" + splityto[13] + "_separador_" + "null.txt";
                            //crear_archivo(new_name);
                            //agregar_linea_archivo("facturas_online.txt", "abajo " + new_name + " " + SpreadSheet + " " + Sheet + " " + tipo_lot);
                            //Se hace que file sea un archivo igual a cualquier factura para subirla. Se guarda la informacion necesaria en el file.
                            String linea_leida = "";
                            for (String key : hashMap.keySet()) {
                                Log.v("E0 for hashMap", "\nKey: " + key + "\nValue: " + hashMap.get(key) + "\ntipo_lot: " + tipo_lot + "\n");
                                String[] splity = key.split("ojo-rojo_ojo-rojo");
                                //msg("Factura: " + key + "\nValor: " + hashMap.get(key) + "\n");
                                int otnom = Integer.parseInt(splity[5]) * -1;
                                if (tipo_lot.equals("Monazos")) {
                                    linea_leida = linea_leida + splity[1] + "_" + String.valueOf(otnom) + "_" + splity[3] + "__";
                                    //agregar_linea_archivo(new_name, splity[1] + "      " + String.valueOf(otnom) + "      " + splity[3] + "      " + SpreadSheet + "      " + Sheet);
                                    flagsitilla = true;
                                } else if(tipo_lot.equals("Parley")) {
                                    linea_leida = linea_leida + splity[1] + "_" + splity[2] + "_" + String.valueOf(otnom) + "__";
                                    //agregar_linea_archivo(new_name, splity[1] + "      " + splity[2] + "      " + String.valueOf(otnom) + "      " + SpreadSheet + "      " + Sheet);
                                    flagsitilla = true;
                                } else if (tipo_lot.equals("Reventados")) {
                                    linea_leida = linea_leida + splity[1] + "_" + String.valueOf(otnom) + "__";
                                    //agregar_linea_archivo(new_name, splity[1] + "      " + String.valueOf(otnom) + "      " + SpreadSheet + "      " + Sheet);
                                    flagsitilla = true;
                                } else if (tipo_lot.equals("Regular")) {
                                    linea_leida = linea_leida + splity[1] + "_" + String.valueOf(otnom) + "__";
                                    flagsitilla = true;
                                } else {
                                    //Do nothing.
                                }
                            }
                            String fecha_invoice = anio + mes + fecha + "_" + nombre_dia;
                            String linea_escribir = linea_leida + "      " + SpreadSheet + "      " + Sheet + "      " + file + "      " + fecha;
                            String linea_escribir2 = linea_leida + "      " + SpreadSheet + "      " + Sheet + "      " + file + "      " + fecha_invoice;
                            agregar_linea_archivo(facturas_diarias, linea_escribir);
                            agregar_linea_archivo(historial_facturas, linea_escribir2);

                            if (flagsitilla) {
                                //agregar_linea_archivo("facturas_online.txt", "abajo " + new_name + " " + SpreadSheet + " " + Sheet + " " + tipo_lot);//Se hace que file sea un archivo igual a cualquier factura para subirla. Se guarda la informacion necesaria en el file.
                                agregar_fact_online(new_name, SpreadSheet, Sheet, tipo_lot);
                                Log.v("flagsitilla: ", "se cambiara la bandera de la factura # " + String.valueOf((Integer.parseInt(splityto[6]))));
                                cambiar_bandera(String.valueOf((Integer.parseInt(splityto[6]))), "equi");
                                //Aqui se crea un tiquete con algun tipo de id que sirva para equilibrar alguna factura que se subio de manera parcial.
                                int factur = Integer.parseInt(factura) * -1;
                                JSONObject objeto_json = generar_Json_resagadas(new_name, String.valueOf(factur), Sheet, SpreadSheet, tipo_lot);
                                abajos2.remove(key);
                                //cambiar_bandera(String.valueOf(factur), "equi");
                                Log.v("Error9003_facturas", "\n\nTipo loteria: " + tipo_lot + "\nSpreadSheet: " + SpreadSheet + "\nSheet: " + Sheet + "\nFactura numero: " + factura + "file: " + new_name);
                                try {
                                    subir_factura_resagadas(objeto_json, "equi");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            } else {
                                Log.v("equilibrar flagsitilla", "Todo or do nothing! I don't know right now :-|");
                                //Todo or do nothing! I don't know right now :-|
                            }
                            //cambiar_bandera(String.valueOf(factura), "equi");
                            //cambiar_bandera(String.valueOf(factura), "equi");

                        } else {
                            Log.v("Error de respuesta", ".\nResponse:\n--> " + response + " <--\n\n.");
                            //Respuesta es null. No deberia pasar nunca.
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

    private void select_lot_msg () {
        Toast.makeText(this, "Debe seleccionar una loteria", Toast.LENGTH_SHORT).show();
    }

    private void select_hor_msg () {
        Toast.makeText(this, "Debe seleccionar un horario valido", Toast.LENGTH_SHORT).show();
    }

    private void parleyActivity(){
        Intent Parley = new Intent(this, ParleyActivity.class);
        Parley.putExtra("Loteria", loteria.getSelectedItem().toString());
        startActivity(Parley);
        finish();
        System.exit(0);
    }

    private void reventados_act() {
        Intent Reventados = new Intent(this, ReventadosActivity.class);
        Reventados.putExtra("Loteria", loteria.getSelectedItem().toString());
        startActivity(Reventados);
        finish();
        System.exit(0);
    }

    private void error_msm() {
        Toast.makeText(this, "Error!!! Llame a soporte tecnico. \nTelefono: (506)85258108", Toast.LENGTH_LONG).show();
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
    //##############################################################################################################
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
    //###############################################################################################################

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
        nombre_dia = split[0];
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

    private void msg(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

    private void generar_tiquete(String file, int Winner_num, String cliente) {
        //Aqui se revisa cada tiquete por si tiene premio.
        //contenido = "";
        String Loteria = loteria.getSelectedItem().toString();
        String Horario = horario.getSelectedItem().toString();


        try {
            InputStreamReader archivo24 = new InputStreamReader(openFileInput(file));//Se abre el archivo
            BufferedReader br24 = new BufferedReader(archivo24);
            String linea = br24.readLine();//Se lee archivo
            Pattern pattern = Pattern.compile("BORRADA", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(linea);//Se verifica si la factura se ha borrado
            String seleccion_premio = paga.getSelectedItem().toString();
            //Toast.makeText(this, "Linea: " + linea + "\nSeleccion premio: " + seleccion_premio, Toast.LENGTH_LONG).show();
            boolean matchFound = matcher.find();
            if (matchFound){
                //Toast.makeText(this, "Se ha borrado esta factura!!!", Toast.LENGTH_LONG).show();
                return;
            }
            while (linea != null){

                //Toast.makeText(this, "La linea aun no es nula!!!", Toast.LENGTH_LONG).show();
                boolean flag_1 = false, flag_2 = false, flag_3 = false, flag_11 = false, flag_12 = false, flag_13 = false, flag_21 = false, flag_22 = false, flag_23 = false;
                String[] split = linea.split("      ");//Se separa el monto del numero guardado, tipo de premio y si esta en orden o en desorden en caso de ser Monazos.
                int pagar = 0;

                if (loter.get("Tipo_juego").equals("Monazos")) {
                    if (split[2].equals("Orden")) {
                        pagar = Integer.parseInt(loter.get("Paga1"));
                    } else if (split[2].equals("Desorden")) {
                        pagar = Integer.parseInt(loter.get("Paga2"));
                    } else {
                        //Do nothing. Nunca debe llegar aqui.
                    }
                } else {
                    pagar = Integer.parseInt(paga.getSelectedItem().toString());
                }

                if (split[0].isEmpty()){
                    break;//      numero jugado        numero ganador
                }else if (Integer.parseInt(split[0]) == Winner_num){ //si numero jugado es igual a numero ganador, se ha ganado un premio
                    if (loter.get("Tipo_juego").equals("Monazos")) {
                        //Toast.makeText(this, "Coinside monazos" + "\n" + "split[0]: " + split[0] + "\nSplit[1]: " + split[1] + "\nSplit[2]: " + split[2], Toast.LENGTH_LONG).show();
                        //Aqui captura los numeros que ganan en orden. Aunque tambien puede capturar numeros jugdos en desorden.

                        //Se debe comentar la siguiente linea cuando se tenga rollos
                        //contenido = contenido + "###############################\nPremio encontrado!!!\nCliente: " + cliente + "\nNumero ganador: " + split[0] + " " + split[2] + "\n" + split[1] + " X " + pagar + " = " + String.valueOf(pagar * Integer.parseInt(split[1])) + " colones. \n###############################\n";
                        //Se debe des-comentar la siguiente linea cuando se tenga rollos.
                        String cliente_print = cliente.replace("x_x"," ");
                        contenido = contenido + "\n\n################################\nPremio encontrado!!!\nCliente: " + cliente_print + "\nNumero ganador: " + split[0] + " " + split[2] + "\n\n" + split[1] + " X " + pagar + " = " + String.valueOf(pagar * Integer.parseInt(split[1])) + " colones. \n################################\n\n";
                        total = total + (pagar * Integer.parseInt(split[1]));
                    } else {
                        //Toast.makeText(this, "Coinside otros que no son monazos!" + "\n" + "split[0]: " + split[0] + "\nSplit[1]: " + split[1], Toast.LENGTH_LONG).show();
                        //Se debe comentar la siguiente linea cuando se tenga rollos.
                        //contenido = contenido + "################################\nPremio encontrado!!!\nCliente: " + cliente + "\nNumero ganador: " + num_premio.getText().toString() + "\n" + split[1] + " X " + paga.getSelectedItem().toString() + " = " + String.valueOf(Integer.parseInt(paga.getSelectedItem().toString()) * Integer.parseInt(split[1])) + " colones. \n################################\n";
                        //Se debe des-comentar la siguiente linea cuando se tenga rollos.
                        String cliente_print = cliente.replace("x_x"," ");
                        contenido = contenido + "\n\n################################\nPremio encontrado!!!\nCliente: " + cliente_print + "\nNumero ganador: " + num_premio.getText().toString() + "\n\n" + split[1] + " X " + paga.getSelectedItem().toString() + " = " + String.valueOf(Integer.parseInt(paga.getSelectedItem().toString()) * Integer.parseInt(split[1])) + " colones. \n################################\n\n";
                        total = total + Integer.parseInt(paga.getSelectedItem().toString()) * Integer.parseInt(split[1]);
                    }
                } else {
                    if (loter.get("Tipo_juego").equals("Monazos")) {
                        //Aqui se van a capturar todos los demas numeros que no calleron en el if del orden.
                        //Primero que nada se debe verificar si hay algun premio en desorden.
                        //Ahora vamos a separar el numero ganador en sus digitos.
                        //boolean flag_1 = false, flag_2 = false, flag_3 = false, flag_11 = false, flag_12 = false, flag_13 = false, flag_21 = false, flag_22 = false, flag_23 = false;
                        int Wdig_1 = -1, Wdig_2 = -1, Wdig_3 = -1;
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
                        }
                        //Ahora vamos a separar en sus digitos el numero que se esta analizando en esta iteracion.
                        int dig_1 = -1, dig_2 = -1, dig_3 = -1;
                        int iter_num = Integer.parseInt(split[0]);//  split[0] es el numero jugado que gano!!!
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
                                    if (split[2].equals("Desorden")) {
                                        //Se ha capturado un premio en desorden

                                        //Se debe comentar la siguiente linea cuando se tenga rollos.
                                        //contenido = contenido + "###############################\nPremio encontrado!!!\nCliente: " + cliente + "\nNumero ganador: " + split[0] + " " + split[2] + "\n" + split[1] + " X " + pagar + " = " + String.valueOf(pagar * Integer.parseInt(split[1])) + " colones. \n###############################\n";
                                        //Se debe des-comentar la siguiente linea cuando se tenga rollos.
                                        String cliente_print = cliente.replace("x_x"," ");
                                        contenido = contenido + "\n\n################################\nPremio encontrado!!!\nCliente: " + cliente_print + "\nNumero ganador: " + split[0] + " " + split[2] + "\n\n" + split[1] + " X " + pagar + " = " + String.valueOf(pagar * Integer.parseInt(split[1])) + " colones. \n################################\n\n";
                                        total = total + (pagar * Integer.parseInt(split[1]));
                                    }
                                }
                            }
                        }
                    }
                }
                linea = br24.readLine();
            }

            br24.close();
            archivo24.close();
        }catch (IOException e) {
        }
    }

    private String imprimir_archivo(String nombre_archivo){
        String archivos[] = fileList();
        String contenido = "";//Aqui se lee el contenido del archivo guardado.
        if (ArchivoExiste(archivos, nombre_archivo)) {//Archivo nombre_archivo es el archivo que vamos a imprimir
            try {
                InputStreamReader archivo = new InputStreamReader(openFileInput(nombre_archivo));//Se abre archivo
                BufferedReader br = new BufferedReader(archivo);


                String linea = br.readLine();//Se lee archivo
                while (linea != null) {
                    contenido = contenido + linea + "\n";
                    linea = br.readLine();
                    //return;
                }
                //Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                //Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                //Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                //Toast.makeText(this, contenido, Toast.LENGTH_LONG).show();
                br.close();
                archivo.close();
            } catch (IOException e) {
            }
        }
        return contenido;
    }


    public void buscar_numero_ganador(String lot) {//Premios premios
        //Algoritmo que revisa la nube a ver si se subieron los numeros ganadores.
        //msg("lot: " + lot);
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
                        //msg("Response: " + response);
                        if (response != null) {
                            //response.replace("loteria", "_sepa_");
                            //msg(response);
                            String[] split = response.split("loteria");//Se separa el objeto Json

                            //Se llena un HashMap con los premios, los cuales se bajan de la nube.
                            for (int i = 1; i < split.length; i++) {
                                String[] split2 = split[i].split("\"");
                                //                       Ej.                    Tica                             Noche
                                String loteria_actual = "ojo-rojo_ojo-rojo" + split2[2] + "ojo-rojo_ojo-rojo" + split2[6] + "ojo-rojo_ojo-rojo";
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

                                //msg("Key:\n" + key + "\n\nlot:\n" + lot);
                                if (key.equals(lot)) {

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
                                    num_premio.setText(nume1);//No es
                                    nume1 = "not_asigned";

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
                                    } else {
                                        nume3 = split4[3];
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

    public void tirar_reporte(View view){//Se deben de leer todos los tiquetes que se generaron dunrante el dia para cada loteria y cada horario elegidos


        String winner_number = "not_yet";

        if (loteria.getSelectedItem().toString().equals("Elija una loteria...")) {
            Toast.makeText(this, "Debe elejir una loteria", Toast.LENGTH_LONG).show();
            return;
        } else if (horario.getSelectedItem().toString().equals("Escoja el horario...")) {
            msn2();
            return;
        } else {
            //Do nothing.
        }

        //msg("nume1: " + nume1);
        if (nume1.equals("not_asigned")){
            if (num_premio.getText().toString().isEmpty()) {
                Toast.makeText(this, "Buscando numero ganador en la nube... ", Toast.LENGTH_LONG).show();
                String horarito = "";
                if (horario.getSelectedItem().toString().equals("Maniana")) {
                    horarito = "maniana";
                } else if (horario.getSelectedItem().toString().equals("Dia")) {
                    horarito = "dia";
                } else if (horario.getSelectedItem().toString().equals("Tarde")) {
                     horarito = "tarde";
                } else if (horario.getSelectedItem().toString().equals("Noche")) {
                    horarito = "noche";
                } else {
                    //Never come here!!!
                }
                String lot = "ojo-rojo_ojo-rojo" + loteria.getSelectedItem().toString() + "ojo-rojo_ojo-rojo" + horarito + "ojo-rojo_ojo-rojo";
                //msg("lot: " + lot);
                buscar_numero_ganador(lot);
                if (nume1.equals("not_asigned")) {
                    return;
                } else {
                    //continue!
                }
            } else {
                //Do nothing.
            }
        } else {
            if (num_premio.getText().toString().isEmpty()) {
                num_premio.setText(nume1);
            } else {
                //Do nothing.
            }
            winner_number = nume1;
            numero_gana = nume1;
            //num_premio.setText(nume1);//no es
        }


        if (loter.get("Tipo_juego").equals("Monazos")) {
            if (Integer.parseInt(num_premio.getText().toString()) > 999) {
                Toast.makeText(this, "Debe indicar un numero correcto", Toast.LENGTH_LONG).show();
                num_premio.setText("");
                num_premio.requestFocus();
                return;
            } else if (num_premio.getText().toString().length() < 3) {
                Toast.makeText(this, "Debe indicar un numero correcto", Toast.LENGTH_LONG).show();
                num_premio.setText("");
                num_premio.requestFocus();
                return;
            }
        } else {
            if (Integer.parseInt(num_premio.getText().toString()) > 99) {
                Toast.makeText(this, "Debe indicar un numero correcto", Toast.LENGTH_LONG).show();
                //num_premio.setText(nume1);
                num_premio.requestFocus();
                return;
            } else {
                //Do nothing.
            }

        }

        /*if (Integer.parseInt(num_premio.getText().toString()) < 0) {
            Toast.makeText(this, "Debe indicar un numero correcto", Toast.LENGTH_LONG).show();
            num_premio.setText("");
            num_premio.requestFocus();
            return;
        } else */if (loteria.getSelectedItem().toString() == null) {
            Toast.makeText(this, "Debe indicar una loteria", Toast.LENGTH_LONG).show();
            return;
        } else {
            if (loter.get("Tipo_juego").equals("Monazos")) {

                //File file = new File("premios" + "x_y" + loteria.getSelectedItem().toString() + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt");
                //file.delete();
                //String Paga = paga.getSelectedItem().toString();
                //int pagA = Integer.parseInt(Paga);
                String Loteria = loteria.getSelectedItem().toString();
                String Horario = horario.getSelectedItem().toString();
                String Winner_number = "";
                if (winner_number.equals("not_yet")){
                    Winner_number = num_premio.getText().toString();
                } else {
                    Winner_number = winner_number;
                }
                numero_gana = Winner_number;
                //agregar_linea_archivo("premios" + "x_y" + Loteria + "x_y" + Horario + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt", Winner_number);

                int Number_winner = Integer.parseInt(Winner_number);
                String archivos[] = fileList();
                contenido = "";

                //Se debe des-comentar la siguente linea cuando se tengan rollos.
                contenido = contenido + "\n    Reporte de ganadores\n\n    ----> " + Loteria + " " + Horario + " <----\n\n    " + fecha + "/" + mes + "/" + anio + "\n\n";
                for (int i = 0; i < archivos.length; i++) {//TODO: Se debe pasar a un solo archivo.
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
                        String[] split_nom_file = archivo.split("_separador_");
                        Log.v("Tirar_reporte mona", "Final del nombre: " + split_nom_file[14] + ".\n\nFactura numero: " + split_nom_file[7] + "\n\n.");
                        if (split_nom_file[14].equals("equi.txt") || (Integer.parseInt(split_nom_file[6]) < 0)) {
                            Log.v("Tirar_reporte mona2", "Final del nombre: " + split_nom_file[14] + ".\n\nFactura numero: " + split_nom_file[7] + "\n\n.");
                            //Creo que nada. TODO: ver que hacer.
                        } else if (split_nom_file[14].equals("null.txt")) {
                            generar_tiquete(archivo, Integer.parseInt(num_premio.getText().toString()), jugador_actual);
                        } else {
                            //Do nothing.
                        }

                    }
                }
                contenido = contenido + "\n\n################################\nTotal de premios en \n" + Loteria + " " + Horario + ": " + String.valueOf(total) + " colones.\n################################\n\n\n\n\n";
                printIt(contenido);
                total = 0;

            } else {
                //File file = new File("premios" + "x_y" + loteria.getSelectedItem().toString() + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt");
                //file.delete();
                //agregar_linea_archivo("premios" + "x_y" + loteria.getSelectedItem().toString() + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt", num_premio.getText().toString() + "  " + paga.getSelectedItem().toString());
                String Loteria = loteria.getSelectedItem().toString();
                String Horario = horario.getSelectedItem().toString();
                String Paga = paga.getSelectedItem().toString();
                int pagA = Integer.parseInt(Paga);
                String Winner_number = "";
                if (winner_number.equals("not_yet")){
                    Winner_number = num_premio.getText().toString();
                } else {
                    Winner_number = winner_number;
                }
               // msg("Winner_number: " + Winner_number);
                num_premio.setText(Winner_number);
                //int Number_winner = Integer.parseInt(Winner_number);
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
                        String[] split_nom_file = archivo.split("_separador_");
                        Log.v("Tirar_reporte regu", "Final del nombre: " + split_nom_file[14] + ".\n\nFactura numero: " + split_nom_file[7] + "\n\n.");
                        if (split_nom_file[14].equals("equi.txt") || (Integer.parseInt(split_nom_file[6]) < 0)) {
                            Log.v("Tirar_reporte regu2", "Final del nombre: " + split_nom_file[14] + ".\n\nFactura numero: " + split_nom_file[6] + "\n\n.");
                            //Creo que nada. TODO: ver que hacer.
                        } else if (split_nom_file[14].equals("null.txt")) {
                            generar_tiquete(archivo, Integer.parseInt(num_premio.getText().toString()), jugador_actual);
                        } else {
                            //Do nothing.
                        }
                    }
                }

                contenido = contenido + "\n\n################################\n Total de premios en \n" + Loteria + " " + Horario + ": " + String.valueOf(total) + " colones.\n################################\n\n\n\n\n";
                printIt(contenido);
                total = 0;
            }
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

    private void guardar_pdf(){
        //Respaldo digital del reporte contable.
    }

    private void borrar_archivo(String s){
        //Se borra el archivo contable.
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
            //startActivity(Activity_ver);


            Intent winn_act = new Intent(this, WinnersActivity.class);
            winn_act.putExtra("Loteria", loteria.getSelectedItem().toString());
            startActivity(winn_act);
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
            String impresora = get_impresora();
            BluetoothDevice device = BluetoothUtil.getDevice(btAdapter, impresora);
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

    private void llenar_spinner_paga() {
        String paga1_str = loter.get("Paga1");
        String paga2_str = loter.get("Paga2");
        //int paga1 = Integer.parseInt(paga1_str);
        int paga2 = Integer.parseInt(paga2_str);
        if (paga2 == 0){
            paga_str_array = new String[1];
            paga_str_array[0] = paga1_str;
        }
        else {
            paga_str_array = new String[2];
            paga_str_array[0] = paga1_str;
            paga_str_array[1] = paga2_str;
        }
        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, paga_str_array);
        paga.setAdapter(adapter3);
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
