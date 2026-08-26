package com.lotofacil.posicional;

import java.util.*;

public class PosicionalEngine {
    public static class ModeloPosicional {
        public final double[] centro = new double[15];
        public final double[] desvio = new double[15];
    }

    public static ModeloPosicional estudar(List<ResultadoParser.Resultado> hist, AnaliseEngine.Progress cb) {
        cb.log("Estudando P01 até P15 do conceito do vídeo...");
        ModeloPosicional m = new ModeloPosicional();
        int ini = Math.max(0, hist.size()-24);
        for (int p=0;p<15;p++) {
            double soma=0,peso=0; int w=1; ArrayList<Double> vals=new ArrayList<>();
            for (int i=ini;i<hist.size();i++,w++) { double v=hist.get(i).dezenas[p]; vals.add(v); soma += v*w; peso += w; }
            double media=soma/peso; double slope=slope(vals); m.centro[p]=media+1.5*slope;
            double normal=0; for(double v:vals) normal+=v; normal/=vals.size();
            double var=0; for(double v:vals)var+=(v-normal)*(v-normal); var/=Math.max(1, vals.size()-1);
            m.desvio[p]=Math.max(0.75, Math.sqrt(var));
        }
        return m;
    }

    public static double score(int[] jogo, ModeloPosicional m) {
        double total=0;
        for(int i=0;i<15;i++) {
            double z=Math.abs(jogo[i]-m.centro[i])/m.desvio[i];
            total += Math.exp(-0.5*z*z);
        }
        return 100.0*total/15.0;
    }

    private static double slope(ArrayList<Double> vals){ int n=vals.size(); if(n<2)return 0; double xm=(n-1)/2.0, ym=0; for(double v:vals)ym+=v; ym/=n; double num=0,den=0; for(int i=0;i<n;i++){num+=(i-xm)*(vals.get(i)-ym); den+=(i-xm)*(i-xm);} return den==0?0:num/den; }
}
