package gui;

import gioco.GestorePartita;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * La finestra principale e il "regista" del gioco: tiene una sola area centrale
 * e ci fa scorrere dentro le varie schermate (menu, piazzamento, battaglia,
 * fine). Decide anche la sequenza delle fasi, mentre ogni schermata si occupa
 * solo di sé stessa.
 */
public class FinestraGioco extends JFrame {

    public FinestraGioco() {
        super("Battaglia Navale");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1040, 720));
        setSize(1180, 800);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());
        mostraMenu();
    }

    /** Sostituisce il contenuto della finestra con una nuova schermata. */
    public void mostra(JComponent schermata) {
        getContentPane().removeAll();
        getContentPane().add(schermata, BorderLayout.CENTER);
        revalidate();
        repaint();
        schermata.requestFocusInWindow();
    }

    public void mostraMenu() {
        mostra(new SchermataMenu(this));
    }

    /** Avvia una nuova partita nella modalità scelta dal menu. */
    public void iniziaPartita(boolean dueUmani) {
        GestorePartita gestore = new GestorePartita(dueUmani);
        // Fase 1: piazzamento del primo giocatore (umano).
        mostra(new SchermataPiazzamento(this, gestore.g1().getGriglia(),
                gestore.g1().getNome(), () -> dopoPiazzamentoG1(gestore)));
    }

    private void dopoPiazzamentoG1(GestorePartita gestore) {
        if (!gestore.dueUmani()) {
            // Contro l'IA: il computer schiera la flotta da solo, poi si combatte.
            gestore.g2().piazzaFlotta(GestorePartita.FLOTTA);
            avviaBattaglia(gestore);
            return;
        }
        // Due giocatori: si passa il computer e tocca al secondo piazzare.
        mostraPassaggio("PASSA IL COMPUTER", gestore.g2().getNome() + ", schiera la tua flotta",
                () -> mostra(new SchermataPiazzamento(this, gestore.g2().getGriglia(),
                        gestore.g2().getNome(), () -> dopoPiazzamentoG2(gestore))));
    }

    private void dopoPiazzamentoG2(GestorePartita gestore) {
        mostraPassaggio("TUTTO PRONTO", "Che la battaglia abbia inizio!",
                () -> avviaBattaglia(gestore));
    }

    public void avviaBattaglia(GestorePartita gestore) {
        mostra(new SchermataBattaglia(this, gestore));
    }

    /** Schermata "ponte" tra due fasi: utile soprattutto per non spiarsi in due. */
    public void mostraPassaggio(String titolo, String sottotitolo, Runnable onContinua) {
        mostra(new SchermataPassaggio(titolo, sottotitolo, onContinua));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FinestraGioco().setVisible(true));
    }
}
