package com.lotofacil.posicional;

import android.content.*;
import android.graphics.*;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import java.io.*;
import java.util.*;

public class PdfReportGenerator {
    public static Uri gerar(Context ctx, AnaliseEngine.AnaliseResultado r) throws Exception {
        String nome = "Lotofacil_Posicional_Resumo_" + System.currentTimeMillis() + ".pdf";
        Uri uri;
        OutputStream out;
        if (Build.VERSION.SDK_INT >= 29) {
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.Downloads.DISPLAY_NAME, nome);
            cv.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            cv.put(MediaStore.Downloads.RELATIVE_PATH, "Download/Lotofacil Posicional");
            uri = ctx.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
            if (uri == null) throw new IOException("Nao foi possivel criar o PDF em Downloads.");
            out = ctx.getContentResolver().openOutputStream(uri);
        } else {
            File dir = ctx.getExternalFilesDir(null);
            File f = new File(dir, nome); uri = Uri.fromFile(f); out = new FileOutputStream(f);
        }

        PdfDocument doc = new PdfDocument();
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(595,842,1).create();
        PdfDocument.Page page = doc.startPage(info); Canvas c = page.getCanvas();
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); p.setTextSize(18); p.setColor(Color.rgb(45,25,55));
        c.drawText("LOTOFACIL POSICIONAL - RELATORIO DO CANDIDATO", 34, 45, p);
        p.setTypeface(Typeface.DEFAULT); p.setTextSize(9); p.setColor(Color.DKGRAY);
        c.drawText("Verde = selecionada pelo motor | Vermelho = fora do candidato",34,65,p);
        c.drawText("Estudo estatistico de tendencia; nao garante resultado de sorteio.",34,79,p);

        Set<Integer> sel = new HashSet<>(); for(int n:r.melhorJogo) sel.add(n);
        float startX=75, startY=120, dx=95, dy=66, rad=22;
        p.setTextAlign(Paint.Align.CENTER); p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD)); p.setTextSize(12);
        for(int n=1;n<=25;n++){
            int row=(n-1)/5, col=(n-1)%5; float x=startX+col*dx, y=startY+row*dy;
            p.setColor(sel.contains(n)?Color.rgb(35,150,75):Color.rgb(210,55,55)); c.drawCircle(x,y,rad,p);
            p.setColor(Color.WHITE); c.drawText(String.format(Locale.US,"%02d",n),x,y+4,p);
        }
        p.setTextAlign(Paint.Align.LEFT); p.setColor(Color.BLACK); p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD)); p.setTextSize(13);
        float y=420; c.drawText("RESUMO DO MOTOR",34,y,p); y+=18;
        p.setTypeface(Typeface.DEFAULT); p.setTextSize(9.5f);
        String[] linhas=r.texto.split("\\n");
        for(String linha:linhas){
            if(linha.trim().isEmpty()) { y+=5; continue; }
            if(y>805) break;
            c.drawText(linha.length()>95?linha.substring(0,95):linha,34,y,p); y+=13;
        }
        doc.finishPage(page); doc.writeTo(out); out.close(); doc.close(); return uri;
    }
}
