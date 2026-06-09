package gui;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * La schermata di fine partita: un grande verdetto (vittoria o sconfitta), un
 * sottotitolo e i pulsanti per rigiocare o tornare al menu. In caso di trionfo
 * piovono coriandoli luminosi; in caso di sconfitta l'atmosfera resta cupa.
 */
public class SchermataFine extends PannelloScena {

    private final boolean trionfo;
    private final String titolo;
    private final String sottotitolo;
    private final BottoneNeon rigioca;
    private final BottoneNeon menu;

    public SchermataFine(FinestraGioco finestra, boolean trionfo, String titolo, String sottotitolo) {
        this.trionfo = trionfo;
        this.titolo = titolo;
        this.sottotitolo = sottotitolo;
        rigioca = new BottoneNeon("↺  RIGIOCA", trionfo ? Tema.ORO : Tema.CIANO);
        menu = new BottoneNeon("⌂  MENU PRINCIPALE", Tema.CIANO);
        rigioca.addActionListener(e -> finestra.mostraMenu());
        menu.addActionListener(e -> finestra.mostraMenu());
        add(rigioca);
        add(menu);
    }

    @Override
    protected void posiziona(int w, int h) {
        int bw = 300, bh = 60, gap = 18;
        rigioca.setBounds(w / 2 - bw - gap / 2, (int) (h * 0.62f), bw, bh);
        menu.setBounds(w / 2 + gap / 2, (int) (h * 0.62f), bw, bh);
    }

    @Override
    protected void disegnaScena(Graphics2D g, int w, int h, float clock) {
        Color colore = trionfo ? Tema.ORO : Tema.ROSSO_NO;

        if (trionfo) coriandoli(g, w, h, clock);

        float puls = (float) (Math.sin(clock * 2.2) * 0.5 + 0.5);
        Tema.alone(g, w / 2, (int) (h * 0.36f), (int) (h * 0.4f), Tema.conAlpha(colore, (int) (40 + 40 * puls)));
        Tema.testoLuminoso(g, titolo, w / 2, (int) (h * 0.38f),
                Tema.titolo(Math.min(96, w / 11f)), colore);

        g.setColor(Tema.TESTO);
        g.setFont(Tema.normale(Math.min(24, w / 44f)));
        int sw = g.getFontMetrics().stringWidth(sottotitolo);
        g.drawString(sottotitolo, w / 2 - sw / 2, (int) (h * 0.38f) + 48);
    }

    /** Coriandoli che cadono ruotando, in colori caldi. */
    private void coriandoli(Graphics2D g, int w, int h, float clock) {
        int n = 90;
        for (int i = 0; i < n; i++) {
            float seed = i * 12.9898f;
            float colx = (frac((float) Math.sin(seed) * 43758.5f));
            float vel = 0.06f + frac(seed * 1.7f) * 0.12f;
            float prog = (clock * vel + frac(seed)) % 1f;
            float x = colx * w + (float) Math.sin(prog * 8 + i) * 26;
            float y = prog * (h + 40) - 20;
            Color c = switch (i % 4) {
                case 0 -> Tema.ORO;
                case 1 -> Tema.GIALLO_FUOCO;
                case 2 -> Tema.CIANO_SOFT;
                default -> Tema.ARANCIO;
            };
            g.setColor(Tema.conAlpha(c, 200));
            double ang = prog * 12 + i;
            int s = 6;
            int dx = (int) (Math.cos(ang) * s);
            int dy = (int) (Math.sin(ang) * s);
            g.fillRect((int) x - dx / 2, (int) y - dy / 2, Math.max(2, Math.abs(dx) + 3), Math.max(2, Math.abs(dy) + 3));
        }
    }

    private static float frac(float v) { v = v - (int) v; return v < 0 ? v + 1 : v; }
}
