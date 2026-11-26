package rpg.apresentacao.interfacegrafica;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dimension;

/** Diálogos padronizados do sistema. */
public final class Dialogos {

    private Dialogos() {
    }

    public static void aviso(Component pai, String texto) {
        JOptionPane.showMessageDialog(pai, texto, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirmarExclusao(Component pai, String mensagem) {
        return confirmar(pai, "Confirmar exclusão", mensagem, "Excluir", "Cancelar");
    }

    /** O botão padrão do diálogo é sempre o de voltar, para evitar confirmações acidentais. */
    public static boolean confirmar(Component pai, String titulo, String mensagem,
                                    String textoConfirmar, String textoVoltar) {
        Object[] opcoes = {textoConfirmar, textoVoltar};
        return JOptionPane.showOptionDialog(pai, mensagem, titulo, JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE, null, opcoes, opcoes[1]) == 0;
    }

    /** Reabre o diálogo enquanto os dados forem inválidos, sem o usuário perder o que digitou. */
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
