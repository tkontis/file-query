package constraints;

import java.io.File;

public interface Constraint<T> {
    boolean satisfyConstraint(File file);
}
