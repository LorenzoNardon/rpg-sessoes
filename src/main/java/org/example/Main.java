package org.example;


import rpg.modelo.Jogador;
import rpg.modelo.Sessao;
import rpg.modelo.StatusSessao;

import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

        // Criando um jogador
        Jogador jogador1 = new Jogador("Lorenzo", "Aragorn");
        Jogador jogador2 = new Jogador("Miguel", "Gandalf");

        // Criando uma sessão de teste usando uma implementação anônima
        Sessao sessao = new Sessao(
                "A Fortaleza Perdida",
                LocalDateTime.now().plusDays(7),
                4
        ) {
            @Override
            public String gerarResumo() {
                return "Sessão: " + getTitulo();
            }
        };

        // Adicionando jogadores
        sessao.adicionarJogador(jogador1);
        sessao.adicionarJogador(jogador2);

        // Exibindo informações
        System.out.println(sessao.gerarResumo());
        System.out.println("Jogadores: " + sessao.getJogadores().size());
        System.out.println("Possui vagas: " + sessao.possuiVagas());
        System.out.println("Status: " + sessao.getStatus());

        // Iniciando sessão
        sessao.iniciar();

        System.out.println("Novo status: " + sessao.getStatus());

        // Finalizando sessão
        sessao.finalizar();

        System.out.println("Status final: " + sessao.getStatus());
    }
}
