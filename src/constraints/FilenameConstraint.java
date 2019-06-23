package constraints;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilenameConstraint implements Constraint<String> {
    private final Pattern filenamePattern = Pattern.compile("(?<name>.*)\\.(?<ext>[^.]*$)");
    private final String nameSubstring;
    private final String[] includedExt, excludedExt;
    private final boolean caseInsensitive;

    private FilenameConstraint(String nameSubstring, String[] includedExt, String[] excludedExt, boolean caseInsensitive) {
        this.nameSubstring = nameSubstring;
        this.includedExt = includedExt;
        this.excludedExt = excludedExt;
        this.caseInsensitive = caseInsensitive;
    }

    public static class Builder {
        private String nameSubstring;
        private String[] includedExt, excludedExt;
        private boolean caseInsensitive;

        public Builder caseInsensitive(boolean caseInsensitive) {
            this.caseInsensitive = caseInsensitive;
            return this;
        }
        public Builder includeExtensions(String[] includedExt) {
            this.includedExt = includedExt;
            return this;
        }
        public Builder excludeExtensions(String[] excludedExt) {
            this.excludedExt = excludedExt;
            return this;
        }
        public Builder contains(String nameSubstring) {
            this.nameSubstring = nameSubstring;
            return this;
        }
        public FilenameConstraint build() {
            return new FilenameConstraint(nameSubstring, includedExt, excludedExt, caseInsensitive);
        }
    }

    private boolean hasExtensionConstraints() {
        return (includedExt != null && includedExt.length > 0) || (excludedExt != null && excludedExt.length > 0);
    }

    private boolean hasNameConstraints() {
        return nameSubstring != null;
    }

    @Override
    public boolean satisfyConstraint(File file) {
        if (!hasNameConstraints() && !hasExtensionConstraints()) {
            return true;
        }
        Matcher matcher = filenamePattern.matcher(file.getName());
        if (!matcher.find()) {
            return false;
        }
        var name = matcher.group("name");
        var ext = matcher.group("ext");
        var satisfyNameConstraints = !hasNameConstraints() || (caseInsensitive ? name.toLowerCase().contains(nameSubstring.toLowerCase()): name.contains(nameSubstring));
        var satisfyExtConstraints = !hasExtensionConstraints() || (
                (excludedExt == null || Arrays.stream(excludedExt).noneMatch(ee -> caseInsensitive ? ee.equalsIgnoreCase(ext) : ee.equals(ext))) &&
                (includedExt == null || Arrays.stream(includedExt).anyMatch(ie -> caseInsensitive ? ie.equalsIgnoreCase(ext) : ie.equals(ext)))
        );
        return satisfyNameConstraints && satisfyExtConstraints;
    }
}
