## Vertisy v90/v92 Fork
THIS SOURCE IS WIP, AND NOT ACTIVELY MAINTAINED.
If you want to start using the publically released Vertisy source, the recommendation is to fork from initial check-in.

JDK 21 / MySQL 8.0.32

### Known Issues
* Script based things will not work. The scripting engine code has not been migrated.
* Database queries need to be reviewed. getLong -> getTimestamp().getTime()
