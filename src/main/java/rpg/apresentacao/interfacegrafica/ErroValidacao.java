
package rpg.apresentacao.interfacegrafica;

import javax.swing.JComponent;

/**
 * Representa um erro de validação de entrada e armazena
 * o componente da interface gráfica onde o problema ocorreu.
 */
public class ErroValidacao extends IllegalArgumentException {

    // Referência ao campo da interface que contém o erro.
    // transient indica que esse atributo não será serializado.
    private final transient JComponent campo;

    // Construtor que recebe a mensagem do erro e o campo relacionado.
    public ErroValidacao(String mensagem, JComponent campo) {
        // Encaminha a mensagem para a classe pai,
        // permitindo utilizar os mecanismos padrão de exceções do Java.
        super(mensagem);

        // Armazena o componente que deverá receber o foco para correção.
        this.campo = campo;
    }

    // Retorna o componente da interface associado ao erro.
    // É utilizado para direcionar o foco ao campo que precisa ser corrigido.
    public JComponent getCampo() {
        return campo;
    }
}
