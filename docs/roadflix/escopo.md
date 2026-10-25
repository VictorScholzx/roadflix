# android-auto-ex2 — Escopo completo do projeto

**Projeto:** navegador e espelhamento de tela na central multimídia original do Geely EX2 Pro, via Android Auto
**Versão:** 1.3 — resultado da Fase 1 (auditoria documental do AA Suite, do método de instalação e das alternativas)
**Data da atualização:** 22/10/2026
**Orçamento prioritário:** R$ 0
**Distribuição:** uso pessoal
**Status:** Fase 1 concluída (documentação). Nenhuma ferramenta foi instalada, nenhum aparelho foi tocado, nada foi gasto. Próximo passo depende de decisão do usuário (seção 13).

> Convenção usada neste documento:
> - ✅ **Confirmado** — verificado em fonte oficial/código-fonte, com link.
> - 📄 **Declarado** — afirmado pelo projeto/autor, mas não testado por nós.
> - ❓ **Incerto** — não comprovado; só um teste resolve.
> - 🔍 **Hipótese** — nossa inferência, a validar.
>
> Compatibilidade com o Geely EX2 Pro nunca deve ser presumida a partir de testes em outro veículo ou no DHU.

---

## 1. Objetivo e visão do produto

Criar ou adaptar uma solução Android que permita acessar um navegador e espelhar conteúdo de um aparelho Android na central multimídia original do Geely EX2 Pro, utilizando Android Auto.

Estratégia acordada (inalterada):

1. Auditar projetos existentes antes de escrever código próprio.
2. Investigar o AA Suite como candidato inicial.
3. Priorizar métodos de instalação e testes gratuitos.
4. Não instalar ferramentas, alterar configurações ou atualizar aparelhos antes de entender requisitos e opções.
5. Testar primeiro o projeto original, sem personalizações.
6. Só depois decidir sobre adaptações, desenvolvimento adicional e aparelho dedicado.

## 2. Equipamentos e ambiente

### 2.1 Veículo e central

- Veículo: Geely EX2 Pro, central original de fábrica.
- Conexão informada: Android Auto e Apple CarPlay por USB.
- Interesse adicional: conexão sem fio.
- ❓ Compatibilidade do AA Suite ou de qualquer alternativa com esta central: não testada.
- ❓ Se a central suporta Android Auto sem fio nativamente: não confirmado.

### 2.2 Aparelhos Android

| Aparelho | Informações conhecidas | Papel possível |
|---|---|---|
| Xiaomi Redmi Note 8 | Android 10; MIUI 12.0.4; build `QKQ1.200114.002` | **Primeiro candidato. Não atualizar** — ver 7.1: o Android 10 é provavelmente uma *vantagem* para o método de instalação |
| Xiaomi Redmi Note 11 | Versão do Android não confirmada | Alternativa |
| POCO X5 Pro | Android 15 (informado pelo usuário em 23/10/2026); versão HyperOS e build ainda não anotadas | **Candidato forte para seleção de app individual e aparelho dedicado**; pior candidato para o caminho B — ver 7.2 |
| iPhone | Disponível | Fora do escopo |

**Redmi Note 8, lido via `adb` em 23/10/2026 (somente leitura):**

| Item | Valor |
|---|---|
| Modelo / codinome | Redmi Note 8 / `ginkgo` |
| Android / API | 10 / 29 |
| MIUI | V12.0.4.0.QCOMIXM (global), build `QKQ1.200114.002` |
| Patch de segurança | 01/02/2021 (antigo) |
| ROM | Original: `release-keys`, build `user`, bootloader travado, verified boot `green` |
| App Android Auto | **17.9.664044**, instalado pela Play Store; atualizado automaticamente pela Play em 23/10/2026 20:54 |
| Google Play Store / Play Services | 53.6.15 / 26.18.33 |
| Tela | 1080 × 2340 |
| Armazenamento livre | 98 GB de 107 GB |
| Bateria | 77%, saúde "boa", 33,2 °C |
| AA Suite | Não instalado |

### 2.3 Computador (verificado em 22/10/2026, somente leitura)

| Item | Resultado |
|---|---|
| Sistema | ✅ Windows 11 Home (build 26200) |
| CPU / RAM | ✅ Intel Core i5-13450HX / 15,6 GB |
| Espaço livre (C:) | ✅ 222,7 GB |
| Git | ✅ 2.53.0 instalado |
| JDK | ✅ JDK 21.0.2 e JDK 26.0.1 instalados (o `java` padrão no PATH é o 26); também Java 8 (JRE) |
| `JAVA_HOME` | Não definido |
| Android Studio | ❌ não encontrado |
| Android SDK / `adb` | ❌ não encontrados |
| Gradle global | ❌ não instalado (não é necessário: o projeto usa o *wrapper*) |
| `winget` | ✅ disponível |

Experiência: Git/GitHub básico; preferência por reaproveitar projeto existente.

## 3. Requisitos definidos pelo usuário

(Inalterados em relação à v1.2; a seção 5.4 compara cada um com o que o AA Suite oferece.)

### 3.1 Navegador
Barra de endereço/pesquisa; voltar e avançar; abas; histórico; favoritos e atalhos; teclado utilizável na horizontal; ajuste de escala e, se possível, resolução; abrir links enviados pelo telefone; interface escura e automotiva.

### 3.2 Screen Share
Espelhamento da tela inteira; investigar seleção de apps individuais; ver sites e apps Android; áudio nos alto-falantes do carro; paisagem; ajuste de escala e proporção.

### 3.3 Conectividade e automação
USB; sem fio, se possível; reconexão; início automático; mínimo de intervenção manual dentro das regras do Android Auto.

### 3.4 Offline
Algum funcionamento sem internet, definido por recurso (ver 5.6).

### 3.5 Aparência e uso
Escura, minimalista, horizontal; uso por passageiros; não incentivar o motorista; testes sempre com o carro estacionado.

## 4. Restrições e decisões de processo

