package model;

/** In che stato si trova una cella della griglia. */
public enum StatoCella {
    MARE,    // acqua non ancora colpita (e senza navi)
    NAVE,    // c'è una nave, non ancora colpita
    COLPITO, // c'era una nave ed è stata colpita
    ACQUA    // abbiamo sparato qui e non c'era nulla (mancato)
}
