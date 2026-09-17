-- Скрипт быстрой очистки всех активных инстансов, задач и истории Camunda (ACT_RU_*, ACT_HI_*)
-- Сохраняет саму структуру таблиц и зарегистрированных пользователей

DO $$ 
DECLARE 
    r RECORD;
BEGIN
    FOR r IN (
        SELECT tablename 
        FROM pg_tables 
        WHERE schemaname = current_schema() 
          AND (tablename ILIKE 'act_ru_%' OR tablename ILIKE 'act_hi_%')
    ) LOOP
        EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' CASCADE';
        RAISE NOTICE 'Truncated table: %', r.tablename;
    END LOOP;
END $$;
