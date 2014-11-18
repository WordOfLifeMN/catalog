package org.wolm.catalog;

public enum AccessLevel {
	RAW, PRIVATE, PROTECTED, PUBLIC;

	public boolean isLessVisibleThan(AccessLevel cutoff) {
		switch (cutoff) {
		case PUBLIC:
			switch (this) {
			case PUBLIC:
				return false;
			default:
				return true;
			}
		case PROTECTED:
			switch (this) {
			case PUBLIC:
			case PROTECTED:
				return false;
			default:
				return true;
			}
		case PRIVATE:
			switch (this) {
			case PUBLIC:
			case PROTECTED:
			case PRIVATE:
				return false;
			default:
				return true;
			}
		case RAW:
			switch (this) {
			case PUBLIC:
			case PROTECTED:
			case PRIVATE:
			case RAW:
				return false;
			default:
				return true;
			}
		default:
			return false;
		}
	}

	public static boolean isLevelLessVisibleThanCutoff(AccessLevel toTest, AccessLevel cutoff) {
		if (toTest == null) {
			if (cutoff == null) return false;
			return true;
		}
		if (cutoff == null) return false;
		return toTest.isLessVisibleThan(cutoff);
	}
}
