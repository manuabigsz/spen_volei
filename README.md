# 🏐 S Pen Placar — Placar de Vôlei + Gestão de Times

Aplicativo Android nativo (Kotlin + Jetpack Compose) para controle de placar de
vôlei, **controle remoto pela S Pen** (Galaxy S24 Ultra), **cadastro de
jogadores**, **sorteio de times balanceados** e **sincronização na nuvem
(Supabase)**. Foco em velocidade de operação durante jogos amadores e rachas.

---

## ✨ Funcionalidades

### Placar
- **Dois times (A e B)** com nomes editáveis, layout premium minimalista.
- **Placar gigante** responsivo (celular/tablet), número animado.
- **+1 / -1** por equipe; toque no card inteiro = +1 (operação rápida).
- **Regras automáticas**: 25 pontos com diferença de 2; **tie-break a 15**;
  **melhor de 5 sets**. Encerramento automático de set/partida.
- **Sets vencidos** (pips) e **placar dos sets** já encerrados.
- **Desfazer**, **histórico de pontos** da partida, **cronômetro**.
- **Tema claro/escuro** (paleta controlada estilo Apple/Stripe).
- **Som distinto por equipe** (grave p/ A, agudo p/ B), **vibração**.
- **Tela de vitória** com troféu e **confete**.

### S Pen (Galaxy)
| Gesto no botão | Ação |
|---|---|
| Clique simples | +1 Time A |
| Clique duplo | +1 Time B |
| Pressionar e segurar | Desfazer |

O card afetado é **realçado** e um indicador aparece a cada comando.

### Jogadores e times
- **Cadastro** com 5 habilidades (Saque, Recepção, Levantamento, Corte,
  Movimentação), cada uma em **Básico / Intermediário / Avançado**.
- **Importar CSV** (menu ⋮). Colunas: `Jogador, Saque, Recepção, Levantamento,
  Corte, Movimentação` (com/sem cabeçalho, separador `,` ou `;`, tolera acentos
  e abreviações). **Ignora nomes já existentes** (sem duplicar).
- **Seleção de presentes** com contador e remoção em massa.
- **Sorteio de times balanceados** (2 a 6 times), divisão igualitária com
  **rodízio** para as sobras. Algoritmo LPT equilibra o nível dos times.
- **Restrições**: pares que **não podem** ficar no mesmo time.
- **Níveis ocultos** no resultado (revelados por um ícone de info).
- **"Usar no placar"**: leva os 2 times para o placar (com elenco).
- **Evolução do jogador**: histórico de melhoras/quedas por habilidade (↑/↓).

### Nuvem (Supabase)
- **Partidas** e **sorteios** são salvos automaticamente / sob demanda.
- **Jogadores** sincronizam (sobe ao editar/importar; desce ao abrir/atualizar).
- **Histórico de partidas** puxa da nuvem, com indicador de carregamento.

---

## 🏗️ Arquitetura (MVVM)

```
com.spen.placar
├── domain/            # Regras puras: ScoreEngine, SkillLevel, TeamBalancer
├── data/
│   ├── local/         # Room: Match, Player, PlayerConstraint, PlayerHistory + DAOs
│   ├── remote/        # SupabaseRemote (PostgREST via HttpURLConnection)
│   ├── repository/    # MatchRepository, PlayerRepository
│   └── prefs/         # SettingsRepository (DataStore)
├── spen/              # SpenButtonDetector + SpenManager (SDK Samsung por reflexão)
├── cast/              # CastOptionsProvider (Chromecast — botão removido da UI)
├── util/              # Formatters, FeedbackPlayer, CsvPlayers
├── ui/
│   ├── theme/         # Material 3 (cores, tipografia, tema)
│   ├── scoreboard/    # ScoreboardViewModel + tela + componentes
│   ├── players/       # PlayersViewModel + telas de jogadores/sorteio/evolução
│   └── history/       # HistoryScreen
├── MainActivity.kt    # Activity única (Compose) + S Pen
└── PlacarApplication.kt # Service locator (repositórios + SupabaseRemote)
```

- **Estado imutável** (`MatchState`) → desfazer trivial (pilha de snapshots).
- **Persistência**: Room (offline) + Supabase (nuvem) + DataStore (preferências).

---

## ▶️ Como compilar

Pré-requisitos: **JDK 17** e **Android SDK** (API 34).

```bash
# Windows
.\gradlew.bat assembleDebug
# Linux/macOS
./gradlew assembleDebug
```

APK em `app/build/outputs/apk/debug/app-debug.apk`.

> O `JAVA_HOME` precisa apontar para um **JDK 17**. O `local.properties`
> (com `sdk.dir` e as chaves do Supabase) é gerado/editado localmente e **não
> vai para o git**.

### Testes
```bash
./gradlew test    # ScoreEngineTest, TeamBalancerTest
```

---

## 🖊️ Habilitar a S Pen

A integração usa o **Samsung S Pen Remote SDK** (`spenremote-v1.0.1.jar`),
distribuído pela Samsung (login gratuito). O projeto compila sem ele (a S Pen
fica inativa, os botões na tela funcionam normalmente).

1. Baixe o SDK em https://developer.samsung.com/galaxy-spen-remote/download.html
2. Extraia e copie o `.jar` para `app/libs/` (o Gradle inclui automaticamente).
3. Recompile. No dispositivo, pareie a S Pen por Bluetooth.

> Importante: o SDK exige um **contexto de Activity** em `connect()` — já tratado
> no `SpenManager`.

---

## ☁️ Supabase

Configuração em `local.properties` (fora do git):

```properties
SUPABASE_URL=https://<project-id>.supabase.co
SUPABASE_ANON_KEY=sb_publishable_xxx   # SOMENTE a publishable key
```

Expostas via `BuildConfig`. **Nunca** coloque a *secret key* no app.

### Tabelas (criadas no banco)
- `matches` — partidas (times, sets, vencedor, duração, `players_a/b`).
- `team_draws` — sorteios (times em `jsonb`, rodízio).
- `players` — jogadores (nome único, 5 habilidades).

Todas com **RLS** e política para a publishable key. Para criar/ajustar o
schema, rode o SQL equivalente no **SQL Editor** do Supabase (ou via conexão
direta com o Postgres).

> O **histórico de evolução** (`player_history`) é **local** (Room). Pode ser
> sincronizado na nuvem futuramente.

---

## ⚙️ Regras configuráveis

Em `domain/Models.kt` (`MatchConfig`): pontos por set, tie-break, diferença
mínima, sets para vencer.

---

## 📄 Licença

Projeto de exemplo — use livremente como base.
