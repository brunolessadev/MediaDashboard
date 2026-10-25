# MediaDashboard

Aplicativo desktop desenvolvido em Java com JavaFX para organizar e navegar por uma biblioteca local de mídias.

> **Versão de referência:** `media_dashboard_1.5`  
> **Commit de referência:** [`069ce83379d980188e95873ec05e2f7ecab8a69b`](https://github.com/brunolessadev/MediaDashboard/commit/069ce83379d980188e95873ec05e2f7ecab8a69b)

## Sobre o projeto

O MediaDashboard oferece uma interface gráfica para navegar por pastas de mídia organizadas por categoria. O projeto está em desenvolvimento incremental, com foco em manter a organização da biblioteca, personalizar a interface e acompanhar o progresso de vídeos assistidos.

## Categorias de mídia

A estrutura de categorias prevista pelo aplicativo inclui:

- Animes
- Desenhos
- Séries
- Filmes
- Mangás
- Livros
- Desejos

As mídias são organizadas em pastas dentro do diretório `midia/`. A navegação permite entrar em pastas e retornar aos diretórios anteriores.

## Funcionalidades presentes na base de desenvolvimento

- Navegação por categorias e diretórios.
- Exibição de mídias em cards.
- Identificação de pastas e arquivos de vídeo.
- Capas de mídia quando há uma imagem de capa disponível.
- Favoritos persistidos na configuração.
- Temas visuais selecionáveis.
- Contagem de vídeos assistidos e progresso por pasta.
- Reprodução de arquivos compatíveis com o mecanismo de mídia do JavaFX.
- Persistência das configurações em `midia/config.json`.

O suporte à reprodução depende do formato do contêiner e dos codecs reconhecidos pelo JavaFX. Alguns arquivos, incluindo determinados arquivos MKV, podem não ser reproduzidos. A compatibilidade de reprodução não é garantida para todos os formatos listados.

## Tecnologias

- **Java 17**
- **JavaFX 21.0.2**
  - `javafx-controls`
  - `javafx-graphics`
  - `javafx-media`
- **Gson 2.10.1** para leitura e gravação da configuração JSON.
- **Maven** para gerenciamento de dependências e execução do projeto.

## Estrutura geral

A estrutura atual inclui classes na package `com.war`, como:

- `Main`: inicialização da aplicação e integração da interface.
- `MediaScanner`: descoberta de pastas e arquivos de mídia e cálculo de progresso.
- `MediaConfig`: leitura e gravação de configurações, favoritos, tema e estado de assistidos.
- `CardComponent`: criação dos cards de mídia.
- `WindowBar`: barra superior e controles da janela.
- `StyleA` e `StyleB`: estilos visuais da aplicação.

A organização em subpacotes para separar navegação, lista de episódios, player e estilos faz parte da refatoração planejada. A estrutura final poderá mudar conforme essa refatoração avançar.

## Organização da biblioteca

Crie o diretório `midia/` na localização esperada pelo aplicativo e organize suas mídias nas categorias. Exemplo:

```text
midia/
├── config.json
├── Animes/
│   └── Nome do anime/
│       ├── Temporada 01/
│       │   ├── episodio-01.mp4
│       │   └── episodio-02.mkv
│       └── cover.jpg
├── Séries/
├── Desenhos/
├── Filmes/
├── Mangás/
├── Livros/
└── Desejos/
```

O exemplo ilustra uma organização possível; os nomes das pastas e dos arquivos podem variar. A descoberta das mídias depende da estrutura que o scanner consegue ler.

## Configuração

O arquivo `midia/config.json` é usado para persistir preferências da aplicação, incluindo favoritos e tema. O estado dos vídeos assistidos também é armazenado pela configuração da aplicação.

Evite editar o arquivo enquanto o aplicativo estiver aberto. Antes de alterar ou substituir a configuração manualmente, faça uma cópia de segurança.

## Como executar

É necessário ter um JDK compatível com Java 17 e Maven.

Na raiz do projeto, execute:

```bash
mvn clean javafx:run
```

Se a execução estiver configurada por uma IDE, importe o projeto como projeto Maven e execute a classe principal `com.war.Main` ou utilize a configuração Maven equivalente.

## Limitações conhecidas

- A reprodução depende dos formatos e codecs suportados pela implementação de mídia do JavaFX.
- Um arquivo ser detectado pelo scanner não significa que ele necessariamente será reproduzido.
- A interface e a arquitetura estão em evolução; mudanças de organização podem ocorrer durante a refatoração.

## Próximas melhorias planejadas

- Separar as responsabilidades atualmente concentradas em `Main.java`.
- Implementar uma lista compacta de episódios.
- Permitir marcar e desmarcar episódios manualmente como assistidos, mesmo quando não puderem ser reproduzidos.
- Exibir um indicador visual para o estado de cada episódio.
- Manter a contagem e a barra de progresso sincronizadas com os estados salvos.
- Preservar os temas, as capas e os favoritos durante a refatoração.

Esses itens são objetivos de desenvolvimento e não devem ser considerados concluídos até serem implementados e testados.

## Histórico

O desenvolvimento segue por commits incrementais. Esta descrição toma como referência o commit [`media_dashboard_1.5`](https://github.com/brunolessadev/MediaDashboard/commit/069ce83379d980188e95873ec05e2f7ecab8a69b). A refatoração e as melhorias descritas como planejadas devem ser registradas em commits posteriores após implementação e validação.

## Repositório

[github.com/brunolessadev/MediaDashboard](https://github.com/brunolessadev/MediaDashboard)
