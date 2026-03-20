package io.github.lennyslounge.aureum;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class FileNamePattern implements FileNamingStrategy {

    private final List<Function<Context, String>> segments;

    public FileNamePattern() {
        this.segments = Collections.emptyList();
    }

    private FileNamePattern(List<Function<Context, String>> segments) {
        this.segments = Collections.unmodifiableList(segments);
    }

    private FileNamePattern append(Function<Context, String> segment) {
        List<Function<Context, String>> newSegments = new ArrayList<>(segments);
        newSegments.add(segment);
        return new FileNamePattern(newSegments);
    }

    public FileNamePattern fixed(String value) {
        return append(ctx -> value);
    }

    public FileNamePattern packageAsPath() {
        return append(ctx -> ctx.getTestMethod().getDeclaringClass().getPackage().getName().replace('.', '/') + "/");
    }

    public FileNamePattern classNameWithPrefix(String prefix) {
        return append(ctx -> prefix + ctx.getTestMethod().getDeclaringClass().getSimpleName());
    }

    public FileNamePattern className() {
        return classNameWithPrefix("");
    }

    public FileNamePattern methodNameWithPrefix(String prefix) {
        return append(ctx -> prefix + ctx.getTestMethod().getName());
    }

    public FileNamePattern methodName() {
        return methodNameWithPrefix("");
    }

    public FileNamePattern verificationNameWithPrefix(String prefix) {
        return append(ctx -> prefix + (ctx.getName() != null ? ctx.getName() : ""));
    }

    public FileNamePattern verificationName() {
        return verificationNameWithPrefix("");
    }

    public FileNamePattern verificationNameWithPrefixIfPresent(String prefix) {
        return append(ctx -> ctx.getName() != null ? (prefix +ctx.getName()) : "");
    }

    public FileNamePattern verificationNameIfPresent() {
        return verificationNameWithPrefixIfPresent("");
    }

    public FileNamePattern roleWithPrefix(String prefix) {
        return append(ctx -> prefix + ctx.getRole().name().toLowerCase());
    }

    public FileNamePattern role() {
        return roleWithPrefix("");
    }

    public FileNamingStrategy fileExtension(String ext) {
        return append(ctx -> "." + ext);
    }

    @Override
    public Path resolve(Context context) {
        String build = segments.stream()
                 .map(segment -> segment.apply(context))
                 .collect(Collectors.joining());
        return Paths.get(build);
    }
}
