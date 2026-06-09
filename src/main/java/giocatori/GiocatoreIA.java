package giocatori;

import model.Griglia;
import model.Posizione;
import model.RisultatoSparo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Giocatore controllato dal computer. Usa la classica strategia "Caccia e Mira":
 *
 *  - CACCIA: finché non trova una nave, spara a caso sulle caselle di una
 *    scacchiera (somma riga+colonna pari). Basta colpire metà delle caselle per
 *    trovare di sicuro ogni nave, visto che la più piccola è lunga 2.
 *
 *  - MIRA: appena colpisce, mette in coda le caselle vicine e le prova una a una
 *    per affondare la nave. Con due colpi capisce se la nave è orizzontale o
 *    verticale e prosegue solo lungo quella linea.
 */
public class GiocatoreIA extends Giocatore {

    // Un solo Random per tutta la partita: creare un Random a ogni sparo è inutile.
    private static final Random RND = new Random();

    // Caselle da escludere: già sparate, oppure mare certo attorno a navi affondate.
    private final boolean[][] escluse = new boolean[Griglia.DIM][Griglia.DIM];

    // Caselle da provare per affondare la nave che stiamo inseguendo.
    private final Deque<Posizione> bersagli = new ArrayDeque<>();

    // I colpi messi a segno sulla nave attualmente sotto attacco (non ancora affondata).
    private final List<Posizione> colpiNaveCorrente = new ArrayList<>();

    public GiocatoreIA(String nome, Griglia griglia) {
        super(nome, griglia);
    }

    @Override
    public boolean eUmano() {
        return false;
    }

    @Override
    public void piazzaFlotta(int[] lunghezzeNavi) {
        for (int lunghezza : lunghezzeNavi) {
            boolean piazzata = false;
            while (!piazzata) {
                int r = RND.nextInt(Griglia.DIM);
                int c = RND.nextInt(Griglia.DIM);
                boolean orizzontale = RND.nextBoolean();
                if (getGriglia().puoiPiazzare(r, c, lunghezza, orizzontale)) {
                    getGriglia().piazzaNave(r, c, lunghezza, orizzontale);
                    piazzata = true;
                }
            }
        }
    }

    @Override
    public Posizione scegliMossa(Griglia grigliaNemica) {
        // MIRA: se ho dei bersagli in coda, ne provo uno ancora valido.
        while (!bersagli.isEmpty()) {
            Posizione p = bersagli.poll();
            if (!escluse[p.riga()][p.colonna()]) {
                return p;
            }
        }
        // CACCIA: nessun bersaglio, sparo su una casella libera.
        return cellaDaCaccia();
    }

    /**
     * Sceglie una casella della scacchiera (riga+colonna pari) ancora libera.
     * Se non ne resta nessuna, ripiega su una casella libera qualsiasi: così non
     * resta mai bloccata (era il bug del do-while infinito).
     */
    private Posizione cellaDaCaccia() {
        List<Posizione> scacchiera = new ArrayList<>();
        List<Posizione> qualsiasi = new ArrayList<>();
        for (int r = 0; r < Griglia.DIM; r++) {
            for (int c = 0; c < Griglia.DIM; c++) {
                if (escluse[r][c]) continue;
                Posizione p = new Posizione(r, c);
                qualsiasi.add(p);
                if ((r + c) % 2 == 0) scacchiera.add(p);
            }
        }
        List<Posizione> candidate = scacchiera.isEmpty() ? qualsiasi : scacchiera;
        return candidate.get(RND.nextInt(candidate.size()));
    }

    @Override
    public void ricevutoEsito(Posizione p, RisultatoSparo esito) {
        escluse[p.riga()][p.colonna()] = true;

        switch (esito) {
            case COLPITO -> {
                colpiNaveCorrente.add(p);
                ricalcolaBersagli();
            }
            case AFFONDATO -> {
                colpiNaveCorrente.add(p);
                escludiContorno();        // tutto attorno alla nave è mare
                colpiNaveCorrente.clear();
                bersagli.clear();         // torno in modalità CACCIA
            }
            default -> {
                // ACQUA o NON_VALIDO: niente da fare
            }
        }
    }

    /**
     * Ricostruisce la coda dei bersagli in base ai colpi messi a segno finora
     * sulla nave in corso.
     *  - 1 colpo: provo le 4 caselle adiacenti (su/giù/sinistra/destra).
     *  - 2+ colpi: conosco la direzione, quindi provo solo le due estremità
     *    della linea di colpi.
     */
    private void ricalcolaBersagli() {
        bersagli.clear();
        Posizione primo = colpiNaveCorrente.get(0);

        if (colpiNaveCorrente.size() == 1) {
            aggiungiBersaglio(new Posizione(primo.riga() - 1, primo.colonna()));
            aggiungiBersaglio(new Posizione(primo.riga() + 1, primo.colonna()));
            aggiungiBersaglio(new Posizione(primo.riga(), primo.colonna() - 1));
            aggiungiBersaglio(new Posizione(primo.riga(), primo.colonna() + 1));
            return;
        }

        // Tutti i colpi della stessa nave stanno in fila: stessa riga = orizzontale.
        boolean orizzontale = colpiNaveCorrente.stream()
                .allMatch(q -> q.riga() == primo.riga());

        if (orizzontale) {
            int riga = primo.riga();
            int minCol = Griglia.DIM, maxCol = -1;
            for (Posizione q : colpiNaveCorrente) {
                minCol = Math.min(minCol, q.colonna());
                maxCol = Math.max(maxCol, q.colonna());
            }
            aggiungiBersaglio(new Posizione(riga, minCol - 1));
            aggiungiBersaglio(new Posizione(riga, maxCol + 1));
        } else {
            int colonna = primo.colonna();
            int minRiga = Griglia.DIM, maxRiga = -1;
            for (Posizione q : colpiNaveCorrente) {
                minRiga = Math.min(minRiga, q.riga());
                maxRiga = Math.max(maxRiga, q.riga());
            }
            aggiungiBersaglio(new Posizione(minRiga - 1, colonna));
            aggiungiBersaglio(new Posizione(maxRiga + 1, colonna));
        }
    }

    /** Aggiunge un bersaglio solo se è dentro la griglia e non è già escluso. */
    private void aggiungiBersaglio(Posizione p) {
        if (p.valida() && !escluse[p.riga()][p.colonna()]) {
            bersagli.add(p);
        }
    }

    /** Segna come "mare certo" le 8 caselle attorno a ogni pezzo della nave affondata. */
    private void escludiContorno() {
        for (Posizione colpo : colpiNaveCorrente) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int r = colpo.riga() + dr;
                    int c = colpo.colonna() + dc;
                    if (r >= 0 && r < Griglia.DIM && c >= 0 && c < Griglia.DIM) {
                        escluse[r][c] = true;
                    }
                }
            }
        }
    }
}
