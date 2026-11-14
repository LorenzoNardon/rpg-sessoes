
package rpg.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Campanha {

    private final String nome;
    private final String sistemaRpg;
    private final List<Sessao> sessoes;

    public Campanha(String nome, String sistemaRpg) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da campanha é obrigatório.");
        }

        if (sistemaRpg == null || sistemaRpg.isBlank()) {
            throw new IllegalArgumentException("O sistema de RPG é obrigatório.");
        }

        this.nome = nome;
        this.sistemaRpg = sistemaRpg;
        this.sessoes = new ArrayList<>();
    }

    public String getNome() {
        return nome;
    }

    public String getSistemaRpg() {
        return sistemaRpg;
    }

    public List<Sessao> getSessoes() {
        return Collections.unmodifiableList(sessoes);
    }

    public void adicionarSessao(Sessao sessao) {
        if (sessao == null) {
            throw new IllegalArgumentException("A sessão não pode ser nula.");
        }

        if (sessoes.contains(sessao)) {
            throw new IllegalArgumentException("Essa sessão já está cadastrada na campanha.");
        }

        sessoes.add(sessao);
    }

    public int quantidadeSessoes() {
        return sessoes.size();
    }
}
