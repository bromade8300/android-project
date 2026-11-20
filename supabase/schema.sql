-- À exécuter dans le SQL Editor du projet Supabase.
-- Les politiques RLS protègent les données même si un client modifie ses requêtes.
create table if not exists public.tastings (
    id uuid primary key,
    -- La clé étrangère relie la fiche au compte et supprime ses fiches lorsque le compte est supprimé.
    owner_id uuid not null references auth.users(id) on delete cascade,
    author text not null check (char_length(author) between 1 and 80),
    wine text not null check (char_length(trim(wine)) between 1 and 120),
    vintage text not null check (vintage ~ '^[0-9]{4}$' and vintage::integer between 1900 and 2100),
    color text not null check (color in ('Rouge', 'Blanc', 'Rosé', 'Effervescent')),
    region text not null default '',
    rating integer not null check (rating between 1 and 5),
    notes text not null default '' check (char_length(notes) <= 2000),
    photo_uri text not null default '',
    created_at bigint not null
);

-- Les index facilitent le tri par date et la recherche par propriétaire.
create index if not exists tastings_created_at_idx on public.tastings(created_at desc);
create index if not exists tastings_owner_idx on public.tastings(owner_id);
alter table public.tastings enable row level security;
-- Retirer aussi les privilèges automatiques Supabase (TRUNCATE, TRIGGER, REFERENCES).
revoke all on public.tastings from anon, authenticated;
grant select, insert, update, delete on public.tastings to authenticated;

-- Cette politique autorise la lecture à tous les comptes connectés. Le filtre du carnet est appliqué dans l’application.
drop policy if exists "Connected users read tastings" on public.tastings;
create policy "Connected users read tastings" on public.tastings
    for select to authenticated using ((select auth.uid()) is not null);
drop policy if exists "Owners create tastings" on public.tastings;
create policy "Owners create tastings" on public.tastings
    for insert to authenticated with check ((select auth.uid()) = owner_id);
-- USING vérifie le propriétaire de la ligne existante. WITH CHECK vérifie celui de la ligne après modification.
drop policy if exists "Owners update tastings" on public.tastings;
create policy "Owners update tastings" on public.tastings
    for update to authenticated using ((select auth.uid()) = owner_id)
    with check ((select auth.uid()) = owner_id);
drop policy if exists "Owners delete tastings" on public.tastings;
create policy "Owners delete tastings" on public.tastings
    for delete to authenticated using ((select auth.uid()) = owner_id);

-- Photos publiques pour le partage ; les écritures sont limitées au dossier de l'auteur.
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('photos', 'photos', true, 5242880, array['image/jpeg'])
on conflict (id) do nothing;

drop policy if exists "Owners upload photos" on storage.objects;
create policy "Owners upload photos" on storage.objects
    for insert to authenticated with check (
        bucket_id = 'photos' and (storage.foldername(name))[1] = (select auth.uid())::text
    );
drop policy if exists "Owners remove photos" on storage.objects;
create policy "Owners remove photos" on storage.objects
    for delete to authenticated using (
        bucket_id = 'photos' and (storage.foldername(name))[1] = (select auth.uid())::text
    );

-- La suppression via Storage nécessite aussi la lecture des métadonnées de ses propres fichiers.
drop policy if exists "Owners read photo metadata" on storage.objects;
create policy "Owners read photo metadata" on storage.objects
    for select to authenticated using (
        bucket_id = 'photos' and (storage.foldername(name))[1] = (select auth.uid())::text
    );
