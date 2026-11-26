package rpg.apresentacao.interfacegrafica;

import javax.swing.JFrame;

/**
 * O que as abas precisam da janela principal: o frame (para diálogos) e a execução
 * de operações com tratamento de erro e, quando demoradas, em segundo plano.
 */
interface ContextoTela {

    JFrame janela();

    /** Executa na thread da interface; erros viram aviso ao usuário. */
    void executar(Runnable operacao);

    /** Executa fora da thread da interface, sem congelar a janela, e atualiza as abas ao final. */
    void emSegundoPlano(Runnable tarefa, Runnable aoSucesso, String mensagemSucesso);
}
