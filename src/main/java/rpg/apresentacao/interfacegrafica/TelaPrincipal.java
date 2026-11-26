package rpg.apresentacao.interfacegrafica;

import rpg.servico.GerenciadorRpg;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static rpg.apresentacao.interfacegrafica.Tema.*;

/**
 * Janela principal: cabeçalho, abas e barra de status. O conteúdo de cada aba fica
 * em AbaJogadores, AbaCampanhas e AbaSessoes.
 */
public class TelaPrincipal extends JFrame implements ContextoTela {

    static {
        Tema.aplicar();
    }

    private final AbaBase[] secoes;
    private final JTabbedPane abas = new JTabbedPane();
    private final JLabel textoStatus = new JLabel("Pronto.");
    private final JProgressBar barraProgresso = new JProgressBar();
    private final JPanel painelOcupado = new JPanel();
    private final Timer temporizadorStatus = new Timer(7000, e -> definirStatusPadrao());
    private boolean ocupado;

    public TelaPrincipal(GerenciadorRpg gerenciador) {
        this(gerenciador, new DialogoSelecaoConvidados());
    }

    public TelaPrincipal(GerenciadorRpg gerenciador, SeletorConvidados seletorConvidados) {
        Objects.requireNonNull(gerenciador);
        Objects.requireNonNull(seletorConvidados);
        setTitle("Gerenciador de Sessões RPG");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 740);
        setMinimumSize(new Dimension(960, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COR_FUNDO);
        setIconImages(java.util.List.of(IconeD20.imagemDourada(32), IconeD20.imagemDourada(64)));

        configurarBloqueioOcupado();
        temporizadorStatus.setRepeats(false);

        secoes = new AbaBase[]{
                new AbaJogadores(gerenciador, this),
                new AbaCampanhas(gerenciador, this),
                new AbaSessoes(gerenciador, this, seletorConvidados)
        };
        abas.setFont(abas.getFont().deriveFont(Font.BOLD));
        abas.addTab("Jogadores", secoes[0]);
        abas.addTab("Campanhas", secoes[1]);
        abas.addTab("Sessões", secoes[2]);
        abas.addChangeListener(e ->
                getRootPane().setDefaultButton(secoes[abas.getSelectedIndex()].getBotaoPadrao()));

        add(criarCabecalho(), BorderLayout.NORTH);
        add(abas, BorderLayout.CENTER);
        add(criarBarraStatus(), BorderLayout.SOUTH);
        getRootPane().setDefaultButton(secoes[0].getBotaoPadrao());
        atualizarListas();
    }

    // ---------------------------------------------------------------------
    // ContextoTela
    // ---------------------------------------------------------------------
    @Override
    public JFrame janela() {
        return this;
    }

    /** Executa na thread da interface: validações e diálogos. Erros viram aviso ao usuário. */
    @Override
    public void executar(Runnable operacao) {
        try {
            operacao.run();
        } catch (RuntimeException ex) {
            tratarErro(ex);
        }
    }

