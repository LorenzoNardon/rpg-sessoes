
package rpg.dominio.modelo;

import rpg.dominio.regra.RegraParticipacao;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Representa uma sessão de RPG realizada ao vivo por meio de uma plataforma online.
 *
 * Herda os atributos e comportamentos comuns de Sessao e adiciona
 * informações específicas sobre a plataforma utilizada e o link de acesso.
 */
public class SessaoAoVivo extends Sessao {

    /** Plataforma utilizada para realizar a sessão online. */
    private final String plataforma;

    /** Link de acesso à sala virtual da sessão. */
    private final String linkSala;

    /**
     * Cria uma nova sessão ao vivo com identificador gerado automaticamente.
     *
     * @param titulo título da sessão
     * @param dataHora data e horário programados
     * @param capacidadeMaxima quantidade máxima de jogadores
     * @param plataforma plataforma utilizada na sessão
     * @param linkSala link de acesso à sala virtual
     * @param regraParticipacao regra que controla a participação dos jogadores
     * @throws IllegalArgumentException se a plataforma ou o link forem inválidos
     */
    public SessaoAoVivo(String titulo, LocalDateTime dataHora,
                        int capacidadeMaxima, String plataforma,
                        String linkSala, RegraParticipacao regraParticipacao) {

        super(titulo, dataHora, capacidadeMaxima, regraParticipacao);
        validarDadosEspecificos(plataforma, linkSala);
        this.plataforma = plataforma;
        this.linkSala = linkSala;
    }

    /**
     * Construtor privado utilizado para reconstruir uma sessão ao vivo
     * com seus dados previamente registrados.
     *
     * @param id identificador único da sessão
     * @param titulo título da sessão
     * @param dataHora data e horário programados
     * @param capacidadeMaxima quantidade máxima de jogadores
     * @param plataforma plataforma utilizada na sessão
     * @param linkSala link de acesso à sala virtual
     * @param regraParticipacao regra de participação
     * @param status estado atual da sessão
     * @param presencas mapa de jogadores e seus respectivos status de presença
     * @throws IllegalArgumentException se algum dado obrigatório for inválido
     */
    private SessaoAoVivo(UUID id, String titulo, LocalDateTime dataHora,
                         int capacidadeMaxima, String plataforma, String linkSala,
                         RegraParticipacao regraParticipacao, StatusSessao status,
                         Map<Jogador, StatusPresenca> presencas) {
        super(id, titulo, dataHora, capacidadeMaxima, regraParticipacao, status, presencas, false);
        validarDadosEspecificos(plataforma, linkSala);
        this.plataforma = plataforma;
        this.linkSala = linkSala;
    }

    /**
     * Reconstrói uma sessão ao vivo a partir de seus dados.
     *
     * @param id identificador da sessão
     * @param titulo título da sessão
     * @param dataHora data e horário programados
     * @param capacidadeMaxima quantidade máxima de jogadores
     * @param plataforma plataforma utilizada
     * @param linkSala link de acesso à sala virtual
     * @param regraParticipacao regra de participação
     * @param status estado atual da sessão
     * @param presencas mapa de jogadores e seus status de presença
     * @return sessão ao vivo reconstruída
     */
    public static SessaoAoVivo reconstituir(UUID id, String titulo, LocalDateTime dataHora,
                                            int capacidadeMaxima, String plataforma, String linkSala,
                                            RegraParticipacao regraParticipacao, StatusSessao status,
                                            Map<Jogador, StatusPresenca> presencas) {
        return new SessaoAoVivo(id, titulo, dataHora, capacidadeMaxima, plataforma, linkSala,
                regraParticipacao, status, presencas);
    }

    /**
     * Valida as informações específicas de uma sessão ao vivo.
     *
     * @param plataforma plataforma utilizada na sessão
     * @param linkSala link de acesso à sala virtual
     * @throws IllegalArgumentException se a plataforma ou o link forem nulos ou vazios
     */
    private static void validarDadosEspecificos(String plataforma, String linkSala) {
        if (plataforma == null || plataforma.isBlank())
            throw new IllegalArgumentException("A plataforma é obrigatória.");
        if (linkSala == null || linkSala.isBlank())
            throw new IllegalArgumentException("O link da sala é obrigatório.");
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
     * Retorna o link de acesso à sala virtual.
     *
     * @return link da sala
     */
    public String getLinkSala() {
        return linkSala;
    }

    /**
     * Gera um resumo textual com as informações específicas da sessão ao vivo.
     *
     * @return resumo contendo título, plataforma e link de acesso
     */
    @Override
    public String gerarResumo() {
        return "Sessão ao vivo: " + getTitulo()
                + " | Plataforma: " + plataforma
                + " | Link: " + linkSala;
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
