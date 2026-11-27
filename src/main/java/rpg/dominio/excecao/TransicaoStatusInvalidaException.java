package rpg.dominio.excecao;

/**
 * Exceção lançada quando ocorre uma tentativa de realizar
 * uma transição inválida entre os estados de uma sessão.
 */
public class TransicaoStatusInvalidaException extends RuntimeException {

    /**
     * Cria uma exceção com uma mensagem que descreve
     * o motivo da transição de estado inválida.
     *
     * @param message mensagem explicativa da exceção
     */
    public TransicaoStatusInvalidaException(String message) {
        super(message);
    }
}