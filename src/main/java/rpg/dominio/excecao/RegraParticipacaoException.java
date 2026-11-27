package rpg.dominio.excecao;

/**
 * Exceção lançada quando uma regra de participação impede
 * a entrada de um jogador em uma sessão.
 */
public class RegraParticipacaoException extends RuntimeException {

    /**
     * Cria uma exceção com uma mensagem que descreve
     * o motivo da restrição de participação.
     *
     * @param message mensagem explicativa da exceção
     */
    public RegraParticipacaoException(String message) {
        super(message);
    }
}