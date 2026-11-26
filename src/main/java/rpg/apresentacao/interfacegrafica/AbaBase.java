package rpg.apresentacao.interfacegrafica;

import rpg.servico.GerenciadorRpg;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;

/** Base das abas: painel com margens padrão, acesso ao serviço e ao contexto da janela. */
abstract class AbaBase extends JPanel {

    protected final GerenciadorRpg gerenciador;
    protected final ContextoTela contexto;

    protected AbaBase(GerenciadorRpg gerenciador, ContextoTela contexto) {
        super(new BorderLayout(14, 14));
        this.gerenciador = gerenciador;
        this.contexto = contexto;
        setBackground(Tema.COR_FUNDO);
        setBorder(new EmptyBorder(16, 16, 16, 16));
    }

    /** Botão acionado pela tecla Enter enquanto a aba está visível. */
    abstract JButton getBotaoPadrao();

    /** Relê os dados do serviço e atualiza os componentes, mantendo a seleção do usuário. */
    abstract void atualizar();
}
