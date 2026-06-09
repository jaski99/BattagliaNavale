package gui;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Sfondo animato comune alle schermate "a tutto schermo" (menu, passaggio, fine):
 * un mare notturno con onde luminose che scorrono e qualche bolla che sale.
 *
 * Le schermate concrete ereditano da qui e si limitano a disegnare il proprio
 * contenuto sopra, dentro {@link #disegnaScena(Graphics2D, int, int, float)}.
 */
public abstract class PannelloScena extends JPanel {

    private final long t0 = System.nanoTime();
    protected float clock = 0f;
    private final Timer timer;

    // bolle che salgono lentamente: posizioni e fasi fissate all'inizio
    private final int numBolle = 36;
    private final float[] bollaX = new float[numBolle];
    private final float[] bollaFase = new float[numBolle];
    private final float[] bollaVel = new float[numBolle];
    private final float[] bollaR = new float[numBolle];

    protected PannelloScena() {
        setOpaque(true);
        setLayout(null); // posizioniamo i componenti a mano, per centrarli con precisione
        Random rnd = new Random(7);
        for (int i = 0; i < numBolle; i++) {
            bollaX[i] = rnd.nextFloat();
            bollaFase[i] = rnd.nextFloat() * 10;
            bollaVel[i] = 0.04f + rnd.nextFloat() * 0.10f;
            bollaR[i] = 1.5f + rnd.nextFloat() * 4f;
        }
        timer = new Timer(16, e -> {
            clock = (System.nanoTime() - t0) / 1_000_000_000f;
            posiziona(getWidth(), getHeight()); // ricolloca i componenti se la finestra cambia
            repaint();
        });
    }

    @Override public void addNotify() { super.addNotify(); timer.start(); posiziona(getWidth(), getHeight()); }
    @Override public void removeNotify() { timer.stop(); super.removeNotify(); }

    /** Le sottoclassi collocano qui i propri bottoni/etichette in base alla dimensione. */
    protected void posiziona(int w, int h) { }

    /** Le sottoclassi disegnano qui il contenuto della scena, sopra lo sfondo. */
    protected abstract void disegnaScena(Graphics2D g, int w, int h, float clock);

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        Tema.qualita(g);
        int w = getWidth(), h = getHeight();

        Tema.sfondo(g, w, h);

        // bande d'onda che scorrono orizzontalmente nella metà bassa
        for (int b = 0; b < 5; b++) {
            float y = h * (0.55f + b * 0.09f);
            float amp = 10 + b * 4;
            g.setColor(Tema.conAlpha(Tema.MARE_CHIARO, 26 - b * 3));
            int passo = 6;
            int prevX = 0, prevY = (int) y;
            for (int x = 0; x <= w; x += passo) {
                int yy = (int) (y + Math.sin(x * 0.012 + clock * 1.3 + b) * amp);
                g.fillRect(prevX, prevY, passo + 1, h - prevY);
                prevX = x; prevY = yy;
            }
        }

        // alone centrale soffuso
        Tema.alone(g, w / 2, (int) (h * 0.42f), (int) (h * 0.6f), Tema.conAlpha(Tema.MARE_CHIARO, 50));

        // bolle che salgono
        for (int i = 0; i < numBolle; i++) {
            float prog = (clock * bollaVel[i] + bollaFase[i]) % 1f;
            float by = h - prog * h;
            float bx = bollaX[i] * w + (float) Math.sin(prog * 6 + bollaFase[i]) * 14;
            int alpha = (int) (60 * (1 - prog));
            g.setColor(new Color(180, 230, 255, alpha));
            float r = bollaR[i];
            g.fillOval((int) (bx - r), (int) (by - r), (int) (r * 2), (int) (r * 2));
        }

        disegnaScena(g, w, h, clock);
        g.dispose();
    }
}
