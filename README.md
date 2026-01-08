# 🧬 Life OS Planner

**Life OS Planner** to modułowy system do zarządzania życiem osobistym, stworzony w Javie. Projekt ma na celu zintegrowanie zarządzania finansami, zadaniami i nawykami w jednej, spójnej aplikacji typu "Life Operating System".

## 🚀 Nowości w tej wersji

### ✅ Nowe funkcjonalności

#### 🎯 **Dashboard** - Główny pulpit podsumowujący
- **Podsumowanie finansowe**: Saldo całkowite, przychody i wydatki miesięczne, oszczędności
- **Postęp celów**: Liczba aktywnych i zrealizowanych celów, procent realizacji
- **Statystyki zadań**: Wszystkie zadania, wykonane dzisiaj, oczekujące i zaległe
- **Nawyki**: Liczba nawyków, wykonane dzisiaj, średnia realizacja, najdłuższa seria
- **Powiadomienia i przypomnienia**: Automatyczne powiadomienia o zaległych zadaniach i terminach
- **Wykresy**: Interaktywne wykresy finansowe i statystyki nawyków

#### 🔄 **Habit Tracker** - System śledzenia nawyków
- **Kategorie nawyków**: Zdrowie, Fitness, Zdrowie psychiczne, Produktywność, Nauka, Relacje, Hobby, Finanse
- **Częstotliwość**: Codziennie, co tydzień, dni robocze, weekendy, niestandardowe
- **Śledzenie serii**: Aktualna seria, najlepsza seria, historia realizacji
- **Statystyki**: Procent realizacji, analiza trudności, wykres serii
- **Szybkie oznaczanie**: Łatwe oznaczanie nawyków jako wykonane na dziś
- **Rekordy dziennie**: Możliwość dodawania notatek i oceny trudności

#### 🎯 **Rozbudowa modułu Celów**
- **Statusy celów**: Aktywny, Wstrzymany, Zakończony, Anulowany
- **Kategorie celów**: Finansowy, Zdrowotny, Edukacyjny, Zawodowy, Osobisty, Podróże, Hobby, Rodzinny
- **Kamienie milowe**: Dzielenie celów na mniejsze etapy z nagrodami
- **Śledzenie postępu**: Dokładne śledzenie wpłat i postępu procentowego
- **Priorytety**: System priorytetów (Niski, Średni, Wysoki, Krytyczny)
- **Analiza czasowa**: Obliczanie pozostałych dni i kwoty do zebrania

#### 📊 **Rozbudowa modułu Zadań**
- **Statusy zadań**: Oczekujące, W trakcie, Zakończone, Anulowane, Wstrzymane
- **Priorytety**: Kompletny system zarządzania priorytetami
- **Terminy**: Śledzenie terminów, automatyczne oznaczanie zaległych zadań
- **Czas**: Estymacja i śledzenie faktycznie spędzonego czasu
- **Kategorie i tagi**: Organizacja zadań według kategorii
- **Powtarzalność**: Zadania cykliczne (codziennie, co tydzień, miesięcznie)

### 🛠 Ulepszenia techniczne

#### 📋 **Nowe modele danych**
- `Habit` - Encja nawyku z pełną konfiguracją
- `HabitRecord` - Rekord dzienny realizacji nawyku
- `HabitCategory` - Kategorie nawyków
- `HabitFrequency` - Częstotliwość wykonywania
- `Dashboard` - Model podsumowania danych
- `GoalMilestone` - Kamienie milowe celów
- `GoalProgress` - Historia postępu celów
- `GoalStatus` - Statusy celów
- `GoalCategory` - Kategorie celów
- `Priority` - Priorytety zadań i celów
- `TaskStatus` - Statusy zadań
- `RecurringPattern` - Wzorce powtarzalności zadań

#### 🔧 **Nowe serwisy**
- `HabitService` - Zarządzanie nawykami i ich statystykami
- `DashboardService` - Generowanie podsumowań i analiz
- `GoalService` - Zarządzanie celami (ulepszony)
- `TaskService` - Zarządzanie zadaniami (ulepszony)

#### 🎨 **Nowe kontrolery JavaFX**
- `DashboardController` - Kontroler panelu głównego
- `HabitsController` - Kontroler widoku nawyków
- `GoalsController` - Kontroler widoku celów (ulepszony)

#### 🖼 **Nowe widoki FXML**
- `dashboard.fxml` - Panel główny z wykresami i statystykami
- `habits.fxml` - Widok zarządzania nawykami
- `goals.fxml` - Widok zarządzania celami (ulepszony)

## 🛠 Technologie

Projekt wykorzystuje następujące technologie:
- **Język**: Java 17+ (JDK)
- **Build Tool**: Maven (dołączony wrapper `mvnw`)
- **UI**: JavaFX 21
- **Baza danych**: H2 Database
- **Styl**: CSS + FXML
- **Narzędzia**: Lombok, JUnit 5

## 📂 Struktura Projektu

Projekt podzielony jest na trzy główne moduły:

