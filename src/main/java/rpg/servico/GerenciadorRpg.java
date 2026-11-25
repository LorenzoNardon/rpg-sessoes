package rpg.servico;

import rpg.aplicacao.repositorio.CampanhaRepositorio;
import rpg.aplicacao.repositorio.JogadorRepositorio;
import rpg.aplicacao.repositorio.SessaoRepositorio;
import rpg.dominio.modelo.Campanha;
import rpg.dominio.modelo.Jogador;
import rpg.dominio.modelo.Sessao;
import rpg.dominio.modelo.StatusPresenca;
import rpg.dominio.modelo.StatusSessao;

import java.util.List;

public class GerenciadorRpg {
    private final JogadorRepositorio jogadorRepositorio;
    private final CampanhaRepositorio campanhaRepositorio;
    private final SessaoRepositorio sessaoRepositorio;
    private List<Jogador> jogadores = List.of();
    private List<Campanha> campanhas = List.of();

    public GerenciadorRpg(JogadorRepositorio jogadorRepositorio,
                          CampanhaRepositorio campanhaRepositorio,
                          SessaoRepositorio sessaoRepositorio) {
        this.jogadorRepositorio = jogadorRepositorio;
        this.campanhaRepositorio = campanhaRepositorio;
        this.sessaoRepositorio = sessaoRepositorio;
        recarregarDados();
    }

    public void cadastrarJogador(Jogador jogador) {
        if (jogador == null) throw new IllegalArgumentException("Jogador inválido.");
        jogadorRepositorio.salvar(jogador);
        recarregarDados();
    }

    public void atualizarJogador(Jogador jogadorAtual, String nome, String nomePersonagem) {
        if (jogadorAtual == null) throw new IllegalArgumentException("Selecione um jogador.");
        jogadorRepositorio.atualizar(Jogador.reconstituir(jogadorAtual.getId(), nome, nomePersonagem));
        recarregarDados();
    }

    public void excluirJogador(Jogador jogador) {
        if (jogador == null) throw new IllegalArgumentException("Selecione um jogador.");
        if (jogadorRepositorio.possuiVinculos(jogador.getId()))
            throw new IllegalStateException("O jogador não pode ser excluído porque está vinculado a uma sessão.");
        jogadorRepositorio.excluir(jogador.getId());
        recarregarDados();
    }

    public void cadastrarCampanha(Campanha campanha) {
        if (campanha == null) throw new IllegalArgumentException("Campanha inválida.");
        campanhaRepositorio.salvar(campanha);
        recarregarDados();
    }

    public void atualizarCampanha(Campanha campanhaAtual, String nome, String sistemaRpg) {
        if (campanhaAtual == null) throw new IllegalArgumentException("Selecione uma campanha.");
        campanhaRepositorio.atualizar(Campanha.reconstituir(campanhaAtual.getId(), nome, sistemaRpg));
        recarregarDados();
    }

    public void excluirCampanha(Campanha campanha) {
        if (campanha == null) throw new IllegalArgumentException("Selecione uma campanha.");
        if (sessaoRepositorio.campanhaPossuiSessoes(campanha.getId()))
            throw new IllegalStateException("A campanha não pode ser excluída porque possui sessões.");
        campanhaRepositorio.excluir(campanha.getId());
        recarregarDados();
    }

    public void adicionarSessao(Campanha campanha, Sessao sessao) {
        if (campanha == null || !campanhas.contains(campanha))
            throw new IllegalArgumentException("Selecione uma campanha cadastrada.");
        if (sessao == null) throw new IllegalArgumentException("Sessão inválida.");
        sessaoRepositorio.salvar(campanha.getId(), sessao);
        recarregarDados();
    }

    public void inscreverJogador(Sessao sessao, Jogador jogador) {
        sessao.adicionarJogador(jogador);
        sessaoRepositorio.salvarParticipacao(sessao.getId(), jogador.getId(), StatusPresenca.PENDENTE);
        recarregarDados();
    }

    public void registrarPresenca(Sessao sessao, Jogador jogador, StatusPresenca presenca) {
        sessao.registrarPresenca(jogador, presenca);
        sessaoRepositorio.atualizarPresenca(sessao.getId(), jogador.getId(), presenca);
        recarregarDados();
    }

    public void iniciarSessao(Sessao sessao) {
        sessao.iniciar();
        atualizarStatus(sessao);
    }

    public void finalizarSessao(Sessao sessao) {
        sessao.finalizar();
        atualizarStatus(sessao);
    }

    public void cancelarSessao(Sessao sessao) {
        sessao.cancelar();
        atualizarStatus(sessao);
    }

    public void excluirSessao(Sessao sessao) {
        if (sessao == null) throw new IllegalArgumentException("Selecione uma sessão.");
        if (sessao.getStatus() != StatusSessao.AGENDADA && sessao.getStatus() != StatusSessao.CANCELADA)
            throw new IllegalStateException("Somente sessões agendadas ou canceladas podem ser excluídas.");
        sessaoRepositorio.excluir(sessao.getId());
        recarregarDados();
    }

    public List<Jogador> getJogadores() { return jogadores; }
    public List<Campanha> getCampanhas() { return campanhas; }

    public final void recarregarDados() {
        jogadores = List.copyOf(jogadorRepositorio.listarTodos());
        List<Campanha> campanhasCarregadas = campanhaRepositorio.listarTodas();
        for (Campanha campanha : campanhasCarregadas) {
            sessaoRepositorio.listarPorCampanha(campanha.getId()).forEach(campanha::adicionarSessao);
        }
        campanhas = List.copyOf(campanhasCarregadas);
    }

    private void atualizarStatus(Sessao sessao) {
        sessaoRepositorio.atualizarStatus(sessao.getId(), sessao.getStatus());
        recarregarDados();
    }
}
