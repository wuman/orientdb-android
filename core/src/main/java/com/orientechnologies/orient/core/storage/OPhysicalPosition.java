/*
 * Copyright 2010-2012 Luca Garulli (l.garulli--at--orientechnologies.com)
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
package com.orientechnologies.orient.core.storage;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.orientechnologies.orient.core.exception.OSerializationException;
import com.orientechnologies.orient.core.serialization.OBinaryProtocol;
import com.orientechnologies.orient.core.serialization.OSerializableStream;

public class OPhysicalPosition implements OSerializableStream, Comparable<OPhysicalPosition>, Externalizable {
  // POSITION IN THE CLUSTER
  public long              clusterPosition;
  // ID OF DATA SEGMENT
  public int               dataSegmentId;
  // POSITION OF CHUNK EXPRESSES AS OFFSET IN BYTES INSIDE THE DATA SEGMENT
  public long              dataSegmentPos;
  // TYPE
  public byte              recordType;
  // VERSION
  public int               recordVersion = 0;
  // SIZE IN BYTES OF THE RECORD. USED ONLY IN MEMORY
  public int               recordSize;

  private static final int BINARY_SIZE   = OBinaryProtocol.SIZE_LONG + OBinaryProtocol.SIZE_INT + OBinaryProtocol.SIZE_LONG
                                             + OBinaryProtocol.SIZE_BYTE + OBinaryProtocol.SIZE_INT + OBinaryProtocol.SIZE_INT;

  public OPhysicalPosition() {
  }

  public OPhysicalPosition(final long iClusterPosition) {
    clusterPosition = iClusterPosition;
  }

  public OPhysicalPosition(final int iDataSegmentId, final long iDataSegmentPosition, final byte iRecordType) {
    dataSegmentId = iDataSegmentId;
    dataSegmentPos = iDataSegmentPosition;
    recordType = iRecordType;
  }

  public OPhysicalPosition(final long iClusterPosition, final int iVersion) {
    clusterPosition = iClusterPosition;
    recordVersion = iVersion;
  }

  public void copyTo(final OPhysicalPosition iDest) {
    iDest.clusterPosition = clusterPosition;
    iDest.dataSegmentId = dataSegmentId;
    iDest.dataSegmentPos = dataSegmentPos;
    iDest.recordType = recordType;
    iDest.recordVersion = recordVersion;
    iDest.recordSize = recordSize;
  }

  public void copyFrom(final OPhysicalPosition iSource) {
    iSource.copyTo(this);
  }

  @Override
  public String toString() {
    return "cluster(?:" + clusterPosition + ") data(" + dataSegmentId + ":" + dataSegmentPos + ") record(type:" + recordType
        + " size:" + recordSize + " v:" + recordVersion + ")";
  }

  public OSerializableStream fromStream(final byte[] iStream) throws OSerializationException {
    int pos = 0;

    clusterPosition = OBinaryProtocol.bytes2long(iStream, pos);
    pos += OBinaryProtocol.SIZE_LONG;

    dataSegmentId = OBinaryProtocol.bytes2int(iStream, pos);
    pos += OBinaryProtocol.SIZE_INT;

    dataSegmentPos = OBinaryProtocol.bytes2long(iStream, pos);
    pos += OBinaryProtocol.SIZE_LONG;

    recordType = iStream[pos];
    pos += OBinaryProtocol.SIZE_BYTE;

    recordSize = OBinaryProtocol.bytes2int(iStream, pos);
    pos += OBinaryProtocol.SIZE_INT;

    recordVersion = OBinaryProtocol.bytes2int(iStream, pos);

    return this;
  }

  public byte[] toStream() throws OSerializationException {
    byte[] buffer = new byte[BINARY_SIZE];
    int pos = 0;

    OBinaryProtocol.long2bytes(clusterPosition, buffer, pos);
    pos += OBinaryProtocol.SIZE_LONG;

    OBinaryProtocol.int2bytes(dataSegmentId, buffer, pos);
    pos += OBinaryProtocol.SIZE_INT;

    OBinaryProtocol.long2bytes(dataSegmentPos, buffer, pos);
    pos += OBinaryProtocol.SIZE_LONG;

    buffer[pos] = recordType;
    pos += OBinaryProtocol.SIZE_BYTE;

    OBinaryProtocol.int2bytes(recordSize, buffer, pos);
    pos += OBinaryProtocol.SIZE_INT;

    OBinaryProtocol.int2bytes(recordVersion, buffer, pos);
    return buffer;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof OPhysicalPosition))
      return false;

    final OPhysicalPosition other = (OPhysicalPosition) obj;

    return clusterPosition == other.clusterPosition && recordType == other.recordType && recordVersion == other.recordVersion
        && recordSize == other.recordSize;
  }

  public int compareTo(final OPhysicalPosition iOther) {
    return (int) (dataSegmentPos - iOther.dataSegmentPos);
  }

  public void writeExternal(final ObjectOutput out) throws IOException {
    out.writeLong(clusterPosition);
    out.writeInt(dataSegmentId);
    out.writeLong(dataSegmentPos);
    out.writeByte(recordType);
    out.writeInt(recordSize);
    out.writeInt(recordVersion);
  }

  public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
    clusterPosition = in.readLong();
    dataSegmentId = in.readInt();
    dataSegmentPos = in.readLong();
    recordType = in.readByte();
    recordSize = in.readInt();
    recordVersion = in.readInt();
  }
}
