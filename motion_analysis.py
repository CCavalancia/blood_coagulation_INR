#!/usr/bin/env python3
"""
Motion Analysis Utilities for Blood Coagulation INR Analysis

This module provides curve analysis functions for detecting inflection
points in motion curves, critical for identifying coagulation events.

Direct port from MATLAB functions:
- knee_pt.m → find_knee_point()

Medical Device Modernization - Phase 1b
CRITICAL: Exact algorithm port - NO optimizations

Attribution:
Original knee_pt.m by Dmitry Kaplan (2021)
MATLAB Central File Exchange #35094
"""

from typing import Tuple, Optional
import numpy as np


def find_knee_point(
    y: np.ndarray,
    x: Optional[np.ndarray] = None,
    just_return: bool = False
) -> Tuple[float, int]:
    """
    Find the knee (elbow) point of a curve y=f(x).

    Direct port of knee_pt.m (MATLAB). The knee point is where the curve
    transitions from one linear regime to another, critical for detecting
    coagulation onset/completion in PT/INR analysis.

    Algorithm:
        Walks along the curve, fitting two lines at each bisection point:
        - Left line: fits all points to the left
        - Right line: fits all points to the right
        Knee is at the bisection point that minimizes sum of fit errors.

    Args:
        y: Vector of Y values (at least 3 elements)
        x: Optional vector of X values (same length as y).
           If None, uses 1:length(y)
        just_return: If True, returns NaN on error instead of raising exception

    Returns:
        Tuple of (x_at_knee, index_of_knee)
        - x_at_knee: X coordinate at the knee point
        - index_of_knee: Index into original arrays at knee point

    Raises:
        ValueError: If inputs are invalid (when just_return=False)

    MATLAB equivalent:
        [res_x, idx_of_result] = knee_pt(y,x,just_return)

    Reference:
        Dmitry Kaplan (2021). Knee Point
        MATLAB Central File Exchange #35094
    """
    # Constants from MATLAB
    USE_ABSOLUTE_DEV = False  # Use RMS error (quadratic) not absolute

    # Default return values
    res_x = np.nan
    idx_of_result = np.nan

    # Validate: y must be non-empty
    if y is None or len(y) == 0:
        if just_return:
            return res_x, idx_of_result
        raise ValueError('knee_pt: y cannot be an empty vector')

    # Validate: y must be a vector (1-dimensional)
    if y.ndim != 1:
        if just_return:
            return res_x, idx_of_result
        raise ValueError('knee_pt: y must be a vector')

    # Ensure y is a column vector
    y = y.flatten()

    # Create or validate x
    if x is None:
        x = np.arange(1, len(y) + 1, dtype=float)
    else:
        x = x.flatten()

    # Validate: x and y must have same dimensions
    if len(x) != len(y):
        if just_return:
            return res_x, idx_of_result
        raise ValueError('knee_pt: y and x must have the same dimensions')

    # Validate: y must be at least 3 elements
    if len(y) < 3:
        if just_return:
            return res_x, idx_of_result
        raise ValueError('knee_pt: y must be at least 3 elements long')

    # Sort by x if needed
    if np.any(np.diff(x) < 0):
        idx = np.argsort(x)
        y = y[idx]
        x = x[idx]
    else:
        idx = np.arange(len(x))

    # Compute forward (left-of-knee) linear fits
    # For each point i, fit line to points [0...i]
    n = np.arange(1, len(y) + 1, dtype=float)
    sigma_xy = np.cumsum(x * y)
    sigma_x = np.cumsum(x)
    sigma_y = np.cumsum(y)
    sigma_xx = np.cumsum(x * x)

    det = n * sigma_xx - sigma_x * sigma_x
    # Avoid division by zero
    det = np.where(det == 0, np.nan, det)

    mfwd = (n * sigma_xy - sigma_x * sigma_y) / det
    bfwd = -(sigma_x * sigma_xy - sigma_xx * sigma_y) / det

    # Compute backward (right-of-knee) linear fits
    # For each point i, fit line to points [i...end]
    sigma_xy_rev = np.cumsum(x[::-1] * y[::-1])
    sigma_x_rev = np.cumsum(x[::-1])
    sigma_y_rev = np.cumsum(y[::-1])
    sigma_xx_rev = np.cumsum(x[::-1] * x[::-1])

    det_rev = n * sigma_xx_rev - sigma_x_rev * sigma_x_rev
    det_rev = np.where(det_rev == 0, np.nan, det_rev)

    mbck = (n * sigma_xy_rev - sigma_x_rev * sigma_y_rev) / det_rev
    bbck = -(sigma_x_rev * sigma_xy_rev - sigma_xx_rev * sigma_y_rev) / det_rev

    # Reverse backward coefficients
    mbck = mbck[::-1]
    bbck = bbck[::-1]

    # Compute error curve for each potential breakpoint
    error_curve = np.full(len(y), np.nan)

    for breakpt in range(1, len(y) - 1):  # Skip first and last points
        # Forward errors (left of knee)
        delsfwd = (mfwd[breakpt] * x[:breakpt + 1] + bfwd[breakpt]) - y[:breakpt + 1]

        # Backward errors (right of knee)
        delsbck = (mbck[breakpt] * x[breakpt:] + bbck[breakpt]) - y[breakpt:]

        # Compute total error
        if USE_ABSOLUTE_DEV:
            error_curve[breakpt] = np.sum(np.abs(delsfwd)) + np.sum(np.abs(delsbck))
        else:
            # RMS error (quadratic)
            error_curve[breakpt] = np.sqrt(np.sum(delsfwd**2)) + np.sqrt(np.sum(delsbck**2))

    # Find minimum error location
    loc = np.nanargmin(error_curve)
    res_x = x[loc]
    idx_of_result = idx[loc]

    return res_x, int(idx_of_result)


