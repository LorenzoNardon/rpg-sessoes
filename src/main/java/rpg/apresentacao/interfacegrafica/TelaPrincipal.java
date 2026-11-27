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
 * Janela principal da aplicação, responsável por organizar as abas,
 * exibir informações de status e coordenar as operações da interface.
 */
public class TelaPrincipal extends JFrame implements ContextoTela {

    static {
        Tema.aplicar();
    }

    /** Abas da aplicação, mantidas para facilitar a atualização e a organização. */
    private final AbaBase[] secoes;

    /** Componente que permite alternar entre as abas. */
    private final JTabbedPane abas = new JTabbedPane();

    /** Mensagem de status: sucesso, erro ou processamento. */
    private final JLabel textoStatus = new JLabel("Pronto.");

    /** Barra de progresso exibida durante operações demoradas. */
    private final JProgressBar barraProgresso = new JProgressBar();

    /** Painel transparente que bloqueia as interações durante operações em andamento. */
    private final JPanel painelOcupado = new JPanel();

    /** Temporizador que restaura a mensagem padrão de status após alguns segundos. */
    private final Timer temporizadorStatus = new Timer(7000, e -> definirStatusPadrao());

    /** Indica se há uma operação sendo executada em segundo plano. */
    private boolean ocupado;

    /**
     * Cria a janela utilizando a implementação padrão de seleção de convidados.
     *
     * @param gerenciador serviço utilizado pelas abas
     */
    public TelaPrincipal(GerenciadorRpg gerenciador) {
        this(gerenciador, new DialogoSelecaoConvidados());
    }

    /**
     * Cria a janela com uma implementação personalizada para selecionar
     * os convidados das sessões.
     *
     * @param gerenciador       serviço utilizado pelas abas
     * @param seletorConvidados seletor de convidados das sessões
     */
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

    /**
     * Retorna a própria janela, usada por outras classes como referência para diálogos.
     *
     * @return esta janela
     */
    @Override
    public JFrame janela() {
        return this;
    }

    /**
     * Executa uma operação na thread da interface gráfica;
     * erros são encaminhados ao tratamento centralizado.
     *
     * @param operacao operação a ser executada
     */
    @Override
    public void executar(Runnable operacao) {
        try {
            operacao.run();
        } catch (RuntimeException ex) {
            tratarErro(ex);
        }
    }

    /**
     * Executa uma tarefa em segundo plano para que a interface não congele,
     * tratando o sucesso e a falha ao final. Ignora a chamada se já houver
     * outra operação em andamento.
     *
     * @param tarefa          tarefa executada em segundo plano
     * @param aoSucesso       ação executada na interface após o sucesso (pode ser {@code null})
     * @param mensagemSucesso mensagem exibida na barra de status ao concluir
     */
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

    /**
     * Solicita que todas as abas atualizem suas informações.
     */
    private void atualizarListas() {
        for (AbaBase aba : secoes) aba.atualizar();
    }

    /**
     * Monta o cabeçalho com título, subtítulo e um dado D20 de cada lado.
     *
     * @return painel do cabeçalho
     */
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

        JLabel dadoEsquerdo = new JLabel(IconeD20.dourado(52, "20"));
        JLabel dadoDireito = new JLabel(IconeD20.dourado(52, "20"));

        dadoEsquerdo.getAccessibleContext().setAccessibleName("Dado de 20 lados");
        dadoDireito.getAccessibleContext().setAccessibleName("Dado de 20 lados");

        cabecalho.add(dadoEsquerdo, BorderLayout.WEST);
        cabecalho.add(textos, BorderLayout.CENTER);
        cabecalho.add(dadoDireito, BorderLayout.EAST);

        return cabecalho;
    }

    /**
     * Monta a barra inferior que exibe mensagens de status e o progresso das operações.
     *
     * @return painel da barra de status
     */
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

    /**
     * Restaura a mensagem padrão de status da aplicação.
     */
    private void definirStatusPadrao() {
        textoStatus.setText("Pronto.");
        textoStatus.setForeground(COR_TEXTO_SUAVE);
        textoStatus.setIcon(new IconePonto(COR_TEXTO_SUAVE));
    }

    /**
     * Exibe uma mensagem de sucesso ou erro na barra de status e reinicia o temporizador
     * que a substitui pela mensagem padrão após sete segundos.
     *
     * @param texto mensagem exibida
     * @param erro  {@code true} para exibir como erro, {@code false} como sucesso
     */
    private void informar(String texto, boolean erro) {

        Color cor = erro ? COR_PERIGO : COR_SUCESSO;

        textoStatus.setText(texto);
        textoStatus.setForeground(cor);
        textoStatus.setIcon(new IconePonto(cor));

        temporizadorStatus.restart();
    }

    /**
     * Configura o painel transparente (glass pane) que bloqueia as interações
     * com os componentes abaixo durante operações em andamento.
     */
    private void configurarBloqueioOcupado() {
        painelOcupado.setOpaque(false);
        painelOcupado.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        painelOcupado.addMouseListener(new MouseAdapter() { });
        painelOcupado.addMouseMotionListener(new MouseMotionAdapter() { });

        setGlassPane(painelOcupado);
    }

    /**
     * Atualiza o estado visual da janela conforme uma operação começa ou termina.
     *
     * @param valor {@code true} quando uma operação começa, {@code false} quando termina
     */
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

    /**
     * Trata erros das operações: exibe a mensagem na barra de status e em um diálogo
     * e, em erros de validação, destaca o campo e devolve o foco a ele.
     *
     * @param ex erro ocorrido
     */
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

    /**
     * Ícone pequeno em forma de ponto colorido, usado para indicar o estado das mensagens.
     */
    private static class IconePonto implements Icon {

        /** Cor usada para desenhar o ponto. */
        private final Color cor;

        /**
         * Cria o ponto com a cor informada.
         *
         * @param cor cor do ponto
         */
        IconePonto(Color cor) {
            this.cor = cor;
        }

        /**
         * Desenha um círculo preenchido na posição informada.
         *
         * @param c componente de referência
         * @param g contexto gráfico
         * @param x posição horizontal
         * @param y posição vertical
         */
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(cor);
            g2.fillOval(x, y, 10, 10);

            g2.dispose();
        }

        /**
         * Informa a largura do ícone.
         *
         * @return largura em pixels
         */
        @Override
        public int getIconWidth() {
            return 10;
        }

        /**
         * Informa a altura do ícone.
         *
         * @return altura em pixels
         */
        @Override
        public int getIconHeight() {
            return 10;
        }
    }
}
