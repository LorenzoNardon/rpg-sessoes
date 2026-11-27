package rpg.dominio.excecao;

/**
 * Exceção lançada quando um jogador tenta entrar em uma sessão
 * que já atingiu sua capacidade máxima.
 */
public class SessaoLotadaException extends RuntimeException {

    /**
     * Cria uma exceção com uma mensagem que descreve
     * a falta de vagas disponíveis na sessão.
     *
     * @param message mensagem explicativa da exceção
     */
    public SessaoLotadaException(String message) {
        super(message);
    }
}