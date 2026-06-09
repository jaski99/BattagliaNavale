package giocatori;

import model.Griglia;
import model.Posizione;
import model.RisultatoSparo;

/**
 * Un giocatore, umano o IA. È una classe ASTRATTA: definisce cosa ogni
 * giocatore DEVE saper fare, ma non come (lo decidono le sottoclassi
 * GiocatoreUmano e GiocatoreIA).
 *
 * Grazie a questa classe, il gestore della partita tratta i due giocatori allo
 * stesso modo: "piazza la flotta", "scegli dove sparare", "ecco com'è andata".
 * Niente più if per distinguere chi sta giocando.
 */
public abstract class Giocatore {

    private final String nome;
    private final Griglia griglia; // la griglia con la PROPRIA flotta

    protected Giocatore(String nome, Griglia griglia) {
        this.nome = nome;
        this.griglia = griglia;
    }

    public String getNome() {
        return nome;
    }

    public Griglia getGriglia() {
        return griglia;
    }

    /** Vero se è un umano (serve al gestore per decidere cosa mostrare). */
    public abstract boolean eUmano();

    /** Sistema la propria flotta sulla propria griglia. */
    public abstract void piazzaFlotta(int[] lunghezzeNavi);

    /** Sceglie dove sparare sulla griglia nemica. */
    public abstract Posizione scegliMossa(Griglia grigliaNemica);

    /**
     * Riceve l'esito del proprio colpo. L'umano lo ignora (vede il risultato a
     * schermo), l'IA invece lo usa per ragionare sulla mossa successiva.
     */
    public void ricevutoEsito(Posizione p, RisultatoSparo esito) {
        // di default non fa nulla
    }
}