- **Orçamento prioritário de R$ 0.** Nenhum gasto sem autorização expressa.
- Não atualizar o Android de nenhum aparelho.
- Não instalar ferramentas antes de autorização (a Fase 1 terminou; a Fase 2 pede autorização — seção 13).
- Não alterar o código do projeto-base antes de compilar/testar a versão original.
- Não presumir que um APK instalado por USB aparece no Android Auto (**agora confirmado que, por padrão, não aparece** — seção 6).
- Não presumir compatibilidade com o Geely sem teste na central real.
- Evitar root, downgrade do Android Auto e procedimentos que comprometam segurança, estabilidade ou dados.

---

## 5. AA Suite — resultado da auditoria

### 5.1 Identificação e saúde do repositório

| Item | Resultado |
|---|---|
| Repositório | ✅ https://github.com/iAlias/AASuite (autor: "Antonio", Itália) |
| Licença | ✅ MIT (© 2026 iAlias) — permite modificar e usar livremente |
| Criado / último push | ✅ 26/08/2026 / 17/10/2026 — projeto **muito novo** (≈2 meses) |
| Popularidade | ✅ 6 estrelas, 2 forks, 49 commits, 0 issues abertas, 1 PR (fechada) |
| Releases | ✅ **Nenhum release publicado** — não há APK pronto; é preciso compilar |
| Versão no código | ✅ 0.9 (`versionCode 9`) |
| Idioma | Kotlin; README em inglês e italiano |
| Módulos | ✅ `app/` (celular conectado ao carro) e `companion/` ("AA Share", celular do passageiro) |

**Leitura de risco:** projeto pequeno, de um único autor, recente e sem histórico de uso por terceiros. Por outro lado, o código é legível, tem testes unitários e commits bem documentados. Não há comunidade para pedir ajuda; problemas serão resolvidos por nós.

### 5.2 Como o app se registra no Android Auto (confirmado no código)

Do `AndroidManifest.xml` e `automotive_app_desc.xml`:

- ✅ É um app da **Android for Cars App Library** (`androidx.car.app:app:1.4.0`), tipo *template*.
- ✅ Registra-se na categoria **NAVIGATION** (`androidx.car.app.category.NAVIGATION`) com as permissões `NAVIGATION_TEMPLATES` e `ACCESS_SURFACE`. É isso que dá acesso a uma superfície de desenho livre na tela do carro (o "mapa"), onde ele desenha o espelhamento e o navegador.
- ✅ O próprio autor reconhece (em `store/scheda-play.md`) que esse uso da categoria navegação **não é conforme** às diretrizes de qualidade do Google e não passaria na revisão da Play Store; por isso ele usa somente o canal de **teste interno**.
- ✅ Permissões no celular: Internet, serviço em primeiro plano (`mediaProjection`), notificações, sobreposição (`SYSTEM_ALERT_WINDOW`), `WRITE_SETTINGS` (brilho/rotação), serviço de acessibilidade (para injetar toques).

**Consequência importante:** por ser um app de *template* da Car App Library, ele está sujeito à regra de distribuição descrita na seção 6.

### 5.3 Requisitos de compilação (confirmado nos arquivos de build)

| Item | Valor |
|---|---|
| Gradle (wrapper) | ✅ 8.11.1 (baixado automaticamente pelo `gradlew`) |
| Android Gradle Plugin | ✅ 8.7.3 |
| Kotlin | ✅ 2.1.0 |
| `compileSdk` / `targetSdk` | ✅ 36 (Android 16) |
| `minSdk` | ✅ 26 (Android 8) |
| Bytecode Java | ✅ 17 |
| Assinatura release | Opcional; sem keystore, só o build *debug* é gerado (suficiente para testes) |

Implicações para o Windows deste PC:

- 🔍 **Usar o JDK 21 já instalado**, apontando `JAVA_HOME` para ele só na sessão do build. O JDK 26 (padrão no PATH) é mais novo do que o suportado pelo Gradle 8.11.1 e provavelmente fará o build falhar. Isso evita instalar outro JDK.
- 🔍 **Android Studio não é obrigatório para compilar.** Basta o *Android SDK Command-line Tools* + `platforms;android-36` + `build-tools` + `platform-tools` (inclui o `adb`). Estimativa: 2–4 GB. O Android Studio completo (≈3 GB + SDK) é mais confortável para iniciantes e para depurar, mas é opcional.
- ✅ O DHU é instalado pelo SDK Manager (pacote *Android Auto Desktop Head Unit Emulator*, em `extras/google/auto/`). A documentação oficial só descreve a instalação via Android Studio; 🔍 o `sdkmanager` de linha de comando provavelmente instala o mesmo pacote (`extras;google;auto`).
- Comandos de build do README: `gradlew installDebug` (app), `gradlew :companion:installDebug` (AA Share), `gradlew test`.

### 5.4 Requisitos do usuário × o que o AA Suite oferece

| Requisito | AA Suite | Status |
|---|---|---|
| Barra de endereço / pesquisa | Pesquisa Google ou URL digitada no carro | 📄 declarado |
| Teclado | Teclado do próprio Android Auto; 🔍 normalmente só disponível com o carro parado | 📄 / 🔍 |
| Voltar | Botão voltar na barra de ações | 📄 |
| Avançar | Não mencionado | ❓ provavelmente ausente |
| **Abas** | Não mencionado | ❌ **não oferecido** (desenvolvimento futuro) |
| **Histórico** | Não mencionado | ❌ **não oferecido** (desenvolvimento futuro) |
| Favoritos | Sim, editados no celular | 📄 |
| Abrir link enviado pelo celular | Sim, um toque no próprio celular; e do celular de um passageiro via app AA Share (servidor HTTP na porta 8977, rede do hotspot) | 📄 |
| Escala | Zoom do WebView; modo "fill screen" | 📄 |
| Resolução | ✅ Limitada pelo código: o display virtual não pode ser maior que a superfície do carro; usa-se zoom em vez de resolução | ✅ limitação de projeto |
| Interface escura | Não; tema padrão `Theme.Material.Light` no celular; tela do carro segue o host | ❌ desenvolvimento futuro |
| Espelhamento da tela inteira | Sim (MediaProjection → VirtualDisplay) com toque e arraste repassados via acessibilidade | 📄 |
| **Seleção de app individual** | Não | ❌ não oferecido (ver 8.3) |
| Paisagem | Trava de rotação horizontal via sobreposição (não persiste entre sessões) | 📄 |
| Proporção | "Aspect-fit" ou "fill" (corta) | 📄 |
| Áudio do YouTube TV | Sai pelo carro via Android Auto | 📄 |
| Áudio no espelhamento geral | Não documentado | ❓ (ver 5.7) |
| Barra de ações | menu, play/pause, voltar, home | 📄 |
| Bônus | Interface YouTube TV (pareamento por código de TV) | 📄 |

