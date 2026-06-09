package gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

/**
 * La schermata iniziale: titolo luminoso, una nave che pattuglia il mare sullo
 * sfondo e i pulsanti per scegliere come giocare.
 */
public class SchermataMenu extends PannelloScena {

    private final BottoneNeon bottoneIA;
    private final BottoneNeon bottoneDue;
    private final BottoneNeon bottoneEsci;

    public SchermataMenu(FinestraGioco finestra) {
        bottoneIA = new BottoneNeon("⚔  SFIDA IL COMPUTER", Tema.CIANO);
        bottoneDue = new BottoneNeon("👥  DUE GIOCATORI", Tema.VERDE_OK);
        bottoneEsci = new BottoneNeon("✕  ESCI", Tema.ROSSO_NO);

        bottoneIA.addActionListener(e -> finestra.iniziaPartita(false));
        bottoneDue.addActionListener(e -> finestra.iniziaPartita(true));
        bottoneEsci.addActionListener(e -> System.exit(0));

        add(bottoneIA);
        add(bottoneDue);
        add(bottoneEsci);
    }

    @Override
    protected void posiziona(int w, int h) {
        int bw = 360, bh = 66, gap = 20;
        int cx = w / 2 - bw / 2;
        int y = (int) (h * 0.50f);
        bottoneIA.setBounds(cx, y, bw, bh);
        bottoneDue.setBounds(cx, y + (bh + gap), bw, bh);
        bottoneEsci.setBounds(cx + bw / 4, y + (bh + gap) * 2, bw / 2, bh - 8);
    }

    @Override
    protected void disegnaScena(Graphics2D g, int w, int h, float clock) {
        // nave-silhouette che pattuglia lentamente sullo sfondo
        disegnaNaveSfondo(g, w, h, clock);

        // titolo con leggero "respiro" luminoso
        float puls = (float) (Math.sin(clock * 1.6) * 0.5 + 0.5);
        Color colore = Tema.mescola(Tema.CIANO, Tema.CIANO_SOFT, puls);
        int cy = (int) (h * 0.26f);
        Tema.testoLuminoso(g, "BATTAGLIA NAVALE", w / 2, cy, Tema.titolo(Math.min(74, w / 16f)), colore);

        g.setColor(Tema.TESTO_SOFT);
        g.setFont(Tema.normale(Math.min(22, w / 52f)));
        String sub = "Affonda l'intera flotta nemica prima che lo facciano con la tua";
        int sw = g.getFontMetrics().stringWidth(sub);
        g.drawString(sub, w / 2 - sw / 2, cy + 42);

        // riga di firma in basso
        g.setColor(Tema.conAlpha(Tema.TESTO_SOFT, 120));
        g.setFont(Tema.normale(14));
        String firma = "Java • Swing • griglia 10×10 • flotta 5·4·3·3·2";
        int fw = g.getFontMetrics().stringWidth(firma);
        g.drawString(firma, w / 2 - fw / 2, h - 24);
    }

    /** Una sagoma di nave che scorre orizzontalmente, lasciando una scia. */
    private void disegnaNaveSfondo(Graphics2D g, int w, int h, float clock) {
        float t = (clock * 0.04f) % 1f;
        float x = t * (w + 400) - 200;
        float y = h * 0.70f;
        float s = Math.min(w, h) * 0.10f;

        // scia
        g.setColor(Tema.conAlpha(Tema.CIANO_SOFT, 30));
        g.fillOval((int) (x - s * 2.4f), (int) (y + s * 0.2f), (int) (s * 2.2f), (int) (s * 0.4f));

        GeneralPath scafo = new GeneralPath();
        scafo.moveTo(x - s, y);
        scafo.lineTo(x + s, y);
        scafo.lineTo(x + s * 0.7f, y + s * 0.35f);
        scafo.lineTo(x - s * 0.7f, y + s * 0.35f);
        scafo.closePath();
        g.setColor(Tema.conAlpha(new Color(20, 40, 66), 200));
        g.fill(scafo);
        // sovrastruttura
        g.fillRect((int) (x - s * 0.25f), (int) (y - s * 0.4f), (int) (s * 0.5f), (int) (s * 0.4f));
        // torre/antenna
        g.setColor(Tema.conAlpha(new Color(20, 40, 66), 200));
        g.fillRect((int) (x - s * 0.03f), (int) (y - s * 0.75f), (int) (s * 0.06f), (int) (s * 0.35f));
    }
}
