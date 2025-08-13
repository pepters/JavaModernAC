# ModernAC — Production‑ready combat/aim anticheat for Paper 1.16.5

> Лёгкий, асинхронный античит, сфокусированный **только** на бою: aimbot/aimassist/killaura/trigger‑bot. Минимум Bukkit API, пакетный пайплайн на **PacketEvents v2**.

## ✨ Возможности
- **Современные детекции прицела/килауры**: компоненты гладкости/синхронизации/энтропии, GCD/константность, статистика по окнам (25/100/1000 тик), эвристики для *Nursultan/LiquidBounce* (PID/tail, frame‑lock, tween/pre‑aim и т. п.).
- **Адаптивные наказания**:
  - *CRITICAL*: 30–60 с (быстрое наказание для почти безошибочных сигналов);
  - *HIGH*: 2–4 мин; *MEDIUM*: 4–7 мин.
  - Эскалация/демоут с учётом «soft» условий (высокий пинг/низкий TPS).
- **Митигейшн вместо мгновенного бана**: динамическое снижение урона (до 90%) по логистике; задержка применения 5–15 с; длительность 60–120 с; авто‑продление при новых алёртах.
- **Устойчивая система алёртов для стаффа**: задержка 5–10 с, батч‑окно 2 с, rate‑limit 1 алёрт/3 с на игрока. Форматируемые сообщения, цвет‑коды `&`.
- **Latency‑aware**:
  - группа **LATENCY_ABUSE** (blink/queue‑spoof/узкая спектральность ACK) — влияет на митигейшн, но не банит;
  - «soft‑mode» по `ping` и `TPS` снижает агрессию и блокирует быстрые наказания.
- **Производительность**: события через PacketEvents, минимум Bukkit API, планировщики и буферы для тяжёлых вычислений.
- **Готов к продакшену**:
  - зависимости как `provided`, **ничего не шейдится**;
  - дружелюбная конфигурация без «магических» значений;
  - `messages.yml` для локализации;
  - детальный файл‑лог (опционально).

## ⚙️ Совместимость
- Сервер: **Paper/Purpur 1.16.5** (совместим с ViaVersion).
- Java: **17** (плагин собран под 17).
- Обязательная зависимость (в рантайме): **PacketEvents v2.9.x** (pre‑built plugin).

## 📦 Установка
1. Установите плагин **PacketEvents v2.9.x** (как обычный плагин).
2. Скопируйте `ModernAC.jar` в `plugins/` и запустите сервер.
3. Настройте `config.yml` и `messages.yml` (создаётся автоматически).

> В `plugin.yml` у ModernAC указано `depend: [packetevents]`, так что сервер сам обеспечит порядок загрузки.

## 🚀 Quick-Start
1. Скачайте PacketEvents v2.9.x и положите в `plugins/`.
2. Поместите ModernAC.jar в `plugins/`.
3. Запустите сервер и настройте `config.yml`/`messages.yml`.

## 🧰 Команды
| Команда | Описание | Право |
|---------|----------|-------|
| `/ac info [ник]` | показать метрики/таймеры игрока | `ac.command.info` |
| `/ac debug <ник>` | включить/выключить подробные алёрты для игрока | `ac.command.debug` |
| `/ac exempt <ник> <время>` | временно исключить игрока (`10s`, `5m`, `1h`) | `ac.command.exempt` |
| `/ac reload` | перечитать конфиг/сообщения и перезапустить таймеры | `ac.command.reload` |
| `/ac devfake <ник>` | (dev) сгенерировать тестовый сигнал | `ac.use` |

### Права
| Право | Назначение |
|-------|------------|
| `ac.use` | базовый доступ к `/ac` (по умолчанию: OP) |
| `ac.alerts` | получать алёрты |
| `ac.command.info` | использование `/ac info` |
| `ac.command.debug` | использование `/ac debug` |
| `ac.command.exempt` | использование `/ac exempt` |
| `ac.command.reload` | использование `/ac reload` |

## 🧪 Как это работает (в двух словах)
```
PacketEvents → PacketListenerImpl → CheckManager → DetectionEngine
                                   ├→ AlertEngine  (отложенные/батч‑алёрты)
                                   ├→ PunishmentManager (адаптивные таймеры)
                                   └→ MitigationManager (снижение урона)
```
- **Окна анализа**: SHORT (~25 тиков), ANALYSIS (~100 тиков), LONG (~1000 тиков).
- **Семейства детекций** (AIM/LB/NR/...): внутри — максимум по окнам; между — нужно ≥2 независимых семейства (и, опционально, ≥2 окна).
- **Soft‑mode** активируется при `ping > unstable_connection_latency_limit` или `TPS < tps_soft_guard` — снижает tier и запрещает «быстрый бан».
- **LatencyAbuse** даёт вес на митигейшн, но не может банить напрямую.

