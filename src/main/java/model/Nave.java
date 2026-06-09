package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Una nave: sa quanto è lunga, DOVE si trova e quanti colpi ha subito.
 *
 * Tenere le posizioni dentro la nave permette, quando affonda, di sapere
 * esattamente quali celle occupava (utile per segnare il mare intorno e per
 * disegnare l'effetto di affondamento nella GUI).
 */
public class Nave {
    private final int lunghezza;
    private final List<Posizione> posizioni = new ArrayList<>();
    private int colpiSubiti = 0;

    public Nave(int lunghezza) {
        this.lunghezza = lunghezza;
    }

    /** Registra una cella occupata dalla nave (chiamato durante il piazzamento). */
    void occupa(Posizione p) {
        posizioni.add(p);
    }

    /** La nave incassa un colpo. */
    void subisciColpo() {
        colpiSubiti++;
    }

    /** Vero quando tutte le celle della nave sono state colpite. */
    public boolean affondata() {
        return colpiSubiti >= lunghezza;
    }

    public int getLunghezza() {
        return lunghezza;
    }

    /** Quanti colpi ha incassato finora. Serve alla GUI per la barra di salute. */
    public int getColpiSubiti() {
        return colpiSubiti;
    }

    public List<Posizione> getPosizioni() {
        return posizioni;
    }
}
