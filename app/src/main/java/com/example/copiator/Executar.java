package com.example.copiator;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

public class Executar extends AsyncTask<String, Integer, Void> {

    private Context context;
    private ProgressDialog progress;
    public Integer contadorarquivos = 0;

    public Executar(Context context) {

        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        progress = new ProgressDialog(context);
        progress.setMessage("Buscando e copiando...");
        progress.show();

    }

    @Override
    protected Void doInBackground(String... param)  {
        String escaneamento = param[0];
        String destino = param[1];
        String dataconsulta = param[2];
        String horainicio = param[3];
        String horafim = param[4];
        String tipoarquivo = param[5];

        try{
            runProcesso(escaneamento, destino, dataconsulta, horainicio, horafim, tipoarquivo);
        } catch (Exception e) {
            Log.d("errom", e.toString());
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... voids) {
        progress.setMessage("Arquivos copiados: " + String.valueOf(voids[0]));
        progress.show();

    }

    @Override
    protected void onPostExecute(Void voids) {
        progress.dismiss();

    }

    public void criaPastas(String caminho) {

        File novaPasta = new File(caminho);
        if (!novaPasta.exists()) {
            novaPasta.mkdirs();
        }
    }

    public void runProcesso(String local, String localcomplemento, String dataconsulta, String horaInicio, String horaFim, String tipo) throws IOException {

        File file;
        File[] arquivos;

        file = new File(local);
        arquivos = file.listFiles();
        for (File filetmp : arquivos) {
            if (filetmp.isFile()) {
                ArrayList<String> datahora = dataDeCriacao(filetmp.getAbsolutePath());
                if(verificaArquivo(dataconsulta, horaInicio, horaFim, tipo, filetmp.getAbsolutePath(), datahora.get(0), datahora.get(1))) {
                    contadorarquivos++;
                    criaPastas(localcomplemento);
                    copiaArquivo(filetmp.getAbsolutePath(), localcomplemento + "/" + filetmp.getName());
                    publishProgress(contadorarquivos);
                }
            } else if (filetmp.isDirectory()){
                if (filetmp.canRead()){
                    runProcesso(filetmp.getAbsolutePath(), localcomplemento, dataconsulta, horaInicio, horaFim, tipo);
                }
            }
        }
    }

    private ArrayList<String> dataDeCriacao(String arquivo){

        Path file = new File(arquivo).toPath();
        BasicFileAttributes attr = null;
        ArrayList<String> dados = new ArrayList<String>();
        try {
            attr = Files.readAttributes(file, BasicFileAttributes.class);
            final String s = attr.creationTime().toString();
            String data = s.substring(0, 10);
            String hora = s.substring(11, 19);
            dados.add(data);
            dados.add(hora);
            return dados;
        } catch (IOException e) {
            e.printStackTrace();
            return dados;
        }
    }

    private boolean comaparaData(String dataUsuario, String dataArquivo) {

        SimpleDateFormat modeloDataUsuario = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat modeloDataAquivo = new SimpleDateFormat("yyyy-MM-dd");
        try{
            Date dataUser = modeloDataUsuario.parse(dataUsuario);
            Date dataarquivo = modeloDataAquivo.parse(dataArquivo);

            return dataUser.equals(dataarquivo);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    private boolean verificaNomeArquivo(String endereco, String extencao) {

        if (Pattern.matches(".*" + extencao + "$", endereco)) {
            return true;
        } else {
            return false;
        }

    }

    private boolean comparaHora (String horaInicio, String horaFim, String horaArquivo) {

        SimpleDateFormat modeloHoraUser = new SimpleDateFormat("hh:mm");
        SimpleDateFormat modeloHoraAquivo = new SimpleDateFormat("hh:mm:ss");
        try{
            Date horaI = modeloHoraUser.parse(horaInicio);
            Date horaF = modeloHoraUser.parse(horaFim);
            Date horaA = modeloHoraAquivo.parse(horaArquivo);

            return  horaI.before(horaA) && horaF.after(horaA);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean verificaArquivo(String dataconsulta, String horaInicio, String horaFim, String tipo, String enderecoArquivo, String dataArquivo, String horaArquivo){
        final boolean extencao;
        final boolean datas;
        final boolean horas;

        extencao = verificaNomeArquivo(enderecoArquivo, tipo);
        datas = comaparaData(dataconsulta, dataArquivo);
        horas = comparaHora(horaInicio, horaFim, horaArquivo);

        final boolean b = extencao && datas && horas;
        return b;

    }

    private void copiaArquivo(String origem, String destino) throws IOException {
        InputStream in = new FileInputStream(origem);
        OutputStream out = new FileOutputStream(destino);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

}
