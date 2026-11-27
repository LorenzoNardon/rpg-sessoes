package rpg.apresentacao.interfacegrafica;

import rpg.servico.GerenciadorRpg;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;

/**
 * Base das abas: painel com margens padrão, acesso ao serviço e ao contexto da janela.
 */
abstract class AbaBase extends JPanel {

    /** Serviço responsável pelas regras de negócio e pelos dados da aplicação. */
    protected final GerenciadorRpg gerenciador;

    /** Contexto da janela principal, usado para diálogos e execução de operações. */
    protected final ContextoTela contexto;

    /**
     * Cria a aba com layout, cor de fundo e margens padronizados.
     *
     * @param gerenciador serviço utilizado pela aba
     * @param contexto    contexto da janela principal
     */
    protected AbaBase(GerenciadorRpg gerenciador, ContextoTela contexto) {
        super(new BorderLayout(14, 14));
        this.gerenciador = gerenciador;
        this.contexto = contexto;
        setBackground(Tema.COR_FUNDO);
        setBorder(new EmptyBorder(16, 16, 16, 16));
    }

    /**
     * Botão acionado pela tecla Enter enquanto a aba está visível.
     *
     * @return botão padrão da aba
     */
    abstract JButton getBotaoPadrao();

    /**
     * Relê os dados do serviço e atualiza os componentes, mantendo a seleção do usuário.
     */
    abstract void atualizar();
}