### 5.5 Limitações conhecidas (declaradas)

- Não é receptor Chromecast certificado: Netflix, Disney+, Prime Video impossíveis (DRM).
- YouTube TV: sair de um vídeo exige dois toques em voltar.
- Vídeo 16:9 em tela 2:1 tem barras laterais, a menos que "fill" esteja ligado (corta a imagem).
- Desenvolvido/testado num Nissan Qashqai J12 e no DHU — **nunca** no Geely EX2 Pro.
- ✅ A tela de consentimento de captura (MediaProjection) aparece **a cada início** de espelhamento e precisa ser confirmada no celular (o autor diz isso na descrição da loja).

### 5.6 Funcionamento offline (definição por recurso)

| Recurso | Sem internet? |
|---|---|
| Abrir o app no carro, menus, configurações | 🔍 Sim (lógica local) |
| Favoritos (lista) | 🔍 Sim, guardados no celular; abrir o site exige internet |
| Navegar em sites, pesquisa Google, YouTube TV | Não |
| Espelhamento da tela | 🔍 Sim — é local entre celular e carro; o conteúdo espelhado depende do app |
| Envio de link pelo AA Share | 🔍 Sim, se os dois celulares estiverem na mesma rede local (hotspot), mesmo sem internet; o link em si só abre com internet |

### 5.7 Áudio

- 📄 YouTube TV: áudio pelo carro.
- 🔍 Espelhamento geral: o AA Suite não captura nem transmite áudio. O que tocar no celular deve sair pelo canal de mídia normal do Android Auto (o mesmo usado por Spotify e afins), mas isso depende de foco de áudio e do app. **Só o teste no carro confirma.**

### 5.8 Automação

- ❌ **Início automático do espelhamento sem toque é inviável sem root**: o Android exige confirmação manual do MediaProjection a cada sessão.
- 🔍 O modo navegador não usa MediaProjection, então pode, em tese, abrir sem confirmação no celular.
- ❓ Reconexão após desconexão: não documentada.
- 🔍 O app precisa de "bateria sem restrições" no MIUI para não ser morto em segundo plano.

---

## 6. Método de instalação e custo — resultado da auditoria

### 6.1 A regra oficial (confirmada)

Da documentação oficial do Google (*Test Android apps for cars*):

- ✅ Para testar em **veículos reais**, o app precisa ser instalado de uma fonte confiável, como o Google Play.
- ✅ O Google indica **Internal App Sharing** ou **faixa de teste interno** para distribuir a dispositivos sem revisão.
- ✅ A opção "Fontes desconhecidas" do Android Auto vale para apps de mídia, mensagens e apps "parked", mas **não se aplica a apps feitos com a Android for Cars App Library** — que é exatamente o caso do AA Suite.

Isso coincide com o README do AA Suite: apps de template não aparecem em carros reais, mesmo com fontes desconhecidas, a menos que venham da Play Store.

✅ **No DHU (emulador no PC), apps instalados por `adb` funcionam normalmente.** O próprio AA Suite foi desenvolvido assim. Por isso o DHU serve para validar o app, mas **não** o método de instalação nem a central do Geely.

### 6.2 Distribuição limitada gratuita (Android Developer Console) — **não resolve**

- ✅ Existe, é gratuita, sem documento de identidade, para até 20 dispositivos (estudantes, hobbistas, uso pessoal). Lançada globalmente em agosto/2026.
- ✅ O objetivo dela é a **verificação de desenvolvedor para sideload** (a regra nova que bloqueia, desde 30/09/2026 em regiões selecionadas, incluindo o Brasil segundo a imprensa, a instalação de apps de desenvolvedores não verificados por lojas e arquivos APK).
- 🔍 Ela **não** torna o app "instalado pelo Google Play" e não dá acesso a teste interno/Internal App Sharing (que exigem a conta da Play Console). **Logo, não satisfaz a regra do Android Auto.**
- ✅ Instalações via **`adb` continuam isentas** dessa verificação ("não há mudanças em como o ADB funciona"). Como os testes serão via `adb`, a regra nova não nos afeta.

### 6.3 Opções para o app aparecer na central real

| # | Caminho | Custo | Confiabilidade | Risco | Observações |
|---|---|---|---|---|---|
| A | **Pedir ao autor (iAlias) para incluir seu e-mail na lista de testadores internos** dele e instalar pela Play Store | R$ 0 | ✅ alta (é o método oficial) | Baixo | Depende de o autor aceitar; você recebe o build *dele*, sem personalizações. O repositório não informa contato público (sem e-mail/site no perfil); o canal seria abrir uma *issue* no GitHub. Serve para **validar o Geely antes de investir**. |
| B | **Instalar via `adb` declarando a Play Store como instalador** (`pm install -i com.android.vending`) | R$ 0 | ❓ incerta | Baixo, reversível (desinstalar) | É o mecanismo usado por AAAD, BluMirror e similares. O BluMirror diz que "não funciona na maioria dos dispositivos" recentes; o AAAD diz que no Android 14+ a compatibilidade é incerta. 🔍 **No Android 10 do Redmi Note 8 as chances são melhores** — mais um motivo para não atualizar. Não exige root nem downgrade. Só o teste no carro confirma. |
| C | **Conta tradicional da Google Play Console + teste interno** com o build próprio | US$ 25 (taxa única) | ✅ alta (método oficial) | Baixo | Único caminho oficial com app *personalizado*. Exige autorização expressa. |
| D | **Dongle de Android Auto sem fio com "modo desenvolvedor"** (ex.: AAWireless) | Pago (centenas de reais) | 📄 declarada por MirrorMobile e AAAD como a opção mais confiável sem root | Baixo | Bônus: daria Android Auto sem fio. Fora do orçamento atual. |
| E | Root + alterar fonte de instalação (AlterInstaller etc.) | R$ 0 | Média | **Alto** | ❌ Excluído pelas restrições do escopo. |
| F | Downgrade do Android Auto | R$ 0 | — | Alto | ❌ Excluído. |

