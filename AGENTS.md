# AGENTS.md — ModernAC Agent Guide

> **Коротко:** работаем **офлайн**, ничего не тянем из сети и не правим `pom.xml`. Сборка — обычный `mvn -DskipTests package`. PacketEvents — **`provided`**. Никакого NMS/рефлексии под версии. Алёрты только для **HIGH/CRITICAL**, trace — только в файл. Окна — через снапшоты массивов, без `stream()` по живым `Deque`.

---

## 1) Среда, сборка, совместимость
- **Java:** 17. **Maven:** стандартная сборка, без дополнительных плагинов (Spotless удалён).
- **PacketEvents:** 2.9.x как **`provided`** (установлен на сервере).  
  - **Builder импорт:** `io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder`  
  - **API импорты:** `com.github.retrooper.packetevents.*`
- **Поддержка серверов:** Paper/Purpur **1.16.5–1.21.4**.  
  **Запрещается:** NMS/Reflection, APИ/классы, зависящие от конкретных версий.
- **Сборка:** не добавлять/менять плагины/зависимости, **не возвращать Spotless**. Должно компилироваться офлайн.

---

## 2) Политика детектов и решений
- **Tiers:** `MEDIUM` / `HIGH` / `CRITICAL`. Никаких числовых «уверенностей» в логах/чатах.  
  Допустима *дискретная* подстановка для шаблонов сообщений: `HIGH → 0.9 (90%)`, `CRITICAL → 1.0 (100%)`.
- **Multi-window подтверждение:** включено (короткое/длинное/очень длинное окно).  
- **Мульти‑семейства:** действие допускается только при достаточном количестве **независимых семейств** (читается из конфига).  
- **Гейты (прежде чем флагать):** `ping ≤ ~180 ms`, `TPS ≥ ~18`, `LOS == true`.
- **Soft mode:** при плохих условиях понижать tier (CRITICAL→HIGH→MEDIUM), но алёрт всё равно отправлять c пометкой `[soft]`.

---

## 3) Alert/Log пайплайн (обязательно)
- **Нельзя наказывать без алёрта.** При достижении `HIGH/CRITICAL` — сначала **enqueue в `AlertEngine`**, затем mitigation/punish.
- **`AlertEngine`**
  - В очередь попадают **только** `HIGH/CRITICAL`.  
  - Первый `CRITICAL` по игроку — **без задержки**, последующие — через delay/batch/rate-limit (конфиг).  
  - Доставка: консоль (по флагу), стафф (perm из конфига, по умолчанию `ac.alerts`). Формат — по `messages.yml`.
  - **`AlertDetail`** содержит: `family`, `window`, `confidence(0.9/1.0)`, `ping`, `tps`, `tier`, `soft`.
- **`DetectionLogger`**
  - `trace(...)` → **только файл**, управляется конфигом и `/ac debug <ник>`.  
  - `alert(...)` → консоль/файл по флагам. Никаких `LEGACY`, «handled…», сырых процентов и пр. шума.

---

## 4) DetectionEngine (агрегация)
- Агрегируй по **семействам** и **окнам**; учитывай `require_multi_window_confirmation` и `min_independent_families_for_action`.
- Рассчитывай итоговый tier с учётом soft‑понижения.
- В `record(Check, DetectionResult)` обязательно формируй корректную семью (`result.getFamily()`), **не** использовать заглушку `LEGACY`.
- Возвращай чистое состояние в `reset/reload/shutdown` (очистка записей, отмена задач punishment/mitigation).

---

## 5) Чеки: стиль и безопасность
**Общий паттерн (обязательно):**
- **Никогда** не итерироваться/стримить по **живым** `Deque`.  
  Всегда: `snapshot(Deque) → примитивный массив → Arrays.sort / циклы`.
- Перед добавлением в окна — фильтровать вход: `Double.isFinite(v)` (не класть `null/NaN/Inf`).
- В начале `handle(...)` — ранние гейты и `MIN_SAMPLES`, иначе `return` (опционально `trace → файл`).
- Сравнения — по `double` с `EPS=1e-6`, **не** `Double.equals(...)`.

