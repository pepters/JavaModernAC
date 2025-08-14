# ModernAC — античит для PvP (Paper/Purpur 1.16.5–1.21.4)

ModernAC — лёгкий боевой античит, нацеленный на **PvP** (aim/killaura/triggerbot и родственные паттерны).
Работает поверх **PacketEvents 2.9.x** (как отдельный плагин), использует **уровни уверенности (tiers)** — `MEDIUM / HIGH / CRITICAL` и подтверждение по нескольким окнам (≈25/100/600 тиков). Консоль по умолчанию чистая — только алёрты и ошибки.

- **Серверы:** Paper/Purpur **1.16.5–1.21.4**
- **Java:** сборка на **Java 17** (ок — запуск на серверах Java 21)
- **Зависимость в рантайме:** **PacketEvents 2.9.x** (*не шейдится*, должен быть установлен как плагин)

---

## ✨ Возможности

- **Проверки боя** (семейства aim/killaura/triggerbot). Aggregation через несколько окон (≈25/100/600 тиков) с политикой `tiers` — быстрые алёрты на сильных краткосрочных сигналах, устойчивость на длинной дистанции.
- **Учёт задержек**: гейты по ping/TPS/LOS, чтобы снизить ложные срабатывания при нестабильных клиентах/сервере.
- **Alert Engine**: задержка, батчинг и rate‑limit на игрока — читаемые уведомления для персонала.
- **Mitigation Engine**: снижение урона у флагнутых игроков вместо агрессивных наказаний «с ходу».
- **Политика логирования**: консоль — старт/стоп, алёрты, ошибки; детальный трейс — **только в файл** через `DetectionLogger` и по флагу (можно включить точечно на игрока).
- **AutoTotem (experimental)**: коррелирует offhand‑swap / клики слота 45 / «воскрешение» с почти летальным уроном. По умолчанию — **только алёрты**, выключено.
- **Без NMS**: PacketEvents + Bukkit/Paper API; один JAR для нескольких версий.
- **Стиль кода**: CI принуждает **Spotless** (google‑java‑format).

---

## ✅ Совместимость

- **Paper/Purpur:** 1.16.5 – 1.21.4
- **Java:** build 17; запуск на Java 21 — ок
- **Нужно в плагинах:** PacketEvents 2.9.x

`plugin.yml` содержит `depend: [packetevents]` — порядок загрузки корректный.

---

## 🚀 Установка

1. Положите **PacketEvents 2.9.x** в `plugins/`.
2. Положите **ModernAC.jar** в `plugins/`.
3. Запустите сервер один раз для генерации конфигов.
4. Отредактируйте `config.yml` (и при необходимости локализацию в `messages.yml`).

### Быстрый старт
- Оставьте `experimental_detections: false` для начала (прод).
- Выдайте персоналу право `ac.alerts`.
- Для точечного расследования используйте `/ac debug <ник>` — включает **file‑trace только для этого игрока**.

---

## 🧰 Команды и права

| Команда | Описание | Permission |
|---|---|---|
| `/ac info [ник]` | Снимок игрока: ping/TPS, сводка по окнам/таймерам, TTL `exempt` | `ac.command.info` |
| `/ac debug <ник>` | Вкл/выкл детальный file‑trace для игрока | `ac.command.debug` |
| `/ac exempt <ник> <время>` | Временно исключить игрока (напр. `10s`, `5m`, `1h`) | `ac.command.exempt` |
| `/ac reload` | Перезагрузка конфигов/сообщений, рестарт таймеров | `ac.command.reload` |
| `/ac devfake <ник>` | (dev) синтетический сигнал для тестов | `modernac.dev` |

Права: `ac.use`, `ac.alerts`, `ac.command.info`, `ac.command.debug`, `ac.command.exempt`, `ac.command.reload`, `modernac.dev`.

---

## ⚙️ Конфиг (общее)

ModernAC использует **tiers** (`MEDIUM/HIGH/CRITICAL`) и подтверждение несколькими окнами. В `config.yml` вы встретите:

```yml
latency:
  unstable_connection_latency_limit: 1750   # ms (порог «мягкого режима»)
  tps_soft_guard: 18.0                      # TPS-порог для жёстких действий

checks:
  experimental_detections: false            # для прод-старта лучше off
  combat_tolerance: "normal"                # normal | lenient | aggressive

policy:
  min_independent_families_for_action: 2    # требовать ≥2 независимых семейств
  require_multi_window_confirmation: true   # подтверждение по окнам включено
  # Алёрты обычно на HIGH; наказания — на CRITICAL при здоровых ping/TPS.

mitigation:
  max_damage_reduction: 0.90
  apply_delay_seconds: [5, 15]
  duration_seconds: [60, 120]

punishments:
  adaptive: true
  # Под капотом — tiers; числовые «оценки уверенности» не используются.

alerts:
  staff_permission: "ac.alerts"
  delay_seconds: [5, 10]
  rate_limit_per_player_seconds: 3
  batch_window_seconds: 2

logging:
  alerts:
    to_console: true
    to_file: true
  detection_trace:
    enabled: false
    to_console: false
    to_file: false
    sample_per_second: 0
```

> **Важное:** трассировка по чек‑деталям по умолчанию **выключена**, чтобы не засорять прод‑логи. Включайте точечно `/ac debug <ник>` — пишется только в файл.

---

## 🧪 Как это устроено (кратко)

```
PacketEvents → PacketListenerImpl → CheckManager → DetectionEngine
                                      ├→ AlertEngine       (задержка/батч/лимиты)
                                      ├→ PunishmentManager (адаптивные таймеры; CRITICAL)
                                      └→ MitigationManager (снижение урона)
```

- **Окна**: SHORT (~25 тиков), ANALYSIS (~100), LONG (~600). Короткое окно даёт быстрые алёрты при сильном сигнале, длинные — устойчивость без блокировки действий.
- **Политика**: уровни `MEDIUM/HIGH/CRITICAL`; подтверждение по нескольким окнам и/или независимым семействам; строгие действия — только при здоровых `ping/TPS/LOS`.
- **AutoTotem (experimental)**: offhand‑swap / слот‑45 / resurrect + корреляция с почти летальным уроном. По умолчанию **только алёрты**.

---

## 📦 Сборка из исходников

```bash
# Java 17
mvn -DskipTests package
```
Артефакт: `target/ModernAC-<version>.jar`

> **PacketEvents** помечен как `provided` и ставится отдельно в `plugins/`. Мы его **не** шейдим.

---

## 🤝 Вклад (для контрибьюторов)

- Импорты PacketEvents: **API** — `com.github.retrooper.packetevents.*`; **builder** — `io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder`.
- Все логи — через `DetectionLogger`: консоль — алёрты/ошибки, детальный трейс — файл/по флагу.
- Не использовать NMS/Reflection, только PacketEvents+Bukkit/Paper.
- Соблюдать формат **Spotless**: один LF в конце файла, без неиспользуемых импортов, длинные конкатенации — в одну строку.
- Для офлайн‑правил и требований к задачам см. **AGENTS.md** (tiers, multi‑window, гейты ping/TPS/LOS).

---

ModernAC — практичный античит для PvP‑серверов: **быстрый, тихий, безопасный**. Начните с консервативных настроек, наблюдайте за алёртами, затем постепенно ужесточайте политику.
