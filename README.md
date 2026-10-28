# 🚀 KSP AI Copilot: Direct kRPC & Neuroevolution

> **W pełni autonomiczny, oparty na Sieciach Neuronowych system sterowania dla Kerbal Space Program.**

Projekt inteligentnego systemu nawigacji (GNC) dla rakiet w grze *Kerbal Space Program (KSP)*. System wykorzystuje bezpośrednie połączenie z silnikiem gry (kRPC) oraz architekturę sieci neuronowych do optymalizacji manewru wznoszenia (Gravity Turn), zarządzania stagingiem i osiągania stabilnej orbity.

---

## 🛠️ Architektura Systemu

W nowej wersji projektu zrezygnowano z powolnej komunikacji plikowej na rzecz architektury opartej o bezpośrednie API.

* **Zewnętrzny Mózg AI (Java/Deeplearning4j):** Główna aplikacja zarządzająca całym lotem, analizująca dane w czasie rzeczywistym i podejmująca krytyczne decyzje nawigacyjne[cite: 4].
* **Interfejs kRPC:** Zapewnia asynchroniczne, bezpośrednie połączenie między kodem w Javie a silnikiem KSP, co pozwala na natychmiastowe odczyty telemetrii i błyskawiczną egzekucję poleceń[cite: 5].

### 🔄 Pętla Sterowania (Real-Time Control Loop)
1.  **Telemetria (kRPC):** Java pobiera w czasie rzeczywistym krytyczne parametry lotu (wysokość, prędkość, przeciążenia G, ciśnienie dynamiczne Q, profil aerodynamiczny AoA)[cite: 4].
2.  **Analiza (DL4J):** Znormalizowane dane trafiają prosto do modelu sieci neuronowej[cite: 4, 10].
3.  **Decyzja:** Sieć na bieżąco wylicza optymalny kąt pochylenia (Pitch) oraz poziom przepustnicy (Throttle)[cite: 4].
4.  **Egzekucja:** Komendy są wysyłane z powrotem do gry poprzez moduł `AutoPilot`, błyskawicznie korygując trajektorię[cite: 4].

---

## 🧠 Model AI i Logika Lotu

Projekt przeszedł droge od prostego uczenia nadzorowanego do zaawansowanych algorytmów genetycznych:

- [1] **Etap 1: Behavioral Cloning (Uczenie Nadzorowane):** Pierwotna wersja (moduł `TelemetryTrainer`) opierała się na treningu z wykorzystaniem historycznych danych telemetrycznych[cite: 10]. Sieć uczyła się kopiować bazowe trajektorie z plików CSV, optymalizując wagi algorytmem Adam[cite: 10].
- [2] **Etap 2: Hybrydowa Neuroewolucja:** Obecny system (`EvolutionTrainer`) wykorzystuje model hybrydowy[cite: 3, 4]. Sieć ewoluuje w symulacji poprzez tworzenie generacji "mutantów" (wprowadzanie szumu do wag modelu)[cite: 4]. Klasa `RewardCalculator` rygorystycznie ocenia każdy lot, przyznając punkty m.in. za osiągniętą wysokość, kołowość orbity i oszczędność paliwa, jednocześnie karząc za katastrofy czy zbyt wysoki kąt natarcia (AoA)[cite: 9]. Najlepszy model z generacji staje się bazą dla kolejnej ("absolute_master.zip")[cite: 4].

* **Specyfikacja Techniczna Modelu:**
  * **Architektura:** Wielowarstwowy Perceptron (MLP) o strukturze warstw: Wejście(10) -> Dense(64) -> Dense(32) -> Wyjście(3)[cite: 4, 10].
  * **Funkcje Aktywacji:** TANH dla warstw ukrytych (zapewniająca płynność zmian wektorowych) oraz SIGMOID dla warstwy wyjściowej[cite: 4, 10].
  * **Optymalizator:** Adam (learning rate: 0.001) gwarantujący szybką zbieżność[cite: 4, 10].

---

## 🚀 Osiągnięcia (Progress Log)

- [x] **Przejście na kRPC:** Eliminacja opóźnień I/O dzięki integracji z serwerem kRPC[cite: 5].
- [x] **Autonomia w Javie (Auto-Staging):** System AI samodzielnie wykrywa spadek ciągu do zera i aktywuje procedurę odrzucenia pustych członów rakiety[cite: 4].
- [x] **Smart Warp 2.0:** Zaimplementowano dynamiczne sterowanie przyspieszeniem czasu (Physics Warp) bezpośrednio z kodu Javy, co drastycznie skraca czas potrzebny na ewolucyjny trening AI[cite: 4].
- [x] **Złożona Funkcja Nagrody:** Stworzenie precyzyjnego systemu scoringowego (m.in. gradient nagród za perycentrum), który skutecznie promuje stabilizację orbity kosmicznej (powyżej 70 km)[cite: 4, 9].

---

## 📋 Wymagania

* Gra **Kerbal Space Program** z uruchomionym modyfikacją **kRPC**.
* **Java 17** (lub nowsza).
* Biblioteki: **Deeplearning4j (DL4J)**, **ND4J**, **krpc-java**.

---

## ⚙️ Jak uruchomić?

1. Uruchom grę KSP, umieść testową rakietę na platformie startowej i upewnij się, że serwer kRPC jest włączony[cite: 5].
2. Otwórz projekt w środowisku programistycznym wspierającym Javę.
3. Aby rozpocząć trening ewolucyjny, skompiluj i uruchom klasę `EvolutionMain.java`[cite: 3].
4. System zautomatyzuje cały proces: przejmie stery, wykona lot w przyspieszeniu (Warp), wyliczy nagrodę, załaduje quicksave (ksp.quickload) i przystąpi do testowania kolejnego modelu[cite: 4].
