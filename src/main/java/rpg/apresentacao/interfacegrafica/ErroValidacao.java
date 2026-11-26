package rpg.apresentacao.interfacegrafica;

import javax.swing.JComponent;

/** Erro de validação de entrada que sabe qual componente da tela deve receber o foco. */
public class ErroValidacao extends IllegalArgumentException {

    private final transient JComponent campo;

    public ErroValidacao(String mensagem, JComponent campo) {
        super(mensagem);
        this.campo = campo;
    }

    public JComponent getCampo() {
        return campo;
    }
}
