
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

// Janela principal da aplicação, responsável por organizar as abas,
// exibir informações de status e coordenar as operações da interface.
public class TelaPrincipal extends JFrame implements ContextoTela {

    // Aplica o tema visual antes da criação das instâncias da janela.
    static {
        Tema.aplicar();
    }

    // Armazena as abas da aplicação para facilitar sua atualização e organização.
    private final AbaBase[] secoes;

    // Componente que permite alternar entre as diferentes abas.
    private final JTabbedPane abas = new JTabbedPane();

    // Exibe mensagens de status, como sucesso, erro ou processamento.
    private final JLabel textoStatus = new JLabel("Pronto.");

    // Barra de progresso exibida durante operações demoradas.
    private final JProgressBar barraProgresso = new JProgressBar();

    // Painel transparente utilizado para bloquear interações durante operações.
    private final JPanel painelOcupado = new JPanel();

    // Temporizador que restaura a mensagem padrão após alguns segundos.
    private final Timer temporizadorStatus = new Timer(7000, e -> definirStatusPadrao());

    // Indica se existe alguma operação sendo executada em segundo plano.
    private boolean ocupado;

    // Construtor padrão que utiliza a implementação padrão de seleção de convidados.
    public TelaPrincipal(GerenciadorRpg gerenciador) {
        this(gerenciador, new DialogoSelecaoConvidados());
    }

    // Construtor que permite receber uma implementação personalizada
    // para selecionar os convidados das sessões.
    public TelaPrincipal(GerenciadorRpg gerenciador, SeletorConvidados seletorConvidados) {

        // Verifica se as dependências obrigatórias foram fornecidas.
        Objects.requireNonNull(gerenciador);
        Objects.requireNonNull(seletorConvidados);

        // Configura as propriedades básicas da janela.
        setTitle("Gerenciador de Sessões RPG");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 740);
        setMinimumSize(new Dimension(960, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COR_FUNDO);

        // Define os ícones da janela em dois tamanhos diferentes.
        setIconImages(java.util.List.of(IconeD20.imagemDourada(32), IconeD20.imagemDourada(64)));

        // Configura o bloqueio da interface e o comportamento do temporizador.
        configurarBloqueioOcupado();
        temporizadorStatus.setRepeats(false);

        // Cria as abas e fornece a elas o gerenciador e o contexto da tela.
        secoes = new AbaBase[]{
                new AbaJogadores(gerenciador, this),
                new AbaCampanhas(gerenciador, this),
                new AbaSessoes(gerenciador, this, seletorConvidados)
        };

        // Configura a aparência e adiciona as abas à janela.
        abas.setFont(abas.getFont().deriveFont(Font.BOLD));
        abas.addTab("Jogadores", secoes[0]);
        abas.addTab("Campanhas", secoes[1]);
        abas.addTab("Sessões", secoes[2]);

        // Atualiza o botão padrão da janela conforme a aba selecionada.
        abas.addChangeListener(e ->
                getRootPane().setDefaultButton(secoes[abas.getSelectedIndex()].getBotaoPadrao()));

        // Adiciona o cabeçalho, as abas e a barra de status à janela.
        add(criarCabecalho(), BorderLayout.NORTH);
        add(abas, BorderLayout.CENTER);
        add(criarBarraStatus(), BorderLayout.SOUTH);

        // Define o botão padrão inicial e atualiza os dados das abas.
        getRootPane().setDefaultButton(secoes[0].getBotaoPadrao());
        atualizarListas();
    }

    // ---------------------------------------------------------------------
    // Implementação da interface ContextoTela
    // ---------------------------------------------------------------------

    // Retorna a própria janela para que outras classes possam utilizá-la
    // como referência para exibir diálogos.
    @Override
    public JFrame janela() {
        return this;
    }

    // Executa uma operação na thread da interface gráfica.
    // Caso ocorra um erro, ele é encaminhado para o tratamento centralizado.
    @Override
    public void executar(Runnable operacao) {
        try {
            operacao.run();
        } catch (RuntimeException ex) {
            tratarErro(ex);
        }
    }

