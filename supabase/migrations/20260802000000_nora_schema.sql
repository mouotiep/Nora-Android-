-- Extension nécessaire pour les UUID
create extension if not exists "pgcrypto";

-- ========== PROFILS UTILISATEURS ==========
-- Étend automatiquement la table auth.users gérée par Supabase Auth
create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  name text not null default 'Nouveau Membre',
  email text default '',
  whatsapp_number text default '',
  avatar_url text default '',
  shop_name text default '',
  has_shop boolean default false,
  kyc_status text not null default 'Non vérifié',
  n_coins_balance numeric not null default 0,
  created_at timestamptz not null default now()
);

alter table public.profiles enable row level security;

drop policy if exists "Les profils sont lisibles par tout utilisateur connecté" on public.profiles;
create policy "Les profils sont lisibles par tout utilisateur connecté"
  on public.profiles for select
  using (auth.uid() is not null);

drop policy if exists "Un utilisateur ne peut créer que son propre profil" on public.profiles;
create policy "Un utilisateur ne peut créer que son propre profil"
  on public.profiles for insert
  with check (auth.uid() = id);

drop policy if exists "Un utilisateur modifie son profil sans toucher son solde" on public.profiles;
create policy "Un utilisateur modifie son profil sans toucher son solde"
  on public.profiles for update
  using (auth.uid() = id)
  with check (auth.uid() = id and n_coins_balance = (select n_coins_balance from public.profiles where id = auth.uid()));

-- ========== ADMINISTRATEURS ==========
create table if not exists public.admins (
  user_id uuid primary key references auth.users(id) on delete cascade
);

alter table public.admins enable row level security;

drop policy if exists "Un utilisateur ne peut lire que son propre statut admin" on public.admins;
create policy "Un utilisateur ne peut lire que son propre statut admin"
  on public.admins for select
  using (auth.uid() = user_id);

create or replace function public.is_admin()
returns boolean
language sql
security definer
stable
as $$
  select exists (select 1 from public.admins where user_id = auth.uid());
$$;

-- ========== PRODUITS ==========
create table if not exists public.products (
  id uuid primary key default gen_random_uuid(),
  seller_id uuid references auth.users(id),
  title text not null,
  description text default '',
  price numeric not null default 0,
  stock integer not null default 0,
  category text default '',
  shop_name text default '',
  location text default '',
  image_url text default '',
  shop_id text default '',
  is_certified boolean default false,
  is_scammer boolean default false,
  created_at timestamptz not null default now()
);

alter table public.products enable row level security;

drop policy if exists "Les produits sont visibles publiquement" on public.products;
create policy "Les produits sont visibles publiquement"
  on public.products for select
  using (true);

drop policy if exists "Seuls les utilisateurs connectés peuvent publier/modifier/supprimer" on public.products;
create policy "Seuls les utilisateurs connectés peuvent publier/modifier/supprimer"
  on public.products for all
  using (auth.uid() is not null)
  with check (auth.uid() is not null);

-- ========== REELS ==========
create table if not exists public.reels (
  id uuid primary key default gen_random_uuid(),
  creator_id uuid references auth.users(id),
  caption text default '',
  category text default '',
  media_type text default 'Vidéo',
  media_url text default '',
  creator_name text default '',
  aspect_ratio text default '9:16',
  zoom_level real default 1.0,
  rotation_angle real default 0.0,
  start_sec real default 0.0,
  end_sec real default 0.0,
  likes_count integer default 0,
  views_count integer default 0,
  created_at timestamptz not null default now()
);

alter table public.reels enable row level security;

drop policy if exists "Les reels sont visibles publiquement" on public.reels;
create policy "Les reels sont visibles publiquement"
  on public.reels for select
  using (true);

drop policy if exists "Seuls les utilisateurs connectés peuvent publier/modifier/supprimer" on public.reels;
create policy "Seuls les utilisateurs connectés peuvent publier/modifier/supprimer"
  on public.reels for all
  using (auth.uid() is not null)
  with check (auth.uid() is not null);

