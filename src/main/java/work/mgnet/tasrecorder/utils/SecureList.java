package work.mgnet.tasrecorder.utils;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * List with a set amount of items filled with Byte Buffers
 * @author Pancake
 */
public class SecureList {

	ByteBuffer[] buffers;
	boolean[] locked;
	boolean[] filled;
	
	/**
	 * Creates and fills a new list of ByteBuffers
	 * @param length Amount of Byte Buffers
	 * @param size Length of Byte Buffers
	 */
	public SecureList(int length, int size) {
		// Prepare Array
		buffers = new ByteBuffer[length];
		locked = new boolean[length];
		filled = new boolean[length];
		for (int i = 0; i < buffers.length; i++)  buffers[i] = ByteBuffer.allocateDirect(size);
	}
	
	/**
	 * Tries to find a unlocked and unfilled byte buffer
	 * @return Does a buffer exist
	 */
	public boolean containsUnfilledUnlocked() {
		for (int i = 0; i < locked.length; i++) {
			if (!locked[i] && !filled[i]) return true;
		}
		return false;
	}
	
	/**
	 * Tries to find a unlocked and filled byte buffer
	 * @return Does a buffer exist
	 */
	public boolean containsFilledUnlocked() {
		for (int i = 0; i < locked.length; i++) {
			if (!locked[i] && filled[i]) return true;
		}
		return false;
	}
	
	/**
	 * Tries to find a unlocked and unfilled byte buffer
	 * @return Buffer Index or size of SecureList
	 */
	public int findFilled() {
		int i = 0;
		for (i = 0; i < locked.length; i++) {
			if (!locked[i] && filled[i]) break;
		}
		return i;
	}
	
	/**
	 * Tries to find a unlocked and filled byte buffer
	 * @return Buffer Index or size of SecureList
	 */
	public int findUnfilled() {
		int i = 0;
		for (i = 0; i < locked.length; i++) {
			if (!locked[i] && !filled[i]) break;
		}
		return i;
	}
	
	/**
	 * Locks and updates the state of a Buffer
	 * @param i Index to lock
	 * @param fill Whether fill or not
	 * @return Byte Buffer locked
	 */
	public ByteBuffer getAndLock(int i, boolean fill) {
		if (locked[i]) return null;
		locked[i] = true;
		filled[i] = fill;
		if (fill) buffers[i].clear();
		return buffers[i];
	}
	
	/**
	 * Unlockes a Byte Buffer
	 * @param index Index to Lock
	 */
	public void unlock(int index) {
		locked[index] = false;
	}

	/**
	 * Clears the entire List
	 */
	public void clear() {
		Arrays.fill(locked, false);
		Arrays.fill(filled, false);
		for (int i = 0; i < buffers.length; i++) buffers[i].clear();
	}
	
}
