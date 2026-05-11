# 🚀 KSP AI Copilot - Autopilot oparty na Sieciach Neuronowych

Projekt inteligentnego systemu sterowania rakietą w grze **Kerbal Space Program (KSP)**. System wykorzystuje architekturę sieci neuronowych do optymalizacji manewru wznoszenia (**Gravity Turn**) i osiągania stabilnej orbity.

## 🛠️ Architektura Systemu

Projekt składa się z dwóch współpracujących modułów:

1.  **Moduł Kontroli (KerboScript/kOS):** Skrypt działający wewnątrz gry, który odpowiada za egzekucję lotu, staging oraz dodawanie danych telemetrycznych.
2.  **Mózg AI (Java/Deeplearning4j):** Aplikacja zewnętrzna, która analizuje dane w czasie rzeczywistym i podejmuje decyzje o kącie nachylenia rakiety.

### Pętla Sterowania (Control Loop):
* **Telemetria:** kOS zapisuje dane (wysokość, prędkość) do pliku `telemetria.csv`.
* **Analiza:** Java odczytuje ostatnią linię, normalizuje dane i przepuszcza je przez sieć neuronową.
* **Decyzja:** Model AI wylicza optymalny kąt (Pitch) i zapisuje go w `sterowanie.csv`.
* **Reakcja:** kOS odczytuje komendę i koryguje kurs rakiety.



## 🧠 Model AI i Logika Lotu

W projekcie zaimplementowano dwa podejścia do sterowania:

* **Model Matematyczny (Baseline):** Wykorzystuje funkcję paraboliczną (`shapeFactor = 1.7`) do wyznaczania idealnego łuku lotu w celu zminimalizowania oporów atmosferycznych.
* **Sieć Neuronowa (DL4J):** * Architektura: Wielowarstwowy Perceptron (Dense Layers).
    * Funkcja Aktywacji: `TANH` (Tangens hiperboliczny) dla płynnej regulacji kątów.
    * Optymalizator: `Adam` (learning rate: 0.001).

## 🚀 Osiągnięcia (Progress Log)

- [x] **Integracja kOS <-> Java:** Stabilna komunikacja plikowa z zabezpieczeniem przed konfliktami dostępu (Atomic Move).
- [x] **Automatyczny Staging:** System wykrywa brak ciągu i automatycznie odrzuca puste człony rakiety.
- [x] **Gravity Turn:** Implementacja zoptymalizowanego łuku lotu (parabola).
- [x] **Stabilna Orbita:** Skrypt automatycznie wykonuje manewr cyrkularyzacji w Apoapsis, stabilizując orbitę powyżej 70 km.

## 📋 Wymagania

* **Kerbal Space Program** z zainstalowanym modem **kOS**.
* **Java 17+**
* Biblioteki: **Deeplearning4j (DL4J)**, **ND4J**.

## ⚙️ Jak uruchomić?

1.  Uruchom grę KSP i umieść rakietę na platformie startowej.
2.  Uruchom klasę `MainAI` w środowisku Java.
3.  W terminalu kOS wewnątrz gry wpisz:
    ```kerboscript
    run autopilot.
    ```
4.  Obserwuj, jak AI wynosi Twoją rakietę w kosmos!

---
*Projekt rozwijany hobbystycznie w ramach nauki uczenia maszynowego i inżynierii systemów kosmicznych.*
