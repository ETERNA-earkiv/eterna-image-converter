# ImageConverterPluginTest.java — Test Documentation

## Overview

This test verifies the functionality of the `ImageConverterPlugin` in the ETERNA system. It ensures that image files in various formats can be converted to supported output formats (e.g., `jpg`, `png`, `tiff`) and that the conversion is both complete and correct.

## What Gets Tested

- **AIP and Representation Creation:**
  - The test creates an Archival Information Package (AIP) and a representation containing all sample files from the test corpus.
- **File Ingestion:**
  - All files from the test corpus are ingested into the representation.
- **Conversion for Each Supported Format:**
  - For each supported output format (as reported by the plugin), the test:
    - Excludes files that are already in the target format or are in a list of known unsupported formats.
    - Runs the `ImageConverterPlugin` to convert all eligible files to the target format.
    - Runs the Siegfried plugin to characterize the converted files and populate their format metadata.
    - Validates that the correct number of new preservation representations were created for the conversion.
    - Validates that each converted file:
      - Has the correct extension.
      - Has the correct MIME type (as determined by Siegfried).
      - Is only validated if it was actually converted in this run (not if it was already in the target format).

## What Gets Excluded

- **Configurable Excluded Extensions:**
  - Files with extensions that are configured to be excluded from conversion (e.g., `cur`, `pict`, `ico`, `dds`, `pfm`, `hdr`).
  - The excluded extensions are defined in the `core.tools.image-converter.excludedExtensions` property in the configuration file.
  - This list can be customized by modifying the properties file without changing the test code.
- **Files Already in Target Format:**
  - Files whose original extension matches the current target format are excluded from conversion and validation for that run.

## Test Logic Highlights

- **Configuration-Driven Testing:**
  - The test uses the same configuration properties as the actual plugin, ensuring consistency between test and production behavior.
  - Excluded extensions, supported formats, and other configuration are read from the properties file rather than hardcoded in the test.
- **Representation Counting:**
  - The test only counts new preservation representations created in the current conversion run, avoiding double-counting from previous runs.
- **Validation Filtering:**
  - Only files that were actually converted in the current run (i.e., whose original extension did not match the target format) are validated for correct format and MIME type.
- **Robustness:**
  - The test is robust to the presence of files with the same base name and different extensions, and to the plugin's behavior of copying or converting files.

## Future Improvements & Suggestions

- **Better MIME Type Mapping:**
  - Ensure the plugin and test configuration support robust MIME type to extension mapping for all supported formats.
- **Plugin Conversion Logic:**
  - Improve the plugin to only add converted files to new representations, not all files, to avoid confusion and potential overwrites.
- **Support for More Formats:**
  - Expand the list of supported input and output formats as the plugin's capabilities grow.
- **Unique File Naming:**
  - Consider using unique file names for each sample in the test corpus to make validation and debugging easier.
- **Test for Conversion Failures:**
  - Add explicit checks for files that cannot be converted (e.g., unsupported formats) and ensure the plugin handles these gracefully.
- **Parallelization:**
  - If the plugin supports it, test conversion in parallel to ensure thread safety and performance.
- **Detailed Error Reporting:**
  - Enhance test assertions to provide more context on failures (e.g., which file, what original/converted format, etc.).

## Summary

This test ensures that the `ImageConverterPlugin` correctly converts eligible files to supported formats, excludes unsupported or already-converted files, and validates the results using both extension and MIME type.