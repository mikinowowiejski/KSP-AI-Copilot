CLEARSCREEN.
PRINT "Inicjalizacja Hardware in the loop...".

SET csvPath TO "0:/telemetria.csv".
SET cmdPath TO "0:/sterowanie.csv".

IF EXISTS(csvPath) { DELETEPATH(csvPath).}
LOG "Czas_s,Wysokosc_m,Predkosc_ms,Pochylenie_deg" TO csvPath.

PRINT "Systemy gotowe. Odliczanie do startu...".
WAIT 3.

SET targetPitch TO 90.0.
LOCK THROTTLE TO 1.0.

LOCK STEERING TO HEADING(90, targetPitch).

STAGE.
PRINT "START!".

UNTIL SHIP:ALTITUDE > 50000 OR ABORT{
    //FAZA 1

    SET t TO ROUND(MISSIONTIME, 2).
    SET alti TO ROUND(SHIP:ALTITUDE, 2).
    SET velo TO ROUND(SHIP:VELOCITY:SURFACE:MAG, 2).
    SET ptc TO ROUND(90 - VANG(UP:VECTOR, SHIP:FACING:FOREVECTOR), 2).

    LOG (t + "," + alti + "," + velo + "," + ptc) TO csvPath.

    //FAZA 2

    IF EXISTS(cmdPath) {

        LOCAL raw IS OPEN(cmdPath):READALL():STRING.

        IF raw:LENGTH > 0 {
            SET targetPitch TO raw:TONUMBER(targetPitch).
        }
    }

    PRINT "Wysokosc: " + alt + " m " AT (0,10).
    PRINT "Zadane pochylenie: " + ROUND(targetPitch, 2) + " deg " AT (0,11).
    PRINT "Aktualne pochylenie: " + ptc + " deg " AT (0,12).

    //FAZA 3

    IF MAXTHRUST = 0 {
        PRINT "Wykryto brak ciągu. Staging...".
        STAGE.
        WAIT 0.5.
    }
    WAIT 0.1.
}

PRINT " ".
PRINT "MECO (Main Engine Cut Off). Opuszczono gęstą atmosferę!".
LOCK THROTTLE TO 0.
UNLOCK ALL.
