package model;

/**
 * Una posizione sulla griglia: riga e colonna (entrambe 0..9).
 *
 * È un "record": una classe immutabile che serve solo a tenere insieme due dati.
 * Java ci genera in automatico costruttore, equals() e hashCode(), quindi
 * possiamo confrontare due posizioni per valore (cosa che con int[] non era possibile).
 */
public record Posizione(int riga, int colonna) {

    /** Vero se la posizione è dentro la griglia 10x10. */
    public boolean valida() {
        return riga >= 0 && riga < Griglia.DIM
            && colonna >= 0 && colonna < Griglia.DIM;
    }

    /** Mostra la posizione in stile "B4" (lettera per la riga, numero per la colonna). */
    @Override
    public String toString() {
        return "" + (char) ('A' + riga) + (colonna + 1);
    }
}