    // Executa uma tarefa em segundo plano para evitar que a interface congele.
    // Também define o comportamento após o sucesso ou a falha da operação.
    @Override
    public void emSegundoPlano(Runnable tarefa, Runnable aoSucesso, String mensagemSucesso) {

        // Impede que outra operação seja iniciada enquanto a atual estiver em andamento.
        if (ocupado) return;

        // Informa à interface que uma operação começou.
        definirOcupado(true);

        // SwingWorker permite executar tarefas demoradas fora da thread da interface.
        new SwingWorker<Void, Void>() {

            // Executado em segundo plano, sem bloquear a janela.
            @Override
            protected Void doInBackground() {
                tarefa.run();
                return null;
            }

            // Executado quando a tarefa termina, na thread da interface gráfica.
            @Override
            protected void done() {

                // Libera a interface para novas interações.
                definirOcupado(false);

                // Armazena uma possível falha durante a execução.
                Throwable falha = null;

                try {
                    // Verifica se a tarefa terminou corretamente.
                    get();

                } catch (ExecutionException ex) {

                    // Recupera a causa original do erro, quando disponível.
                    falha = ex.getCause() != null ? ex.getCause() : ex;

                } catch (InterruptedException ex) {

                    // Restaura o estado de interrupção da thread.
                    Thread.currentThread().interrupt();
                    falha = ex;
                }

                // Executa a ação de sucesso, caso não tenha ocorrido erro.
                if (falha == null && aoSucesso != null) aoSucesso.run();

                // Atualiza os dados exibidos em todas as abas.
                atualizarListas();

                // Exibe uma mensagem de sucesso ou encaminha o erro para tratamento.
                if (falha == null) informar(mensagemSucesso, false);
                else tratarErro(falha);
            }
        }.execute();
    }

    // Solicita que todas as abas atualizem suas informações.
    private void atualizarListas() {
        for (AbaBase aba : secoes) aba.atualizar();
    }

    // ---------------------------------------------------------------------
    // Criação do cabeçalho e da barra de status
    // ---------------------------------------------------------------------