**Конкретика по семействам:**
- **IQR:** сортируем массив; Q1/Q3 (линейная интерполяция допустима). `dist > 1.5*IQR → HIGH`, `>3*IQR → CRITICAL` (при здоровых ping/TPS/LOS). При `iqr≈0` — не флагать.
- **PerfectEntropy:** бининг (например, 64 бина), Шеннон `H`. Низкая энтропия → HIGH/CRITICAL. Избегать stream по Deque; min/max считать ручными циклами.
- **Rank/RankLongTerm:**
  - `Rank`: позиционируй **последнее** значение относительно **предыдущих N-1** (без него), триггер — перцентили (<1%/>99%).
  - `RankLongTerm`: считаем `distinct` с `EPS`, порог на «слишком мало» уникальных.
- **Distinct/Pattern Analysis/Pattern Statistics:** использовать отсортированный массив и сравнением блоков с `EPS`.
- **LB\*** (LiquidBounce сигнатуры): использовать `snapshotInt(...)` + `MathUtil.findGcd(int[])`; не очищать буфер до проверки длины.

---

## 6) Потоки, задачи и ошибки
- В `CheckManager` вокруг `check.handle(...)` — `try/catch(Throwable)` с логом в `DetectionLogger.error`.
- ThreadFactory с `setUncaughtExceptionHandler(...)` для детекторных потоков.
- Очереди/экзекьюторы — **ограниченные**; при переполнении — редкий (rate‑limited) error-лог, не падение.
- `MitigationManager`/`PunishmentManager`/`AlertEngine`: хранить `BukkitTask` и надёжно отменять при `disable/reload/reset`.
- Использовать `ThreadLocalRandom.current()` вместо `new Random()` в горячих путях.

---

## 7) Команды и права
- Не менять `/ac info` (оставить как есть).  
- `/ac debug <ник>`: подписывает/отписывает отправителя на **file‑trace** конкретного игрока. В чат ничего глобально не шлёт.
- Права: стафф‑алёрты по праву из конфига (по умолчанию `ac.alerts`). Убедиться, что `plugin.yml` соответствует.

---

## 8) Конфиг‑контракт (важно)
- Все флаги, упомянутые в README/AGENTS, реально читаются и применяются. Не оставлять «мёртвых» опций.
- Рекомендованные дефолты для прода:  
  `experimental_detections: false`,  
  `require_multi_window_confirmation: true`,  
  `min_independent_families_for_action: 2`,  
  `logging.alerts.to_console: true`, `logging.detection_trace.enabled: false`.

---

## 9) Формат/стиль
- Соблюдать обычный google‑java‑style (но **без Spotless** и без требования «в одну строку любой ценой»).  
- Чистые импорты, ровно один перевод строки в конце файла, отсутствие `System.out.println`/`printStackTrace`.

---

## 10) Что возвращать в PR
- Полные тексты **изменённых** файлов с **относительными путями** (например, `src/main/java/com/modernac/engine/DetectionEngine.java`).
- Короткий отчёт (5–10 строк):
  - где мог быть разрыв «punish без alert» и как устранено;
  - какие чеки переписаны/подправлены (и почему);
  - что сделано по потокам/таскам/очисткам;
  - какие конфиг‑флаги/права/ключи сообщений были недостающими и что добавлено;
  - любые точечные оптимизации (без изменения поведения).

---

## 11) Нельзя
- Менять `pom.xml`, возвращать Spotless или добавлять зависимости.  
- Тянуть из интернета, хардкодить сетевые пути/загрузчики.  
- Использовать NMS/Reflection под версии.  
- Менять структуру `messages.yml` и команды (`/ac info` и др.).  
- Ломать совместимость 1.16.5–1.21.4.

---

## 12) Быстрый self‑check агента
- Срабатывание HIGH/CRITICAL → **есть алёрт** (консоль/стафф по конфигу) **до** punish/mitigation.  
- При плохом ping/TPS — `[soft]`, понижение tier, но алёрт есть.  
- Под нагрузкой — нет `NPE/CME`, нет спама «handled…/LEGACY…».  
- `/ac debug <ник>` не засоряет чат; trace — только в файл.  
- Reload/reset/disable — снимают mitigation/punishment, отменяют задачи.  
- Сборка проходит офлайн; PacketEvents — `provided`; никаких new deps/plugins.

