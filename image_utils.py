#!/usr/bin/env python3
"""
Image Processing Utilities for Blood Coagulation INR Analysis

This module provides circular masking and image processing functions
for isolating regions of interest in PT/INR video analysis.

Direct port from MATLAB functions:
- circlecrop.m → circle_crop()
- circlecropbw.m → circle_crop_bw()

Medical Device Modernization - Phase 1b
CRITICAL: Exact algorithm port - NO optimizations
"""

from typing import Tuple
import numpy as np
import cv2


def circle_crop(
    img: np.ndarray,
    cx: int,
    cy: int,
    cr: int,
    invert: bool = False
) -> Tuple[np.ndarray, np.ndarray]:
    """
    Apply circular mask to RGB image.

    Direct port of circlecrop.m (MATLAB).

    Args:
        img: Input RGB image (H x W x 3)
        cx: Circle center X coordinate
        cy: Circle center Y coordinate
        cr: Circle radius
        invert: If True, mask outside circle; if False, mask inside

    Returns:
        Tuple of (masked_image, mask)
        - masked_image: RGB image with circular mask applied
        - mask: Boolean mask array

    Algorithm:
        1. Create zero-initialized output image
        2. Generate circular mask using distance from center
        3. Apply mask to each RGB channel
        4. Return masked image and mask

    MATLAB equivalent:
        [out,mask]=circlecrop(img,cx,cy,cr,invert)
    """
    rows, columns = img.shape[:2]

    # Initialize output image with zeros
    rgb_image2 = np.zeros((rows, columns, 3), dtype=np.uint8)

    # Create meshgrid for circular mask
    # MATLAB uses ndgrid, NumPy uses meshgrid with indexing='ij' for same behavior
    y_grid, x_grid = np.meshgrid(
        np.arange(columns) - cx,
        np.arange(rows) - cy,
        indexing='ij'
    )

    # Create circular mask: distance from center < radius
    mask = (x_grid**2 + y_grid**2) < cr**2

    # Invert mask if requested
    if invert:
        mask = ~mask

    # Apply mask to each channel
    for channel in range(3):
        rgb_image2[:, :, channel][mask.T] = img[:, :, channel][mask.T]

    return rgb_image2, mask.T


def circle_crop_bw(
    img: np.ndarray,
    cx: int,
    cy: int,
    cr: int,
    invert: bool = False
) -> Tuple[np.ndarray, np.ndarray]:
    """
    Apply circular mask to grayscale image.

    Direct port of circlecropbw.m (MATLAB).

    Args:
        img: Input grayscale image (H x W)
        cx: Circle center X coordinate
        cy: Circle center Y coordinate
        cr: Circle radius
        invert: If True, mask outside circle; if False, mask inside

    Returns:
        Tuple of (masked_image, mask)
        - masked_image: Grayscale image with circular mask applied
        - mask: Boolean mask array

    Algorithm:
        1. Convert grayscale to RGB (replicate channel 3 times)
        2. Apply circle_crop() to RGB image
        3. Extract single channel from result

    MATLAB equivalent:
        [out,mask]=circlecropbw(img,cx,cy,cr,invert)
    """
    # Convert grayscale to RGB by replicating channels
    # MATLAB: img2=uint8(cat(3,img,img,img))
    img2 = np.stack([img, img, img], axis=2).astype(np.uint8)

    # Apply RGB circular crop
    out, mask = circle_crop(img2, cx, cy, cr, invert)

    # Extract single channel (all channels are identical)
    # MATLAB: out=out(:,:,1)
    out = out[:, :, 0]

    return out, mask


# Unit tests for validation
if __name__ == '__main__':
    print("Image Utils - Unit Test Suite")
    print("=" * 60)

    # Test 1: Circle crop on synthetic image
    print("\nTest 1: Circle crop on 100x100 synthetic image")
    test_img = np.ones((100, 100, 3), dtype=np.uint8) * 255
    result, mask = circle_crop(test_img, 50, 50, 30, invert=False)

    print(f"  Input shape: {test_img.shape}")
    print(f"  Output shape: {result.shape}")
    print(f"  Mask shape: {mask.shape}")
    print(f"  Masked pixels: {np.sum(mask)}")
    print(f"  Expected ~2827 (π*30²)")

    # Test 2: Circle crop BW
    print("\nTest 2: Circle crop BW on grayscale image")
    test_img_bw = np.ones((100, 100), dtype=np.uint8) * 255
    result_bw, mask_bw = circle_crop_bw(test_img_bw, 50, 50, 30, invert=False)

    print(f"  Input shape: {test_img_bw.shape}")
    print(f"  Output shape: {result_bw.shape}")
    print(f"  Mask shape: {mask_bw.shape}")
    print(f"  Masked pixels: {np.sum(mask_bw)}")

    # Test 3: Invert mask
    print("\nTest 3: Inverted mask")
    result_inv, mask_inv = circle_crop_bw(test_img_bw, 50, 50, 30, invert=True)
    print(f"  Masked pixels (inverted): {np.sum(mask_inv)}")
    print(f"  Expected ~7173 (100²-π*30²)")

    print("\n" + "=" * 60)
    print("✅ All tests completed - Verify against MATLAB output")
