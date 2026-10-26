# MediaDashboard

Aplicativo desktop desenvolvido em Java com JavaFX para organizar, navegar e acompanhar uma biblioteca local de mídias.

> **Versão de referência:** `media_dashboard_1.8`  

## Sobre o projeto

O MediaDashboard oferece uma interface gráfica para navegar por uma biblioteca local organizada em categorias. O projeto utiliza cards para representar pastas e mídias, permite gerenciar favoritos, acompanha o estado dos vídeos assistidos e oferece temas visuais personalizáveis.

A aplicação está em desenvolvimento incremental, com foco na organização do código, na experiência de navegação e na criação de uma interface desktop leve e personalizável.

## Categorias de mídia

O aplicativo contempla as seguintes categorias:

- Animes
- Desenhos
- Séries
- Filmes
- Mangás
- Livros
- Fotos
- Vídeos
- Desejos

As mídias são organizadas em pastas dentro do diretório `midia/`. A navegação permite entrar em subpastas, retornar aos diretórios anteriores e visualizar o conteúdo disponível em cada categoria.

Categorias sem conteúdo detectado podem não ser exibidas na interface.

## Funcionalidades

### Navegação e organização

- Navegação por categorias e subdiretórios.
- Exibição de pastas em cards.
- Exibição de capas quando há imagens disponíveis.
- Indicadores de quantidade de vídeos e progresso por pasta.
- Gerenciamento de favoritos com persistência das configurações.
- Barra de navegação com caminho do diretório atual e botão para voltar.

### Vídeos e episódios

- Listagem de arquivos de vídeo em diretórios.
- Identificação visual de episódios assistidos e não assistidos.
- Indicadores coloridos de status: verde para assistidos e vermelho para não assistidos.
- Botões para marcar ou desmarcar episódios como assistidos.
- Atualização do progresso após alterações no estado dos episódios.
- Abertura de vídeos pelo reprodutor padrão do Windows na categoria `Vídeos`.
- Reprodução por meio do mecanismo configurado na aplicação nas demais categorias, conforme o fluxo utilizado.

A abertura de um vídeo pela categoria `Vídeos` registra o episódio como assistido após a ação de abertura ser acionada. Isso não confirma que o reprodutor externo iniciou ou concluiu a reprodução.

A compatibilidade de reprodução depende do formato do contêiner e dos codecs reconhecidos pelo mecanismo utilizado. A detecção de um arquivo não garante que ele possa ser reproduzido.

### Fotos

- Exibição de imagens em miniaturas.
- Ajuste do tamanho das miniaturas por meio de um controle deslizante.
- Visualização de imagens em tela cheia.
- Navegação entre imagens usando os controles da interface ou as teclas direcionais.
- Exibição do nome e da posição da imagem na coleção.
- Tratamento de arquivos de imagem indisponíveis ou inválidos.

### Livros

- Organização de livros por pastas.
- Contagem de livros nas coleções.
- Abertura da pasta que contém o arquivo selecionado.

### Interface e personalização

- Temas visuais selecionáveis:
  - Cyberpunk Neon
  - Crunchyroll
  - Nórdico
- Botões de episódios com estilos diferentes conforme sua função.
- Atualização dos estilos de acordo com o tema selecionado.
- Interface baseada em componentes JavaFX.

## Tecnologias

- **Java 17**
- **JavaFX 21.0.2**
  - `javafx-controls`
  - `javafx-graphics`
  - `javafx-media`
- **Gson 2.10.1** — leitura e gravação de configurações JSON.
- **Maven** — gerenciamento de dependências e execução do projeto.

## Estrutura do projeto

A aplicação utiliza classes organizadas em pacotes para separar responsabilidades.

Entre os componentes principais estão:

- `Main` — inicialização e integração da aplicação.
- `MediaScanner` — descoberta de pastas e arquivos, identificação de mídias e cálculo de progresso.
- `MediaConfig` — persistência de configurações, favoritos, tema e estado dos episódios assistidos.
- `CardComponent` — criação dos cards de mídia.
- `WindowBar` — barra superior e controles da janela.
- `MediaBrowser` — navegação por categorias e diretórios, visualização de fotos e abertura de mídias.
- `EpisodeList` — criação e organização da lista de episódios.
- `EpisodeRow` — representação de cada episódio, status e ações disponíveis.
- `StyleA` e `StyleB` — aplicação dos temas e estilos visuais.

A estrutura pode evoluir conforme novas funcionalidades sejam implementadas e as responsabilidades sejam refinadas.

## Organização da biblioteca

Exemplo de organização local:

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
├── Fotos/
├── Vídeos/
└── Desejos/
```

Essa estrutura é ilustrativa. Os nomes e a organização das pastas podem variar, desde que sejam compatíveis com a lógica de descoberta implementada no `MediaScanner`.

As imagens podem ser utilizadas como capas ou visualizadas na categoria `Fotos`, conforme o local em que estiverem armazenadas e os formatos reconhecidos pelo scanner.

## Configuração e persistência

O arquivo `midia/config.json` é utilizado para armazenar configurações da aplicação, incluindo favoritos, tema e estado dos vídeos assistidos.

Como o diretório `midia/` contém arquivos pessoais e arquivos de mídia, ele é ignorado pelo Git. Assim, vídeos, imagens, livros, capas e demais arquivos da biblioteca não são enviados ao repositório por meio do controle de versão.

**Importante:** mantenha uma cópia de segurança de `config.json` se desejar preservar seus favoritos, tema e progresso ao reinstalar ou reorganizar a biblioteca.

## Como executar

Requisitos:

- JDK compatível com Java 17.
- Maven, instalado ou disponibilizado pela IDE.
- Dependências Maven acessíveis para resolução do projeto.

Na raiz do projeto, execute:

```bash
mvn clean javafx:run
```

Se estiver utilizando uma IDE, importe o projeto como projeto Maven e execute a classe principal `com.war.Main` ou utilize uma configuração Maven equivalente.

## Limitações conhecidas

- A reprodução depende dos formatos e codecs suportados pelo mecanismo de mídia utilizado.
- Arquivos detectados pelo scanner podem não ser reproduzíveis.
- A abertura de vídeos por um reprodutor externo não confirma o início ou a conclusão da reprodução.
- A estrutura e a interface podem sofrer alterações durante a evolução do projeto.
- O diretório local de mídias não é versionado pelo Git.

## Próximas melhorias

As melhorias futuras podem incluir:

- Registro da data e hora da última abertura de cada vídeo.
- Destaque visual do último episódio aberto.
- Refinamento da organização e separação das responsabilidades do código.
- Melhorias na compatibilidade de reprodução de formatos adicionais.
- Preparação de uma distribuição portátil com as dependências necessárias, sem exigir instalações adicionais no computador de destino.

Os itens acima são propostas futuras e não devem ser considerados concluídos até sua implementação e validação.

## Histórico de desenvolvimento

O projeto evolui por meio de commits incrementais. Esta documentação mantém como referência a versão `media_dashboard_1.5` e o commit inicial indicado no cabeçalho.

Alterações posteriores devem ser registradas em novos commits após implementação e validação.