# Unit tests for validation
if __name__ == '__main__':
    print("Motion Analysis - Unit Test Suite")
    print("=" * 60)

    # Test 1: Simple piecewise linear data (known knee)
    print("\nTest 1: Piecewise linear data with known knee at x=7")
    y_test1 = np.array([30, 27, 24, 21, 18, 15, 12, 10, 8, 6, 4, 2, 0])
    res_x, res_idx = find_knee_point(y_test1)
    print(f"  Found knee at index: {res_idx} (expected: 7)")
    print(f"  Found knee at x: {res_x} (expected: 7)")
    print(f"  Status: {'✅ PASS' if res_idx == 7 else '❌ FAIL'}")

    # Test 2: Smooth curve (sine wave)
    print("\nTest 2: Smooth curve (sine, knee ~90)")
    x_test2 = np.linspace(0, np.pi/2, 100)
    y_test2 = np.sin(x_test2)
    res_x2, res_idx2 = find_knee_point(y_test2, x_test2)
    expected_knee = np.pi / 4  # ~0.785, midpoint of sine curve
    print(f"  Found knee at x: {res_x2:.3f} (expected ~0.785)")
    print(f"  Found knee at index: {res_idx2}")
    print(f"  Error from expected: {abs(res_x2 - expected_knee):.3f}")

    # Test 3: Custom x values
    print("\nTest 3: Custom x values (20x multiplier)")
    y_test3 = np.array([30, 27, 24, 21, 18, 15, 12, 10, 8, 6, 4, 2, 0])
    x_test3 = np.arange(1, 14) * 20
    res_x3, res_idx3 = find_knee_point(y_test3, x_test3)
    print(f"  Found knee at x: {res_x3} (expected: 140 = 7*20)")
    print(f"  Found knee at index: {res_idx3} (expected: 7)")
    print(f"  Status: {'✅ PASS' if res_x3 == 140 else '❌ FAIL'}")

    # Test 4: Error handling - empty array
    print("\nTest 4: Error handling (empty array)")
    try:
        res_x4, res_idx4 = find_knee_point(np.array([]), just_return=True)
        print(f"  Returned NaN as expected: {np.isnan(res_x4)}")
        print(f"  Status: {'✅ PASS' if np.isnan(res_x4) else '❌ FAIL'}")
    except Exception as e:
        print(f"  ❌ FAIL: Unexpected exception: {e}")

    # Test 5: Error handling - too short
    print("\nTest 5: Error handling (array too short)")
    try:
        res_x5, res_idx5 = find_knee_point(np.array([1, 2]), just_return=True)
        print(f"  Returned NaN as expected: {np.isnan(res_x5)}")
        print(f"  Status: {'✅ PASS' if np.isnan(res_x5) else '❌ FAIL'}")
    except Exception as e:
        print(f"  ❌ FAIL: Unexpected exception: {e}")

    print("\n" + "=" * 60)
    print("✅ All tests completed - Verify against MATLAB knee_pt.m")
