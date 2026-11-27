
package rpg.dominio.modelo;

/**
 * Representa os possíveis estados de uma sessão de RPG
 * durante seu ciclo de vida.
 */
public enum StatusSessao {

    /** A sessão foi criada e está aguardando seu início. */
    AGENDADA,

    /** A sessão foi iniciada e está em andamento. */
    EM_ANDAMENTO,

    /** A sessão foi concluída. */
    FINALIZADA,

    /** A sessão foi cancelada antes de sua conclusão. */
    CANCELADA
}