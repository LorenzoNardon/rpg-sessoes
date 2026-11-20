import rpg.interfacegrafica.TelaPrincipal;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Usa a aparência padrão do Swing caso a do sistema não esteja disponível.
            }
            TelaPrincipal tela = new TelaPrincipal();
            tela.setVisible(true);
        });
    }
}
