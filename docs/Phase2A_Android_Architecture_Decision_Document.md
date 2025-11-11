# Phase 2A Android Architecture Decision Document
## Blood Coagulation PT/INR Measurement System

**Version:** 2.0
**Date:** 2025-11-11
**Status:** Proposed
**Author:** Development Team

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Project Context](#2-project-context)
3. [Architecture Overview](#3-architecture-overview)
4. [Core Architecture Decisions](#4-core-architecture-decisions)
5. [Technology Stack](#5-technology-stack)
6. [Module Architecture](#6-module-architecture)
7. [Data Architecture](#7-data-architecture)
8. [Security Architecture](#8-security-architecture)
9. [Performance & Optimization](#9-performance--optimization)
10. [Testing Strategy](#10-testing-strategy)
11. [Development Guidelines](#11-development-guidelines)
12. [Migration Path](#12-migration-path)
13. [Risk Assessment](#13-risk-assessment)
14. [Appendices](#14-appendices)

---

## 1. Executive Summary

### 1.1 Purpose
This document outlines the architectural decisions for Phase 2A development of the Blood Coagulation PT/INR Measurement Android application. The system will transform the existing MATLAB/Python-based video analysis system into a mobile-native Android application.

### 1.2 Key Objectives
- **Medical-Grade Accuracy**: Maintain the precision of PT/INR measurements from the existing system
- **Real-time Processing**: Enable on-device video capture and analysis
- **Offline Capability**: Support measurements without internet connectivity
- **Data Security**: Ensure HIPAA compliance and patient data protection
- **Scalability**: Support future feature additions and platform extensions
- **Performance**: Achieve sub-30 second measurement completion time

### 1.3 Architecture Summary
The application will utilize:
- **Pattern**: Clean Architecture with MVVM presentation layer
- **Language**: Kotlin with Coroutines for asynchronous operations
- **DI Framework**: Hilt for dependency injection
- **Image Processing**: OpenCV Android SDK + TensorFlow Lite
- **Storage**: Room Database with encrypted SharedPreferences
- **UI**: Jetpack Compose with Material Design 3

---

## 2. Project Context

### 2.1 Current System Analysis

#### Existing Components
```
┌─────────────────────────────────────────────┐
│         Current System (MATLAB/Python)      │
├─────────────────────────────────────────────┤
│ 1. Video Input (video.mp4)                 │
│ 2. Frame Extraction (extract.py)           │
│ 3. Pipette Motion Analysis (start_time.m)  │
│ 4. Particle Motion Analysis (stop_time.m)  │
│ 5. PT/INR Calculation (compute_pt.m)       │
└─────────────────────────────────────────────┘
```

#### Processing Pipeline
1. Extract video frames (~22 seconds)
2. Generate pipette motion curve (~25 seconds)
3. Generate particle motion curve (~28 seconds)
4. Compute PT/INR values (~7ms)
**Total:** ~75 seconds

#### Migration Requirements
- Port image processing algorithms from MATLAB to Android
- Implement real-time video capture using Android Camera2 API
- Optimize for mobile hardware constraints
- Ensure measurement accuracy parity with desktop system

### 2.2 Stakeholder Requirements

#### Clinical Users
- Simple, intuitive interface
- Quick measurement turnaround (<30 seconds)
- Clear result visualization
- Historical data tracking
- Export capabilities (PDF, CSV)

#### Regulatory Requirements
- FDA 21 CFR Part 11 compliance
- HIPAA data protection
- Audit trail for measurements
- Data integrity validation

#### Technical Requirements
- Support Android 8.0+ (API 26+)
- Work on devices with 4GB+ RAM
- Camera with 1080p resolution minimum
- Offline-first architecture

---

## 3. Architecture Overview

### 3.1 High-Level Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │
│  │   Compose    │  │  ViewModels  │  │   UI State   │        │
│  │   Screens    │◄─┤   (MVVM)     │◄─┤   Managers   │        │
│  └──────────────┘  └──────────────┘  └──────────────┘        │
└────────────────────────────────────────────────────────────────┘
                            │
┌────────────────────────────────────────────────────────────────┐
│                       DOMAIN LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │
│  │  Use Cases   │  │   Business   │  │  Domain      │        │
│  │  (Interactors)│ │   Rules      │  │  Models      │        │
│  └──────────────┘  └──────────────┘  └──────────────┘        │
└────────────────────────────────────────────────────────────────┘
                            │
┌────────────────────────────────────────────────────────────────┐
│                        DATA LAYER                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │
│  │ Repositories │  │ Data Sources │  │   Camera     │        │
│  │              │  │ (Local/Remote)│ │   Manager    │        │
│  └──────────────┘  └──────────────┘  └──────────────┘        │
└────────────────────────────────────────────────────────────────┘
                            │
┌────────────────────────────────────────────────────────────────┐
│                     FRAMEWORK LAYER                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │
│  │   OpenCV     │  │  TensorFlow  │  │    Room      │        │
│  │   Android    │  │     Lite     │  │   Database   │        │
│  └──────────────┘  └──────────────┘  └──────────────┘        │
└────────────────────────────────────────────────────────────────┘
```

### 3.2 Design Principles

#### 3.2.1 SOLID Principles
- **Single Responsibility**: Each class has one reason to change
- **Open/Closed**: Open for extension, closed for modification
- **Liskov Substitution**: Subtypes must be substitutable for base types
- **Interface Segregation**: Many specific interfaces over one general
- **Dependency Inversion**: Depend on abstractions, not concretions

#### 3.2.2 Clean Architecture Principles
- **Independence of Frameworks**: Business logic independent of libraries
- **Testability**: Business logic testable without UI, database, or external services
- **Independence of UI**: UI can change without changing business logic
- **Independence of Database**: Swap data sources without affecting business logic
- **Independence of External Agencies**: Business logic isolated from external services

---

## 4. Core Architecture Decisions

### 4.1 ADR-001: Clean Architecture Pattern

**Status**: Accepted
**Date**: 2025-11-11

#### Context
Need to choose an architectural pattern that supports:
- Long-term maintainability
- Testability at all layers
- Separation of concerns
- Framework independence

#### Decision
Implement Clean Architecture with distinct layers:
- **Presentation Layer**: UI components (Jetpack Compose) and ViewModels
- **Domain Layer**: Business logic, use cases, domain models
- **Data Layer**: Repositories, data sources, mappers
- **Framework Layer**: Android SDK, third-party libraries

#### Consequences
**Positive:**
- Clear separation of concerns
- Highly testable code
- Easy to replace frameworks/libraries
- Reduced coupling between layers

**Negative:**
- More boilerplate code
- Steeper learning curve for new developers
- Initial development may be slower

**Mitigation:**
- Provide code templates and generators
- Comprehensive onboarding documentation
- Establish clear package structure conventions

---

### 4.2 ADR-002: MVVM for Presentation Layer

**Status**: Accepted
**Date**: 2025-11-11

#### Context
Need to choose a presentation pattern that works well with Jetpack Compose and supports reactive UI updates.

#### Decision
Use Model-View-ViewModel (MVVM) pattern with:
- **View**: Jetpack Compose UI components
- **ViewModel**: Android Architecture Components ViewModel
- **Model**: Domain models and UI state classes

#### Consequences
**Positive:**
- Native Android support with Jetpack libraries
- Lifecycle-aware components
- Built-in configuration change handling
- Excellent tooling support
- Natural fit with Compose's declarative UI

**Negative:**
- ViewModels can become bloated if not carefully managed
- Potential for tight coupling between ViewModel and View

**Mitigation:**
- Limit ViewModels to presentation logic only
- Use Use Cases for complex business logic
- Implement UI State pattern to encapsulate view state

---

### 4.3 ADR-003: Kotlin as Primary Language

**Status**: Accepted
**Date**: 2025-11-11

#### Context
Need to select the primary programming language for the Android application.

#### Decision
Use Kotlin as the primary language with:
- Kotlin Coroutines for asynchronous operations
- Kotlin Flow for reactive streams
- Kotlin DSL for configuration

#### Consequences
**Positive:**
- Null safety reduces runtime crashes
- Concise syntax improves readability
- Coroutines simplify async programming
- Excellent IDE support (Android Studio)
- Official Google recommendation

**Negative:**
- Team may need training if unfamiliar with Kotlin
- Some legacy libraries may be Java-only

**Mitigation:**
- Provide Kotlin training resources
- Establish Kotlin coding standards
- Use Kotlin-friendly libraries where possible

---

### 4.4 ADR-004: Jetpack Compose for UI

**Status**: Accepted
**Date**: 2025-11-11

#### Context
Need to choose UI framework for Android application.

#### Decision
Use Jetpack Compose for all UI development with:
- Material Design 3 components
- Compose Navigation
- Compose animations

#### Consequences
**Positive:**
- Declarative UI reduces boilerplate
- Better performance than XML layouts
- Easier to create custom components
- Built-in accessibility support
- Future-proof (Google's strategic direction)

**Negative:**
- Relatively newer technology
- Smaller community compared to XML views
- Some third-party libraries may lack Compose support

**Mitigation:**
- Target Android 8.0+ to ensure Compose compatibility
- Use accompanist libraries for missing features
- Create custom composables where needed

---

### 4.5 ADR-005: Hilt for Dependency Injection

**Status**: Accepted
**Date**: 2025-11-11

#### Context
Need dependency injection framework for managing object creation and lifecycle.

#### Decision
Use Hilt (built on Dagger) for dependency injection.

#### Consequences
**Positive:**
- Official Google recommendation
- Compile-time validation
- Android-specific components (@ViewModelScope, etc.)
- Good documentation and tooling

**Negative:**
- Annotation processing increases build time
- Steep learning curve for complex scenarios

**Mitigation:**
- Use standard Hilt patterns and conventions
- Provide team training on DI best practices
- Create module templates for common scenarios

---

### 4.6 ADR-006: OpenCV Android SDK for Image Processing

**Status**: Accepted
**Date**: 2025-11-11

#### Context
Need to port MATLAB image processing algorithms to Android.

#### Decision
Use OpenCV Android SDK version 4.8+ for:
- Frame extraction and processing
- Motion detection and tracking
- Circle crop operations
- Feature detection

#### Consequences
**Positive:**
- Direct port path from existing MATLAB code
- Highly optimized for mobile devices
- Extensive documentation
- Active community support
- Native Android integration

**Negative:**
- Large library size (~25MB)
- Learning curve for team
- May require native code (C++) for optimization

**Mitigation:**
- Use ProGuard to reduce library size
- Implement lazy loading of OpenCV
- Create abstraction layer over OpenCV APIs

---

### 4.7 ADR-007: TensorFlow Lite for ML Enhancement

**Status**: Accepted
**Date**: 2025-11-11

#### Context
Need to potentially enhance particle detection and motion analysis with machine learning.

#### Decision
Use TensorFlow Lite for optional ML-based enhancements:
- Particle detection optimization
- Motion pattern recognition
- Result validation

#### Consequences
**Positive:**
- On-device inference (privacy)
- Optimized for mobile
- Can improve accuracy over time
- Google support and tooling

**Negative:**
- Adds complexity
- Requires model training infrastructure
- Initial development overhead

**Mitigation:**
- Make ML features optional/progressive
- Start with traditional CV, add ML incrementally
- Use pre-trained models where applicable

---

### 4.8 ADR-008: Room Database for Local Storage

**Status**: Accepted
**Date**: 2025-11-11

#### Context
Need local database for storing measurements, patient data, and settings.

#### Decision
Use Room Persistence Library with:
- SQLite backend
- Type converters for complex objects
- LiveData/Flow for reactive queries
- Migration support

#### Consequences
**Positive:**
- Compile-time SQL validation
- LiveData/Flow integration
- Migration support
- Official Android recommendation

**Negative:**
- Requires schema planning upfront
- Can be verbose for simple operations

**Mitigation:**
- Design normalized database schema
- Use database versioning from start
- Create repository abstraction over Room

---

### 4.9 ADR-009: Camera2 API for Video Capture

**Status**: Accepted
**Date**: 2025-11-11

#### Context
Need to capture high-quality video for PT/INR analysis.

#### Decision
Use Camera2 API (not deprecated Camera API) with:
- CameraX wrapper for simplicity
- ImageAnalysis use case for frame processing
- VideoCapture for recording (if needed)

#### Consequences
**Positive:**
- Modern API with advanced features
- CameraX simplifies implementation
- Better performance and quality control
- Support for multiple cameras

**Negative:**
- More complex than deprecated Camera API
- Device fragmentation issues

**Mitigation:**
- Use CameraX to abstract device differences
- Implement fallback for unsupported features
- Test on wide range of devices

---

### 4.10 ADR-010: Kotlin Coroutines & Flow for Async Operations

**Status**: Accepted
**Date**: 2025-11-11

#### Context
Need to handle async operations: image processing, database, network.

#### Decision
Use Kotlin Coroutines and Flow for all async operations:
- Coroutines for one-shot operations
- Flow for streams of data
- StateFlow/SharedFlow for state management

#### Consequences
**Positive:**
- Structured concurrency prevents leaks
- Easy to read and maintain
- Built-in cancellation support
- Excellent testing support

**Negative:**
- Team needs to understand coroutines concepts
- Can be challenging to debug

**Mitigation:**
- Establish coroutine usage patterns
- Use proper exception handling
- Implement comprehensive logging

---

## 5. Technology Stack

### 5.1 Core Technologies

| Category | Technology | Version | Rationale |
|----------|-----------|---------|-----------|
| Language | Kotlin | 1.9+ | Official Android language, null safety |
| UI Framework | Jetpack Compose | 1.5+ | Modern declarative UI |
| DI Framework | Hilt | 2.48+ | Standard Android DI solution |
| Async | Coroutines + Flow | 1.7+ | Structured concurrency |
| Database | Room | 2.6+ | Type-safe SQLite wrapper |
| Image Processing | OpenCV Android | 4.8+ | Computer vision algorithms |
| ML (Optional) | TensorFlow Lite | 2.13+ | On-device ML inference |
| Camera | CameraX | 1.3+ | Simplified camera operations |

### 5.2 Jetpack Libraries

```kotlin
dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Compose
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")

    // CameraX
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // OpenCV
    implementation("org.opencv:opencv-android:4.8.0")

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.13.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.13.0")
}
```

### 5.3 Additional Libraries

| Library | Purpose | Version |
|---------|---------|---------|
| Timber | Logging | 5.0+ |
| Retrofit | Network (future) | 2.9+ |
| OkHttp | HTTP client | 4.12+ |
| Gson | JSON parsing | 2.10+ |
| Coil | Image loading | 2.5+ |
| MPAndroidChart | Data visualization | 3.1+ |
| iText | PDF generation | 7.2+ |
| Accompanist | Compose utilities | 0.32+ |

---

## 6. Module Architecture

### 6.1 Multi-Module Structure

```
blood-coagulation-inr/
├── app/                          # Application module
├── core/
│   ├── common/                   # Common utilities
│   ├── design-system/            # UI components, theme
│   ├── data/                     # Data layer interfaces
│   ├── domain/                   # Domain models, use cases
│   └── testing/                  # Test utilities
├── feature/
│   ├── measurement/              # PT/INR measurement feature
│   ├── history/                  # Measurement history
│   ├── patient/                  # Patient management
│   ├── settings/                 # App settings
│   └── export/                   # Data export
├── processing/
│   ├── opencv/                   # OpenCV processing
│   ├── ml/                       # ML models (optional)
│   └── camera/                   # Camera management
└── data/
    ├── database/                 # Room database
    ├── repository/               # Repository implementations
    └── storage/                  # File storage
```

### 6.2 Module Dependencies

```
┌─────────────┐
│     app     │
└──────┬──────┘
       │
       ├──────────────────────┬──────────────────────┐
       │                      │                      │
┌──────▼──────┐      ┌────────▼────────┐   ┌────────▼────────┐
│   feature   │      │   processing    │   │      data       │
│  (modules)  │      │    (modules)    │   │   (modules)     │
└──────┬──────┘      └────────┬────────┘   └────────┬────────┘
       │                      │                      │
       └──────────────────────┼──────────────────────┘
                              │
                     ┌────────▼────────┐
                     │      core       │
                     │    (modules)    │
                     └─────────────────┘
```

### 6.3 Layer Definitions

#### 6.3.1 Presentation Layer (feature/*)
```kotlin
// Package structure
feature/measurement/
├── ui/
│   ├── MeasurementScreen.kt
│   ├── components/
│   │   ├── CameraPreview.kt
│   │   ├── ProgressIndicator.kt
│   │   └── ResultDisplay.kt
│   └── theme/
├── viewmodel/
│   ├── MeasurementViewModel.kt
│   └── MeasurementState.kt
├── navigation/
│   └── MeasurementNavigation.kt
└── di/
    └── MeasurementModule.kt
```

#### 6.3.2 Domain Layer (core/domain/)
```kotlin
// Package structure
core/domain/
├── model/
│   ├── Measurement.kt
│   ├── Patient.kt
│   ├── PTResult.kt
│   └── INRResult.kt
├── usecase/
│   ├── PerformMeasurementUseCase.kt
│   ├── CalculatePTUseCase.kt
│   ├── CalculateINRUseCase.kt
│   └── SaveMeasurementUseCase.kt
└── repository/
    ├── MeasurementRepository.kt
    ├── PatientRepository.kt
    └── ProcessingRepository.kt
```

#### 6.3.3 Data Layer (data/*)
```kotlin
// Package structure
data/repository/
├── MeasurementRepositoryImpl.kt
├── PatientRepositoryImpl.kt
└── mapper/
    ├── MeasurementMapper.kt
    └── PatientMapper.kt

data/database/
├── AppDatabase.kt
├── dao/
│   ├── MeasurementDao.kt
│   └── PatientDao.kt
└── entity/
    ├── MeasurementEntity.kt
    └── PatientEntity.kt
```

---

## 7. Data Architecture

### 7.1 Database Schema

```sql
-- Patients Table
CREATE TABLE patients (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    patient_id TEXT NOT NULL UNIQUE,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    date_of_birth INTEGER NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);

-- Measurements Table
CREATE TABLE measurements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    measurement_id TEXT NOT NULL UNIQUE,
    patient_id INTEGER NOT NULL,
    pt_value REAL NOT NULL,
    inr_value REAL NOT NULL,
    video_path TEXT,
    start_time_ms INTEGER NOT NULL,
    stop_time_ms INTEGER NOT NULL,
    processing_time_ms INTEGER NOT NULL,
    confidence_score REAL,
    status TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(id)
);

-- Motion Data Table (for analysis)
CREATE TABLE motion_data (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    measurement_id INTEGER NOT NULL,
    frame_number INTEGER NOT NULL,
    pipette_x REAL,
    pipette_y REAL,
    particle_x REAL,
    particle_y REAL,
    timestamp_ms INTEGER NOT NULL,
    FOREIGN KEY (measurement_id) REFERENCES measurements(id)
);

-- Audit Log Table (for compliance)
CREATE TABLE audit_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT,
    action TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id TEXT,
    timestamp INTEGER NOT NULL,
    details TEXT
);

-- Indexes
CREATE INDEX idx_measurements_patient_id ON measurements(patient_id);
CREATE INDEX idx_measurements_created_at ON measurements(created_at);
CREATE INDEX idx_motion_data_measurement_id ON motion_data(measurement_id);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);
```

### 7.2 Data Flow

```
┌──────────────────────────────────────────────────────────┐
│                    Camera (CameraX)                      │
└───────────────────────┬──────────────────────────────────┘
                        │ Image frames
┌───────────────────────▼──────────────────────────────────┐
│              Frame Processing (OpenCV)                   │
│  • Circle crop                                           │
│  • Motion detection                                      │
│  • Feature tracking                                      │
└───────────────────────┬──────────────────────────────────┘
                        │ Motion data
┌───────────────────────▼──────────────────────────────────┐
│          PT/INR Calculation (Use Case)                   │
│  • Knee point detection                                  │
│  • PT computation                                        │
│  • INR computation                                       │
└───────────────────────┬──────────────────────────────────┘
                        │ Results
┌───────────────────────▼──────────────────────────────────┐
│              Repository Layer                            │
│  • Data validation                                       │
│  • Entity mapping                                        │
└───────────────────────┬──────────────────────────────────┘
                        │
        ┌───────────────┴───────────────┐
        │                               │
┌───────▼────────┐           ┌──────────▼────────┐
│ Room Database  │           │   File Storage    │
│  • Measurements│           │   • Videos        │
│  • Patients    │           │   • Frames        │
│  • Audit logs  │           │   • Exports       │
└────────────────┘           └───────────────────┘
```

### 7.3 Data Models

#### 7.3.1 Domain Models

```kotlin
// core/domain/model/Measurement.kt
data class Measurement(
    val id: String,
    val patientId: String,
    val ptValue: Double,
    val inrValue: Double,
    val startTimeMs: Long,
    val stopTimeMs: Long,
    val processingTimeMs: Long,
    val confidenceScore: Double,
    val status: MeasurementStatus,
    val createdAt: Instant,
    val videoPath: String? = null
)

enum class MeasurementStatus {
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    INVALIDATED
}

// core/domain/model/Patient.kt
data class Patient(
    val id: String,
    val patientId: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val createdAt: Instant,
    val updatedAt: Instant
)

// core/domain/model/MotionCurve.kt
data class MotionCurve(
    val points: List<MotionPoint>,
    val kneePoint: MotionPoint,
    val startFrame: Int,
    val endFrame: Int
)

data class MotionPoint(
    val frameNumber: Int,
    val x: Double,
    val y: Double,
    val timestamp: Long
)
```

#### 7.3.2 Entity Models

```kotlin
// data/database/entity/MeasurementEntity.kt
@Entity(
    tableName = "measurements",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patient_id"), Index("created_at")]
)
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "measurement_id")
    val measurementId: String,

    @ColumnInfo(name = "patient_id")
    val patientId: Long,

    @ColumnInfo(name = "pt_value")
    val ptValue: Double,

    @ColumnInfo(name = "inr_value")
    val inrValue: Double,

    @ColumnInfo(name = "video_path")
    val videoPath: String?,

    @ColumnInfo(name = "start_time_ms")
    val startTimeMs: Long,

    @ColumnInfo(name = "stop_time_ms")
    val stopTimeMs: Long,

    @ColumnInfo(name = "processing_time_ms")
    val processingTimeMs: Long,

    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Double?,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
```

### 7.4 Repository Pattern

```kotlin
// core/domain/repository/MeasurementRepository.kt
interface MeasurementRepository {
    suspend fun saveMeasurement(measurement: Measurement): Result<String>
    suspend fun getMeasurement(id: String): Result<Measurement>
    suspend fun getMeasurementsByPatient(patientId: String): Flow<List<Measurement>>
    suspend fun deleteMeasurement(id: String): Result<Unit>
    suspend fun getAllMeasurements(): Flow<List<Measurement>>
}

// data/repository/MeasurementRepositoryImpl.kt
class MeasurementRepositoryImpl @Inject constructor(
    private val measurementDao: MeasurementDao,
    private val mapper: MeasurementMapper,
    private val ioDispatcher: CoroutineDispatcher
) : MeasurementRepository {

    override suspend fun saveMeasurement(
        measurement: Measurement
    ): Result<String> = withContext(ioDispatcher) {
        try {
            val entity = mapper.toEntity(measurement)
            val id = measurementDao.insert(entity)
            Result.success(id.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMeasurement(
        id: String
    ): Result<Measurement> = withContext(ioDispatcher) {
        try {
            val entity = measurementDao.getById(id.toLong())
            entity?.let {
                Result.success(mapper.toDomain(it))
            } ?: Result.failure(NoSuchElementException("Measurement not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMeasurementsByPatient(
        patientId: String
    ): Flow<List<Measurement>> {
        return measurementDao.getByPatientId(patientId.toLong())
            .map { entities ->
                entities.map { mapper.toDomain(it) }
            }
            .flowOn(ioDispatcher)
    }
}
```

---

## 8. Security Architecture

### 8.1 Data Security

#### 8.1.1 Encryption at Rest

```kotlin
// Use SQLCipher for database encryption
dependencies {
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
}

// Encrypted SharedPreferences for sensitive data
class SecurePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
```

#### 8.1.2 Data Sanitization

```kotlin
// Data sanitization for PHI
class DataSanitizer {
    fun sanitizePatientName(name: String): String {
        return name.trim()
            .replace(Regex("[^a-zA-Z\\s-]"), "")
            .take(MAX_NAME_LENGTH)
    }

    fun validatePatientId(id: String): Boolean {
        return id.matches(Regex("^[A-Z0-9]{6,20}$"))
    }
}
```

### 8.2 HIPAA Compliance

#### 8.2.1 Audit Logging

```kotlin
// core/domain/usecase/AuditLogUseCase.kt
class LogAuditEventUseCase @Inject constructor(
    private val auditRepository: AuditRepository,
    private val timeProvider: TimeProvider
) {
    suspend operator fun invoke(
        action: AuditAction,
        entityType: String,
        entityId: String?,
        details: Map<String, String>? = null
    ) {
        val event = AuditEvent(
            userId = getCurrentUserId(),
            action = action.name,
            entityType = entityType,
            entityId = entityId,
            timestamp = timeProvider.now(),
            details = details?.let { Json.encodeToString(it) }
        )
        auditRepository.logEvent(event)
    }
}

enum class AuditAction {
    CREATE,
    READ,
    UPDATE,
    DELETE,
    EXPORT,
    SHARE
}
```

#### 8.2.2 Access Control

```kotlin
// Simple role-based access control
enum class UserRole {
    CLINICIAN,
    LAB_TECHNICIAN,
    ADMINISTRATOR,
    READ_ONLY
}

class PermissionManager @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun hasPermission(
        permission: Permission
    ): Boolean {
        val user = userRepository.getCurrentUser()
        return user.role.permissions.contains(permission)
    }
}

enum class Permission {
    PERFORM_MEASUREMENT,
    VIEW_MEASUREMENTS,
    DELETE_MEASUREMENTS,
    MANAGE_PATIENTS,
    EXPORT_DATA,
    MANAGE_SETTINGS
}
```

### 8.3 Network Security (Future)

```kotlin
// Certificate pinning for API communication
class NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val certificatePinner = CertificatePinner.Builder()
            .add("api.example.com", "sha256/AAAAAAAAAAAAAAAA...")
            .build()

        return OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .addInterceptor(AuthInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
```

### 8.4 Secure Video Storage

```kotlin
class SecureVideoStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptionManager: EncryptionManager
) {
    private val videoDir = File(context.filesDir, "videos")

    suspend fun saveVideo(
        measurementId: String,
        videoData: ByteArray
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            videoDir.mkdirs()
            val file = File(videoDir, "$measurementId.enc")

            // Encrypt video before saving
            val encryptedData = encryptionManager.encrypt(videoData)
            file.writeBytes(encryptedData)

            // Set file permissions
            file.setReadable(false, false)
            file.setReadable(true, true)
            file.setWritable(false, false)

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteVideo(path: String): Result<Unit> {
        return try {
            val file = File(path)
            if (file.exists()) {
                // Secure deletion: overwrite before deleting
                file.writeBytes(ByteArray(file.length().toInt()))
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## 9. Performance & Optimization

### 9.1 Image Processing Optimization

#### 9.1.1 Frame Processing Pipeline

```kotlin
class OptimizedFrameProcessor @Inject constructor(
    private val openCVManager: OpenCVManager
) {
    private val processingExecutor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
    )

    suspend fun processFrames(
        frames: Flow<Bitmap>
    ): Flow<ProcessedFrame> = flow {
        frames
            .buffer(capacity = FRAME_BUFFER_SIZE)
            .collect { frame ->
                val processed = processFrameAsync(frame)
                emit(processed)
            }
    }.flowOn(Dispatchers.Default)

    private suspend fun processFrameAsync(
        frame: Bitmap
    ): ProcessedFrame = withContext(Dispatchers.Default) {
        // Resize frame to optimal resolution
        val resized = resizeFrame(frame, TARGET_WIDTH, TARGET_HEIGHT)

        // Convert to grayscale for faster processing
        val grayscale = openCVManager.toGrayscale(resized)

        // Apply circle crop
        val cropped = openCVManager.circleCrop(grayscale)

        ProcessedFrame(cropped, frame.width, frame.height)
    }

    companion object {
        const val FRAME_BUFFER_SIZE = 10
        const val TARGET_WIDTH = 720
        const val TARGET_HEIGHT = 1280
    }
}
```

#### 9.1.2 Memory Management

```kotlin
class MemoryManager @Inject constructor() {
    private val framePool = object : Pools.Pool<Mat> {
        private val pool = ArrayDeque<Mat>(POOL_SIZE)

        override fun acquire(): Mat? {
            return pool.pollFirst() ?: Mat()
        }

        override fun release(instance: Mat): Boolean {
            instance.release()
            return if (pool.size < POOL_SIZE) {
                pool.add(instance)
                true
            } else {
                false
            }
        }
    }

    fun acquireFrame(): Mat = framePool.acquire() ?: Mat()

    fun releaseFrame(mat: Mat) {
        framePool.release(mat)
    }

    companion object {
        const val POOL_SIZE = 20
    }
}
```

### 9.2 Database Optimization

```kotlin
@Dao
interface MeasurementDao {
    // Use pagination for large datasets
    @Query("SELECT * FROM measurements ORDER BY created_at DESC")
    fun getAllPaged(): PagingSource<Int, MeasurementEntity>

    // Use indexes for frequent queries
    @Query("""
        SELECT * FROM measurements
        WHERE patient_id = :patientId
        ORDER BY created_at DESC
    """)
    fun getByPatientId(patientId: Long): Flow<List<MeasurementEntity>>

    // Batch operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(measurements: List<MeasurementEntity>)

    // Use transactions for complex operations
    @Transaction
    suspend fun saveMeasurementWithMotionData(
        measurement: MeasurementEntity,
        motionData: List<MotionDataEntity>
    ) {
        val measurementId = insert(measurement)
        motionDataDao.insertAll(
            motionData.map { it.copy(measurementId = measurementId) }
        )
    }
}
```

### 9.3 Lazy Loading & Initialization

```kotlin
// Lazy initialization of OpenCV
class OpenCVManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (!_isInitialized.value) {
            OpenCVLoader.initDebug()
            _isInitialized.value = true
        }
    }
}

// Application class
@HiltAndroidApp
class BloodCoagulationApp : Application() {
    @Inject
    lateinit var openCVManager: OpenCVManager

    override fun onCreate() {
        super.onCreate()

        // Initialize heavy libraries in background
        lifecycleScope.launch {
            openCVManager.initialize()
        }
    }
}
```

### 9.4 Caching Strategy

```kotlin
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val diskCache = DiskLruCache.open(
        File(context.cacheDir, "app_cache"),
        APP_VERSION,
        VALUE_COUNT,
        MAX_CACHE_SIZE
    )

    private val memoryCache = LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()
    )

    fun getBitmap(key: String): Bitmap? {
        // Try memory cache first
        return memoryCache.get(key) ?: run {
            // Try disk cache
            diskCache.get(key)?.let { snapshot ->
                val bitmap = BitmapFactory.decodeStream(
                    snapshot.getInputStream(0)
                )
                // Add to memory cache
                memoryCache.put(key, bitmap)
                bitmap
            }
        }
    }

    companion object {
        const val APP_VERSION = 1
        const val VALUE_COUNT = 1
        const val MAX_CACHE_SIZE = 10 * 1024 * 1024L // 10 MB
    }
}
```

### 9.5 Performance Monitoring

```kotlin
class PerformanceMonitor @Inject constructor() {
    fun measureExecutionTime(
        tag: String,
        block: () -> Unit
    ) {
        val startTime = System.nanoTime()
        block()
        val endTime = System.nanoTime()
        val duration = (endTime - startTime) / 1_000_000 // Convert to ms

        Timber.d("$tag execution time: ${duration}ms")

        // Log warning if exceeds threshold
        if (duration > SLOW_OPERATION_THRESHOLD) {
            Timber.w("$tag is slow: ${duration}ms")
        }
    }

    suspend fun <T> measureSuspendExecutionTime(
        tag: String,
        block: suspend () -> T
    ): T {
        val startTime = System.nanoTime()
        val result = block()
        val endTime = System.nanoTime()
        val duration = (endTime - startTime) / 1_000_000

        Timber.d("$tag execution time: ${duration}ms")
        return result
    }

    companion object {
        const val SLOW_OPERATION_THRESHOLD = 100 // ms
    }
}
```

---

## 10. Testing Strategy

### 10.1 Testing Pyramid

```
        ┌─────────────┐
        │  UI Tests   │ 10%
        │  (E2E)      │
        └─────────────┘
       ┌───────────────┐
       │ Integration   │ 30%
       │    Tests      │
       └───────────────┘
      ┌─────────────────┐
      │   Unit Tests    │ 60%
      │                 │
      └─────────────────┘
```

### 10.2 Unit Testing

#### 10.2.1 ViewModel Testing

```kotlin
@ExperimentalCoroutinesTest
class MeasurementViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: MeasurementViewModel
    private lateinit var performMeasurementUseCase: PerformMeasurementUseCase
    private lateinit var saveMeasurementUseCase: SaveMeasurementUseCase

    @Before
    fun setup() {
        performMeasurementUseCase = mockk()
        saveMeasurementUseCase = mockk()

        viewModel = MeasurementViewModel(
            performMeasurementUseCase = performMeasurementUseCase,
            saveMeasurementUseCase = saveMeasurementUseCase
        )
    }

    @Test
    fun `when measurement starts, state should be InProgress`() = runTest {
        // Given
        val frames = flowOf<Bitmap>()

        // When
        viewModel.startMeasurement(frames)

        // Then
        val state = viewModel.state.value
        assertThat(state).isInstanceOf(MeasurementState.InProgress::class.java)
    }

    @Test
    fun `when measurement completes, should save result`() = runTest {
        // Given
        val result = MeasurementResult(ptValue = 12.1, inrValue = 1.0)
        coEvery {
            performMeasurementUseCase(any())
        } returns flowOf(Result.success(result))
        coEvery {
            saveMeasurementUseCase(any())
        } returns Result.success("123")

        // When
        viewModel.startMeasurement(flowOf())
        advanceUntilIdle()

        // Then
        coVerify { saveMeasurementUseCase(any()) }
        val state = viewModel.state.value
        assertThat(state).isInstanceOf(MeasurementState.Success::class.java)
    }
}
```

#### 10.2.2 Use Case Testing

```kotlin
class CalculatePTUseCaseTest {

    private lateinit var useCase: CalculatePTUseCase
    private lateinit var motionAnalyzer: MotionAnalyzer

    @Before
    fun setup() {
        motionAnalyzer = mockk()
        useCase = CalculatePTUseCase(motionAnalyzer)
    }

    @Test
    fun `should calculate PT correctly for normal values`() = runTest {
        // Given
        val pipetteCurve = createMockMotionCurve(startMs = 0, endMs = 5000)
        val particleCurve = createMockMotionCurve(startMs = 5000, endMs = 17100)

        every {
            motionAnalyzer.findKneePoint(any())
        } returns MotionPoint(100, 0.0, 0.0, 5000)

        // When
        val result = useCase(pipetteCurve, particleCurve)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.value).isEqualTo(12.1, within(0.1))
    }

    @Test
    fun `should return failure when knee point not found`() = runTest {
        // Given
        val curve = createMockMotionCurve(startMs = 0, endMs = 1000)
        every { motionAnalyzer.findKneePoint(any()) } returns null

        // When
        val result = useCase(curve, curve)

        // Then
        assertThat(result.isFailure).isTrue()
    }
}
```

#### 10.2.3 Repository Testing

```kotlin
@ExperimentalCoroutinesTest
class MeasurementRepositoryTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: MeasurementRepositoryImpl
    private lateinit var measurementDao: MeasurementDao
    private lateinit var mapper: MeasurementMapper

    @Before
    fun setup() {
        measurementDao = mockk()
        mapper = MeasurementMapper()
        repository = MeasurementRepositoryImpl(
            measurementDao = measurementDao,
            mapper = mapper,
            ioDispatcher = StandardTestDispatcher()
        )
    }

    @Test
    fun `saveMeasurement should insert entity and return id`() = runTest {
        // Given
        val measurement = createTestMeasurement()
        coEvery { measurementDao.insert(any()) } returns 1L

        // When
        val result = repository.saveMeasurement(measurement)

        // Then
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo("1")
        coVerify { measurementDao.insert(any()) }
    }
}
```

### 10.3 Integration Testing

```kotlin
@HiltAndroidTest
class MeasurementFlowIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var database: AppDatabase

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun complete_measurement_flow() {
        // Navigate to measurement screen
        composeTestRule.onNodeWithText("New Measurement")
            .performClick()

        // Select patient
        composeTestRule.onNodeWithText("Select Patient")
            .performClick()
        composeTestRule.onNodeWithText("John Doe")
            .performClick()

        // Start measurement
        composeTestRule.onNodeWithText("Start")
            .performClick()

        // Wait for processing
        composeTestRule.waitUntil(timeoutMillis = 30000) {
            composeTestRule.onAllNodesWithText("Complete")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify result displayed
        composeTestRule.onNodeWithText("PT: 12.1")
            .assertExists()
        composeTestRule.onNodeWithText("INR: 1.0")
            .assertExists()
    }
}
```

### 10.4 UI Testing

```kotlin
class MeasurementScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun initial_state_shows_start_button() {
        composeTestRule.setContent {
            MeasurementScreen(
                state = MeasurementState.Idle,
                onStartClick = {},
                onCancelClick = {}
            )
        }

        composeTestRule.onNodeWithText("Start Measurement")
            .assertExists()
            .assertIsEnabled()
    }

    @Test
    fun processing_state_shows_progress() {
        composeTestRule.setContent {
            MeasurementScreen(
                state = MeasurementState.InProgress(progress = 0.5f),
                onStartClick = {},
                onCancelClick = {}
            )
        }

        composeTestRule.onNode(hasProgressBarRangeInfo(
            ProgressBarRangeInfo(0.5f, 0f..1f)
        )).assertExists()

        composeTestRule.onNodeWithText("Cancel")
            .assertExists()
            .assertIsEnabled()
    }
}
```

### 10.5 Performance Testing

```kotlin
@RunWith(AndroidJUnit4::class)
class FrameProcessingBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun benchmark_frame_processing() {
        val frameProcessor = FrameProcessor()
        val testFrame = createTestBitmap(720, 1280)

        benchmarkRule.measureRepeated {
            frameProcessor.process(testFrame)
        }
    }

    @Test
    fun benchmark_pt_calculation() {
        val calculator = PTCalculator()
        val motionCurve = createTestMotionCurve(1000)

        benchmarkRule.measureRepeated {
            calculator.calculate(motionCurve)
        }
    }
}
```

### 10.6 Test Coverage Goals

| Layer | Target Coverage | Critical Paths |
|-------|----------------|----------------|
| Domain (Use Cases) | 90%+ | All business logic |
| ViewModels | 85%+ | State transitions |
| Repositories | 80%+ | Data operations |
| UI Components | 70%+ | User interactions |
| Utilities | 85%+ | Core functions |

---

## 11. Development Guidelines

### 11.1 Code Style & Conventions

#### 11.1.1 Kotlin Style Guide

Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) with these additions:

```kotlin
// ✅ Good: Clear naming, single responsibility
class MeasurementViewModel @Inject constructor(
    private val performMeasurementUseCase: PerformMeasurementUseCase,
    private val saveMeasurementUseCase: SaveMeasurementUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<MeasurementState>(MeasurementState.Idle)
    val state: StateFlow<MeasurementState> = _state.asStateFlow()

    fun startMeasurement(frames: Flow<Bitmap>) {
        viewModelScope.launch {
            _state.value = MeasurementState.InProgress(0f)
            // ...
        }
    }
}

// ❌ Bad: Multiple responsibilities, unclear naming
class Manager @Inject constructor(
    private val u1: UseCase1,
    private val u2: UseCase2,
    private val u3: UseCase3
) : ViewModel() {
    var s: Any? = null
    fun doStuff(x: Any) { /* ... */ }
}
```

#### 11.1.2 Package Structure

```
com.example.bloodcoagulation/
├── app/                          # Application class, DI setup
├── feature/
│   └── measurement/
│       ├── ui/                   # Composables
│       ├── viewmodel/            # ViewModels
│       └── navigation/           # Navigation graphs
├── core/
│   ├── domain/
│   │   ├── model/                # Domain models
│   │   ├── usecase/              # Use cases
│   │   └── repository/           # Repository interfaces
│   ├── data/
│   │   ├── repository/           # Repository implementations
│   │   ├── database/             # Room database
│   │   └── mapper/               # Entity <-> Domain mappers
│   └── common/
│       ├── util/                 # Utility functions
│       └── ext/                  # Extension functions
└── processing/
    ├── opencv/                   # OpenCV processing
    └── camera/                   # Camera management
```

#### 11.1.3 Naming Conventions

| Type | Convention | Example |
|------|-----------|---------|
| Classes | PascalCase | `MeasurementViewModel` |
| Functions | camelCase | `startMeasurement()` |
| Constants | SCREAMING_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Private vars | _camelCase (for StateFlow) | `_state` |
| Resources | snake_case | `btn_start_measurement` |
| Composables | PascalCase | `MeasurementScreen()` |

### 11.2 Composable Guidelines

```kotlin
// ✅ Good: Stateless composable with clear parameters
@Composable
fun MeasurementResultCard(
    ptValue: Double,
    inrValue: Double,
    timestamp: Instant,
    modifier: Modifier = Modifier,
    onExportClick: () -> Unit = {}
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PT: ${ptValue.format(1)}",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "INR: ${inrValue.format(1)}",
                style = MaterialTheme.typography.headlineMedium
            )
            // ...
        }
    }
}

// ❌ Bad: Stateful composable with side effects
@Composable
fun BadMeasurementCard() {
    val context = LocalContext.current
    val data = remember { mutableStateOf<Any?>(null) }

    LaunchedEffect(Unit) {
        // Don't fetch data in composable
        data.value = fetchDataFromDatabase()
    }

    // ...
}
```

### 11.3 Error Handling

```kotlin
// Use sealed class for results
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

// Domain layer exceptions
sealed class DomainException(message: String) : Exception(message) {
    class InvalidMeasurementException(message: String) : DomainException(message)
    class ProcessingException(message: String) : DomainException(message)
    class StorageException(message: String) : DomainException(message)
}

// Error handling in ViewModel
class MeasurementViewModel @Inject constructor(
    private val performMeasurementUseCase: PerformMeasurementUseCase
) : ViewModel() {

    fun startMeasurement(frames: Flow<Bitmap>) {
        viewModelScope.launch {
            _state.value = MeasurementState.InProgress(0f)

            performMeasurementUseCase(frames)
                .catch { exception ->
                    _state.value = MeasurementState.Error(
                        message = exception.toUserFriendlyMessage()
                    )
                    Timber.e(exception, "Measurement failed")
                }
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _state.value = MeasurementState.Success(result.data)
                        }
                        is Result.Error -> {
                            _state.value = MeasurementState.Error(
                                message = result.exception.toUserFriendlyMessage()
                            )
                        }
                        is Result.Loading -> {
                            // Update progress
                        }
                    }
                }
        }
    }
}
```

### 11.4 Logging Strategy

```kotlin
// Initialize Timber in Application class
class BloodCoagulationApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }
}

// Usage in code
class MeasurementProcessor {
    fun processFrame(frame: Bitmap) {
        Timber.v("Processing frame: ${frame.width}x${frame.height}")

        try {
            // Process frame
            Timber.d("Frame processed successfully")
        } catch (e: Exception) {
            Timber.e(e, "Frame processing failed")
        }
    }
}

// Custom crash reporting tree
class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority >= Log.WARN) {
            // Send to crash reporting service
            // Firebase Crashlytics, Sentry, etc.
        }
    }
}
```

### 11.5 Documentation Requirements

```kotlin
/**
 * Calculates Prothrombin Time (PT) from motion curve data.
 *
 * The PT is calculated by finding the knee point in the particle motion curve
 * and computing the time difference from the pipette motion start.
 *
 * @param pipetteCurve The motion curve data for the pipette movement
 * @param particleCurve The motion curve data for particle movement
 * @return Result containing the PT value in seconds, or an error
 *
 * @throws InvalidMeasurementException if the curves are invalid or incomplete
 *
 * @see CalculateINRUseCase
 */
class CalculatePTUseCase @Inject constructor(
    private val motionAnalyzer: MotionAnalyzer,
    private val kneeDetector: KneePointDetector
) {
    suspend operator fun invoke(
        pipetteCurve: MotionCurve,
        particleCurve: MotionCurve
    ): Result<PTResult> {
        // Implementation
    }
}
```

---

## 12. Migration Path

### 12.1 Phase 2A Deliverables

#### 12.1.1 Milestone 1: Foundation (Weeks 1-2)
- [ ] Project setup with multi-module architecture
- [ ] DI framework configuration (Hilt)
- [ ] Database schema and Room setup
- [ ] Base UI with Jetpack Compose
- [ ] Navigation structure
- [ ] Logging and error handling framework

#### 12.1.2 Milestone 2: Camera Integration (Weeks 3-4)
- [ ] CameraX integration
- [ ] Real-time preview implementation
- [ ] Frame capture pipeline
- [ ] Video recording capability
- [ ] Camera permission handling

#### 12.1.3 Milestone 3: Image Processing (Weeks 5-7)
- [ ] OpenCV integration
- [ ] Port circle crop algorithm from MATLAB
- [ ] Port motion detection from MATLAB
- [ ] Frame processing pipeline
- [ ] Performance optimization

#### 12.1.4 Milestone 4: PT/INR Calculation (Weeks 8-9)
- [ ] Port knee point detection from MATLAB
- [ ] Implement PT calculation algorithm
- [ ] Implement INR calculation algorithm
- [ ] Algorithm validation against MATLAB baseline
- [ ] Accuracy testing

#### 12.1.5 Milestone 5: Data Management (Weeks 10-11)
- [ ] Measurement storage
- [ ] Patient management
- [ ] Historical data viewing
- [ ] Data export functionality
- [ ] Audit logging

#### 12.1.6 Milestone 6: Testing & Refinement (Weeks 12-14)
- [ ] Unit test coverage (90%+ for domain)
- [ ] Integration testing
- [ ] UI testing
- [ ] Performance testing
- [ ] User acceptance testing
- [ ] Bug fixes and refinements

### 12.2 Algorithm Migration Strategy

#### MATLAB to Android OpenCV Mapping

| MATLAB Function | OpenCV Equivalent | Notes |
|----------------|-------------------|-------|
| `imread()` | `Imgcodecs.imread()` | Image loading |
| `rgb2gray()` | `Imgproc.cvtColor(COLOR_BGR2GRAY)` | Grayscale conversion |
| `imresize()` | `Imgproc.resize()` | Image resizing |
| `strel()` | `Imgproc.getStructuringElement()` | Morphological operations |
| `imfill()` | `Imgproc.floodFill()` | Hole filling |
| `bwlabel()` | `Imgproc.connectedComponents()` | Component labeling |
| `regionprops()` | Custom implementation | Region properties |

#### Example Migration: Circle Crop

**MATLAB (circlecrop.m):**
```matlab
function cropped = circlecrop(img)
    [rows, cols] = size(img);
    center = [rows/2, cols/2];
    radius = min(rows, cols)/2;

    [X, Y] = meshgrid(1:cols, 1:rows);
    mask = ((X - center(2)).^2 + (Y - center(1)).^2) <= radius^2;

    cropped = img;
    cropped(~mask) = 0;
end
```

**Kotlin/OpenCV:**
```kotlin
class CircleCropProcessor @Inject constructor() {
    fun cropCircle(image: Mat): Mat {
        val rows = image.rows()
        val cols = image.cols()
        val center = Point(cols / 2.0, rows / 2.0)
        val radius = min(rows, cols) / 2.0

        val mask = Mat.zeros(image.size(), CvType.CV_8UC1)
        Imgproc.circle(
            mask,
            center,
            radius.toInt(),
            Scalar(255.0),
            -1  // Filled circle
        )

        val result = Mat()
        Core.bitwise_and(image, image, result, mask)

        mask.release()
        return result
    }
}
```

### 12.3 Data Migration

#### Initial Setup (No Existing Data)
- Create fresh database on first launch
- Import sample data for testing/demo
- Set up default configuration

#### Future Migration (v1 → v2)
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new columns
        database.execSQL(
            "ALTER TABLE measurements ADD COLUMN confidence_score REAL"
        )

        // Create new tables
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS audit_log (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT,
                action TEXT NOT NULL,
                entity_type TEXT NOT NULL,
                entity_id TEXT,
                timestamp INTEGER NOT NULL,
                details TEXT
            )
        """)
    }
}
```

---

## 13. Risk Assessment

### 13.1 Technical Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **Algorithm accuracy loss in migration** | High | Medium | - Parallel testing with MATLAB<br>- Statistical validation<br>- Expert review |
| **Performance issues on low-end devices** | High | Medium | - Device profiling<br>- Optimization strategies<br>- Minimum requirements |
| **OpenCV library size** | Medium | High | - ProGuard/R8 optimization<br>- Dynamic feature delivery |
| **Camera API fragmentation** | Medium | Medium | - Use CameraX abstraction<br>- Device testing matrix |
| **Memory constraints** | High | Medium | - Object pooling<br>- Efficient bitmap handling<br>- Memory profiling |

### 13.2 Regulatory Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **HIPAA compliance gaps** | Critical | Low | - Security audit<br>- Compliance checklist<br>- Legal review |
| **FDA approval requirements** | Critical | High | - Early FDA consultation<br>- Documentation trail<br>- Quality management system |
| **Data breach** | Critical | Low | - Encryption at rest/transit<br>- Security testing<br>- Incident response plan |

### 13.3 Project Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **Timeline delays** | Medium | Medium | - Agile sprints<br>- Regular reviews<br>- Buffer time |
| **Resource availability** | High | Low | - Cross-training<br>- Documentation<br>- Knowledge sharing |
| **Scope creep** | Medium | High | - Clear requirements<br>- Change control<br>- Prioritization |

---

## 14. Appendices

### 14.1 Glossary

| Term | Definition |
|------|------------|
| **PT** | Prothrombin Time - time for blood to clot |
| **INR** | International Normalized Ratio - standardized PT measurement |
| **Knee Point** | Inflection point in motion curve indicating coagulation start |
| **Motion Curve** | Time-series data of particle/pipette movement |
| **PHI** | Protected Health Information |
| **HIPAA** | Health Insurance Portability and Accountability Act |

### 14.2 References

1. [Android Architecture Components](https://developer.android.com/topic/architecture)
2. [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
3. [OpenCV Android SDK](https://opencv.org/android/)
4. [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
5. [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
6. [HIPAA Security Rule](https://www.hhs.gov/hipaa/for-professionals/security/index.html)
7. [FDA Mobile Medical Applications](https://www.fda.gov/medical-devices/digital-health-center-excellence/mobile-medical-applications)

### 14.3 Decision Log

| ADR | Title | Status | Date |
|-----|-------|--------|------|
| ADR-001 | Clean Architecture Pattern | Accepted | 2025-11-11 |
| ADR-002 | MVVM Presentation Layer | Accepted | 2025-11-11 |
| ADR-003 | Kotlin Primary Language | Accepted | 2025-11-11 |
| ADR-004 | Jetpack Compose UI | Accepted | 2025-11-11 |
| ADR-005 | Hilt Dependency Injection | Accepted | 2025-11-11 |
| ADR-006 | OpenCV Image Processing | Accepted | 2025-11-11 |
| ADR-007 | TensorFlow Lite ML | Accepted | 2025-11-11 |
| ADR-008 | Room Database | Accepted | 2025-11-11 |
| ADR-009 | Camera2 API | Accepted | 2025-11-11 |
| ADR-010 | Kotlin Coroutines | Accepted | 2025-11-11 |

### 14.4 Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-11-11 | Development Team | Initial draft |
| 2.0 | 2025-11-11 | Development Team | Comprehensive Phase 2A version |

---

## Approval

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Technical Lead | _______________ | _______________ | _______ |
| Product Owner | _______________ | _______________ | _______ |
| QA Lead | _______________ | _______________ | _______ |
| Security Officer | _______________ | _______________ | _______ |

---

**Document Status:** PROPOSED
**Next Review Date:** 2025-11-25
**Distribution:** Development Team, Product Management, QA, Security

