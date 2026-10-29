# 🚀 KSP AI Copilot: Direct kRPC & Neuroevolution

> **W pełni autonomiczny, oparty na Sieciach Neuronowych system sterowania dla Kerbal Space Program.**

[![Obejrzyj lot testowy systemu AI](https://img.youtube.com/vi/VRNC-VgQhbM/maxresdefault.jpg)](https://www.youtube.com/watch?v=VRNC-VgQhbM)
*(Kliknij w obrazek powyżej, aby obejrzeć demonstrację wideo na YouTube)*

Projekt inteligentnego systemu nawigacji (GNC) dla rakiet w grze *Kerbal Space Program (KSP)*. System wykorzystuje bezpośrednie połączenie z silnikiem gry (kRPC) oraz architekturę sieci neuronowych do optymalizacji manewru wznoszenia (Gravity Turn), zarządzania stagingiem i osiągania stabilnej orbity.

---

## 🛠️ Architektura Systemu

W nowej wersji projektu zrezygnowano z powolnej komunikacji plikowej na rzecz architektury opartej o bezpośrednie API.

* **Zewnętrzny Mózg AI (Java/Deeplearning4j):** Główna aplikacja zarządzająca całym lotem, analizująca dane w czasie rzeczywistym i podejmująca krytyczne decyzje nawigacyjne.
* **Interfejs kRPC:** Zapewnia asynchroniczne, bezpośrednie połączenie między kodem w Javie a silnikiem KSP, co pozwala na natychmiastowe odczyty telemetrii i błyskawiczną egzekucję poleceń.

### 🔄 Pętla Sterowania (Real-Time Control Loop)
1.  **Telemetria (kRPC):** Java pobiera w czasie rzeczywistym krytyczne parametry lotu (wysokość, prędkość, przeciążenia G, ciśnienie dynamiczne Q, profil aerodynamiczny AoA).
2.  **Analiza (DL4J):** Znormalizowane dane trafiają prosto do modelu sieci neuronowej.
3.  **Decyzja:** Sieć na bieżąco wylicza optymalny kąt pochylenia (Pitch) oraz poziom przepustnicy (Throttle).
4.  **Egzekucja:** Komendy są wysyłane z powrotem do gry poprzez moduł `AutoPilot` (`setEngaged(true)`), błyskawicznie korygując trajektorię.

---

## 🧠 Model AI i Logika Lotu

Projekt przeszedł drogę od prostego uczenia nadzorowanego do zaawansowanych algorytmów genetycznych:

- [1] **Etap 1: Behavioral Cloning (Uczenie Nadzorowane):** Pierwotna wersja opierała się na treningu z wykorzystaniem historycznych danych telemetrycznych. Sieć uczyła się kopiować bazowe trajektorie z plików CSV, optymalizując wagi algorytmem Adam.
- [2] **Etap 2: Hybrydowa Neuroewolucja:** Obecny system wykorzystuje model hybrydowy. Sieć ewoluuje w symulacji poprzez tworzenie generacji "mutantów" (wprowadzanie szumu do wag modelu). Autorska klasa `RewardCalculator` rygorystycznie ocenia każdy lot, przyznając punkty m.in. za osiągniętą wysokość, kołowość orbity i oszczędność paliwa, jednocześnie karząc za katastrofy czy zbyt wysoki kąt natarcia (AoA). Najlepszy model z generacji staje się bazą dla kolejnej (`absolute_master.zip`).

* **Specyfikacja Techniczna Modelu:**
  * **Architektura:** Wielowarstwowy Perceptron (MLP) o strukturze warstw: Wejście(10) -> Dense(64) -> Dense(32) -> Wyjście(3).
  * **Funkcje Aktywacji:** TANH dla warstw ukrytych (zapewniająca płynność zmian wektorowych) oraz SIGMOID dla warstwy wyjściowej.
  * **Optymalizator:** Adam (learning rate: 0.001) gwarantujący szybką zbieżność.

---

## 🚀 Osiągnięcia (Progress Log)

- [x] **Przejście na kRPC:** Eliminacja opóźnień I/O dzięki integracji z serwerem kRPC.
- [x] **Autonomia w Javie (Auto-Staging):** System AI samodzielnie wykrywa spadek ciągu do zera i aktywuje procedurę odrzucenia pustych członów rakiety.
- [x] **Smart Warp 2.0:** Zaimplementowano dynamiczne sterowanie przyspieszeniem czasu (Physics Warp) bezpośrednio z kodu Javy, co drastycznie skraca czas potrzebny na ewolucyjny trening AI.
- [x] **Złożona Funkcja Nagrody:** Stworzenie precyzyjnego systemu scoringowego (m.in. gradient nagród za perycentrum), który skutecznie promuje stabilizację orbity kosmicznej (powyżej 70 km).

---

## 📋 Wymagania i Zależności

Aby poprawnie nawiązać połączenie i uniknąć problemów z API, wymagane jest dokładne dopasowanie wersji:

* Gra **Kerbal Space Program** z uruchomionym serwerem **kRPC (wersja 0.7.0 lub 0.7.1)**.
* **Java 17** (lub nowsza).
* Biblioteki AI: **Deeplearning4j (DL4J)** oraz **ND4J**.
* System komunikacji: **krpc-java (0.7.0)** + **Protobuf (4.36.2)**.

---

## ⚙️ Jak uruchomić?

1. Uruchom grę KSP, umieść testową rakietę na platformie startowej i upewnij się, że serwer kRPC jest włączony.
2. Otwórz projekt w swoim środowisku programistycznym (Java).
3. Projekt oferuje dwa główne tryby działania:

**A) Tryb Demonstracyjny (Inference)**
Aby zobaczyć system w akcji na już wytrenowanym modelu (bez nadpisywania wag), uruchom klasę `TestMain.java`. System załaduje wagi z pliku `absolute_master.zip`, przejmie stery i wykona pokazowy lot, zostawiając rakietę na stabilnej orbicie.

**B) Tryb Treningowy (Neuroewolucja)**
Aby rozpocząć nową sesję ewolucyjną, skompiluj i uruchom klasę `EvolutionMain.java`. System zautomatyzuje cały proces: wykona loty setkami mutantów z włączonym przyspieszeniem czasu (Warp), wyliczy nagrody, załaduje szybki zapis gry (quicksave) i samoczynnie przystąpi do testowania kolejnego pokolenia.
