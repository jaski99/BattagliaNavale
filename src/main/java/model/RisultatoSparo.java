package model;

/** Cosa succede quando si spara su una cella. */
public enum RisultatoSparo {
    ACQUA,      // colpo a vuoto
    COLPITO,    // colpita una nave (ma non ancora affondata)
    AFFONDATO,  // colpita e affondata l'intera nave
    NON_VALIDO  // fuori griglia oppure cella già colpita in precedenza
}
