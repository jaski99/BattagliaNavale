package giocatori;

import model.Griglia;
import model.Posizione;

/**
 * Giocatore umano in versione grafica.
 *
 * A differenza della versione da terminale, qui l'umano non "chiede" nulla da
 * solo: le sue scelte (dove piazzare, dove sparare) arrivano dai click del mouse
 * e vengono applicate direttamente dalle schermate della GUI. Per questo i metodi
 * decisionali non vengono usati e questa classe serve soprattutto a tenere
 * insieme il nome e la propria griglia, esattamente come per l'IA.
 */
public class GiocatoreUmano extends Giocatore {

    public GiocatoreUmano(String nome, Griglia griglia) {
        super(nome, griglia);
    }

    @Override
    public boolean eUmano() {
        return true;
    }

    @Override
    public void piazzaFlotta(int[] lunghezzeNavi) {
        // Nella GUI il piazzamento è interattivo: lo gestisce la SchermataPiazzamento.
    }

    @Override
    public Posizione scegliMossa(Griglia grigliaNemica) {
        // Nella GUI la mossa arriva dal click del mouse, non viene decisa qui.
        throw new UnsupportedOperationException(
                "La mossa dell'umano nella GUI arriva dal mouse, non da scegliMossa().");
    }
}