### 6.4 Conclusão da auditoria de instalação

- **Não existe método gratuito *garantido* e oficial** para o AA Suite compilado por nós aparecer na central do Geely.
- **Existem dois caminhos gratuitos plausíveis:** A (pedir ao autor) e B (adb com instalador = Play Store no Android 10).
- **O caminho pago oficial custa US$ 25** (C). Só com autorização.
- O DHU é gratuito e valida o funcionamento do app, mas não o carro.

---

## 7. Compatibilidade dos aparelhos

### 7.1 Redmi Note 8 (Android 10)

- ✅ Atende ao mínimo (Android 8) e ao recomendado (Android 10) do AA Suite.
- ✅ Atende ao DHU (exige Android 9+).
- 🔍 Android 10 favorece o caminho B (instalador declarado via adb).
- ❌ Não suporta compartilhamento de **app individual** do MediaProjection (recurso do Android 14+).
- 🔍 MIUI é agressivo com apps em segundo plano: configurar "sem restrições de bateria" e "início automático".
- **Continua: não atualizar.**

### 7.2 POCO X5 Pro (Android 15)

- ✅ Atende ao mínimo e ao recomendado do AA Suite, e ao DHU.
- 🔍 Por ser Android 14+, o sistema oferece a opção de compartilhar **um app só** na tela de consentimento de captura. ❓ Falta confirmar se a HyperOS da Xiaomi mantém essa opção.
- 🔍 Hardware bem mais forte que o do Redmi Note 8 (Snapdragon 778G, tela de 120 Hz). Bom candidato a aparelho dedicado, mas aquecimento e bateria precisam ser medidos no carro.
- 🔍 **Caminho B (instalador declarado via adb) tende a ser menos confiável aqui**: AAAD e BluMirror relatam problemas no Android 14+. Os caminhos A e C (Play Store) não são afetados.
- 🔍 Android 13+ tem as "configurações restritas": apps instalados fora da loja podem ser impedidos de ativar o serviço de acessibilidade até o usuário liberar em *Informações do app → ⋮ → Permitir configurações restritas*. Isso afeta o toque do AA Suite. Pode não se aplicar a instalações via `adb`; precisa ser testado.
- **Não atualizar** (mesma regra dos outros aparelhos).
- Pendente: anotar a versão da HyperOS, o build e a versão do app Android Auto.

### 7.3 Redmi Note 11

- Confirmar versão do Android (Configurações → Sobre o telefone).
- Se for Android 14+, ganha o compartilhamento de app individual, mas o caminho B tende a ser menos confiável.

### 7.4 Estratégia de teste com dois aparelhos

- **Redmi Note 8 (Android 10):** melhor aposta para o caminho gratuito B.
- **POCO X5 Pro (Android 15):** melhor aposta para desempenho e seleção de app individual, desde que o app chegue pela Play Store (caminhos A ou C).
- Testar o mesmo build nos dois, no DHU primeiro, separa o que é limitação do app do que é limitação do aparelho.

### 7.5 Critérios para o aparelho dedicado (inalterados)
Versão do Android, desempenho, estabilidade, aquecimento, carregamento contínuo, bateria, fixação/conexão, custo.

---

## 8. Alternativas avaliadas

### 8.1 Comparativo

| Projeto | Faz o quê | Licença | Toque | Situação | Instalação no carro |
|---|---|---|---|---|---|
| **AA Suite** | Espelho + navegador + YouTube TV + envio de links | MIT | ✅ via acessibilidade | Ativo (out/2026), muito novo | Teste interno da Play (autor); sem release |
| **BluMirror** (BluDood) | Espelho de tela inteira **ou de um app** | MIT | Não mencionado | 1 commit, sem releases visíveis | Loja própria "BluApps" (modo de compatibilidade, não auditado), APK, ou adb com instalador declarado |
| **MirrorMobile** (chenxiaolong) | Espelho | GPL-3.0 | ❌ sem toque | **Arquivado** em set/2026 | Play Store, root ou dongle com modo dev; **pausa com o carro em movimento** |
| **AAStream** | Espelho | — | — | — | ❌ exige root |
| **ScreenOnAuto** | Espelho, auto-início, atalhos | Não confirmada (repo só de APKs) | ? | ? | Não auditado |
| **AAAD** (instalador) | Instala apps não oficiais (Screen2Auto, AA Mirror Plus, CarStream, AA Browser, Fermata Auto…) no celular, sem PC nem root | Licença ambígua (EULA restritivo vs. MIT no GitHub) | — | 3,1 mil estrelas, mantido | Grátis: 1 download a cada 30 dias; PRO: € 3,99. Usa o mesmo mecanismo do caminho B. Android 14+: incerto. |

### 8.2 Leitura

- O **AA Suite continua sendo o melhor ponto de partida**: é o único que reúne navegador e espelhamento com toque, tem licença MIT e código limpo para personalizar.
- O **AAAD** é interessante como **teste rápido e gratuito do caminho B** sem PC: se um app de espelhamento instalado por ele aparecer no Geely, isso é forte evidência de que o mecanismo de "instalador = Play Store" funciona nesta central com este celular. Mas: binário de terceiros, licença restritiva e o limite de 1 download/30 dias. **Não instalar sem autorização.**
- O **BluMirror** é a referência técnica para a seleção de **app individual** (no Android 14+).

