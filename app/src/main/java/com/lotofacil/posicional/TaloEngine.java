package com.lotofacil.posicional;

import java.util.*;

public class TaloEngine {
    public static class TaloScore {
        public final int[] grupo;
        public final double score;
        public final int[] arrasto;
        public final double slope;
        TaloScore(int[] grupo, double score, int[] arrasto, double slope) {
            this.grupo = grupo; this.score = score; this.arrasto = arrasto; this.slope = slope;
        }
    }

    public static List<TaloScore> rankear(int[] universo, int k, List<ResultadoParser.Resultado> historico, AnaliseEngine.Progress cb, String nome) {
        ArrayList<TaloScore> ranking = new ArrayList<>();
        ArrayList<Integer> atual = new ArrayList<>();
        int total = comb(universo.length, k);
        cb.log(nome + ": gerando " + total + " combinações...");
        int[] count = {0};
        combinar(universo, k, 0, atual, historico, ranking, count, total, cb, nome);
        ranking.sort((a,b) -> Double.compare(b.score, a.score));
        cb.log(nome + ": ranking concluído.");
        return ranking;
    }

    private static void combinar(int[] u, int k, int idx, ArrayList<Integer> atual, List<ResultadoParser.Resultado> hist,
                                 ArrayList<TaloScore> out, int[] count, int total, AnaliseEngine.Progress cb, String nome) {
        if (atual.size() == k) {
            int[] g = atual.stream().mapToInt(Integer::intValue).toArray();
            out.add(score(g, hist));
            count[0]++;
            int passo = Math.max(1, total / 10);
            if (count[0] % passo == 0 || count[0] == total) cb.log(nome + ": " + count[0] + "/" + total);
            return;
        }
        for (int i = idx; i <= u.length - (k - atual.size()); i++) {
            atual.add(u[i]);
            combinar(u, k, i+1, atual, hist, out, count, total, cb, nome);
            atual.remove(atual.size()-1);
        }
    }

    public static TaloScore score(int[] grupo, List<ResultadoParser.Resultado> historico) {
        int janela = Math.min(18, historico.size());
        int[] hits = new int[janela];
        Set<Integer> set = new HashSet<>(); for (int n:grupo) set.add(n);
        for (int i = 0; i < janela; i++) {
            int[] d = historico.get(historico.size() - janela + i).dezenas;
            int h = 0; for (int n:d) if (set.contains(n)) h++;
            hits[i] = h;
        }
        double slope = slope(hits);
        double recente = 0, pesos = 0;
        for (int i=0;i<hits.length;i++) { recente += hits[i]*(i+1); pesos += (i+1); }
        recente /= (grupo.length * pesos);
        int piso = (int)Math.ceil(grupo.length * 0.60);
        int pers=0; for (int h:hits) if (h>=piso) pers++;
        double persist = pers/(double)hits.length;
        double score = 55*recente + 20*persist + 15*Math.max(0,slope);
        return new TaloScore(grupo, score, hits, slope);
    }

    private static double slope(int[] vals) {
        int n = vals.length; if (n<2) return 0;
        double xm=(n-1)/2.0, ym=0; for(int v:vals) ym+=v; ym/=n;
        double num=0, den=0; for(int i=0;i<n;i++){ num+=(i-xm)*(vals[i]-ym); den+=(i-xm)*(i-xm); }
        return den==0?0:num/den;
    }

    private static int comb(int n, int k) {
        if (k < 0 || k > n) return 0; if (k > n-k) k = n-k;
        long r=1; for(int i=1;i<=k;i++) r = r*(n-k+i)/i; return (int)r;
    }
}
