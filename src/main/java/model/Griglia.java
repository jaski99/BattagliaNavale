package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Il campo di gioco 10x10 di un giocatore: contiene le celle e la flotta.
 *
 * Questa classe conosce solo le REGOLE (dove si può piazzare una nave, cosa
 * succede quando si spara, quando la flotta è distrutta). Non disegna nulla e
 * non legge il mouse: di quello si occupa il package "gui".
 */
public class Griglia {

    /** Lato della griglia. Un solo posto dove cambiarlo, se mai servisse. */
    public static final int DIM = 10;

    private final Cella[][] celle = new Cella[DIM][DIM];
    private final List<Nave> flotta = new ArrayList<>();

    public Griglia() {
        for (int r = 0; r < DIM; r++) {
            for (int c = 0; c < DIM; c++) {
                celle[r][c] = new Cella();
            }
        }
    }

    /** Stato di una cella: è l'unico modo per "guardare" la griglia da fuori. */
    public StatoCella getStato(int r, int c) {
        return celle[r][c].stato;
    }

    public StatoCella getStato(Posizione p) {
        return getStato(p.riga(), p.colonna());
    }

    /** La nave presente nella cella, oppure null. Serve alla GUI per disegnare l'affondamento. */
    public Nave naveIn(Posizione p) {
        return celle[p.riga()][p.colonna()].nave;
    }

    private boolean dentro(int r, int c) {
        return r >= 0 && r < DIM && c >= 0 && c < DIM;
    }

    // ---------------------------------------------------------------
    // PIAZZAMENTO
    // ---------------------------------------------------------------

    /**
     * Si può piazzare qui una nave? Controlla due cose:
     * 1) che non esca dai bordi;
     * 2) che tutta l'area attorno alla nave (la nave + una cornice di 1 cella)
     *    sia ancora mare. Così due navi non possono toccarsi, nemmeno in diagonale.
     */
    public boolean puoiPiazzare(int r, int c, int lunghezza, boolean orizzontale) {
        // 1) controllo dei bordi
        if (r < 0 || c < 0) return false;
        if (orizzontale && c + lunghezza > DIM) return false;
        if (!orizzontale && r + lunghezza > DIM) return false;

        // 2) controllo della cornice: una cella in più per ogni lato
        int rInizio = Math.max(0, r - 1);
        int cInizio = Math.max(0, c - 1);
        int rFine = Math.min(DIM - 1, orizzontale ? r + 1 : r + lunghezza);
        int cFine = Math.min(DIM - 1, orizzontale ? c + lunghezza : c + 1);

        for (int i = rInizio; i <= rFine; i++) {
            for (int j = cInizio; j <= cFine; j++) {
                if (celle[i][j].stato != StatoCella.MARE) {
                    return false; // c'è già una nave troppo vicina
                }
            }
        }
        return true;
    }

    /** Mette una nave sulla griglia. Va chiamato solo dopo puoiPiazzare(). */
    public void piazzaNave(int r, int c, int lunghezza, boolean orizzontale) {
        Nave nave = new Nave(lunghezza);
        flotta.add(nave);

        for (int i = 0; i < lunghezza; i++) {
            int rr = orizzontale ? r : r + i;
            int cc = orizzontale ? c + i : c;
            celle[rr][cc].stato = StatoCella.NAVE;
            celle[rr][cc].nave = nave;
            nave.occupa(new Posizione(rr, cc));
        }
    }

    // ---------------------------------------------------------------
    // SPARO
    // ---------------------------------------------------------------

    /** Spara su una posizione e dice com'è andata. */
    public RisultatoSparo spara(Posizione p) {
        int r = p.riga();
        int c = p.colonna();
        if (!dentro(r, c)) return RisultatoSparo.NON_VALIDO;

        Cella cella = celle[r][c];

        // già sparato qui in passato
        if (cella.stato == StatoCella.COLPITO || cella.stato == StatoCella.ACQUA) {
            return RisultatoSparo.NON_VALIDO;
        }

        // colpo a vuoto
        if (cella.stato == StatoCella.MARE) {
            cella.stato = StatoCella.ACQUA;
            return RisultatoSparo.ACQUA;
        }

        // colpito una nave
        cella.stato = StatoCella.COLPITO;
        cella.nave.subisciColpo();
        if (cella.nave.affondata()) {
            segnaMareAttorno(cella.nave); // tutto intorno a una nave affondata è mare
            return RisultatoSparo.AFFONDATO;
        }
        return RisultatoSparo.COLPITO;
    }

    /**
     * Quando una nave affonda, le 8 celle attorno a ciascun suo pezzo sono
     * sicuramente mare (le navi non si toccano). Le marchiamo come ACQUA così
     * compaiono già "rivelate" e nessuno spreca colpi lì.
     */
    private void segnaMareAttorno(Nave nave) {
        for (Posizione p : nave.getPosizioni()) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int r = p.riga() + dr;
                    int c = p.colonna() + dc;
                    if (dentro(r, c) && celle[r][c].stato == StatoCella.MARE) {
                        celle[r][c].stato = StatoCella.ACQUA;
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // VITTORIA
    // ---------------------------------------------------------------

    /** Vero quando la flotta è stata schierata ed è tutta affondata. */
    public boolean tutteNaviAffondate() {
        return !flotta.isEmpty() && flotta.stream().allMatch(Nave::affondata);
    }

    /** La flotta schierata. Serve alla GUI per mostrare lo stato delle navi nell'HUD. */
    public List<Nave> getFlotta() {
        return flotta;
    }

    /**
     * Riporta la griglia allo stato iniziale (tutto mare, nessuna nave).
     * Serve al pulsante "Ricomincia" nella fase di piazzamento della GUI.
     */
    public void svuota() {
        for (int r = 0; r < DIM; r++) {
            for (int c = 0; c < DIM; c++) {
                celle[r][c].stato = StatoCella.MARE;
                celle[r][c].nave = null;
            }
        }
        flotta.clear();
    }
}
