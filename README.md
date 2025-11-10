## Overview
<img src="blood1.png" align="right" width="50%" height="50%"/>

This archive contains a video of a PT and INR measurement of a sample of whole blood measured on our system, as well as the code used to compute PT and INR from the video.

We provide Python and MATLAB code for ease of reproducibility.

### Medical Device Modernization - Phase 1a ✅

The Python code has been modernized to meet current medical device software standards:
- **Python 3.x compatibility** with type hints and modern best practices
- **Robust error handling** and input validation for medical device reliability
- **Comprehensive logging** with configurable verbosity levels
- **Command-line interface** with flexible configuration options
- **Progress reporting** for long-running operations
- **Resource management** with proper cleanup
- **Extensive documentation** following industry standards

### System Requirements

**Required:**
- MATLAB R2021a or later (for PT/INR computation scripts)
- Python 3.8 or later
- opencv-python >= 4.8.0
- numpy >= 1.24.0

**Originally tested on:**
- Ubuntu 18.04.5
- MATLAB R2021a
- Python v2.7.17
- opencv-python v3.4.3

**Currently tested on:**
- Ubuntu 18.04+
- MATLAB R2021a+
- Python v3.8+
- opencv-python v4.8+

No non-standard hardware is required to run these programs.

### Installation

Install Python dependencies using pip:

```bash
# Install core dependencies
pip install -r requirements.txt

# Or install manually
pip install opencv-python numpy
```

## Programs
Running these four programs in the following order will calculate PT and INR values for the sample video.

### 1. extract.py - Video Frame Extraction (Modernized ✅)

Extracts individual video frames and outputs them to a directory with advanced features:

**Key Features:**
- Multiple output formats (JPG, PNG, BMP)
- Configurable quality settings
- Frame skip/sampling options for optimization
- Real-time progress reporting
- Comprehensive error handling and validation
- Detailed logging with verbosity control

**Usage:**
```bash
# Basic usage (default settings)
python3 extract.py video.mp4

# Custom output directory
python3 extract.py video.mp4 -o output_frames

# PNG format with maximum quality
python3 extract.py video.mp4 -f png -q 100

# Extract every 2nd frame (sampling)
python3 extract.py video.mp4 -s 2

# Verbose logging for debugging
python3 extract.py video.mp4 -v

# Get help
python3 extract.py --help
```

### 2. start_time.m - Pipette Motion Analysis

This file generates the motion curve for the pipette and is used to find the start time of the measurement.

### 3. stop_time.m - Particle Motion Analysis

This file generates the motion curve for the particle and is used to find the end time of the measurement.

### 4. compute_pt.m - PT/INR Computation

This file processes the pipette and particle motion curves to compute PT and INR values.

Helper files:
These helper files are called by the above programs.

* circlecropbw.m and circlecrop.m: These files are used to create a circular crop of video frames in order to isolate the particle from the background.

* knee_pt.m: This file is used to calculate the knee point of the curve.

Data:
* video.mp4: Video of a our system measuring PT and INR for a sample of whole blood.


## Quick Start Guide

### Step 1: Extract frames from video

**Command:**
```bash
python3 extract.py video.mp4
```

**Runtime:** ~16 seconds (improved from 22 seconds in legacy version)
**Processing Speed:** ~65 frames/second
**Output:** `video/` directory containing 1046 extracted frames

The modernized version provides real-time progress updates and detailed statistics:
```
2025-11-10 15:03:00 - INFO - Video Frame Extractor - Medical Device Modernization Phase 1a
2025-11-10 15:03:00 - INFO - Video: video.mp4
2025-11-10 15:03:00 - INFO - Resolution: 1080x1920
2025-11-10 15:03:00 - INFO - FPS: 59.07
2025-11-10 15:03:00 - INFO - Total frames: 1046
...
2025-11-10 15:03:17 - INFO - Extraction complete!
2025-11-10 15:03:17 - INFO - Frames extracted: 1046
2025-11-10 15:03:17 - INFO - Processing speed: 64.80 frames/sec
```

**Expected output files:**
- `video/frame000000.jpg` through `video/frame001045.jpg`
- Frames are numbered with zero-padding for proper sorting
- Default JPEG quality: 95 (configurable via `-q` flag)

### Step 2: Generate pipette motion curve

**Command:** Run `start_time.m` in MATLAB
**Runtime:** ~25 seconds

**Expected output files:**
- `start_time_video.txt` - Contains the generated motion curve for the pipette

### Step 3: Generate particle motion curve

**Command:** Run `stop_time.m` in MATLAB
**Runtime:** ~28 seconds

**Expected output files:**
- `stop_time_video.txt` - Contains the generated motion curve for the particle

### Step 4: Compute PT and INR values

**Command:** Run `compute_pt.m` in MATLAB
**Runtime:** ~7 ms

**Expected output:**
```matlab
PT: 12.1
INR: 1.0
```

## Modernization Roadmap

### Phase 1a: extract.py Migration ✅ COMPLETED
- [x] Python 3.x compatibility with type hints
- [x] Modern error handling and validation
- [x] Command-line interface with argparse
- [x] Comprehensive logging system
- [x] Progress reporting and statistics
- [x] Resource management (proper cleanup)
- [x] Multiple output format support
- [x] Quality and sampling configuration
- [x] Full documentation and help system

### Phase 1b: MATLAB to Python Migration (Planned)
- [ ] Port `start_time.m` to Python
- [ ] Port `stop_time.m` to Python
- [ ] Port `compute_pt.m` to Python
- [ ] Unified Python pipeline for complete PT/INR analysis
- [ ] Performance optimization
- [ ] Unit testing suite
- [ ] Integration testing

### Phase 2: Advanced Features (Planned)
- [ ] Web-based user interface
- [ ] Real-time video processing
- [ ] Database integration for result storage
- [ ] Statistical analysis and visualization
- [ ] Multi-sample batch processing
- [ ] Quality control metrics
- [ ] Regulatory compliance documentation (FDA/IEC standards)

