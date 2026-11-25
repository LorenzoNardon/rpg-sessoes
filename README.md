# Gerenciador de Sessões de RPG

Aplicação desktop em Java para cadastrar jogadores e campanhas, organizar sessões de RPG e controlar convites, inscrições, presença e andamento das sessões.

O projeto utiliza Java Swing na interface, PostgreSQL para persistência, JDBC para acesso aos dados e Flyway para criação e evolução automática do banco. A separação entre domínio, aplicação, infraestrutura e apresentação procura manter as regras de negócio independentes da interface e do mecanismo de persistência.

## Funcionalidades atuais

### Jogadores

- Cadastro de jogador e personagem.
- Listagem dos jogadores persistidos.
- Edição dos dados do jogador.
- Exclusão de jogadores sem vínculos com sessões.
- Impedimento de nomes duplicados, sem diferenciar letras maiúsculas e minúsculas.

### Campanhas

- Cadastro de campanha e sistema de RPG.
- Listagem das campanhas persistidas.
- Edição dos dados da campanha.
- Exclusão de campanhas que ainda não possuem sessões.
- Impedimento de nomes duplicados, sem diferenciar letras maiúsculas e minúsculas.

### Sessões

- Criação de sessões associadas a uma campanha.
- Suporte a sessões ao vivo e sessões por texto.
- Definição de data, horário e capacidade máxima.
- Entrada livre ou participação por convite.
- Seleção de vários convidados por caixas de seleção.
- Convite e inscrição tratados como operações diferentes: ser convidado autoriza a inscrição, mas não inscreve o jogador automaticamente.
- Inscrição de jogadores respeitando capacidade, estado da sessão e regra de participação.
- Início, finalização e cancelamento de sessões.
- Registro de presença ou ausência durante uma sessão em andamento.
- Exibição de participantes, vagas e resumo da sessão.
- Exclusão de sessões agendadas ou canceladas.

## Regras de negócio principais

- Uma sessão nova deve possuir data futura e capacidade maior que zero.
- Somente sessões agendadas aceitam inscrições.
- Um jogador não pode se inscrever duas vezes na mesma sessão.
- Uma sessão não pode ultrapassar sua capacidade máxima.
- Em sessões por convite, apenas jogadores convidados podem se inscrever.
- Uma sessão precisa ter pelo menos um participante para ser iniciada.
- Presença e ausência somente podem ser registradas durante a sessão.
- Somente sessões em andamento podem ser finalizadas.
- Somente sessões agendadas podem ser canceladas.
- Jogadores vinculados como convidados ou participantes não podem ser excluídos.
- Campanhas com sessões não podem ser excluídas.

## Tecnologias

- Java 27
- Java Swing
- Maven
- PostgreSQL 18
- PostgreSQL JDBC Driver 42.7.14
- Flyway 13.13.0
- Docker e Docker Compose

## Arquitetura

O código está dividido nas seguintes áreas:

```text
src/main/java
├── Main.java
└── rpg
    ├── aplicacao
    │   ├── excecao          Exceções da camada de aplicação
    │   └── repositorio      Contratos de persistência
    ├── apresentacao
    │   └── interfacegrafica Telas, diálogos e componentes Swing
    ├── dominio
    │   ├── excecao          Exceções das regras de negócio
    │   ├── modelo           Jogador, campanha, sessão e estados
    │   └── regra            Estratégias de participação
    ├── infraestrutura
    │   ├── configuracao     DataSource, configuração e Flyway
    │   └── persistencia     Implementações JDBC dos repositórios
    └── servico              Coordenação dos casos de uso
```

### Responsabilidades

- **Domínio:** concentra os modelos e regras, sem depender de Swing, JDBC ou PostgreSQL.
- **Repositórios:** definem as operações necessárias para persistência por meio de interfaces.
- **Infraestrutura:** implementa os repositórios com JDBC e comandos SQL parametrizados.
- **Serviço:** coordena operações, validações do fluxo e recarga dos dados persistidos.
- **Apresentação:** recebe a interação do usuário e delega as operações ao serviço.
- **Main:** funciona como ponto de composição, criando as dependências e iniciando a aplicação.

A seleção de convidados também depende da abstração `SeletorConvidados`. A implementação Swing fica em `DialogoSelecaoConvidados`, mantendo os detalhes visuais fora do fluxo de criação da sessão.

## Banco de dados

O banco armazena:

- jogadores;
- campanhas;
- sessões;
- convidados de cada sessão;
- participantes e suas presenças.

As tabelas de convidados e participantes são separadas porque um convite representa apenas autorização para uma inscrição futura.

As chaves das entidades são UUIDs. Relacionamentos usam chaves estrangeiras, e a remoção de uma sessão elimina automaticamente seus convites e participantes. Jogadores e campanhas com vínculos continuam protegidos contra exclusão indevida.

### Criação automática das tabelas

