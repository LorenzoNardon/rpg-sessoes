package rpg.dominio.excecao;

/**
 * Exceção lançada quando um jogador tenta se inscrever
 * em uma sessão na qual já está inscrito.
 */
public class JogadorJaInscritoException extends RuntimeException {

    /**
     * Cria uma exceção com uma mensagem que descreve o erro ocorrido.
     *
     * @param message mensagem explicativa da exceção
     */
    public JogadorJaInscritoException(String message) {
        super(message);
    }
}