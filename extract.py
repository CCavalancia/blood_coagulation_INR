#!/usr/bin/env python3
"""
Video Frame Extraction Module for Blood Coagulation INR Analysis

This module extracts individual frames from video files for subsequent PT/INR
analysis in medical device applications. Part of the Medical Device Modernization
initiative - Phase 1a.

Author: Medical Device Modernization Team
Date: 2025-11-10
Version: 2.0.0
"""

import argparse
import logging
import os
import sys
import time
from pathlib import Path
from typing import Optional, Tuple

import cv2


class VideoFrameExtractor:
    """
    Extracts frames from video files with progress tracking and error handling.

    This class provides robust video frame extraction capabilities with support
    for various output formats, quality settings, and comprehensive error handling
    suitable for medical device applications.
    """

    def __init__(
        self,
        video_path: str,
        output_dir: str,
        frame_format: str = "jpg",
        quality: int = 95,
        frame_skip: int = 1
    ):
        """
        Initialize the VideoFrameExtractor.

        Args:
            video_path: Path to the input video file
            output_dir: Directory where extracted frames will be saved
            frame_format: Output image format (jpg, png, bmp)
            quality: JPEG quality (1-100), higher is better
            frame_skip: Extract every Nth frame (1 = all frames)

        Raises:
            FileNotFoundError: If video file does not exist
            ValueError: If parameters are invalid
        """
        self.video_path = Path(video_path)
        self.output_dir = Path(output_dir)
        self.frame_format = frame_format.lower()
        self.quality = quality
        self.frame_skip = frame_skip

        # Setup logging
        self.logger = logging.getLogger(__name__)

        # Validate inputs
        self._validate_inputs()

    def _validate_inputs(self) -> None:
        """Validate input parameters."""
        if not self.video_path.exists():
            raise FileNotFoundError(f"Video file not found: {self.video_path}")

        if not self.video_path.is_file():
            raise ValueError(f"Path is not a file: {self.video_path}")

        if self.frame_format not in ['jpg', 'jpeg', 'png', 'bmp']:
            raise ValueError(f"Unsupported format: {self.frame_format}")

        if not 1 <= self.quality <= 100:
            raise ValueError(f"Quality must be 1-100, got: {self.quality}")

        if self.frame_skip < 1:
            raise ValueError(f"Frame skip must be >= 1, got: {self.frame_skip}")

    def _get_video_info(self, vidcap: cv2.VideoCapture) -> Tuple[int, float, int, int]:
        """
        Get video metadata.

        Args:
            vidcap: OpenCV VideoCapture object

        Returns:
            Tuple of (total_frames, fps, width, height)
        """
        total_frames = int(vidcap.get(cv2.CAP_PROP_FRAME_COUNT))
        fps = vidcap.get(cv2.CAP_PROP_FPS)
        width = int(vidcap.get(cv2.CAP_PROP_FRAME_WIDTH))
        height = int(vidcap.get(cv2.CAP_PROP_FRAME_HEIGHT))

        return total_frames, fps, width, height

    def _create_output_directory(self) -> None:
        """Create output directory if it doesn't exist."""
        try:
            self.output_dir.mkdir(parents=True, exist_ok=True)
            self.logger.info(f"Output directory: {self.output_dir}")
        except OSError as e:
            raise OSError(f"Failed to create output directory: {e}")

    def extract(self) -> int:
        """
        Extract frames from the video file.

        Returns:
            Number of frames extracted

        Raises:
            RuntimeError: If video processing fails
        """
        start_time = time.time()

        # Create output directory
        self._create_output_directory()

        # Open video file
        vidcap = cv2.VideoCapture(str(self.video_path))

        if not vidcap.isOpened():
            raise RuntimeError(f"Failed to open video file: {self.video_path}")

        try:
            # Get video information
            total_frames, fps, width, height = self._get_video_info(vidcap)

            self.logger.info(f"Video: {self.video_path.name}")
            self.logger.info(f"Resolution: {width}x{height}")
            self.logger.info(f"FPS: {fps:.2f}")
            self.logger.info(f"Total frames: {total_frames}")
            self.logger.info(f"Extracting every {self.frame_skip} frame(s)")

            # Extract frames
            frame_count = 0
            extracted_count = 0

            # Set JPEG quality parameters
            encode_params = []
            if self.frame_format in ['jpg', 'jpeg']:
                encode_params = [cv2.IMWRITE_JPEG_QUALITY, self.quality]
            elif self.frame_format == 'png':
                # PNG compression level (0-9)
                compression = int((100 - self.quality) / 11)
                encode_params = [cv2.IMWRITE_PNG_COMPRESSION, compression]

            while True:
                success, image = vidcap.read()

                if not success:
                    break

                # Extract frame if it matches skip pattern
                if frame_count % self.frame_skip == 0:
                    output_filename = f"frame{frame_count:06d}.{self.frame_format}"
                    output_path = self.output_dir / output_filename

                    # Write frame with quality settings
                    if encode_params:
                        success_write = cv2.imwrite(
                            str(output_path),
                            image,
                            encode_params
                        )
                    else:
                        success_write = cv2.imwrite(str(output_path), image)

                    if not success_write:
                        self.logger.warning(
                            f"Failed to write frame {frame_count}: {output_path}"
                        )
                    else:
                        extracted_count += 1

                    # Progress reporting
                    if total_frames > 0 and extracted_count % 50 == 0:
                        progress = (frame_count / total_frames) * 100
                        self.logger.info(
                            f"Progress: {progress:.1f}% "
                            f"({extracted_count} frames extracted)"
                        )

                frame_count += 1

            # Calculate statistics
            elapsed_time = time.time() - start_time

            self.logger.info(f"\n{'='*60}")
            self.logger.info(f"Extraction complete!")
            self.logger.info(f"Total frames processed: {frame_count}")
            self.logger.info(f"Frames extracted: {extracted_count}")
            self.logger.info(f"Time elapsed: {elapsed_time:.2f} seconds")
            if elapsed_time > 0:
                self.logger.info(
                    f"Processing speed: {frame_count/elapsed_time:.2f} frames/sec"
                )
            self.logger.info(f"{'='*60}")

            return extracted_count

        except Exception as e:
            self.logger.error(f"Error during frame extraction: {e}")
            raise RuntimeError(f"Frame extraction failed: {e}")

        finally:
            # Always release the video capture object
            vidcap.release()
            self.logger.debug("Video capture released")


