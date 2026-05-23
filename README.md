# Museum Helper

Android-приложение для управления музейными фондами. Позволяет просматривать, искать и редактировать данные об экспонатах, авторах, залах и музеях через облачную базу данных Supabase.

## Функциональность

- **Экспонаты** — поиск по названию, автору и музею; просмотр деталей; добавление, редактирование и удаление (требует авторизации)
- **Авторы** — поиск по имени; биография, даты жизни и фото; список работ автора
- **Музеи** — поиск по названию и городу; адрес, веб-сайт; список залов
- **Залы** — поиск по номеру, названию и музею; список экспонатов в зале
- **Авторизация** — вход по email и паролю через Supabase Auth; защита операций записи

## Стек технологий

| Слой | Технологии |
|---|---|
| Язык | Kotlin |
| UI | Android Views, ViewBinding, Material Design 3 |
| Архитектура | MVVM, StateFlow, SharedFlow |
| Сеть | Retrofit + Gson, Supabase Kotlin SDK |
| Авторизация | Supabase GoTrue |
| Изображения | Coil |
| Асинхронность | Kotlin Coroutines |
| Min SDK | 24 (Android 7.0) |

## Структура проекта

```
app/src/main/java/com/example/museumapp/
├── data/
│   ├── auth/          # AuthManager, состояния сессии
│   ├── cache/         # DataCache — кэш экспонатов и справочников
│   ├── model/         # Модели данных (Exhibit, Author, Hall, Museum)
│   └── repository/    # Репозитории + Retrofit API-интерфейс
├── ui/
│   ├── auth/          # Экран входа
│   ├── exhibits/      # Экспонаты (список, детали, добавление, редактирование)
│   ├── authors/       # Авторы
│   ├── halls/         # Залы
│   ├── museums/       # Музеи
│   └── main/          # Главный экран
└── util/
    └── ErrorUtils.kt  # Преобразование ошибок сети в читаемые сообщения
```

## Настройка

### 1. Клонировать репозиторий

```bash
git clone <repo-url>
cd MuseumApp
```

### 2. Создать `local.properties`

В корне проекта создайте файл `local.properties` (он уже добавлен в `.gitignore`) и укажите ключи Supabase:

```properties
sdk.dir=/path/to/android/sdk
supabase.url=https://bxrgvanoxllcwqvzkvny.supabase.co
supabase.anon.key=sb_publishable_JCl9V3yQIob6BLqreORDhg_bFucn7zf
```

### 3. Собрать проект

Откройте проект в Android Studio и нажмите **Run**, или выполните:

```bash
./gradlew assembleDebug
```

## База данных

Приложение работает с базой данных Supabase (PostgreSQL). Схема включает таблицы:

- `museum` — музеи (name, city, country, address, website)
- `creator` — авторы (name, biography, birth_date, death_date, photo_url)
- `hall` — залы (museum_id, hall_number, name, description, is_storage)
- `exhibit` — экспонаты (name, description, creation_year, current_hall_id, image_url)
- `exhibit_creator` — связь экспонат ↔ автор

Операции записи выполняются через RPC-функции Supabase с политиками Row Level Security.

## Авторизация

Доступ на чтение открыт для всех. Добавление, редактирование и удаление записей доступны только авторизованным пользователям. Учётные записи создаются в панели Supabase (**Authentication → Users**).
