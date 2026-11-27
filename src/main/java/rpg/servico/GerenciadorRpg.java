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

/**
 * Centraliza as operações de gerenciamento da aplicação de RPG.
 *
 * Responsável por coordenar as operações entre o domínio e os repositórios,
 * incluindo o cadastro de jogadores, campanhas e sessões, inscrições,
 * registros de presença e alterações de estado das sessões.
 */
public class GerenciadorRpg {

    /** Repositório responsável pela persistência dos jogadores. */
    private final JogadorRepositorio jogadorRepositorio;

    /** Repositório responsável pela persistência das campanhas. */
    private final CampanhaRepositorio campanhaRepositorio;

    /** Repositório responsável pela persistência das sessões. */
    private final SessaoRepositorio sessaoRepositorio;

    /** Lista de jogadores carregados da persistência. */
    private List<Jogador> jogadores = List.of();

    /** Lista de campanhas carregadas da persistência. */
    private List<Campanha> campanhas = List.of();

    /**
     * Cria o gerenciador com os repositórios necessários
     * e carrega os dados iniciais da aplicação.
     *
     * @param jogadorRepositorio repositório de jogadores
     * @param campanhaRepositorio repositório de campanhas
     * @param sessaoRepositorio repositório de sessões
     */
    public GerenciadorRpg(JogadorRepositorio jogadorRepositorio,
                          CampanhaRepositorio campanhaRepositorio,
                          SessaoRepositorio sessaoRepositorio) {
        this.jogadorRepositorio = jogadorRepositorio;
        this.campanhaRepositorio = campanhaRepositorio;
        this.sessaoRepositorio = sessaoRepositorio;
        recarregarDados();
    }

    /**
     * Cadastra um novo jogador e atualiza os dados carregados.
     *
     * @param jogador jogador que será cadastrado
     * @throws IllegalArgumentException se o jogador for nulo
     */
    public void cadastrarJogador(Jogador jogador) {
        if (jogador == null) throw new IllegalArgumentException("Jogador inválido.");
        jogadorRepositorio.salvar(jogador);
        recarregarDados();
    }

    /**
     * Atualiza os dados de um jogador existente.
     *
     * @param jogadorAtual jogador que será atualizado
     * @param nome novo nome do jogador
     * @param nomePersonagem novo nome do personagem
     * @throws IllegalArgumentException se nenhum jogador for selecionado
     */
    public void atualizarJogador(Jogador jogadorAtual, String nome, String nomePersonagem) {
        if (jogadorAtual == null) throw new IllegalArgumentException("Selecione um jogador.");
        jogadorRepositorio.atualizar(Jogador.reconstituir(jogadorAtual.getId(), nome, nomePersonagem));
        recarregarDados();
    }

    /**
     * Exclui um jogador, desde que ele não esteja vinculado a sessões.
     *
     * @param jogador jogador que será excluído
     * @throws IllegalArgumentException se nenhum jogador for selecionado
     * @throws IllegalStateException se o jogador possuir vínculos com sessões
     */
    public void excluirJogador(Jogador jogador) {
        if (jogador == null) throw new IllegalArgumentException("Selecione um jogador.");
        if (jogadorRepositorio.possuiVinculos(jogador.getId()))
            throw new IllegalStateException("O jogador não pode ser excluído porque está vinculado a uma sessão.");
        jogadorRepositorio.excluir(jogador.getId());
        recarregarDados();
    }

    /**
     * Cadastra uma nova campanha e atualiza os dados carregados.
     *
     * @param campanha campanha que será cadastrada
     * @throws IllegalArgumentException se a campanha for nula
     */
    public void cadastrarCampanha(Campanha campanha) {
        if (campanha == null) throw new IllegalArgumentException("Campanha inválida.");
        campanhaRepositorio.salvar(campanha);
        recarregarDados();
    }

    /**
     * Atualiza os dados de uma campanha existente.
     *
     * @param campanhaAtual campanha que será atualizada
     * @param nome novo nome da campanha
     * @param sistemaRpg novo sistema de RPG utilizado
     * @throws IllegalArgumentException se nenhuma campanha for selecionada
     */
    public void atualizarCampanha(Campanha campanhaAtual, String nome, String sistemaRpg) {
        if (campanhaAtual == null) throw new IllegalArgumentException("Selecione uma campanha.");
        campanhaRepositorio.atualizar(Campanha.reconstituir(campanhaAtual.getId(), nome, sistemaRpg));
        recarregarDados();
    }

    /**
     * Exclui uma campanha, desde que ela não possua sessões cadastradas.
     *
     * @param campanha campanha que será excluída
     * @throws IllegalArgumentException se nenhuma campanha for selecionada
     * @throws IllegalStateException se a campanha possuir sessões
     */
    public void excluirCampanha(Campanha campanha) {
        if (campanha == null) throw new IllegalArgumentException("Selecione uma campanha.");
        if (sessaoRepositorio.campanhaPossuiSessoes(campanha.getId()))
            throw new IllegalStateException("A campanha não pode ser excluída porque possui sessões.");
        campanhaRepositorio.excluir(campanha.getId());
        recarregarDados();
    }

