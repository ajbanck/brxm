# Community Translations

This project contains translations for all projects that contain texts/labels for the CMS user. There should be 
translations for every label in all supported languages. Changes and additions must be registered in this project. 
When a new release is tagged the pending changes must be resolved so all language packs are complete.

# Adding or removing a new project

After adding or removing a module in this project, make sure to update the cms release pom and add update the 
configuration for the hippo-cms-l10n-maven-plugin.

# Translation tooling commands

To check translation state: mvn hippo-cms-l10n:report

To register a change: mvn hippo-cms-l10n:update-registry