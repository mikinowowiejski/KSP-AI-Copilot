@LAZYGLOBAL OFF.

wait until ship:unpacked.

CLEARSCREEN.

LOCAL cmdPath IS "0:/sterowanie.csv".
LOCAL telPath IS "0:/telemetria.csv".
LOCAL resPath IS "0:/result.csv".

IF EXISTS(resPath) {
    DELETEPATH(resPath).
}

PRINT "=== SYSTEM KSP AI: TRENING HYBRYDOWY ===".
PRINT "Oczekiwanie na reset Javy...".
WAIT 2.

LOCAL targetPitch IS 90.0.
LOCAL currentThrottle IS 1.0.
LOCAL start_time IS TIME:SECONDS.



LOCAL max_alt IS 0.
LOCAL is_crashed IS FALSE.

LOCAL flight_phase IS 1.

LOCK STEERING TO HEADING(90, targetPitch).
LOCK THROTTLE TO currentThrottle.
PRINT "3... 2... 1... ZAPLON!".
STAGE.

UNTIL SHIP:PERIAPSIS >= 75000 OR is_crashed {

    IF MAXTHRUST = 0 {
        IF STAGE:NUMBER > 0 { STAGE. WAIT 0.5. }
    }

    IF flight_phase = 1 {
        SET currentThrottle TO 1.0.
        IF SHIP:APOAPSIS >= 80000 {
            SET flight_phase TO 2.
            SET currentThrottle TO 0.0.
        }
    }
    ELSE IF flight_phase = 2 {
       SET currentThrottle TO 0.0.
        IF SHIP:ALTITUDE > 70000 AND ETA:APOAPSIS < 30 {
            SET flight_phase TO 3.
            SET currentThrottle TO 1.0.
        }
    }
    ELSE IF flight_phase = 3 {
        SET currentThrottle to 1.0.
    }

    IF SHIP:ALTITUDE > max_alt { SET max_alt TO SHIP:ALTITUDE. }

    LOCAL lockPath IS cmdPath + ".lock".

    IF EXISTS(cmdPath) AND NOT EXISTS(lockPath) {
        LOCAL raw IS OPEN(cmdPath):READALL():STRING.
        IF raw:LENGTH > 0 { SET targetPitch TO raw:TONUMBER(targetPitch). }
    }

    LOCAL currentApo IS ROUND(SHIP:APOAPSIS, 2).
    LOCAL etaApo IS ROUND(ETA:APOAPSIS, 2).
    IF etaApo > 100000 { SET etaApo TO 999. }

    LOCAL currentTWR IS 0.
    IF SHIP:MASS > 0 { SET currentTWR TO SHIP:AVAILABLETHRUST / (SHIP:MASS * 9.81). }


    PRINT "Wysokosc:  " + ROUND(SHIP:ALTITUDE) + " m       " AT (2, 4).
    PRINT "Predkosc:  " + ROUND(SHIP:AIRSPEED) + " m/s     " AT (2, 5).
    PRINT "Apoapsis:  " + currentApo + " m                " AT (2, 6).
    PRINT "Periapsis: " + ROUND(SHIP:PERIAPSIS) + " m      " AT (2, 7).
    PRINT "ETA Apo:   " + etaApo + " s                  " AT (2, 8).
    PRINT "Kat (AI):  " + ROUND(targetPitch, 2) + " deg    " AT (2, 10).
    PRINT "Faza lotu: " + flight_phase + "                " AT (2, 11).


    LOCAL logLine IS ROUND(TIME:SECONDS - start_time, 2) + "," + ROUND(SHIP:ALTITUDE, 2) + "," + ROUND(SHIP:AIRSPEED, 2) + "," + ROUND(currentTWR, 2) + "," + ROUND(SHIP:Q, 4) + "," + currentApo + "," + etaApo + "," + ROUND(targetPitch, 2).
    LOG logLine TO telPath.

    IF SHIP:ALTITUDE < 50000 AND targetPitch < -10 {
        PRINT "KATASTROFA: Nos rakiety opadł za bardzo!". SET is_crashed TO TRUE.
    }
    IF SHIP:ALTITUDE > 1000 AND SHIP:VERTICALSPEED < -10 AND SHIP:ALTITUDE < 60000 {
        PRINT "KATASTROFA: Rakieta zaczęła spadać!". SET is_crashed TO TRUE.
    }
    IF MAXTHRUST = 0 AND STAGE:NUMBER = 0 AND SHIP:PERIAPSIS < (SHIP:APOAPSIS - 5000) {
        PRINT "KATASTROFA: Zabrakło paliwa przed orbitą!". SET is_crashed TO TRUE.
    }

    IF SHIP:APOAPSIS > 100000 AND SHIP:PERIAPSIS < 70000 {
        PRINT "KATASTROFA: Rakieta przestrzeliła orbitę (Pionowy lot)!". SET is_crashed TO TRUE.
    }

    WAIT 0.1.
}

// === KONIEC LOTU I OCENA ===
LOCK THROTTLE TO 0.
UNLOCK STEERING.
CLEARSCREEN.

LOCAL finalDV IS 0.
IF NOT is_crashed {
    PRINT "=== ORBITA OSIAGNIETA! ===".

    LOCAL resList IS LIST().
    LIST RESOURCES IN resList.

    FOR res IN resList {
        IF res:NAME = "LiquidFuel" { SET finalDV TO finalDV + res:AMOUNT. }
    }
} ELSE {
    PRINT "=== MISJA ZAKONCZONA PORAZKA ===".
}

LOCAL crashedNum IS 0.
IF is_crashed { SET crashedNum TO 1. }

LOCAL resultLine IS ROUND(max_alt) + "," + ROUND(SHIP:APOAPSIS) + "," + ROUND(SHIP:PERIAPSIS) + "," + ROUND(finalDV) + "," + crashedNum.
LOG resultLine TO resPath.

PRINT "Zapisano wynik epoki. RESTART ZA 3 SEKUNDY...".
WAIT 3.
KUNIVERSE:REVERTTOLAUNCH().