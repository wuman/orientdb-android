/*
 * Copyright 1999-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orientechnologies.common.comparator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Comparator;

/**
 * Comparator for fast byte arrays comparison using {@link Unsafe} class. Bytes are compared like unsigned not like signed bytes.
 * 
 * 
 * @author Andrey Lomakin
 * @since 08.07.12
 */
@SuppressWarnings("restriction")
public class OUnsafeByteArrayComparator implements Comparator<byte[]> {
  public static final OUnsafeByteArrayComparator INSTANCE     = new OUnsafeByteArrayComparator();

  private static final Object                    unsafe;
  private static Class<?>                        unsafeClass;
  private static final Method                    unsafeGetLongMethod;
  
  private static final int                       BYTE_ARRAY_OFFSET;
  private static final boolean                   littleEndian = ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN);

  private static final int                       LONG_SIZE    = Long.SIZE / Byte.SIZE;

  static {
    unsafe = AccessController.doPrivileged(new PrivilegedAction<Object>() {
      public Object run() {
        try {
          unsafeClass = Class.forName("sun.misc.Unsafe");
          Field f = unsafeClass.getDeclaredField("theUnsafe");
          f.setAccessible(true);
          return f.get(null);
        } catch (ClassNotFoundException e) {
          throw new Error();
        } catch (NoSuchFieldException e) {
          throw new Error();
        } catch (IllegalAccessException e) {
          throw new Error();
        }
      }
    });

    try {
        Method arrayBaseOffsetMethod = unsafeClass.getMethod("arrayBaseOffset", new Class[] { Class.class });
        BYTE_ARRAY_OFFSET = (Integer) arrayBaseOffsetMethod.invoke(unsafe, new Object[] { byte[].class });

        Method arrayIndexScaleMethod = unsafeClass.getMethod("arrayIndexScale", new Class[] { Class.class });
        final int byteArrayScale = (Integer) arrayIndexScaleMethod.invoke(unsafe, new Object[] { byte[].class });

        if (byteArrayScale != 1) {
            throw new Error();
        }
        
        unsafeGetLongMethod = unsafeClass.getMethod("getLong", new Class[] { Object.class, Long.TYPE });
    } catch (NoSuchMethodException e) {
        throw new Error();
    } catch (IllegalArgumentException e) {
        throw new Error();
    } catch (IllegalAccessException e) {
        throw new Error();
    } catch (InvocationTargetException e) {
        throw new Error();
    }
  }
  
  public int compare(byte[] arrayOne, byte[] arrayTwo) {
    if (arrayOne.length > arrayTwo.length)
      return 1;

    if (arrayOne.length < arrayTwo.length)
      return -1;

    final int WORDS = arrayOne.length / LONG_SIZE;

    for (int i = 0; i < WORDS * LONG_SIZE; i += LONG_SIZE) {
      final long index = i + BYTE_ARRAY_OFFSET;

      try {
          final long wOne = (Long) unsafeGetLongMethod.invoke(unsafe, new Object[] { arrayOne, Integer.valueOf((int) index) });
          final long wTwo = (Long) unsafeGetLongMethod.invoke(unsafe, new Object[] { arrayTwo, Integer.valueOf((int) index) });

          if (wOne == wTwo) {
              continue;
          }

          if (littleEndian) {
            return lessThanUnsigned(Long.reverseBytes(wOne), Long.reverseBytes(wTwo)) ? -1 : 1;
          }

          return lessThanUnsigned(wOne, wTwo) ? -1 : 1;
      } catch ( InvocationTargetException e ) {
          return 0;
      } catch (IllegalArgumentException e) {
          return 0;
      } catch (IllegalAccessException e) {
          return 0;
      }
    }

    for (int i = WORDS * LONG_SIZE; i < arrayOne.length; i++) {
      int diff = compareUnsignedByte(arrayOne[i], arrayTwo[i]);
      if (diff != 0)
        return diff;
    }

    return 0;
  }

  private static boolean lessThanUnsigned(long longOne, long longTwo) {
    return (longOne + Long.MIN_VALUE) < (longTwo + Long.MIN_VALUE);
  }

  private static int compareUnsignedByte(byte byteOne, byte byteTwo) {
    final int valOne = byteOne & 0xFF;
    final int valTwo = byteTwo & 0xFF;
    return valOne - valTwo;
  }
}
