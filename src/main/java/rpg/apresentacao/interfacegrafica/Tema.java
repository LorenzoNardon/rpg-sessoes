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

    /** Cor de fundo geral das telas. */
    public static final Color COR_FUNDO = new Color(0xF4F2EC);

    /** Cor das bordas de campos, cartões e listas. */
    public static final Color COR_BORDA = new Color(0xCFCABB);

    /** Cor principal dos textos. */
    public static final Color COR_TEXTO = new Color(0x1F1D2B);

    /** Cor de textos secundários e rótulos. */
    public static final Color COR_TEXTO_SUAVE = new Color(0x5C5870);

    /** Cor principal da identidade visual (índigo). */
    public static final Color COR_PRIMARIA = new Color(0x3B3486);

    /** Variação da cor primária usada quando o mouse está sobre o botão. */
    public static final Color COR_PRIMARIA_HOVER = new Color(0x4C44A6);

    /** Variação escura da cor primária, usada em cabeçalhos e contornos. */
    public static final Color COR_PRIMARIA_ESCURA = new Color(0x2B2565);

    /** Cor usada para erros e ações destrutivas. */
    public static final Color COR_PERIGO = new Color(0xB3261E);

    /** Variação da cor de perigo usada quando o mouse está sobre o botão. */
    public static final Color COR_PERIGO_HOVER = new Color(0x8F1D17);

    /** Cor usada para indicar sucesso. */
    public static final Color COR_SUCESSO = new Color(0x1B7F3B);

    /** Cor de fundo dos itens selecionados. */
    public static final Color COR_SELECAO = new Color(0xDAD6F5);

    /** Cor de destaque do componente que possui o foco. */
    public static final Color COR_FOCO = new Color(0xD98E04);

    /** Indica se o tema já foi aplicado, evitando configurações repetidas. */
    private static boolean aplicado;

    /**
     * Construtor privado: a classe é utilitária e não deve ser instanciada.
     */
    private Tema() {
    }

    /**
     * Aplica as configurações visuais globais da aplicação uma única vez.
     * Deve ser chamado antes da criação de qualquer janela.
     */
    public static synchronized void aplicar() {

        if (aplicado) return;

        aplicado = true;

        System.setProperty("swing.boldMetal", "false");

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignorada) {
        }

        Font base = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

        for (Object chave : Collections.list(UIManager.getDefaults().keys())) {
            if (UIManager.get(chave) instanceof FontUIResource) {
                UIManager.put(chave, new FontUIResource(base));
            }
        }

        UIManager.put("Panel.background", COR_FUNDO);
        UIManager.put("OptionPane.background", COR_FUNDO);
        UIManager.put("OptionPane.messageForeground", COR_TEXTO);
        UIManager.put("Label.foreground", COR_TEXTO);

        UIManager.put("List.selectionBackground", COR_SELECAO);
        UIManager.put("List.selectionForeground", COR_TEXTO);

        UIManager.put("TabbedPane.selected", COR_FUNDO);
        UIManager.put("TabbedPane.focus", COR_FOCO);
        UIManager.put("TabbedPane.tabInsets", new Insets(8, 18, 8, 18));

        UIManager.put("Button.focus", COR_FOCO);

        UIManager.put("OptionPane.okButtonText", "OK");
        UIManager.put("OptionPane.cancelButtonText", "Cancelar");
        UIManager.put("OptionPane.yesButtonText", "Sim");
        UIManager.put("OptionPane.noButtonText", "Não");
    }
}
