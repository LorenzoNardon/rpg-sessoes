
package rpg.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Sessao {

    private final String titulo;
    private final LocalDateTime dataHora;
    private final int capacidadeMaxima;
    private final List<Jogador> jogadores;
    private StatusSessao status;

    public Sessao(String titulo, LocalDateTime dataHora, int capacidadeMaxima) {

        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("O título da sessão é obrigatório.");
        }

        if (dataHora == null || dataHora.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data da sessão deve ser futura.");
        }

        if (capacidadeMaxima <= 0) {
            throw new IllegalArgumentException("A capacidade deve ser maior que zero.");
        }

        this.titulo = titulo;
        this.dataHora = dataHora;
        this.capacidadeMaxima = capacidadeMaxima;
        this.jogadores = new ArrayList<>();
        this.status = StatusSessao.AGENDADA;
    }

    public String getTitulo() {
        return titulo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public List<Jogador> getJogadores() {
        return Collections.unmodifiableList(jogadores);
    }

    public StatusSessao getStatus() {
        return status;
    }

    public boolean possuiVagas() {
        return jogadores.size() < capacidadeMaxima;
    }

    public void adicionarJogador(Jogador jogador) {

        if (status != StatusSessao.AGENDADA) {
            throw new IllegalStateException(
                    "Não é possível adicionar jogadores nesta sessão."
            );
        }

        if (jogador == null) {
            throw new IllegalArgumentException("O jogador não pode ser nulo.");
        }

        if (jogadores.contains(jogador)) {
            throw new IllegalArgumentException(
                    "Este jogador já está inscrito na sessão."
            );
        }

        if (!possuiVagas()) {
            throw new IllegalStateException("A sessão está lotada.");
        }

        jogadores.add(jogador);
    }

    public void iniciar() {

        if (status != StatusSessao.AGENDADA) {
            throw new IllegalStateException(
                    "Somente sessões agendadas podem ser iniciadas."
            );
        }

        if (jogadores.isEmpty()) {
            throw new IllegalStateException(
                    "A sessão precisa ter pelo menos um jogador."
            );
        }

        status = StatusSessao.EM_ANDAMENTO;
    }

    public void finalizar() {

        if (status != StatusSessao.EM_ANDAMENTO) {
            throw new IllegalStateException(
                    "Somente sessões em andamento podem ser finalizadas."
            );
        }

        status = StatusSessao.FINALIZADA;
    }

    public void cancelar() {

        if (status != StatusSessao.AGENDADA) {
            throw new IllegalStateException(
                    "Somente sessões agendadas podem ser canceladas."
            );
        }

        status = StatusSessao.CANCELADA;
    }

    public abstract String gerarResumo();
}
