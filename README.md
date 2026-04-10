# 🗺️ Visit App - Interaktywny Przewodnik Turystyczny

**Visit App** to mobilna aplikacja na system Android, która zachęca użytkowników do fizycznego eksplorowania miasta. Aplikacja łączy w sobie nawigację na żywo z mechaniką gry miejskiej — aby odkryć szczegóły i zdjęcia historycznych lub ciekawych miejsc, użytkownik musi fizycznie do nich podejść!

## ✨ Główne funkcje

* **📍 Mechanika odkrywania (Geofencing):** Atrakcje na mapie są początkowo zablokowane. Aplikacja stale śledzi pozycję GPS użytkownika. Gdy podejdziesz do miejsca na odległość **poniżej 50 metrów**, punkt zostaje odblokowany.
* **🗺️ Interaktywna mapa (OpenStreetMap):** Wykorzystanie biblioteki `osmdroid` do renderowania mapy offline/online bez opłat za API Google Maps.
* **🖼️ Dynamiczne markery:** Po odkryciu lokalizacji, standardowy znacznik na mapie zmienia się w okrągłą miniaturkę ze zdjęciem danego miejsca.
* **📊 Śledzenie postępów:** Wizualny pasek (Circular Progress Indicator) pokazujący, ile miejsc z puli zostało już odwiedzonych (np. "3/10").
* **💾 Trwały zapis (Zapisywanie stanu):** Odkryte miejsca są zapisywane w pamięci urządzenia (`SharedPreferences`), więc postęp nie znika po restarcie aplikacji.
* **📑 Panel Informacyjny (Bottom Sheet):** Wysuwany z dołu ekranu panel, który prezentuje nazwę, opis szczegółowy oraz opcjonalne funkcje (np. planowany tryb VR).
* **🚶 Ścieżka Znanej Osoby:** Dedykowany tryb mapy pozwalający na ukrycie standardowych atrakcji i skupienie się na specjalnie wyznaczonej ścieżce tematycznej.

## 🛠️ Architektura i Kod

Projekt został podzielony na menedżery o pojedynczej odpowiedzialności (Single Responsibility Principle), co ułatwia zarządzanie i rozwój kodu:

* `MainActivity` – Serce aplikacji; spina wszystkie moduły, zarządza uprawnieniami GPS i uruchamia w tle wątek sprawdzający dystans do atrakcji.
* `MapManager` – Odpowiada za integrację z `osmdroid`, nakładanie markerów, centrowanie kamery na użytkowniku oraz podmianę ikon.
* `AttractionsRepository` – Warstwa danych; wczytuje listę atrakcji z pliku `attractions.xml` (z folderu `assets`) i zarządza zapisanymi stanami w `SharedPreferences`.
* `BottomSheetManager` – Zarządza logiką wysuwanego panelu z opisami miejsc.
* `ProgressManager` – Aktualizuje interfejs użytkownika (wskaźnik procentowy i tekstowy) na podstawie liczby odwiedzonych miejsc.
* `BitmapHelper` – Klasa narzędziowa obrabiająca surowe zdjęcia do postaci ładnych, okrągłych ikon z białą obwódką.
* `FamousPathManager` – Zarządza widocznością specjalnej ścieżki tematycznej na mapie.

## 💻 Technologie i Biblioteki

* **Język:** Java
* **Platforma:** Android (Min. API dostosowane do nowoczesnych urządzeń)
* **Mapa:** [osmdroid](https://github.com/osmdroid/osmdroid) - potężna alternatywa dla Google Maps
* **UI Components:** Material Design (BottomSheetBehavior, CircularProgressIndicator)
* **Parsowanie danych:** Standardowe biblioteki XML (DOM DocumentBuilder)

## 🚀 Jak uruchomić projekt

1. Sklonuj to repozytorium na swój komputer.
2. Otwórz projekt w **Android Studio**.
3. Upewnij się, że w folderze `app/src/main/assets/` znajdują się:
   * Plik `attractions.xml` z danymi miejsc.
   * Folder `zdjecia/` zawierający grafiki, do których odwołuje się plik XML.
4. Zbuduj projekt i uruchom go na fizycznym urządzeniu z systemem Android (funkcje GPS działają najlepiej na prawdziwym telefonie, choć emulator pozwala na symulowanie trasy).
5. Zaakceptuj uprawnienia do śledzenia lokalizacji.

## 📝 Przykładowa struktura pliku XML (`attractions.xml`)
Aplikacja oczekuje następującej struktury danych do załadowania punktów na mapie:
```xml
<attractions>
    <attraction>
        <name>Rynek Główny</name>
        <lat>50.06143</lat>
        <lon>19.93658</lon>
        <description>Serce miasta, piękne miejsce pełne historii.</description>
        <photo>rynek.jpg</photo>
    </attraction>
    </attractions>
