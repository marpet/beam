package org.esa.beam.dataio.geotiff;

/**
 * Created by IntelliJ IDEA.
 * User: marco
 * Date: 09.02.2005
 * Time: 09:04:55
 */

import junit.framework.TestCase;

public class TiffDirectoryEntrySetTest extends TestCase {

    public void testCreation() {
        final TiffDirectoryEntrySet entrySet = new TiffDirectoryEntrySet();

        final TiffDirectoryEntry[] entries = entrySet.getEntries();
        assertNotNull(entries);
        assertEquals(0, entries.length);
    }

    public void testSetEntry() {
        final TiffDirectoryEntrySet entrySet = new TiffDirectoryEntrySet();
        final TiffDirectoryEntry entry = new TiffDirectoryEntry(new TiffShort(45), new TiffLong(45678));

        entrySet.set(entry);

        final TiffDirectoryEntry[] entries = entrySet.getEntries();
        assertEquals(1, entries.length);
        assertSame(entry, entries[0]);
    }

    public void testSetEntry_ReplaceExistingEntry() {
        final TiffDirectoryEntrySet entrySet = new TiffDirectoryEntrySet();
        final TiffDirectoryEntry entry = new TiffDirectoryEntry(new TiffShort(45), new TiffLong(45678));
        final TiffDirectoryEntry entry2 = new TiffDirectoryEntry(new TiffShort(45), new TiffLong(4234));

        entrySet.set(entry);
        entrySet.set(entry2);

        final TiffDirectoryEntry[] entries = entrySet.getEntries();
        assertEquals(1, entries.length);
        assertSame(entry2, entries[0]);
    }

    public void testSetEntry_OrderEntries() {
        final TiffDirectoryEntrySet entrySet = new TiffDirectoryEntrySet();
        final TiffDirectoryEntry entry = new TiffDirectoryEntry(new TiffShort(45), new TiffLong(45678));
        final TiffDirectoryEntry entry2 = new TiffDirectoryEntry(new TiffShort(43), new TiffLong(4234));

        entrySet.set(entry);
        entrySet.set(entry2);

        final TiffDirectoryEntry[] entries = entrySet.getEntries();
        assertEquals(2, entries.length);
        assertSame(entry2, entries[0]);
        assertSame(entry, entries[1]);
    }

    public void testSetEntry_ReplaceAndOrderEntries() {
        final TiffDirectoryEntrySet entrySet = new TiffDirectoryEntrySet();
        final TiffDirectoryEntry entry1 = new TiffDirectoryEntry(new TiffShort(45), new TiffLong(45678));
        final TiffDirectoryEntry entry2 = new TiffDirectoryEntry(new TiffShort(43), new TiffLong(4234));
        final TiffDirectoryEntry entry3 = new TiffDirectoryEntry(new TiffShort(45), new TiffLong(42634));

        entrySet.set(entry1);
        entrySet.set(entry2);
        entrySet.set(entry3);

        final TiffDirectoryEntry[] entries = entrySet.getEntries();
        assertEquals(2, entries.length);
        assertSame(entry2, entries[0]);
        assertSame(entry3, entries[1]);
    }

    public void testGetEntry() {
        final TiffDirectoryEntrySet entrySet = new TiffDirectoryEntrySet();
        final TiffShort tag1 = new TiffShort(45);
        final TiffDirectoryEntry entry1 = new TiffDirectoryEntry(tag1, new TiffLong(45678));
        final TiffShort tag2 = new TiffShort(60);
        final TiffDirectoryEntry entry2 = new TiffDirectoryEntry(tag2, new TiffLong(4234));
        final TiffShort tag3 = new TiffShort(44);
        final TiffDirectoryEntry entry3 = new TiffDirectoryEntry(tag3, new TiffLong(42634));
        final TiffShort noEntryTag = new TiffShort(66);

        entrySet.set(entry1);
        entrySet.set(entry2);
        entrySet.set(entry3);

        assertEquals(entry1, entrySet.getEntry(tag1));
        assertEquals(entry2, entrySet.getEntry(tag2));
        assertEquals(entry3, entrySet.getEntry(tag3));
        assertNull(entrySet.getEntry(noEntryTag));
    }
}