-- Ein Tagesplan je Kalendertag und seine Punkte in fester Reihenfolge.
--
-- fixed_at als INTEGER (Minuten nach Mitternacht, 0..1440) und nicht als TIME:
-- "24:00" ist keine gueltige Uhrzeit, wird aber gebraucht ("Im Bett um 24:00"
-- heisst Mitternacht am Ende des Tages, liegt also auf dem Folgetag). INTEGER
-- und nicht SMALLINT, weil ddl-auto=validate JDBC-Typcodes vergleicht und
-- SMALLINT ein Short-Feld in der Entity erzwingen wuerde.
--
-- done_at als TIMESTAMPTZ: Der Zeitpunkt ist ein Instant. Die Zone zum Anzeigen
-- kommt immer aus der Clock-Bean, nie aus der Spalte.

CREATE TABLE day (
  id    INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  date  DATE NOT NULL UNIQUE        -- genau ein Plan je Kalendertag
);

CREATE TABLE item (
  id        INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  day_id    INTEGER NOT NULL REFERENCES day (id) ON DELETE CASCADE,
  position  INTEGER NOT NULL,       -- 0-basiert, die Reihenfolge im Tag
  title     TEXT    NOT NULL,
  duration  INTEGER,                -- Minuten; leer und ohne fixed_at = offener Punkt
  fixed_at  INTEGER,                -- Minuten nach Mitternacht, 1440 = 24:00
  done_at   TIMESTAMPTZ,            -- leer = noch nicht erledigt
  UNIQUE (day_id, position),
  CHECK (duration IS NULL OR duration > 0),
  CHECK (fixed_at IS NULL OR fixed_at BETWEEN 0 AND 1440)
);
