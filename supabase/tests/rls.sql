-- Test d'intégration à exécuter avec le rôle administrateur via le MCP Supabase.
-- Deux identités temporaires et une dégustation sont créées, puis annulées par ROLLBACK.
-- Aucun e-mail n'est envoyé et aucune donnée de test ne reste dans le projet.
begin;

select set_config('test.owner_id', gen_random_uuid()::text, true);
select set_config('test.other_id', gen_random_uuid()::text, true);
select set_config('test.tasting_id', gen_random_uuid()::text, true);

insert into auth.users (id, email, role, aud)
values
    (current_setting('test.owner_id')::uuid, 'rls-owner-' || current_setting('test.owner_id') || '@example.invalid', 'authenticated', 'authenticated'),
    (current_setting('test.other_id')::uuid, 'rls-other-' || current_setting('test.other_id') || '@example.invalid', 'authenticated', 'authenticated');

select set_config('request.jwt.claims', json_build_object(
    'sub', current_setting('test.owner_id'), 'role', 'authenticated'
)::text, true);
set local role authenticated;

do $$
declare
    changed integer;
begin
    if has_table_privilege('authenticated', 'public.tastings', 'TRUNCATE') then
        raise exception 'FAIL: privilège TRUNCATE accordé au client';
    end if;

    insert into public.tastings (id, owner_id, author, wine, vintage, color, region, rating, notes, photo_uri, created_at)
    values (current_setting('test.tasting_id')::uuid, auth.uid(), 'Test RLS', 'Vin de test', '2021', 'Rouge', 'Bordeaux', 4, '', '', 1);

    if not exists (select 1 from public.tastings where id = current_setting('test.tasting_id')::uuid) then
        raise exception 'FAIL: lecture de sa propre dégustation';
    end if;

    update public.tastings set notes = 'Avis modifié' where id = current_setting('test.tasting_id')::uuid;
    get diagnostics changed = row_count;
    if changed <> 1 then raise exception 'FAIL: modification par auteur'; end if;

    begin
        update public.tastings set owner_id = current_setting('test.other_id')::uuid
        where id = current_setting('test.tasting_id')::uuid;
        raise exception 'FAIL: changement de propriétaire autorisé';
    exception when insufficient_privilege then null;
    end;

    begin
        update public.tastings set rating = 6 where id = current_setting('test.tasting_id')::uuid;
        raise exception 'FAIL: note invalide acceptée';
    exception when check_violation then null;
    end;

    begin
        update public.tastings set wine = ' ' where id = current_setting('test.tasting_id')::uuid;
        raise exception 'FAIL: nom vide accepté';
    exception when check_violation then null;
    end;
end $$;

-- Une seconde identité peut lire le fil partagé, mais ne peut pas écrire à la place de l'auteur.
select set_config('request.jwt.claims', json_build_object(
    'sub', current_setting('test.other_id'), 'role', 'authenticated'
)::text, true);

do $$
declare
    changed integer;
begin
    if not exists (select 1 from public.tastings where id = current_setting('test.tasting_id')::uuid) then
        raise exception 'FAIL: lecture du fil partagé';
    end if;

    update public.tastings set notes = 'Modification interdite' where id = current_setting('test.tasting_id')::uuid;
    get diagnostics changed = row_count;
    if changed <> 0 then raise exception 'FAIL: modification par autre utilisateur'; end if;

    delete from public.tastings where id = current_setting('test.tasting_id')::uuid;
    get diagnostics changed = row_count;
    if changed <> 0 then raise exception 'FAIL: suppression par autre utilisateur'; end if;

    begin
        insert into public.tastings (id, owner_id, author, wine, vintage, color, region, rating, notes, photo_uri, created_at)
        values (gen_random_uuid(), current_setting('test.owner_id')::uuid, 'Usurpation', 'Interdit', '2021', 'Rouge', '', 4, '', '', 1);
        raise exception 'FAIL: création au nom d''un autre utilisateur';
    exception when insufficient_privilege then null;
    end;
end $$;

-- L'auteur conserve son droit de suppression.
select set_config('request.jwt.claims', json_build_object(
    'sub', current_setting('test.owner_id'), 'role', 'authenticated'
)::text, true);

do $$
declare
    changed integer;
begin
    delete from public.tastings where id = current_setting('test.tasting_id')::uuid;
    get diagnostics changed = row_count;
    if changed <> 1 then raise exception 'FAIL: suppression par auteur'; end if;
end $$;

reset role;
set local role anon;
do $$
begin
    begin
        perform 1 from public.tastings;
        raise exception 'FAIL: accès sans connexion';
    exception when insufficient_privilege then null;
    end;
end $$;

rollback;
select 'PASS: CRUD auteur, lecture partagée, isolation des écritures, contraintes et accès anonyme' as result;