-- ========== CONVERSATIONS (PRIVÉES) ==========
create table if not exists public.conversations (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references auth.users(id),
  contact_name text default '',
  last_message text default '',
  last_timestamp timestamptz not null default now(),
  user_phone text default '',
  user_email text default ''
);

alter table public.conversations enable row level security;

drop policy if exists "Conversations privées : propriétaire ou admin uniquement" on public.conversations;
create policy "Conversations privées : propriétaire ou admin uniquement"
  on public.conversations for select
  using (auth.uid() = user_id or public.is_admin());

drop policy if exists "Créer sa propre conversation, ou un admin peut en créer pour un client" on public.conversations;
create policy "Créer sa propre conversation, ou un admin peut en créer pour un client"
  on public.conversations for insert
  with check (auth.uid() = user_id or public.is_admin());

drop policy if exists "Modifier sa propre conversation, ou un admin" on public.conversations;
create policy "Modifier sa propre conversation, ou un admin"
  on public.conversations for update
  using (auth.uid() = user_id or public.is_admin());

-- ========== MESSAGES (dans une conversation privée) ==========
create table if not exists public.messages (
  id uuid primary key default gen_random_uuid(),
  conversation_id uuid not null references public.conversations(id) on delete cascade,
  sender text not null,
  text text not null,
  reply_to_text text default '',
  reply_to_sender text default '',
  status text not null default 'SENT',
  created_at timestamptz not null default now()
);

alter table public.messages enable row level security;

drop policy if exists "Messages visibles uniquement par le propriétaire de la conversation ou un admin" on public.messages;
create policy "Messages visibles uniquement par le propriétaire de la conversation ou un admin"
  on public.messages for select
  using (
    public.is_admin()
    or exists (
      select 1 from public.conversations c
      where c.id = conversation_id and c.user_id = auth.uid()
    )
  );

drop policy if exists "Écrire un message uniquement dans sa propre conversation, ou en tant qu'admin" on public.messages;
create policy "Écrire un message uniquement dans sa propre conversation, ou en tant qu'admin"
  on public.messages for insert
  with check (
    public.is_admin()
    or exists (
      select 1 from public.conversations c
      where c.id = conversation_id and c.user_id = auth.uid()
    )
  );

-- ========== ÉVÉNEMENTS DE GAIN N-COINS ==========
create table if not exists public.wallet_events (
  id uuid primary key default gen_random_uuid(),
  user_id uuid references auth.users(id),
  event_type text not null,
  amount numeric not null,
  meta text default '',
  created_at timestamptz not null default now()
);

alter table public.wallet_events enable row level security;

drop policy if exists "Un utilisateur peut proposer un événement pour lui-même" on public.wallet_events;
create policy "Un utilisateur peut proposer un événement pour lui-même"
  on public.wallet_events for insert
  with check (auth.uid() = user_id);

drop policy if exists "Un utilisateur peut lire ses propres événements" on public.wallet_events;
create policy "Un utilisateur peut lire ses propres événements"
  on public.wallet_events for select
  using (auth.uid() = user_id);

-- ========== TEMPS RÉEL ==========
alter publication supabase_realtime add table public.products;
alter publication supabase_realtime add table public.reels;
alter publication supabase_realtime add table public.conversations;
alter publication supabase_realtime add table public.messages;

-- ========== BUCKET MEDIA STORAGE POLICIES ==========
insert into storage.buckets (id, name, public)
values ('media', 'media', true)
on conflict (id) do nothing;

drop policy if exists "Lecture publique des fichiers média" on storage.objects;
create policy "Lecture publique des fichiers média"
  on storage.objects for select
  using (bucket_id = 'media');

drop policy if exists "Seuls les utilisateurs connectés peuvent uploader" on storage.objects;
create policy "Seuls les utilisateurs connectés peuvent uploader"
  on storage.objects for insert
  with check (bucket_id = 'media' and auth.uid() is not null);
