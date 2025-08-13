# AGENTS.md — правила для Codex/агентов (ModernAC)

Этот документ фиксирует **финальные правила** для задач по проекту ModernAC и ориентирован на офлайн-режим (без интернета/Maven).  
**Важно: используем уровни уверенности (tiers) — `MEDIUM/HIGH/CRITICAL`, а НЕ числовые метрики.**

---

## 0) TL;DR

1. **Офлайн**: не запускай Maven/Gradle, не меняй `pom.xml`, не подтягивай зависимости. Возвращай **только изменённые/новые исходники целиком**.
2. **PacketEvents 2.9.x** — внешний плагин (`provided`), **не шейдить**.
3. **Импорты PacketEvents**:  
   • API → `com.github.retrooper.packetevents.*`  
   • Builder → **только** `io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder`
4. **Один PacketListener** — без дублей регистраций.
5. **Без NMS/Reflection Mojang** — только PacketEvents + Bukkit/Paper API.
6. **Логирование**: консоль = старт/стоп, алёрты, ошибки. Детальный trace — **только в файл** через `DetectionLogger` (перс-игрок, по конфигу). Никаких `System.out.println`.
7. **Формат**: совместимость со Spotless (google-java-format): однострочные конкатенации, 1 LF в конце файла, без неиспользуемых импортов.

---

## 1) Базовые инварианты

- Серверы: Paper/Purpur **1.16.5–1.21.4**, единый JAR.  
- Java: compile **17** (на серверах Java 21 — ок).  
- `plugin.yml`: `depend: [packetevents]`, корректный `main:`.  
- В `ModernACPlugin.java` **единственный** корректный импорт билдера:
  ```java
  import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
  ```
  Любые `com.github...factory.spigot.SpigotPacketEventsBuilder` — удалить.

---

## 2) Политика качеcтва чеков (реализовать, а не удалять)

Категории проблем и действия. **Внешнее поведение/сообщения не менять** (`/ac info`, `messages.yml` неизменны).

### 2.1 Категории
- **STUB** — заглушка: псевдо-пакеты (`instanceof String`), мгновенный `fail(...)`, TODO/FIXME.  
  → **РЕАЛИЗОВАТЬ** на реальных событиях PacketEvents/Bukkit (вращения, атаки, window-click, offhand swap, урон и т.п.).
- **NONDETERMINISTIC** — `Random/Math.random()` в принятии решения.  
  → **УБРАТЬ СЛУЧАЙНОСТЬ** — решения детерминированные.
- **NO-GATES** — нет гейтов ping/TPS/LOS до эскалации.  
  → **ДОБАВИТЬ ГЕЙТЫ**: `latencyOK/stabilityOK`, `ping ≤ 180 ms`, `TPS ≥ 18`, `LOS == true` (или эквиваленты проекта).
- **NOISE** — прямые логи «handled…» в консоль/общий лог, обход `DetectionLogger`.  
  → **ЦЕНТРАЛИЗОВАТЬ ЛОГИ**: trace → файл (по конфигу), консоль → только алёрты/ошибки.
- **DEAD** — класс не регистрируется/не вызывается.  
  → Если чек нужен — **реализовать и зарегистрировать**; если исторический мусор — **удалить**.

### 2.2 Инварианты детектов
- **Уровни уверенности (tiers)**: используем `MEDIUM`, `HIGH`, `CRITICAL`. **Числовых очков не вводить.**
- **Окна**: short/analysis/long (рекомендуем **25 / 100 / 600** тиков). Реализовывать на кольцевых буферах без аллокаций.  
- **Подтверждение (multi‑window)**: подтверждать сигнал через несколько окон и/или независимые семейства. Можно использовать правило:  
  - алёрт при **HIGH** на коротком окне *и* подтверждении вторым семейством/окном;  
  - наказание при **CRITICAL** (при здоровых ping/TPS).  
- **De‑escalation/decay**: если в течение ~8 с не набрался вес до наказания — сбрасывать накопление.
- **Границы среды**: при плохих условиях (высокий ping/низкий TPS/плохой LOS) — максимум алёрт, **без** наказаний.

---

## 3) Реализация AutoTotem (пример политики в рамках tiers)

- Источники: `DiggingAction.SWAP_ITEM_WITH_OFFHAND`, клики окна (slot **45**), `EntityResurrectEvent`, крит-урон/здоровье игрока.  
- Поведение: одиночные «реакции» < 1–2 тиков → **MEDIUM**; повтор в коротком окне + корреляция с летальным уроном → **HIGH**; систематичность в 100/600 тик — **CRITICAL** (но по умолчанию **без punish**, только алёрты).  
- Гейты: `ping ≤ 180ms`, `TPS ≥ 18`, LOS == true.

> Не менять тексты сообщений; всё логирование через `DetectionLogger` (trace — файл, off по умолчанию).

---

## 4) Офлайн Pre‑submit чек‑лист

```bash
# Импорты PacketEvents
rg -n "import\s+com\.github\.retrooper\.packetevents\.factory\.spigot\.SpigotPacketEventsBuilder" src/main/java && echo "ERROR: wrong builder import" || true
rg -n "import\s+io\.github\.retrooper\.packetevents\.factory\.spigot\.SpigotPacketEventsBuilder;" src/main/java

# Нет исходников в чужих пакетах
rg -n "^package (org\.bukkit|net\.md_5|io\.github\.retrooper|com\.github\.retrooper)(?=\.)" src/main/java || true

# Один PacketListener
rg -n "registerListener\(" src/main/java | wc -l

# Запрещённые конструкции
rg -n "System\.out\.println" src/main/java || true
rg -n "new\s+Random\(|Math\.random\(" src/main/java || true

# Хвостовые пробелы и финальный LF
rg -n "\s+$" src/main/java || true
rg -n "[^\n]$" src/main/java
```

---

## 5) Что НЕ менять агенту

- `/ac info` и `messages.yml` — оставить как есть.  
- Структуру `config.yml` — не ломать; новые ключи добавлять только по явному заданию.  
- Не добавлять новые типы проверок «от себя». Реализуем существующие.

---

## 6) Шаблон задания (Issues/PR)

> **Задача:** Реализовать/починить X в ModernAC (tiers `MEDIUM/HIGH/CRITICAL`, без числовых очков). Верни **только изменённые/новые .java** целиком, без логов/бинарей.  
> **Ограничения:** офлайн; не менять `pom.xml`; PacketEvents 2.9.x (`provided`); builder‑импорт `io.github…SpigotPacketEventsBuilder`; один PacketListener; без NMS; логирование через `DetectionLogger`; Spotless‑friendly стиль.

