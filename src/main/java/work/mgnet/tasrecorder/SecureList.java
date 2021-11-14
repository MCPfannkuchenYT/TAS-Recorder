package work.mgnet.tasrecorder;

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
	
	public SecureList(int length, int size) {
		// Prepare Array
		buffers = new ByteBuffer[length];
		locked = new boolean[length];
		filled = new boolean[length];
		for (int i = 0; i < buffers.length; i++)  buffers[i] = ByteBuffer.allocateDirect(size);
	}
	
	public boolean containsUnfilledUnlocked() {
		for (int i = 0; i < locked.length; i++) {
			if (!locked[i] && !filled[i]) return true;
		}
		return false;
	}
	
	public boolean containsFilledUnlocked() {
		for (int i = 0; i < locked.length; i++) {
			if (!locked[i] && filled[i]) return true;
		}
		return false;
	}
	
	public int findFilled() {
		int i = 0;
		for (i = 0; i < locked.length; i++) {
			if (!locked[i] && filled[i]) break;
		}
		return i;
	}
	
	public int findUnfilled() {
		int i = 0;
		for (i = 0; i < locked.length; i++) {
			if (!locked[i] && !filled[i]) break;
		}
		return i;
	}
	
	public ByteBuffer getAndLock(int i, boolean fill) {
		if (locked[i]) return null;
		locked[i] = true;
		filled[i] = fill;
		if (fill) buffers[i].clear();
		return buffers[i];
	}
	
	public void unlock(int index) {
		locked[index] = false;
	}

	public void clear() {
		Arrays.fill(locked, false);
		Arrays.fill(filled, false);
		for (int i = 0; i < buffers.length; i++) buffers[i].clear();
	}
	
}
