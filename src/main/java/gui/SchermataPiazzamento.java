package gui;

import model.Griglia;
import model.Posizione;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * La fase in cui un giocatore schiera la propria flotta.
 *
 * Sotto il mouse compare l'anteprima della nave da piazzare: verde se la
 * posizione è valida, rossa se no. Si clicca per posizionare, si ruota con il
 * tasto R (o col pulsante), si può lasciar fare al caso o ricominciare da capo.
 */
public class SchermataPiazzamento extends JPanel {

    private static final Random RND = new Random();

    private final Griglia griglia;
    private final String nome;
    private final Runnable onFatto;
    private final int[] flotta = gioco.GestorePartita.FLOTTA;

    private final PannelloGriglia pannello;
    private final BottoneNeon btnRuota, btnCasuale, btnRicomincia, btnPronto;

    private int indice = 0;          // quale nave della flotta stiamo piazzando
    private boolean orizzontale = true;

    public SchermataPiazzamento(FinestraGioco finestra, Griglia griglia, String nome, Runnable onFatto) {
        this.griglia = griglia;
        this.nome = nome;
        this.onFatto = onFatto;
        setLayout(new BorderLayout());
        setBackground(Tema.SFONDO_BASSO);

        // --- griglia interattiva al centro ---
        pannello = new PannelloGriglia(griglia, true);
        pannello.setInterattiva(true);
        pannello.setAscoltatoreHover((r, c) -> aggiornaAnteprima(r, c));
        pannello.setAscoltatore(this::provaPiazza);
        add(new Cornice(pannello), BorderLayout.CENTER);

        // --- intestazione ---
        add(new Intestazione(), BorderLayout.NORTH);

        // --- pannello informazioni a destra ---
        add(new Laterale(), BorderLayout.EAST);

        // --- barra dei pulsanti in basso ---
        btnRuota = piccolo("⟳  RUOTA (R)", Tema.CIANO);
        btnCasuale = piccolo("🎲  CASUALE", Tema.CIANO);
        btnRicomincia = piccolo("↺  RICOMINCIA", Tema.ARANCIO);
        btnPronto = piccolo("✔  PRONTO", Tema.VERDE_OK);
        btnRuota.addActionListener(e -> ruota());
        btnCasuale.addActionListener(e -> casuale());
        btnRicomincia.addActionListener(e -> ricomincia());
        btnPronto.addActionListener(e -> { if (tuttePiazzate()) onFatto.run(); });
        btnPronto.setEnabled(false);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 14));
        barra.setOpaque(false);
        barra.add(btnRuota); barra.add(btnCasuale); barra.add(btnRicomincia); barra.add(btnPronto);
        add(barra, BorderLayout.SOUTH);

        // --- scorciatoie da tastiera ---
        scorciatoia("R", this::ruota);
        scorciatoia("SPACE", this::ruota);
        scorciatoia("ENTER", () -> { if (tuttePiazzate()) onFatto.run(); });
    }

    private BottoneNeon piccolo(String testo, Color colore) {
        BottoneNeon b = new BottoneNeon(testo, colore);
        b.setFont(Tema.grande(16));
        b.setPreferredSize(new Dimension(190, 52));
        return b;
    }

    private void scorciatoia(String tasto, Runnable azione) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tasto), tasto);
        getActionMap().put(tasto, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { azione.run(); }
        });
    }

    // --- logica del piazzamento ----------------------------------------------

    private boolean tuttePiazzate() { return indice >= flotta.length; }

    /** Calcola le celle che la nave occuperebbe partendo da (r,c). */
    private List<Posizione> celleNave(int r, int c, int lunghezza) {
        List<Posizione> celle = new ArrayList<>();
        for (int i = 0; i < lunghezza; i++) {
            int rr = orizzontale ? r : r + i;
            int cc = orizzontale ? c + i : c;
            if (rr >= 0 && rr < Griglia.DIM && cc >= 0 && cc < Griglia.DIM) {
                celle.add(new Posizione(rr, cc));
            }
        }
        return celle;
    }

    private void aggiornaAnteprima(int r, int c) {
        if (tuttePiazzate() || r < 0 || c < 0) { pannello.pulisciAnteprima(); return; }
        int len = flotta[indice];
        boolean valida = griglia.puoiPiazzare(r, c, len, orizzontale);
        pannello.setAnteprima(celleNave(r, c, len), valida);
    }

    private void provaPiazza(int r, int c) {
        if (tuttePiazzate()) return;
        int len = flotta[indice];
        if (griglia.puoiPiazzare(r, c, len, orizzontale)) {
            griglia.piazzaNave(r, c, len, orizzontale);
            indice++;
            if (tuttePiazzate()) {
                btnPronto.setEnabled(true);
                pannello.pulisciAnteprima();
            } else {
                aggiornaAnteprima(pannello.getHoverR(), pannello.getHoverC());
            }
            ridisegnaTutto();
        }
    }

    private void ruota() {
        orizzontale = !orizzontale;
        aggiornaAnteprima(pannello.getHoverR(), pannello.getHoverC());
    }

    private void casuale() {
        griglia.svuota();
        for (int len : flotta) {
            boolean ok = false;
            while (!ok) {
                int r = RND.nextInt(Griglia.DIM), c = RND.nextInt(Griglia.DIM);
                boolean o = RND.nextBoolean();
                if (griglia.puoiPiazzare(r, c, len, o)) { griglia.piazzaNave(r, c, len, o); ok = true; }
            }
        }
        indice = flotta.length;
        btnPronto.setEnabled(true);
        pannello.pulisciAnteprima();
        ridisegnaTutto();
    }

    private void ricomincia() {
        griglia.svuota();
        indice = 0;
        orizzontale = true;
        btnPronto.setEnabled(false);
        aggiornaAnteprima(pannello.getHoverR(), pannello.getHoverC());
        ridisegnaTutto();
    }

    private void ridisegnaTutto() { repaint(); }

    // --- nome "di mestiere" di una nave in base alla lunghezza ----------------
    static String nomeNave(int lunghezza) {
        return switch (lunghezza) {
            case 5 -> "Portaerei";
            case 4 -> "Corazzata";
            case 3 -> "Incrociatore";
            case 2 -> "Cacciatorpediniere";
            default -> "Nave (" + lunghezza + ")";
        };
    }

    // ==========================================================================
    //  COMPONENTI INTERNI (intestazione, cornice della griglia, pannello info)
    // ==========================================================================

    /** Barra in alto: titolo della fase + istruzione corrente. */
    private class Intestazione extends JPanel {
        Intestazione() { setOpaque(false); setPreferredSize(new Dimension(10, 86)); }
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            Tema.qualita(g);
            int w = getWidth();
            Tema.testoLuminoso(g, "SCHIERA LA TUA FLOTTA — " + nome.toUpperCase(),
                    w / 2, 40, Tema.titolo(30), Tema.CIANO_SOFT);
            g.setColor(Tema.TESTO_SOFT);
            g.setFont(Tema.normale(17));
            String hint = tuttePiazzate()
                    ? "Flotta completa! Premi PRONTO per continuare."
                    : "Clicca per posizionare la " + nomeNave(flotta[indice]).toUpperCase()
                      + " (lunga " + flotta[indice] + ")  •  R per ruotarla";
            int hw = g.getFontMetrics().stringWidth(hint);
            g.drawString(hint, w / 2 - hw / 2, 66);
            g.dispose();
        }
    }

    /** Riquadro che centra la griglia lasciando un po' di respiro. */
    private static class Cornice extends JPanel {
        Cornice(JComponent dentro) {
            setOpaque(false);
            setLayout(new java.awt.GridBagLayout());
            add(dentro);
        }
    }

    /** Colonna di destra: checklist della flotta e indicatore di orientamento. */
    private class Laterale extends JPanel {
        Laterale() { setOpaque(false); setPreferredSize(new Dimension(300, 10)); }
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            Tema.qualita(g);
            int w = getWidth(), h = getHeight();
            int m = 16;
            Tema.pannelloVetro(g, m, m, w - m * 2, h - m * 2 - 10, Tema.CIANO);

            int x = m + 22;
            int y = m + 48;
            g.setColor(Tema.TESTO);
            g.setFont(Tema.grande(20));
            g.drawString("LA TUA FLOTTA", x, y);
            y += 16;
            g.setColor(Tema.conAlpha(Tema.CIANO, 120));
            g.drawLine(x, y, w - m - 22, y);
            y += 34;

            g.setFont(Tema.normale(17));
            for (int i = 0; i < flotta.length; i++) {
                boolean piazzata = i < indice;
                boolean corrente = (i == indice);
                int len = flotta[i];

                // pallino di stato
                Color stato = piazzata ? Tema.VERDE_OK : (corrente ? Tema.CIANO_SOFT : Tema.TESTO_SOFT);
                g.setColor(stato);
                g.fillOval(x, y - 12, 12, 12);

                // nave a segmenti
                int sx = x + 24, sy = y - 14, seg = 16;
                for (int s = 0; s < len; s++) {
                    g.setColor(piazzata ? Tema.NAVE_CORPO
                            : Tema.conAlpha(corrente ? Tema.CIANO : Tema.TESTO_SOFT, 90));
                    g.fillRoundRect(sx + s * (seg + 2), sy, seg, 14, 6, 6);
                }

                // nome
                g.setColor(piazzata ? Tema.TESTO : (corrente ? Tema.CIANO_SOFT : Tema.TESTO_SOFT));
                g.setFont(corrente ? Tema.grande(16) : Tema.normale(16));
                g.drawString(nomeNave(len), sx + len * (seg + 2) + 12, y);

                y += 40;
            }

            // indicatore orientamento corrente
            y += 6;
            g.setColor(Tema.TESTO_SOFT);
            g.setFont(Tema.normale(16));
            g.drawString("Orientamento:", x, y);
            g.setColor(Tema.CIANO_SOFT);
            g.setFont(Tema.grande(16));
            g.drawString(orizzontale ? "ORIZZONTALE  ⟷" : "VERTICALE  ↕", x + 130, y);

            g.dispose();
        }
    }
}
