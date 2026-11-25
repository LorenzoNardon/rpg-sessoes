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

public abstract class Sessao {
    private final String titulo;
    private final LocalDateTime dataHora;
    private final int capacidadeMaxima;
    private final List<Jogador> jogadores;
    private final Map<Jogador, StatusPresenca> presencas;
    private final RegraParticipacao regraParticipacao;
    private StatusSessao status;

    public Sessao(String titulo, LocalDateTime dataHora, int capacidadeMaxima,
                  RegraParticipacao regraParticipacao) {
        if (titulo == null || titulo.isBlank())
            throw new IllegalArgumentException("O título da sessão é obrigatório.");
        if (dataHora == null || !dataHora.isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("A data da sessão deve ser futura.");
        if (capacidadeMaxima <= 0)
            throw new IllegalArgumentException("A capacidade deve ser maior que zero.");
        if (regraParticipacao == null)
            throw new IllegalArgumentException("A regra de participação é obrigatória.");

        this.titulo = titulo;
        this.dataHora = dataHora;
        this.capacidadeMaxima = capacidadeMaxima;
        this.regraParticipacao = regraParticipacao;
        this.jogadores = new ArrayList<>();
        this.presencas = new LinkedHashMap<>();
        this.status = StatusSessao.AGENDADA;
    }

    public String getTitulo() { return titulo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public int getCapacidadeMaxima() { return capacidadeMaxima; }
    public List<Jogador> getJogadores() { return Collections.unmodifiableList(jogadores); }
    public Map<Jogador, StatusPresenca> getPresencas() { return Collections.unmodifiableMap(presencas); }
    public StatusSessao getStatus() { return status; }

    public boolean possuiVagas() {
        return jogadores.size() < capacidadeMaxima;
    }

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

    public void registrarPresenca(Jogador jogador, StatusPresenca presenca) {
        if (status != StatusSessao.EM_ANDAMENTO)
            throw new TransicaoStatusInvalidaException("A presença só pode ser registrada durante a sessão.");
        if (jogador == null || !presencas.containsKey(jogador))
            throw new IllegalArgumentException("O jogador não está inscrito nesta sessão.");
        if (presenca == null || presenca == StatusPresenca.PENDENTE)
            throw new IllegalArgumentException("Informe PRESENTE ou AUSENTE.");

        presencas.put(jogador, presenca);
    }

    public int quantidadePresentes() {
        return (int) presencas.values().stream().filter(p -> p == StatusPresenca.PRESENTE).count();
    }

    public int quantidadeAusentes() {
        return (int) presencas.values().stream().filter(p -> p == StatusPresenca.AUSENTE).count();
    }

    public int quantidadePendentes() {
        return (int) presencas.values().stream().filter(p -> p == StatusPresenca.PENDENTE).count();
    }

    public void iniciar() {
        if (status != StatusSessao.AGENDADA)
            throw new TransicaoStatusInvalidaException("Somente sessões agendadas podem ser iniciadas.");
        if (jogadores.isEmpty())
            throw new IllegalStateException("A sessão precisa ter pelo menos um jogador.");
        status = StatusSessao.EM_ANDAMENTO;
    }

    public void finalizar() {
        if (status != StatusSessao.EM_ANDAMENTO)
            throw new TransicaoStatusInvalidaException("Somente sessões em andamento podem ser finalizadas.");
        status = StatusSessao.FINALIZADA;
    }

    public void cancelar() {
        if (status != StatusSessao.AGENDADA)
            throw new TransicaoStatusInvalidaException("Somente sessões agendadas podem ser canceladas.");
        status = StatusSessao.CANCELADA;
    }

    public abstract String gerarResumo();

    @Override
    public String toString() {
        return titulo + " - " + dataHora.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " - " + status;
    }
}