1.  **`lifeos-core`** - Zawiera logikę biznesową, modele domenowe i interfejsy usług
    - Model: Encje danych (Habit, Goal, Task, Transaction, etc.)
    - Moduły: HabitService, GoalService, TaskService, DashboardService
    - Infrastruktura: Interfejsy repozytoriów i serwisów

2.  **`lifeos-data`** - Warstwa trwałości, komunikacja z bazą danych H2 i repozytoria
    - Konfiguracja bazy danych H2
    - Repozytoria dla wszystkich encji
    - Migracje schematu bazy danych

3.  **`lifeos-ui`** - Warstwa prezentacji, odpowiada za to, co widzi użytkownik
    - Kontrolery: DashboardController, HabitsController, GoalsController
    - Widoki: FXML dla wszystkich ekranów
    - Style: CSS dla spójnego wyglądu

## ⚙️ Jak uruchomić (Development)

Aby uruchomić projekt lokalnie, potrzebujesz zainstalowanego **JDK** (Java Development Kit) w wersji 17+.

### 1. Sklonuj repozytorium:
```bash
git clone https://github.com/gacek1423/life-os2-planner1.git
cd life-os2-planner1
```

### 2. Zbuduj projekt przy użyciu Mavena:
**Linux/macOS:**
```bash
./mvnw clean install
```

**Windows:**
```cmd
mvnw.cmd clean install
```

### 3. Uruchom aplikację:
**Linux/macOS:**
```bash
cd lifeos-ui
../mvnw javafx:run
```

**Windows:**
```cmd
cd lifeos-ui
..\mvnw.cmd javafx:run
```

### 4. Uruchomienie z pliku JAR:
```bash
java -jar lifeos-ui/target/lifeos-ui-1.0-SNAPSHOT-shaded.jar
```

## 🎮 Jak korzystać z aplikacji

### Dashboard (Panel główny)
- Przeglądaj podsumowanie wszystkich danych na jednym ekranie
- Śledź wykresy finansowe i statystyki nawyków
- Sprawdzaj powiadomienia i przypomnienia
- Generuj raporty tygodniowe

### Nawyki (Habit Tracker)
- Dodawaj nowe nawyki wybierając kategorię i częstotliwość
- Oznaczaj nawyki jako wykonane na dziś
- Śledź swoje serie i postęp na wykresach
- Analizuj statystyki realizacji

### Cele (Goals)
- Twórz cele finansowe i osobiste
- Dziel cele na kamienie milowe
- Dodawaj postęp i śledź realizację
- Monitoruj terminy i priorytety

### Zadania (Tasks)
- Zarządzaj zadaniami z priorytetami i terminami
- Używaj kategorii i tagów do organizacji
- Ustawiaj zadania cykliczne
- Śledź czas wykonania

### Finanse
- Zarządzaj portfelami i transakcjami
- Kategoryzuj wydatki i przychody
- Śledź bilans finansowy
- Analizuj trendy wydatków

## 🚧 Planowane rozszerzenia (Roadmapa)

### Faza 1 (Aktualnie zrealizowane) ✅
- [x] Dashboard z podsumowaniem
- [x] Habit Tracker
- [x] Rozbudowa Goals o kamienie milowe
- [x] Rozbudowa Tasks o priorytety i powtarzalność
- [x] Wykresy i statystyki

### Faza 2 (Planowane) 🚧
- [ ] **Kalendarz**: Integracja terminów i spotkań
- [ ] **Raporty**: Generowanie szczegółowych raportów PDF/CSV
- [ ] **Eksport/Import**: Możliwość eksportu danych
- [ ] **Synchronizacja**: Cloud sync dla danych
- [ ] **Mobilna wersja**: Aplikacja na Android/iOS

### Faza 3 (Przyszłość) 🔮
- [ ] **AI Insights**: Inteligentne analizy i sugestie
- [ ] **Integracje**: Połączenie z bankami API
- [ ] **Zdrowie**: Integracja z trackerami fitness
- [ ] **Społeczność**: Udostępnianie celów i wyzwań
- [ ] **Gamifikacja**: System nagród i osiągnięć

## 🤝 Wkład w projekt (Contributing)

Chętnie przyjmujemy wkład w rozwój projektu! Oto jak możesz pomóc:

1. Zgłaszaj pomysły i błędy przez Issues
2. Twórz Pull Requests z nowymi funkcjonalnościami
3. Poprawiaj dokumentację i tłumaczenia
4. Dziel się swoimi doświadczeniami i feedbackiem

## 📄 Licencja

Projekt jest otwarty i dostępny dla wszystkich. Możesz go modyfikować i rozwijać zgodnie z potrzebami.

## 👨‍💻 Autor

- **gacek1423** - [GitHub Profile](https://github.com/gacek1423)

## 📧 Kontakt

Masz pytania, sugestie lub chcesz współpracować? Śmiało pisz!

---

*Projekt w fazie aktywnego rozwoju. Ostatnia aktualizacja: Styczeń 2026*