O Docker Compose cria o banco e o usuário. Quando o programa inicia, o Flyway aplica automaticamente os arquivos de `src/main/resources/db/migration`:

- `V1__criar_schema_inicial.sql`: cria as tabelas, restrições e índices iniciais.
- `V2__adicionar_indices_de_vinculos.sql`: adiciona índices para consultas de vínculos dos jogadores.

O Flyway mantém a tabela `flyway_schema_history`, que registra quais migrações já foram aplicadas. Dessa forma, as tabelas são criadas somente quando necessário.

## Pré-requisitos

- JDK 27 configurado no `PATH`.
- Maven instalado.
- Docker com suporte ao comando `docker compose`.

Confirme as instalações com:

```bash
java -version
mvn -version
docker compose version
```

## Como executar com Docker

Na raiz do projeto, inicie o PostgreSQL:

```bash
docker compose up -d
```

Confira se o banco está saudável:

```bash
docker compose ps
```

Compile e empacote a aplicação:

```bash
mvn package
```

Execute o JAR gerado:

```bash
java -jar target/rpg-sessoes.jar
```

Na primeira compilação, o Maven baixa as dependências. Na primeira execução da aplicação, o Flyway cria todas as tabelas automaticamente.

Para interromper o banco sem apagar seus dados:

```bash
docker compose stop
```

## Conflito na porta 5432

Se outro PostgreSQL já estiver usando a porta `5432`, escolha outra porta para o contêiner. O mesmo valor deve ser informado ao banco e à aplicação:

```bash
RPG_DB_PORT=5433 docker compose up -d
RPG_DB_PORT=5433 java -jar target/rpg-sessoes.jar
```

## Configuração da conexão

Sem configuração adicional, a aplicação utiliza:

| Propriedade | Valor padrão |
|---|---|
| Host | `localhost` |
| Porta | `5432` |
| Banco | `rpg_sessoes` |
| Usuário | `rpg_app` |
| Senha | `rpg_app` |

Os valores podem ser alterados por variáveis de ambiente:

| Variável | Finalidade |
|---|---|
| `RPG_DB_URL` | URL JDBC completa; quando informada, substitui host, porta e nome do banco |
| `RPG_DB_HOST` | Endereço do PostgreSQL |
| `RPG_DB_PORT` | Porta do PostgreSQL e porta publicada pelo Compose |
| `RPG_DB_NAME` | Nome do banco |
| `RPG_DB_USER` | Usuário da conexão |
| `RPG_DB_PASSWORD` | Senha da conexão |

Exemplo usando um PostgreSQL externo:

```bash
RPG_DB_URL=jdbc:postgresql://servidor:5432/rpg_sessoes \
RPG_DB_USER=usuario \
RPG_DB_PASSWORD=senha \
java -jar target/rpg-sessoes.jar
```

Também é possível utilizar propriedades da JVM:

```bash
java \
  -Drpg.db.url=jdbc:postgresql://localhost:5432/rpg_sessoes \
  -Drpg.db.user=rpg_app \
  -Drpg.db.password=rpg_app \
  -jar target/rpg-sessoes.jar
```

## PostgreSQL sem Docker

Para usar uma instalação local, execute o script de preparação com um usuário administrador do PostgreSQL:

```bash
psql -U postgres -d postgres -f database/bootstrap.sql
```

O script cria, caso ainda não existam:

- o usuário `rpg_app`;
- o banco `rpg_sessoes` pertencente a esse usuário.

Depois, compile e execute normalmente. As tabelas continuam sendo criadas pelo Flyway quando a aplicação inicia.

## Fluxo de inicialização

```text
Main
  ├── carrega a configuração do banco
  ├── cria o DataSource PostgreSQL
  ├── executa as migrações do Flyway
  ├── instancia os repositórios JDBC
  ├── cria o GerenciadorRpg
  └── abre a TelaPrincipal
```

Se o banco estiver indisponível, a interface principal não é aberta e o programa apresenta uma mensagem orientando a iniciar o PostgreSQL.

## Empacotamento

O Maven Shade Plugin gera um único JAR executável com o driver PostgreSQL, Flyway e demais dependências:

```text
target/rpg-sessoes.jar
```

Isso permite executar a aplicação com `java -jar`, sem montar o classpath manualmente.

## Solução de problemas

### A aplicação não conecta ao banco

Verifique o estado do contêiner:

```bash
docker compose ps
docker compose logs postgres
```

Confirme também se a porta configurada na aplicação é a mesma publicada pelo Compose.

### A porta 5432 já está em uso

Use uma porta alternativa conforme a seção de conflito de porta, informando `RPG_DB_PORT` tanto ao Compose quanto ao comando que inicia a aplicação.

### O JAR não existe

Execute novamente, na raiz do projeto:

```bash
mvn package
```

### O Java utilizado não é a versão correta

Confira se `java -version` e `mvn -version` indicam o JDK 27. O projeto é compilado com `maven.compiler.release` definido como `27`.
