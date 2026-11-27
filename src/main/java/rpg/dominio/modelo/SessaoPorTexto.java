
package rpg.dominio.modelo;

import rpg.dominio.regra.RegraParticipacao;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Representa uma sessão de RPG realizada por meio de comunicação textual.
 *
 * Herda os atributos e comportamentos comuns de Sessao e adiciona
 * informações específicas sobre a plataforma utilizada e o canal
 * de comunicação da sessão.
 */
public class SessaoPorTexto extends Sessao {

    /** Plataforma utilizada para realizar a sessão por texto. */
    private final String plataforma;

    /** Canal utilizado para a comunicação entre os participantes. */
    private final String canal;

    /**
     * Cria uma nova sessão por texto com identificador gerado automaticamente.
     *
     * @param titulo título da sessão
     * @param dataHora data e horário programados
     * @param capacidadeMaxima quantidade máxima de jogadores
     * @param plataforma plataforma utilizada na sessão
     * @param canal canal de comunicação da sessão
     * @param regraParticipacao regra que controla a participação dos jogadores
     * @throws IllegalArgumentException se a plataforma ou o canal forem inválidos
     */
    public SessaoPorTexto(String titulo, LocalDateTime dataHora,
                          int capacidadeMaxima, String plataforma,
                          String canal, RegraParticipacao regraParticipacao) {

        super(titulo, dataHora, capacidadeMaxima, regraParticipacao);
        validarDadosEspecificos(plataforma, canal);
        this.plataforma = plataforma;
        this.canal = canal;
    }

    /**
     * Construtor privado utilizado para reconstruir uma sessão por texto
     * com seus dados previamente registrados.
     *
     * @param id identificador único da sessão
     * @param titulo título da sessão
     * @param dataHora data e horário programados
     * @param capacidadeMaxima quantidade máxima de jogadores
     * @param plataforma plataforma utilizada na sessão
     * @param canal canal de comunicação da sessão
     * @param regraParticipacao regra de participação
     * @param status estado atual da sessão
     * @param presencas mapa de jogadores e seus respectivos status de presença
     * @throws IllegalArgumentException se algum dado obrigatório for inválido
     */
    private SessaoPorTexto(UUID id, String titulo, LocalDateTime dataHora,
                           int capacidadeMaxima, String plataforma, String canal,
                           RegraParticipacao regraParticipacao, StatusSessao status,
                           Map<Jogador, StatusPresenca> presencas) {
        super(id, titulo, dataHora, capacidadeMaxima, regraParticipacao, status, presencas, false);
        validarDadosEspecificos(plataforma, canal);
        this.plataforma = plataforma;
        this.canal = canal;
    }

    /**
     * Reconstrói uma sessão por texto a partir de seus dados.
     *
     * @param id identificador da sessão
     * @param titulo título da sessão
     * @param dataHora data e horário programados
     * @param capacidadeMaxima quantidade máxima de jogadores
     * @param plataforma plataforma utilizada
     * @param canal canal de comunicação
     * @param regraParticipacao regra de participação
     * @param status estado atual da sessão
     * @param presencas mapa de jogadores e seus status de presença
     * @return sessão por texto reconstruída
     */
    public static SessaoPorTexto reconstituir(UUID id, String titulo, LocalDateTime dataHora,
                                              int capacidadeMaxima, String plataforma, String canal,
                                              RegraParticipacao regraParticipacao, StatusSessao status,
                                              Map<Jogador, StatusPresenca> presencas) {
        return new SessaoPorTexto(id, titulo, dataHora, capacidadeMaxima, plataforma, canal,
                regraParticipacao, status, presencas);
    }

    /**
     * Valida as informações específicas de uma sessão por texto.
     *
     * @param plataforma plataforma utilizada na sessão
     * @param canal canal de comunicação da sessão
     * @throws IllegalArgumentException se a plataforma ou o canal forem nulos ou vazios
     */
    private static void validarDadosEspecificos(String plataforma, String canal) {
        if (plataforma == null || plataforma.isBlank())
            throw new IllegalArgumentException("A plataforma é obrigatória.");
        if (canal == null || canal.isBlank())
            throw new IllegalArgumentException("O canal é obrigatório.");
    }

    /**
     * Retorna o nome da plataforma utilizada na sessão.
     *
     * @return plataforma da sessão
     */
    public String getPlataforma() {
        return plataforma;
    }

    /**
     * Retorna o canal utilizado para a comunicação entre os participantes.
     *
     * @return canal da sessão
     */
    public String getCanal() {
        return canal;
    }

    /**
     * Gera um resumo textual com as informações específicas da sessão por texto.
     *
     * @return resumo contendo título, plataforma e canal de comunicação
     */
    @Override
    public String gerarResumo() {
        return "Sessão por texto: " + getTitulo()
                + " | Plataforma: " + plataforma
                + " | Canal: " + canal;
    }

    /**
     * Retorna uma representação textual da sessão, contendo seu título,
     * data, horário e status atual.
     *
     * @return representação textual da sessão
     */
    @Override
    public String toString() {
        return getTitulo() + " - " + getDataHora().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " - " + getStatus();
    }
}
