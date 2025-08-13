# AGENTS.md — правила для генерации кода (Codex/агенты)

Этот файл — «правила дорожного движения» для любых будущих задач по проекту **ModernAC**.
Цель — исключить типовые поломки (вшитые заглушки, неверные координаты зависимостей, дубли слушателей, запуск Maven в оффлайне и пр.) и стабильно получать рабочие патчи.

## 0) TL;DR (золотые правила)
1. **Никаких заглушек в чужих пакетах**: *запрещено* создавать исходники в `org.bukkit/**`, `net/md_5/**`, `io/github/retrooper/**`, `com/github/retrooper/**`. Эти пакеты принадлежат внешним библиотекам.
2. **PacketEvents — внешний плагин**, не шейдить, **scope=provided**. Мы используем **PacketEvents v2.9.4** (spigot module) + **Paper API 1.16.5**.
3. **Единые импорты PacketEvents**: `com.github.retrooper.packetevents.*` (API 2.9.x). Не переключать пакеты на `io.github...` и не миксовать.
4. **Не запускать Maven/тесты** в оффлайн-среде агентов. Просто возвращай файлы.
5. **Один пакет‑слушатель**: регистрируем только один `PacketListener`.
6. **Вывод всегда**: только изменённые/новые файлы целиком, без объяснений и логов.

---