    /**
     * Adiciona uma sessão a uma campanha cadastrada.
     *
     * @param campanha campanha à qual a sessão será associada
     * @param sessao sessão que será adicionada
     * @throws IllegalArgumentException se a campanha não estiver cadastrada
     *                                   ou se a sessão for nula
     */
    public void adicionarSessao(Campanha campanha, Sessao sessao) {
        if (campanha == null || !campanhas.contains(campanha))
            throw new IllegalArgumentException("Selecione uma campanha cadastrada.");
        if (sessao == null) throw new IllegalArgumentException("Sessão inválida.");
        sessaoRepositorio.salvar(campanha.getId(), sessao);
        recarregarDados();
    }

    /**
     * Inscreve um jogador em uma sessão e registra sua presença
     * inicial como pendente.
     *
     * @param sessao sessão na qual o jogador será inscrito
     * @param jogador jogador que será inscrito
     */
    public void inscreverJogador(Sessao sessao, Jogador jogador) {
        sessao.adicionarJogador(jogador);
        sessaoRepositorio.salvarParticipacao(sessao.getId(), jogador.getId(), StatusPresenca.PENDENTE);
        recarregarDados();
    }

    /**
     * Registra ou atualiza a situação de presença de um jogador em uma sessão.
     *
     * @param sessao sessão na qual a presença será registrada
     * @param jogador jogador cuja presença será atualizada
     * @param presenca novo estado de presença do jogador
     */
    public void registrarPresenca(Sessao sessao, Jogador jogador, StatusPresenca presenca) {
        sessao.registrarPresenca(jogador, presenca);
        sessaoRepositorio.atualizarPresenca(sessao.getId(), jogador.getId(), presenca);
        recarregarDados();
    }

    /**
     * Inicia uma sessão e persiste sua alteração de estado.
     *
     * @param sessao sessão que será iniciada
     */
    public void iniciarSessao(Sessao sessao) {
        sessao.iniciar();
        atualizarStatus(sessao);
    }

    /**
     * Finaliza uma sessão e persiste sua alteração de estado.
     *
     * @param sessao sessão que será finalizada
     */
    public void finalizarSessao(Sessao sessao) {
        sessao.finalizar();
        atualizarStatus(sessao);
    }

    /**
     * Cancela uma sessão e persiste sua alteração de estado.
     *
     * @param sessao sessão que será cancelada
     */
    public void cancelarSessao(Sessao sessao) {
        sessao.cancelar();
        atualizarStatus(sessao);
    }

    /**
     * Exclui uma sessão somente quando seu estado é agendado ou cancelado.
     *
     * @param sessao sessão que será excluída
     * @throws IllegalArgumentException se nenhuma sessão for selecionada
     * @throws IllegalStateException se a sessão não estiver agendada
     *                                ou cancelada
     */
    public void excluirSessao(Sessao sessao) {
        if (sessao == null) throw new IllegalArgumentException("Selecione uma sessão.");
        if (sessao.getStatus() != StatusSessao.AGENDADA && sessao.getStatus() != StatusSessao.CANCELADA)
            throw new IllegalStateException("Somente sessões agendadas ou canceladas podem ser excluídas.");
        sessaoRepositorio.excluir(sessao.getId());
        recarregarDados();
    }

    /**
     * Retorna a lista de jogadores carregados.
     *
     * @return lista imutável de jogadores
     */
    public List<Jogador> getJogadores() {
        return jogadores;
    }

    /**
     * Retorna a lista de campanhas carregadas.
     *
     * @return lista imutável de campanhas
     */
    public List<Campanha> getCampanhas() {
        return campanhas;
    }

    /**
     * Recarrega os jogadores, as campanhas e suas respectivas sessões
     * a partir dos repositórios.
     */
    public final void recarregarDados() {
        jogadores = List.copyOf(jogadorRepositorio.listarTodos());
        List<Campanha> campanhasCarregadas = campanhaRepositorio.listarTodas();
        for (Campanha campanha : campanhasCarregadas) {
            sessaoRepositorio.listarPorCampanha(campanha.getId()).forEach(campanha::adicionarSessao);
        }
        campanhas = List.copyOf(campanhasCarregadas);
    }

    /**
     * Persiste o estado atual de uma sessão e atualiza os dados carregados.
     *
     * @param sessao sessão cujo estado será atualizado
     */
    private void atualizarStatus(Sessao sessao) {
        sessaoRepositorio.atualizarStatus(sessao.getId(), sessao.getStatus());
        recarregarDados();
    }
}