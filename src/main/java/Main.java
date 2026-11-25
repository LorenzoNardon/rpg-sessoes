import rpg.apresentacao.interfacegrafica.TelaPrincipal;
import rpg.infraestrutura.configuracao.ConfiguracaoBanco;
import rpg.infraestrutura.configuracao.FabricaDataSource;
import rpg.infraestrutura.configuracao.MigradorBanco;
import rpg.infraestrutura.persistencia.jdbc.JdbcCampanhaRepositorio;
import rpg.infraestrutura.persistencia.jdbc.JdbcJogadorRepositorio;
import rpg.infraestrutura.persistencia.jdbc.JdbcSessaoRepositorio;
import rpg.servico.GerenciadorRpg;

import javax.sql.DataSource;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            ConfiguracaoBanco configuracao = ConfiguracaoBanco.carregar();
            DataSource dataSource = FabricaDataSource.criar(configuracao);
            MigradorBanco.migrar(dataSource);

            GerenciadorRpg gerenciador = new GerenciadorRpg(
                    new JdbcJogadorRepositorio(dataSource),
                    new JdbcCampanhaRepositorio(dataSource),
                    new JdbcSessaoRepositorio(dataSource)
            );
            abrirInterface(gerenciador);
        } catch (RuntimeException excecao) {
            exibirErroInicializacao(excecao);
        }
    }

    private static void abrirInterface(GerenciadorRpg gerenciador) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Usa a aparência padrão do Swing caso a do sistema não esteja disponível.
            }
            TelaPrincipal tela = new TelaPrincipal(gerenciador);
            tela.setVisible(true);
        });
    }

    private static void exibirErroInicializacao(RuntimeException excecao) {
        String mensagem = "Não foi possível inicializar o banco de dados.\n"
                + "Inicie o PostgreSQL com: docker compose up -d\n\n"
                + "Detalhe: " + excecao.getMessage();
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                null, mensagem, "Erro de inicialização", JOptionPane.ERROR_MESSAGE
        ));
    }
}
