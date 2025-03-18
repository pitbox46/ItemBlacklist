- Add optional field, `"ban_nonplayer"`, to the item predicate entries. 
If `false`, items on the ground or in inventories won't be banned. 
Defaults to `true`
- Internal code refactoring
- Add optional field, `"game_types"`, to group entries.
Must be a list of game modes to check for. Ignored if it isn't 
declared.
- Add simple version of ban command