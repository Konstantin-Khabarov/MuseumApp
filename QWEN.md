# MuseumApp - Android Приложение для Управления Музеями

## Обзор проекта

MuseumApp - это современное Android-приложение для управления музейными экспонатами, авторами и музеями. Приложение использует архитектуру MVVM и современный стек технологий Android разработки.

### Основные технологии
- **Язык программирования**: Kotlin
- **Платформа**: Android (API 24+)
- **Архитектура**: MVVM (Model-View-ViewModel)
- **Сборка**: Gradle с Kotlin DSL
- **Навигация**: Android Navigation Component
- **Сеть**: Retrofit + Ktor
- **Backend**: Supabase (PostgreSQL)
- **Асинхронность**: Kotlin Coroutines
- **Внедрение зависимостей**: Hilt (через libs)

### Структура проекта

```
app/
├── src/main/
│   ├── java/com/example/museumapp/
│   │   ├── MuseumApp.kt          # Основной класс приложения
│   │   ├── data/                 # Слой данных
│   │   │   ├── auth/            # Аутентификация
│   │   │   ├── cache/           # Кэширование
│   │   │   ├── model/           # Модели данных
│   │   │   └── repository/      # Репозитории
│   │   └── ui/                  # Слой UI
│   │       ├── auth/            # Аутентификация
│   │       ├── authors/         # Управление авторами
│   │       ├── exhibits/        # Управление экспонатами
│   │       ├── main/            # Главный экран
│   │       └── museums/         # Управление музеями
│   ├── res/                     # Ресурсы приложения
│   └── AndroidManifest.xml      # Манифест приложения
├── build.gradle.kts             # Конфигурация модуля
└── proguard-rules.pro          # Правила ProGuard
```

## Функциональность

### Основные экраны
- **Главный экран** (`MainActivity`) - точка входа в приложение
- **Аутентификация** (`LoginActivity`) - вход в систему
- **Управление экспонатами**:
  - `ExhibitManagementActivity` - список экспонатов
  - `ExhibitDetailActivity` - детали экспоната
  - `AddExhibitActivity` - добавление экспоната
  - `EditExhibitActivity` - редактирование экспоната
- **Управление музеями**:
  - `MuseumManagementActivity` - список музеев
  - `MuseumDetailActivity` - детали музея
- **Управление авторами**:
  - `AuthorManagementActivity` - список авторов
  - `AuthorDetailActivity` - детали автора
  - `AddAuthorActivity` - добавление автора

### Модели данных
Приложение работает с основными сущностями:
- Экспонаты (Exhibits)
- Музеи (Museums) 
- Авторы (Authors)

## Сборка и запуск

### Требования
- Android Studio Arctic Fox или новее
- JDK 11 или новее
- Android SDK API 24-36

### Команды сборки

#### Сборка APK
```bash
./gradlew assembleDebug
```

#### Сборка AAB
```bash
./gradlew bundleRelease
```

#### Запуск тестов
```bash
./gradlew test
./gradlew connectedAndroidTest
```

#### Очистка проекта
```bash
./gradlew clean
```

### Запуск в Android Studio
1. Откройте проект в Android Studio
2. Синхронизируйте Gradle (Tools → Sync Project with Gradle Files)
3. Выберите устройство или эмулятор
4. Нажмите кнопку "Run" (Shift + F10)

## Конвенции разработки

### Структура пакетов
- `data` - слой данных (репозитории, модели, API)
- `ui` - слой пользовательского интерфейса
- `data.model` - модели данных
- `data.repository` - репозитории для доступа к данным
- `ui.*` - экраны и компоненты UI

### Архитектура приложения
Приложение следует архитектурному шаблону MVVM:
- **Models** - классы данных
- **Views** - Activity и Fragment
- **ViewModels** - классы для управления состоянием UI

### Использование View Binding
Включено в build.gradle:
```kotlin
buildFeatures {
    viewBinding = true
}
```

### Сетевые запросы
Для сетевых запросов используется Retrofit с Supabase в качестве бэкенда:
- Аутентификация через Gotrue
- Работа с данными через PostgREST
- Хранение файлов через Storage

### Асинхронные операции
Для асинхронных операций используются Kotlin Coroutines:
- `viewModelScope` для ViewModel
- `lifecycleScope` для UI компонентов

## Зависимости

### Основные библиотеки
- **AndroidX**: Core, AppCompat, Material Design, Navigation
- **Kotlin**: Coroutines, Serialization
- **Architecture**: ViewModel, LiveData, Room (косвенно)
- **Networking**: Retrofit, Gson, Ktor
- **Supabase**: Auth, Database, Storage
- **Testing**: JUnit, Espresso

### Версии
- Compile SDK: 36
- Min SDK: 24
- Target SDK: 36
- Kotlin: указано через libs.plugins.kotlin.android
- Android Gradle Plugin: указано через libs.plugins.android.application

## Особенности проекта

### Интернационализация
В AndroidManifest.xml обнаружены русские строки, что указывает на поддержку русского языка.

### Безопасность
- Используются современные практики безопасности
- Поддержка шифрования через Android Security Crypto
- Правила ProGuard для минификации кода

### Производительность
- Включено viewBinding для оптимизации производительности UI
- Использование современных компонентов Android Jetpack
- Оптимизация сборки через Gradle

## Примечания для разработки

1. **Gradle Version Catalog**: Зависимости управляются через libs, что упрощает управление версиями
2. **Namespace**: `com.example.museumapp` - стандартное пространство имен
3. **Version**: Версия 1.0 с versionCode 1
4. **Build Types**: настроен debug и release сборки
5. **Test Runner**: AndroidJUnitRunner для UI тестов

## Дальнейшие улучшения

Рекомендуется добавить:
- Hilt/Dagger для внедрения зависимостей
- Room для локального хранения данных
- CoIL для загрузки изображений
- WorkManager для фоновых задач
- Feature modules для масштабирования