package gioco;

import giocatori.Giocatore;
import giocatori.GiocatoreIA;
import giocatori.GiocatoreUmano;
import model.Griglia;

/**
 * Tiene insieme i pezzi di una partita: le due griglie, i due giocatori e la
 * modalità scelta. Non disegna nulla e non gestisce i turni (di quello si
 * occupano le schermate della GUI): è solo il "tavolo da gioco" condiviso.
 */
public class GestorePartita {

    /** Lunghezze delle navi della flotta: 1 portaerei, 1 corazzata, 2 incrociatori, 1 cacciatorpediniere. */
    public static final int[] FLOTTA = {5, 4, 3, 3, 2};

    private final boolean dueUmani;
    private final Giocatore g1;
    private final Giocatore g2;

    public GestorePartita(boolean dueUmani) {
        this.dueUmani = dueUmani;
        Griglia griglia1 = new Griglia();
        Griglia griglia2 = new Griglia();
        this.g1 = new GiocatoreUmano(dueUmani ? "Giocatore 1" : "Tu", griglia1);
        this.g2 = dueUmani
                ? new GiocatoreUmano("Giocatore 2", griglia2)
                : new GiocatoreIA("Computer", griglia2);
    }

    public boolean dueUmani() { return dueUmani; }
    public Giocatore g1() { return g1; }
    public Giocatore g2() { return g2; }
}
