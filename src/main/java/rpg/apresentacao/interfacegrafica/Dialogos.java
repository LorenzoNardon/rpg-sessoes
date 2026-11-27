
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

    // Construtor privado para impedir a criação de objetos dessa classe.
    // Todos os métodos são estáticos e podem ser chamados diretamente.
    private Dialogos() {
    }

    // Exibe uma mensagem de aviso para o usuário.
    // Recebe o componente pai para posicionar o diálogo corretamente.
    public static void aviso(Component pai, String texto) {
        JOptionPane.showMessageDialog(pai, texto, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    // Exibe uma confirmação de exclusão.
    // Utiliza o método confirmar para manter um padrão visual
    // e de comportamento nas confirmações do sistema.
    public static boolean confirmarExclusao(Component pai, String mensagem) {
        return confirmar(pai, "Confirmar exclusão", mensagem, "Excluir", "Cancelar");
    }

    /**
     * Exibe um diálogo com duas opções de resposta.
     * O botão de voltar é definido como padrão para evitar confirmações acidentais.
     */
    public static boolean confirmar(Component pai, String titulo, String mensagem,
                                    String textoConfirmar, String textoVoltar) {

        // Define os textos dos botões exibidos no diálogo.
        Object[] opcoes = {textoConfirmar, textoVoltar};

        // Exibe o diálogo de confirmação.
        // Retorna true somente quando o usuário seleciona a primeira opção.
        return JOptionPane.showOptionDialog(pai, mensagem, titulo, JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, opcoes, opcoes[1]) == 0;
    }

    /**
     * Exibe um formulário de edição e permite corrigir os dados
     * enquanto houver erros de validação, sem perder o que foi digitado.
     */
    public static boolean edicao(Component pai, String titulo, JPanel form,
                                 JComponent primeiroCampo, Runnable validacao) {

        // Define uma largura mínima para o formulário,
        // evitando que o diálogo fique estreito demais.
        form.setPreferredSize(new Dimension(Math.max(400, form.getPreferredSize().width),
                form.getPreferredSize().height));

        // Guarda o componente que deverá receber o foco inicialmente.
        JComponent proximoFoco = primeiroCampo;

        // Mantém o diálogo aberto enquanto os dados forem inválidos
        // ou até que o usuário cancele a edição.
        while (true) {

            // Armazena o componente que receberá o foco nesta tentativa.
            JComponent alvo = proximoFoco;

            // Solicita o foco para o campo antes de exibir o diálogo.
            // invokeLater agenda a operação para a fila de eventos do Swing.
            SwingUtilities.invokeLater(() -> alvo.requestFocusInWindow());

            // Exibe o formulário com as opções de confirmar ou cancelar.
            int resposta = JOptionPane.showConfirmDialog(pai, form, titulo,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            // Se o usuário cancelar ou fechar o diálogo, encerra a edição.
            if (resposta != JOptionPane.OK_OPTION) return false;

            try {
                // Executa a validação dos dados preenchidos no formulário.
                validacao.run();

                // Se nenhuma exceção for lançada, a validação foi concluída.
                return true;

            } catch (IllegalArgumentException ex) {

                // Verifica se o erro é uma exceção específica de validação
                // que informa qual campo contém o problema.
                if (ex instanceof ErroValidacao) {

                    // Recupera o campo que precisa ser corrigido.
                    proximoFoco = ((ErroValidacao) ex).getCampo();

                    // Destaca visualmente o campo com uma borda de erro.
                    Componentes.marcarErro(proximoFoco);
                }

                // Exibe a mensagem de erro para o usuário.
                // O formulário permanece aberto para que ele possa corrigir os dados.
                aviso(pai, ex.getMessage());
            }
        }
    }
}
