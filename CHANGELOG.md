# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.0] - 2024-08-09

### Added

- `LocalizedString` - custom Java class used as a type of entity attributes.
- `LocalizedStringDatatype` - a `Datatype` implementation class for `LocalizedString`.
- `LocalizedStringConverter` - a class that converts entity attribute state into database column representation and back again.
- `LocalizedStringEditAction` - that opens a dialog that edits a localized string value represented by the `LocalizedString` datatype.

[unreleased]: https://github.com/glebfox/jmix-localized-string-datatype-addon/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/glebfox/jmix-localized-string-datatype-addon/releases/tag/v1.0.0
