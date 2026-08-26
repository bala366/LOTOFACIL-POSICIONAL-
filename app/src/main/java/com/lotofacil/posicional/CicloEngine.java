package com.lotofacil.posicional;

import java.util.*;

public class CicloEngine {
    public static class Ciclos { public Set<Integer> faltandoAtual = new TreeSet<>(); public List<Set<Integer>> fortes = new ArrayList<>(); }

    public static Ciclos estudar(List<ResultadoParser.Resultado> hist, AnaliseEngine.Progress cb) {
        cb.log("Estudando ciclo aberto e ciclos completos...");
        ArrayList<int[]> completos = new ArrayList<>();
        Set<Integer> faltando = base(); int inicio=0;
        for(int i=0;i<hist.size();i++) {
            for(int n:hist.get(i).dezenas) faltando.remove(n);
            if(faltando.isEmpty()) { completos.add(new int[]{inicio,i}); inicio=i+1; faltando=base(); }
        }
        Ciclos c = new Ciclos(); c.faltandoAtual.addAll(faltando);
        for(int j=completos.size()-1;j>=0 && c.fortes.size()<3;j--) {
            int[] inter=completos.get(j); HashMap<Integer,Integer> freq=new HashMap<>();
            for(int i=inter[0];i<=inter[1];i++) for(int n:hist.get(i).dezenas) freq.put(n, freq.getOrDefault(n,0)+1);
            ArrayList<Integer> nums=new ArrayList<>(freq.keySet()); nums.sort((a,b)->freq.get(b)-freq.get(a));
            c.fortes.add(new HashSet<>(nums.subList(0, Math.min(8, nums.size()))));
        }
        cb.log("Ciclo atual aberto: faltam " + c.faltandoAtual.size() + " dezenas para fechar.");
        return c;
    }

    public static double score(int[] jogo, Ciclos c) {
        Set<Integer> sj=new HashSet<>(); for(int n:jogo)sj.add(n);
        double total=0; double[] pesos={0.5,0.3,0.2};
        for(int i=0;i<c.fortes.size();i++) { Set<Integer> s=new HashSet<>(sj); s.retainAll(c.fortes.get(i)); total += pesos[i]*(s.size()/8.0); }
        Set<Integer> falt=new HashSet<>(sj); falt.retainAll(c.faltandoAtual);
        double aberto = c.faltandoAtual.isEmpty()?0.5:(falt.size()/(double)c.faltandoAtual.size());
        return Math.min(100, Math.max(0, 100*(0.85*total + 0.15*aberto)));
    }

    private static Set<Integer> base(){ Set<Integer> s=new TreeSet<>(); for(int n=1;n<=25;n++)s.add(n); return s; }
}
