-- Скрипт полного удаления всех таблиц Camunda Engine (ACT_*) в PostgreSQL
-- Не затрагивает таблицы бизнес-логики приложения (app_user, course, learning_task и др.)

DO $$ 
DECLARE 
    r RECORD;
BEGIN
    FOR r IN (
        SELECT tablename 
        FROM pg_tables 
        WHERE schemaname = current_schema() 
          AND tablename ILIKE 'act_%'
    ) LOOP
        EXECUTE 'DROP TABLE IF EXISTS ' || quote_ident(r.tablename) || ' CASCADE';
        RAISE NOTICE 'Dropped table: %', r.tablename;
    END LOOP;
END $$;
