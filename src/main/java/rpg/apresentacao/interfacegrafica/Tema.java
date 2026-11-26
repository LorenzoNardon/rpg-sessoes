package rpg.apresentacao.interfacegrafica;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.util.Collections;

/** Paleta de cores e aparência global (fonte, look and feel e textos dos diálogos). */
public final class Tema {

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

    private static boolean aplicado;

    private Tema() {
    }

    /** Aplica o tema uma única vez. Deve ser chamado antes de criar qualquer janela. */
    public static synchronized void aplicar() {
        if (aplicado) return;
        aplicado = true;

        System.setProperty("swing.boldMetal", "false");
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignorada) {
            // mantém o visual padrão
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
