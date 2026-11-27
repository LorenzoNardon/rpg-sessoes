
package rpg.apresentacao.interfacegrafica;

import rpg.dominio.modelo.StatusPresenca;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static rpg.apresentacao.interfacegrafica.Tema.*;

/**
 * Fábrica de componentes padronizados:
 * botões, campos de texto, listas, cartões e formulários.
 */
public final class Componentes {

    // Propriedade usada para identificar se um campo está marcado com erro.
    private static final String PROP_ERRO = "campoComErro";

    // Construtor privado para impedir a criação de objetos dessa classe.
    // Seus métodos são estáticos, então podem ser utilizados diretamente.
    private Componentes() {
    }

    // ---------------------------------------------------------------------
    // Bordas
    // ---------------------------------------------------------------------

    // Cria uma borda com contorno colorido e espaçamento interno.
    // A espessura define o tamanho da linha externa.
    // Os parâmetros vertical e horizontal definem o espaço interno total.
    public static Border borda(Color cor, int espessura, int vertical, int horizontal) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(cor, espessura),
                new EmptyBorder(vertical - espessura, horizontal - espessura,
                        vertical - espessura, horizontal - espessura));
    }

    // ---------------------------------------------------------------------
    // Campos de texto
    // ---------------------------------------------------------------------

    // Campo de texto personalizado que permite exibir uma dica
    // dentro dele enquanto não houver nenhum texto digitado.
    private static class CampoTexto extends JTextField {
        private final String dica;

        CampoTexto(String texto, String dica) {
            super(texto);
            this.dica = dica;
        }

        // Personaliza a aparência do campo para desenhar a dica.
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // A dica só aparece quando existe uma dica configurada
            // e o campo está vazio.
            if (dica != null && getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();

                // Melhora a qualidade visual das letras.
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Define a cor e o estilo da dica.
                g2.setColor(new Color(0x8A869B));
                g2.setFont(getFont().deriveFont(Font.ITALIC));

                // Obtém as informações necessárias para posicionar o texto.
                FontMetrics fm = g2.getFontMetrics();
                Insets in = getInsets();

                // Calcula a posição vertical para centralizar a dica no campo.
                int y = in.top + (getHeight() - in.top - in.bottom - fm.getHeight()) / 2 + fm.getAscent();

                // Desenha a dica dentro do campo.
                g2.drawString(dica, in.left, y);

                // Libera os recursos gráficos utilizados.
                g2.dispose();
            }
        }
    }

    // Cria um campo de texto vazio com uma dica.
    public static JTextField campo(String dica) {
        return campo("", dica);
    }

    // Cria um campo de texto com um texto inicial e uma dica.
    public static JTextField campo(String texto, String dica) {
        CampoTexto campo = new CampoTexto(texto, dica);

        // Configura a quantidade de colunas e as cores do campo.
        campo.setColumns(18);
        campo.setBackground(Color.WHITE);
        campo.setForeground(COR_TEXTO);
        campo.setCaretColor(COR_PRIMARIA);
        campo.setSelectionColor(COR_SELECAO);
        campo.setSelectedTextColor(COR_TEXTO);

        // Define a borda inicial do campo.
        atualizarBordaCampo(campo);

        // Atualiza a borda quando o campo recebe ou perde o foco.
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                atualizarBordaCampo(campo);
            }

            @Override
            public void focusLost(FocusEvent e) {
                atualizarBordaCampo(campo);
            }
        });

        // Observa alterações no conteúdo do campo.
        // Quando o usuário começa a corrigir um campo com erro,
        // a indicação visual de erro é removida.
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                limparErro(campo);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                limparErro(campo);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                limparErro(campo);
            }
        });

        return campo;
    }

    // Atualiza a borda de acordo com o estado atual do campo:
    // erro, foco ou estado normal.
    private static void atualizarBordaCampo(JComponent campo) {
        if (Boolean.TRUE.equals(campo.getClientProperty(PROP_ERRO))) {
            // Campo com erro: borda vermelha.
            campo.setBorder(borda(COR_PERIGO, 2, 8, 10));
        } else if (campo.isFocusOwner()) {
            // Campo selecionado: borda destacada.
            campo.setBorder(borda(COR_FOCO, 2, 8, 10));
        } else {
            // Campo normal: borda padrão.
            campo.setBorder(borda(COR_BORDA, 1, 8, 10));
        }

        // Solicita que o componente seja redesenhado.
        campo.repaint();
    }

    /** Destaca o campo em vermelho até o usuário voltar a digitar nele. */
    public static void marcarErro(JComponent campo) {
        // Só marca componentes que sejam campos de texto.
        if (campo instanceof JTextComponent) {
            campo.putClientProperty(PROP_ERRO, Boolean.TRUE);
            atualizarBordaCampo(campo);
        }
    }

    // Remove a indicação de erro do campo, caso ela esteja ativa.
    private static void limparErro(JComponent campo) {
        if (Boolean.TRUE.equals(campo.getClientProperty(PROP_ERRO))) {
            campo.putClientProperty(PROP_ERRO, Boolean.FALSE);
            atualizarBordaCampo(campo);
        }
    }

    // Verifica se o campo contém algum texto.
    // Caso esteja vazio, lança um erro de validação associado ao campo.
    public static void exigirTexto(JTextField campo, String rotulo) {
        if (campo.getText().trim().isEmpty())
            throw new ErroValidacao("Preencha o campo \"" + rotulo + "\".", campo);
    }

    // ---------------------------------------------------------------------
    // Botões
    // ---------------------------------------------------------------------

    // Define os estilos disponíveis para os botões da aplicação.
    public enum Variante {
        PRIMARIO(COR_PRIMARIA, COR_PRIMARIA_HOVER, Color.WHITE, COR_PRIMARIA_ESCURA),
        SECUNDARIO(Color.WHITE, COR_SELECAO, COR_PRIMARIA_ESCURA, COR_PRIMARIA),
        PERIGO(COR_PERIGO, COR_PERIGO_HOVER, Color.WHITE, COR_PERIGO_HOVER);

        // Cores utilizadas em cada estado do botão.
        final Color fundo;
        final Color fundoHover;
        final Color texto;
        final Color contorno;

        Variante(Color fundo, Color fundoHover, Color texto, Color contorno) {
            this.fundo = fundo;
            this.fundoHover = fundoHover;
            this.texto = texto;
            this.contorno = contorno;
        }
    }

    // Cria um botão padronizado com texto, estilo, atalho de teclado,
    // dica de utilização e ação executada quando ele é pressionado.
    public static JButton botao(String texto, Variante variante, int mnemonico, String dica,
                                ActionListener acao) {
        JButton botao = new JButton(texto);

        // Configura a aparência inicial do botão.
        botao.setOpaque(true);
        botao.setContentAreaFilled(true);
        botao.setFocusPainted(false);
        botao.setBackground(variante.fundo);
        botao.setForeground(variante.texto);
        botao.setBorder(borda(variante.contorno, 1, 10, 16));

        // Altera o cursor para indicar que o botão pode ser clicado.
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Define o atalho de teclado e a descrição exibida ao passar o mouse.
        botao.setMnemonic(mnemonico);
        botao.setToolTipText(dica + " (Alt+" + (char) mnemonico + ")");

        // Associa a ação que será executada ao clicar no botão.
        botao.addActionListener(acao);

        // Altera a cor de fundo quando o mouse entra ou sai do botão.
        botao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (botao.isEnabled()) botao.setBackground(variante.fundoHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                botao.setBackground(variante.fundo);
            }
        });

        // Destaca a borda quando o botão recebe o foco
        // e restaura a borda original quando perde o foco.
        botao.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                botao.setBorder(borda(COR_FOCO, 3, 10, 16));
            }

            @Override
            public void focusLost(FocusEvent e) {
                botao.setBorder(borda(variante.contorno, 1, 10, 16));
            }
        });

        return botao;
    }

    // ---------------------------------------------------------------------
    // Listas e combos
    // ---------------------------------------------------------------------

    // Renderizador personalizado responsável pela aparência
    // dos itens exibidos em listas e caixas de seleção.
    private static class RenderizadorLista extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                                                      boolean selecionado, boolean comFoco) {

            // Obtém o rótulo padrão que será personalizado.
            JLabel rotulo = (JLabel) super.getListCellRendererComponent(lista, valor, indice, selecionado, comFoco);

            // Define uma borda diferente quando o item está com foco.
            Border contorno = comFoco && indice >= 0
                    ? BorderFactory.createLineBorder(COR_FOCO, 1)
                    : new EmptyBorder(1, 1, 1, 1);

            // Configura o espaçamento interno e a fonte do item.
            rotulo.setBorder(BorderFactory.createCompoundBorder(contorno, new EmptyBorder(5, 9, 5, 9)));
            rotulo.setFont(lista.getFont());

            if (selecionado) {
                // Item selecionado: utiliza a cor de seleção.
                rotulo.setBackground(COR_SELECAO);
                rotulo.setForeground(COR_TEXTO);
            } else {
                // Item não selecionado: utiliza as cores padrão.
                rotulo.setBackground(Color.WHITE);
                rotulo.setForeground(COR_TEXTO);

                // Personaliza a cor de textos relacionados à presença
                // e também de mensagens indicando ausência de itens.
                if (valor instanceof String) {
                    String texto = (String) valor;

                    if (texto.endsWith(StatusPresenca.PRESENTE.toString())) {
                        rotulo.setForeground(COR_SUCESSO);
                    } else if (texto.endsWith(StatusPresenca.AUSENTE.toString())) {
                        rotulo.setForeground(COR_PERIGO);
                    } else if (texto.startsWith("Nenhum")) {
                        rotulo.setForeground(COR_TEXTO_SUAVE);
                        rotulo.setFont(lista.getFont().deriveFont(Font.ITALIC));
                    }
                }
            }

            return rotulo;
        }
    }

    // Configura a aparência e o comportamento de uma lista.
    public static <T> void estilizarLista(JList<T> lista, String nomeAcessivel) {
        // Define o renderizador personalizado.
        lista.setCellRenderer(new RenderizadorLista());

        // Permite selecionar somente um item por vez.
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Define o fundo da lista.
        lista.setBackground(Color.WHITE);

        // Define um nome acessível para tecnologias assistivas.
        lista.getAccessibleContext().setAccessibleName(nomeAcessivel);
    }

    // Aplica o mesmo padrão visual utilizado nas listas
    // a uma caixa de seleção (JComboBox).
    public static <T> void estilizarCombo(JComboBox<T> combo) {
        combo.setRenderer(new RenderizadorLista());
        combo.setBackground(Color.WHITE);
        combo.setForeground(COR_TEXTO);
    }

    // Coloca um componente dentro de uma área com rolagem.
    public static JScrollPane rolagem(JComponent conteudo) {
        JScrollPane rolagem = new JScrollPane(conteudo);

        // Define a borda e a cor de fundo da área de rolagem.
        rolagem.setBorder(BorderFactory.createLineBorder(COR_BORDA));
        rolagem.getViewport().setBackground(Color.WHITE);

        // Quando o conteúdo for uma lista, destaca a borda
        // enquanto ela estiver com o foco.
        if (conteudo instanceof JList) {
            conteudo.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    rolagem.setBorder(BorderFactory.createLineBorder(COR_FOCO, 2));
                }

                @Override
                public void focusLost(FocusEvent e) {
                    rolagem.setBorder(BorderFactory.createLineBorder(COR_BORDA));
                }
            });
        }

        return rolagem;
    }

    // Monta um painel contendo uma lista e uma área de botões abaixo dela.
    // Os botões são recebidos como parâmetros variáveis.
    public static JPanel listaComAcoes(JList<?> lista, JButton... botoes) {
        JPanel conteudo = new JPanel(new BorderLayout(0, 10));
        conteudo.setOpaque(false);

        // Cria o painel que organizará os botões à direita.
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acoes.setOpaque(false);

        // Adiciona todos os botões recebidos ao painel de ações.
        for (JButton b : botoes) acoes.add(b);

        // Adiciona a lista com rolagem e o painel de botões.
        conteudo.add(rolagem(lista), BorderLayout.CENTER);
        conteudo.add(acoes, BorderLayout.SOUTH);

        return conteudo;
    }

    /** Enter/duplo clique edita e Delete exclui o item selecionado. Passe null para ignorar uma ação. */
    public static void atalhosLista(JList<?> lista, Runnable editar, Runnable excluir) {
        // Obtém os mapas responsáveis por associar teclas a ações.
        InputMap entradas = lista.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap acoes = lista.getActionMap();

        // Configura a tecla Enter e o duplo clique para editar.
        // Se a ação for null, a funcionalidade de edição não é configurada.
        if (editar != null) {
            entradas.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "editarItem");

            acoes.put("editarItem", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editar.run();
                }
            });

            // Também permite editar o item com um duplo clique do mouse.
            lista.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && lista.getSelectedIndex() >= 0) editar.run();
                }
            });
        }

        // Configura a tecla Delete para excluir o item selecionado.
        // Se a ação for null, a funcionalidade de exclusão não é configurada.
        if (excluir != null) {
            entradas.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "excluirItem");

            acoes.put("excluirItem", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    excluir.run();
                }
            });
        }
    }

    // ---------------------------------------------------------------------
    // Cartões e formulários
    // ---------------------------------------------------------------------

    // Cria um cartão visual com título, conteúdo e uma ação opcional no cabeçalho.
    public static JPanel cartao(String titulo, JComponent conteudo, JComponent acaoCabecalho) {
        JPanel cartao = new JPanel(new BorderLayout(0, 10));

        // Define a aparência externa do cartão.
        cartao.setBackground(Color.WHITE);
        cartao.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA), new EmptyBorder(12, 14, 14, 14)));

        // Cria e configura o título do cartão.
        JLabel rotulo = new JLabel(titulo);
        rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 16f));
        rotulo.setForeground(COR_PRIMARIA_ESCURA);

        // Cria o cabeçalho e posiciona o título à esquerda.
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);
        cabecalho.add(rotulo, BorderLayout.WEST);

        // Adiciona uma ação no lado direito do cabeçalho, se ela existir.
        if (acaoCabecalho != null) cabecalho.add(acaoCabecalho, BorderLayout.EAST);

        // Organiza o cabeçalho e o conteúdo dentro do cartão.
        cartao.add(cabecalho, BorderLayout.NORTH);
        cartao.add(conteudo, BorderLayout.CENTER);

        return cartao;
    }

    // Cria um painel de formulário com organização em grade.
    // A estrutura utiliza duas colunas: uma para os rótulos e outra para os campos.
    public static JPanel formulario() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        return form;
    }

    // Controla automaticamente o número da próxima linha do formulário.
    // A posição atual é armazenada como uma propriedade do próprio painel.
    private static int proximaLinha(JPanel form) {
        Integer atual = (Integer) form.getClientProperty("linha");

        // Se ainda não existe uma linha registrada, começa pela posição zero.
        int linha = atual == null ? 0 : atual;

        // Atualiza o contador para a próxima chamada.
        form.putClientProperty("linha", linha + 1);

        return linha;
    }

    // Adiciona uma linha ao formulário contendo um rótulo e seu campo correspondente.
    public static void linha(JPanel form, String rotulo, JComponent campo) {
        // Obtém a posição da linha que será adicionada.
        int y = proximaLinha(form);

        // Cria o rótulo e associa ele ao campo correspondente.
        JLabel texto = new JLabel(rotulo);
        texto.setForeground(COR_TEXTO_SUAVE);
        texto.setLabelFor(campo);

        // Configura o posicionamento do rótulo na primeira coluna.
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = y;
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 0, 5, 12);
        form.add(texto, c);

        // Configura o campo na segunda coluna.
        // weightx e fill permitem que ele ocupe o espaço horizontal disponível.
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 0, 5, 0);
        form.add(campo, c);
    }

    // Adiciona um botão em uma nova linha do formulário,
    // ocupando as duas colunas e ficando alinhado à direita.
    public static void botaoLinha(JPanel form, JButton botao) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = proximaLinha(form);
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(14, 0, 0, 0);
        form.add(botao, c);
    }

    /** Empurra os campos para o topo quando o formulário é mais alto que o conteúdo. */
    public static void fecharFormulario(JPanel form) {
        // Cria uma área flexível na última linha do formulário.
        // Ela absorve o espaço vertical restante e mantém os campos no topo.
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = proximaLinha(form);
        c.gridwidth = 2;
        c.weighty = 1;
        form.add(Box.createVerticalGlue(), c);
    }
}
