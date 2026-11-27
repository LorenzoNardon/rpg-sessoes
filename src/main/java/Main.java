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

/**
 * Ponto de entrada da aplicação de gerenciamento de sessões de RPG.
 *
 * Responsável por configurar o acesso ao banco de dados,
 * executar as migrações necessárias e inicializar a interface gráfica.
 */
public class Main {

    /**
     * Inicializa os componentes da aplicação e abre a interface gráfica.
     *
     * @param args argumentos recebidos na inicialização da aplicação
     */
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

    /**
     * Configura a aparência da interface e abre a tela principal
     * na thread de despacho de eventos do Swing.
     *
     * @param gerenciador responsável pelas operações de gerenciamento do RPG
     */
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

    /**
     * Exibe uma mensagem informando que a inicialização da aplicação falhou,
     * incluindo orientações para iniciar o banco de dados.
     *
     * @param excecao exceção responsável pela falha na inicialização
     */
    private static void exibirErroInicializacao(RuntimeException excecao) {
        String mensagem = "Não foi possível inicializar o banco de dados.\n"
                + "Inicie o PostgreSQL com: docker compose up -d\n\n"
                + "Detalhe: " + excecao.getMessage();
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                null, mensagem, "Erro de inicialização", JOptionPane.ERROR_MESSAGE
        ));
    }
}