package org.wolm.catalog;

/**
 * Defines the environment that we are currently rendering. This is a singleton global under the assumption that we
 * won't need multi-threaded rendering.
 * 
 * @author wolm
 */
public class RenderEnvironment {
	static private RenderEnvironment instance;

	/** Minimum level of visibility that should be output */
	private AccessLevel minVisibility;

	private RenderEnvironment() {
		super();
	}

	static public RenderEnvironment instance() {
		if (instance == null) {
			synchronized (RenderEnvironment.class) {
				if (instance == null) instance = new RenderEnvironment();
			}
		}
		return instance;
	}

	public AccessLevel getMinVisibility() {
		return minVisibility;
	}

	public void setMinVisibility(AccessLevel minVisibility) {
		this.minVisibility = minVisibility;
	}

	/**
	 * Determines if the specified item is visible under the current visibilty settings
	 * 
	 * @param itemVisibility Visibility of item in question
	 * @return <code>true</code> if the item is visible (can be displayed), <code>false</code> if the item should be
	 * hidden
	 */
	public boolean isAtLeastVisible(AccessLevel itemVisibility) {
		AccessLevel effectiveItemVisibility = itemVisibility == null ? AccessLevel.PRIVATE : itemVisibility;

		switch (minVisibility) {
		case RAW:
			return true;
		case PRIVATE:
			switch (effectiveItemVisibility) {
			case RAW:
				return false;
			default:
				return true;
			}
		case PROTECTED:
			switch (effectiveItemVisibility) {
			case RAW:
			case PRIVATE:
				return false;
			default:
				return true;
			}
		case PUBLIC:
			switch (effectiveItemVisibility) {
			case RAW:
			case PRIVATE:
			case PROTECTED:
				return false;
			default:
				return true;
			}
		default:
			return false;
		}
	}

	/**
	 * Determines if the specified item is visible exactly at the current visibility setting
	 * 
	 * @param itemVisibility Visibility of item in question
	 * @return <code>true</code> if the item is visible (can be displayed), <code>false</code> if the item should be
	 * hidden
	 */
	public boolean isExactlyVisible(AccessLevel itemVisibility) {
		AccessLevel effectiveItemVisibility = itemVisibility == null ? AccessLevel.PRIVATE : itemVisibility;

		return effectiveItemVisibility == minVisibility;
	}

}
