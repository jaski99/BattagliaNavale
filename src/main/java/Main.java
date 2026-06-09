import gui.FinestraGioco;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto di avvio del gioco in versione grafica.
 * Il suo unico compito è accendere l'interfaccia: crea la finestra principale
 * sul thread grafico di Swing (l'EDT) e la mostra. Tutta la logica vera del
 * gioco vive negli altri package (model, giocatori, gui).
 */
public class Main {

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignorato) {
            // se non riesce, va benissimo il tema di default
        }
        SwingUtilities.invokeLater(() -> new FinestraGioco().setVisible(true));
    }
}
