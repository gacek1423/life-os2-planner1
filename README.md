# 🧬 Life OS Planner

**Life OS Planner** to modułowy system do zarządzania życiem osobistym, stworzony w Javie. Projekt ma na celu zintegrowanie zarządzania finansami, zadaniami i nawykami w jednej, spójnej aplikacji typu "Life Operating System".

Obecnie projekt skupia się na module finansowym i posiada architekturę gotową do łatwej rozbudowy.

## 🚀 Funkcjonalności

### ✅ Aktualne (Co mamy)
* **Architektura wielomodułowa:** Czysty podział na logikę (`core`), dane (`data`) i interfejs (`ui`).
* **Moduł Budżetu:** Zarządzanie finansami osobistymi (wstępna implementacja).
* **Baza Danych:** Lokalna, lekka baza danych H2 (`budget_db`) zapewniająca prywatność danych.
* **System budowania:** Pełna integracja z Apache Maven.

### 🚧 Planowane (Roadmapa)
* [ ] **Dashboard:** Główny pulpit podsumowujący stan finansów i zadań.
* [ ] **Menedżer Zadań (To-Do):** Listy zadań z priorytetami.
* [ ] **Habit Tracker:** Narzędzie do śledzenia i budowania nawyków.
* [ ] **Kalendarz:** Integracja terminów i spotkań.

## 🛠 Technologie

Projekt wykorzystuje następujące technologie:
* **Język:** Java 17+ (JDK)
* **Build Tool:** Maven (dołączony wrapper `mvnw`)
* **Baza danych:** H2 Database
* **UI:** JavaFX / Swing (zależnie od implementacji w `lifeos-ui`)
* **Style:** CSS

## 📂 Struktura Projektu

Projekt podzielony jest na trzy główne moduły:

1.  **`lifeos-core`** - Zawiera logikę biznesową, modele domenowe i interfejsy usług.
2.  **`lifeos-data`** - Odpowiada za warstwę trwałości, komunikację z bazą danych H2 i repozytoria.
3.  **`lifeos-ui`** - Warstwa prezentacji, odpowiada za to, co widzi użytkownik (widoki, kontrolery, style CSS).

## ⚙️ Jak uruchomić (Development)

Aby uruchomić projekt lokalnie, potrzebujesz zainstalowanego **JDK** (Java Development Kit).

1.  **Sklonuj repozytorium:**
    ```bash
    git clone [https://github.com/gacek1423/life-os2-planner1.git](https://github.com/gacek1423/life-os2-planner1.git)
    cd life-os2-planner1
    ```

2.  **Zbuduj projekt przy użyciu Mavena:**
    W systemie Linux/macOS:
    ```bash
    ./mvnw clean install
    ```
    W systemie Windows:
    ```cmd
    mvnw.cmd clean install
    ```

3.  **Uruchom aplikację:**
    Przejdź do modułu UI i uruchom aplikację (komenda może się różnić w zależności od konfiguracji klasy głównej):
    ```bash
    cd lifeos-ui
    ../mvnw javafx:run
    # Lub uruchomienie wygenerowanego pliku JAR z folderu target
    ```

## 🤝 Autor

* **gacek1423** - [GitHub Profile](https://github.com/gacek1423)

---
*Projekt w fazie aktywnego rozwoju.*
