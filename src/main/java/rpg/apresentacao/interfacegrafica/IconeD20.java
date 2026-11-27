
package rpg.apresentacao.interfacegrafica;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import static rpg.apresentacao.interfacegrafica.Tema.COR_FOCO;
import static rpg.apresentacao.interfacegrafica.Tema.COR_PRIMARIA_ESCURA;

/**
 * Representa um dado de 20 lados (D20), desenhado utilizando Java2D.
 * O ícone pode ser redimensionado e personalizado com diferentes cores e números.
 */
public class IconeD20 implements Icon {

    // Armazena o tamanho do ícone em pixels.
    private final int tamanho;

    // Número exibido no centro do dado.
    private final String numero;

    // Cores utilizadas na construção visual do dado.
    private final Color corpo;
    private final Color faceCentral;
    private final Color linhas;
    private final Color corNumero;

    // Construtor que permite personalizar o tamanho, o número
    // e todas as cores utilizadas no desenho do dado.
    public IconeD20(int tamanho, String numero, Color corpo, Color faceCentral, Color linhas, Color corNumero) {
        this.tamanho = tamanho;
        this.numero = numero;
        this.corpo = corpo;
        this.faceCentral = faceCentral;
        this.linhas = linhas;
        this.corNumero = corNumero;
    }

    // Cria uma versão dourada do D20 com contorno e número em índigo escuro.
    // Centraliza a configuração visual padrão do ícone.
    public static IconeD20 dourado(int tamanho, String numero) {
        return new IconeD20(tamanho, numero, COR_FOCO, new Color(0xF0B33C),
                COR_PRIMARIA_ESCURA, COR_PRIMARIA_ESCURA);
    }

    // Gera uma imagem do D20 dourado.
    // Esse método permite utilizar o desenho como imagem,
    // por exemplo, no ícone da janela principal.
    public static Image imagemDourada(int tamanho) {

        // Cria uma imagem com suporte a transparência.
        BufferedImage imagem = new BufferedImage(tamanho, tamanho, BufferedImage.TYPE_INT_ARGB);

        // Obtém o contexto gráfico responsável por desenhar na imagem.
        Graphics2D g = imagem.createGraphics();

        // Desenha um D20 com o número 20 na imagem.
        dourado(tamanho, "20").paintIcon(null, g, 0, 0);

        // Libera os recursos gráficos utilizados.
        g.dispose();

        return imagem;
    }

    // Cria um polígono a partir de um centro, um raio e uma lista de ângulos.
    // Os ângulos determinam a posição de cada vértice do polígono.
    private static Path2D poligono(double cx, double cy, double raio, double... angulos) {

        // Objeto que armazena o caminho geométrico do polígono.
        Path2D.Double caminho = new Path2D.Double();

        // Percorre os ângulos para calcular a posição de cada vértice.
        for (int i = 0; i < angulos.length; i++) {

            // Converte o ângulo de graus para radianos,
            // pois as funções trigonométricas do Java utilizam radianos.
            double a = Math.toRadians(angulos[i]);

            // Calcula as coordenadas horizontal e vertical do vértice.
            // Seno e cosseno permitem posicionar os pontos em uma circunferência.
            double px = cx + raio * Math.cos(a);
            double py = cy + raio * Math.sin(a);

            // O primeiro ponto inicia o desenho.
            // Os demais são conectados ao ponto anterior.
            if (i == 0) caminho.moveTo(px, py);
            else caminho.lineTo(px, py);
        }

        // Fecha o polígono conectando o último vértice ao primeiro.
        caminho.closePath();

        return caminho;
    }

    // Método chamado pelo Swing para desenhar o ícone na tela.
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {

        // Cria uma cópia do contexto gráfico para personalizar o desenho
        // sem modificar diretamente o contexto gráfico original.
        Graphics2D g2 = (Graphics2D) g.create();

        // Ativa o antialiasing para suavizar as bordas e os textos.
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Calcula o centro do ícone considerando sua posição na tela.
        double cx = x + tamanho / 2.0;
        double cy = y + tamanho / 2.0;

        // Define o raio externo e o raio do triângulo central.
        // O raio externo considera uma pequena margem para evitar cortes.
        double raio = tamanho / 2.0 - 1;
        double raioTriangulo = raio * 0.55;

        // Cria o contorno externo do dado utilizando seis vértices.
        Path2D contorno = poligono(cx, cy, raio, -90, -30, 30, 90, 150, 210);

        // Cria o triângulo central que representa uma das faces do D20.
        Path2D triangulo = poligono(cx, cy, raioTriangulo, -90, 30, 150);

        // Preenche o contorno externo com a cor do corpo do dado.
        g2.setColor(corpo);
        g2.fill(contorno);

        // Preenche o triângulo central com uma cor diferente.
        g2.setColor(faceCentral);
        g2.fill(triangulo);

        // Define a cor e a espessura das linhas do desenho.
        // A espessura aumenta proporcionalmente ao tamanho do ícone.
        g2.setColor(linhas);
        g2.setStroke(new BasicStroke(Math.max(1f, tamanho / 28f),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Desenha os contornos externos e do triângulo central.
        g2.draw(contorno);
        g2.draw(triangulo);

        // Cada vértice do triângulo central é conectado aos três
        // vértices mais próximos do contorno externo.
        // Essas conexões formam as divisões visuais das faces do dado.
        for (double angulo : new double[]{-90, 30, 150}) {

            // Calcula a posição do vértice do triângulo central.
            double ax = cx + raioTriangulo * Math.cos(Math.toRadians(angulo));
            double ay = cy + raioTriangulo * Math.sin(Math.toRadians(angulo));

            // Calcula as posições dos três vértices externos relacionados.
            for (double desvio : new double[]{-60, 0, 60}) {
                double bx = cx + raio * Math.cos(Math.toRadians(angulo + desvio));
                double by = cy + raio * Math.sin(Math.toRadians(angulo + desvio));

                // Desenha a linha que conecta o vértice central ao externo.
                g2.draw(new Line2D.Double(ax, ay, bx, by));
            }
        }

        // Exibe o número somente quando o ícone possui tamanho suficiente
        // e um número foi informado.
        if (tamanho >= 28 && numero != null) {

            // Define a cor e a fonte do número central.
            // O tamanho da fonte é proporcional ao tamanho do dado.
            g2.setColor(corNumero);
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.round(tamanho * 0.22f)));

            // Obtém as dimensões do texto para posicioná-lo corretamente.
            FontMetrics fm = g2.getFontMetrics();

            // Calcula a posição horizontal para centralizar o número.
            double tx = cx - fm.stringWidth(numero) / 2.0;

            // Calcula a posição vertical considerando as métricas da fonte.
            double ty = cy + (fm.getAscent() - fm.getDescent()) / 2.0 + tamanho * 0.05;

            // Desenha o número no centro do dado.
            g2.drawString(numero, (float) tx, (float) ty);
        }

        // Libera os recursos gráficos utilizados no desenho.
        g2.dispose();
    }

    // Informa ao Swing a largura do ícone.
    @Override
    public int getIconWidth() {
        return tamanho;
    }

    // Informa ao Swing a altura do ícone.
    @Override
    public int getIconHeight() {
        return tamanho;
    }
}