### 8.3 Seleção de apps individuais

- ✅ O Android 14+ permite que o usuário escolha compartilhar só um app na tela de consentimento do MediaProjection (é o que o BluMirror usa).
- 🔍 Implementável no AA Suite como desenvolvimento futuro, **somente em aparelhos Android 14+**. No Redmi Note 8 (Android 10) não é possível; no **POCO X5 Pro (Android 15)** é possível em tese (❓ confirmar se a HyperOS mantém a opção).

---

## 9. Conectividade sem fio

- ❓ Não se sabe se a central do Geely suporta Android Auto sem fio nativo.
- 🔍 Se não suportar, o caminho seria um dongle (pago; ver opção D, que ainda resolveria a instalação).
- O AA Suite em si não depende de USB ou Wi-Fi: funciona sobre o que o Android Auto estiver usando.

---

## 10. Plano de trabalho

### Fase 1 — Auditoria documental ✅ concluída em 22/10/2026

- [x] Ler README completo do AA Suite.
- [x] Verificar licença, releases, atividade e issues.
- [x] Confirmar requisitos de SDK, JDK, Gradle e dependências.
- [x] Entender como o app se registra no Android Auto.
- [x] Confirmar procedimento para teste em carro real.
- [x] Investigar distribuição limitada gratuita (não resolve).
- [x] Avaliar o DHU (viável; valida o app, não o carro).
- [x] Comparar alternativas.
- [x] Registrar confirmado × incerto.
- [x] Verificar o que já existe no PC (somente leitura).

### Fase 2 — Preparação do computador ✅ concluída em 23/10/2026

Decisão do usuário (23/10/2026): seguir pelos caminhos gratuitos A e B; SDK de linha de comando (sem Android Studio); licenças do Android SDK aceitas pelo usuário.

- [x] SDK de linha de comando em `C:\Users\Victor\AppData\Local\Android\Sdk` (checksum SHA-1 do zip conferido com o índice oficial do Google).
- [x] Pacotes instalados: `cmdline-tools` 23.0, `platform-tools` 37.0.1 (`adb`), `platforms/android-36`, `build-tools` 34.0.0 e 36.0.0, `extras/google/auto` 2.0 (DHU).
- [x] JDK 21 existente usado só durante o build (PATH e `JAVA_HOME` do sistema não foram alterados).
- [x] Repositório clonado em `AASuite/`, commit **`e449095`** (17/10/2026), sem nenhuma alteração. Único arquivo criado: `local.properties` (aponta para o SDK; ignorado pelo Git).
- [x] Build original: **BUILD SUCCESSFUL em 2 min 8 s**; 132 testes unitários (variantes debug + release), 0 falhas.
- [x] APKs gerados: `AASuite/app/build/outputs/apk/debug/app-debug.apk` (3,9 MB) e `AASuite/companion/build/outputs/apk/debug/companion-debug.apk` (0,8 MB).
- [x] Log: `logs/build_original.log`.
- [x] Pedido ao autor (caminho A) redigido em `pedido_autor_issue.md` — **publicação pelo usuário**.

Notas para repetir o build:
- O `sdkmanager` foi substituído pela nova **Android CLI** (`cmdline-tools\latest\bin\android.exe`). Os nomes de pacote usam `/` em vez de `;` (ex.: `android sdk install platforms/android-36`).
- Neste PC o `cmd` não executa o `gradlew.bat` da pasta atual pelo nome. Usar o script pelo Git Bash:
  ```bash
  cd AASuite
  JAVA_HOME="/c/Program Files/Java/jdk-21" PATH="/c/Program Files/Java/jdk-21/bin:$PATH" ./gradlew assembleDebug :companion:assembleDebug test
  ```
  (No PowerShell, `.\gradlew.bat` com `$env:JAVA_HOME` apontando para o JDK 21 também deve funcionar.)

### Caminho B — instalação no Redmi Note 8 (23/10/2026, autorizada pelo usuário)

- [x] `adb install -i com.android.vending app-debug.apk` → **Success** (20:57:29).
- [x] APK instalado: commit `e449095`, versão 0.9 (`versionCode 9`), SHA-256 `3e0eed1ad0a7b6673f369a4ed7212fea412ae0cb9e75aadb35abcabb355ec34a`.
- [x] Instalador registrado: ✅ **`installerPackageName=com.android.vending`** (o Android considera que o app veio da Play Store).
- ❓ O app é um build *debug* (flag `DEBUGGABLE`). Não se sabe se o Android Auto rejeita apps depuráveis. Se o app não aparecer no carro, a próxima tentativa gratuita é um build *release* assinado com uma chave própria, instalado do mesmo jeito.
- [x] Modo desenvolvedor do Android Auto ativado (23/10/2026). Neste aparelho as configurações do Android Auto não têm atalho; abrir pelo cabo com `adb shell am start -n com.google.android.projection.gearhead/.companion.settings.DefaultSettingsActivity`.
- [x] Fontes desconhecidas: ligada pelo usuário (não verificado por nós).
- [x] 🔍 **"AA Suite" aparece e está marcado em "Personalizar acesso rápido"** do Android Auto. É um bom sinal de que o Android Auto reconheceu o app, mas não prova que ele vai aparecer na central.
- [x] Permissões verificadas via `adb` (23/10/2026): serviço de acessibilidade ✅ ativo; exibir sobre outros apps ✅; modificar configurações do sistema ✅; bateria sem restrições ✅ (na lista de exceções do Doze). Captura de tela: concedida a cada sessão, na hora do teste.
- Observação: a interface do AA Suite no celular está **em italiano** (sem tradução para português) → item para a Fase 7.
- [x] DHU conectado (23/10/2026), com o AA Suite visível no menu do Android Auto emulado (confirmado pelo usuário). Isso **não prova** nada sobre a central do Geely: no DHU, apps instalados via adb sempre aparecem.
  - Problemas resolvidos no caminho: (1) o DHU fecha sozinho se o console dele ficar sem entrada → rodar com `tail -f /dev/null | desktop-head-unit.exe -c dhu/headunit.ini`; (2) DHU 2.0 (2022) trocado pelo 2.1 (`android sdk install --beta extras/google/auto`); (3) o servidor da unidade principal travou com `IllegalStateException: Already connected` depois da primeira tentativa → `adb shell am force-stop com.google.android.projection.gearhead` e iniciar o servidor de novo.
  - Configuração usada: `dhu/headunit.ini` (1280×720, toque, 160 dpi).
