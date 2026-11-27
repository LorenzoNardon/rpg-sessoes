
package rpg.dominio.modelo;

import rpg.dominio.excecao.JogadorJaInscritoException;
import rpg.dominio.excecao.RegraParticipacaoException;
import rpg.dominio.excecao.SessaoLotadaException;
import rpg.dominio.excecao.TransicaoStatusInvalidaException;
import rpg.dominio.regra.RegraParticipacao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa uma sessão de RPG, definindo suas informações,
 * participantes, presenças e regras de participação.
 *
 * É uma classe abstrata que estabelece o comportamento comum
 * para os diferentes tipos de sessão.
 *
 * Também controla o ciclo de vida da sessão, permitindo iniciar,
 * finalizar ou cancelar sua execução.
 */
public abstract class Sessao {

    /** Identificador único da sessão. */
    private final UUID id;

    /** Título da sessão. */
    private final String titulo;

    /** Data e horário programados para a sessão. */
    private final LocalDateTime dataHora;

    /** Quantidade máxima de jogadores permitidos na sessão. */
    private final int capacidadeMaxima;

    /** Lista de jogadores inscritos na sessão. */
    private final List<Jogador> jogadores;

    /** Mapeia cada jogador ao seu respectivo status de presença. */
    private final Map<Jogador, StatusPresenca> presencas;

    /** Regra responsável por determinar quais jogadores podem participar. */
    private final RegraParticipacao regraParticipacao;

    /** Estado atual da sessão. */
    private StatusSessao status;

    /**
     * Cria uma nova sessão com identificador gerado automaticamente
     * e status inicial AGENDADA.
     *
     * @param titulo título da sessão
     * @param dataHora data e horário programados
     * @param capacidadeMaxima quantidade máxima de jogadores
     * @param regraParticipacao regra que controla a participação dos jogadores
     */
    public Sessao(String titulo, LocalDateTime dataHora, int capacidadeMaxima,
                  RegraParticipacao regraParticipacao) {
        this(UUID.randomUUID(), titulo, dataHora, capacidadeMaxima, regraParticipacao,
                StatusSessao.AGENDADA, Map.of(), true);
    }

    /**
     * Construtor protegido que inicializa uma sessão com todos os seus dados.
     *
     * Permite que subclasses reconstruam sessões existentes, inclusive
     * com participantes e status previamente registrados.
     *
     * @param id identificador único da sessão
     * @param titulo título da sessão
     * @param dataHora data e horário programados
     * @param capacidadeMaxima quantidade máxima de jogadores
     * @param regraParticipacao regra de participação
     * @param status estado atual da sessão
     * @param presencas mapa de jogadores e seus respectivos status de presença
     * @param validarDataFutura indica se a data deve ser obrigatoriamente futura
     * @throws IllegalArgumentException se algum dado obrigatório for inválido
     */
    protected Sessao(UUID id, String titulo, LocalDateTime dataHora, int capacidadeMaxima,
                     RegraParticipacao regraParticipacao, StatusSessao status,
                     Map<Jogador, StatusPresenca> presencas, boolean validarDataFutura) {
        if (id == null)
            throw new IllegalArgumentException("O identificador da sessão é obrigatório.");
        if (titulo == null || titulo.isBlank())
            throw new IllegalArgumentException("O título da sessão é obrigatório.");
        if (dataHora == null || (validarDataFutura && !dataHora.isAfter(LocalDateTime.now())))
            throw new IllegalArgumentException("A data da sessão deve ser futura.");
        if (capacidadeMaxima <= 0)
            throw new IllegalArgumentException("A capacidade deve ser maior que zero.");
        if (regraParticipacao == null)
            throw new IllegalArgumentException("A regra de participação é obrigatória.");
        if (status == null)
            throw new IllegalArgumentException("O status da sessão é obrigatório.");
        if (presencas == null)
            throw new IllegalArgumentException("As presenças da sessão são obrigatórias.");

        this.id = id;
        this.titulo = titulo;
        this.dataHora = dataHora;
        this.capacidadeMaxima = capacidadeMaxima;
        this.regraParticipacao = regraParticipacao;
        this.jogadores = new ArrayList<>(presencas.keySet());
        this.presencas = new LinkedHashMap<>(presencas);
        this.status = status;
    }

