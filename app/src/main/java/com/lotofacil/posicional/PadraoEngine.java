package com.lotofacil.posicional;

import java.util.*;

public class PadraoEngine {
    static final Set<Integer> PRIMOS = set(2,3,5,7,11,13,17,19,23);
    static final Set<Integer> FIB = set(1,2,3,5,8,13,21);
    static final Set<Integer> MIOLO = set(7,8,9,12,13,14,17,18,19);
    static final Set<Integer> CRUZ = set(3,8,11,12,13,14,15,18,23);
    static final Set<Integer> PERIMETRO = set(1,2,3,4,5,6,10,11,15,16,20,21,22,23,24,25);

    static Set<Integer> set(int... v){ HashSet<Integer> s=new HashSet<>(); for(int n:v)s.add(n); return s; }

    public static class F {
        int soma, pares, primos, fib, miolo, cruz, perimetro, seq;
        int[] linhas = new int[5]; int[] colunas = new int[5];
    }

    public static F features(int[] jogo) {
        F f = new F(); f.soma=0;
        HashSet<Integer> s = new HashSet<>(); for(int n:jogo)s.add(n);
        int cur=0;
        for (int n=1;n<=25;n++) {
            if (s.contains(n)) { cur++; f.seq=Math.max(f.seq,cur); } else cur=0;
        }
        for (int n:jogo) {
            f.soma += n;
            if (n%2==0) f.pares++;
            if (PRIMOS.contains(n)) f.primos++;
            if (FIB.contains(n)) f.fib++;
            if (MIOLO.contains(n)) f.miolo++;
            if (CRUZ.contains(n)) f.cruz++;
            if (PERIMETRO.contains(n)) f.perimetro++;
            int z=n-1; f.linhas[z/5]++; f.colunas[z%5]++;
        }
        return f;
    }

    public static double scoreEstrutura(int[] jogo, List<ResultadoParser.Resultado> hist) {
        F f = features(jogo);
        double score = 0;
        score += curva(f.soma, media(hist,"soma"), 18);
        score += curva(f.pares, media(hist,"pares"), 1.5);
        score += curva(f.primos, media(hist,"primos"), 1.4);
        score += curva(f.fib, media(hist,"fib"), 1.2);
        score += curva(f.miolo, media(hist,"miolo"), 1.4);
        score += curva(f.perimetro, media(hist,"perimetro"), 1.6);
        score += curva(f.cruz, media(hist,"cruz"), 1.5);
        score += curva(f.seq, media(hist,"seq"), 1.6);
        return 100.0 * score / 8.0;
    }

    private static double curva(double valor, double alvo, double abertura) {
        double z = Math.abs(valor-alvo)/abertura;
        return Math.exp(-0.5*z*z);
    }

    private static double media(List<ResultadoParser.Resultado> hist, String campo) {
        int ini = Math.max(0, hist.size()-80); double soma=0, peso=0; int w=1;
        for(int i=ini;i<hist.size();i++,w++) {
            F f=features(hist.get(i).dezenas); double v=0;
            switch(campo){case "soma":v=f.soma;break;case "pares":v=f.pares;break;case "primos":v=f.primos;break;case "fib":v=f.fib;break;case "miolo":v=f.miolo;break;case "perimetro":v=f.perimetro;break;case "cruz":v=f.cruz;break;case "seq":v=f.seq;break;}
            soma += v*w; peso += w;
        }
        return soma/peso;
    }

    public static String descricao(int[] jogo) {
        F f=features(jogo);
        return "Soma " + f.soma + " | Pares " + f.pares + " | Primos " + f.primos +
                " | Fib " + f.fib + " | Miolo " + f.miolo + " | Perímetro " + f.perimetro +
                " | Cruz " + f.cruz + " | Seq " + f.seq +
                "\nLinhas " + Arrays.toString(f.linhas) + " | Colunas " + Arrays.toString(f.colunas);
    }
}
