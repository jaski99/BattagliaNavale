package gui;

import model.Griglia;
import model.Nave;
import model.Posizione;
import model.StatoCella;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Il componente che DISEGNA una griglia 10x10 e ci fa interagire col mouse.
 *
 * È volutamente "muto" sulle regole: non sa cosa significhi colpire o vincere.
 * Sa solo leggere lo stato della {@link Griglia} e dipingerlo in modo bello —
 * acqua animata, navi in rilievo, esplosioni — e avvisare chi ascolta quando si
 * clicca una cella. Le decisioni di gioco restano fuori, nelle schermate.
 */
public class PannelloGriglia extends JPanel {

    /** Chi vuole sapere su quale cella si è cliccato implementa questa interfaccia. */
    public interface AscoltatoreCella {
        void cellaCliccata(int riga, int colonna);
    }

    private final Griglia griglia;
    private boolean mostraNavi;          // true = griglia propria; false = griglia nemica (navi nascoste)
    private boolean interattiva = false; // accetta i click?
    private AscoltatoreCella ascoltatore;
    private AscoltatoreCella ascoltatoreHover; // avvisato quando il mouse cambia cella

    private final List<Effetto> effetti = new ArrayList<>();

    // anteprima di piazzamento (celle evidenziate sotto il mouse)
    private List<Posizione> anteprima = List.of();
    private boolean anteprimaValida = true;

    private int hoverR = -1, hoverC = -1;

    // geometria calcolata a ogni disegno, riusata dal mouse per capire la cella
    private int x0, y0, cella;

    // orologio condiviso da tutte le animazioni (in secondi)
    private final long t0 = System.nanoTime();
    private float clock = 0f;
    private final Timer timer;

    public PannelloGriglia(Griglia griglia, boolean mostraNavi) {
        this.griglia = griglia;
        this.mostraNavi = mostraNavi;
        setOpaque(false);
        setPreferredSize(new Dimension(440, 440));

        // ~60 fotogrammi al secondo: aggiorna l'orologio, scarta gli effetti finiti, ridisegna
        timer = new Timer(16, e -> {
            clock = (System.nanoTime() - t0) / 1_000_000_000f;
            effetti.removeIf(ef -> ef.finito(clock));
            repaint();
        });

        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) { aggiornaHover(e); }
            @Override public void mouseDragged(MouseEvent e) { aggiornaHover(e); }
            @Override public void mouseExited(MouseEvent e) {
                hoverR = hoverC = -1;
                if (ascoltatoreHover != null) ascoltatoreHover.cellaCliccata(-1, -1);
                repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (!interattiva || ascoltatore == null) return;
                int[] rc = cellaDaPixel(e.getX(), e.getY());
                if (rc != null) ascoltatore.cellaCliccata(rc[0], rc[1]);
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    // l'animazione parte/ferma da sola quando il pannello compare/scompare
    @Override public void addNotify() { super.addNotify(); timer.start(); }
    @Override public void removeNotify() { timer.stop(); super.removeNotify(); }

    // --- API per le schermate -------------------------------------------------

    public void setInterattiva(boolean v) { this.interattiva = v; if (!v) { hoverR = hoverC = -1; } repaint(); }
    public void setAscoltatore(AscoltatoreCella a) { this.ascoltatore = a; }
    public void setAscoltatoreHover(AscoltatoreCella a) { this.ascoltatoreHover = a; }
    public void setMostraNavi(boolean v) { this.mostraNavi = v; repaint(); }

    public void setAnteprima(List<Posizione> celle, boolean valida) {
        this.anteprima = celle; this.anteprimaValida = valida; repaint();
    }
    public void pulisciAnteprima() { this.anteprima = List.of(); repaint(); }

    public void spruzzo(int r, int c)      { effetti.add(new Effetto(Effetto.Tipo.SPRUZZO, r, c, clock)); }
    public void esplosione(int r, int c)   { effetti.add(new Effetto(Effetto.Tipo.ESPLOSIONE, r, c, clock)); }
    public void affondamento(List<Posizione> celle) {
        for (Posizione p : celle) effetti.add(new Effetto(Effetto.Tipo.AFFONDAMENTO, p.riga(), p.colonna(), clock));
    }

    /** Vero finché c'è almeno un effetto in corso: le schermate aspettano prima di proseguire. */
    public boolean animazioniInCorso() { return !effetti.isEmpty(); }

    // --- mouse -> cella -------------------------------------------------------

    private void aggiornaHover(MouseEvent e) {
        if (!interattiva) return;
        int[] rc = cellaDaPixel(e.getX(), e.getY());
        int nr = rc == null ? -1 : rc[0];
        int nc = rc == null ? -1 : rc[1];
        if (nr != hoverR || nc != hoverC) {
            hoverR = nr; hoverC = nc;
            if (ascoltatoreHover != null) ascoltatoreHover.cellaCliccata(nr, nc);
            repaint();
        }
    }

    private int[] cellaDaPixel(int px, int py) {
        if (cella <= 0) return null;
        int c = (px - x0) / cella;
        int r = (py - y0) / cella;
        if (r < 0 || r >= Griglia.DIM || c < 0 || c >= Griglia.DIM) return null;
        if (px < x0 || py < y0) return null;
        return new int[]{r, c};
    }

    public int getHoverR() { return hoverR; }
    public int getHoverC() { return hoverC; }

    // --- DISEGNO --------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        Tema.qualita(g);
        calcolaGeometria();

        int gw = cella * Griglia.DIM;

        // alone soffuso dietro la griglia, per darle profondità
        Tema.alone(g, x0 + gw / 2, y0 + gw / 2, gw, Tema.conAlpha(Tema.MARE_CHIARO, 70));

        disegnaAcqua(g, gw);
        disegnaLineeEtichette(g, gw);
        disegnaNavi(g);
        disegnaSegni(g);          // colpiti e mancati
        disegnaAnteprima(g);
        disegnaMirino(g);
        for (Effetto ef : new ArrayList<>(effetti)) {
            ef.disegna(g, x0 + ef.colonna * cella + cella / 2f,
                          y0 + ef.riga * cella + cella / 2f, cella, clock);
        }
        g.dispose();
    }

