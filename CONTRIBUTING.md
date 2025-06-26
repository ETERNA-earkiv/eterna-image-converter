# Contributing to Image Converter Plugin

Thank you for your interest in contributing to the Image Converter Plugin for ETERNA! This document provides guidelines for contributing to the project.

## Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally
3. **Create a feature branch** for your changes
4. **Make your changes** following the guidelines below
5. **Test your changes** thoroughly
6. **Submit a pull request**

## Development Guidelines

### Code Style

- Follow Java coding conventions
- Use meaningful variable and method names
- Add comments for complex logic
- Keep methods focused and reasonably sized
- Use appropriate access modifiers

### Testing

- **Write tests** for new functionality
- **Update existing tests** when modifying behavior
- **Ensure all tests pass** before submitting
- **Add test documentation** for complex test scenarios

### Configuration Changes

- **Document new properties** in the README.md
- **Update both main and test** configuration files
- **Provide default values** for new properties
- **Consider backward compatibility**

### Documentation

- **Update README.md** for new features or configuration changes
- **Update test documentation** when modifying test behavior
- **Add inline comments** for complex code
- **Update this file** if contributing guidelines change

## Configuration-Driven Development

This project emphasizes configuration-driven development:

- **Avoid hardcoding** values in tests or code
- **Use properties files** for configurable values
- **Make exclusion lists configurable** (like the `excludedExtensions` property)
- **Test with the same configuration** as production

## Pull Request Guidelines

### Before Submitting

1. **Run the full test suite**: `mvn test`
2. **Check code style**: Ensure consistent formatting
3. **Update documentation**: README.md, test docs, etc.
4. **Test with different configurations**: Verify flexibility

### Pull Request Description

Include:
- **Summary** of changes
- **Motivation** for the change
- **Testing approach** used
- **Configuration changes** (if any)
- **Breaking changes** (if any)

### Review Process

- All PRs require review
- Address feedback promptly
- Keep discussions constructive
- Be open to suggestions

## Reporting Issues

When reporting issues:

1. **Use the issue template** if available
2. **Provide clear steps** to reproduce
3. **Include relevant logs** and error messages
4. **Specify your environment** (OS, Java version, etc.)
5. **Check existing issues** before creating new ones

## Questions or Need Help?

- **Open an issue** for questions or discussions
- **Check existing documentation** first
- **Be specific** about what you need help with

## License

By contributing to this project, you agree that your contributions will be licensed under the same license as the project (GNU Lesser General Public License v3).

Thank you for contributing to the Image Converter Plugin! 