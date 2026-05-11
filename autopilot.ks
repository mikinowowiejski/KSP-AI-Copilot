@LAZYGLOBAL OFF.
CLEARSCREEN.

LOCAL cmdPath IS "0:/sterowanie.csv".
LOCAL telPath IS "0:/telemetria.csv".

PRINT " KSP AI COPILOT - SYSTEM GOTOWY ".
PRINT "Synchronizacja z Java...".
WAIT 1.

LOCAL targetPitch IS 90.0.
LOCAL start_time IS TIME:SECONDS.

 PRINT "Uruchamianie procedury startowej...".
LOCK THROTTLE TO 1.0.
LOCK STEERING TO HEADING(90, targetPitch).

WAIT 1.
PRINT "3... 2... 1... ZAPLON!".
STAGE.

CLEARSCREEN.


UNTIL SHIP:APOAPSIS > 80000 {

    IF MAXTHRUST = 0 {
        PRINT "Flameout! Odrzucanie czlonu..." AT (0, 10).
        STAGE.
        WAIT 0.5.
    }

    IF EXISTS(cmdPath) {
        LOCAL raw IS OPEN(cmdPath):READALL():STRING.
        IF raw:LENGTH > 0 {
            SET targetPitch TO raw:TONUMBER(targetPitch).
        }
    }

    LOCAL current_time IS ROUND(TIME:SECONDS - start_time, 2).
    LOCAL alti IS ROUND(SHIP:ALTITUDE, 2).
    LOCAL spd IS ROUND(SHIP:AIRSPEED, 2).

    // Format: Czas_s, Wysokosc_m, Predkosc_ms, Pochylenie_deg
    LOCAL logLine IS current_time + "," + alti + "," + spd + "," + ROUND(targetPitch, 2).
    LOG logLine TO telPath.

    PRINT "=== FAZA 1: WZNOSZENIE ATMOSFERYCZNE ===" AT (0, 2).
    PRINT "Wysokosc:       " + ROUND(SHIP:ALTITUDE) + " m      " AT (0, 4).
    PRINT "Apoapsis (Cel): " + ROUND(SHIP:APOAPSIS) + " / 80000 m  " AT (0, 5).
    PRINT "Predkosc:       " + ROUND(SHIP:AIRSPEED) + " m/s    " AT (0, 6).
    PRINT "Kąt AI (Pitch): " + ROUND(targetPitch, 2) + " st     " AT (0, 7).

    WAIT 0.1.
}


CLEARSCREEN.
PRINT "=== FAZA 2: DRYF POZA ATMOSFERE ===" AT (0, 2).
PRINT "Osiagnieto cel Apoapsis. Odciecie silnika glownego." AT (0, 4).

LOCK THROTTLE TO 0.0.
SET targetPitch TO 0. // Kładziemy rakietę poziomo (równolegle do planety)

UNTIL SHIP:ALTITUDE > 70000 {
    PRINT "Wysokosc: " + ROUND(SHIP:ALTITUDE) + " / 70000 m (Wyjscie z prozni)" AT (0, 6).
    PRINT "Czas do Apoapsis: " + ROUND(ETA:APOAPSIS) + " s    " AT (0, 7).
    WAIT 0.5.
}

CLEARSCREEN.
PRINT "=== FAZA 3: CYRKULARYZACJA ORBITY ===" AT (0, 2).
PRINT "Oczekiwanie na optymalny moment zaplonu..." AT (0, 4).

UNTIL ETA:APOAPSIS < 40 {
    PRINT "Zaplon za: " + ROUND(ETA:APOAPSIS - 40) + " s    " AT (0, 6).
    WAIT 0.1.
}

PRINT "burn!" AT (0, 8).
LOCK THROTTLE TO 1.0.

UNTIL SHIP:PERIAPSIS > 75000 {

    IF MAXTHRUST = 0 { STAGE. WAIT 0.5. }
    PRINT "Podnoszenie Periapsis: " + ROUND(SHIP:PERIAPSIS) + " / 75000 m   " AT (0, 10).
    WAIT 0.1.
}


LOCK THROTTLE TO 0.0.
UNLOCK STEERING.
CLEARSCREEN.
PRINT "MISJA ZAKONCZONA SUKCESEM!".
PRINT "Koncowe Apoapsis:  " + ROUND(SHIP:APOAPSIS) + " m".
PRINT "Koncowe Periapsis: " + ROUND(SHIP:PERIAPSIS) + " m".