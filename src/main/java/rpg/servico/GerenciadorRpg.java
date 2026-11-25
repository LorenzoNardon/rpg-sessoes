package rpg.servico;

import rpg.dominio.modelo.Campanha;
import rpg.dominio.modelo.Jogador;
import rpg.dominio.modelo.Sessao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GerenciadorRpg {
    private final List<Jogador> jogadores = new ArrayList<>();
    private final List<Campanha> campanhas = new ArrayList<>();

    public void cadastrarJogador(Jogador jogador) {
        if (jogador == null) throw new IllegalArgumentException("Jogador inválido.");
        for (Jogador existente : jogadores) {
            if (existente.getNome().equalsIgnoreCase(jogador.getNome())) {
                throw new IllegalArgumentException("Já existe um jogador com esse nome.");
            }
        }
        jogadores.add(jogador);
    }

    public void cadastrarCampanha(Campanha campanha) {
        if (campanha == null) throw new IllegalArgumentException("Campanha inválida.");
        for (Campanha existente : campanhas) {
            if (existente.getNome().equalsIgnoreCase(campanha.getNome())) {
                throw new IllegalArgumentException("Já existe uma campanha com esse nome.");
            }
        }
        campanhas.add(campanha);
    }

    public void adicionarSessao(Campanha campanha, Sessao sessao) {
        if (!campanhas.contains(campanha)) throw new IllegalArgumentException("Selecione uma campanha cadastrada.");
        campanha.adicionarSessao(sessao);
    }

    public List<Jogador> getJogadores() { return Collections.unmodifiableList(jogadores); }
    public List<Campanha> getCampanhas() { return Collections.unmodifiableList(campanhas); }
}
