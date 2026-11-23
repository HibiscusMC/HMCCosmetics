# PostgreSQL storage

HMCCosmetics can keep the equipped cosmetics and their colors in PostgreSQL, so several servers can share them.

## Configuration

In `config.yml`:

```yaml
database-settings:
  type: POSTGRESQL
  postgresql:
    host: "localhost"
    port: 5432
    database: "database"
    user: "username"
    # Leave empty to read the HMCCOSMETICS_POSTGRESQL_PASSWORD environment variable instead.
    password: ""
```

The database and the user must already exist. The plugin creates the `COSMETICDATABASE` table on start.
The PostgreSQL driver and HikariCP are part of the plugin jar; nothing is downloaded at runtime.

## How it behaves

- All database work runs on one dedicated thread, in the order it was requested. The server thread never waits for
  the database. A failed statement is retried (3 attempts); if the database is down when a player joins, the
  player's cosmetics are not loaded and nothing is saved for them, so stored data is never overwritten by an empty state.
- Each row has a `version`. A save only replaces the version this server loaded, so a late save from an old state
  (for example a slow quit save on a server the player already left) is discarded instead of overwriting what
  another server saved since. After a discarded save nothing more is written for that player until they join again.
- With this storage, a cosmetic change is saved one tick after it happens, so another server sees it without
  waiting for the player to quit. The other storages are unchanged.
- `/cosmetic dataclear <player>` deletes the row. A server that still holds the old state cannot bring it back;
  the player's next join starts from the empty state.

## Migrating from MySQL or SQLite

The table uses the same columns as the MySQL one (`UUID varchar(36)`, `COSMETICS text`) plus `VERSION`, and the
`COSMETICS` text has the same format, so rows can be copied as they are. For example, export `UUID` and `COSMETICS`
to a CSV file and load it with `\copy cosmeticdatabase (uuid, cosmetics) FROM 'file.csv' CSV`. Rows copied without a
version start at version 1 (the plugin adds the column when it is missing).

Stop all servers using the table before copying, then set `type: POSTGRESQL` and start them.
