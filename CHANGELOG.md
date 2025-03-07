- Add several config options
  - Control specific types of banning
  - Control the change an item is checked (for performance)
  - Stop broadcasting the banned items to non-ops
- Add `"ban_nonplayer"` to the item predicate entries. 
If false, items on the ground or in inventories won't be banned. Defaults to true
- Internal code refactoring