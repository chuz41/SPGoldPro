package com.example.spgold;

//import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
//import android.app.TimePickerDialog;
import android.content.Intent;
//import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.TimePicker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VentasActivity extends AppCompatActivity {

    private TextView lot;
    private TextView hor;
    private Spinner loteria;
    private Spinner horario;
    private String[] loterias;//Informacion que aparecera en el spinner de loterias
    private String[] horarios;//Informacion que aparecera en el spinner de horarios
    private String[] archivos_lot;
    private TextView tv_fecha_ventas;
    private Button btn_fecha_ventas;
    private boolean caduc = false;

    //##############################################################################################

    private String mes;
    private String anio;
    private String dia;
    private String hora;
    private String hora_completa;
    private String minuto;
    private String fecha;

    private int mes_selected;
    private int anio_selected;
    private int fecha_selected;

    //##############################################################################################

    private String mensaje_to_print;

    private Map<String, String> loter = new HashMap<String, String>();
    private Map<String, Integer> meses = new HashMap<String, Integer>();


    //@RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventas);

        mensaje_to_print = getIntent().getStringExtra( "mensaje_toast");

        if (mensaje_to_print != null) {
            Toast.makeText(this, mensaje_to_print, Toast.LENGTH_LONG).show();
        }

        llenar_mapa_meses();

        //fecha = (TextView) findViewById(R.id.tv_repventas);
        lot = (TextView) findViewById(R.id.tv_ingrese_lot_rev);
        hor = (TextView) findViewById(R.id.tv_ingrese_hor_rev);
        loteria = (Spinner) findViewById(R.id.spinner_lot);
        horario = (Spinner) findViewById(R.id.spinner_hor);
        tv_fecha_ventas = (TextView) findViewById(R.id.textView_fecha_ventas);
        btn_fecha_ventas = (Button) findViewById(R.id.button_fecha_ventas);



        //###########################################################################################

        Date now = Calendar.getInstance().getTime();
        String ahora = now.toString();
        //se separan los campos de la fecha y hora para verificar que si se pueda realizar la venta.
        separar_fechaYhora(ahora);
        tv_fecha_ventas.setText(fecha + "/" + String.valueOf(meses.get(mes)) + "/" + anio);

        //###########################################################################################



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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, loterias);
        loteria.setAdapter(adapter);

        loteria.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        //Crear diccionario con la informacion de la loteria seleccionada
                        String seleccion = loteria.getSelectedItem().toString();
                        if (seleccion.equals("Elija un sorteo...")) {
                            //Do nothing!
                        }else {
                            crearDiccionario();//Meter aqui la loteria seleccionada en el spinner
                            //Agregar horarios al otro Spinner
                            crear_array_horarios();
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
                        //Crear diccionario con la informacion de la loteria seleccionada
                        String seleccion = horario.getSelectedItem().toString();
                        if (seleccion.equals("Escoja el horario...")) {
                            //Do nothing!
                        }else {
                            //Ir a pagina de ventas
                            String seleccion2 = loteria.getSelectedItem().toString();
                            if (seleccion2.equals("Elija un sorteo...")) {
                                //Do nothing!
                            }else {
                                ir_ventas();
                            }

                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

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

    //##############################################################################################


    //@RequiresApi(api = Build.VERSION_CODES.N)
    //@Override
    public void cambiar_fecha(View view) {

        if (view == btn_fecha_ventas) {//button cambiar fecha
            final Calendar c = Calendar.getInstance();
            mes_selected = (c.get(Calendar.MONTH));
            //Toast.makeText(this, "mes selected: " + mes_selected, Toast.LENGTH_LONG).show();
            anio_selected = c.get(Calendar.YEAR);
            fecha_selected = c.get(Calendar.DAY_OF_MONTH);



            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                    tv_fecha_ventas.setText(String.valueOf(i2) + "/" + String.valueOf(i1+1) + "/" + String.valueOf(i));
                    mes_selected = i1+1;
                    anio_selected = i;
                    fecha_selected = i2;

                    //Generamos numero comparador:
                    int comparador_selected = anio_selected;
                    comparador_selected = comparador_selected * 100;
                    comparador_selected = comparador_selected + mes_selected;
                    comparador_selected = comparador_selected*100;
                    comparador_selected = comparador_selected + fecha_selected;

                    int comparador = Integer.parseInt(anio);
                    comparador = comparador * 100;
                    comparador = comparador + meses.get(mes);
                    comparador = comparador*100;
                    comparador = comparador + Integer.parseInt(fecha);

                    if (comparador_selected > comparador) {
                        caduc = true;
                    } else if (comparador_selected == comparador) {
                        //caduc = false;
                    } else {
                        msn();
                        mes_selected = (c.get(Calendar.MONTH));
                        //Toast.makeText(this, "mes selected: " + mes_selected, Toast.LENGTH_LONG).show();
                        anio_selected = c.get(Calendar.YEAR);
                        fecha_selected = c.get(Calendar.DAY_OF_MONTH);
                        tv_fecha_ventas.setText(String.valueOf(fecha_selected) + "/" + String.valueOf(mes_selected + 1) + "/" + String.valueOf(anio_selected));
                        //return;
                    }

                }
            },anio_selected,mes_selected,fecha_selected);
            datePickerDialog.show();

        }

    }

    private void msn() {
        Toast.makeText(this, "     ERROR!!!   \n\nLa fecha elegida es anterior al dia de hoy.\n\nSeleccione una fecha valida!!", Toast.LENGTH_LONG).show();
    }


    //##############################################################################################

    private void separar_fechaYhora(String ahora) {
        String[] split = ahora.split(" ");
        dia = split[0];
        mes = String.valueOf(meses.get(split[1]));
        anio = split[5];
        hora_completa = split[3];
        fecha = split[2];
        split = hora_completa.split(":");
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

    /*

    Pattern pattern = Pattern.compile(linea, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(archivos[i]);
            boolean matchFound = matcher.find();
            if (matchFound) {
                archivo_leido = archivos[i];
            }

     */


    private void ir_ventas(){
        Intent Config;
        if (loter.get("Tipo_juego").equals("Parley")) {
            Config = new Intent(this, TiqueteparleyActivity.class);
            Config.putExtra("Paga1", loter.get("Paga1"));
            Config.putExtra("Paga2", loter.get("Paga2"));
            Config.putExtra("Maniana", loter.get("Maniana"));
            Config.putExtra("Hora_juego_M", loter.get("Hora_juego_M"));
            Config.putExtra("Hora_lista_M", loter.get("Hora_lista_M"));
            Config.putExtra("Dia", loter.get("Dia"));
            Config.putExtra("Hora_juego_D", loter.get("Hora_juego_D"));
            Config.putExtra("Hora_lista_D", loter.get("Hora_lista_D"));
            Config.putExtra("Tarde", loter.get("Tarde"));
            Config.putExtra("Hora_juego_T", loter.get("Hora_juego_T"));
            Config.putExtra("Hora_lista_T", loter.get("Hora_lista_T"));
            Config.putExtra("Noche", loter.get("Noche"));
            Config.putExtra("Hora_juego_N", loter.get("Hora_juego_N"));
            Config.putExtra("Hora_lista_N", loter.get("Hora_lista_N"));
            Config.putExtra("Limite_maximo", loter.get("Limite_maximo"));
            Config.putExtra("Nombre_puesto", loter.get("Nombre_puesto"));
            Config.putExtra("Numero_maquina", loter.get("Numero_maquina"));
            Config.putExtra("Comision_vendedor", loter.get("Comision_vendedor"));
            Config.putExtra("tipo_lot", loter.get("Tipo_juego"));
            String Loteria = loteria.getSelectedItem().toString();
            String Horario = horario.getSelectedItem().toString();
            Config.putExtra("Loteria", Loteria);
            Config.putExtra("Horario", Horario);
            Config.putExtra("Spread_Sheet_Id", loter.get("Spread_Sheet_Id"));
            Config.putExtra("Spread_Sheet_Id_maniana", loter.get("Spread_Sheet_Id_maniana"));
            Config.putExtra("fecha_selected", String.valueOf(fecha_selected));
            Config.putExtra("mes_selected", String.valueOf(mes_selected));
            Config.putExtra("anio_selected", String.valueOf(anio_selected));
            Config.putExtra("caduce", String.valueOf(caduc));
            loter.clear();
            startActivity(Config);
            finish();
            System.exit(0);
        } else if (loter.get("Tipo_juego").equals("Monazos")) {
            Config = new Intent(this, TiquetemonazosActivity.class);
            Config.putExtra("Paga1", loter.get("Paga1"));
            Config.putExtra("Paga2", loter.get("Paga2"));
            Config.putExtra("Maniana", loter.get("Maniana"));
            Config.putExtra("Hora_juego_M", loter.get("Hora_juego_M"));
            Config.putExtra("Hora_lista_M", loter.get("Hora_lista_M"));
            Config.putExtra("Dia", loter.get("Dia"));
            Config.putExtra("Hora_juego_D", loter.get("Hora_juego_D"));
            Config.putExtra("Hora_lista_D", loter.get("Hora_lista_D"));
            Config.putExtra("Tarde", loter.get("Tarde"));
            Config.putExtra("Hora_juego_T", loter.get("Hora_juego_T"));
            Config.putExtra("Hora_lista_T", loter.get("Hora_lista_T"));
            Config.putExtra("Noche", loter.get("Noche"));
            Config.putExtra("Hora_juego_N", loter.get("Hora_juego_N"));
            Config.putExtra("Hora_lista_N", loter.get("Hora_lista_N"));
            Config.putExtra("Limite_maximo", loter.get("Limite_maximo"));
            Config.putExtra("Nombre_puesto", loter.get("Nombre_puesto"));
            Config.putExtra("Numero_maquina", loter.get("Numero_maquina"));
            Config.putExtra("Comision_vendedor", loter.get("Comision_vendedor"));
            Config.putExtra("tipo_lot", loter.get("Tipo_juego"));
            String Loteria = loteria.getSelectedItem().toString();
            String Horario = horario.getSelectedItem().toString();
            Config.putExtra("Loteria", Loteria);
            Config.putExtra("Horario", Horario);
            Config.putExtra("Spread_Sheet_Id", loter.get("Spread_Sheet_Id"));
            Config.putExtra("Spread_Sheet_Id_maniana", loter.get("Spread_Sheet_Id_maniana"));
            Config.putExtra("fecha_selected", String.valueOf(fecha_selected));
            Config.putExtra("mes_selected", String.valueOf(mes_selected));
            Config.putExtra("anio_selected", String.valueOf(anio_selected));
            Config.putExtra("caduce", String.valueOf(caduc));
            loter.clear();
            startActivity(Config);
            finish();
            System.exit(0);
        } else {
            Config = new Intent(this, TiqueteActivity.class);
            Config.putExtra("Paga1", loter.get("Paga1"));
            Config.putExtra("Paga2", loter.get("Paga2"));
            Config.putExtra("Maniana", loter.get("Maniana"));
            Config.putExtra("Hora_juego_M", loter.get("Hora_juego_M"));
            Config.putExtra("Hora_lista_M", loter.get("Hora_lista_M"));
            Config.putExtra("Dia", loter.get("Dia"));
            Config.putExtra("Hora_juego_D", loter.get("Hora_juego_D"));
            Config.putExtra("Hora_lista_D", loter.get("Hora_lista_D"));
            Config.putExtra("Tarde", loter.get("Tarde"));
            Config.putExtra("Hora_juego_T", loter.get("Hora_juego_T"));
            Config.putExtra("Hora_lista_T", loter.get("Hora_lista_T"));
            Config.putExtra("Noche", loter.get("Noche"));
            Config.putExtra("Hora_juego_N", loter.get("Hora_juego_N"));
            Config.putExtra("Hora_lista_N", loter.get("Hora_lista_N"));
            Config.putExtra("Limite_maximo", loter.get("Limite_maximo"));
            Config.putExtra("Nombre_puesto", loter.get("Nombre_puesto"));
            Config.putExtra("Numero_maquina", loter.get("Numero_maquina"));
            Config.putExtra("Comision_vendedor", loter.get("Comision_vendedor"));
            Config.putExtra("tipo_lot", loter.get("Tipo_juego"));
            String Loteria = loteria.getSelectedItem().toString();
            String Horario = horario.getSelectedItem().toString();
            Config.putExtra("Loteria", Loteria);
            Config.putExtra("Horario", Horario);
            Config.putExtra("Spread_Sheet_Id", loter.get("Spread_Sheet_Id"));
            Config.putExtra("Spread_Sheet_Id_maniana", loter.get("Spread_Sheet_Id_maniana"));
            Config.putExtra("fecha_selected", String.valueOf(fecha_selected));
            Config.putExtra("mes_selected", String.valueOf(mes_selected));
            Config.putExtra("anio_selected", String.valueOf(anio_selected));
            Config.putExtra("caduce", String.valueOf(caduc));
            loter.clear();
            startActivity(Config);
            finish();
            System.exit(0);
        }
    }

    private void llenar_spinner_hor() {
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, R.layout.spinner_item_loteria, horarios);
        horario.setAdapter(adapter2);
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

    private boolean ArchivoExiste (String archivos [],String Tiquete){
        for (int i = 0; i < archivos.length; i++)
            if (Tiquete.equals(archivos[i]))
                return true;
        return false;
    }
}