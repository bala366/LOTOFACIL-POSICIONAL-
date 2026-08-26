package com.lotofacil.posicional;

import java.util.*;

public class AnaliseEngine {
    public interface Progress { void log(String msg); }
    public static class AnaliseResultado { public String texto; public int[] melhorJogo; }

    public static AnaliseResultado analisar(String textoArquivo, int repetidas, Progress cb) {
        cb.log("Carregando histórico...");
        List<ResultadoParser.Resultado> hist = ResultadoParser.parse(textoArquivo);
        if (hist.size() < 30) throw new IllegalArgumentException("Histórico insuficiente: " + hist.size() + " concursos.");
        cb.log("Concursos carregados: " + hist.size() + " | último: " + hist.get(hist.size()-1).concurso);

        ResultadoParser.Resultado ultimo = hist.get(hist.size()-1);
        int[] espelho = espelho(ultimo.dezenas);
        List<ResultadoParser.Resultado> histTalo = hist.subList(0, hist.size()-1);

        CicloEngine.Ciclos ciclos = CicloEngine.estudar(hist, cb);
        PosicionalEngine.ModeloPosicional modelo = PosicionalEngine.estudar(hist, cb);

        List<TaloEngine.TaloScore> reps = TaloEngine.rankear(ultimo.dezenas, repetidas, histTalo, cb, "Talo das repetidas");
        List<TaloEngine.TaloScore> esps = TaloEngine.rankear(espelho, 15-repetidas, histTalo, cb, "Talo do espelho");

        int top = 150;
        int maxR = Math.min(top, reps.size()), maxE = Math.min(top, esps.size());
        cb.log("Cruzando " + maxR + " x " + maxE + " talos...");
        ArrayList<Candidato> candidatos = new ArrayList<>();
        int total=maxR*maxE, proc=0, passo=Math.max(1,total/10);
        for(int i=0;i<maxR;i++) {
            for(int j=0;j<maxE;j++) {
                proc++;
                int[] jogo = juntar(reps.get(i).grupo, esps.get(j).grupo);
                double pos = PosicionalEngine.score(jogo, modelo);
                double estrutura = PadraoEngine.scoreEstrutura(jogo, hist);
                double ciclo = CicloEngine.score(jogo, ciclos);
                double taloPenalty = 0;
                if (reps.get(i).slope < -0.02) taloPenalty += 4;
                if (esps.get(j).slope < -0.02) taloPenalty += 6;
                double finalScore = 0.34*reps.get(i).score + 0.24*esps.get(j).score + 0.18*pos + 0.14*estrutura + 0.10*ciclo - taloPenalty;
                candidatos.add(new Candidato(jogo, finalScore, reps.get(i), esps.get(j), pos, estrutura, ciclo));
                if(proc % passo == 0 || proc == total) cb.log("Cruzamento: " + proc + "/" + total);
            }
        }
        candidatos.sort((a,b)->Double.compare(b.score, a.score));
        Candidato melhor = candidatos.get(0);

        StringBuilder sb = new StringBuilder();
        sb.append("LOTOFÁCIL POSICIONAL\n\n");
        sb.append("Último concurso: ").append(ultimo.concurso).append("\n");
        sb.append("Último resultado: ").append(fmt(ultimo.dezenas)).append("\n");
        sb.append("Modelo: ").append(repetidas).append(" repetidas + ").append(15-repetidas).append(" do espelho\n\n");
        sb.append("MELHOR TENDÊNCIA\n").append(fmt(melhor.jogo)).append("\n\n");
        sb.append("Score final: ").append(round(melhor.score)).append("\n");
        sb.append("Talo repetidas: ").append(round(melhor.rep.score)).append(" | slope ").append(round(melhor.rep.slope)).append("\n");
        sb.append("Talo espelho: ").append(round(melhor.esp.score)).append(" | slope ").append(round(melhor.esp.slope)).append("\n");
        sb.append("Posicionamento do vídeo: ").append(round(melhor.pos)).append("\n");
        sb.append("Estrutura/perímetro: ").append(round(melhor.estrutura)).append("\n");
        sb.append("Ciclos: ").append(round(melhor.ciclo)).append("\n\n");
        sb.append("Grupo repetidas: ").append(fmt(melhor.rep.grupo)).append("\n");
        sb.append("Arrasto repetidas: ").append(Arrays.toString(melhor.rep.arrasto)).append("\n\n");
        sb.append("Grupo espelho: ").append(fmt(melhor.esp.grupo)).append("\n");
        sb.append("Arrasto espelho: ").append(Arrays.toString(melhor.esp.arrasto)).append("\n\n");
        sb.append(PadraoEngine.descricao(melhor.jogo)).append("\n\n");
        sb.append("TOP 10\n");
        for(int i=0;i<Math.min(10,candidatos.size());i++) sb.append(i+1).append(" - ").append(round(candidatos.get(i).score)).append(" - ").append(fmt(candidatos.get(i).jogo)).append("\n");

        AnaliseResultado ar = new AnaliseResultado(); ar.texto=sb.toString(); ar.melhorJogo=melhor.jogo; return ar;
    }

    static class Candidato { int[] jogo; double score,pos,estrutura,ciclo; TaloEngine.TaloScore rep,esp; Candidato(int[] jogo,double score,TaloEngine.TaloScore rep,TaloEngine.TaloScore esp,double pos,double estrutura,double ciclo){this.jogo=jogo;this.score=score;this.rep=rep;this.esp=esp;this.pos=pos;this.estrutura=estrutura;this.ciclo=ciclo;} }

    private static int[] espelho(int[] ultimo){ Set<Integer> s=new HashSet<>(); for(int n:ultimo)s.add(n); int[] e=new int[10]; int idx=0; for(int n=1;n<=25;n++) if(!s.contains(n)) e[idx++]=n; return e; }
    private static int[] juntar(int[] a,int[] b){ TreeSet<Integer> s=new TreeSet<>(); for(int n:a)s.add(n); for(int n:b)s.add(n); int[] out=new int[s.size()]; int i=0; for(int n:s)out[i++]=n; return out; }
    public static String fmt(int[] jogo){ StringBuilder sb=new StringBuilder(); for(int i=0;i<jogo.length;i++){ if(i>0)sb.append(' '); if(jogo[i]<10)sb.append('0'); sb.append(jogo[i]); } return sb.toString(); }
    private static String round(double v){ return String.format(Locale.US, "%.2f", v).replace('.', ','); }
}
