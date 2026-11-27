package rpg.dominio.excecao;

/**
 * Exceção lançada quando ocorre um erro durante uma operação
 * de persistência de dados.
 *
 * Permite encapsular a exceção original que causou a falha.
 */
public class PersistenciaException extends RuntimeException {

    /**
     * Cria uma exceção de persistência com uma mensagem
     * explicativa e a causa original do erro.
     *
     * @param mensagem descrição do erro de persistência
     * @param causa exceção original que provocou a falha
     */
    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}