    /** Executa a tarefa fora da thread da interface, sem congelar a janela. */
    @Override
    public void emSegundoPlano(Runnable tarefa, Runnable aoSucesso, String mensagemSucesso) {
        if (ocupado) return;
        definirOcupado(true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                tarefa.run();
                return null;
            }

            @Override
            protected void done() {
                definirOcupado(false);
                Throwable falha = null;
                try {
                    get();
                } catch (ExecutionException ex) {
                    falha = ex.getCause() != null ? ex.getCause() : ex;
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    falha = ex;
                }
                if (falha == null && aoSucesso != null) aoSucesso.run();
                atualizarListas();
                if (falha == null) informar(mensagemSucesso, false);
                else tratarErro(falha);
            }
        }.execute();
    }

    private void atualizarListas() {
        for (AbaBase aba : secoes) aba.atualizar();
    }

    // ---------------------------------------------------------------------
    // Cabeçalho e barra de status
    // ---------------------------------------------------------------------
    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(COR_PRIMARIA_ESCURA);
        cabecalho.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, COR_FOCO),
                new EmptyBorder(14, 20, 14, 20)));
        JLabel titulo = new JLabel("Gerenciador de Sessões RPG");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        titulo.setForeground(Color.WHITE);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel subtitulo = new JLabel("Jogadores, campanhas e sessões em um só lugar");
        subtitulo.setForeground(new Color(0xD8D4F2));
        subtitulo.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel textos = new JPanel(new GridLayout(0, 1, 0, 2));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(subtitulo);

        // Dois d20 do mesmo tamanho, um em cada lado, com o título no meio
        JLabel dadoEsquerdo = new JLabel(IconeD20.dourado(52, "20"));
        JLabel dadoDireito = new JLabel(IconeD20.dourado(52, "20"));
        dadoEsquerdo.getAccessibleContext().setAccessibleName("Dado de 20 lados");
        dadoDireito.getAccessibleContext().setAccessibleName("Dado de 20 lados");
        cabecalho.add(dadoEsquerdo, BorderLayout.WEST);
        cabecalho.add(textos, BorderLayout.CENTER);
        cabecalho.add(dadoDireito, BorderLayout.EAST);
        return cabecalho;
    }

    private JPanel criarBarraStatus() {
        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setBackground(Color.WHITE);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COR_BORDA),
                new EmptyBorder(7, 16, 7, 16)));
        textoStatus.setIconTextGap(8);
        textoStatus.getAccessibleContext().setAccessibleName("Mensagem de status");
        barraProgresso.setIndeterminate(true);
        barraProgresso.setVisible(false);
        barraProgresso.setPreferredSize(new Dimension(140, 14));
        barraProgresso.setForeground(COR_PRIMARIA);
        barra.add(textoStatus, BorderLayout.CENTER);
        barra.add(barraProgresso, BorderLayout.EAST);
        definirStatusPadrao();
        return barra;
    }

    private void definirStatusPadrao() {
        textoStatus.setText("Pronto.");
        textoStatus.setForeground(COR_TEXTO_SUAVE);
        textoStatus.setIcon(new IconePonto(COR_TEXTO_SUAVE));
    }

    private void informar(String texto, boolean erro) {
        Color cor = erro ? COR_PERIGO : COR_SUCESSO;
        textoStatus.setText(texto);
        textoStatus.setForeground(cor);
        textoStatus.setIcon(new IconePonto(cor));
        temporizadorStatus.restart();
    }

    private void configurarBloqueioOcupado() {
        painelOcupado.setOpaque(false);
        painelOcupado.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        painelOcupado.addMouseListener(new MouseAdapter() { });
        painelOcupado.addMouseMotionListener(new MouseMotionAdapter() { });
        setGlassPane(painelOcupado);
    }

    private void definirOcupado(boolean valor) {
        ocupado = valor;
        painelOcupado.setVisible(valor);
        barraProgresso.setVisible(valor);
        if (valor) {
            temporizadorStatus.stop();
            textoStatus.setText("Processando...");
            textoStatus.setForeground(COR_TEXTO_SUAVE);
            textoStatus.setIcon(new IconePonto(COR_PRIMARIA));
        }
    }

    private void tratarErro(Throwable ex) {
        String texto = ex.getMessage() == null || ex.getMessage().isBlank()
                ? "Ocorreu um erro inesperado." : ex.getMessage();
        JComponent foco = null;
        if (ex instanceof ErroValidacao) {
            foco = ((ErroValidacao) ex).getCampo();
            Componentes.marcarErro(foco);
        }
        informar("Atenção: " + texto, true);
        Dialogos.aviso(this, texto);
        if (foco != null) foco.requestFocusInWindow();
    }

    private static class IconePonto implements Icon {
        private final Color cor;

        IconePonto(Color cor) {
            this.cor = cor;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(cor);
            g2.fillOval(x, y, 10, 10);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 10;
        }

        @Override
        public int getIconHeight() {
            return 10;
        }
    }
}
