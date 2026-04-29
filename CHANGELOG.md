# Changelog
## [2.0.0]
### Changed
- Updated parent version from 0.4.1 to 1.0.0-alpha-SNAPSHOT (eterna v1-alpha)
- Removed explicit `roda-core` version declaration, now managed by parent BOM
- Updated `init()`, `beforeAllExecute()`, and `afterAllExecute()` signatures to match eterna v1 Plugin interface
- Translated plugin name and description to Swedish

### Breaking Changes
- Incompatible with eterna 0.x — requires eterna 1.0.0-alpha-SNAPSHOT or later

## [1.1.0]
### Added
- Lossy format normalization with color-space conversions, bit-depth down-sampling with dithering, and alpha channel removal
- Enhanced image handling with explicit loading of first image from multi-image files (animated GIFs, ICO files with different dimensions)
- More lenient image loading and improved error handling
- New ImageFormatProperties record for managing format-specific properties

### Changed
- Refactored image conversion logic into dedicated ImageConverter class
- Renamed ImageConverter class to ImageConverterPlugin to avoid naming conflicts
- Registered additional service providers for JPEG and TIFF image writing capabilities

### Fixed
- Improved handling of multi-image files and edge cases in image loading

## [1.0.0]
### Added
- Initial release of the Image Converter plugin.
- Batch image format conversion (JPG, PNG, TIFF)
- SVG conversion support
- Automatic format detection
- Preservation representation creation
- Comprehensive documentation including test logic and licensing details
- Enhanced license clarity for ETERNA Image Converter Plugin and dependencies

### Changed
- Updated Java version from 17 to 21
- Updated parent version from 0.3.1-dev to 0.4.1
- Updated dependency versions for improved compatibility

### Fixed
- Fixed paths in zip file generation
- Re-enabled tests in CI/CD pipeline
- Fixed version extraction from pom.xml in release workflow
