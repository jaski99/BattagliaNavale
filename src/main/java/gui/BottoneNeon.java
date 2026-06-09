package gui;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Un bottone "al neon": rettangolo arrotondato scuro con bordo luminoso del
 * colore scelto, che si illumina dolcemente quando il mouse ci passa sopra.
 * Disegnato a mano per avere lo stesso stile di tutto il resto del gioco.
 */
public class BottoneNeon extends JButton {

    private final Color accento;
    private float luce = 0f;            // 0 = spento, 1 = acceso (animato)
    private boolean sopra = false;
    private final Timer animatore;

    public BottoneNeon(String testo, Color accento) {
        super(testo);
        this.accento = accento;
        setForeground(Tema.TESTO);
        setFont(Tema.grande(20));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(300, 64));

        animatore = new Timer(16, e -> {
            float obiettivo = sopra ? 1f : 0f;
            luce += (obiettivo - luce) * 0.2f;
            if (Math.abs(obiettivo - luce) < 0.01f) luce = obiettivo;
            repaint();
        });
        animatore.start();

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { sopra = true; }
            @Override public void mouseExited(MouseEvent e) { sopra = false; }
        });
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema.qualita(g);
        int w = getWidth(), h = getHeight();
        boolean premuto = getModel().isPressed();

        RoundRectangle2D corpo = new RoundRectangle2D.Float(2, 2, w - 4, h - 4, h - 4, h - 4);

        // alone esterno quando è illuminato
        if (luce > 0.02f) {
            Tema.alone(g, w / 2, h / 2, (int) (w * 0.6f), Tema.conAlpha(accento, (int) (90 * luce)));
        }

        // riempimento scuro che si schiarisce un po' verso l'accento al passaggio
        Color alto = Tema.mescola(new Color(18, 30, 50), accento.darker(), 0.15f + 0.25f * luce);
        Color basso = Tema.mescola(new Color(8, 16, 30), accento.darker().darker(), 0.1f + 0.2f * luce);
        if (premuto) { alto = alto.darker(); basso = basso.darker(); }
        g.setPaint(new GradientPaint(0, 0, alto, 0, h, basso));
        g.fill(corpo);

        // bordo luminoso
        g.setStroke(new BasicStroke(2f));
        g.setColor(Tema.mescola(Tema.conAlpha(accento, 150), accento, luce));
        g.draw(corpo);

        // testo centrato
        g.setFont(getFont());
        String txt = getText();
        int tw = g.getFontMetrics().stringWidth(txt);
        int tx = (w - tw) / 2;
        int ty = (h + g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent()) / 2;
        g.setColor(Tema.conAlpha(accento, (int) (120 * luce)));
        g.drawString(txt, tx, ty + 1);
        g.setColor(Tema.mescola(Tema.TESTO, Color.WHITE, luce * 0.5f));
        g.drawString(txt, tx, ty);
        g.dispose();
    }

    /** Permette di staccare il timer quando il bottone non serve più. */
    public void ferma() { animatore.stop(); }

    // evita che Swing disegni lo sfondo standard
    @Override public void updateUI() { super.updateUI(); setContentAreaFilled(false); setBorderPainted(false); }
    @Override public boolean isContentAreaFilled() { return false; }
    @SuppressWarnings("unused") private AbstractButton self() { return this; }
}