- [x] Teste no DHU (23/10/2026, Redmi Note 8): **navegador, espelhamento, toque, botões e envio de URL pelo celular funcionaram**, segundo o usuário. O "fill screen" fica na engrenagem (⚙) do canto superior direito da tela inicial do AA Suite no carro: "Impostazioni" → "Riempi schermo (mirroring)". Áudio: não avaliado no DHU.
- [x] ✅ **Teste na central do Geely EX2 Pro (24/10/2026, informado pelo usuário): o AA Suite APARECEU na central e as opções funcionaram.** Aparelho: Redmi Note 8, Android 10, Android Auto 17.9, build *debug* 0.9 (commit `e449095`) instalado com `adb install -i com.android.vending`. **O caminho B funciona neste conjunto, a custo zero.** Não foi preciso o build *release* nem a Play Console.
  - Detalhes (informados pelo usuário em 24/10/2026):
    - **Sem internet** (garagem sem sinal): o app apareceu e funcionou mesmo assim. ✅ A visibilidade pelo caminho B **não depende de checagem online**.
    - **Toque:** ✅ funcionou.
    - **Proporção:** ✅ sem faixas pretas com o celular deitado. A tela da central acompanha a rotação do celular. O fill screen não foi encontrado (fica na ⚙ da tela inicial do AA Suite no carro), e a trava horizontal ("Blocco rotazione orizzontale") fica no mesmo lugar.
    - **Latência:** "não demorou muito" (aceitável, sem medição).
    - **Pendente:** áudio (não testado), aquecimento (não observado), fotos e resolução da central, navegador e YouTube com internet.
- Para desfazer: `adb uninstall com.viami.aamirror`.

### Fase 3 — Teste no computador (DHU)

- [ ] Celular de teste com depuração USB → `adb install`.
- [ ] Android Auto no celular: modo desenvolvedor → "Iniciar servidor da unidade principal".
- [ ] `adb forward tcp:5277 tcp:5277` → `desktop-head-unit.exe`.
- [ ] Configurar `headunit.ini` com a resolução da central do Geely (a descobrir).
- [ ] Testar navegador, espelhamento, toque, orientação.
- [ ] Documentar o que o DHU não comprova (instalação real, central do Geely, áudio no carro).

⚠️ O DHU exige o app Android Auto atualizado no celular. Atualizar o **app** Android Auto pela Play Store é diferente de atualizar o **Android**, mas ainda assim é uma alteração: pedir autorização antes.

### Fase 4 — Teste controlado no Android
(Inalterada.) Backup, instalar só após definir o caminho, permissões mínimas, testar navegador, espelhamento, offline, registrar erros.

### Fase 5 — Teste na central do Geely (sempre estacionado)
(Inalterada.) Acrescentar: testar o caminho de instalação escolhido (A, B ou C) e registrar qual funcionou.

### Fase 6 — Comparação e aparelho dedicado (inalterada)

### Fase 7 — Personalização (liberada em 24/10/2026, depois do teste no carro)
Prioridades identificadas pela auditoria: abas, histórico, botão avançar, tema escuro, seleção de app individual (Android 14+), persistência da trava de paisagem, início do navegador sem toque.

**Design aprovado (24/10/2026):** estilo iOS/CarPlay escuro ("true-black flat", escolhido pela taste-skill), fonte Inter, cartões arredondados, botão principal em pílula, listas agrupadas e chaves no estilo iPhone, tudo em português.
- Prévia aprovada: https://claude.ai/artifact/SmKEYJckPH36zK4oJiDfuu
- Canvas de teste de cores e temas: https://claude.ai/artifact/WXWfcxmfow121RHPwf8F9N

**Seletor de temas (pedido do usuário, 24/10/2026):**
- O usuário escolhe o tema na tela do celular (um item "Tema", com bolinhas coloridas).
- Seis temas: **Meia-noite** (padrão), **Grafite azul**, **Âmbar noturno**, **Floresta**, **Coral esportivo** e **Dia (claro)**. Os valores de cor estão no canvas de teste.
- Mais o modo **Automático**: Dia (claro) quando a central está no modo dia, e o tema escuro escolhido no modo noite (via `CarContext.isDarkMode()`).
- Vale para a tela do celular e para a página inicial do navegador. Os menus do carro e os botões da lateral são do Android Auto e seguem o claro/escuro da central.
- Custo: baixo. As cores ficam centralizadas e a escolha é salva no celular junto com as outras configurações. Sem impacto de desempenho.

**Ajustes de design pedidos pelo usuário (24/10/2026):**
- **Nome do app: RoadFlix.** Substitui "AA Suite" no celular, no menu do carro e no nome que aparece no Android Auto (`app_name`). O pacote interno (`com.viami.aamirror`) não muda, para não precisar reinstalar nem refazer as permissões.
- **Ícones dos sites automáticos** nos favoritos da página inicial: o app busca o ícone de cada site sozinho e guarda no celular, para continuar aparecendo sem internet. Fontes: o ícone que o próprio site declara (`apple-touch-icon`/`favicon`), com o serviço de favicons do Google como reserva; sem ícone, mostra a inicial do nome.
- **Menu do carro com ícones centralizados e maiores.** No Android Auto real, o menu em grade já mostra o ícone centralizado acima do nome; o tamanho final é decidido pelo Android Auto.
- Canvas de teste com as alterações e as escolhas de tema do usuário: https://claude.ai/artifact/WXWfcxmfow121RHPwf8F9N