## 1) Среда и зависимости
- **Java**: 17 (compile target 17).
- **Сервер**: Paper 1.16.5.
- **PacketEvents**: `com.github.retrooper:packetevents-spigot:2.9.4` (`scope: provided`).
- **Paper API**: `com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT` (`scope: provided`).
- **Репозитории**:
  - `papermc-repo`: `https://repo.papermc.io/repository/maven-public/`
  - `codemc-releases`: `https://repo.codemc.io/repository/maven-releases/`
  - `codemc-snapshots`: `https://repo.codemc.io/repository/maven-snapshots/`

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
        <configuration>
          <release>17</release>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.2.5</version>
        <configuration>
          <skipTests>true</skipTests>
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
# PacketEvents обязателен в рантайме
depend: [packetevents]
# ViaVersion опционален
softdepend: [ViaVersion]
```

---

## 2) Код‑правила (обязательно)
- **Импорты PacketEvents**: `com.github.retrooper.packetevents.*`.
- **Bootstrap PacketEvents (2.9.x)**:
  ```java
  import com.github.retrooper.packetevents.PacketEvents;
  import com.github.retrooper.packetevents.manager.server.ServerVersion;

  @Override public void onLoad() {
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
- **Один слушатель**: регистрируется лишь один `PacketListener` (или `PacketListenerImpl`, или `PacketBus` — не оба сразу).
- **Никаких стубов**: не создавать исходники в пакетах внешних библиотек.
- **Bukkit API**: использовать только для команд/сообщений/атрибутов; тяжёлые вычисления — не на main‑треде.

---

## 3) Формат ответов агента
Всегда возвращай **только изменённые/новые файлы целиком**, без объяснений и логов. Примеры формулировок задач:

### 3.1 «Правим только pom.xml»
> Обнови зависимости и репозитории под PacketEvents 2.9.4 и Paper 1.16.5. Верни **только** `pom.xml`.

### 3.2 «Удаляем заглушки пакетов»
> Удали каталоги `src/main/java/org/bukkit/**` и `src/main/java/io/github/retrooper/**`. Ничего другого не меняй. Верни список удалённых файлов.

### 3.3 «Единые импорты PE»
> Пройди все `*.java` и замени импорты PacketEvents на `com.github.retrooper.packetevents.*`. Верни затронутые файлы целиком.

### 3.4 «Один слушатель»
> Оставь `PacketListenerImpl` как единственный слушатель; снимай регистрацию других. Верни изменённые файлы целиком.

---

## 4) Самопроверка перед отдачей
Агент обязан прогнать логические проверки (без запуска Maven):

```bash
# 1) В исходниках нет чужих пакетов
rg -n "^package (org\.bukkit|net\.md_5|io\.github\.retrooper|com\.github\.retrooper)(?=\.)" src/main/java || true
# ожидается пусто

# 2) Импорты PacketEvents едины
rg -n "io\.github\.retrooper\.packetevents" src/main/java || true
# ожидается пусто

# 3) plugin.yml указывает правильный main и зависимости
rg -n "^main: com\.modernac\.ModernACPlugin" src/main/resources/plugin.yml
rg -n "^depend: \[packetevents\]" src/main/resources/plugin.yml

# 4) jar не содержит чужих пакетов (локально, если сборка есть)
# jar tf target/*.jar | rg "^(org/bukkit|net/md_5|io/github/retrooper|com/github/retrooper)/"
```

*(Если пункт 1 выдаёт строки в `src/main/java/com/modernac/...` — это нормально. Критично, если в исходниках есть **директории** чужих пакетов.)*

---

## 5) Автозащита в CI (опционально, но полезно)
Добавьте **Checkstyle** с простыми регэксп‑правилами, чтобы билд падал, если кто‑то снова положит исходники в запрещённые пакеты или использует «левые» импорты:

`pom.xml` — plugin:
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-checkstyle-plugin</artifactId>
  <version>3.3.1</version>
  <executions>
    <execution>
      <phase>validate</phase>
      <goals><goal>check</goal></goals>
      <configuration>
        <configLocation>checkstyle.xml</configLocation>
        <failOnViolation>true</failOnViolation>
      </configuration>
    </execution>
  </executions>
</plugin>
```

`checkstyle.xml` — минимум правил:
```xml
<!DOCTYPE module PUBLIC "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
  "https://checkstyle.org/dtds/configuration_1_3.dtd">
<module name="Checker">
  <module name="RegexpOnFilename">
    <property name="format" value=".*/(org/bukkit|net/md_5|io/github/retrooper|com/github/retrooper)/.*"/>
    <property name="message" value="Do NOT include sources from external packages in this repo."/>
    <property name="illegalPattern" value="true"/>
  </module>
  <module name="RegexpMultiline">
    <property name="format" value="^package (org\.bukkit|net\.md_5|io\.github\.retrooper|com\.github\.retrooper)\."/>
    <property name="message" value="Forbidden package declaration in sources."/>
    <property name="illegalPattern" value="true"/>
  </module>
  <module name="RegexpMultiline">
    <property name="format" value="io\.github\.retrooper\.packetevents"/>
    <property name="message" value="Use com.github.retrooper.packetevents imports for v2.9.x."/>
    <property name="illegalPattern" value="true"/>
  </module>
</module>
```

---

## 6) Частые ошибки и как их избегать
- **Не резолвится PacketEvents** → проверь координаты/репозитории (см. шаблон POM). Не меняй группу/артефакт на «похожую». Не добавляй источники пакетов в проект.
- **`cannot find symbol` по PE классам** → где‑то остался импорт `io.github...` или в исходниках лежит заглушка. Пройди «Самопроверку перед отдачей».
- **Два слушателя** → в `onEnable()` регистрируется только один. Удали лишний.
- **Снова появились `org/bukkit/**` в JAR** → кто‑то добавил исходники в этот пакет. Проверь Checkstyle и жёстко удаляй такие файлы.

---

## 7) Шаблон короткого задания (копируй при каждой новой правке)
> **Задача:** Выполни правку X. **Никаких заглушек**. Не запускай Maven. Верни только изменённые файлы целиком.
> **Ограничения:**
> - POM строго по шаблону из `AGENTS.md` (PacketEvents 2.9.4, Paper 1.16.5, scope=provided, репозитории papermc/codemc).
> - Импорты PacketEvents: `com.github.retrooper.packetevents.*`.
> - В исходниках не должно быть пакетов `org.bukkit/**`, `net/md_5/**`, `io/github/retrooper/**`, `com/github/retrooper/**`.
> - Регистрировать только **один** PacketListener.

---

Держите этот файл в корне репозитория и ссылайтесь на него в PR/Issues. Это резко снижает шанс, что агент снова «подтянет PacketEvents как либу» или положит заглушки в проект.

