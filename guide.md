# Guide to using Item Blacklist

## `itemblacklist.json`

The config file (located under `serverconfig/itemblacklist.json`) provides
a list of the banned items and defined groups. An example structure is the 
following:

<details><summary>JSON Example</summary>

```json
{
  "items": [
    {
      "item_predicate": {
        "items": "minecraft:lingering_potion",
        "components": {
          "minecraft:potion_contents": {
            "potion": "minecraft:harming"
          }
        }
      },
      "groups": [
        "default"
      ]
    },
    {
      "item_predicate": {
        "items": "minecraft:rabbit"
      },
      "groups": [
        "red"
      ]
    }
  ],
  "groups": [
    {
      "name": "default",
      "properties": {}
    },
    {
      "name": "red",
      "properties": {
        "teams": [
          "red"
        ],
        "op_level_min": 3,
        "usernames": [
          "Dev"
        ]
      }
    }
  ]
}
```

</details>

We can define a default config that gets copied to every world that we
create. Put this default `itemblacklist.json` in your `defaultconfigs`
folder.

By default, we have no banned items and only one group `default`.
This `default` group will have all it's properties empty. This means
that it contains every player. See the section on groups for the 
different properties and their logic.

### `"items": [...]`

This section defines the item predicates for banned items. 
Each JSON Object in the array contains `item_predicate` and `groups`.
`groups` defines the groups that the ban will apply to while `item_predicate`
is a vanilla Minecraft `item_predicate` usually used for advancements and
other data driven things. You can see an example for the advancement
trigger [item_durability_changed](https://minecraft.wiki/w/Advancement_definition#minecraft:item_durability_changed).
Note that this applies to 1.21 versions and up. `item_predicate` has slightly different syntax in 1.20.1, so
you will have to do your own research.

### `"groups": [...]`

This section defines each group that we use in the item predicates. Each group
has a `name` and a set of `properties`. Each player gets checked against the 
properties to see if they belong in the group. If none are defined, then every player
belongs to the group.

<details><summary>All Properties</summary>

- `op_level_min`(Integer): Player has permission level of at least this value
- `op_level_max`(Integer): Player has permission level at most this value
- `usernames`(List of strings): Players with these usernames
- `usernames_blacklist`(List of strings): Never include these players
- `teams`(List of strings): Include players on these teams
- `teams_blacklist`(List of strings): Never include players on these teams

</details>

The properties work as follows:  
Players are kicked out if they belong to `usernames_blacklist`, `teams_blacklist`, 
or their permission level isn't within the bounds. Otherwise, the player must match both of the
`teams` and `usernames` categories. If any property is omitted, it is simply skipped. 
This means if you wanted to give a specific player a ban, you can set the group up to be
```json
"properties": {
  "username": ["mike192"]
}
```

but if you add the field for `teams`
```json
"properties": {
  "username": ["mike192"],
  "teams": ["red_team", "blue_team"]
}
```

Then `mike192` will only be included if they are in `red_team` or `blue_team`.

Furthermore, the following will only include `mike192` if they are not a server operator
```json
"properties": {
  "username": ["mike192"],
  "op_level_max": 0
}
```

If we wanted to have a `mike192` and `red_team` rather than `mike192` only when
they're in the `red_team`, then we simply make two separate teams and list both
of them in the `"items": {"groups": ["a", "b,], ...}`.

## Commands

This is a list of the commands for this mod. If you can't do something with commands,
you can likely do it inside the `itemblacklist.json` file instead. 

Every command has `/itemblacklist` as the beginning token.

```
ban [item] [group](optional) 
#Makes an entry for the item and lists it. 
#Sensitive to data components (such as enchantments)

ban hand [group](optional)
#Makes an entry for the item in your main hand and lists it. 
#Sensitive to data components (such as enchantments)

unban [item]
#Removes every entry that tests true for the itemstack

list
#Prints all entries into chat

recalculate
#Recalculate what groups players belong to
#Needed if players change group status (ie. changed teams or are now OPs)
```