    private void calcolaGeometria() {
        int w = getWidth(), h = getHeight();
        int etich = Math.max(16, Math.min(w, h) / 20); // banda per le etichette A-J / 1-10
        cella = Math.max(10, Math.min((w - etich) / Griglia.DIM, (h - etich) / Griglia.DIM));
        int gw = cella * Griglia.DIM;
        x0 = etich + (w - etich - gw) / 2;
        y0 = etich + (h - etich - gw) / 2;
    }

    private void disegnaAcqua(Graphics2D g, int gw) {
        // bordo arrotondato attorno al mare
        RoundRectangle2D cornice = new RoundRectangle2D.Float(x0 - 6, y0 - 6, gw + 12, gw + 12, 18, 18);
        g.setColor(Tema.conAlpha(Tema.MARE_SCURO, 230));
        g.fill(cornice);

        for (int r = 0; r < Griglia.DIM; r++) {
            for (int c = 0; c < Griglia.DIM; c++) {
                int x = x0 + c * cella, y = y0 + r * cella;
                // onda: luminosità che ondeggia nel tempo, sfasata per cella
                double onda = Math.sin(clock * 1.6 + (r + c) * 0.55) * 0.5 + 0.5;
                Color acqua = Tema.mescola(Tema.MARE_SCURO, Tema.MARE_CHIARO, (float) (0.25 + onda * 0.5));
                g.setColor(acqua);
                g.fillRect(x, y, cella, cella);
                // riflesso che scorre in diagonale
                float rifl = (float) (Math.sin(clock * 1.2 + r * 0.4 - c * 0.3) * 0.5 + 0.5);
                g.setColor(Tema.conAlpha(Tema.CIANO_SOFT, (int) (18 * rifl)));
                g.fillRect(x, y, cella, cella / 3);
            }
        }
    }