    /**
     * Retorna o identificador único da sessão.
     *
     * @return identificador da sessão
     */
    public UUID getId() { return id; }

    /**
     * Retorna o título da sessão.
     *
     * @return título da sessão
     */
    public String getTitulo() { return titulo; }

    /**
     * Retorna a data e o horário programados para a sessão.
     *
     * @return data e horário da sessão
     */
    public LocalDateTime getDataHora() { return dataHora; }

    /**
     * Retorna a capacidade máxima de jogadores.
     *
     * @return quantidade máxima de jogadores
     */
    public int getCapacidadeMaxima() { return capacidadeMaxima; }

    /**
     * Retorna os jogadores inscritos em modo somente leitura.
     *
     * @return lista não modificável de jogadores
     */
    public List<Jogador> getJogadores() {
        return Collections.unmodifiableList(jogadores);
    }

    /**
     * Retorna o mapa de presenças em modo somente leitura.
     *
     * @return mapa não modificável de jogadores e seus status de presença
     */
    public Map<Jogador, StatusPresenca> getPresencas() {
        return Collections.unmodifiableMap(presencas);
    }

    /**
     * Retorna o status atual da sessão.
     *
     * @return status da sessão
     */
    public StatusSessao getStatus() { return status; }

    /**
     * Retorna a regra de participação utilizada pela sessão.
     *
     * @return regra de participação
     */
    public RegraParticipacao getRegraParticipacao() { return regraParticipacao; }

    /**
     * Verifica se ainda existem vagas disponíveis na sessão.
     *
     * @return true se houver vagas disponíveis, false caso contrário
     */
    public boolean possuiVagas() {
        return jogadores.size() < capacidadeMaxima;
    }

    /**
     * Inscreve um jogador na sessão, verificando seu status,
     * sua elegibilidade e a disponibilidade de vagas.
     *
     * Após a inscrição, o jogador recebe o status PENDENTE de presença.
     *
     * @param jogador jogador que será inscrito
     * @throws TransicaoStatusInvalidaException se a sessão não estiver agendada
     * @throws IllegalArgumentException se o jogador for nulo
     * @throws JogadorJaInscritoException se o jogador já estiver inscrito
     * @throws RegraParticipacaoException se o jogador não puder participar
     * @throws SessaoLotadaException se a capacidade máxima tiver sido atingida
     */
    public void adicionarJogador(Jogador jogador) {
        if (status != StatusSessao.AGENDADA)
            throw new TransicaoStatusInvalidaException("Só é possível inscrever jogadores em sessões agendadas.");
        if (jogador == null)
            throw new IllegalArgumentException("O jogador não pode ser nulo.");
        if (presencas.containsKey(jogador))
            throw new JogadorJaInscritoException("Este jogador já está inscrito na sessão.");
        if (!regraParticipacao.permiteEntrada(jogador))
            throw new RegraParticipacaoException("O jogador não foi convidado para esta sessão.");
        if (!possuiVagas())
            throw new SessaoLotadaException("A sessão atingiu sua capacidade máxima.");

        jogadores.add(jogador);
        presencas.put(jogador, StatusPresenca.PENDENTE);
    }

