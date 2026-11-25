package rpg.dominio.excecao;

public class SessaoLotadaException extends RuntimeException {
    public SessaoLotadaException(String message) {
        super(message);
    }
}
