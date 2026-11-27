package rpg.apresentacao.interfacegrafica;

import javax.swing.JComponent;

/**
 * Representa um erro de validação de entrada e armazena
 * o componente da interface gráfica onde o problema ocorreu.
 */
public class ErroValidacao extends IllegalArgumentException {

    /** Campo da interface que contém o erro; {@code transient} evita a serialização. */
    private final transient JComponent campo;

    /**
     * Cria o erro com a mensagem e o campo relacionado.
     *
     * @param mensagem descrição do problema
     * @param campo    componente que deverá receber o foco para correção
     */
    public ErroValidacao(String mensagem, JComponent campo) {
        super(mensagem);
        this.campo = campo;
    }

    /**
     * Retorna o componente associado ao erro, usado para direcionar o foco à correção.
     *
     * @return campo com problema
     */
    public JComponent getCampo() {
        return campo;
    }
}