    private void disegnaLineeEtichette(Graphics2D g, int gw) {
        g.setStroke(new BasicStroke(1f));
        g.setColor(Tema.GRIGLIA_LINEE);
        for (int i = 0; i <= Griglia.DIM; i++) {
            g.drawLine(x0 + i * cella, y0, x0 + i * cella, y0 + gw);
            g.drawLine(x0, y0 + i * cella, x0 + gw, y0 + i * cella);
        }
        g.setFont(Tema.mono(Math.max(10, cella * 0.42f)));
        g.setColor(Tema.TESTO_SOFT);
        for (int i = 0; i < Griglia.DIM; i++) {
            String lettera = String.valueOf((char) ('A' + i));
            int lw = g.getFontMetrics().stringWidth(lettera);
            g.drawString(lettera, x0 - lw - 8, y0 + i * cella + cella / 2 + g.getFontMetrics().getAscent() / 2 - 2);
            String num = String.valueOf(i + 1);
            int nw = g.getFontMetrics().stringWidth(num);
            g.drawString(num, x0 + i * cella + cella / 2 - nw / 2, y0 - 8);
        }
    }

    private void disegnaNavi(Graphics2D g) {
        for (Nave nave : griglia.getFlotta()) {
            boolean affondata = nave.affondata();
            // sulla griglia nemica si vedono solo le navi affondate; sulla propria, tutte
            if (!mostraNavi && !affondata) continue;
            disegnaScafo(g, nave, affondata);
        }
    }

    private void disegnaScafo(Graphics2D g, Nave nave, boolean affondata) {
        List<Posizione> pos = nave.getPosizioni();
        if (pos.isEmpty()) return;
        int minR = Griglia.DIM, minC = Griglia.DIM, maxR = -1, maxC = -1;
        for (Posizione p : pos) {
            minR = Math.min(minR, p.riga()); maxR = Math.max(maxR, p.riga());
            minC = Math.min(minC, p.colonna()); maxC = Math.max(maxC, p.colonna());
        }
        int inset = Math.max(2, cella / 6);
        int x = x0 + minC * cella + inset;
        int y = y0 + minR * cella + inset;
        int w = (maxC - minC + 1) * cella - inset * 2;
        int h = (maxR - minR + 1) * cella - inset * 2;
        boolean orizzontale = (maxR == minR);
        int arc = Math.min(w, h);

        RoundRectangle2D scafo = new RoundRectangle2D.Float(x, y, w, h, arc, arc);

        // corpo con sfumatura (metallo) oppure relitto bruciato se affondata
        if (affondata) {
            g.setPaint(new GradientPaint(x, y, new Color(60, 40, 40), x, y + h, new Color(28, 18, 18)));
        } else {
            g.setPaint(new GradientPaint(x, y, Tema.NAVE_LUCE, x, y + h, Tema.NAVE_OMBRA));
        }
        g.fill(scafo);

        // bordo
        g.setStroke(new BasicStroke(Math.max(1.5f, cella * 0.06f)));
        g.setColor(affondata ? new Color(120, 40, 40) : Tema.NAVE_OMBRA.darker());
        g.draw(scafo);

        // linea di ponte centrale + torrette, lungo l'asse della nave
        if (!affondata) {
            g.setColor(Tema.conAlpha(Tema.NAVE_OMBRA, 160));
            if (orizzontale) g.fillRect(x, y + h / 2 - Math.max(1, h / 12), w, Math.max(2, h / 6));
            else g.fillRect(x + w / 2 - Math.max(1, w / 12), y, Math.max(2, w / 6), h);

            int n = nave.getLunghezza();
            for (int i = 0; i < n; i++) {
                float cx = orizzontale ? x + cella * (i + 0.5f) - inset : x + w / 2f;
                float cy = orizzontale ? y + h / 2f : y + cella * (i + 0.5f) - inset;
                float rr = cella * 0.12f;
                g.setColor(Tema.NAVE_CORPO.brighter());
                g.fillOval((int) (cx - rr), (int) (cy - rr), (int) (rr * 2), (int) (rr * 2));
                g.setColor(Tema.NAVE_OMBRA);
                g.drawOval((int) (cx - rr), (int) (cy - rr), (int) (rr * 2), (int) (rr * 2));
            }
        }
    }

