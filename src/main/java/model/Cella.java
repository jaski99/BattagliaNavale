package model;

/**
 * Una singola cella della griglia.
 *
 * È un dettaglio interno di {@link Griglia}: per questo è "package-private"
 * (niente "public") e ha i campi accessibili solo dentro al package model.
 * Chi sta fuori dal model non deve sapere come è fatta una cella, usa la Griglia.
 */
class Cella {
    StatoCella stato = StatoCella.MARE;
    Nave nave = null; // la nave presente, oppure null se è solo mare
}
