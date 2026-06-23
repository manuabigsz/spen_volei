# 🏐 S Pen Placar — Placar de Vôlei para Android

Aplicativo Android nativo (Kotlin + Jetpack Compose) para controle de placar de
partidas de vôlei, otimizado para **velocidade de operação** em jogos amadores e
campeonatos locais, com **controle remoto pela S Pen** do Galaxy S24 Ultra.

---

## ✨ Funcionalidades

- **Dois times (A e B)** com nomes editáveis (toque no nome).
- **Placar gigante** e legível à distância, com escala responsiva (celular/tablet).
- **+1 / -1** por equipe — o painel inteiro é tocável para somar ponto (rápido).
- **Regras automáticas de vôlei**: 25 pontos com diferença mínima de 2;
  **tie-break até 15**; partida em **melhor de 5 sets**.
- **Sets vencidos** exibidos como “pips” sob o nome de cada time.
- **Desfazer** a última ação (pilha de estados).
- **Histórico de pontos** da partida atual (bottom sheet).
- **Cronômetro** da partida (inicia automático no 1º ponto; play/pause manual).
- **Tema claro / escuro / sistema** (Material 3 + cores dinâmicas no Android 12+).
- **Histórico de partidas** salvas (Room).
- **Compartilhar resultado** (Intent de compartilhamento do Android).
- **Espelhamento em TV via Chromecast** (botão de Cast na barra superior).
- **Sons e vibração** ao registrar pontos / sets / fim de jogo (configuráveis).

### 🖊️ Comandos da S Pen
| Gesto no botão da S Pen | Ação |
|---|---|
| Clique simples | +1 ponto para o **Time A** |
| Clique duplo | +1 ponto para o **Time B** |
| Pressionar e segurar | **Desfazer** última ação |

Um **indicador visual** aparece no centro da tela a cada comando recebido.

---

## 🏗️ Arquitetura

Padrão **MVVM** com camadas bem separadas:

```
com.spen.placar
├── domain/            # Regras puras do vôlei (testáveis, sem Android)
│   ├── Models.kt          # MatchState, MatchConfig, TeamSide, ScoreEvent...
│   └── ScoreEngine.kt     # Motor de pontuação (add/fechar set/fim de jogo)
├── data/
│   ├── local/         # Room: MatchEntity, MatchDao, PlacarDatabase
│   ├── repository/    # MatchRepository (histórico de partidas)
│   └── prefs/         # SettingsRepository (DataStore: tema/som/vibração/spen)
├── spen/              # SpenButtonDetector (gestos) + SpenManager (SDK Samsung)
├── cast/              # CastOptionsProvider (Chromecast)
├── util/              # Formatters, FeedbackPlayer (som/vibração)
├── ui/
│   ├── theme/         # Material 3 (cores, tipografia, tema claro/escuro)
│   ├── scoreboard/    # ScoreboardViewModel, ScoreboardScreen + componentes
│   └── history/       # HistoryScreen (partidas salvas)
├── MainActivity.kt    # Activity única (Compose) + init S Pen / Cast
└── PlacarApplication.kt # Service locator (repositórios)
```

- **ViewModel** (`ScoreboardViewModel`) concentra todo o estado: partida, undo,
  histórico, cronômetro, efeitos e persistência.
- **Estado imutável** (`MatchState`) — cada ponto gera um novo estado, o que
  torna o **desfazer** trivial (pilha de snapshots).
- **Persistência**: Room (histórico de partidas) + DataStore (preferências).

---

## ▶️ Como compilar

Pré-requisitos: **Android Studio Ladybug+** e **JDK 17**.

1. Abra a pasta do projeto no Android Studio (ele baixará o Gradle 8.9 e as deps).
2. Caso vá compilar pela linha de comando e o wrapper não exista, gere-o:
   ```bash
   gradle wrapper --gradle-version 8.9
   ./gradlew assembleDebug          # Windows: gradlew.bat assembleDebug
   ```
3. Rode em um dispositivo/emulador com **Android 8.0 (API 26)** ou superior.

> O arquivo `local.properties` (com `sdk.dir`) é gerado automaticamente pelo
> Android Studio. Não o versione.

### Testes
```bash
./gradlew test      # testes unitários do motor de regras (ScoreEngineTest)
```

---

## 🖊️ Habilitando a S Pen no dispositivo

A integração usa o **Samsung S Pen Remote SDK**
(`com.samsung.android.sdk.penremote`), distribuído como AAR proprietário e que
**não está no Maven Central**.

Para que o projeto **compile em qualquer máquina**, a ligação com o SDK é feita
por **reflexão** em `SpenManager.kt`: se o SDK estiver presente em tempo de
execução, o controle por S Pen é ativado automaticamente; caso contrário, o app
funciona normalmente pelos botões na tela (`isAvailable = false`).

Para ativar de fato no Galaxy S24 Ultra:

1. Baixe o **Pen Remote SDK** no [Samsung Developers](https://developer.samsung.com/galaxy-spen-remote).
2. Copie o `.aar` para `app/libs/`.
3. Adicione em `app/build.gradle.kts`:
   ```kotlin
   implementation(files("libs/spenremote-vX.Y.Z.aar"))
   ```
4. Reinstale o app. A lógica de gestos (clique/duplo/segurar) já está pronta em
   `SpenButtonDetector.kt` — basta o SDK entregar os eventos de botão.

---

## 📺 Chromecast

O `CastOptionsProvider` usa o **Default Media Receiver** (`CC1AD845`) para
desenvolvimento. Para um receiver que renderize o placar em tela cheia na TV,
registre seu **App ID** no [Cast SDK Developer Console](https://cast.google.com/publish)
e substitua `cast_app_id` em `app/build.gradle.kts`.

---

## ⚙️ Regras configuráveis

As regras ficam em `MatchConfig` (em `domain/Models.kt`) e podem ser ajustadas:

```kotlin
MatchConfig(
    pointsPerSet = 25,
    tieBreakPoints = 15,
    minLead = 2,
    setsToWin = 3,   // melhor de 5
    maxSets = 5
)
```

---

## 📄 Licença

Projeto de exemplo — use livremente como base para seus próprios apps.
