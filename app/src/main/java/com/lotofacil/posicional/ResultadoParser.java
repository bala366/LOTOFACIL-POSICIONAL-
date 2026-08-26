package com.lotofacil.posicional;

import java.util.*;
import java.util.regex.*;

public class ResultadoParser {
    public static class Resultado {
        public final int concurso;
        public final int[] dezenas;
        public Resultado(int concurso, int[] dezenas) { this.concurso = concurso; this.dezenas = dezenas; }
    }

    public static List<Resultado> parse(String texto) {
        List<Resultado> lista = new ArrayList<>();
        String[] linhas = texto.split("\\r?\\n");
        Pattern p = Pattern.compile("\\d+");
        for (String linha : linhas) {
            Matcher m = p.matcher(linha);
            ArrayList<Integer> vals = new ArrayList<>();
            while (m.find()) vals.add(Integer.parseInt(m.group()));
            if (vals.size() < 15) continue;
            int concurso = vals.size() >= 16 ? vals.get(0) : lista.size() + 1;
            TreeSet<Integer> set = new TreeSet<>();
            int start = vals.size() >= 16 ? vals.size() - 15 : 0;
            for (int i = start; i < vals.size(); i++) {
                int n = vals.get(i);
                if (n >= 1 && n <= 25) set.add(n);
            }
            if (set.size() == 15) {
                int[] d = new int[15]; int i=0; for (int n:set) d[i++] = n;
                lista.add(new Resultado(concurso, d));
            }
        }
        lista.sort(Comparator.comparingInt(r -> r.concurso));
        return lista;
    }
}
