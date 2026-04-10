🗺️ Visit App – Interactive Tourist Guide

Visit App is an Android mobile application that encourages users to physically explore the city. The app combines live navigation with urban game mechanics — to unlock details and photos of historical or interesting places, the user must physically approach them.

✨ Key Features
📍 Discovery Mechanic (Geofencing): Attractions on the map are initially locked. The app constantly tracks the user’s GPS position. When you get within 50 meters, the location is unlocked.
🗺️ Interactive Map (OpenStreetMap): Uses the osmdroid library to render maps offline/online without Google Maps API fees.
🖼️ Dynamic Markers: After discovering a location, the standard map marker changes into a circular thumbnail showing a photo of the place.
📊 Progress Tracking: A visual indicator (Circular Progress Indicator) shows how many locations have already been visited (e.g., "3/10").
💾 Persistent Storage: Discovered locations are saved in device memory (SharedPreferences), so progress is not lost after restarting the app.
📑 Information Panel (Bottom Sheet): A slide-up panel presenting the name, detailed description, and optional features (e.g., planned VR mode).
🚶 Famous Person Path: A dedicated map mode that hides standard attractions and focuses on a special themed route.
🛠️ Architecture and Code

The project is divided into single-responsibility managers (Single Responsibility Principle), which simplifies code maintenance and expansion:

MainActivity – The core of the app; connects all modules, manages GPS permissions, and runs a background thread checking the distance to attractions.
MapManager – Responsible for osmdroid integration, adding markers, centering the camera on the user, and updating icons.
AttractionsRepository – Data layer; loads attractions from the attractions.xml file (from the assets folder) and manages saved states in SharedPreferences.
BottomSheetManager – Handles the logic of the sliding information panel.
ProgressManager – Updates the UI (percentage and text indicators) based on visited locations.
BitmapHelper – Utility class that processes raw images into clean, circular icons with a white border.
FamousPathManager – Manages the visibility of the special themed path on the map.
💻 Technologies and Libraries
Language: Java
Platform: Android (minimum API adjusted for modern devices)
Map: osmdroid – a powerful alternative to Google Maps
UI Components: Material Design (BottomSheetBehavior, CircularProgressIndicator)
Data Parsing: Standard XML libraries (DOM DocumentBuilder)
🚀 How to Run the Project
Clone this repository to your computer.
Open the project in Android Studio.
Make sure the following are present in app/src/main/assets/:
attractions.xml file with location data.
zdjecia/ folder containing images referenced in the XML file.
Build the project and run it on a physical Android device (GPS features work best on a real phone, although the emulator allows route simulation).
Grant location tracking permissions.
📝 Example XML Structure (attractions.xml)

The application expects the following data structure to load points onto the map:

<attractions>
    <attraction>
        <name>Rynek Główny</name>
        <lat>50.06143</lat>
        <lon>19.93658</lon>
        <description>The heart of the city, a beautiful place full of history.</description>
        <photo>rynek.jpg</photo>
    </attraction>
</attractions>
