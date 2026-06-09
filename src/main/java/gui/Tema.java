package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Il "vestito" del gioco: tutti i colori, i font e qualche aiutante per disegnare
 * (sfondi, pannelli vetro, testo con bagliore). Tenere lo stile in un solo posto
 * fa sì che cambiando qui cambi l'aspetto di tutto il gioco, in modo coerente.
 */
public final class Tema {

    private Tema() {}

    // --- PALETTE: tema "plancia di comando" notturna --------------------------
    public static final Color SFONDO_ALTO   = new Color(8, 18, 36);    // blu notte profondo
    public static final Color SFONDO_BASSO  = new Color(2, 6, 16);     // quasi nero
    public static final Color MARE_CHIARO   = new Color(22, 64, 110);  // acqua illuminata
    public static final Color MARE_SCURO    = new Color(10, 32, 62);   // acqua in ombra
    public static final Color GRIGLIA_LINEE = new Color(90, 200, 230, 70);

    public static final Color CIANO       = new Color(64, 224, 255);   // accento principale
    public static final Color CIANO_SOFT  = new Color(120, 235, 255);
    public static final Color VERDE_OK    = new Color(80, 240, 160);   // piazzamento valido
    public static final Color ROSSO_NO    = new Color(255, 90, 90);    // piazzamento non valido
    public static final Color ARANCIO     = new Color(255, 150, 40);   // fuoco / colpito
    public static final Color GIALLO_FUOCO= new Color(255, 220, 90);
    public static final Color ORO         = new Color(255, 210, 90);   // vittoria
    public static final Color FUMO        = new Color(70, 80, 95);

    public static final Color NAVE_CORPO  = new Color(120, 134, 150);  // scafo acciaio
    public static final Color NAVE_LUCE   = new Color(186, 200, 214);
    public static final Color NAVE_OMBRA  = new Color(58, 70, 84);

    public static final Color TESTO       = new Color(225, 240, 255);
    public static final Color TESTO_SOFT  = new Color(150, 175, 200);

    // --- FONT -----------------------------------------------------------------
    // "Segoe UI" è quasi sempre presente su Windows; se manca, Swing ripiega da solo.
    public static Font titolo(float size) { return new Font("Segoe UI", Font.BOLD, (int) size); }
    public static Font grande(float size) { return new Font("Segoe UI", Font.BOLD, (int) size); }
    public static Font normale(float size){ return new Font("Segoe UI", Font.PLAIN, (int) size); }
    public static Font mono(float size)   { return new Font("Consolas", Font.BOLD, (int) size); }

    // --- AIUTANTI DI DISEGNO --------------------------------------------------

    /** Attiva l'antialiasing: il disegno diventa morbido invece che "a scaletta". */
    public static void qualita(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** Sfondo verticale dal blu notte al nero, usato da tutte le schermate. */
    public static void sfondo(Graphics2D g, int w, int h) {
        g.setPaint(new GradientPaint(0, 0, SFONDO_ALTO, 0, h, SFONDO_BASSO));
        g.fillRect(0, 0, w, h);
    }

    /** Un alone di luce radiale (per dare profondità dietro ai titoli o alle griglie). */
    public static void alone(Graphics2D g, int cx, int cy, int raggio, Color colore) {
        RadialGradientPaint p = new RadialGradientPaint(
                new Point2D.Float(cx, cy), raggio,
                new float[]{0f, 1f},
                new Color[]{colore, new Color(colore.getRed(), colore.getGreen(), colore.getBlue(), 0)});
        g.setPaint(p);
        g.fillRect(cx - raggio, cy - raggio, raggio * 2, raggio * 2);
    }

    /** Pannello "vetro": rettangolo arrotondato semitrasparente con bordo luminoso. */
    public static void pannelloVetro(Graphics2D g, int x, int y, int w, int h, Color bordo) {
        RoundRectangle2D r = new RoundRectangle2D.Float(x, y, w, h, 26, 26);
        g.setColor(new Color(255, 255, 255, 14));
        g.fill(r);
        g.setColor(new Color(bordo.getRed(), bordo.getGreen(), bordo.getBlue(), 120));
        g.setStroke(new java.awt.BasicStroke(1.5f));
        g.draw(r);
    }

    /** Testo centrato con un morbido bagliore dietro, in stile insegna al neon. */
    public static void testoLuminoso(Graphics2D g, String testo, int cx, int cy,
                                     Font font, Color colore) {
        g.setFont(font);
        int larg = g.getFontMetrics().stringWidth(testo);
        int x = cx - larg / 2;
        // bagliore: lo stesso testo disegnato più volte, semitrasparente
        g.setColor(new Color(colore.getRed(), colore.getGreen(), colore.getBlue(), 60));
        for (int d = 3; d >= 1; d--) {
            g.drawString(testo, x - d, cy);
            g.drawString(testo, x + d, cy);
            g.drawString(testo, x, cy - d);
            g.drawString(testo, x, cy + d);
        }
        g.setColor(colore);
        g.drawString(testo, x, cy);
    }

    /** Mescola due colori (t da 0 a 1): utile per le animazioni che "sfumano". */
    public static Color mescola(Color a, Color b, float t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
                (int) (a.getRed()   + (b.getRed()   - a.getRed())   * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue()  + (b.getBlue()  - a.getBlue())  * t),
                (int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t));
    }

    /** Lo stesso colore ma con trasparenza (alpha 0..255). */
    public static Color conAlpha(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, alpha)));
    }
}
