package constraints;

import java.io.File;

public class SizeConstraint implements Constraint<Long> {
    private long minSize, maxSize, exactSize;

    private SizeConstraint(long minSize, long maxSize, long exactSize) {
        this.minSize = minSize == -1 ? minSize : Math.max(0, Math.min(minSize, maxSize == -1 ? Long.MAX_VALUE: maxSize));
        this.maxSize = maxSize == -1 ? maxSize : Math.max(minSize == -1 ? 0: minSize, maxSize);
        this.exactSize = exactSize == -1 ? exactSize : Math.max(0, exactSize);
    }

    public static class Builder {
        private long minSize = -1, maxSize = -1, exactSize = -1;

        public Builder greaterThan(long minSize) {
            this.minSize = minSize;
            return this;
        }
        public Builder lessThan(long maxSize) {
            this.maxSize = maxSize;
            return this;
        }
        public Builder exactly(long exactSize) {
            this.exactSize = exactSize;
            return this;
        }
        public SizeConstraint build() {
            return new SizeConstraint(minSize, maxSize, exactSize);
        }
    }

    @Override
    public boolean satisfyConstraint(File file) {
        final long fileSize = file.length();
        return  (exactSize == -1 || exactSize == fileSize) &&
                (minSize == -1 || minSize < fileSize) &&
                (maxSize == -1 || fileSize < maxSize);
    }
}
