/*
 * Copyright 2007 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing;

import java.util.List;

/**
 * 包装了一种提示信息，用于传递给barcodeReader帮助它更快或者更加精确地解码
 * 
 * Encapsulates a type of hint that a caller may pass to a barcode reader to help it
 * more quickly or accurately decode it. It is up to implementations to decide what,
 * if anything, to do with the information that is supplied.
 *
 * @author Sean Owen
 * @author dswitkin@google.com (Daniel Switkin)
 * @see Reader#decode(BinaryBitmap,java.util.Map)
 */
public enum DecodeHintType {

  /**
   * Unspecified, application-specific hint. Maps to an unspecified {@link Object}.
   */
 OTHER(Object.class),

  /**
   * 单色的barcode
   * Image is a pure monochrome image of a barcode. Doesn't matter what it maps to;
   * use {@link Boolean#TRUE}.
   */
  PURE_BARCODE(Void.class),

  /**
   * 
   * Image is known to be of one of a few possible formats.
   * Maps to a {@link List} of {@link BarcodeFormat}s.
   */
  POSSIBLE_FORMATS(List.class),

  /**
   * Spend more time to try to find a barcode; optimize for accuracy, not speed.
   * Doesn't matter what it maps to; use {@link Boolean#TRUE}.
   */
  TRY_HARDER(Void.class),

  /**
   * Specifies what character encoding to use when decoding, where applicable (type String)
   */
  CHARACTER_SET(String.class),

  /**
   * Allowed lengths of encoded data -- reject anything else. Maps to an {@code int[]}.
   */
  ALLOWED_LENGTHS(int[].class),

  /**
   * Assume Code 39 codes employ a check digit. Doesn't matter what it maps to;
   * use {@link Boolean#TRUE}.
   */
  ASSUME_CODE_39_CHECK_DIGIT(Void.class),

  /**
   * Assume the barcode is being processed as a GS1 barcode, and modify behavior as needed.
   * For example this affects FNC1 handling for Code 128 (aka GS1-128). Doesn't matter what it maps to;
   * use {@link Boolean#TRUE}.
   */
  ASSUME_GS1(Void.class),

  /**
   * If true, return the start and end digits in a Codabar barcode instead of stripping them. They
   * are alpha, whereas the rest are numeric. By default, they are stripped, but this causes them
   * to not be. Doesn't matter what it maps to; use {@link Boolean#TRUE}.
   */
  RETURN_CODABAR_START_END(Void.class),

  /**
   * The caller needs to be notified via callback when a possible {@link ResultPoint}
   * is found. Maps to a {@link ResultPointCallback}.
   */
  NEED_RESULT_POINT_CALLBACK(ResultPointCallback.class),

  // End of enumeration values.
  ;

  /**
   * Data type the hint is expecting.
   * Among the possible values the {@link Void} stands out as being used for
   * hints that do not expect a value to be supplied (flag hints). Such hints
   * will possibly have their value ignored, or replaced by a
   * {@link Boolean#TRUE}. Hint suppliers should probably use
   * {@link Boolean#TRUE} as directed by the actual hint documentation.
   */
  private final Class<?> valueType;

  DecodeHintType(Class<?> valueType) {
    this.valueType = valueType;
  }
  
  public Class<?> getValueType() {
    return valueType;
  }

}
