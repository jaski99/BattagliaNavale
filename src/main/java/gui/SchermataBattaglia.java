package gui;

import gioco.GestorePartita;
import giocatori.Giocatore;
import model.Griglia;
import model.Nave;
import model.Posizione;
import model.RisultatoSparo;
import model.StatoCella;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Il cuore del gioco: la battaglia vera e propria.
 *
 * A sinistra la flotta di chi attacca, a destra il "radar" del mare nemico, in
 * cui sparare. Sotto a ciascuna griglia una barra mostra lo stato della flotta.
 * Un turno è sempre lo stesso: si spara su una cella, parte l'effetto, si
 * controlla se la flotta nemica è distrutta, poi tocca all'avversario.
 *
 * La classe gestisce due modalità con lo stesso codice di fondo:
 *  - contro il COMPUTER: la vista resta fissa (la tua flotta a sinistra, il
 *    radar a destra) e l'IA risponde da sola dopo una breve pausa "di mira";
 *  - in DUE: a ogni cambio di turno si passa il computer con una schermata-ponte
 *    e le due griglie vengono ricollocate dal punto di vista di chi gioca.
 */
public class SchermataBattaglia extends JPanel {

    private final FinestraGioco finestra;
    private final GestorePartita gestore;
    private final boolean vsIA;

    private Giocatore attaccante;
    private Giocatore difensore;

    // le due colonne attualmente a schermo
    private Giocatore playerSx, playerDx;
    private PannelloGriglia pannelloSx, pannelloDx;
    private PannelloFlotta flottaSx, flottaDx;
    private final JPanel centro = new JPanel(new GridLayout(1, 2, 24, 0));

    private boolean inputBloccato = true;
    private String messaggio = "";
    private Color messaggioColore = Tema.CIANO_SOFT;