**Ícone do app (24/10/2026):** imagem gerada pelo usuário num gerador de imagens a partir do conceito "Destino" (estrada vermelha até o horizonte), usada como está, com o formato arredondado ("squircle") que o usuário pediu para manter.
- Arquivos em `icone/`: `roadflix_original.webp` (original), `roadflix_arredondado_1024.png` (cantos transparentes, sem a borda cinza) e `roadflix_quadrado_1024.png` (sem cantos, para usos que aplicam o próprio recorte).
- No app (branch `roadflix/icone`, **ainda sem commit**): PNGs em `mipmap-mdpi` até `mipmap-xxxhdpi` (48 a 192 px).
- **Correção (24/10/2026):** sem ícone adaptativo, o celular/Android Auto colocaram o ícone dentro de uma **borda branca**. Voltamos ao ícone adaptativo: fundo preto + `ic_launcher_foreground.png` (arte a 90 de 108 dp, 108 a 432 px). O formato final passa a ser o do launcher (quadrado arredondado na MIUI, círculo no Android Auto), sem borda branca. Prévia: `icone/previa_launcher_e_android_auto.png`.
- Observação: o Android Auto costuma mostrar os ícones dos apps dentro de um círculo; lá o formato arredondado não aparece igual.

**RoadFlix instalado ao lado do AA Suite (24/10/2026, a pedido do usuário, para comparar antes e depois):**
- O build *debug* da branch `roadflix/icone` usa `applicationIdSuffix = ".roadflix"` → app separado `com.viami.aamirror.roadflix`, com nome "RoadFlix" e ícone novo. O AA Suite original (`com.viami.aamirror`) continua instalado, sem alterações.
- Instalado no Redmi Note 8 com `adb install -i com.android.vending` → `installer=com.android.vending` (caminho B).
- O RoadFlix precisa das próprias permissões e de ser ativado em "Personalizar acesso rápido" do Android Auto. Usar um app de cada vez: os dois abrem o servidor de links (AA Share) na mesma porta 8977.

**Ordem de implementação proposta:**
1. ✅ **Concluída em 24/10/2026:** tela do celular nova (português, visual aprovado) **com o seletor de temas**. Seis temas + Automático (por enquanto segue o modo claro/escuro do celular; no carro vai seguir o dia/noite da central). Permissões com status "Ativo"/"Ativar", envio de link, favoritos e passo a passo do Android Auto. Notificação e tela de favoritos em português. Barras do sistema seguem o tema. Lógica dos temas coberta por testes (`AppThemeTest`, 70 testes no total, 0 falhas). Testada pelo usuário no Redmi Note 8.
   - Detalhe conhecido: logo após reinstalar, "Toque pela central" pode mostrar "Ativar" por alguns segundos, até o Android religar o serviço de acessibilidade.
2. ✅ **Concluída em 24/10/2026:** página inicial local do navegador (relógio, data, barra de pesquisa que abre o teclado do Android Auto, favoritos em blocos) seguindo o tema; no modo Automático acompanha o dia/noite da central (`CarContext.isDarkMode`). Ícones dos favoritos buscados automaticamente (`apple-touch-icon` do site, com o serviço de favicons do Google como reserva) e guardados no celular para funcionar sem internet; sem ícone, mostra a inicial. "Navegador" abre direto a página inicial (a lista de favoritos do Android Auto foi removida). Botões da lateral: menu, voltar, avançar e início. 78 testes, 0 falhas. Testada pelo usuário no DHU (favoritos, botões e pesquisa).
3. ✅ **Concluída em 24/10/2026:** textos e ícones dos menus do carro em português (menu, configurações e mensagens da central, que agora apontam para os nomes da tela nova do celular). Ícones novos: celular para "Espelhar" e tela com play para "YouTube". O Android Auto guarda os ícones dos apps em cache: um ícone trocado precisa de um **nome de recurso novo** (`ic_mode_mirror`, `ic_mode_youtube`) para aparecer. Conferida pelo usuário no DHU.
4. ✅ **Concluída em 24/10/2026:** coluna "Recentes" na página inicial do carro (5 últimos sites, com horário "14:10" / "Ontem" / data), histórico guardado só no celular (até 50, sem duplicar), destaque "Enviado do celular há N min · Abrir" por 2 horas para links do botão "Abrir no carro" ou do AA Share, e "Limpar histórico" na tela do celular (com confirmação; favoritos ficam). 89 testes, 0 falhas. Testada pelo usuário no DHU.
   - Detalhe conhecido: voltar à página inicial pelo botão "voltar" pode mostrar os Recentes de antes; o botão "início" sempre mostra a lista atual.
5. Abas.

---

## 11. Critérios de sucesso

### Sucesso mínimo de auditoria ✅ atingido
- Requisitos e limitações do AA Suite documentados.
- Método de instalação verificado em fonte oficial atual.
- Alternativas investigadas.
- Nada instalado nem atualizado.

### Sucesso técnico inicial / no veículo
(Inalterados em relação à v1.2.) A meta completa só conta após demonstração no veículo real.

## 12. Segurança, privacidade e uso responsável

(Inalterado.) Acrescentar:
- O serviço de acessibilidade do AA Suite consegue tocar em qualquer lugar do celular. Conceder só no aparelho de teste/dedicado.
- O AA Share abre um servidor HTTP sem autenticação na porta 8977 enquanto o app está ativo. Usar apenas no hotspot do próprio celular, nunca em Wi-Fi público.
- Binários de terceiros (AAAD, BluApps, APKs de releases) só com autorização e baixados da página oficial.

---

## 13. Decisões pendentes do usuário

