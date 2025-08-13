# AGENTS.md — правила для генерации кода (Codex/агенты)

Этот файл — «правила дорожного движения» для задач по проекту **ModernAC**.
Он минимизирует типовые поломки (ложные импорты PacketEvents, заглушки чужих пакетов,
дубли слушателей, шум в логах, несоответствие форматеру и т. п.).

> **Ключевой хотфикс (всегда применять, если увидели обратное):**
> в `src/main/java/com/modernac/ModernACPlugin.java` **должен быть только один импорт билдера**
> ```java
> import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
> ```
> Если встретился
> `com.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder` — **удалить**.
> При этом остальные API‑импорты PacketEvents остаются `com.github.retrooper.packetevents.*` — это норма для 2.9.x.

---

## 0) TL;DR (золотые правила)

1. **Никаких исходников в чужих пакетах**: запрещено создавать файлы в
   `org.bukkit/**`, `net/md_5/**`, `io/github/retrooper/**`, `com/github/retrooper/**`.
   Эти пакеты принадлежат внешним библиотекам.
2. **PacketEvents — внешний плагин**. Не шейдить. `scope: provided`. Используем линейку **2.9.x**.
3. **Импорты PacketEvents**:  
   • **API** — `com.github.retrooper.packetevents.*`  
   • **Builder** — **только** `io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder`
4. **Один слушатель пакетов**: регистрируется один `PacketListener`/`PacketListenerImpl`.
5. **Без NMS/Reflection Mojang**. Только PacketEvents + Bukkit/Paper API.
6. **Форматирование строго Spotless (google-java-format)**. Если правили код — прогоняйте `spotless:apply`.
7. **Логирование**: в консоль — только алёрты/ошибки. Подробный trace детектов — **только в файл** через `DetectionLogger` и по настройкам (без спама).

---

## 1) Среда и совместимость

- **Поддерживаемые сервера**: Paper/Purpur **1.16.5–1.21.4** (единый JAR).
- **Java (compile)**: 17 (`maven-compiler-plugin` → `<release>17</release>`).  
  **Runtime**: плагин работает и на серверах, запущенных на Java 21.
- **PacketEvents**: `com.github.retrooper:packetevents-spigot:2.9.x` (`scope: provided`).
- **Paper API для компиляции**: `1.16.5-R0.1-SNAPSHOT` (`scope: provided`).

### 1.1 POM — шаблон (использовать «как есть»)
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.modernac</groupId>
  <artifactId>ModernAC</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <packaging>jar</packaging>
  <name>ModernAC</name>
  <properties>
    <maven.compiler.release>17</maven.compiler.release>
  </properties>

  <repositories>
    <repository>
      <id>papermc-repo</id>
      <url>https://repo.papermc.io/repository/maven-public/</url>
    </repository>
    <repository>
      <id>codemc-releases</id>
      <url>https://repo.codemc.io/repository/maven-releases/</url>
    </repository>
    <repository>
      <id>codemc-snapshots</id>
      <url>https://repo.codemc.io/repository/maven-snapshots/</url>
    </repository>
  </repositories>

  <dependencies>
    <dependency>
      <groupId>com.github.retrooper</groupId>
      <artifactId>packetevents-spigot</artifactId>
      <version>2.9.4</version>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>com.destroystokyo.paper</groupId>
      <artifactId>paper-api</artifactId>
      <version>1.16.5-R0.1-SNAPSHOT</version>
      <scope>provided</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.11.0</version>
        <configuration><release>17</release></configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.2.5</version>
        <configuration><skipTests>true</skipTests></configuration>
      </plugin>
      <!-- Spotless обязателен в CI -->
      <plugin>
        <groupId>com.diffplug.spotless</groupId>
        <artifactId>spotless-maven-plugin</artifactId>
        <version>2.43.0</version>
        <configuration>
          <java>
            <googleJavaFormat/>
            <removeUnusedImports/>
          </java>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

### 1.2 plugin.yml — обязательные настройки
```yml
name: ModernAC
main: com.modernac.ModernACPlugin
version: 0.1.0
api-version: 1.16
depend: [packetevents]   # PacketEvents обязателен в рантайме
softdepend: [ViaVersion] # опционально
```

---

## 2) PacketEvents 2.9.x — правильные импорты и bootstrap

- **API‑импорты**: `com.github.retrooper.packetevents.*`
- **Builder‑импорт (единственный корректный)**:
  ```java
  import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
  ```

