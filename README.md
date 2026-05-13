# 🚀 KSP AI Copilot

> **Autopilot oparty na Sieciach Neuronowych dla Kerbal Space Program**

Projekt inteligentnego systemu sterowania rakietą w grze *Kerbal Space Program (KSP)*. System wykorzystuje architekturę sieci neuronowych do optymalizacji manewru wznoszenia (Gravity Turn) i osiągania stabilnej orbity.

---

## 🛠️ Architektura Systemu

Projekt składa się z dwóch ściśle współpracujących ze sobą modułów:

* **Moduł Kontroli (KerboScript/kOS):** Skrypt działający wewnątrz gry, który odpowiada za egzekucję lotu, staging oraz wprowadzanie danych telemetrycznych.
* **Mózg AI (Java/Deeplearning4j):** Zewnętrzna aplikacja, która analizuje dane w czasie rzeczywistym i podejmuje decyzje o optymalnym kącie nachylenia rakiety.

### 🔄 Pętla Sterowania (Control Loop)
1.  **Telemetria:** kOS zapisuje bieżące dane lotu (wysokość, prędkość) do pliku `telemetria.csv`.
2.  **Analiza:** Moduł w Javie odczytuje ostatnią linię, normalizuje dane i przetwarza je przez sieć neuronową.
3.  **Decyzja:** Model AI wylicza optymalny kąt (Pitch) i zapisuje go w pliku `sterowanie.csv`.
4.  **Reakcja:** kOS odczytuje komendę i natychmiast koryguje kurs rakiety.

---

## 🧠 Model AI i Logika Lotu

W projekcie zaimplementowano dwa równoległe podejścia do sterowania:

* **Model Matematyczny (Baseline):** Wykorzystuje funkcję paraboliczną (`shapeFactor = 1.7`) do wyznaczania idealnego łuku lotu w celu zminimalizowania oporów atmosferycznych.
* **Ewolucja Sieci Neuronowej (DL4J):**

   - [1] **Etap 1: Behavioral Cloning (Uczenie Nadzorowane):** Pierwotna wersja systemu opierała się na naśladownictwie (Supervisor). Sieć uczyła się kopiować optymalne trajektorie z plików wzorcowych. Metoda ta zapewniała stabilność w znanych warunkach, ale brakowało jej elastyczności przy nieprzewidzianych anomaliach.

   - [2] **Etap 2: Hybrydowa Neuroewolucja (Supervised Learning + Genetic Algorithm)** : Obecna wersja wykorzystuje podejście hybrydowe. Model łączy początkową bazę wiedzy z algorytmami ewolucyjnymi. Dzięki temu AI nie tylko „kopiuje” ruchy pilota, ale aktywnie optymalizuje kąt nachylenia (Pitch) poprzez mechanizm selekcji i mutacji, dążąc do wyewoluowania trajektorii maksymalizującej końcową nagrodę (idealna stabilność orbity i ekstremalna oszczędność paliwa).

* **Specyfikacja Techniczna Modelu:**

- [ ] **Architektura:** Wielowarstwowy Perceptron (MLP - Dense Layers).

- [ ] **Funkcja Aktywacji:** TANH (Tangens hiperboliczny) – wybrana ze względu na zapewnienie płynności zmian kątowych, co jest kluczowe dla stabilności aerodynamicznej rakiety.

- [ ] **Optymalizator: Adam** (learning rate: 0.001) – zapewniający szybką zbieżność modelu podczas sesji treningowych.

---

## 🚀 Osiągnięcia (Progress Log)

- [x] **Integracja kOS <-> Java:** Stabilna komunikacja plikowa z zabezpieczeniem przed konfliktami dostępu (Atomic Move).
- [x] **Automatyczny Staging:** System wykrywa brak ciągu i automatycznie odrzuca puste człony rakiety.
- [x] **Gravity Turn:** Implementacja zoptymalizowanego łuku lotu (parabola).
- [x] **Stabilna Orbita:** Skrypt automatycznie wykonuje manewr cyrkularyzacji w Apoapsis, stabilizując orbitę powyżej 70 km.
- [x] **Neuroewolucja** Przejście z prostego modelu Supervised Learning na zaawansowany **Hybryd Neuroevolution**.

---

## 📋 Wymagania

* Gra **Kerbal Space Program** z zainstalowanym modem **kOS**.
* **Java 17** (lub nowsza).
* Biblioteki AI: **Deeplearning4j (DL4J)** oraz **ND4J**.

---

## ⚙️ Jak uruchomić?

1. Uruchom grę KSP i umieść przygotowaną rakietę na platformie startowej.
2. Uruchom główną klasę `MainAI` w swoim środowisku programistycznym (Java).
3. Otwórz terminal kOS wewnątrz gry i wpisz poniższe komendy:
   ```kerboscript
   SWITCH TO 0.
   run autopilot.