1. **Caminho de instalação para o carro real:** tentar primeiro os gratuitos, A (pedir ao autor) e/ou B (adb com instalador = Play Store)? Ou aceitar já o C (US$ 25)?
2. **Fase 2:** autoriza instalar ferramentas no PC? Android Studio completo (mais fácil, ≈6–8 GB) ou só o SDK de linha de comando (≈2–4 GB)?
3. **DHU:** autoriza atualizar o **app** Android Auto (não o Android) no Redmi Note 8, se for necessário para o DHU?
4. **AAAD como teste rápido do caminho B:** considerar ou descartar?
5. Informações que ainda faltam: versão do app Android Auto no Redmi Note 8 e no POCO X5 Pro; versão da HyperOS/build do POCO X5 Pro; versão do Android do Redmi Note 11; se a central já funcionou sem fio; resolução da tela da central (se souber).

## Ideias (fora do escopo, para avaliar depois)

Registros de ideias do usuário que **não** fazem parte do escopo atual. Só entram no plano por decisão explícita.

- **Abas "Filmes", "Séries" e "Canais"** na página inicial do navegador (23/10/2026), com atalhos para fontes online **legais**: Pluto TV, YouTube, emissoras públicas (TV Brasil, TV Cultura), Libreflix, vídeos próprios e um servidor caseiro (Jellyfin/Plex). Limitações: serviços com DRM (Netflix, Prime, Disney+, Max, Globoplay pago) não tocam no navegador e ficam com tela preta no espelhamento. Fontes piratas ou IPTV sem licença ficam de fora.
- **Trava de vídeo e espelhamento em movimento** (23/10/2026): o AA Suite original não tem nenhuma trava ligada a carro em P ou em movimento; só o teclado do Android Auto é bloqueado pelo próprio Android Auto. O usuário decidiu **manter o comportamento atual** por enquanto. Se for implementar: depende de a central do Geely repassar a velocidade ao Android Auto (verificar no teste).

## 14. Fontes consultadas (22/10/2026)

- AA Suite — repositório, README, `AndroidManifest.xml`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle-wrapper.properties`, `store/scheda-play.md`, histórico de commits: https://github.com/iAlias/AASuite
- Google — Test Android apps for cars: https://developer.android.com/training/cars/testing
- Google — Desktop Head Unit: https://developer.android.com/training/cars/testing/dhu
- Google — Android developer verification FAQ: https://developer.android.com/developer-verification/guides/faq
- Google — Understanding Android developer verification: https://support.google.com/android-developer-console/answer/16561738
- Google — blog sobre o cronograma da verificação: https://android-developers.googleblog.com/2026/06/android-developer-verification.html
- Help Net Security — mercados e datas da verificação: https://www.helpnetsecurity.com/2026/06/19/android-developer-verification-rollout-markets/
- MirrorMobile: https://github.com/chenxiaolong/MirrorMobile
- BluMirror: https://github.com/BluDood/BluMirror
- AAAD: https://github.com/shmykelsa/AAAD
- AAStream: https://github.com/endyrubbin/AAStream
- ScreenOnAuto: https://github.com/slzn/ScreenOnAuto-releases
- XDA — mecanismo do AAAD: https://www.xda-developers.com/install-unapproved-apps-for-android-auto/

---

## Histórico de decisões

- **24/10/2026:** projeto publicado no GitHub do usuário como repositório **público e independente** (não é fork): https://github.com/VictorScholzx/roadflix (branch `main`). Mantém o histórico e a licença MIT do AA Suite, com créditos a iAlias no README. Commits com o e-mail noreply do GitHub. Remotos locais: `origin` = roadflix e `upstream` = iAlias/AASuite. O escopo publicado fica em `docs/roadflix/escopo.md` (sem o número de série do celular).

- **24/10/2026:** como o caminho B funcionou, o caminho A deixou de ser necessário. A issue https://github.com/iAlias/AASuite/issues/2 foi **fechada sem comentário**, a pedido do usuário.

- **24/10/2026:** 🎉 **Marco principal atingido.** O AA Suite original apareceu e funcionou na central original do Geely EX2 Pro via caminho B (Redmi Note 8, Android 10), sem custo. Próximas etapas: detalhar o resultado (áudio, toque, proporção) e começar a Fase 7 (personalização), que o usuário tinha deixado para depois deste teste.

- **23/10/2026:** DHU validado com o Redmi Note 8. Usuário achou a interface feia e quer personalizar, mas decidiu **esperar o teste no carro** antes. Prioridades de visual quando a Fase 7 começar: (1) tela do celular em português e com tema escuro; (3) página inicial escura e automotiva no navegador, com atalhos, barra, abas e histórico. Os menus do carro (2) são desenhados pelo Android Auto: dá para mudar só ícones, textos e ordem.

- **23/10/2026:** pedido ao autor (caminho A) publicado pelo usuário (conta VictorScholzx): https://github.com/iAlias/AASuite/issues/2. Aguardando resposta; se não houver em 1–2 semanas, seguir só com o caminho B.

- **23/10/2026:** usuário escolheu os caminhos gratuitos A e B e autorizou a Fase 2. SDK de linha de comando instalado, licenças aceitas a pedido do usuário, versão original compilada com sucesso (commit `e449095`).

- **23/10/2026:** o POCO é um **POCO X5 Pro com Android 15** (antes se supunha um POCO C51). Adicionado como segundo aparelho de teste: forte para desempenho e app individual, fraco para o caminho B.

- **v1.2 → v1.3 (22/10/2026):** Fase 1 concluída. Nome do projeto: android-auto-ex2. Confirmado que apps de template da Car App Library (caso do AA Suite) só aparecem em carro real se instalados pela Play Store; "fontes desconhecidas" não se aplica. Distribuição limitada gratuita descartada como solução para o Android Auto. Levantados caminhos gratuitos A e B e o pago C. PC verificado: Windows 11, JDK 21 utilizável, sem Android SDK.
- Escopo funcional, uso, visual, conectividade, aparelho dedicado e orçamento: inalterados.

*Documento vivo. Atualizar após cada verificação ou teste, separando sempre os requisitos do usuário, as declarações da documentação e os resultados observados.*