### 2.1 Инициализация (канонический шаблон)
```java
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;

@Override public void onLoad() {
  // Строим API (если в проекте используется builder)
  SpigotPacketEventsBuilder.build(this);
  PacketEvents.getAPI().getSettings()
      .fallbackServerVersion(ServerVersion.V_1_16_5)
      .checkForUpdates(false);
  PacketEvents.getAPI().load();
}

@Override public void onEnable() {
  PacketEvents.getAPI().init();
  PacketEvents.getAPI().getEventManager().registerListener(new PacketListenerImpl(this));
}

@Override public void onDisable() {
  PacketEvents.getAPI().terminate();
}
```

**Важно:** не шейдить PacketEvents в JAR плагина и не добавлять «заглушек» их пакетов в исходники.

---

## 3) Логирование и производительность

- **Console‑noise policy**: в консоль — старт/стоп, алёрты, ошибки. Детальный trace «handled …» — только в файл, управляется конфигом через `DetectionLogger`; поддерживается
  выборочно per‑player для отладки.
- **Файловый вывод** выполнять через единый асинхронный исполнитель; не блокировать тики.
- **Spotless** обязателен. Разделяйте длинную конкатенацию строк по требованиям форматера
  (или используйте `String.format`). Конец файла — ровно один LF.
- **Окна/фичи**: в горячем пути — кольцевые буферы на примитивах, без бокса/временных коллекций.

---

## 4) Политики детекта (резюме для задач)

- **Multi‑window confirmation** оставляем. Для быстрого UX допускается ранний алёрт по короткому окну при высокой уверенности и «здоровых» ping/TPS; длинные окна — для повышения доверия (не блокируют).
- **Уверенность** — числовая метрика (0..1 или 0..100). Группы `CRITICAL/HIGH/MEDIUM` не использовать в новой логике. Старые ключи в конфиге — поддерживать как deprecated (маппинг на числа).
- **ElytraTarget** — единая проверка, помеченная experimental, по умолчанию выключена, без punish/mitigation; реализует lead‑lock и boost‑snap эвристики. `/ac info` и `messages.yml` — не менять.

---

## 5) Самопроверка перед отдачей (без запуска Maven)

```bash
# 1) В исходниках НЕ должно быть пакетов чужих библиотек
rg -n "^package (org\.bukkit|net\.md_5|io\.github\.retrooper|com\.github\.retrooper)(?=\.)" src/main/java || true
# Ожидается пусто. Исключение: наши собственные пакеты com.modernac.


# 2) Импорты PacketEvents
# 2.1 API всегда com.github...
rg -n "import\s+io\.github\.retrooper\.packetevents\.(?!factory\.spigot\.SpigotPacketEventsBuilder)" src/main/java || true
# Должно быть пусто (разрешаем только builder‑импорт).

# 2.2 Допустимый builder‑импорт
rg -n "import\s+io\.github\.retrooper\.packetevents\.factory\.spigot\.SpigotPacketEventsBuilder;" src/main/java

# 3) plugin.yml указывает корректный main и зависимость на PacketEvents
rg -n "^main: com\.modernac\.ModernACPlugin" src/main/resources/plugin.yml
rg -n "^depend: \[packetevents\]" src/main/resources/plugin.yml
```

---

## 6) Частые ошибки и как их избегать

- **Два импорт‑пути билдера**: кто‑то добавил и `io.github...`, и `com.github...`. Оставляем **только** `io.github...` для билдера.
- **Не резолвится PacketEvents**: проверь координаты/репозитории (см. POM). Не добавляй исходники чужих пакетов.
- **Два слушателя**: зарегистрирован только один `PacketListener`. Удали лишние регистрации.
- **Шум в консоли**: все детальные `handled …` должны идти через `DetectionLogger` и уметь выключаться конфигом.

---

## 7) Шаблон короткого задания (копируй в Issues/PR)

> **Задача:** Выполни правку X. Верни **только изменённые/новые файлы целиком**, без логов.  
> **Ограничения:**  
> • POM/репозитории — как в `AGENTS.md`. PacketEvents 2.9.x, `scope: provided`.  
> • Импорты PacketEvents: API = `com.github...`, builder = `io.github...`.  
> • В исходниках нет пакетов `org.bukkit/**`, `net/md_5/**`, `io/github/retrooper/**`, `com/github/retrooper/**`.  
> • Регистрируется только один `PacketListener`.  
> • Логирование — через `DetectionLogger`, без спама в консоль.  
> • Spotless зелёный.