    public SchermataBattaglia(FinestraGioco finestra, GestorePartita gestore) {
        this.finestra = finestra;
        this.gestore = gestore;
        this.vsIA = !gestore.dueUmani();
        this.attaccante = gestore.g1();
        this.difensore = gestore.g2();

        setLayout(new BorderLayout());
        setBackground(Tema.SFONDO_BASSO);
        add(new Intestazione(), BorderLayout.NORTH);
        centro.setOpaque(false);
        centro.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 24, 18, 24));
        add(centro, BorderLayout.CENTER);

        costruisciColonne(attaccante, difensore);

        // primo turno: tocca sempre al giocatore 1 (umano)
        abilitaFuocoUmano();
    }

    // --- costruzione della vista ---------------------------------------------

    private void costruisciColonne(Giocatore proprio, Giocatore avversario) {
        playerSx = proprio;
        playerDx = avversario;

        pannelloSx = new PannelloGriglia(proprio.getGriglia(), true);
        pannelloDx = new PannelloGriglia(avversario.getGriglia(), false);
        pannelloDx.setAscoltatore(this::sparaUmano);

        flottaSx = new PannelloFlotta(proprio.getGriglia(), true);
        flottaDx = new PannelloFlotta(avversario.getGriglia(), false);

        String capSx = vsIA ? "LA TUA FLOTTA" : proprio.getNome().toUpperCase() + " · LA TUA FLOTTA";
        String capDx = vsIA ? "MARE NEMICO · RADAR" : "BERSAGLIO · " + avversario.getNome().toUpperCase();

        centro.removeAll();
        centro.add(colonna(capSx, Tema.CIANO, pannelloSx, flottaSx));
        centro.add(colonna(capDx, Tema.ROSSO_NO, pannelloDx, flottaDx));
        centro.revalidate();
        centro.repaint();
    }

    private JPanel colonna(String titolo, Color colore, PannelloGriglia griglia, PannelloFlotta flotta) {
        JPanel col = new JPanel(new BorderLayout());
        col.setOpaque(false);
        col.add(new Didascalia(titolo, colore), BorderLayout.NORTH);
        col.add(griglia, BorderLayout.CENTER);
        col.add(flotta, BorderLayout.SOUTH);
        return col;
    }

    private PannelloGriglia pannelloDi(Giocatore g) {
        return g == playerSx ? pannelloSx : pannelloDx;
    }

    // --- flusso dei turni -----------------------------------------------------

    private void abilitaFuocoUmano() {
        inputBloccato = false;
        pannelloDx.setInterattiva(true);
        pannelloDx.setAscoltatore(this::sparaUmano);
        imposta("FUOCO A VOLONTÀ — scegli un bersaglio nel mare nemico", Tema.CIANO_SOFT);
    }

    private void passaTurno() {
        Giocatore t = attaccante; attaccante = difensore; difensore = t;
        if (vsIA) nuovoTurnoVsIA();
        else nuovoTurno2P();
    }

    private void nuovoTurnoVsIA() {
        if (attaccante.eUmano()) {
            abilitaFuocoUmano();
        } else {
            turnoIA();
        }
    }

    private void nuovoTurno2P() {
        // si passa il computer all'altro giocatore prima di rivelarne la vista
        inputBloccato = true;
        finestra.mostraPassaggio("TURNO DI " + attaccante.getNome().toUpperCase(),
                "Passa il computer e premi quando sei pronto", () -> {
            finestra.mostra(this);
            costruisciColonne(attaccante, difensore);
            abilitaFuocoUmano();
        });
    }

    private void turnoIA() {
        inputBloccato = true;
        pannelloDx.setInterattiva(false);
        imposta(attaccante.getNome() + " prende la mira…", Tema.ARANCIO);
        Timer pausa = new Timer(950, e -> {
            Posizione p = attaccante.scegliMossa(difensore.getGriglia());
            fuoco(p);
        });
        pausa.setRepeats(false);
        pausa.start();
    }

    // --- lo sparo, identico per umano e IA ------------------------------------

    private void sparaUmano(int r, int c) {
        if (inputBloccato) return;
        StatoCella s = difensore.getGriglia().getStato(r, c);
        if (s == StatoCella.COLPITO || s == StatoCella.ACQUA) {
            imposta("Lì hai già sparato — scegli un'altra cella", Tema.ROSSO_NO);
            return;
        }
        inputBloccato = true;
        pannelloDx.setInterattiva(false);
        fuoco(new Posizione(r, c));
    }

    private void fuoco(Posizione p) {
        PannelloGriglia pan = pannelloDi(difensore);
        Griglia grigliaColpita = difensore.getGriglia();
        RisultatoSparo esito = grigliaColpita.spara(p);
        attaccante.ricevutoEsito(p, esito);

        animaEsito(pan, p, esito, grigliaColpita);
        ridisegnaFlotte();

        attendiAnimazioni(pan, () -> {
            if (grigliaColpita.tutteNaviAffondate()) {
                fine(attaccante);
            } else {
                passaTurno();
            }
        });
    }

    private void animaEsito(PannelloGriglia pan, Posizione p, RisultatoSparo esito, Griglia g) {
        String chi = attaccante.eUmano() && vsIA ? "" : attaccante.getNome() + ": ";
        switch (esito) {
            case ACQUA -> { pan.spruzzo(p.riga(), p.colonna()); imposta(chi + "ACQUA in " + p, Tema.CIANO_SOFT); }
            case COLPITO -> { pan.esplosione(p.riga(), p.colonna()); imposta(chi + "COLPITO in " + p + "!", Tema.ARANCIO); }
            case AFFONDATO -> {
                pan.esplosione(p.riga(), p.colonna());
                Nave nave = g.naveIn(p);
                if (nave != null) pan.affondamento(nave.getPosizioni());
                imposta(chi + "AFFONDATO! " + SchermataPiazzamento.nomeNave(g.naveIn(p) != null ? g.naveIn(p).getLunghezza() : 0)
                        + " colata a picco", Tema.GIALLO_FUOCO);
            }
            default -> { /* NON_VALIDO: non dovrebbe capitare, già filtrato */ }
        }
    }

    /** Aspetta che gli effetti sul pannello finiscano, poi esegue l'azione. */
    private void attendiAnimazioni(PannelloGriglia pan, Runnable dopo) {
        Timer t = new Timer(60, null);
        t.addActionListener(e -> {
            if (!pan.animazioniInCorso()) {
                t.stop();
                dopo.run();
            }
        });
        t.start();
    }

    private void fine(Giocatore vincitore) {
        boolean trionfo;
        String titolo, sub;
        if (vsIA) {
            trionfo = vincitore.eUmano();
            titolo = trionfo ? "VITTORIA!" : "SCONFITTA";
            sub = trionfo ? "Hai mandato a fondo l'intera flotta nemica!"
                          : "Il Computer ha affondato tutte le tue navi.";
        } else {
            trionfo = true;
            titolo = "VINCE " + vincitore.getNome().toUpperCase();
            sub = "Flotta avversaria distrutta. Ammiraglio implacabile!";
        }
        finestra.mostra(new SchermataFine(finestra, trionfo, titolo, sub));
    }

    private void imposta(String msg, Color colore) {
        this.messaggio = msg;
        this.messaggioColore = colore;
        repaint();
    }

    private void ridisegnaFlotte() {
        if (flottaSx != null) flottaSx.repaint();
        if (flottaDx != null) flottaDx.repaint();
    }

    // ==========================================================================
    //  COMPONENTI INTERNI
    // ==========================================================================

    /** Barra superiore: messaggio dell'azione + indicazione del turno. */
    private class Intestazione extends JPanel {
        Intestazione() { setOpaque(false); setPreferredSize(new Dimension(10, 84)); }
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            Tema.qualita(g);
            int w = getWidth();
            Tema.testoLuminoso(g, messaggio.isEmpty() ? "BATTAGLIA NAVALE" : messaggio,
                    w / 2, 42, Tema.grande(26), messaggioColore);
            g.setColor(Tema.TESTO_SOFT);
            g.setFont(Tema.normale(15));
            String turno = inputBloccato ? "Attendi…" : "Turno di " + attaccante.getNome();
            int tw = g.getFontMetrics().stringWidth(turno);
            g.drawString(turno, w / 2 - tw / 2, 66);
            g.dispose();
        }
    }

    /** Titoletto sopra una griglia. */
    private static class Didascalia extends JPanel {
        private final String testo; private final Color colore;
        Didascalia(String testo, Color colore) {
            this.testo = testo; this.colore = colore;
            setOpaque(false); setPreferredSize(new Dimension(10, 40));
        }
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            Tema.qualita(g);
            Tema.testoLuminoso(g, testo, getWidth() / 2, 26, Tema.grande(20), colore);
            g.dispose();
        }
    }

    /**
     * Barra di stato di una flotta: una fila di navi a segmenti.
     * Sulla propria flotta i segmenti colpiti diventano fuoco; sulla flotta
     * nemica si rivela solo quando una nave è interamente affondata.
     */
    private static class PannelloFlotta extends JPanel {
        private final Griglia griglia;
        private final boolean propria;
        PannelloFlotta(Griglia griglia, boolean propria) {
            this.griglia = griglia; this.propria = propria;
            setOpaque(false); setPreferredSize(new Dimension(10, 70));
        }
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            Tema.qualita(g);
            int w = getWidth();

            int affondate = 0;
            for (Nave n : griglia.getFlotta()) if (n.affondata()) affondate++;
            int totali = griglia.getFlotta().size();

            g.setFont(Tema.grande(14));
            g.setColor(Tema.TESTO_SOFT);
            String etich = (propria ? "FLOTTA" : "NEMICO") + "  " + affondate + "/" + Math.max(totali, GestorePartita.FLOTTA.length) + " ✖";
            g.drawString(etich, 6, 20);

            // disegna le navi a segmenti, centrate
            int seg = 13, gap = 3, spazioNave = 16;
            int larghezzaTot = 0;
            for (int len : GestorePartita.FLOTTA) larghezzaTot += len * (seg + gap) + spazioNave;
            int x = Math.max(90, (w - larghezzaTot) / 2);
            int y = 34;

            java.util.List<Nave> flotta = griglia.getFlotta();
            for (int i = 0; i < GestorePartita.FLOTTA.length; i++) {
                int len = GestorePartita.FLOTTA[i];
                Nave nave = i < flotta.size() ? flotta.get(i) : null;
                boolean affondata = nave != null && nave.affondata();
                int colpi = nave != null ? nave.getColpiSubiti() : 0;
                for (int s = 0; s < len; s++) {
                    boolean rotto = propria ? (s < colpi) : affondata;
                    Color c;
                    if (rotto) c = affondata ? Tema.ROSSO_NO : Tema.ARANCIO;
                    else c = propria ? Tema.NAVE_CORPO : Tema.conAlpha(Tema.MARE_CHIARO, 200);
                    g.setColor(c);
                    g.fillRoundRect(x + s * (seg + gap), y, seg, 16, 5, 5);
                    if (rotto) { // crocetta sul segmento distrutto
                        g.setColor(Tema.conAlpha(Color.BLACK, 120));
                        g.drawLine(x + s * (seg + gap) + 2, y + 2, x + s * (seg + gap) + seg - 2, y + 14);
                        g.drawLine(x + s * (seg + gap) + seg - 2, y + 2, x + s * (seg + gap) + 2, y + 14);
                    }
                }
                x += len * (seg + gap) + spazioNave;
            }
            g.dispose();
        }
    }
}
