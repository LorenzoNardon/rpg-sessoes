package org.example;


import rpg.modelo.Jogador;
import rpg.modelo.SessaoAoVivo;
import rpg.modelo.SessaoPorTexto;
import rpg.regra.EntradaLivre;
import rpg.regra.EntradaPorConvite;

import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Jogador jogador1 = new Jogador("Lorenzo", "Aragorn");
        Jogador jogador2 = new Jogador("Miguel", "Gandalf");
        Jogador jogador3 = new Jogador("João", "Legolas");

        SessaoAoVivo sessaoLivre = new SessaoAoVivo(
                "A Fortaleza Perdida",
                LocalDateTime.now().plusDays(7),
                5,
                "Discord",
                "https://discord.com/",
                new EntradaLivre()
        );

        SessaoPorTexto sessaoConvite = new SessaoPorTexto(
                "A Floresta Sombria",
                LocalDateTime.now().plusDays(10),
                5,
                "Discord",
                "#aventura",
                new EntradaPorConvite(List.of(jogador1, jogador2))
        );

        // Sessão com entrada livre.
        sessaoLivre.adicionarJogador(jogador1);
        sessaoLivre.adicionarJogador(jogador3);

        // Sessão que exige convite.
        sessaoConvite.adicionarJogador(jogador1);
        sessaoConvite.adicionarJogador(jogador2);

        System.out.println("=== SESSÃO AO VIVO ===");
        System.out.println(sessaoLivre.gerarResumo());
        System.out.println("Jogadores: " + sessaoLivre.getJogadores().size());

        System.out.println("\n=== SESSÃO POR TEXTO ===");
        System.out.println(sessaoConvite.gerarResumo());
        System.out.println("Jogadores: " + sessaoConvite.getJogadores().size());

        // Tentativa de entrada sem convite.
        try {
            sessaoConvite.adicionarJogador(jogador3);
        } catch (Exception e) {
            System.out.println("\nTentativa de entrada:");
            System.out.println(e.getMessage());
        }
    }
}