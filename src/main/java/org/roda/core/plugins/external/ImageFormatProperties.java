package org.roda.core.plugins.external;

public record ImageFormatProperties(
        int maximumBitDepth,
        Boolean supportsAlpha
) {
}
