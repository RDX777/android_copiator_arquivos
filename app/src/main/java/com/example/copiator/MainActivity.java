package com.example.copiator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    protected EditText ed_nomepaciente;
    protected EditText ed_dataconsulta;
    protected EditText ed_horainicio;
    protected EditText ed_horafim;
    protected Spinner sp_tipoarquivo;
    protected Spinner sp_localescaneamento;
    protected TextView tv_local;
    protected Button bt_copia;

    //Usando para definir permissões
    protected static final int CODIGO_PERMISSOES_REQUERIDAS = 1;
    protected String[] appPermissioes = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    //Usando para definir permissões

    //Endereços
    public Map<String, String> CAMINHOS = new HashMap<>();
    //Endereços

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissoes();

        //Captura eventos dos botões

        ed_nomepaciente = findViewById(R.id.ed_nomepaciente);
        ed_dataconsulta = findViewById(R.id.ed_dataconsulta);
        ed_horainicio = findViewById(R.id.ed_horainicio);
        ed_horafim = findViewById(R.id.ed_horafim);
        sp_tipoarquivo = findViewById(R.id.sp_tipoarquivo);
        sp_localescaneamento = findViewById(R.id.sp_localescaneamento);

        pastasInternas();

        tv_local = findViewById(R.id.tv_local);
        bt_copia = findViewById(R.id.bt_copia);

        //Captura eventos dos botões

    }

    protected void permissoes(){

        for (String permissao : appPermissioes){
            if (ContextCompat.checkSelfPermission(this, permissao) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{permissao}, CODIGO_PERMISSOES_REQUERIDAS);
            } else {
                mostraMensagem("Permissões Ativas.");
            }
        }
    }

    public void mostraMensagem(String mensagem) {

        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }

    protected void pastasInternas() {
        File file;
        File[] pastas;
        ArrayList<String> listadepastas = new ArrayList<String>();
        listadepastas.add("Raiz");
        CAMINHOS.put("Raiz", "/storage/emulated/0");


        file = new File("/storage/emulated/0/");
        pastas = file.listFiles();
        for (File filetmp : pastas) {
            if (filetmp.isDirectory()) {
                listadepastas.add(filetmp.getName());
                CAMINHOS.put(filetmp.getName(), filetmp.getAbsolutePath());
            }
        }
        for (String a : listadepastas) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, listadepastas);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp_localescaneamento.setAdapter(adapter);
        }
    }

    public void inicio(View view) {

        if (validaCampos()) {


            String ednome = ed_nomepaciente.getText().toString();
            String dia = ed_dataconsulta.getText().toString().substring(0, 2);
            String mes = ed_dataconsulta.getText().toString().substring(3, 5);
            String ano = ed_dataconsulta.getText().toString().substring(6, 10);
            String spcaminhoescaneamento = CAMINHOS.get(sp_localescaneamento.getSelectedItem().toString());
            String caminhodestino = CAMINHOS.get(sp_localescaneamento.getSelectedItem().toString()) + "/Copiator/" +  ed_nomepaciente.getText().toString() + "/" + ano + "/" + mes + "/" + dia;
            String dataconsulta = ed_dataconsulta.getText().toString();
            String horainicio = ed_horainicio.getText().toString();
            String horafim = ed_horafim.getText().toString();
            String tipoarquivo = sp_tipoarquivo.getSelectedItem().toString();

            tv_local.setText("/Raiz/Copiator/" +  ednome + "/" + ano + "/" + mes + "/" + dia);

            Executar exe = new Executar(this);
            exe.execute(spcaminhoescaneamento, caminhodestino, dataconsulta, horainicio, horafim, tipoarquivo);

        }

    }

    protected boolean validaEmOrdem() {
        SimpleDateFormat modeloHoraUser = new SimpleDateFormat("hh:mm");
        try{
            Date horainicio = modeloHoraUser.parse(ed_horainicio.getText().toString());
            Date horafim = modeloHoraUser.parse(ed_horafim.getText().toString());

            if (horainicio.before(horafim)) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean validaData(String data) {
        SimpleDateFormat modeloDataUsuario = new SimpleDateFormat("dd/MM/yyyy");
        try{
            modeloDataUsuario.parse(data);
            if (data.length() != 10) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }

    }

    protected boolean validaHora (String hora) {
        SimpleDateFormat modeloHoraUser = new SimpleDateFormat("hh:mm");
        try{
            modeloHoraUser.parse(hora);
            if (hora.length() != 5) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean validaNome (String nome) {
        if (nome.length() == 0) {
            return false;
        } else {
            return true;
        }
    }

    protected boolean validaCampos () {
        Boolean nome = true;
        boolean dataconsulta = true;
        boolean horainicio = true;
        boolean horafim = true;

        //Valida nome
        if (!validaNome(ed_nomepaciente.getText().toString())) {
            nome = false;
            ed_nomepaciente.setError("Insira um nome");
            ed_nomepaciente.requestFocus();
        }

        //Valida data da consulta
        if (!validaData(ed_dataconsulta.getText().toString())) {
            dataconsulta = false;
            ed_dataconsulta.setError("Verifique este campo");
            ed_dataconsulta.requestFocus();
        }

        //Valida hora de entrada da consulta
        if (!validaHora(ed_horainicio.getText().toString())) {
            horainicio = false;
            ed_horainicio.setError("Verifique este campo");
            ed_horainicio.requestFocus();
        } else if (validaEmOrdem()) {
            horainicio = false;
            ed_horainicio.setError("O inicio não pode ser maior que o fim");
            ed_horainicio.requestFocus();
        }

        //Valida hora de saida da consulta
        if (!validaHora(ed_horafim.getText().toString())) {
            horafim = false;
            ed_horafim.setError("Verifique este campo");
            ed_horafim.requestFocus();
        }

        return nome && dataconsulta && horainicio && horafim;

    }

}