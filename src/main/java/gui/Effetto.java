package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Un effetto visivo che vive per un breve istante sopra una cella: lo spruzzo di
 * un colpo a vuoto, l'esplosione di un colpo a segno, o la grande deflagrazione
 * di una nave che affonda.
 *
 * Ogni effetto nasce con un istante di partenza e una durata. Conosce il suo
 * "avanzamento" (da 0 a 1) e si disegna di conseguenza; quando arriva a 1 ha
 * finito e il pannello lo rimuove. Le particelle usano numeri casuali fissati
 * alla nascita, così l'animazione è stabile e non "tremola".
 */
public class Effetto {

    public enum Tipo { SPRUZZO, ESPLOSIONE, AFFONDAMENTO }

    private final Tipo tipo;
    public final int riga;
    public final int colonna;
    private final float nascita;     // istante (in secondi) in cui è comparso
    private final float durata;      // quanto vive, in secondi
    private final Random rnd;

    // dati delle particelle, calcolati una volta sola
    private final double[] angolo;
    private final double[] velocita;
    private final int numParticelle;

    public Effetto(Tipo tipo, int riga, int colonna, float clockAttuale) {
        this.tipo = tipo;
        this.riga = riga;
        this.colonna = colonna;
        this.nascita = clockAttuale;
        this.durata = switch (tipo) {
            case SPRUZZO -> 0.65f;
            case ESPLOSIONE -> 0.85f;
            case AFFONDAMENTO -> 1.4f;
        };
        this.rnd = new Random((long) riga * 73856093 ^ (long) colonna * 19349663 ^ tipo.ordinal());
        this.numParticelle = switch (tipo) {
            case SPRUZZO -> 10;
            case ESPLOSIONE -> 16;
            case AFFONDAMENTO -> 28;
        };
        this.angolo = new double[numParticelle];
        this.velocita = new double[numParticelle];
        for (int i = 0; i < numParticelle; i++) {
            angolo[i] = rnd.nextDouble() * Math.PI * 2;
            velocita[i] = 0.5 + rnd.nextDouble();
        }
    }

    /** Avanzamento da 0 (appena nato) a 1 (finito), dato l'orologio attuale. */
    public float avanzamento(float clock) {
        return (clock - nascita) / durata;
    }

    public boolean finito(float clock) {
        return avanzamento(clock) >= 1f;
    }

    /**
     * Disegna l'effetto centrato nella cella.
     * @param cx,cy centro in pixel della cella
     * @param cella lato della cella in pixel (per scalare tutto)
     */
    public void disegna(Graphics2D g, float cx, float cy, int cella, float clock) {
        float t = Math.max(0, Math.min(1, avanzamento(clock)));
        switch (tipo) {
            case SPRUZZO -> spruzzo(g, cx, cy, cella, t);
            case ESPLOSIONE -> esplosione(g, cx, cy, cella, t, 1f);
            case AFFONDAMENTO -> esplosione(g, cx, cy, cella, t, 1.7f);
        }
    }

    // --- COLPO A VUOTO: anelli d'acqua + goccioline ---------------------------
    private void spruzzo(Graphics2D g, float cx, float cy, int cella, float t) {
        float max = cella * 0.55f;
        // due o tre anelli concentrici che si allargano e svaniscono
        for (int k = 0; k < 3; k++) {
            float tt = t - k * 0.12f;
            if (tt <= 0 || tt >= 1) continue;
            float raggio = max * tt;
            int alpha = (int) (180 * (1 - tt));
            g.setStroke(new BasicStroke(Math.max(1f, cella * 0.05f * (1 - tt))));
            g.setColor(Tema.conAlpha(Tema.CIANO_SOFT, alpha));
            g.drawOval((int) (cx - raggio), (int) (cy - raggio * 0.6f),
                    (int) (raggio * 2), (int) (raggio * 1.2f));
        }
        // goccioline che schizzano verso l'alto e ricadono
        for (int i = 0; i < numParticelle; i++) {
            float dist = (float) (velocita[i] * cella * 0.5f * t);
            float gx = cx + (float) Math.cos(angolo[i]) * dist;
            float gy = cy + (float) Math.sin(angolo[i]) * dist * 0.5f
                    - (float) Math.sin(t * Math.PI) * cella * 0.4f; // arco verso l'alto
            int alpha = (int) (220 * (1 - t));
            float s = cella * 0.08f * (1 - t * 0.5f);
            g.setColor(Tema.conAlpha(Color.WHITE, alpha));
            g.fillOval((int) (gx - s), (int) (gy - s), (int) (s * 2), (int) (s * 2));
        }
    }

    // --- COLPO A SEGNO / AFFONDAMENTO: flash + fuoco + scintille + fumo --------
    private void esplosione(Graphics2D g, float cx, float cy, int cella, float t, float scala) {
        float max = cella * 0.7f * scala;

        // 1) lampo iniziale, brevissimo e accecante
        if (t < 0.25f) {
            float tf = t / 0.25f;
            int alpha = (int) (230 * (1 - tf));
            float raggio = max * (0.4f + tf * 0.6f);
            Tema.alone(g, (int) cx, (int) cy, (int) raggio, Tema.conAlpha(Tema.GIALLO_FUOCO, alpha));
        }

        // 2) palla di fuoco che si gonfia e poi si spegne diventando rossa
        float tf = (float) Math.sin(Math.min(1, t * 1.2f) * Math.PI / 2); // sale veloce
        float raggio = max * (0.3f + 0.7f * tf);
        Color cuore = Tema.mescola(Tema.GIALLO_FUOCO, Tema.ARANCIO, t);
        Color bordo = Tema.mescola(Tema.ARANCIO, Tema.ROSSO_NO, t);
        int alphaPalla = (int) (235 * (1 - t));
        Tema.alone(g, (int) cx, (int) cy, (int) raggio, Tema.conAlpha(bordo, alphaPalla));
        g.setColor(Tema.conAlpha(cuore, (int) (255 * (1 - t * 0.8f))));
        g.fillOval((int) (cx - raggio * 0.55f), (int) (cy - raggio * 0.55f),
                (int) (raggio * 1.1f), (int) (raggio * 1.1f));

        // 3) scintille che volano via in tutte le direzioni
        for (int i = 0; i < numParticelle; i++) {
            float dist = (float) (velocita[i] * max * 1.4f * t);
            float sx = cx + (float) Math.cos(angolo[i]) * dist;
            float sy = cy + (float) Math.sin(angolo[i]) * dist;
            int alpha = (int) (255 * (1 - t));
            float s = Math.max(1f, cella * 0.06f * (1 - t));
            g.setColor(Tema.conAlpha(Tema.mescola(Tema.GIALLO_FUOCO, Tema.ARANCIO, (float) velocita[i] - 0.5f), alpha));
            g.fillOval((int) (sx - s), (int) (sy - s), (int) (s * 2), (int) (s * 2));
        }

        // 4) sbuffi di fumo che salgono e sbiadiscono (più marcati se affonda)
        if (t > 0.3f) {
            float ts = (t - 0.3f) / 0.7f;
            for (int i = 0; i < numParticelle / 2; i++) {
                float dx = (float) Math.cos(angolo[i]) * cella * 0.3f * ts;
                float fy = cy - cella * 0.8f * ts * scala;
                float fx = cx + dx;
                float s = cella * (0.2f + 0.3f * ts) * scala;
                int alpha = (int) (110 * (1 - ts));
                g.setColor(Tema.conAlpha(Tema.FUMO, alpha));
                g.fillOval((int) (fx - s), (int) (fy - s), (int) (s * 2), (int) (s * 2));
            }
        }
    }
}
