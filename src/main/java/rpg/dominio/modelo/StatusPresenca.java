package rpg.dominio.modelo;

/**
 * Representa os possíveis estados de presença de um jogador
 * em uma sessão de RPG.
 */
public enum StatusPresenca {

    /** A presença do jogador ainda não foi registrada. */
    PENDENTE,

    /** O jogador confirmou presença na sessão. */
    PRESENTE,

    /** O jogador foi marcado como ausente na sessão. */
    AUSENTE
}