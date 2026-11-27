package rpg.apresentacao.interfacegrafica;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dimension;

/**
 * Classe responsável por centralizar e padronizar
 * as caixas de diálogo utilizadas no sistema.
 */
public final class Dialogos {

    /**
     * Construtor privado: a classe só possui métodos estáticos.
     */
    private Dialogos() {
    }

    /**
     * Exibe uma mensagem de aviso para o usuário.
     *
     * @param pai   componente pai usado para posicionar o diálogo
     * @param texto mensagem exibida
     */
    public static void aviso(Component pai, String texto) {
        JOptionPane.showMessageDialog(pai, texto, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Exibe uma confirmação de exclusão, mantendo o padrão das demais confirmações.
     *
     * @param pai      componente pai usado para posicionar o diálogo
     * @param mensagem pergunta exibida ao usuário
     * @return {@code true} se o usuário confirmar a exclusão
     */
    public static boolean confirmarExclusao(Component pai, String mensagem) {
        return confirmar(pai, "Confirmar exclusão", mensagem, "Excluir", "Cancelar");
    }

    /**
     * Exibe um diálogo com duas opções de resposta.
     * O botão de voltar é definido como padrão para evitar confirmações acidentais.
     *
     * @param pai            componente pai usado para posicionar o diálogo
     * @param titulo         título da janela
     * @param mensagem       mensagem exibida
     * @param textoConfirmar texto do botão de confirmação
     * @param textoVoltar    texto do botão de voltar
     * @return {@code true} somente se o usuário escolher a opção de confirmação
     */
    public static boolean confirmar(Component pai, String titulo, String mensagem,
                                    String textoConfirmar, String textoVoltar) {

        Object[] opcoes = {textoConfirmar, textoVoltar};

        return JOptionPane.showOptionDialog(pai, mensagem, titulo, JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, opcoes, opcoes[1]) == 0;
    }

    /**
     * Exibe um formulário de edição e permite corrigir os dados
     * enquanto houver erros de validação, sem perder o que foi digitado.
     *
     * @param pai           componente pai usado para posicionar o diálogo
     * @param titulo        título da janela
     * @param form          painel com os campos de edição
     * @param primeiroCampo componente que recebe o foco inicialmente
     * @param validacao     validação executada ao confirmar; lança
     *                      {@link IllegalArgumentException} em caso de dados inválidos
     * @return {@code true} se os dados foram confirmados e validados, {@code false} se cancelado
     */
    public static boolean edicao(Component pai, String titulo, JPanel form,
                                 JComponent primeiroCampo, Runnable validacao) {

        form.setPreferredSize(new Dimension(Math.max(400, form.getPreferredSize().width),
                form.getPreferredSize().height));

        JComponent proximoFoco = primeiroCampo;

        while (true) {

            JComponent alvo = proximoFoco;

            SwingUtilities.invokeLater(() -> alvo.requestFocusInWindow());

            int resposta = JOptionPane.showConfirmDialog(pai, form, titulo,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (resposta != JOptionPane.OK_OPTION) return false;

            try {
                validacao.run();
                return true;

            } catch (IllegalArgumentException ex) {

                if (ex instanceof ErroValidacao) {
                    proximoFoco = ((ErroValidacao) ex).getCampo();
                    Componentes.marcarErro(proximoFoco);
                }

                aviso(pai, ex.getMessage());
            }
        }
    }
}
