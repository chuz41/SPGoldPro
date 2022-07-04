package com.example.spgold;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AdministrarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrar);
    }

    //////////////////Personalizacion de la navegacion hacia atras!//////////////////
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
    ////////////////////////////////////////////////////////////////////////////////

    public void ingresar_numeros(View view){
        Intent Ingresar = new Intent(this, IngresarnumActivity.class);
        startActivity(Ingresar);
        finish();
        System.exit(0);
    }

    public void vendedores(View view){
        Intent Vendedor = new Intent(this, VendedoresActivity.class);
        startActivity(Vendedor);
        finish();
        System.exit(0);
    }

    public void Cierre(View view){
        Intent Cierres = new Intent(this, CierreActivity.class);
        startActivity(Cierres);
        finish();
        System.exit(0);
    }

    public void editar_loterias(View view){
        Intent Editar = new Intent(this, EditonlineActivity.class);
        startActivity(Editar);
        finish();
        System.exit(0);
    }
}