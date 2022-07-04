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

public class ParleyActivity extends AppCompatActivity {

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
    private Spinner paga;
    private String[] loterias;//Informacion que aparecera en el spinner de loterias
    private String[] horarios;//Informacion que aparecera en el spinner de horarios
    private String[] archivos_lot;
    private String[] paga_str_array;
    private TextView titulo;
    private TextView paga_x_veces;
    private EditText num_premio1;
    private EditText num_premio2;
    private EditText num_premio3;
    //private TextView
    private int total = 0;
    private String Loteria;
    Map<String, String> loter = new HashMap<String, String>();
    private Map<String, Integer> meses = new HashMap<String, Integer>();
    private String dispositivo;
    private String readRowURL = "https://script.google.com/macros/s/AKfycbxJNCrEPYSw8CceTwPliCscUtggtQ2l_otieFmE/exec?spreadsheetId=1iMXw4z0ljwvfhdR5BBmh586h1AOmNCWll7GYI1MJFbM&sheet=hoy";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parley);

        Loteria = getIntent().getStringExtra("Loteria");//Nombre con el que se guardo esta loteria Parley. Puede ser cualquier nombre. Se recibe de la activity anterior.
        titulo = (TextView) findViewById(R.id.tv_winparley);
        lot = (TextView) findViewById(R.id.tv_ingrese_lot_par);
        hor = (TextView) findViewById(R.id.tv_ingrese_hor_par);
        loteria = (TextView) findViewById(R.id.tv_lot_par);
        horario = (Spinner) findViewById(R.id.spinner_hor_par);
        paga = (Spinner) findViewById(R.id.spinner_paga_par);
        num_premio1 = (EditText) findViewById(R.id.et_numwinn_par);
        num_premio2 = (EditText) findViewById(R.id.et_numwinn2_par);
        num_premio3 = (EditText) findViewById(R.id.et_numwinn3_par);
        paga_x_veces = (TextView) findViewById(R.id.textView_paga_par);

        //titulo.setText("Reporte de ventas");
        loteria.setText(Loteria);

        llenar_mapa_meses();
        dispositivo = check_device();

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
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, loterias);
        //loteria.setAdapter(adapter);

        //num_premio.setFocusableInTouchMode(true);

        horario.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (horario.getSelectedItem().toString().equals("Escoja el horario...")) {
                            //msn2();

                        } else {
                            num_premio1.setFocusableInTouchMode(true);
                            mostrar_teclado(num_premio1);
                            num_premio1.requestFocus();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
        crearDiccionario();
        crear_array_horarios();
        llenar_spinner_hor();
        llenar_spinner_paga();

        //Implementacion de un text listener
        num_premio1.setFocusableInTouchMode(false);//Pasar al siguiente espacio en lugar de esto que hace actualmente.
        num_premio2.setFocusableInTouchMode(false);
        num_premio3.setFocusableInTouchMode(false);


        num_premio1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //num_premio.setFocusableInTouchMode(false);
                if (horario.getSelectedItem().toString().equals("Escoja el horario")) {
                    select_hor_msg();
                    //num_premio.setFocusableInTouchMode(false);
                    //num_premio.setText("");
                    ocultar_teclado();
                } else if (s.length() == 2) {
                    //ocultar_teclado();
                    num_premio2.setFocusableInTouchMode(true);
                    num_premio2.requestFocus();
                    //num_premio2.requestFocus();
                } else if (num_premio2.getText().toString().isEmpty()) {
                    //Do nothin
                } else if (num_premio3.getText().toString().isEmpty()) {
                    //Do nothing
                } else if (Integer.parseInt(num_premio2.getText().toString()) < 0 ) {
                    //Do nothing
                } else if (Integer.parseInt(num_premio2.getText().toString()) > 99 ) {
                    //Do nothing
                } else if (Integer.parseInt(num_premio3.getText().toString()) < 0 ) {
                    //Do nothing
                } else if (Integer.parseInt(num_premio3.getText().toString()) > 99 ) {
                    //Do nothing
                } else {
                    if (s.length() == 2) {
                        //ocultar_teclado();
                        if (ArchivoExiste(archivos, "premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt")) ;
                        try {
                            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput("premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt", Activity.MODE_PRIVATE));
                            String ArchivoCompleto = "";
                            archivo.write(ArchivoCompleto);
                            archivo.flush();
                        } catch (IOException e) {
                        }
                        File file = new File("premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt");
                        file.delete();
                        agregar_linea_archivo("premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt", num_premio1.getText().toString() + "  " + num_premio2.getText().toString() + "  " + num_premio3.getText().toString());
                        //imprimir_archivo("premios" + "x_y" + loteria.getSelectedItem().toString() + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt");
                        num_premio2.setFocusableInTouchMode(true);
                        num_premio2.requestFocus();
                    } else {
                        error_msm();
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        num_premio2.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //num_premio.setFocusableInTouchMode(false);
                if (horario.getSelectedItem().toString().equals("Escoja el horario")) {
                    select_hor_msg();
                    //num_premio.setFocusableInTouchMode(false);
                    //num_premio.setText("");
                    ocultar_teclado();
                    return;
                } else if (s.length() == 2) {
                    //ocultar_teclado();
                    num_premio3.setFocusableInTouchMode(true);
                    num_premio3.requestFocus();
                    return;
                } else if (num_premio1.getText().toString().isEmpty()) {
                    imprimir_mensaje3();
                    //numero1.setText("");

                    num_premio2.setText("");
                    num_premio2.requestFocus();
                    mostrar_teclado(num_premio2);
                    return;
                    //Do nothin
                } else if (num_premio3.getText().toString().isEmpty()) {
                    //Do nothing
                    return;
                } else if (Integer.parseInt(num_premio1.getText().toString()) < 0 ) {
                    imprimir_mensaje3();
                    //numero1.setText("");

                    num_premio2.setText("");
                    num_premio2.requestFocus();
                    mostrar_teclado(num_premio2);
                    return;
                    //Do nothing
                } else if (Integer.parseInt(num_premio1.getText().toString()) > 99 ) {
                    imprimir_mensaje3();
                    //numero1.setText("");

                    num_premio2.setText("");
                    num_premio2.requestFocus();
                    mostrar_teclado(num_premio2);
                    return;
                    //Do nothing
                } else if (s.length() < 2) {
                    imprimir_mensaje3();
                    //numero1.setText("");

                    num_premio2.setText("");
                    num_premio2.requestFocus();
                    mostrar_teclado(num_premio2);
                    return;
                } else if (Integer.parseInt(num_premio3.getText().toString()) < 0 ) {
                    //Do nothing
                    return;
                } else if (Integer.parseInt(num_premio3.getText().toString()) > 99 ) {
                    //Do nothing
                    return;
                } else {
                    ocultar_teclado();
                    if (s.length() == 2) {
                        //ocultar_teclado();
                        if (ArchivoExiste(archivos, "premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt")) ;

                        try {
                            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput("premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt", Activity.MODE_PRIVATE));
                            String ArchivoCompleto = "";
                            archivo.write(ArchivoCompleto);
                            archivo.flush();
                        } catch (IOException e) {
                        }
                        File file = new File("premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt");
                        file.delete();
                        agregar_linea_archivo("premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt", num_premio1.getText().toString() + "  " + num_premio2.getText().toString() + "  " + num_premio3.getText().toString());
                        //imprimir_archivo("premios" + "x_y" + loteria.getSelectedItem().toString() + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt");
                        num_premio3.setFocusableInTouchMode(true);
                        num_premio3.requestFocus();
                        mostrar_teclado(num_premio3);
                    } else {
                        error_msm();
                        return;
                    }
                }
                //num_premio2.setFocusableInTouchMode(true);

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        num_premio3.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //num_premio.setFocusableInTouchMode(false);
                if (horario.getSelectedItem().toString().equals("Escoja el horario")) {
                    select_hor_msg();
                    //num_premio.setFocusableInTouchMode(false);
                    //num_premio.setText("");
                    ocultar_teclado();
                    return;
                } else if (s.length() == 2) {
                    ocultar_teclado();
                } else if (num_premio2.getText().toString().isEmpty()) {
                    //Do nothin
                } else if (num_premio1.getText().toString().isEmpty()) {
                    //Do nothing
                } else if (Integer.parseInt(num_premio2.getText().toString()) < 0 ) {
                    //Do nothing
                } else if (Integer.parseInt(num_premio2.getText().toString()) > 99 ) {
                    //Do nothing
                } else if (Integer.parseInt(num_premio1.getText().toString()) < 0 ) {
                    //Do nothing
                } else if (Integer.parseInt(num_premio1.getText().toString()) > 99 ) {
                    //Do nothing
                } else {
                    if (s.length() == 2) {
                        ocultar_teclado();
                        if (ArchivoExiste(archivos, "premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt")) ;

                        try {
                            OutputStreamWriter archivo = new OutputStreamWriter(openFileOutput("premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt", Activity.MODE_PRIVATE));
                            String ArchivoCompleto = "";
                            archivo.write(ArchivoCompleto);
                            archivo.flush();
                        } catch (IOException e) {
                        }
                        File file = new File("premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt");
                        file.delete();
                        agregar_linea_archivo("premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt", num_premio1.getText().toString() + "  " + num_premio2.getText().toString() + "  " + num_premio3.getText().toString());
                        //imprimir_archivo("premios" + "x_y" + loteria.getSelectedItem().toString() + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt");
                    } else {
                        //Do nothing
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
                            msn(valor_paga, 0);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
    }

    public void imprimir_mensaje3(){
        Toast.makeText(this, "Debe ingresar un numero valido!!!!!!", Toast.LENGTH_LONG).show();
    }

    private void select_lot_msg () {
        Toast.makeText(this, "Debe seleccionar una loteria", Toast.LENGTH_SHORT).show();
    }

    private void select_hor_msg () {
        Toast.makeText(this, "Debe seleccionar un horario valido", Toast.LENGTH_SHORT).show();
    }

    private void error_msm() {
        Toast.makeText(this, "Error!!! Llame a soporte tecnico. \nTelefono: (506)85258108", Toast.LENGTH_LONG).show();
    }

    private void ocultar_teclado(){
        View view = this.getCurrentFocus();
        InputMethodManager imn = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imn.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void mostrar_teclado(EditText view){

        InputMethodManager imn = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imn.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
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

        Intent Main = new Intent(this, MainActivity.class);
        startActivity(Main);
        finish();
        System.exit(0);
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


    private void generar_tiquete(String file, int Winner_num1, int Winner_num2, int Winner_num3, String cliente) {
        //Aqui se revisa si cada tiquete tiene premio.
        //contenido = "";
        String Horario = horario.getSelectedItem().toString();

        try {
            InputStreamReader archivo24 = new InputStreamReader(openFileInput(file));//Se abre el archivo contable
            BufferedReader br24 = new BufferedReader(archivo24);
            String linea = br24.readLine();//Se lee archivo
            Pattern pattern = Pattern.compile("BORRADA", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(linea);//Se verifica si la factura se ha borrado
            String seleccion_premio = paga.getSelectedItem().toString();
            //Toast.makeText(this, "Linea: " + linea + "\nSeleccion premio: " + seleccion_premio, Toast.LENGTH_LONG).show();
            boolean matchFound = matcher.find();
            if (matchFound){
                //Toast.makeText(this, "Se ha borrado esta loteria!!!", Toast.LENGTH_LONG).show();
                return;
            }
            String flag1 = "false";
            String flag2 = "false";
            String flag3 = "false";
            int num1 = -1;
            int num2 = -1;
            while (linea != null){
                boolean flag11 = false;
                boolean flag12 = false;
                boolean flag13 = false;
                //Toast.makeText(this, "La linea aun no es nula!!!", Toast.LENGTH_LONG).show();
                String[] split = linea.split("      ");//Se separa el monto de los numeros guardados.
                if (split[0].isEmpty()){
                    break;
                } else if (Integer.parseInt(split[0]) == Winner_num1) {
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

                if (flag3.equals("true")){


                    String cliente_print = cliente.replace("x_x"," ");
                    contenido = contenido + "\n\n################################\nPremio encontrado!!!\nCliente: " + cliente_print + "\nParejita ganadora: " + String.valueOf(num1) + "  " + String.valueOf(num2) + "\n\n" + split[2] + " X " + seleccion_premio + " = " + String.valueOf(Integer.parseInt(seleccion_premio) * Integer.parseInt(split[2])) + " colones. \n################################\n\n";
                    total = total + (Integer.parseInt(seleccion_premio) * Integer.parseInt(split[2]));
                    flag3 = "false";
                    flag1 = "false";
                    flag2 = "false";
                    flag3 = "false";
                }
                linea = br24.readLine();
            }
            br24.close();
            archivo24.close();
            //printIt(contenido);
        }catch (IOException e) {
        }
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

    private void msg(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
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

                        //msg("Response:\n\n" + response);
                        HashMap<String, String> premios = new HashMap<String, String>();
                        if (response != null) {
                            //msg("Response:\n\n" + response);
                            //response.replace("loteria", "_sepa_");
                            //msg(response);
                            String[] split = response.split("loteria");//Se separa el objeto Json

                            //Se llena un HashMap con los premios, los cuales se bajan de la nube.
                            for (int i = 1; i < split.length; i++) {
                                //msg("split[i]: " + split[i]);
                                String[] split2 = split[i].split("\"");
                                //msg("split2: " + split2[6]);
                                String nomLot = "";
                                if (split2[6].equals("dia")) {
                                    nomLot = "Dia";
                                } else if (split2[6].equals("tarde")) {
                                    nomLot = "Tarde";
                                } else if (split2[6].equals("noche")) {
                                    nomLot = "Noche";
                                } else if (split2[6].equals("maniana")) {
                                    nomLot = "Maniana";
                                } else {
                                    //Do nothing.
                                }
                                //msg("nomLot: " + nomLot + "\n\nsplit2[3]: " + split2[3]);
                                //                       Ej.                    Parley                          Noche
                                String loteria_actual = "ojo-rojo_ojo-rojo" + split2[2] + "ojo-rojo_ojo-rojo" + nomLot + "ojo-rojo_ojo-rojo";
                                //                       Ej.                    03                                  no                                no                                 ID
                                String premio_actual = "ojo-rojo_ojo-rojo" + split2[10] + "ojo-rojo_ojo-rojo" + split2[14] + "ojo-rojo_ojo-rojo" + split2[18] + "ojo-rojo_ojo-rojo" + split2[26] + "ojo-rojo_ojo-rojo";
                                //msg("loteria_actual: " + loteria_actual + "\n\npremios.key: " + premios.get(loteria_actual));
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

                                //msg("key: " + key + "\n\nlot: " + lot);

                                if (key.equals(lot)) {

                                    //msg("key: " + key + "\n\nlot: " + lot);
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
                                    } else {
                                        nume3 = split4[3];
                                        if (Integer.parseInt(nume3) < 10) {
                                            nume3 = "0" + String.valueOf(Integer.parseInt(nume3));
                                        } else {
                                            //Do nothing.
                                        }
                                    }
                                    //msg("nume1: " + nume1 + "nume2: " + nume2 + "nume3: " + nume3);
                                    num_premio1.setText(nume1);
                                    num_premio2.setText(nume2);
                                    num_premio3.setText(nume3);
                                    nume1 = "not_asigned";
                                    nume2 = "not_asigned";
                                    nume3 = "not_asigned";
                                    msg("Numeros ganadores encontrados!!!");
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

 
        //String winner_number = "not_yet";
        if (nume1.equals("not_asigned") | nume2.equals("not_asigned") | nume3.equals("not_asigned")) {
            // if (nume1.Equals("not_asigned") & nume2.Equals("not_asigned") & nume3.Equals("not_asigned")) {
            //Toast.makeText(this, "Numeros ganadores NO se han encontrado!\n\nDebe indicar los numeros ganadores!!!", Toast.LENGTH_LONG).show();
            //return;
        } else if (horario.getSelectedItem().toString().equals("Escoja el horario...")) {
            msn2();
            return;
        } else {
            //Do nothing. Contunuar con la ejecucion.
        //    num_premio1.setText(nume1);
        //    num_premio2.setText(nume2);
        //    num_premio3.setText(nume3);
        }

        if(num_premio1.getText().toString().isEmpty() | num_premio2.getText().toString().isEmpty() | num_premio3.getText().toString().isEmpty()) {
            if (horario.getSelectedItem().toString().equals("Escoja el horario...")) {// Lista de excepciones
                msn2();
                return;
            } else {
                Toast.makeText(this, "Buscando numeros ganadores en la nube... ", Toast.LENGTH_LONG).show();
                String lot = "ojo-rojo_ojo-rojoParleyojo-rojo_ojo-rojo" + horario.getSelectedItem().toString() + "ojo-rojo_ojo-rojo";
                buscar_numero_ganador(lot);
                if (nume1.equals("not_asigned") | nume2.equals("not_asigned") | nume3.equals("not_asigned")) {
                    // if (nume1.Equals("not_asigned") & nume2.Equals("not_asigned") & nume3.Equals("not_asigned")) {
                    //Toast.makeText(this, "Numeros ganadores NO se han encontrado!\n\nDebe indicar los numeros ganadores!!!", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    //Do nothing. Contunuar con la ejecucion.
                    num_premio1.setText(nume1);
                    num_premio2.setText(nume2);
                    num_premio3.setText(nume3);
                }
            }
        }

        if (horario.getSelectedItem().toString().equals("Escoja el horario...")) {// Lista de excepciones
            msn2();
            return;
        } else if (num_premio1.getText().toString().isEmpty()) {
            Toast.makeText(this, "Debe indicar el numero correspondiente al primer premio", Toast.LENGTH_LONG).show();
            num_premio1.setFocusableInTouchMode(true);
            num_premio1.requestFocus();
            return;
        } else if (Integer.parseInt(num_premio1.getText().toString()) > 99) {
            Toast.makeText(this, "Debe indicar un numero correcto en el primer premio", Toast.LENGTH_LONG).show();
            num_premio1.setText("");
            num_premio1.requestFocus();
            return;
        } else if (Integer.parseInt(num_premio1.getText().toString()) < 0) {
            Toast.makeText(this, "Debe indicar un numero correcto en el primer premio", Toast.LENGTH_LONG).show();
            num_premio1.setText("");
            num_premio1.requestFocus();
            return;
        } else if (num_premio1.getText().toString().length() < 2) {
            Toast.makeText(this, "Debe indicar un numero correcto en el primer premio", Toast.LENGTH_LONG).show();
            num_premio1.setFocusableInTouchMode(true);
            num_premio1.requestFocus();
            return;
        } else if (num_premio2.getText().toString().isEmpty()) {
            Toast.makeText(this, "Debe indicar el segundo numero ganador", Toast.LENGTH_LONG).show();
            num_premio2.setFocusableInTouchMode(true);
            num_premio2.requestFocus();
            return;
        } else if (Integer.parseInt(num_premio2.getText().toString()) > 99) {
            Toast.makeText(this, "Debe indicar un numero correcto en el segundo premio", Toast.LENGTH_LONG).show();
            num_premio2.setText("");
            num_premio2.setFocusableInTouchMode(true);
            num_premio2.requestFocus();
            return;
        } else if (Integer.parseInt(num_premio2.getText().toString()) < 0) {
            Toast.makeText(this, "Debe indicar un numero correcto en el segundo premio", Toast.LENGTH_LONG).show();
            num_premio2.setText("");
            num_premio2.setFocusableInTouchMode(true);
            num_premio2.requestFocus();
            return;
        } else if (num_premio2.getText().toString().length() < 2) {
            Toast.makeText(this, "Debe indicar un numero correcto en el segundo premio", Toast.LENGTH_LONG).show();
            num_premio2.setFocusableInTouchMode(true);
            num_premio2.requestFocus();
            return;
        } else if (num_premio3.getText().toString().isEmpty()) {
            Toast.makeText(this, "Debe indicar el numero del tercer premio", Toast.LENGTH_LONG).show();
            num_premio3.setFocusableInTouchMode(true);
            num_premio3.requestFocus();
            return;
        } else if (Integer.parseInt(num_premio3.getText().toString()) > 99) {
            Toast.makeText(this, "Debe indicar un numero correcto en el tercer premio", Toast.LENGTH_LONG).show();
            num_premio3.setText("");
            num_premio3.setFocusableInTouchMode(true);
            num_premio3.requestFocus();
            return;
        } else if (Integer.parseInt(num_premio3.getText().toString()) < 0) {
            Toast.makeText(this, "Debe indicar un numero correcto en el tercer premio", Toast.LENGTH_LONG).show();
            num_premio3.setText("");
            num_premio3.setFocusableInTouchMode(true);
            num_premio3.requestFocus();
            return;
        } else if (num_premio3.getText().toString().length() < 2) {
            Toast.makeText(this, "Debe indicar un numero correcto en el tercer premio", Toast.LENGTH_LONG).show();
            num_premio3.setFocusableInTouchMode(true);
            num_premio3.requestFocus();
            return;
        } else {
            File file = new File("premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt");
            file.delete();
            agregar_linea_archivo("premios" + "x_y" + Loteria + "x_y" + horario.getSelectedItem().toString() + "x_y" + fecha + "x_y" + mes + "x_y" + anio + "x_y.txt", num_premio1.getText().toString() + "  " + num_premio2.getText().toString() + "  " + num_premio3.getText().toString() + "  " + paga.getSelectedItem().toString());
            String Horario = horario.getSelectedItem().toString();
            String Paga = paga.getSelectedItem().toString();
            int pagA = Integer.parseInt(Paga);
            String Winner_number[] = new String[3];
            Winner_number[0] = num_premio1.getText().toString();
            Winner_number[1] = num_premio2.getText().toString();
            Winner_number[2] = num_premio3.getText().toString();
            int Number_winner1 = Integer.parseInt(Winner_number[0]);
            int Number_winner2 = Integer.parseInt(Winner_number[1]);
            int Number_winner3 = Integer.parseInt(Winner_number[2]);
            String archivos[] = fileList();
            contenido = "";


            contenido = contenido + "\n    Reporte de ganadores\n\n    ----> " + Loteria + " " + Horario + " <----\n\nFecha: " + fecha + "/" + mes + "/" + anio + "\n\n";
            for (int i = 0; i < archivos.length; i++) {
                Pattern pattern = Pattern.compile(Loteria + "_separador_" + Horario + "_separador_" + fecha + "_separador_", Pattern.CASE_INSENSITIVE);//Este es el tiquete de cada cliente se generoo en la activity anterior (TiqueteparleyActivity.java)
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
                    generar_tiquete(archivo, Number_winner1, Number_winner2, Number_winner3, jugador_actual);
                }
            }
            contenido = contenido + "\n\n################################\n Total de premios en \n" + Loteria + " " + Horario + ": " + String.valueOf(total) + " colones.\n################################\n\n\n\n\n";
            printIt(contenido);
            total = 0;
        }
        boton_atras();
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

    public void printIt(String Mensaje) {

        if (dispositivo.equals("Celular")) {

            Intent Activity_ver = new Intent(this, VerActivity.class);
            Activity_ver.putExtra("mensaje", Mensaje);
            //startActivity(Activity_ver);


            Intent parl_act = new Intent(this, ParleyActivity.class);
            parl_act.putExtra("Loteria", Loteria);
            startActivity(parl_act);
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