    // Monta o cabeçalho com o título, o subtítulo e os dados ilustrativos.
    private JPanel criarCabecalho() {

        // Cria o painel principal do cabeçalho.
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(COR_PRIMARIA_ESCURA);

        // Define uma borda inferior colorida e margens internas.
        cabecalho.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, COR_FOCO),
                new EmptyBorder(14, 20, 14, 20)));

        // Configura o título principal.
        JLabel titulo = new JLabel("Gerenciador de Sessões RPG");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        titulo.setForeground(Color.WHITE);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);

        // Configura o texto complementar abaixo do título.
        JLabel subtitulo = new JLabel("Jogadores, campanhas e sessões em um só lugar");
        subtitulo.setForeground(new Color(0xD8D4F2));
        subtitulo.setHorizontalAlignment(SwingConstants.CENTER);

        // Agrupa o título e o subtítulo em um painel vertical.
        JPanel textos = new JPanel(new GridLayout(0, 1, 0, 2));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(subtitulo);

        // Cria dois dados d20 do mesmo tamanho, um em cada lado do cabeçalho.
        JLabel dadoEsquerdo = new JLabel(IconeD20.dourado(52, "20"));
        JLabel dadoDireito = new JLabel(IconeD20.dourado(52, "20"));

        // Define nomes acessíveis para os ícones, auxiliando tecnologias assistivas.
        dadoEsquerdo.getAccessibleContext().setAccessibleName("Dado de 20 lados");
        dadoDireito.getAccessibleContext().setAccessibleName("Dado de 20 lados");

        // Posiciona os dados e os textos no cabeçalho.
        cabecalho.add(dadoEsquerdo, BorderLayout.WEST);
        cabecalho.add(textos, BorderLayout.CENTER);
        cabecalho.add(dadoDireito, BorderLayout.EAST);

        return cabecalho;
    }

    // Monta a barra inferior responsável por exibir mensagens e progresso.
    private JPanel criarBarraStatus() {

        // Cria o painel com espaçamento horizontal entre os componentes.
        JPanel barra = new JPanel(new BorderLayout(12, 0));
        barra.setBackground(Color.WHITE);

        // Define uma borda superior e margens internas.
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, COR_BORDA),
                new EmptyBorder(7, 16, 7, 16)));

        // Ajusta o espaço entre o texto e seu ícone.
        textoStatus.setIconTextGap(8);

        // Define um nome acessível para a mensagem de status.
        textoStatus.getAccessibleContext().setAccessibleName("Mensagem de status");

        // Configura a barra de progresso para funcionar de forma indeterminada.
        barraProgresso.setIndeterminate(true);
        barraProgresso.setVisible(false);
        barraProgresso.setPreferredSize(new Dimension(140, 14));
        barraProgresso.setForeground(COR_PRIMARIA);

        // Posiciona os componentes na barra inferior.
        barra.add(textoStatus, BorderLayout.CENTER);
        barra.add(barraProgresso, BorderLayout.EAST);

        // Define a mensagem inicial.
        definirStatusPadrao();

        return barra;
    }

    // Restaura a mensagem padrão de status da aplicação.
    private void definirStatusPadrao() {
        textoStatus.setText("Pronto.");
        textoStatus.setForeground(COR_TEXTO_SUAVE);
        textoStatus.setIcon(new IconePonto(COR_TEXTO_SUAVE));
    }

    // Exibe uma mensagem de sucesso ou erro na barra de status.
    private void informar(String texto, boolean erro) {

        // Escolhe a cor de acordo com o tipo de mensagem.
        Color cor = erro ? COR_PERIGO : COR_SUCESSO;

        textoStatus.setText(texto);
        textoStatus.setForeground(cor);
        textoStatus.setIcon(new IconePonto(cor));

        // Reinicia o temporizador para restaurar a mensagem padrão depois de sete segundos.
        temporizadorStatus.restart();
    }

    // Configura o painel transparente que bloqueia interações durante operações.
    private void configurarBloqueioOcupado() {
        painelOcupado.setOpaque(false);
        painelOcupado.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // Captura eventos do mouse para impedir interações com os componentes abaixo.
        painelOcupado.addMouseListener(new MouseAdapter() { });
        painelOcupado.addMouseMotionListener(new MouseMotionAdapter() { });

        // Define o painel como camada de bloqueio da janela.
        setGlassPane(painelOcupado);
    }

    // Atualiza o estado visual da janela conforme uma operação começa ou termina.
    private void definirOcupado(boolean valor) {

        ocupado = valor;

        // Mostra ou esconde o painel de bloqueio e a barra de progresso.
        painelOcupado.setVisible(valor);
        barraProgresso.setVisible(valor);

        if (valor) {

            // Interrompe o temporizador para evitar que a mensagem de processamento desapareça.
            temporizadorStatus.stop();

            // Informa que existe uma operação em andamento.
            textoStatus.setText("Processando...");
            textoStatus.setForeground(COR_TEXTO_SUAVE);
            textoStatus.setIcon(new IconePonto(COR_PRIMARIA));
        }
    }

    // Trata erros ocorridos durante as operações da aplicação.
    private void tratarErro(Throwable ex) {

        // Obtém a mensagem do erro ou utiliza um texto padrão.
        String texto = ex.getMessage() == null || ex.getMessage().isBlank()
                ? "Ocorreu um erro inesperado." : ex.getMessage();

        // Guarda o componente que deverá receber o foco, caso seja um erro de validação.
        JComponent foco = null;

        if (ex instanceof ErroValidacao) {

            // Recupera o campo relacionado ao erro e destaca-o visualmente.
            foco = ((ErroValidacao) ex).getCampo();
            Componentes.marcarErro(foco);
        }

        // Exibe o aviso na barra de status e em uma janela de diálogo.
        informar("Atenção: " + texto, true);
        Dialogos.aviso(this, texto);

        // Devolve o foco ao campo que precisa ser corrigido, se houver.
        if (foco != null) foco.requestFocusInWindow();
    }

    // Ícone pequeno e colorido utilizado para indicar o estado das mensagens.
    private static class IconePonto implements Icon {

        // Cor utilizada para desenhar o ponto.
        private final Color cor;

        IconePonto(Color cor) {
            this.cor = cor;
        }

        // Desenha um círculo preenchido na posição informada.
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {

            // Cria uma cópia do contexto gráfico para não alterar o original.
            Graphics2D g2 = (Graphics2D) g.create();

            // Ativa o antialiasing para deixar o círculo com bordas suaves.
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Define a cor e desenha o círculo.
            g2.setColor(cor);
            g2.fillOval(x, y, 10, 10);

            // Libera os recursos gráficos utilizados.
            g2.dispose();
        }

        // Informa a largura do ícone.
        @Override
        public int getIconWidth() {
            return 10;
        }

        // Informa a altura do ícone.
        @Override
        public int getIconHeight() {
            return 10;
        }
    }
}
