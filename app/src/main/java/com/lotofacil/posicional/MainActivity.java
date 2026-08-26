package com.lotofacil.posicional;

import android.app.*;
import android.os.*;
import android.content.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import java.io.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private static final int PICK_FILE = 77;
    TextView logView, resultView, titleView;
    LinearLayout volanteGrid;
    Button pdfButton;
    AnaliseEngine.AnaliseResultado ultimoResultado;
    ProgressBar progressBar;
    RadioGroup radioGroup;
    String arquivoTexto = null;
    ExecutorService exec = Executors.newSingleThreadExecutor();

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(criarTela());
    }

    View criarTela() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,24,24,24); root.setBackgroundColor(Color.rgb(250,247,252));
        scroll.addView(root);

        TextView logo = new TextView(this); logo.setText("☘"); logo.setTextSize(54); logo.setTextColor(Color.rgb(106,27,154)); logo.setGravity(Gravity.CENTER);
        root.addView(logo, new LinearLayout.LayoutParams(-1, -2));

        titleView = new TextView(this); titleView.setText("LOTOFÁCIL POSICIONAL"); titleView.setTextSize(24); titleView.setTextColor(Color.rgb(59,11,89)); titleView.setGravity(Gravity.CENTER); titleView.setTypeface(null,1);
        root.addView(titleView);

        TextView sub = new TextView(this); sub.setText("Talo + Posicionamento do vídeo + ciclos + perímetro"); sub.setTextSize(15); sub.setGravity(Gravity.CENTER); root.addView(sub);

        Button importar = botao("IMPORTAR RESULTADOS"); root.addView(importar); importar.setOnClickListener(v -> abrirArquivo());

        TextView rep = new TextView(this); rep.setText("Repetidas do último concurso"); rep.setTextSize(18); rep.setPadding(0,22,0,6); root.addView(rep);
        radioGroup = new RadioGroup(this); radioGroup.setOrientation(RadioGroup.HORIZONTAL);
        for(int n:new int[]{8,9,10}) { RadioButton rb=new RadioButton(this); rb.setText(String.valueOf(n)); rb.setId(n); rb.setTextSize(18); radioGroup.addView(rb); }
        radioGroup.check(9); root.addView(radioGroup);

        Button analisar = botao("ANALISAR PRÓXIMA TENDÊNCIA"); root.addView(analisar); analisar.setOnClickListener(v -> analisar());

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal); progressBar.setIndeterminate(false); progressBar.setProgress(0); root.addView(progressBar, new LinearLayout.LayoutParams(-1, 24));

        logView = caixa("PROGRESSO\nAguardando arquivo..."); root.addView(logView);
        TextView melhor = new TextView(this); melhor.setText("MELHOR TENDÊNCIA"); melhor.setTextSize(19); melhor.setTypeface(null,1); melhor.setPadding(0,20,0,8); root.addView(melhor);
        volanteGrid = new LinearLayout(this); volanteGrid.setOrientation(LinearLayout.VERTICAL); root.addView(volanteGrid); montarVolante(null);
        resultView = caixa("RESULTADO\nImporte o histórico e toque em analisar."); root.addView(resultView);
        pdfButton = botao("GERAR PDF RESUMO"); pdfButton.setEnabled(false); root.addView(pdfButton); pdfButton.setOnClickListener(v -> gerarPdf());
        return scroll;
    }

    Button botao(String t) { Button b=new Button(this); b.setText(t); b.setTextColor(Color.WHITE); b.setTextSize(16); b.setBackgroundColor(Color.rgb(106,27,154)); return b; }
    TextView caixa(String t) { TextView v=new TextView(this); v.setText(t); v.setTextSize(15); v.setTextColor(Color.rgb(35,25,40)); v.setPadding(18,18,18,18); return v; }

    void abrirArquivo() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("text/*");
        startActivityForResult(i, PICK_FILE);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==PICK_FILE && resultCode==RESULT_OK && data!=null) {
            Uri uri=data.getData();
            try { arquivoTexto = ler(uri); log("Arquivo carregado. Pronto para analisar."); }
            catch(Exception e){ log("Erro ao ler arquivo: " + e.getMessage()); }
        }
    }

    String ler(Uri uri) throws Exception {
        InputStream in=getContentResolver().openInputStream(uri);
        BufferedReader br=new BufferedReader(new InputStreamReader(in));
        StringBuilder sb=new StringBuilder(); String line;
        while((line=br.readLine())!=null) sb.append(line).append('\n');
        br.close(); return sb.toString();
    }

    void analisar() {
        if(arquivoTexto==null) { log("Importe primeiro o arquivo de resultados."); return; }
        int repetidas = radioGroup.getCheckedRadioButtonId();
        progressBar.setIndeterminate(true); resultView.setText("Analisando..."); logView.setText("PROGRESSO\n");
        exec.submit(() -> {
            try {
                AnaliseEngine.AnaliseResultado r = AnaliseEngine.analisar(arquivoTexto, repetidas, this::log);
                runOnUiThread(() -> { ultimoResultado=r; progressBar.setIndeterminate(false); progressBar.setProgress(100); resultView.setText(r.texto); montarVolante(r.melhorJogo); pdfButton.setEnabled(true); });
            } catch(Exception e) {
                runOnUiThread(() -> { progressBar.setIndeterminate(false); resultView.setText("Erro: " + e.getMessage()); });
            }
        });
    }

    void montarVolante(int[] jogo) {
        volanteGrid.removeAllViews(); java.util.HashSet<Integer> sel=new java.util.HashSet<>(); if(jogo!=null) for(int n:jogo) sel.add(n);
        for(int r=0;r<5;r++){ LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER); for(int c=0;c<5;c++){ int n=r*5+c+1; TextView b=new TextView(this); b.setText(String.format(java.util.Locale.US,"%02d",n)); b.setGravity(Gravity.CENTER); b.setTextColor(Color.WHITE); b.setTextSize(15); android.graphics.drawable.GradientDrawable gd=new android.graphics.drawable.GradientDrawable(); gd.setShape(android.graphics.drawable.GradientDrawable.OVAL); gd.setColor(jogo==null?Color.rgb(120,120,120):(sel.contains(n)?Color.rgb(35,150,75):Color.rgb(210,55,55))); b.setBackground(gd); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(58,58); lp.setMargins(7,7,7,7); row.addView(b,lp);} volanteGrid.addView(row); }
    }

    void gerarPdf(){ if(ultimoResultado==null)return; exec.submit(() -> { try { Uri uri=PdfReportGenerator.gerar(this,ultimoResultado); runOnUiThread(() -> Toast.makeText(this,"PDF salvo em Downloads/Lotofacil Posicional",Toast.LENGTH_LONG).show()); } catch(Exception e){ runOnUiThread(() -> Toast.makeText(this,"Erro no PDF: "+e.getMessage(),Toast.LENGTH_LONG).show()); }}); }

    void log(String msg) { runOnUiThread(() -> logView.append("\n" + msg)); }
}
