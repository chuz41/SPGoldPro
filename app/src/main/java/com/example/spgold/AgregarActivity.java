package com.example.spgold;

import static android.content.Intent.getIntent;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class AgregarActivity extends AppCompatActivity {

    private String loteria;
    private String nombre_archivo;
    private String nuevo_archivo = "";
    TextInputLayout nombre_loteria,num_maquina,paga_1,paga_2,limite,puesto,comision;
    private String u1,u2,u3,u4,u5,u6,u7;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar);
        nombre_loteria = (TextInputLayout) findViewById(R.id.nombre_loteria);
        paga_1 = (TextInputLayout) findViewById(R.id.Premio_1);
        paga_2 = (TextInputLayout) findViewById(R.id.Premio_2);
        limite = (TextInputLayout) findViewById(R.id.Limite_maximo);
        puesto = (TextInputLayout) findViewById(R.id.nombre_puesto);
        Button button = (Button) findViewById(R.id.button11);
        num_maquina = (TextInputLayout) findViewById(R.id.numero_maquina);
        comision = (TextInputLayout) findViewById(R.id.comision_vendedor);
        loteria = getIntent().getStringExtra("Loteria");
    }

    //##############Navegacion hacia atras personalizada!!!#########################################
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
    //##############################################################################################

    public boolean valid_comision_vendedor () {
        if (comision.getEditText()!=null){
            u7 = comision.getEditText().getText().toString().trim();
        }

        if (u7.isEmpty()) {

            comision.setError(getText(R.string.cantempty_comision));
            return false;

        }

        else if (u7.length() > 3) {

            comision.setError(getText(R.string.toolong_num));
            return false;
        }

        else if (Integer.parseInt(u7) > 100) {
            comision.setError(getText(R.string.porcentaje_muy_alto));
            return false;
        }

        else if (Integer.parseInt(u7) < 0) {
            comision.setError(getText(R.string.porcentaje_muy_bajo));
            return false;
        }

        else {
            comision.setError(null);

            //Aqui se programa las acciones que se van a ejecutar con este parametro.
            //Se crea una linea de texto que se va a agregar al archivo nuevo que se va a crear.
            String linea = "Comision_vendedor  " + u7 + "\n";
            nuevo_archivo = nuevo_archivo + linea;

            return true;
        }

    }

    public boolean valid_numero_maquina () {
        if (num_maquina.getEditText()!=null){
            u6 = num_maquina.getEditText().getText().toString().trim();
        }

        if (u6.isEmpty()) {

            num_maquina.setError(getText(R.string.cantempty_num_maquina));
            return false;

        }

        else if (u6.length() > 3) {

            num_maquina.setError(getText(R.string.toolong_num));
            return false;
        }

        else {
            num_maquina.setError(null);
            String linea = "Numero_maquina  " + u6 + "\n";
            nuevo_archivo = nuevo_archivo + linea;
            return true;
        }

    }

    public boolean validLoteryName () {
        if (nombre_loteria.getEditText()!=null){
            u1 = nombre_loteria.getEditText().getText().toString().trim();
        }

        if (u1.isEmpty()) {

            nombre_loteria.setError(getText(R.string.cantempty_nombre));
            return false;

        }

        else if (u1.length() > 15) {

            nombre_loteria.setError(getText(R.string.toolong));
            return false;
        }

        else {
            nombre_loteria.setError(null);
            //Do nothing. Este nombre se va a usar en la proxima activity (HorariosagregarActivity.java)
            return true;
        }

    }

    public boolean valid_nombre_puesto () {
        if (puesto.getEditText()!=null){
            u5 = puesto.getEditText().getText().toString().trim();
        }

        if (u5.isEmpty()) {

            puesto.setError(getText(R.string.cantempty_nombre_puesto));
            return false;

        }

        else if (u5.length() > 15) {

            puesto.setError(getText(R.string.toolong));
            return false;
        }

        else {
            puesto.setError(null);
            String linea = "Nombre_puesto  " + u5 + "\n";
            nuevo_archivo = nuevo_archivo + linea;
            return true;
        }

    }


    public boolean validPaga1(){
        if (paga_1.getEditText()!=null){
            u2 = paga_1.getEditText().getText().toString().trim();
        }

        if (u2.isEmpty()) {

            paga_1.setError(getText(R.string.cantempty_num));
            return false;

        }

        else if (u2.length() > 5) {

            paga_1.setError(getText(R.string.toolong_num));
            return false;
        }

        else {
            paga_1.setError(null);
            String linea = "Paga1  " + u2 + "\n";
            nuevo_archivo = nuevo_archivo + linea;
            return true;
        }

    }

    public boolean validPaga2(){
        if (paga_2.getEditText()!=null){
            u3 = paga_2.getEditText().getText().toString().trim();
        }

        if (u3.isEmpty()) {

            paga_2.setError(getText(R.string.cantempty_num));
            return false;

        }

        else if (u3.length() > 5) {

            paga_2.setError(getText(R.string.toolong_num));
            return false;
        }

        else {
            paga_2.setError(null);
            String linea = "Paga2  " + u3 + "\n";
            nuevo_archivo = nuevo_archivo + linea;
            return true;
        }

    }

    public boolean valid_max_limit(){
        if (limite.getEditText()!=null){
            u4 = limite.getEditText().getText().toString().trim();
        }

        if (u4.isEmpty()) {

            limite.setError(getText(R.string.cantempty_limit));
            return false;

        }

        else if (u4.length() > 7) {

            limite.setError(getText(R.string.toolong_num));
            return false;
        }

        else {
            limite.setError(null);
            String linea = "Limite_maximo  " + u4 + "\n";
            nuevo_archivo = nuevo_archivo + linea;
            return true;
        }
    }

    public void confirm(View view){
        if (!validLoteryName() | !validPaga1() | !validPaga2() | !valid_max_limit() | !valid_nombre_puesto() |
                !valid_numero_maquina() | !valid_comision_vendedor()){
            Toast.makeText(this, "Debe llenar todos los campos! ", Toast.LENGTH_LONG).show();
            //return;
        }
        else {

            //Agregar Toast personalizado para finjir guardar el archivo!!!
            //Pasar a la activity que selecciona los horarios de los juegos...
            //guardar(nuevo_archivo, nombre_archivo);//En lugar de guardar, se pasa a la nueva activity en la que se seleccionaran los horarios.
            select_horarios();
        }

    }

    private void select_horarios () {

            Intent horarios_agregar = new Intent(this, HorariosagregarActivity.class);
            horarios_agregar.putExtra("Archivo", nuevo_archivo);
            //horarios_agregar.putExtra("Nombre", nombre_archivo);
            horarios_agregar.putExtra("Tipo_loteria", loteria);
            horarios_agregar.putExtra("Nombre_loteria", u1);
            startActivity(horarios_agregar);
            finish();
            System.exit(0);

        }

    private boolean ArchivoExiste (String archivos [],String Tiquete){
        for (int i = 0; i < archivos.length; i++)
            if (Tiquete.equals(archivos[i]))
                return true;
        return false;
    }

}