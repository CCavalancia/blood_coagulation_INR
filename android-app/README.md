# INR Measurement - Android App (Phase 2A)

Android application for measuring Prothrombin Time (PT) and International Normalized Ratio (INR) from blood coagulation videos.

## Overview

This Android app is the Phase 2A implementation of the blood coagulation INR measurement system. It captures video of blood samples and processes them using computer vision algorithms to calculate PT and INR values.

## Features

- **Video Recording**: Capture high-quality video of blood coagulation process
- **Real-time Analysis**: Process video frames to extract motion curves
- **PT/INR Calculation**: Automatic calculation of Prothrombin Time and INR values
- **Measurement History**: Store and view previous measurements
- **Result Sharing**: Export and share measurement results

## Technical Stack

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI Framework**: Jetpack Compose + XML layouts
- **Camera**: CameraX
- **Image Processing**: OpenCV for Android
- **Database**: Room
- **Async**: Kotlin Coroutines
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## Project Structure

```
android-app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/inr/measurement/
│   │   │   │   ├── data/
│   │   │   │   │   ├── database/      # Room database
│   │   │   │   │   ├── models/        # Data models
│   │   │   │   │   └── repository/    # Data repository
│   │   │   │   ├── processing/
│   │   │   │   │   ├── VideoFrameExtractor.kt    # Frame extraction (extract.py equivalent)
│   │   │   │   │   ├── MotionCurveGenerator.kt   # Motion curve generation (start_time.m/stop_time.m)
│   │   │   │   │   └── INRCalculator.kt          # PT/INR calculation (compute_pt.m)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── main/          # Main screen
│   │   │   │   │   ├── measurement/   # Video recording
│   │   │   │   │   ├── results/       # Results display
│   │   │   │   │   └── history/       # Measurement history
│   │   │   │   └── utils/             # Utility classes
│   │   │   ├── res/
│   │   │   │   ├── layout/            # XML layouts
│   │   │   │   ├── values/            # Strings, colors, themes
│   │   │   │   └── xml/               # Configuration files
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/               # Instrumented tests
│   │   └── test/                      # Unit tests
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Algorithm Implementation

The Android app implements the same algorithms as the MATLAB/Python prototype:

1. **Frame Extraction** (`VideoFrameExtractor.kt`)
   - Equivalent to `extract.py`
   - Extracts video frames using Android MediaMetadataRetriever
   - Converts frames to OpenCV Mat format

2. **Pipette Motion Curve** (`MotionCurveGenerator.kt`)
   - Equivalent to `start_time.m`
   - Generates motion curve for pipette movement
   - Detects start time of coagulation

3. **Particle Motion Curve** (`MotionCurveGenerator.kt`)
   - Equivalent to `stop_time.m`
   - Generates motion curve for particle movement
   - Uses circular cropping (circlecrop.m/circlecropbw.m)
   - Detects stop time of coagulation

4. **PT/INR Calculation** (`INRCalculator.kt`)
   - Equivalent to `compute_pt.m`
   - Uses knee point detection (knee_pt.m)
   - Calculates PT = stop_time - start_time
   - Calculates INR = (PT / MNPT)^ISI

## Building the App

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK with API 34
- OpenCV Android SDK 4.8.0

### Build Instructions

1. Clone the repository
2. Open Android Studio
3. Open the `android-app` directory
4. Sync Gradle files
5. Build and run on device or emulator

```bash
./gradlew assembleDebug
```

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## Usage

1. **Grant Permissions**: Allow camera and storage permissions
2. **Start Measurement**: Tap "Start Measurement" on main screen
3. **Position Sample**: Place blood sample in holder
4. **Record Video**: Tap "Record" button
5. **Stop Recording**: Tap "Stop" after coagulation is visible
6. **Analyze**: Tap "Analyze" to process video
7. **View Results**: See PT and INR values
8. **Save**: Save results to history

## Configuration

Key parameters can be adjusted in the code:

- **ISI** (International Sensitivity Index): Default = 1.0
- **MNPT** (Mean Normal PT): Default = 12.0 seconds
- **Video Quality**: Configurable in CameraX settings
- **Frame Rate**: Adjustable for processing

## Data Storage

- **Videos**: Stored in `app-specific-storage/Videos/`
- **Results**: Stored in Room database
- **Exports**: JSON format in `app-specific-storage/Results/`

## Dependencies

Key libraries used:

- AndroidX Core, AppCompat, Material
- Jetpack Compose
- CameraX
- OpenCV 4.8.0
- Room Database
- Kotlin Coroutines
- Gson for JSON

See `app/build.gradle.kts` for complete dependency list.

## Development Roadmap

### Phase 2A (Current)
- ✅ Project structure
- ⏳ Camera implementation
- ⏳ Video processing
- ⏳ INR calculation
- ⏳ Results storage

### Phase 2B (Future)
- Cloud sync
- Multi-user support
- Advanced analytics
- Bluetooth device integration

## License

See main repository LICENSE file.

## Related Files

This Android app implements algorithms from the parent repository:
- `extract.py` → `VideoFrameExtractor.kt`
- `start_time.m` → `MotionCurveGenerator.generatePipetteMotionCurve()`
- `stop_time.m` → `MotionCurveGenerator.generateParticleMotionCurve()`
- `compute_pt.m` → `INRCalculator.calculateINR()`
- `circlecrop.m`/`circlecropbw.m` → `MotionCurveGenerator.extractCircularRegion()`
- `knee_pt.m` → `MotionCurveGenerator.detectKneePoint()`

## Support

For issues and questions, please refer to the main repository.