def setup_logging(verbose: bool = False) -> None:
    """
    Configure logging for the application.

    Args:
        verbose: Enable verbose (DEBUG level) logging
    """
    log_level = logging.DEBUG if verbose else logging.INFO

    logging.basicConfig(
        level=log_level,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S'
    )


def parse_arguments() -> argparse.Namespace:
    """
    Parse command-line arguments.

    Returns:
        Parsed arguments namespace
    """
    parser = argparse.ArgumentParser(
        description='Extract frames from video files for PT/INR analysis',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  %(prog)s video.mp4                          # Basic extraction
  %(prog)s video.mp4 -o frames                # Custom output directory
  %(prog)s video.mp4 -f png -q 100            # PNG format, max quality
  %(prog)s video.mp4 -s 2                     # Extract every 2nd frame
  %(prog)s video.mp4 -v                       # Verbose logging

Medical Device Modernization - Phase 1a
        """
    )

    parser.add_argument(
        'video_file',
        help='Path to the input video file (e.g., video.mp4)'
    )

    parser.add_argument(
        '-o', '--output',
        default='video',
        help='Output directory for extracted frames (default: video)'
    )

    parser.add_argument(
        '-f', '--format',
        choices=['jpg', 'jpeg', 'png', 'bmp'],
        default='jpg',
        help='Output image format (default: jpg)'
    )

    parser.add_argument(
        '-q', '--quality',
        type=int,
        default=95,
        help='JPEG quality 1-100 (default: 95)'
    )

    parser.add_argument(
        '-s', '--skip',
        type=int,
        default=1,
        help='Extract every Nth frame (default: 1, all frames)'
    )

    parser.add_argument(
        '-v', '--verbose',
        action='store_true',
        help='Enable verbose logging'
    )

    parser.add_argument(
        '--version',
        action='version',
        version='%(prog)s 2.0.0'
    )

    return parser.parse_args()


def main() -> int:
    """
    Main entry point for the frame extraction application.

    Returns:
        Exit code (0 for success, 1 for failure)
    """
    try:
        # Parse command-line arguments
        args = parse_arguments()

        # Setup logging
        setup_logging(verbose=args.verbose)
        logger = logging.getLogger(__name__)

        logger.info("Video Frame Extractor - Medical Device Modernization Phase 1a")
        logger.info(f"Version: 2.0.0")

        # Create extractor and process video
        extractor = VideoFrameExtractor(
            video_path=args.video_file,
            output_dir=args.output,
            frame_format=args.format,
            quality=args.quality,
            frame_skip=args.skip
        )

        # Extract frames
        frame_count = extractor.extract()

        if frame_count == 0:
            logger.warning("No frames were extracted!")
            return 1

        logger.info(f"Successfully extracted {frame_count} frames")
        return 0

    except FileNotFoundError as e:
        print(f"Error: {e}", file=sys.stderr)
        return 1
    except ValueError as e:
        print(f"Error: Invalid parameter - {e}", file=sys.stderr)
        return 1
    except RuntimeError as e:
        print(f"Error: {e}", file=sys.stderr)
        return 1
    except KeyboardInterrupt:
        print("\nOperation cancelled by user", file=sys.stderr)
        return 1
    except Exception as e:
        print(f"Unexpected error: {e}", file=sys.stderr)
        logging.exception("Unexpected error occurred")
        return 1


if __name__ == '__main__':
    sys.exit(main())