## 🛠 Конфигурация (основное)
`config.yml` (фрагменты):
```yml
commands:
  kick: "kick {player} Unfair combat advantage"
  ban:  "ban {player} 7d Unfair combat advantage"

latency:
  unstable_connection_latency_limit: 1750   # ms
  tps_soft_guard: 18.0

checks:
  experimental_detections: true
  combat_tolerance: "normal"

policy:
  min_independent_families_for_ban: 2
  require_multi_window_confirmation: true

mitigation:
  max_damage_reduction: 0.90           # 90%
  apply_delay_seconds: [5, 15]
  duration_seconds: [60, 120]

punishments:
  adaptive: true
  tiers:
    CRITICAL: { delay_seconds: [30, 60] }
    HIGH:     { delay_minutes: [2, 4] }
    MEDIUM:   { delay_minutes: [4, 7] }

alerts:
  staff_permission: "ac.alerts"
  delay_seconds: [5, 10]
  rate_limit_per_player_seconds: 3
  batch_window_seconds: 2

logging:
  detections_debug: true
```
`messages.yml` (фрагменты):
```yml
alerts:
  staff_format: "&8[&cAC&8] &7{player} &8→ &c{families}&8 @ &7{confidence}% &8(&7ping {ping}ms, tps {tps}&8)"
  debug_format:  "&8[&bAC-Debug&8] &7{player}: &c{families}&8 @ &7{confidence}% &8(&7ping {ping}ms, tps {tps}&8)"
commands:
  reloaded: "&aConfiguration reloaded."
  no_permission: "&cYou do not have permission."
  usage: "&cUsage: /ac <status|debug|exempt|reload|devfake>"
```

## 🧭 Поведение наказаний и митигейшна
- **Эскалация tier’ов**: при появлении более «строгого» сигнала старый таймер отменяется и ставится новый, более короткий.
- **Soft‑демоут**: при `ping/TPS` вне норм CRITICAL→HIGH, HIGH→MEDIUM.
- **Митигейшн**:
  - применяет снижение урона с задержкой (рандом в заданном диапазоне),
  - если игрок офлайн — попытка применить повторно,
  - авто‑снятие по истечении длительности или при сбросе детекций.

## 🧩 Расширение (добавить новый чек)
1. Реализуйте интерфейс/базовый класс `Check` в своём пакете (минимум зависимостей на Bukkit).
2. Публикуйте результат через `DetectionEngine.record(check, DetectionResult)` — задайте `family`, `window`, `evidence`, флаги `latencyOK/stabilityOK`.
3. Зарегистрируйте чек в `CheckManager` и добавьте tier в `checks_registry` конфига.
4. Нестабильные пилотные детекции помечайте `experimental` — они не будут банить.

## ❓ FAQ
**Почему нет перемещений/velocity?**  
Фокус — на боевых читах. Для перемещений можно использовать, например, Grim (совместим).

**Нужен ли ViaVersion?**  
Не обязателен, но поддерживается. Детекции выстроены через PacketEvents и не зависят от версии клиента.

**PacketEvents где брать?**  
Нужен pre‑built плагин v2.9.x. ModernAC не шейдит его внутрь, это осознанно.

**Билд падает на PacketEvents?**  
Проверьте POM: `packetevents-spigot:2.9.x` и репозитории CodeMC. Не добавляйте исходники чужих пакетов в проект.

## 🧱 Сборка из исходников
```bash
# Java 17
mvn -DskipTests package
```
Артефакт: `target/ModernAC-<version>.jar`

## 🗺 Roadmap (кратко)
- Доп. эвристики для Nursultan/«гладких» нейронных читов (адаптация под новые рандомайзеры).
- Тонкая самокалибровка baseline профиля (персональные Z‑оценки с дрейфом).
- Веб‑экспорт алёртов (Webhook) — опционально.

---

**ModernAC** стремится к минимальному числу ложных, поэтому часть детекций работает консервативно. Рекомендуется наблюдать за алёртами и постепенно ужесточать политику — без резких движений.