    /**
     * Registra ou atualiza a presença de um jogador inscrito.
     *
     * O registro somente pode ser realizado enquanto a sessão
     * estiver em andamento.
     *
     * @param jogador jogador cuja presença será registrada
     * @param presenca novo status de presença
     * @throws TransicaoStatusInvalidaException se a sessão não estiver em andamento
     * @throws IllegalArgumentException se o jogador não estiver inscrito
     * ou se o status informado for inválido
     */
    public void registrarPresenca(Jogador jogador, StatusPresenca presenca) {
        if (status != StatusSessao.EM_ANDAMENTO)
            throw new TransicaoStatusInvalidaException("A presença só pode ser registrada durante a sessão.");
        if (jogador == null || !presencas.containsKey(jogador))
            throw new IllegalArgumentException("O jogador não está inscrito nesta sessão.");
        if (presenca == null || presenca == StatusPresenca.PENDENTE)
            throw new IllegalArgumentException("Informe PRESENTE ou AUSENTE.");

        presencas.put(jogador, presenca);
    }

    /**
     * Retorna a quantidade de jogadores registrados como presentes.
     *
     * @return quantidade de jogadores presentes
     */
    public int quantidadePresentes() {
        return (int) presencas.values().stream().filter(p -> p == StatusPresenca.PRESENTE).count();
    }

    /**
     * Retorna a quantidade de jogadores registrados como ausentes.
     *
     * @return quantidade de jogadores ausentes
     */
    public int quantidadeAusentes() {
        return (int) presencas.values().stream().filter(p -> p == StatusPresenca.AUSENTE).count();
    }

    /**
     * Retorna a quantidade de jogadores cuja presença ainda não foi registrada.
     *
     * @return quantidade de jogadores com presença pendente
     */
    public int quantidadePendentes() {
        return (int) presencas.values().stream().filter(p -> p == StatusPresenca.PENDENTE).count();
    }

    /**
     * Inicia a sessão, alterando seu status para EM_ANDAMENTO.
     *
     * A sessão precisa estar agendada e possuir pelo menos um jogador inscrito.
     *
     * @throws TransicaoStatusInvalidaException se a sessão não estiver agendada
     * @throws IllegalStateException se não houver jogadores inscritos
     */
    public void iniciar() {
        if (status != StatusSessao.AGENDADA)
            throw new TransicaoStatusInvalidaException("Somente sessões agendadas podem ser iniciadas.");
        if (jogadores.isEmpty())
            throw new IllegalStateException("A sessão precisa ter pelo menos um jogador.");
        status = StatusSessao.EM_ANDAMENTO;
    }

    /**
     * Finaliza a sessão, alterando seu status para FINALIZADA.
     *
     * Somente sessões em andamento podem ser finalizadas.
     *
     * @throws TransicaoStatusInvalidaException se a sessão não estiver em andamento
     */
    public void finalizar() {
        if (status != StatusSessao.EM_ANDAMENTO)
            throw new TransicaoStatusInvalidaException("Somente sessões em andamento podem ser finalizadas.");
        status = StatusSessao.FINALIZADA;
    }

    /**
     * Cancela a sessão, alterando seu status para CANCELADA.
     *
     * Somente sessões agendadas podem ser canceladas.
     *
     * @throws TransicaoStatusInvalidaException se a sessão não estiver agendada
     */
    public void cancelar() {
        if (status != StatusSessao.AGENDADA)
            throw new TransicaoStatusInvalidaException("Somente sessões agendadas podem ser canceladas.");
        status = StatusSessao.CANCELADA;
    }

    /**
     * Gera um resumo textual com as informações específicas do tipo de sessão.
     *
     * Deve ser implementado pelas subclasses.
     *
     * @return resumo textual da sessão
     */
    public abstract String gerarResumo();

    /**
     * Retorna uma representação textual da sessão, contendo seu título,
     * data, horário e status atual.
     *
     * @return representação textual da sessão
     */
    @Override
    public String toString() {
        return titulo + " - " + dataHora.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " - " + status;
    }

    /**
     * Verifica se duas sessões possuem o mesmo identificador.
     *
     * @param objeto objeto que será comparado
     * @return true se representarem a mesma sessão, false caso contrário
     */
    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof Sessao sessao)) return false;
        return id.equals(sessao.id);
    }

    /**
     * Retorna o código hash da sessão, calculado a partir do identificador.
     *
     * @return código hash da sessão
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
