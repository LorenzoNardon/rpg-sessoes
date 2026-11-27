
package rpg.apresentacao.interfacegrafica;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.util.Collections;

/**
 * Centraliza a paleta de cores e a aparência global da aplicação,
 * incluindo fontes, estilo visual e textos dos diálogos.
 */
public final class Tema {

    // Cores utilizadas para manter a identidade visual da aplicação.
    public static final Color COR_FUNDO = new Color(0xF4F2EC);
    public static final Color COR_BORDA = new Color(0xCFCABB);
    public static final Color COR_TEXTO = new Color(0x1F1D2B);
    public static final Color COR_TEXTO_SUAVE = new Color(0x5C5870);
    public static final Color COR_PRIMARIA = new Color(0x3B3486);
    public static final Color COR_PRIMARIA_HOVER = new Color(0x4C44A6);
    public static final Color COR_PRIMARIA_ESCURA = new Color(0x2B2565);
    public static final Color COR_PERIGO = new Color(0xB3261E);
    public static final Color COR_PERIGO_HOVER = new Color(0x8F1D17);
    public static final Color COR_SUCESSO = new Color(0x1B7F3B);
    public static final Color COR_SELECAO = new Color(0xDAD6F5);
    public static final Color COR_FOCO = new Color(0xD98E04);

    // Indica se o tema já foi aplicado para evitar configurações repetidas.
    private static boolean aplicado;

    // Construtor privado para impedir a criação de objetos dessa classe utilitária.
    private Tema() {
    }

    /**
     * Aplica as configurações visuais globais da aplicação uma única vez.
     * Deve ser chamado antes da criação de qualquer janela.
     */
    public static synchronized void aplicar() {

        // Se o tema já foi aplicado, encerra o método.
        if (aplicado) return;

        // Marca o tema como aplicado para impedir novas execuções.
        aplicado = true;

        // Desativa o uso automático de fontes em negrito no estilo Metal.
        System.setProperty("swing.boldMetal", "false");

        try {
            // Define o estilo visual multiplataforma do Swing.
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignorada) {
            // Caso não seja possível alterar o estilo, mantém o visual padrão.
        }

        // Define a fonte padrão utilizada pelos componentes da aplicação.
        Font base = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

        // Percorre as configurações visuais do Swing.
        for (Object chave : Collections.list(UIManager.getDefaults().keys())) {

            // Verifica se a configuração atual corresponde a uma fonte.
            if (UIManager.get(chave) instanceof FontUIResource) {

                // Substitui a fonte encontrada pela fonte padrão definida acima.
                UIManager.put(chave, new FontUIResource(base));
            }
        }

        // Define as cores de fundo e de texto dos componentes.
        UIManager.put("Panel.background", COR_FUNDO);
        UIManager.put("OptionPane.background", COR_FUNDO);
        UIManager.put("OptionPane.messageForeground", COR_TEXTO);
        UIManager.put("Label.foreground", COR_TEXTO);

        // Define as cores utilizadas nas listas quando um item é selecionado.
        UIManager.put("List.selectionBackground", COR_SELECAO);
        UIManager.put("List.selectionForeground", COR_TEXTO);

        // Configura as cores das abas e do destaque de foco.
        UIManager.put("TabbedPane.selected", COR_FUNDO);
        UIManager.put("TabbedPane.focus", COR_FOCO);

        // Define o espaçamento interno das abas.
        UIManager.put("TabbedPane.tabInsets", new Insets(8, 18, 8, 18));

        // Define a cor de destaque dos botões quando recebem foco.
        UIManager.put("Button.focus", COR_FOCO);

        // Personaliza os textos dos botões dos diálogos do Swing.
        UIManager.put("OptionPane.okButtonText", "OK");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
        UIManager.put("OptionPane.yesButtonText", "Sim");
        UIManager.put("OptionPane.noButtonText", "Não");
    }
}
