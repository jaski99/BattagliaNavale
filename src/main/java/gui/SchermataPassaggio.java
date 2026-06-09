package gui;

import java.awt.Graphics2D;

/**
 * Schermata "ponte" mostrata tra due fasi (per esempio quando si passa il
 * computer dall'uno all'altro giocatore). Mostra un titolo, un sottotitolo e un
 * pulsante per proseguire quando si è pronti.
 */
public class SchermataPassaggio extends PannelloScena {

    private final String titolo;
    private final String sottotitolo;
    private final BottoneNeon avanti;

    public SchermataPassaggio(String titolo, String sottotitolo, Runnable onContinua) {
        this.titolo = titolo;
        this.sottotitolo = sottotitolo;
        this.avanti = new BottoneNeon("SONO PRONTO  ▶", Tema.CIANO);
        avanti.addActionListener(e -> onContinua.run());
        add(avanti);
    }

    @Override
    protected void posiziona(int w, int h) {
        int bw = 320, bh = 64;
        avanti.setBounds(w / 2 - bw / 2, (int) (h * 0.58f), bw, bh);
    }

    @Override
    protected void disegnaScena(Graphics2D g, int w, int h, float clock) {
        Tema.testoLuminoso(g, titolo, w / 2, (int) (h * 0.40f),
                Tema.titolo(Math.min(60, w / 18f)), Tema.CIANO_SOFT);
        g.setColor(Tema.TESTO);
        g.setFont(Tema.normale(Math.min(24, w / 44f)));
        int sw = g.getFontMetrics().stringWidth(sottotitolo);
        g.drawString(sottotitolo, w / 2 - sw / 2, (int) (h * 0.40f) + 44);
    }
}