    private void disegnaSegni(Graphics2D g) {
        for (int r = 0; r < Griglia.DIM; r++) {
            for (int c = 0; c < Griglia.DIM; c++) {
                StatoCella s = griglia.getStato(r, c);
                int cx = x0 + c * cella + cella / 2;
                int cy = y0 + r * cella + cella / 2;
                if (s == StatoCella.COLPITO) {
                    // brace pulsante + crepa scura
                    float puls = (float) (Math.sin(clock * 4 + (r + c)) * 0.5 + 0.5);
                    Tema.alone(g, cx, cy, (int) (cella * 0.5f), Tema.conAlpha(Tema.ARANCIO, (int) (90 + 90 * puls)));
                    g.setColor(Tema.conAlpha(Tema.GIALLO_FUOCO, 230));
                    int rr = (int) (cella * 0.16f);
                    g.fillOval(cx - rr, cy - rr, rr * 2, rr * 2);
                } else if (s == StatoCella.ACQUA) {
                    // colpo a vuoto: pallino tenue con anellino
                    g.setColor(Tema.conAlpha(Color.WHITE, 60));
                    int rr = (int) (cella * 0.12f);
                    g.fillOval(cx - rr, cy - rr, rr * 2, rr * 2);
                    g.setColor(Tema.conAlpha(Tema.CIANO_SOFT, 90));
                    g.setStroke(new BasicStroke(1.5f));
                    int ro = (int) (cella * 0.22f);
                    g.drawOval(cx - ro, cy - ro, ro * 2, ro * 2);
                }
            }
        }
    }

    private void disegnaAnteprima(Graphics2D g) {
        if (anteprima.isEmpty()) return;
        Color base = anteprimaValida ? Tema.VERDE_OK : Tema.ROSSO_NO;
        float puls = (float) (Math.sin(clock * 5) * 0.5 + 0.5);
        for (Posizione p : anteprima) {
            int x = x0 + p.colonna() * cella, y = y0 + p.riga() * cella;
            g.setColor(Tema.conAlpha(base, (int) (70 + 60 * puls)));
            g.fillRoundRect(x + 2, y + 2, cella - 4, cella - 4, cella / 3, cella / 3);
            g.setColor(Tema.conAlpha(base, 220));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(x + 2, y + 2, cella - 4, cella - 4, cella / 3, cella / 3);
        }
    }

    private void disegnaMirino(Graphics2D g) {
        if (!interattiva || hoverR < 0 || hoverC < 0) return;
        // non mostrare il mirino dove abbiamo già sparato
        StatoCella s = griglia.getStato(hoverR, hoverC);
        boolean giaSparato = (s == StatoCella.COLPITO || s == StatoCella.ACQUA);
        Color col = giaSparato ? Tema.ROSSO_NO : Tema.CIANO;
        int x = x0 + hoverC * cella, y = y0 + hoverR * cella;

        g.setColor(Tema.conAlpha(col, 40));
        g.fillRect(x, y, cella, cella);

        g.setStroke(new BasicStroke(2.2f));
        g.setColor(col);
        // parentesi angolari agli angoli della cella
        int t = cella / 4;
        g.drawLine(x, y, x + t, y);             g.drawLine(x, y, x, y + t);
        g.drawLine(x + cella, y, x + cella - t, y);     g.drawLine(x + cella, y, x + cella, y + t);
        g.drawLine(x, y + cella, x + t, y + cella);     g.drawLine(x, y + cella, x, y + cella - t);
        g.drawLine(x + cella, y + cella, x + cella - t, y + cella);
        g.drawLine(x + cella, y + cella, x + cella, y + cella - t);

        // crocino rotante al centro
        int cx = x + cella / 2, cy = y + cella / 2;
        double a = clock * 2;
        int rr = (int) (cella * 0.18f);
        g.drawLine(cx + (int) (Math.cos(a) * rr), cy + (int) (Math.sin(a) * rr),
                   cx - (int) (Math.cos(a) * rr), cy - (int) (Math.sin(a) * rr));
        g.drawLine(cx + (int) (Math.cos(a + Math.PI / 2) * rr), cy + (int) (Math.sin(a + Math.PI / 2) * rr),
                   cx - (int) (Math.cos(a + Math.PI / 2) * rr), cy - (int) (Math.sin(a + Math.PI / 2) * rr));
    }
}
